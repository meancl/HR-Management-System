package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.entity.Employee;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileDto uploadFile(MultipartFile file, Long uploaderId);
    Resource downloadFile(Long fileId);
    Resource downloadFileByName(String fileName);
    FileDto listTopFileByEmployeeId(Long employeeId);


    List<FileDto> listFiles();
    List<FileDto> listFilesByEmployee(Long employeeId);
    FileDto updateFile(Long fileId, MultipartFile newFile);
    void deleteFile(Long fileId);
}
