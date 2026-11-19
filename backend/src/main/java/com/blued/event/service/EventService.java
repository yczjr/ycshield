package com.blued.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blued.common.BizException;
import com.blued.common.DictCache;
import com.blued.common.EventStatus;
import com.blued.common.PageResult;
import com.blued.event.dto.EventReq;
import com.blued.event.dto.EventVo;
import com.blued.event.entity.EventAlarm;
import com.blued.event.entity.EventDisposal;
import com.blued.event.entity.EventEvidence;
import com.blued.event.entity.EventFlow;
import com.blued.event.entity.EventMain;
import com.blued.event.mapper.EventAlarmMapper;
import com.blued.event.mapper.EventDisposalMapper;
import com.blued.event.mapper.EventEvidenceMapper;
import com.blued.event.mapper.EventFlowMapper;
import com.blued.event.mapper.EventMainMapper;
import com.blued.security.LoginUser;
import com.blued.security.UserContext;
import com.blued.system.entity.SysUser;
import com.blued.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventMainMapper mainMapper;
    private final EventAlarmMapper alarmMapper;
    private final EventEvidenceMapper evidenceMapper;
    private final EventFlowMapper flowMapper;
    private final EventDisposalMapper disposalMapper;
    private final SysUserMapper userMapper;
    private final DictCache dictCache;
    private final EventNoGenerator noGenerator;
    private final com.blued.common.IpRegionService ipRegion;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /* ================= 创建 ================= */
    @Transactional
    public EventVo.Detail create(EventReq.Create req) {
        LoginUser u = UserContext.require();

        LinkedHashSet<String> victims = new LinkedHashSet<>();
        if (req.getVictimIps() != null) {
            victims.addAll(clean(req.getVictimIps()));
        }
        for (EventReq.Alarm a : req.getAlarms()) {
            if (a.getVictimIps() != null) {
                victims.addAll(clean(a.getVictimIps()));
            }
        }
        LinkedHashSet<String> attacks = new LinkedHashSet<>();
        if (req.getAttackIps() != null) {
            attacks.addAll(clean(req.getAttackIps()));
        }

        EventMain e = new EventMain();
        e.setEventNo(noGenerator.next());
        e.setTitle(req.getTitle() == null || req.getTitle().isBlank()
                ? buildTitle(req, attacks, victims) : req.getTitle().trim());
        e.setStatus(EventStatus.SUBMITTED.name());
        e.setLevel(req.getLevel());
        e.setAttackTypeCategory(req.getAttackTypeCategory());
        e.setAttackType(req.getAttackType());
        e.setAttackPhase(req.getAttackPhase());
        e.setAttackDirection(req.getAttackDirection());
        e.setAttackResult(req.getAttackResult());
        e.setEventSource(req.getEventSource() == null ? "MONITOR" : req.getEventSource());
        e.setFoundTime(req.getFoundTime());
        e.setAttackIps(String.join(",", attacks));
        if (!attacks.isEmpty()) {
            String first = attacks.iterator().next();
            e.setAttackRegion(ipRegion.label(first));
            e.setAttackCountry(ipRegion.country(first));
        }
        e.setVictimIps(String.join(",", victims));
        e.setAssetName(req.getAssetName());
        e.setImpactStatus(req.getImpactStatus());
        e.setBusinessImpact(req.getBusinessImpact());
        e.setPreliminaryConclusion(req.getPreliminaryConclusion());
        e.setResultModified(0);
        e.setReporterId(u.getId());
        e.setReporterName(u.getRealName());
        e.setSubmitTime(LocalDateTime.now());
        e.setIsImportant(req.getIsImportant() == null ? 0 : req.getIsImportant());
        e.setIsTimeout(0);
        mainMapper.insert(e);

        int seq = 1;
        for (EventReq.Alarm a : req.getAlarms()) {
            EventAlarm al = new EventAlarm();
            al.setEventId(e.getId());
            al.setSeq(seq++);
            al.setDeviceType(a.getDeviceType());
            al.setDeviceName(a.getDeviceName());
            al.setAlarmTime(a.getAlarmTime());
            al.setAlarmName(a.getAlarmName());
            al.setDirection(a.getDirection() == null || a.getDirection().isBlank()
                    ? req.getAttackDirection() : a.getDirection());
            al.setSrcIps(join(a.getSrcIps()));
            al.setDstIps(join(a.getDstIps()));
            al.setVictimIps(join(a.getVictimIps()));
            al.setRawText(a.getRawText());
            al.setCreateBy(u.getId());
            alarmMapper.insert(al);
        }

        flow(e.getId(), null, EventStatus.SUBMITTED.name(), "CREATE", "监测员提交事件，初步研判：" + req.getAttackResult());
        return detail(e.getId());
    }

    private String buildTitle(EventReq.Create req, Set<String> attacks, Set<String> victims) {
        String typeName = dictCache.name("attack_type", req.getAttackType());
        String asset = req.getAssetName() != null && !req.getAssetName().isBlank()
                ? req.getAssetName()
                : (victims.isEmpty() ? "未知资产" : victims.iterator().next());
        return typeName + " 攻击 " + asset;
    }

    /* ================= 列表 ================= */
    public PageResult<EventVo.ListItem> list(EventReq.Query q) {
        LoginUser u = UserContext.require();
        LambdaQueryWrapper<EventMain> w = new LambdaQueryWrapper<>();

        if (notBlank(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(EventMain::getEventNo, kw).or().like(EventMain::getTitle, kw)
                    .or().like(EventMain::getAttackIps, kw).or().like(EventMain::getVictimIps, kw));
        }
        if (notBlank(q.getStatus())) {
            w.eq(EventMain::getStatus, q.getStatus());
        }
        if (q.getLevel() != null) {
            w.eq(EventMain::getLevel, q.getLevel());
        }
        if (notBlank(q.getAttackTypeCategory())) {
            w.eq(EventMain::getAttackTypeCategory, q.getAttackTypeCategory());
        }
        if (notBlank(q.getAttackType())) {
            w.eq(EventMain::getAttackType, q.getAttackType());
        }
        if (notBlank(q.getAttackDirection())) {
            w.eq(EventMain::getAttackDirection, q.getAttackDirection());
        }
        if (notBlank(q.getAttackResult())) {
            w.eq(EventMain::getAttackResult, q.getAttackResult());
        }
        if (notBlank(q.getAttackIp())) {
            w.like(EventMain::getAttackIps, q.getAttackIp().trim());
        }
        if (notBlank(q.getVictimIp())) {
            w.like(EventMain::getVictimIps, q.getVictimIp().trim());
        }
        if (q.getReporterId() != null) {
            w.eq(EventMain::getReporterId, q.getReporterId());
        }
        if (notBlank(q.getBeginTime())) {
            w.ge(EventMain::getFoundTime, LocalDateTime.parse(q.getBeginTime().length() > 10
                    ? q.getBeginTime() : q.getBeginTime() + " 00:00:00", FMT));
        }
        if (notBlank(q.getEndTime())) {
            w.le(EventMain::getFoundTime, LocalDateTime.parse(q.getEndTime().length() > 10
                    ? q.getEndTime() : q.getEndTime() + " 23:59:59", FMT));
        }
        String tab = q.getTab() == null ? "" : q.getTab().toUpperCase();
        switch (tab) {
            case "MINE" -> w.eq(EventMain::getReporterId, u.getId());
            case "IMPORTANT" -> w.eq(EventMain::getIsImportant, 1);
            case "TIMEOUT" -> w.eq(EventMain::getIsTimeout, 1);
            case "ARCHIVED" -> w.eq(EventMain::getStatus, EventStatus.ARCHIVED.name());
            case "TODO" -> w.in(EventMain::getStatus,
                    List.of(EventStatus.SUBMITTED.name(), EventStatus.ANALYZING.name(),
                            EventStatus.HANDLING.name(), EventStatus.REVIEWING.name()));
            default -> { }
        }
        if (notBlank(q.getDeviceType())) {
            List<Long> ids = alarmMapper.selectList(new LambdaQueryWrapper<EventAlarm>()
                            .eq(EventAlarm::getDeviceType, q.getDeviceType()))
                    .stream().map(EventAlarm::getEventId).distinct().collect(Collectors.toList());
            if (ids.isEmpty()) {
                return PageResult.of(0, List.of());
            }
            w.in(EventMain::getId, ids);
        }
        w.orderByDesc(EventMain::getFoundTime);

        int pn = q.getPageNum() == null || q.getPageNum() < 1 ? 1 : q.getPageNum();
        int ps = q.getPageSize() == null || q.getPageSize() < 1 ? 20 : Math.min(q.getPageSize(), 200);
        Page<EventMain> page = mainMapper.selectPage(new Page<>(pn, ps), w);

        Map<Long, List<EventAlarm>> alarmMap = loadAlarms(page.getRecords());
        Map<String, String> catNames = dictCache.nameMap("attack_type_category");
        Map<String, String> typeNames = dictCache.nameMap("attack_type");
        Map<String, String> dirNames = dictCache.nameMap("attack_direction");
        Map<String, String> resultNames = dictCache.nameMap("attack_result");
        Map<String, String> levelNames = dictCache.nameMap("event_level");
        Map<String, String> deviceNames = dictCache.nameMap("device_type");

        List<EventVo.ListItem> items = new ArrayList<>();
        for (EventMain e : page.getRecords()) {
            EventVo.ListItem it = new EventVo.ListItem();
            it.setId(e.getId());
            it.setEventNo(e.getEventNo());
            it.setTitle(e.getTitle());
            it.setStatus(e.getStatus());
            it.setStatusName(EventStatus.labelOf(e.getStatus()));
            it.setLevel(e.getLevel());
            it.setLevelName(levelNames.getOrDefault(String.valueOf(e.getLevel()), ""));
            it.setAttackTypeCategory(e.getAttackTypeCategory());
            it.setAttackTypeCategoryName(catNames.getOrDefault(e.getAttackTypeCategory(), e.getAttackTypeCategory()));
            it.setAttackType(e.getAttackType());
            it.setAttackTypeName(typeNames.getOrDefault(e.getAttackType(), e.getAttackType()));
            it.setAttackDirection(e.getAttackDirection());
            it.setAttackDirectionName(dirNames.getOrDefault(e.getAttackDirection(), e.getAttackDirection()));
            it.setAttackResult(e.getAttackResult());
            it.setAttackResultName(resultNames.getOrDefault(e.getAttackResult(), e.getAttackResult()));
            it.setAttackIps(e.getAttackIps());
            it.setVictimIps(e.getVictimIps());
            it.setAssetName(e.getAssetName());
            it.setIsTimeout(e.getIsTimeout());
            it.setIsImportant(e.getIsImportant());
            it.setFoundTime(e.getFoundTime());
            it.setReporterName(e.getReporterName());
            it.setHandlerName(e.getHandlerName());
            List<EventAlarm> alarms = alarmMap.getOrDefault(e.getId(), List.of());
            it.setAlarmCount(alarms.size());
            if (!alarms.isEmpty()) {
                EventAlarm first = alarms.get(0);
                it.setDeviceType(first.getDeviceType());
                it.setDeviceTypeName(deviceNames.getOrDefault(first.getDeviceType(), first.getDeviceType()));
                it.setDeviceName(first.getDeviceName());
            }
            items.add(it);
        }
        return PageResult.of(page.getTotal(), items);
    }

    private Map<Long, List<EventAlarm>> loadAlarms(List<EventMain> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = events.stream().map(EventMain::getId).collect(Collectors.toList());
        List<EventAlarm> alarms = alarmMapper.selectList(new LambdaQueryWrapper<EventAlarm>()
                .in(EventAlarm::getEventId, ids).orderByAsc(EventAlarm::getSeq));
        return alarms.stream().collect(Collectors.groupingBy(EventAlarm::getEventId));
    }

    /* ================= 详情 ================= */
    public EventVo.Detail detail(Long id) {
        EventMain e = mainMapper.selectById(id);
        if (e == null) {
            throw new BizException(404, "事件不存在");
        }
        EventVo.Detail d = new EventVo.Detail();
        d.setMain(e);
        d.setAlarms(alarmMapper.selectList(new LambdaQueryWrapper<EventAlarm>()
                .eq(EventAlarm::getEventId, id).orderByAsc(EventAlarm::getSeq)));
        d.setEvidences(evidenceMapper.selectList(new LambdaQueryWrapper<EventEvidence>()
                .eq(EventEvidence::getEventId, id).orderByDesc(EventEvidence::getId)));
        d.setDisposals(disposalMapper.selectList(new LambdaQueryWrapper<EventDisposal>()
                .eq(EventDisposal::getEventId, id).orderByAsc(EventDisposal::getId)));
        d.setFlows(flowMapper.selectList(new LambdaQueryWrapper<EventFlow>()
                .eq(EventFlow::getEventId, id).orderByAsc(EventFlow::getId)));
        d.setCategoryNames(dictCache.nameMap("attack_type_category"));
        d.setTypeNames(dictCache.nameMap("attack_type"));
        d.setDeviceNames(dictCache.nameMap("device_type"));
        d.setDirectionNames(dictCache.nameMap("attack_direction"));
        d.setResultNames(dictCache.nameMap("attack_result"));
        d.setLevelNames(dictCache.nameMap("event_level"));
        d.setDisposalActionNames(dictCache.nameMap("disposal_action"));
        d.setExecDeviceNames(dictCache.nameMap("exec_device"));
        d.setVerifyResultNames(dictCache.nameMap("verify_result"));
        d.setStatusNames(dictCache.nameMap("event_status"));
        return d;
    }

    /* ================= 流转 ================= */
    @Transactional
    public void claim(Long id) {
        EventMain e = require(id);
        requireStatus(e, EventStatus.SUBMITTED);
        requireRole("ANALYST", "ADMIN");
        LoginUser u = UserContext.require();

        EventMain upd = new EventMain();
        upd.setId(id);
        upd.setStatus(EventStatus.ANALYZING.name());
        upd.setOwnerId(u.getId());
        upd.setOwnerName(u.getRealName());
        mainMapper.updateById(upd);
        flow(id, EventStatus.SUBMITTED.name(), EventStatus.ANALYZING.name(), "CLAIM", "认领并开始研判");
    }

    @Transactional
    public void analyze(Long id, EventReq.Analyze req) {
        EventMain e = require(id);
        // 研判可多次进行：分析中、已指派处置（应急升级前）都允许补充/修正结论
        if (!EventStatus.ANALYZING.name().equals(e.getStatus())
                && !EventStatus.HANDLING.name().equals(e.getStatus())) {
            throw new BizException("当前状态为「" + EventStatus.labelOf(e.getStatus()) + "」，不能研判");
        }
        requireOwnerOrAdmin(e);
        LoginUser u = UserContext.require();

        EventMain upd = new EventMain();
        upd.setId(id);
        boolean modified = false;
        String reason = req.getModifyReason();

        if (req.getAttackResult() != null && !req.getAttackResult().equals(e.getAttackResult())) {
            changeField(id, "attack_result", e.getAttackResult(), req.getAttackResult(), reason);
            upd.setAttackResult(req.getAttackResult());
            upd.setResultModified(1);
            upd.setModifyReason(reason);
            modified = true;
        }
        if (req.getLevel() != null && !req.getLevel().equals(e.getLevel())) {
            changeField(id, "level", String.valueOf(e.getLevel()), String.valueOf(req.getLevel()), reason);
            upd.setLevel(req.getLevel());
            modified = true;
        }
        if (req.getAttackType() != null && !req.getAttackType().equals(e.getAttackType())) {
            changeField(id, "attack_type", e.getAttackType(), req.getAttackType(), reason);
            upd.setAttackType(req.getAttackType());
            if (req.getAttackTypeCategory() != null) {
                upd.setAttackTypeCategory(req.getAttackTypeCategory());
            }
            modified = true;
        }
        if (req.getAttackDirection() != null && !req.getAttackDirection().equals(e.getAttackDirection())) {
            changeField(id, "attack_direction", e.getAttackDirection(), req.getAttackDirection(), reason);
            upd.setAttackDirection(req.getAttackDirection());
            modified = true;
        }
        upd.setAnalysisConclusion(req.getAnalysisConclusion());
        mainMapper.updateById(upd);
        if (!Objects.equals(req.getAnalysisConclusion(), e.getAnalysisConclusion())) {
            flow(id, e.getStatus(), e.getStatus(), "ANALYZE",
                    "研判结论：" + truncate(req.getAnalysisConclusion()));
        }
    }

    @Transactional
    public void assign(Long id, EventReq.Assign req) {
        EventMain e = require(id);
        requireStatus(e, EventStatus.ANALYZING);
        requireOwnerOrAdmin(e);
        if (e.getAnalysisConclusion() == null || e.getAnalysisConclusion().isBlank()) {
            throw new BizException("请先填写研判结论再指派处置");
        }
        SysUser handler = userMapper.selectById(req.getHandlerId());
        if (handler == null) {
            throw new BizException("处置人不存在");
        }
        EventMain upd = new EventMain();
        upd.setId(id);
        upd.setStatus(EventStatus.HANDLING.name());
        upd.setHandlerId(handler.getId());
        upd.setHandlerName(handler.getRealName() == null ? handler.getUsername() : handler.getRealName());
        upd.setAssignTime(LocalDateTime.now());
        mainMapper.updateById(upd);
        flow(id, EventStatus.ANALYZING.name(), EventStatus.HANDLING.name(), "ASSIGN",
                "指派处置人：" + upd.getHandlerName() + (req.getOpinion() == null ? "" : "；" + req.getOpinion()));
    }

    @Transactional
    public void dispose(Long id, EventReq.Dispose req) {
        EventMain e = require(id);
        requireStatus(e, EventStatus.HANDLING);
        LoginUser u = UserContext.require();
        if (!Objects.equals(e.getHandlerId(), u.getId()) && !UserContext.hasRole("ADMIN")) {
            throw new BizException(403, "只有被指派的处置人可以登记处置");
        }
        EventDisposal d = new EventDisposal();
        d.setEventId(id);
        d.setActions(String.join(",", req.getActions()));
        d.setTargets(req.getTargets());
        d.setExecDevice(req.getExecDevice());
        d.setExecTime(req.getExecTime() == null ? LocalDateTime.now() : req.getExecTime());
        d.setEffectTime(req.getEffectTime());
        d.setVerifyResult(req.getVerifyResult());
        d.setRemark(req.getRemark());
        d.setExecutorId(u.getId());
        d.setExecutorName(u.getRealName());
        disposalMapper.insert(d);
        flow(id, e.getStatus(), e.getStatus(), "DISPOSE",
                "登记处置：" + d.getActions() + " → " + d.getTargets() + "（" + d.getExecDevice() + "）");
    }

    @Transactional
    public void submitReview(Long id, String opinion) {
        EventMain e = require(id);
        requireStatus(e, EventStatus.HANDLING);
        LoginUser u = UserContext.require();
        if (!Objects.equals(e.getHandlerId(), u.getId()) && !UserContext.hasRole("ADMIN")) {
            throw new BizException(403, "只有被指派的处置人可以提交复核");
        }
        Long cnt = disposalMapper.selectCount(new LambdaQueryWrapper<EventDisposal>().eq(EventDisposal::getEventId, id));
        if (cnt == null || cnt == 0) {
            throw new BizException("请先登记处置记录再提交复核");
        }
        EventMain upd = new EventMain();
        upd.setId(id);
        upd.setStatus(EventStatus.REVIEWING.name());
        upd.setReviewTime(LocalDateTime.now());
        mainMapper.updateById(upd);
        flow(id, EventStatus.HANDLING.name(), EventStatus.REVIEWING.name(), "SUBMIT_REVIEW",
                opinion == null ? "提交复核" : opinion);
    }

    @Transactional
    public void review(Long id, EventReq.Review req) {
        EventMain e = require(id);
        requireStatus(e, EventStatus.REVIEWING);
        requireRole("ANALYST", "ADMIN");
        LoginUser u = UserContext.require();
        EventMain upd = new EventMain();
        upd.setId(id);
        if (Boolean.TRUE.equals(req.getPass())) {
            upd.setStatus(EventStatus.ARCHIVED.name());
            upd.setArchivedTime(LocalDateTime.now());
            mainMapper.updateById(upd);
            flow(id, EventStatus.REVIEWING.name(), EventStatus.ARCHIVED.name(), "ARCHIVE",
                    req.getOpinion() == null ? "复核通过，归档" : req.getOpinion());
        } else {
            if (req.getOpinion() == null || req.getOpinion().isBlank()) {
                throw new BizException("驳回必须填写原因");
            }
            upd.setStatus(EventStatus.HANDLING.name());
            mainMapper.updateById(upd);
            flow(id, EventStatus.REVIEWING.name(), EventStatus.HANDLING.name(), "REJECT", req.getOpinion());
        }
    }

    @Transactional
    public void close(Long id, String reason) {
        EventMain e = require(id);
        if (!EventStatus.SUBMITTED.name().equals(e.getStatus())
                && !EventStatus.ANALYZING.name().equals(e.getStatus())) {
            throw new BizException("只有待受理或分析中的事件可以关闭");
        }
        requireRole("ANALYST", "ADMIN");
        EventMain upd = new EventMain();
        upd.setId(id);
        upd.setStatus(EventStatus.CLOSED.name());
        upd.setCloseReason(reason);
        mainMapper.updateById(upd);
        flow(id, e.getStatus(), EventStatus.CLOSED.name(), "CLOSE", reason);
    }

    /** 研判办结：认定为一般扫描/攻击未成功等，无需处置，直接归档 */
    @Transactional
    public void finishAnalyze(Long id, String opinion) {
        EventMain e = require(id);
        if (!EventStatus.ANALYZING.name().equals(e.getStatus())
                && !EventStatus.HANDLING.name().equals(e.getStatus())) {
            throw new BizException("当前状态为「" + EventStatus.labelOf(e.getStatus()) + "」，不能办结");
        }
        requireOwnerOrAdmin(e);
        EventMain upd = new EventMain();
        upd.setId(id);
        upd.setStatus(EventStatus.ARCHIVED.name());
        upd.setArchivedTime(LocalDateTime.now());
        mainMapper.updateById(upd);
        flow(id, e.getStatus(), EventStatus.ARCHIVED.name(), "FINISH",
                opinion == null || opinion.isBlank() ? "研判办结（无需处置），直接归档" : opinion);
    }

    /** 直接封禁 IP 结案：不接入设备，仅记录研判人的封禁决定 */
    @Transactional
    public void blockIp(Long id, EventReq.BlockIp req) {
        EventMain e = require(id);
        if (!EventStatus.ANALYZING.name().equals(e.getStatus())
                && !EventStatus.HANDLING.name().equals(e.getStatus())) {
            throw new BizException("当前状态为「" + EventStatus.labelOf(e.getStatus()) + "」，不能执行封禁结案");
        }
        requireOwnerOrAdmin(e);
        LoginUser u = UserContext.require();

        EventDisposal d = new EventDisposal();
        d.setEventId(id);
        d.setActions("BLOCK_IP");
        d.setTargets(req.getTargets());
        d.setExecDevice("MANUAL");
        d.setExecTime(LocalDateTime.now());
        d.setEffectTime(LocalDateTime.now());
        d.setVerifyResult("PENDING");
        d.setRemark(req.getOpinion() == null || req.getOpinion().isBlank()
                ? "研判后直接封禁，未接入设备，人工执行" : req.getOpinion());
        d.setExecutorId(u.getId());
        d.setExecutorName(u.getRealName());
        disposalMapper.insert(d);

        EventMain upd = new EventMain();
        upd.setId(id);
        upd.setStatus(EventStatus.ARCHIVED.name());
        upd.setArchivedTime(LocalDateTime.now());
        mainMapper.updateById(upd);
        flow(id, e.getStatus(), EventStatus.ARCHIVED.name(), "BLOCK_CLOSE",
                "直接封禁 IP 结案：" + req.getTargets()
                        + (req.getOpinion() == null || req.getOpinion().isBlank() ? "" : "；" + req.getOpinion()));
    }

    /* ================= 重复检测 ================= */
    public List<Map<String, Object>> checkDuplicate(EventReq.DuplicateCheck req) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (req.getFoundTime() == null || req.getVictimIps() == null || req.getVictimIps().isEmpty()) {
            return result;
        }
        LambdaQueryWrapper<EventMain> w = new LambdaQueryWrapper<>();
        w.ge(EventMain::getFoundTime, req.getFoundTime().minusHours(2))
                .le(EventMain::getFoundTime, req.getFoundTime().plusHours(2));
        List<String> ips = clean(req.getVictimIps());
        if (!ips.isEmpty()) {
            w.and(x -> {
                for (int i = 0; i < ips.size(); i++) {
                    if (i == 0) {
                        x.like(EventMain::getVictimIps, ips.get(i));
                    } else {
                        x.or().like(EventMain::getVictimIps, ips.get(i));
                    }
                }
            });
        }
        for (EventMain e : mainMapper.selectList(w.last("limit 10"))) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("eventNo", e.getEventNo());
            m.put("title", e.getTitle());
            m.put("statusName", EventStatus.labelOf(e.getStatus()));
            m.put("foundTime", e.getFoundTime() == null ? null : e.getFoundTime().format(FMT));
            result.add(m);
        }
        return result;
    }

    /* ================= 工具 ================= */
    public EventMain require(Long id) {
        EventMain e = mainMapper.selectById(id);
        if (e == null) {
            throw new BizException(404, "事件不存在");
        }
        return e;
    }

    private void requireStatus(EventMain e, EventStatus expected) {
        if (!expected.name().equals(e.getStatus())) {
            throw new BizException("当前状态为「" + EventStatus.labelOf(e.getStatus()) + "」，不能执行该操作");
        }
    }

    private void requireRole(String... roles) {
        for (String r : roles) {
            if (UserContext.hasRole(r)) {
                return;
            }
        }
        throw new BizException(403, "当前角色无权执行该操作");
    }

    private void requireOwnerOrAdmin(EventMain e) {
        LoginUser u = UserContext.require();
        if (!Objects.equals(e.getOwnerId(), u.getId()) && !UserContext.hasRole("ADMIN")) {
            throw new BizException(403, "只有认领人本人可以研判");
        }
    }

    private void flow(Long eventId, String from, String to, String action, String opinion) {
        LoginUser u = UserContext.get();
        EventFlow f = new EventFlow();
        f.setEventId(eventId);
        f.setFromStatus(from);
        f.setToStatus(to);
        f.setAction(action);
        f.setOpinion(opinion);
        f.setOperatorId(u == null ? null : u.getId());
        f.setOperatorName(u == null ? "system" : u.getRealName());
        f.setOperateTime(LocalDateTime.now());
        flowMapper.insert(f);
    }

    private void changeField(Long eventId, String field, String oldValue, String newValue, String reason) {
        LoginUser u = UserContext.get();
        EventFlow f = new EventFlow();
        f.setEventId(eventId);
        f.setAction("MODIFY");
        f.setFieldName(field);
        f.setOldValue(truncate(oldValue));
        f.setNewValue(truncate(newValue));
        f.setOpinion(reason == null ? "字段修正" : reason);
        f.setOperatorId(u == null ? null : u.getId());
        f.setOperatorName(u == null ? "system" : u.getRealName());
        f.setOperateTime(LocalDateTime.now());
        flowMapper.insert(f);
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 480 ? s.substring(0, 480) : s;
    }

    private List<String> clean(List<String> ips) {
        if (ips == null) {
            return List.of();
        }
        return ips.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
                .distinct().collect(Collectors.toList());
    }

    private String join(List<String> list) {
        return list == null ? null : String.join(",", clean(list));
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /* ================= 超时刷新 ================= */
    @Transactional
    public int refreshTimeout() {
        List<EventMain> list = mainMapper.selectList(new LambdaQueryWrapper<EventMain>()
                .in(EventMain::getStatus, Arrays.asList(EventStatus.SUBMITTED.name(), EventStatus.ANALYZING.name(),
                        EventStatus.HANDLING.name(), EventStatus.REVIEWING.name())));
        int changed = 0;
        LocalDateTime now = LocalDateTime.now();
        for (EventMain e : list) {
            boolean timeout = calcTimeout(e, now);
            int flag = timeout ? 1 : 0;
            if (e.getIsTimeout() == null || e.getIsTimeout() != flag) {
                EventMain upd = new EventMain();
                upd.setId(e.getId());
                upd.setIsTimeout(flag);
                mainMapper.updateById(upd);
                changed++;
            }
        }
        return changed;
    }

    private boolean calcTimeout(EventMain e, LocalDateTime now) {
        boolean high = e.getLevel() != null && e.getLevel() == 1;
        switch (e.getStatus()) {
            case "SUBMITTED" -> {
                return e.getSubmitTime() != null && now.isAfter(e.getSubmitTime().plusMinutes(high ? 15 : 60));
            }
            case "ANALYZING" -> {
                return e.getSubmitTime() != null && now.isAfter(e.getSubmitTime().plusMinutes(high ? 30 : 240));
            }
            case "HANDLING" -> {
                LocalDateTime base = e.getAssignTime() == null ? e.getSubmitTime() : e.getAssignTime();
                return base != null && now.isAfter(base.plusMinutes(high ? 120 : 1440));
            }
            case "REVIEWING" -> {
                return e.getReviewTime() != null && now.isAfter(e.getReviewTime().plusHours(24));
            }
            default -> {
                return false;
            }
        }
    }
}
