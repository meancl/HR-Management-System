package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.FileEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FileDto {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Long uploadedById;
    private LocalDateTime uploadedAt;

    public FileDto(FileEntity fileEntity) {
        this.id = fileEntity.getId();
        this.fileName = fileEntity.getFileName();
        this.filePath = fileEntity.getFilePath();
        this.fileType = fileEntity.getFileType();
        this.fileSize = fileEntity.getFileSize();
        this.uploadedById = fileEntity.getUploadedBy().getId();
        this.uploadedAt = fileEntity.getUploadedAt();
    }
}
