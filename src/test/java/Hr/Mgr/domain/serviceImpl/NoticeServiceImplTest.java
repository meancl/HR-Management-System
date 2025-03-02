package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.NoticeDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.FileEntity;
import Hr.Mgr.domain.entity.Notice;
import Hr.Mgr.domain.entity.NoticeFile;
import Hr.Mgr.domain.repository.NoticeFileRepository;
import Hr.Mgr.domain.repository.NoticeRepository;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.FileService;
import Hr.Mgr.domain.service.NoticeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@Transactional
@Rollback(false)
class NoticeServiceImplTest {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private NoticeFileRepository noticeFileRepository;

    @Autowired
    private FileService fileService;
    @Autowired
    private NoticeService noticeService;
    private Long employeeId;

    @BeforeEach
    void beforeEach(){
        EmployeeReqDto employeeReqDto = new EmployeeReqDto();
        employeeReqDto.setName("민재");
        employeeReqDto.setEmail("sbe03253@naver.com");
        employeeReqDto.setPassword(bCryptPasswordEncoder.encode("passwd1234"));
        employeeReqDto.setAge(30);

        employeeId =  employeeService.createEmployee(employeeReqDto);
    }

    @Test
    void createNotice() {

        // Given
        NoticeDto noticeDto = new NoticeDto();
        noticeDto.setAuthorId(employeeId);
        noticeDto.setTitle("제목1");
        noticeDto.setContent("내용1");


        List<MultipartFile> mockFiles = List.of(
                new MockMultipartFile("file1.txt", "file1.txt", "text/plain", "File 1 Content".getBytes()),
                new MockMultipartFile("file2.txt", "file2.txt", "text/plain", "File 2 Content".getBytes())
        );

        NoticeDto notice = noticeService.createNotice(noticeDto, mockFiles);


        // Then
        // 파일이 저장되었는지 확인
        assertEquals(2, notice.getAttachedFiles().size());

        // 실제로 저장된 NoticeFile이 있는지 확인
        List<NoticeFile> savedNoticeFiles = noticeFileRepository.findAll();
        assertEquals(2, savedNoticeFiles.size());

    }

    @Test
    void getNoticeById() {
    }

    @Test
    void getAllNotices() {
    }

    @Test
    void deleteNotice() {
    }
}