package com.blued.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeoutTask {

    private final EventService eventService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 20_000)
    public void run() {
        try {
            int changed = eventService.refreshTimeout();
            if (changed > 0) {
                log.info("超时状态刷新：{} 条事件变更", changed);
            }
        } catch (Exception e) {
            log.warn("超时刷新失败：{}", e.getMessage());
        }
    }
}
