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
    Resource findFileResourceByName(String fileName);
    FileDto findFileDtoById(Long fileId);
    FileDto findLatestFileDtoByEmployeeId(Long employeeId);
    List<FileDto> findAllFileDtos();
    List<FileDto> findFileDtosByEmployeeId(Long employeeId);
    FileDto updateFile(Long fileId, MultipartFile newFile);
    void deleteFile(Long fileId);
}
