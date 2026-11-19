package com.blued.event.dto;

import com.blued.event.entity.EventAlarm;
import com.blued.event.entity.EventDisposal;
import com.blued.event.entity.EventEvidence;
import com.blued.event.entity.EventFlow;
import com.blued.event.entity.EventMain;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class EventVo {

    @Data
    public static class ListItem {
        private Long id;
        private String eventNo;
        private String title;
        private String status;
        private String statusName;
        private Integer level;
        private String levelName;
        private String attackTypeCategory;
        private String attackTypeCategoryName;
        private String attackType;
        private String attackTypeName;
        private String attackDirection;
        private String attackDirectionName;
        private String attackResult;
        private String attackResultName;
        private String attackIps;
        private String victimIps;
        private String deviceType;
        private String deviceTypeName;
        private String deviceName;
        private Integer alarmCount;
        private String assetName;
        private Integer isTimeout;
        private Integer isImportant;
        private LocalDateTime foundTime;
        private String reporterName;
        private String handlerName;
    }

    @Data
    public static class Detail {
        private EventMain main;
        private List<EventAlarm> alarms;
        private List<EventEvidence> evidences;
        private List<EventDisposal> disposals;
        private List<EventFlow> flows;
        private Map<String, String> categoryNames;
        private Map<String, String> typeNames;
        private Map<String, String> deviceNames;
        private Map<String, String> directionNames;
        private Map<String, String> resultNames;
        private Map<String, String> levelNames;
        private Map<String, String> disposalActionNames;
        private Map<String, String> execDeviceNames;
        private Map<String, String> verifyResultNames;
        private Map<String, String> statusNames;
    }
}
