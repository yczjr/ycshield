package com.blued.common;

import com.blued.system.entity.SysDictItem;
import com.blued.system.mapper.SysDictItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DictCache {

    private final SysDictItemMapper dictItemMapper;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();
    private static final long TTL = 30_000L;

    private record Cached(List<SysDictItem> items, long time) {
    }

    public List<SysDictItem> list(String dictType) {
        Cached c = cache.get(dictType);
        long now = System.currentTimeMillis();
        if (c != null && now - c.time < TTL) {
            return c.items;
        }
        List<SysDictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictType, dictType)
                .eq(SysDictItem::getEnabled, 1)
                .orderByAsc(SysDictItem::getSort));
        cache.put(dictType, new Cached(items, now));
        return items;
    }

    public Map<String, String> nameMap(String dictType) {
        return list(dictType).stream()
                .collect(Collectors.toMap(SysDictItem::getCode, SysDictItem::getName, (a, b) -> a, LinkedHashMap::new));
    }

    public String name(String dictType, String code) {
        if (code == null) {
            return null;
        }
        return nameMap(dictType).getOrDefault(code, code);
    }

    public void evict(String dictType) {
        if (dictType == null) {
            cache.clear();
        } else {
            cache.remove(dictType);
        }
    }
}
