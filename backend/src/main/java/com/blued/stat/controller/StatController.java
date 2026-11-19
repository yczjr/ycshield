package com.blued.stat.controller;

import com.blued.common.R;
import com.blued.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(statService.overview());
    }

    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        return R.ok(statService.dashboard());
    }
}
