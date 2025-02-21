package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.repository.NoticeFileRepository;
import Hr.Mgr.domain.repository.NoticeRepository;
import Hr.Mgr.domain.service.FileService;
import Hr.Mgr.domain.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final FileService fileService;



}
