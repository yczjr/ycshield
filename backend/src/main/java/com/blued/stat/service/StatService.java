package com.blued.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blued.common.DictCache;
import com.blued.common.EventStatus;
import com.blued.event.entity.EventDisposal;
import com.blued.event.entity.EventMain;
import com.blued.event.mapper.EventDisposalMapper;
import com.blued.event.mapper.EventMainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatService {

    private final EventMainMapper mainMapper;
    private final EventDisposalMapper disposalMapper;
    private final DictCache dictCache;

    public Map<String, Object> overview() {
        Map<String, Object> m = new HashMap<>();
        m.put("today", mainMapper.selectCount(new LambdaQueryWrapper<EventMain>()
                .ge(EventMain::getCreateTime, LocalDate.now().atStartOfDay())));
        m.put("total", mainMapper.selectCount(null));
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (EventStatus s : EventStatus.values()) {
            byStatus.put(s.name(), mainMapper.selectCount(new LambdaQueryWrapper<EventMain>()
                    .eq(EventMain::getStatus, s.name())));
        }
        m.put("byStatus", byStatus);
        m.put("timeout", mainMapper.selectCount(new LambdaQueryWrapper<EventMain>().eq(EventMain::getIsTimeout, 1)));
        m.put("byLevel", groupCount("level"));
        m.put("byDirection", groupCount("attack_direction"));
        m.put("byResult", groupCount("attack_result"));
        m.put("byCategory", groupCount("attack_type_category"));
        m.put("disposalCount", disposalMapper.selectCount(null));
        return m;
    }

    /** 态势大屏数据 */
    public Map<String, Object> dashboard() {
        List<EventMain> all = mainMapper.selectList(new LambdaQueryWrapper<EventMain>()
                .select(EventMain::getId, EventMain::getEventNo, EventMain::getTitle, EventMain::getStatus,
                        EventMain::getLevel, EventMain::getAttackDirection, EventMain::getAttackResult,
                        EventMain::getAttackTypeCategory, EventMain::getAttackType, EventMain::getAttackIps,
                        EventMain::getVictimIps, EventMain::getAssetName, EventMain::getFoundTime,
                        EventMain::getCreateTime, EventMain::getIsTimeout,
                        EventMain::getAttackRegion, EventMain::getAttackCountry));

        Map<String, Object> m = new HashMap<>();
        m.put("total", (long) all.size());
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        m.put("today", all.stream().filter(e -> e.getCreateTime() != null
                && e.getCreateTime().isAfter(dayStart)).count());
        m.put("week", all.stream().filter(e -> e.getCreateTime() != null
                && e.getCreateTime().isAfter(dayStart.minusDays(6))).count());
        m.put("timeout", all.stream().filter(e -> Integer.valueOf(1).equals(e.getIsTimeout())).count());
        m.put("high", all.stream().filter(e -> Integer.valueOf(1).equals(e.getLevel())).count());
        m.put("archived", all.stream().filter(e -> EventStatus.ARCHIVED.name().equals(e.getStatus())
                || EventStatus.CLOSED.name().equals(e.getStatus())).count());
        m.put("pending", all.stream().filter(e -> !EventStatus.ARCHIVED.name().equals(e.getStatus())
                && !EventStatus.CLOSED.name().equals(e.getStatus())).count());

        Map<String, Long> statusCounter = new LinkedHashMap<>();
        for (EventStatus s : EventStatus.values()) {
            statusCounter.put(s.name(), 0L);
        }
        for (EventMain e : all) {
            statusCounter.merge(e.getStatus() == null ? "UNKNOWN" : e.getStatus(), 1L, Long::sum);
        }
        List<Map<String, Object>> statusList = new ArrayList<>();
        statusCounter.forEach((k, v) -> {
            Map<String, Object> it = new LinkedHashMap<>();
            it.put("name", EventStatus.labelOf(k));
            it.put("code", k);
            it.put("value", v);
            statusList.add(it);
        });
        m.put("byStatus", statusList);

        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 13; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime from = d.atStartOfDay();
            LocalDateTime to = from.plusDays(1);
            Map<String, Object> it = new LinkedHashMap<>();
            it.put("date", d.format(fmt));
            it.put("count", all.stream().filter(e -> e.getCreateTime() != null
                    && !e.getCreateTime().isBefore(from) && e.getCreateTime().isBefore(to)).count());
            trend.add(it);
        }
        m.put("trend", trend);

        m.put("byLevel", namedCount(all, "level"));
        m.put("byDirection", namedCount(all, "attack_direction"));
        m.put("byResult", namedCount(all, "attack_result"));
        m.put("byCategory", namedCount(all, "attack_type_category"));

        Map<String, Long> ipCounter = new LinkedHashMap<>();
        for (EventMain e : all) {
            if (e.getAttackIps() == null || e.getAttackIps().isBlank()) {
                continue;
            }
            for (String ip : e.getAttackIps().split(",")) {
                String k = ip.trim();
                if (!k.isEmpty()) {
                    ipCounter.merge(k, 1L, Long::sum);
                }
            }
        }
        m.put("topAttackIps", topN(ipCounter, 10));

        // 攻击源地域（国内省份 / 境外国家）
        Map<String, Long> regionCounter = new LinkedHashMap<>();
        Map<String, Long> countryCounter = new LinkedHashMap<>();
        for (EventMain e : all) {
            if (e.getAttackRegion() == null || e.getAttackRegion().isBlank()) {
                continue;
            }
            regionCounter.merge(e.getAttackRegion(), 1L, Long::sum);
            String c = e.getAttackCountry();
            if (c != null && !c.isBlank() && !"中国".equals(c) && !"未知".equals(c)) {
                countryCounter.merge(c, 1L, Long::sum);
            }
        }
        m.put("byRegion", topN(regionCounter, 40));
        m.put("byCountry", topN(countryCounter, 10));

        Map<String, Long> assetCounter = new LinkedHashMap<>();
        for (EventMain e : all) {
            if (e.getAssetName() != null && !e.getAssetName().isBlank()) {
                assetCounter.merge(e.getAssetName().trim(), 1L, Long::sum);
            }
        }
        m.put("topAssets", topN(assetCounter, 8));

        List<Map<String, Object>> actionList = new ArrayList<>();
        Map<String, Long> actionCounter = new LinkedHashMap<>();
        for (EventDisposal d : disposalMapper.selectList(null)) {
            if (d.getActions() == null) {
                continue;
            }
            for (String a : d.getActions().split(",")) {
                String k = a.trim();
                if (!k.isEmpty()) {
                    actionCounter.merge(k, 1L, Long::sum);
                }
            }
        }
        actionCounter.forEach((k, v) -> {
            Map<String, Object> it = new LinkedHashMap<>();
            it.put("name", dictCache.name("disposal_action", k));
            it.put("code", k);
            it.put("value", v);
            actionList.add(it);
        });
        m.put("byAction", actionList.stream()
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .collect(Collectors.toList()));

        List<Map<String, Object>> recent = all.stream()
                .sorted((a, b) -> {
                    LocalDateTime x = a.getCreateTime() == null ? LocalDateTime.MIN : a.getCreateTime();
                    LocalDateTime y = b.getCreateTime() == null ? LocalDateTime.MIN : b.getCreateTime();
                    return y.compareTo(x);
                })
                .limit(10)
                .map(e -> {
                    Map<String, Object> it = new LinkedHashMap<>();
                    it.put("id", e.getId());
                    it.put("eventNo", e.getEventNo());
                    it.put("title", e.getTitle());
                    it.put("level", e.getLevel());
                    it.put("levelName", e.getLevel() == null ? "-"
                            : e.getLevel() == 1 ? "高危" : e.getLevel() == 2 ? "中危" : "低危");
                    it.put("statusName", EventStatus.labelOf(e.getStatus()));
                    it.put("attackTypeName", dictCache.name("attack_type", e.getAttackType()));
                    it.put("foundTime", e.getFoundTime() == null ? "-"
                            : e.getFoundTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
                    return it;
                })
                .collect(Collectors.toList());
        m.put("recent", recent);
        return m;
    }

    private List<Map<String, Object>> namedCount(List<EventMain> all, String column) {
        Map<String, Long> counter = new LinkedHashMap<>();
        for (EventMain e : all) {
            String key = switch (column) {
                case "level" -> e.getLevel() == null ? "未知"
                        : e.getLevel() == 1 ? "高危" : e.getLevel() == 2 ? "中危" : "低危";
                case "attack_direction" -> dictCache.name("attack_direction", e.getAttackDirection());
                case "attack_result" -> dictCache.name("attack_result", e.getAttackResult());
                case "attack_type_category" -> dictCache.name("attack_type_category", e.getAttackTypeCategory());
                default -> "未知";
            };
            if (key == null) {
                key = "未知";
            }
            counter.merge(key, 1L, Long::sum);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        counter.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", k);
            item.put("value", v);
            out.add(item);
        });
        return out.stream().sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> topN(Map<String, Long> counter, int n) {
        List<Map<String, Object>> out = new ArrayList<>();
        counter.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", k);
            item.put("value", v);
            out.add(item);
        });
        return out.stream()
                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .limit(n)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> groupCount(String column) {
        List<EventMain> all = mainMapper.selectList(new LambdaQueryWrapper<EventMain>()
                .select(EventMain::getId, EventMain::getLevel, EventMain::getAttackDirection,
                        EventMain::getAttackResult, EventMain::getAttackTypeCategory));
        Map<String, Long> counter = new LinkedHashMap<>();
        for (EventMain e : all) {
            String key = switch (column) {
                case "level" -> e.getLevel() == null ? "未知" : String.valueOf(e.getLevel());
                case "attack_direction" -> e.getAttackDirection();
                case "attack_result" -> e.getAttackResult();
                case "attack_type_category" -> e.getAttackTypeCategory();
                default -> "未知";
            };
            if (key == null) {
                key = "UNKNOWN";
            }
            counter.merge(key, 1L, Long::sum);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        counter.forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", k);
            item.put("value", v);
            out.add(item);
        });
        return out.stream().sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                .collect(Collectors.toList());
    }
}
