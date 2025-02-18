package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.FileEntity;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.FileRepository;
import Hr.Mgr.domain.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LocalFileServiceImpl implements FileService {

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    private final FileRepository fileRepository;

    private final EmployeeRepository employeeRepository;

    public LocalFileServiceImpl(FileRepository fileRepository, EmployeeRepository employeeRepository) throws IOException {
        this.fileRepository = fileRepository;
        this.employeeRepository = employeeRepository;
        Files.createDirectories(fileStorageLocation); // 폴더 생성
    }

    @Override
    public FileDto uploadFile(MultipartFile file, Long uploaderId) {
        try {

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            boolean fileExists = fileRepository.existsByFileName(fileName);
            if (fileExists) {
                throw new FileAlreadyExistsException("파일이 이미 존재합니다: " + fileName);
            }

            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Employee employeeOrThrow = findEmployeeOrThrow(uploaderId);

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setFilePath(targetLocation.toString());
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setUploadedBy(employeeOrThrow);

            return new FileDto(fileRepository.save(fileEntity));

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }
    }
    private Employee findEmployeeOrThrow(Long uploaderId) {
        return employeeRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("No employee found with ID: " + uploaderId));
    }

    @Override
    public Resource downloadFile(Long fileId) {
        return getResource(fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없음")));
    }

    @Override
    public Resource downloadFileByName(String fileName) {
        FileEntity fileEntity = fileRepository.findByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없음: " + fileName));
        return getResource(fileEntity);
    }

    @Override
    public FileDto listTopFileByEmployeeId(Long employeeId) {
        return fileRepository.findTopByUploadedBy_IdOrderByUploadedAtDesc(employeeId)
                .map(FileDto::new)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없음 by :" + employeeId));
    }

    private Resource getResource(FileEntity fileEntity) {
        try {
            Path filePath = Paths.get(fileEntity.getFilePath()).normalize();
            return new UrlResource(filePath.toUri());
        } catch (IOException e) {
            throw new RuntimeException("파일 다운로드 중 오류 발생", e);
        }
    }

    @Override
    public List<FileDto> listFiles() {
        return fileRepository.findAll().stream()
                .map(FileDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileDto> listFilesByEmployee(Long employeeId) {
        return fileRepository.findByUploadedBy_Id(employeeId).stream()
                .map(FileDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public FileDto updateFile(Long fileId, MultipartFile newFile) {
        try {
            FileEntity fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없음"));

            // 기존 파일 삭제
            Path oldFilePath = Paths.get(fileEntity.getFilePath()).normalize();
            Files.deleteIfExists(oldFilePath);

            // 새 파일 저장
            String newFileName = StringUtils.cleanPath(newFile.getOriginalFilename());
            Path newTargetLocation = fileStorageLocation.resolve(newFileName);
            Files.copy(newFile.getInputStream(), newTargetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 파일 정보 업데이트
            fileEntity.setFileName(newFileName);
            fileEntity.setFilePath(newTargetLocation.toString());
            fileEntity.setFileType(newFile.getContentType());
            fileEntity.setFileSize(newFile.getSize());

            fileRepository.save(fileEntity);
            return new FileDto(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("파일 업데이트 중 오류 발생", e);
        }
    }

    @Override
    public void deleteFile(Long fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없음"));

        // 실제 파일 삭제
        Path filePath = Paths.get(fileEntity.getFilePath()).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류 발생", e);
        }

        // DB에서 메타데이터 삭제
        fileRepository.deleteById(fileId);
    }
}
