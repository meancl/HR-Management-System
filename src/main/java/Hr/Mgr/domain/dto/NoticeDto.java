package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Notice;
import Hr.Mgr.domain.entity.FileEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private LocalDateTime createdAt;
    private List<FileDto> attachedFiles;

    public NoticeDto(Notice notice, List<FileEntity> fileEntities) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.authorId = notice.getAuthor().getId();
        this.createdAt = notice.getCreatedAt();
        this.attachedFiles = fileEntities.stream()
                .map(FileDto::new)
                .collect(Collectors.toList());
    }
}
