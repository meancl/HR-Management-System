package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.NoticeComment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class NoticeCommentDto {
    private Long id;
    private Long noticeId;
    private String content;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자 (Entity → DTO 변환)
    public NoticeCommentDto(NoticeComment comment) {
        this.id = comment.getId();
        this.noticeId = comment.getNotice().getId();
        this.content = comment.getContent();
        this.authorId = comment.getAuthor().getId();
        this.authorName = comment.getAuthor().getName();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
