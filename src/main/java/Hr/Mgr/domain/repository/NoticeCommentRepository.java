package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.NoticeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {

    @Query("SELECT nc FROM NoticeComment nc " +
            "JOIN FETCH nc.notice " +
            "WHERE nc.notice.id = :noticeId")
    List<NoticeComment> findByNoticeId(@Param("noticeId") Long noticeId);
}
