package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.NoticeCommentDto;
import Hr.Mgr.domain.dto.NoticeDto;
import Hr.Mgr.domain.repository.NoticeCommentRepository;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.NoticeCommentService;
import Hr.Mgr.domain.service.NoticeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NoticeCommentServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private NoticeCommentService noticeCommentService;

    private Long employeeId;
    private NoticeDto notice;

    @BeforeEach
    void beforeEach()
    {
        EmployeeReqDto employeeReqDto = new EmployeeReqDto();
        employeeReqDto.setName("민재");
        employeeReqDto.setEmail("sbe03253@naver.com");
        employeeReqDto.setPassword(bCryptPasswordEncoder.encode("passwd1234"));
        employeeReqDto.setAge(30);
        employeeId =  employeeService.createEmployee(employeeReqDto);

        NoticeDto noticeDto = new NoticeDto();
        noticeDto.setAuthorId(employeeId);
        noticeDto.setTitle("제목1");
        noticeDto.setContent("내용1");
        notice = noticeService.createNotice(noticeDto, null);
    }


    @Test
    void addComment() {

        NoticeCommentDto noticeCommentDto = new NoticeCommentDto();
        noticeCommentDto.setNoticeId(notice.getId());
        noticeCommentDto.setContent("댓글1");
        noticeCommentDto.setAuthorId(employeeId);

        NoticeCommentDto noticeCommentDto1 = noticeCommentService.addComment(noticeCommentDto);
        assertThat(noticeCommentDto1.getAuthorName()).isEqualTo("민재");
        assertThat(noticeCommentDto1.getId()).isGreaterThan(0L);
    }

    @Test
    void getCommentsByNotice() {
        NoticeCommentDto noticeCommentDto1 = new NoticeCommentDto();
        noticeCommentDto1.setNoticeId(notice.getId());
        noticeCommentDto1.setContent("댓글1");
        noticeCommentDto1.setAuthorId(employeeId);

        noticeCommentDto1 = noticeCommentService.addComment(noticeCommentDto1);

        NoticeCommentDto noticeCommentDto2 = new NoticeCommentDto();
        noticeCommentDto2.setNoticeId(notice.getId());
        noticeCommentDto2.setContent("댓글2");
        noticeCommentDto2.setAuthorId(employeeId);

        noticeCommentDto2 = noticeCommentService.addComment(noticeCommentDto2);

        List<NoticeCommentDto> commentsByNotice = noticeCommentService.getCommentsByNotice(notice.getId());

        assertThat(commentsByNotice.size()).isEqualTo(2);
    }

    @Test
    void updateComment() {
    }

    @Test
    void deleteComment() {
    }
}