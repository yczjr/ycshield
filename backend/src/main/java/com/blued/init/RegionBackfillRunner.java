package com.blued.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blued.common.IpRegionService;
import com.blued.event.entity.EventMain;
import com.blued.event.mapper.EventMainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时回填历史事件的攻击源归属地（仅处理 attack_region 为空的记录）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegionBackfillRunner implements ApplicationRunner {

    private final EventMainMapper mainMapper;
    private final IpRegionService ipRegion;

    @Override
    public void run(ApplicationArguments args) {
        if (!ipRegion.isReady()) {
            return;
        }
        int total = 0;
        try {
            while (true) {
                List<EventMain> list = mainMapper.selectList(new LambdaQueryWrapper<EventMain>()
                        .select(EventMain::getId, EventMain::getAttackIps)
                        .isNull(EventMain::getAttackRegion)
                        .last("limit 500"));
                if (list.isEmpty()) {
                    break;
                }
                for (EventMain e : list) {
                    EventMain upd = new EventMain();
                    upd.setId(e.getId());
                    if (e.getAttackIps() != null && !e.getAttackIps().isBlank()) {
                        String first = e.getAttackIps().split(",")[0].trim();
                        upd.setAttackRegion(ipRegion.label(first));
                        upd.setAttackCountry(ipRegion.country(first));
                    } else {
                        upd.setAttackRegion("未知");
                        upd.setAttackCountry("未知");
                    }
                    mainMapper.updateById(upd);
                }
                total += list.size();
                if (list.size() < 500) {
                    break;
                }
            }
            if (total > 0) {
                log.info("攻击源归属地回填完成，共 {} 条", total);
            }
        } catch (Exception ex) {
            log.warn("攻击源归属地回填失败：{}", ex.getMessage());
        }
    }
}
