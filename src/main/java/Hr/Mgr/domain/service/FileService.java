package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.entity.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileDto createFile(MultipartFile file, Long uploaderId);
    FileEntity createFileEntity(MultipartFile file, Long uploaderId);
    Resource findFileResourceById(Long fileId);
    FileDto findFileDtoById(Long fileId);
    void deleteFile(Long fileId);
}
