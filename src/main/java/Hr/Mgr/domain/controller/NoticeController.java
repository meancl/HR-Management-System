package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.NoticeCommentDto;
import Hr.Mgr.domain.dto.NoticeDto;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.NoticeCommentService;
import Hr.Mgr.domain.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeCommentService noticeCommentService;
    private final EmployeeService employeeService;

    // 게시글 목록 조회
    @GetMapping
    public String listNotices(Model model) {
        List<NoticeDto> notices = noticeService.getAllNotices();
        model.addAttribute("notices", notices);
        return "/notice/listNotices"; // listNotices.html
    }
    // 🔹 새 게시글 작성 폼 (작성자 목록 불러오기)
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("notice", new NoticeDto());
        model.addAttribute("employees", employeeService.findAllEmployeeDtos());
        return "/notice/addNotice";
    }

    // 🔹 새 게시글 저장 처리
    @PostMapping("/new")
    public String createNotice(@ModelAttribute NoticeDto noticeDto,
                               @RequestParam("files") List<MultipartFile> files) {
        if(files.size() == 1 && files.get(0).getOriginalFilename().isEmpty())
            files = null;
        noticeService.createNotice(noticeDto, files);
        return "redirect:/notices"; // ✅ 저장 후 게시글 목록으로 이동
    }
    // 게시글 상세 조회
    @GetMapping("/{id}")
    public String viewNotice(@PathVariable(name = "id") Long id, Model model) {
        NoticeDto notice = noticeService.getNoticeById(id);
        List<NoticeCommentDto> comments = noticeCommentService.getCommentsByNotice(id);

        model.addAttribute("notice", notice);
        model.addAttribute("comments", comments);

        return "/notice/viewNotice"; // viewNotice.html
    }

    // 댓글 작성
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam String content) {
        NoticeCommentDto commentDto = new NoticeCommentDto();
        commentDto.setNoticeId(id);
        commentDto.setAuthorId(1L); // TODO: 로그인 유저 정보로 변경
        commentDto.setContent(content);

        noticeCommentService.addComment(commentDto);
        return "redirect:/notices/" + id;
    }
}
