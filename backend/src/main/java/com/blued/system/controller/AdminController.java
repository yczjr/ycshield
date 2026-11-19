package com.blued.system.controller;

import com.blued.common.R;
import com.blued.system.dto.AuthDto;
import com.blued.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @GetMapping("/users")
    public R<List<AuthDto.UserItem>> users() {
        return R.ok(authService.listUsers());
    }

    @PostMapping("/users")
    public R<Long> create(@Valid @RequestBody AuthDto.CreateUserReq req) {
        return R.ok(authService.createUser(req));
    }

    @PostMapping("/users/{id}/status")
    public R<Void> status(@PathVariable Long id, @RequestParam Integer status) {
        authService.toggleUser(id, status);
        return R.ok();
    }

    /** 管理员重置任意用户密码 */
    @PostMapping("/users/{id}/password")
    public R<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody AuthDto.ResetPasswordReq req) {
        authService.resetPassword(id, req.getPassword());
        return R.ok();
    }
}
