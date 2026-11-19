package com.blued.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_evidence")
public class EventEvidence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private Long alarmId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String md5;
    private String mime;
    private String source;
    private Long uploaderId;
    private LocalDateTime createTime;
    private Integer deleted;
}
