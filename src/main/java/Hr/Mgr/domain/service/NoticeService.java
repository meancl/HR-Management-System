package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.NoticeDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Notice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NoticeService {
    /**
     * 공지사항 생성 (첨부 파일 포함 가능)
     * @param noticeDto 게시글 dto
     * @param files 첨부 파일 ID 리스트 (선택)
     * @return 생성된 NoticeDto
     */
    NoticeDto createNotice(NoticeDto noticeDto, List<MultipartFile> files);

    /**
     * 특정 공지사항 조회
     * @param id 공지사항 ID
     * @return NoticeDto (첨부 파일 포함)
     */
    NoticeDto getNoticeById(Long id);

    Notice getNoticeEntityById(Long id);
    /**
     * 모든 공지사항 조회
     * @return NoticeDto 리스트
     */
    List<NoticeDto> getAllNotices();

    /**
     * 공지사항 삭제
     * @param id 삭제할 공지사항 ID
     */
    void deleteNotice(Long id);

}
