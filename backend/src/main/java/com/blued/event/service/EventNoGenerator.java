package com.blued.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class EventNoGenerator {

    private final StringRedisTemplate redisTemplate;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String next() {
        String day = LocalDate.now().format(DAY);
        String key = "blued:event:seq:" + day;
        Long seq;
        try {
            seq = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, Duration.ofDays(2));
        } catch (Exception e) {
            seq = System.currentTimeMillis() % 10000;
        }
        if (seq == null) {
            seq = 1L;
        }
        return String.format("BS-%s-%04d", day, seq);
    }
}
