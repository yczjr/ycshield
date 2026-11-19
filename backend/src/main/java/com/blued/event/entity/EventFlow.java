package com.blued.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_flow")
public class EventFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private String fromStatus;
    private String toStatus;
    private String action;
    private String opinion;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime operateTime;
}
