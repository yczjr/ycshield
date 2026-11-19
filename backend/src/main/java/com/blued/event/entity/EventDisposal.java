package com.blued.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_disposal")
public class EventDisposal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private String actions;
    private String targets;
    private String execDevice;
    private LocalDateTime execTime;
    private LocalDateTime effectTime;
    private String verifyResult;
    private String remark;
    private Long executorId;
    private String executorName;
    private LocalDateTime createTime;
    private Integer deleted;
}
