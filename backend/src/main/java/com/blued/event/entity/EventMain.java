package com.blued.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("event_main")
public class EventMain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventNo;
    private String title;
    private String status;
    private Integer level;
    private String attackTypeCategory;
    private String attackType;
    private String attackPhase;
    private String attackDirection;
    private String attackResult;
    private String eventSource;
    private LocalDateTime foundTime;
    private String attackIps;
    private String attackRegion;
    private String attackCountry;
    private String victimIps;
    private String assetName;
    private String impactStatus;
    private String businessImpact;
    private String preliminaryConclusion;
    private String analysisConclusion;
    private Integer resultModified;
    private String modifyReason;
    private Long reporterId;
    private String reporterName;
    private Long ownerId;
    private String ownerName;
    private Long handlerId;
    private String handlerName;
    private LocalDateTime submitTime;
    private LocalDateTime assignTime;
    private LocalDateTime reviewTime;
    private LocalDateTime archivedTime;
    private Integer isImportant;
    private Integer isTimeout;
    private String closeReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
