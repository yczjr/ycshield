package com.blued.event.controller;

import com.blued.common.PageResult;
import com.blued.common.R;
import com.blued.event.dto.EventReq;
import com.blued.event.dto.EventVo;
import com.blued.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public R<EventVo.Detail> create(@Valid @RequestBody EventReq.Create req) {
        return R.ok(eventService.create(req));
    }

    @PostMapping("/page")
    public R<PageResult<EventVo.ListItem>> page(@RequestBody EventReq.Query q) {
        return R.ok(eventService.list(q));
    }

    @GetMapping("/{id}")
    public R<EventVo.Detail> detail(@PathVariable Long id) {
        return R.ok(eventService.detail(id));
    }

    @PostMapping("/{id}/claim")
    public R<Void> claim(@PathVariable Long id) {
        eventService.claim(id);
        return R.ok();
    }

    @PostMapping("/{id}/analyze")
    public R<Void> analyze(@PathVariable Long id, @Valid @RequestBody EventReq.Analyze req) {
        eventService.analyze(id, req);
        return R.ok();
    }

    @PostMapping("/{id}/assign")
    public R<Void> assign(@PathVariable Long id, @Valid @RequestBody EventReq.Assign req) {
        eventService.assign(id, req);
        return R.ok();
    }

    @PostMapping("/{id}/dispose")
    public R<Void> dispose(@PathVariable Long id, @Valid @RequestBody EventReq.Dispose req) {
        eventService.dispose(id, req);
        return R.ok();
    }

    @PostMapping("/{id}/submit-review")
    public R<Void> submitReview(@PathVariable Long id, @RequestParam(required = false) String opinion) {
        eventService.submitReview(id, opinion);
        return R.ok();
    }

    @PostMapping("/{id}/review")
    public R<Void> review(@PathVariable Long id, @Valid @RequestBody EventReq.Review req) {
        eventService.review(id, req);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    public R<Void> close(@PathVariable Long id, @Valid @RequestBody EventReq.Close req) {
        eventService.close(id, req.getReason());
        return R.ok();
    }

    /** 研判办结：无需处置，直接归档 */
    @PostMapping("/{id}/finish")
    public R<Void> finish(@PathVariable Long id, @RequestBody(required = false) EventReq.Finish req) {
        eventService.finishAnalyze(id, req == null ? null : req.getOpinion());
        return R.ok();
    }

    /** 直接封禁 IP 结案（不接入设备） */
    @PostMapping("/{id}/block-ip")
    public R<Void> blockIp(@PathVariable Long id, @Valid @RequestBody EventReq.BlockIp req) {
        eventService.blockIp(id, req);
        return R.ok();
    }

    @PostMapping("/check-duplicate")
    public R<List<Map<String, Object>>> checkDuplicate(@RequestBody EventReq.DuplicateCheck req) {
        return R.ok(eventService.checkDuplicate(req));
    }
}
