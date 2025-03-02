package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.NoticeCommentDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Notice;
import Hr.Mgr.domain.entity.NoticeComment;
import Hr.Mgr.domain.repository.NoticeCommentRepository;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.NoticeCommentService;
import Hr.Mgr.domain.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeCommentServiceImpl implements NoticeCommentService {

    private final NoticeCommentRepository noticeCommentRepository;


    private final NoticeService noticeService;
    private final EmployeeService employeeService;

    @Override
    @Transactional
    public NoticeCommentDto addComment(NoticeCommentDto noticeCommentDto) {
        Notice notice = noticeService.getNoticeEntityById(noticeCommentDto.getNoticeId());

        Employee author = employeeService.findEmployeeEntityById(noticeCommentDto.getAuthorId());

        NoticeComment newComment = new NoticeComment();
        newComment.setNotice(notice);
        newComment.setAuthor(author);
        newComment.setContent(noticeCommentDto.getContent());

        NoticeComment savedComment = noticeCommentRepository.save(newComment);
        return new NoticeCommentDto(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeCommentDto> getCommentsByNotice(Long noticeId) {
        List<NoticeComment> comments = noticeCommentRepository.findByNoticeId(noticeId);

        return comments.stream()
                .map(NoticeCommentDto::new)
                .toList();
    }

    @Override
    @Transactional
    public NoticeCommentDto updateComment(Long commentId, String content) {
        NoticeComment comment = noticeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        comment.setContent(content);
        return new NoticeCommentDto(noticeCommentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        NoticeComment comment = noticeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

        noticeCommentRepository.delete(comment);
    }
}
