package com.blued.file;

import com.blued.common.BizException;
import com.blued.event.entity.EventEvidence;
import com.blued.event.mapper.EventEvidenceMapper;
import com.blued.security.LoginUser;
import com.blued.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final EventEvidenceMapper evidenceMapper;

    @Value("${blued.upload.dir:/data/upload}")
    private String uploadDir;

    private static final Set<String> ALLOWED = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "pdf", "txt", "log", "csv",
            "pcap", "pcapng", "zip", "json", "xml", "xlsx", "docx");

    public EventEvidence save(MultipartFile file, Long eventId, Long alarmId, String source) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件为空");
        }
        String name = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) {
            throw new BizException("不支持的文件类型：" + ext);
        }
        String sub = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        File dir = new File(uploadDir, sub);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("创建上传目录失败");
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        File target = new File(dir, stored);
        String md5;
        try (InputStream in = file.getInputStream()) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (DigestInputStream dis = new DigestInputStream(in, md)) {
                java.nio.file.Files.copy(dis, target.toPath());
            }
            md5 = toHex(md.digest());
        } catch (Exception e) {
            throw new BizException("文件保存失败：" + e.getMessage());
        }

        LoginUser u = UserContext.get();
        EventEvidence ev = new EventEvidence();
        ev.setEventId(eventId == null ? 0L : eventId);
        ev.setAlarmId(alarmId);
        ev.setFileName(name);
        ev.setFilePath(sub + "/" + stored);
        ev.setFileSize(file.getSize());
        ev.setMd5(md5);
        ev.setMime(file.getContentType());
        ev.setSource(source == null ? "UPLOAD" : source);
        ev.setUploaderId(u == null ? null : u.getId());
        evidenceMapper.insert(ev);
        return ev;
    }

    public List<EventEvidence> listByEvent(Long eventId) {
        return evidenceMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EventEvidence>()
                .eq(EventEvidence::getEventId, eventId).orderByDesc(EventEvidence::getId));
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
