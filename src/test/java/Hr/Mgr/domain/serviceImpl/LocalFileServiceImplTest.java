package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LocalFileServiceImplTest {

    @Autowired
    private FileService fileService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Long employeeId;

    @BeforeEach
    void createEmployee(){
        EmployeeReqDto employeeReqDto = new EmployeeReqDto();
        employeeReqDto.setName("민재");
        employeeReqDto.setEmail("sbe03253@naver.com");
        employeeReqDto.setPassword(bCryptPasswordEncoder.encode("passwd1234"));
        employeeReqDto.setAge(30);

        employeeId =  employeeService.createEmployee(employeeReqDto);
    }

    @Test
    void uploadFile() {
        // 1. 실제 파일 데이터 생성
        byte[] fileContent = "This is a test file content.".getBytes(); // 파일 내용
        String fileName = "testFile.txt"; // 파일 이름
        String contentType = "text/plain"; // MIME 타입

        // 2. MockMultipartFile 객체 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", fileName, contentType, fileContent);

        // 3. 테스트 대상 메서드 호출 (예: 파일 업로드)
        FileDto uploadedFileDto = fileService.uploadFile(mockFile, employeeId);

        // 4. 업로드된 파일 데이터 검증
        assertNotNull(uploadedFileDto);
        assertEquals(fileName, uploadedFileDto.getFileName());
        assertTrue(Files.exists(Path.of(uploadedFileDto.getFilePath())));
    }

    @Test
    void downloadFile() throws IOException {
        byte[] fileContent = "This is a test file content.".getBytes(); // 파일 내용
        String fileName = "testFile.txt"; // 파일 이름
        String contentType = "text/plain"; // MIME 타입

        // 2. MockMultipartFile 객체 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", fileName, contentType, fileContent);

        // 3. 테스트 대상 메서드 호출 (예: 파일 업로드)
        FileDto fileDto = fileService.uploadFile(mockFile, employeeId);
        Resource resource1 = fileService.downloadFile(fileDto.getId());

        assertThat(resource1.getFilename()).isEqualTo(fileName);

    }

    @Test
    void downloadFileByName() {
    }

    @Test
    void listFiles() {
        byte[] fileContent = "This is a test file content.".getBytes(); // 파일 내용
        String fileName = "testFile.txt"; // 파일 이름
        String contentType = "text/plain"; // MIME 타입

        // 2. MockMultipartFile 객체 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", fileName, contentType, fileContent);

        // 3. 테스트 대상 메서드 호출 (예: 파일 업로드)
        FileDto fileDto = fileService.uploadFile(mockFile, employeeId);
        List<FileDto> fileDtos = fileService.listFiles();

        assertThat(fileDtos.size()).isEqualTo(1);
    }

    @Test
    void listFilesByEmployee() {
    }

    @Test
    void updateFile() {
        byte[] fileContent = "This is a test file content.".getBytes(); // 파일 내용
        String fileName = "testFile.txt"; // 파일 이름
        String contentType = "text/plain"; // MIME 타입

        // 2. MockMultipartFile 객체 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", fileName, contentType, fileContent);

        // 3. 테스트 대상 메서드 호출 (예: 파일 업로드)
        FileDto fileDto = fileService.uploadFile(mockFile, employeeId);

        byte[] updateFileContent = "This is a test file content. but it is updated".getBytes(); // 파일 내용
        String updateFileName = "testFileUpdated.txt"; // 파일 이름
        String updateContentType = "text/plain"; // MIME 타입

        // 2. MockMultipartFile 객체 생성
        MockMultipartFile updateMockFile = new MockMultipartFile(
                "file", updateFileName, updateContentType, updateFileContent);

        FileDto fileDtoUpdated = fileService.updateFile(fileDto.getId(), updateMockFile);

        assertThat(fileDtoUpdated.getId()).isEqualTo(fileDto.getId());
        assertThat(fileDtoUpdated.getFileName()).isNotEqualTo(fileDto.getFileName());
    }

    @Test
    void deleteFile() {
        byte[] fileContent = "This is a test file content.".getBytes(); // 파일 내용
        String fileName = "testFile.txt"; // 파일 이름
        String contentType = "text/plain"; // MIME 타입

        // 2. MockMultipartFile 객체 생성
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", fileName, contentType, fileContent);

        // 3. 테스트 대상 메서드 호출 (예: 파일 업로드)
        FileDto fileDto = fileService.uploadFile(mockFile, employeeId);
        fileService.deleteFile(fileDto.getId());

        assertThrows(RuntimeException.class,
                () -> fileService.listTopFileByEmployeeId(employeeId));
    }
}