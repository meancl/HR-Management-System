package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.NoticeCommentDto;

import java.util.List;

public interface NoticeCommentService {
    // 댓글 작성
    NoticeCommentDto addComment(NoticeCommentDto noticeCommentDto);

    // 특정 공지사항의 모든 댓글 조회
    List<NoticeCommentDto> getCommentsByNotice(Long noticeId);

    // 댓글 수정
    NoticeCommentDto updateComment(Long commentId, String content);

    // 댓글 삭제
    void deleteComment(Long commentId);
}
