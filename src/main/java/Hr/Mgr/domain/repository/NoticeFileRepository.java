package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {

    @Query("SELECT nf FROM NoticeFile nf " +
            "JOIN FETCH nf.file " +
            "JOIN FETCH nf.notice " +
            "WHERE nf.notice.id = :noticeId")
    List<NoticeFile> findByNoticeId(@Param("noticeId") Long NoticeId);
    void deleteByNoticeId(Long noticeId);
}
