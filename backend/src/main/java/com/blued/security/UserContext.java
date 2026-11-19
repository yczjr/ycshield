package com.blued.security;

import com.blued.common.BizException;

public final class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(401, "未登录或登录已过期");
        }
        return u;
    }

    public static boolean hasRole(String role) {
        LoginUser u = HOLDER.get();
        return u != null && u.getRoles() != null && u.getRoles().contains(role);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
