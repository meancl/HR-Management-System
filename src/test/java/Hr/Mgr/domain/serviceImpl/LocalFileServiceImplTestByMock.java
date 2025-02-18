package Hr.Mgr.domain.serviceImpl;


import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.FileEntity;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class LocalFileServiceImplTestByMock {


    @Mock
    private FileRepository fileRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private LocalFileServiceImpl fileService; // ✅ Mock 객체가 주입됨

    @BeforeEach
    void setUp() {
    }

    @Test
    void uploadFile_Success() throws IOException {
        // Given
        Long uploaderId = 1L;
        String fileName = "test.txt";

        Employee employee = new Employee();
        employee.setId(uploaderId);
        employee.setHashedPwd("passwd1234");
        employee.setName("민재");
        employee.setAge(30);
        employee.setEmail("sbe03253@naver.com");

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setFilePath("uploads/test.txt");
        fileEntity.setUploadedBy(employee);
        fileEntity.setFileSize(1024L);
        fileEntity.setId(1L);
        fileEntity.setFileType("sdg");


        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(fileRepository.existsByFileName(fileName)).thenReturn(false);

        // 🔥 `anyLong()` 사용하여 Mock 설정 문제 해결
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));

        // When
        FileDto result = fileService.uploadFile(multipartFile, uploaderId);

        // Then
        assertNotNull(result);
        assertEquals(fileName, result.getFileName());

        verify(fileRepository).existsByFileName(fileName);
        verify(employeeRepository).findById(uploaderId);
        verify(fileRepository).save(any(FileEntity.class));
    }


}
