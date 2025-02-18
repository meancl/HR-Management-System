package Hr.Mgr.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName; // 파일명

    @Column(nullable = false)
    private String filePath; // 실제 저장된 경로

    @Column(nullable = false)
    private String fileType; // MIME 타입 (예: application/pdf)

    @Column(nullable = false)
    private Long fileSize; // 파일 크기

    @ManyToOne
    @JoinColumn(name = "uploadedBy", nullable = false)
    private Employee uploadedBy; // 업로드한 직원 정보

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
