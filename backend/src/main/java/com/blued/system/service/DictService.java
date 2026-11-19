package com.blued.system.service;

import com.blued.common.BizException;
import com.blued.common.DictCache;
import com.blued.system.entity.SysDictItem;
import com.blued.system.mapper.SysDictItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DictService {

    private final DictCache dictCache;
    private final SysDictItemMapper dictItemMapper;

    public List<Map<String, Object>> list(String dictType) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysDictItem it : dictCache.list(dictType)) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", it.getCode());
            m.put("name", it.getName());
            m.put("id", it.getId());
            m.put("sort", it.getSort());
            out.add(m);
        }
        return out;
    }

    /** 攻击类型：大类 + 子类树 */
    public List<Map<String, Object>> attackTypeTree() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (SysDictItem cat : dictCache.list("attack_type_category")) {
            Map<String, Object> m = new HashMap<>();
            m.put("code", cat.getCode());
            m.put("name", cat.getName());
            List<Map<String, Object>> children = new ArrayList<>();
            for (SysDictItem sub : dictCache.list("attack_type")) {
                if (cat.getId().equals(sub.getParentId())) {
                    Map<String, Object> c = new HashMap<>();
                    c.put("code", sub.getCode());
                    c.put("name", sub.getName());
                    c.put("parentCode", cat.getCode());
                    children.add(c);
                }
            }
            m.put("children", children);
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> all() {
        Map<String, Object> m = new HashMap<>();
        for (String t : List.of("attack_phase", "attack_direction", "attack_result", "event_level",
                "event_source", "device_type", "impact_status", "disposal_action",
                "exec_device", "verify_result", "event_status")) {
            m.put(t, list(t));
        }
        m.put("attack_type_category", list("attack_type_category"));
        m.put("attack_type", list("attack_type"));
        return m;
    }

    public void saveItem(String dictType, String code, String name, Long parentId, Integer sort) {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new BizException("编码与名称不能为空");
        }
        SysDictItem it = new SysDictItem();
        it.setDictType(dictType);
        it.setCode(code.trim());
        it.setName(name.trim());
        it.setParentId(parentId == null ? 0L : parentId);
        it.setSort(sort == null ? 99 : sort);
        it.setEnabled(1);
        it.setCreateTime(LocalDateTime.now());
        dictItemMapper.insert(it);
        dictCache.evict(dictType);
    }

    public void deleteItem(Long id) {
        SysDictItem it = dictItemMapper.selectById(id);
        if (it == null) {
            return;
        }
        dictItemMapper.deleteById(id);
        dictCache.evict(it.getDictType());
    }
}
