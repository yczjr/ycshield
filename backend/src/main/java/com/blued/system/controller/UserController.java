package com.blued.system.controller;

import com.blued.common.R;
import com.blued.system.dto.AuthDto;
import com.blued.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /** 供分析员指派处置人时选择：返回处置员与管理员 */
    @GetMapping("/handlers")
    public R<List<AuthDto.UserItem>> handlers() {
        List<AuthDto.UserItem> all = authService.listUsers();
        return R.ok(all.stream()
                .filter(u -> u.getRoles() != null && (u.getRoles().contains("HANDLER") || u.getRoles().contains("ADMIN")))
                .collect(Collectors.toList()));
    }

    @GetMapping
    public R<List<AuthDto.UserItem>> list(@RequestParam(required = false) String role) {
        List<AuthDto.UserItem> all = authService.listUsers();
        if (role == null || role.isBlank()) {
            return R.ok(all);
        }
        return R.ok(all.stream().filter(u -> u.getRoles() != null && u.getRoles().contains(role))
                .collect(Collectors.toList()));
    }
}
