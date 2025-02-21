package Hr.Mgr.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 게시글 제목

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Employee author; // 글 작성자 (직원)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 게시글 내용

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 작성 날짜
}
