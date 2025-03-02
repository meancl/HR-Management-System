package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Notice;
import Hr.Mgr.domain.entity.FileEntity;
import Hr.Mgr.domain.entity.NoticeComment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;

    private List<FileDto> attachedFiles = new ArrayList<>();
    private List<NoticeCommentDto> noticeCommentDtoList = new ArrayList<>();

    public NoticeDto(){}
    public NoticeDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.authorId = notice.getAuthor().getId();
        this.authorName = notice.getAuthor().getName();
        this.createdAt = notice.getCreatedAt();
    }
}
