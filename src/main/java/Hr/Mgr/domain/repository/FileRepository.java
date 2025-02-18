package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    boolean existsByFileName(String fileName); // 파일명 존재 여부 확인
    Optional<FileEntity> findByFileName(String fileName); // 파일명으로 검색
    List<FileEntity> findByUploadedBy_Id(Long employeeId);
    Optional<FileEntity> findTopByUploadedBy_IdOrderByUploadedAtDesc(Long employeeId);

}
