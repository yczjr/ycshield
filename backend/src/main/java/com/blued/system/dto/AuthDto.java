package com.blued.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Set;

public class AuthDto {

    @Data
    public static class LoginReq {
        @NotBlank(message = "请输入账号")
        private String username;
        @NotBlank(message = "请输入密码")
        private String password;
    }

    @Data
    public static class LoginResp {
        private String token;
        private Long userId;
        private String username;
        private String realName;
        private Set<String> roles;
        private List<String> roleNames;
    }

    @Data
    public static class UserItem {
        private Long id;
        private String username;
        private String realName;
        private String phone;
        private Integer status;
        private List<String> roles;
        private String lastLoginTime;
    }

    @Data
    public static class CreateUserReq {
        @NotBlank(message = "账号不能为空")
        private String username;
        @NotBlank(message = "姓名不能为空")
        private String realName;
        private String phone;
        @NotBlank(message = "角色不能为空")
        private String roleCode;
        private String password;
    }

    @Data
    public static class ResetPasswordReq {
        @NotBlank(message = "新密码不能为空")
        private String password;
    }

    @Data
    public static class ChangePasswordReq {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
