package com.blued.file;

import com.blued.common.R;
import com.blued.event.entity.EventEvidence;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public R<EventEvidence> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) Long eventId,
                                   @RequestParam(required = false) Long alarmId,
                                   @RequestParam(required = false, defaultValue = "UPLOAD") String source) {
        return R.ok(fileService.save(file, eventId, alarmId, source));
    }

    @GetMapping("/event/{eventId}")
    public R<List<EventEvidence>> list(@PathVariable Long eventId) {
        return R.ok(fileService.listByEvent(eventId));
    }
}
