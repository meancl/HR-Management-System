package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.FileDto;
import Hr.Mgr.domain.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        // 파일 정보 조회
        FileDto fileDto = fileService.findFileDtoById(fileId);
        Resource resource = fileService.findFileResourceById(fileId);

        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        // 파일 이름 인코딩 처리 (한글 깨짐 방지)
        String encodedFileName = new String(fileDto.getFileName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
