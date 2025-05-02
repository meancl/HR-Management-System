package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.dto.NoticeDto;
import Hr.Mgr.domain.entity.Notice;
import Hr.Mgr.domain.entity.FileEntity;
import Hr.Mgr.domain.entity.NoticeFile;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.NoticeRepository;
import Hr.Mgr.domain.repository.NoticeFileRepository;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.FileService;
import Hr.Mgr.domain.service.NoticeService;
import Hr.Mgr.domain.service.NoticePersistenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final EmployeeService employeeService;
    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final FileService fileService;
    private final NoticePersistenceService noticePersistenceService;

    private static final Logger logger = LoggerFactory.getLogger(NoticeService.class);

    @Override
    @Transactional
    public NoticeDto createNotice(NoticeDto noticeDto, List<MultipartFile> files) {
        Employee author = employeeService.findEmployeeEntityById(noticeDto.getAuthorId());

        Notice notice = new Notice();
        notice.setTitle(noticeDto.getTitle());
        notice.setContent(noticeDto.getContent());
        notice.setAuthor(author);

        //        Notice savedNotice = noticeRepository.save(notice);
        // redis cache를 사용하려했는데 db정합성을 위해 비동기문에서 notice자체가 이미 commit되어야
        //        cacheManager.getCache("notice").put(savedNotice.getId(), savedNotice);
        Notice savedNotice = noticePersistenceService.saveNoticeInNewTransaction(notice);
        NoticeDto returnNoticeDto = new NoticeDto(savedNotice);

        if(files != null)
        // 파일 비동기 작업 수행
            uploadFilesAsync(files, returnNoticeDto);
        else
            noticeDto.setAttachedFiles(null);

        return returnNoticeDto;
    }


    // todo. 새로운 스레드는 transaction공유를 못하기에 새로운 transacition을 만들어줘야함
    private void uploadFilesAsync(List<MultipartFile> files, NoticeDto noticeDto) {
        for (MultipartFile file : files) {

            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
            {
                try {
                    uploadEachFile(file, noticeDto);
                } catch (Exception e) {
                    logger.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
                }
            });
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void uploadEachFile(MultipartFile file, NoticeDto noticeDto) {

        FileEntity fileEntity = fileService.createFileEntity(file, noticeDto.getAuthorId());
        Notice noNoticeFound = noticeRepository.findById(noticeDto.getId()).orElseThrow(() -> new IllegalArgumentException("no notice found"));

        NoticeFile noticeFile = new NoticeFile();
        noticeFile.setNotice(noNoticeFound);
        noticeFile.setFile(fileEntity);

        NoticeFile save = noticeFileRepository.save(noticeFile);


        noticeDto.getAttachedFiles().add(new FileDto(fileEntity));
    }
    @Override
    public NoticeDto getNoticeById(Long noticeId) {
        Notice notice = getNoticeEntityById(noticeId);

        // 🔹 NoticeFile을 통해 FileEntity 리스트 조회 (FileService 사용)
        List<FileDto> attachedFiles = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFile::getFile)
                .map(FileDto::new)
                .toList();

        NoticeDto noticeDto = new NoticeDto(notice);
        noticeDto.setAttachedFiles(attachedFiles);

        return noticeDto;
    }

    @Override
    public Notice getNoticeEntityById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
    }

    @Override
    public List<NoticeDto> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(NoticeDto::new).toList();
    }

    @Override
    public void deleteNotice(Long noticeId) {
        // 🔹 해당 공지사항의 모든 NoticeFile 삭제
        // TODO. file 자체들도 삭제할거냐
//        List<NoticeFile> byNoticeId = noticeFileRepository.findByNoticeId(noticeId);
//
//        CompletableFuture.runAsync(() -> {
//            for (NoticeFile noticeFile : byNoticeId) {
//                fileService.deleteFile(noticeId);
//            }
//        });


        noticeFileRepository.deleteByNoticeId(noticeId);

        // 🔹 공지사항 삭제
        noticeRepository.deleteById(noticeId);
    }
}
