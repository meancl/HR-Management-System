package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.NoticeCommentDto;
import Hr.Mgr.domain.dto.NoticeDto;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.NoticeCommentService;
import Hr.Mgr.domain.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeCommentService noticeCommentService;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<NoticeDto>> listNotices() {
        List<NoticeDto> notices = noticeService.getAllNotices();
        return ResponseEntity.ok(notices);
    }

    @PostMapping
    public ResponseEntity<Void> createNotice(@RequestPart NoticeDto noticeDto,
                                             @RequestPart(required = false) List<MultipartFile> files) {
        if(files.size() == 1 && files.get(0).getOriginalFilename().isEmpty())
            files = null;
        noticeService.createNotice(noticeDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeDto> viewNotice(@PathVariable(name = "id") Long id) {
        NoticeDto notice = noticeService.getNoticeById(id);
        return ResponseEntity.ok(notice);
    }

    @PostMapping("/{id}/comments")
    public  ResponseEntity<Void> addComment(@PathVariable Long id,  @RequestBody NoticeCommentDto commentDto) {
        commentDto.setNoticeId(id);
        noticeCommentService.addComment(commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<NoticeCommentDto>> listComments(@PathVariable Long id) {
        List<NoticeCommentDto> comments = noticeCommentService.getCommentsByNotice(id);
        return ResponseEntity.ok(comments);
    }
}
