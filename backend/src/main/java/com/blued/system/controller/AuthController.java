package com.blued.system.controller;

import com.blued.common.R;
import com.blued.security.UserContext;
import com.blued.system.dto.AuthDto;
import com.blued.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<AuthDto.LoginResp> login(@Valid @RequestBody AuthDto.LoginReq req) {
        return R.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        UserContext.clear();
        return R.ok();
    }

    @GetMapping("/me")
    public R<AuthDto.LoginResp> me() {
        return R.ok(authService.me());
    }

    /** 修改自己的密码 */
    @PostMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody AuthDto.ChangePasswordReq req) {
        authService.changeOwnPassword(req.getOldPassword(), req.getNewPassword());
        return R.ok();
    }
}
