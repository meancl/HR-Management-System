package Hr.Mgr.domain.service;

import Hr.Mgr.domain.entity.Notice;
import Hr.Mgr.domain.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeTransactionService {
    private final NoticeRepository noticeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notice saveNoticeInNewTransaction(Notice notice) {
        return noticeRepository.save(notice);
    }
}
