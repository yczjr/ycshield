package com.blued.common;

public enum EventStatus {
    SUBMITTED("待受理"),
    ANALYZING("分析中"),
    HANDLING("处置中"),
    REVIEWING("待复核"),
    ARCHIVED("已归档"),
    CLOSED("已关闭");

    private final String label;

    EventStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String code) {
        for (EventStatus s : values()) {
            if (s.name().equals(code)) {
                return s.label;
            }
        }
        return code;
    }
}
