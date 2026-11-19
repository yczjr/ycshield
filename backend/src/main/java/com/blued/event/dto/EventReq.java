package com.blued.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class EventReq {

    @Data
    public static class Alarm {
        @NotBlank(message = "来源设备不能为空")
        private String deviceType;
        private String deviceName;
        @NotNull(message = "告警时间不能为空")
        private LocalDateTime alarmTime;
        private String alarmName;
        private String direction;
        private List<String> srcIps;
        private List<String> dstIps;
        private List<String> victimIps;
        private String rawText;
    }

    @Data
    public static class Create {
        private String title;
        @NotNull(message = "发现时间不能为空")
        private LocalDateTime foundTime;
        private String eventSource;
        @NotBlank(message = "攻击类型大类不能为空")
        private String attackTypeCategory;
        @NotBlank(message = "攻击类型子类不能为空")
        private String attackType;
        private String attackPhase;
        @NotBlank(message = "攻击方向不能为空")
        private String attackDirection;
        private List<String> attackIps;
        @NotBlank(message = "攻击成功与否必须选择")
        private String attackResult;
        @NotNull(message = "危害等级不能为空")
        private Integer level;
        @NotBlank(message = "初步研判结论不能为空")
        private String preliminaryConclusion;
        private List<String> victimIps;
        private String assetName;
        private String impactStatus;
        private String businessImpact;
        private Integer isImportant;
        @NotEmpty(message = "至少需要一条告警记录")
        @Valid
        private List<Alarm> alarms;
    }

    @Data
    public static class Query {
        private String keyword;
        private String tab;
        private String status;
        private Integer level;
        private String attackTypeCategory;
        private String attackType;
        private String attackDirection;
        private String attackResult;
        private String deviceType;
        private String attackIp;
        private String victimIp;
        private Long reporterId;
        private String beginTime;
        private String endTime;
        private Integer pageNum = 1;
        private Integer pageSize = 20;
    }

    @Data
    public static class Analyze {
        private String attackResult;
        private Integer level;
        private String attackTypeCategory;
        private String attackType;
        private String attackDirection;
        @NotBlank(message = "研判结论不能为空")
        private String analysisConclusion;
        private String modifyReason;
    }

    /** 研判办结（不需处置，直接归档） */
    @Data
    public static class Finish {
        private String opinion;
    }

    /** 直接封禁 IP 结案（不接入设备，仅记录处置结果） */
    @Data
    public static class BlockIp {
        @NotBlank(message = "封禁对象不能为空")
        private String targets;
        private String opinion;
    }

    @Data
    public static class Assign {
        @NotNull(message = "请选择处置人")
        private Long handlerId;
        private String opinion;
    }

    @Data
    public static class Dispose {
        @NotEmpty(message = "处置动作不能为空")
        private List<String> actions;
        @NotBlank(message = "处置对象不能为空")
        private String targets;
        @NotBlank(message = "执行设备不能为空")
        private String execDevice;
        private LocalDateTime execTime;
        private LocalDateTime effectTime;
        @NotBlank(message = "验证结果不能为空")
        private String verifyResult;
        @NotBlank(message = "处置结果说明不能为空")
        private String remark;
    }

    @Data
    public static class Review {
        @NotNull(message = "请选择复核结果")
        private Boolean pass;
        private String opinion;
    }

    @Data
    public static class Close {
        @NotBlank(message = "关闭原因不能为空")
        private String reason;
    }

    @Data
    public static class DuplicateCheck {
        private List<String> attackIps;
        private List<String> victimIps;
        private LocalDateTime foundTime;
    }
}
