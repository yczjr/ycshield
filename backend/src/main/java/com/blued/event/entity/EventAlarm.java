package com.blued.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_alarm")
public class EventAlarm {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private Integer seq;
    private String deviceType;
    private String deviceName;
    private LocalDateTime alarmTime;
    private String alarmName;
    private String direction;
    private String srcIps;
    private String dstIps;
    private String victimIps;
    private String rawText;
    private Long createBy;
    private LocalDateTime createTime;
}
