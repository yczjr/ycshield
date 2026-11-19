package com.blued.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blued.event.mapper.EventMainMapper;
import com.blued.system.entity.SysUser;
import com.blued.system.mapper.SysRoleMapper;
import com.blued.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final EventMainMapper eventMainMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${blued.init.admin-password:Admin@Blued2025}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            return;
        }
        create("admin", "系统管理员", "ADMIN", adminPassword);
        create("monitor", "监测员甲", "MONITOR", "Monitor@123");
        create("analyst", "分析员甲", "ANALYST", "Analyst@123");
        create("handler", "处置员甲", "HANDLER", "Handler@123");
        log.info("已初始化默认账号：admin / monitor / analyst / handler");
    }

    private void create(String username, String realName, String roleCode, String rawPwd) {
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setRealName(realName);
        u.setStatus(1);
        u.setPassword(passwordEncoder.encode(rawPwd));
        u.setPwdUpdateTime(LocalDateTime.now());
        userMapper.insert(u);
        Long roleId = roleMapper.selectOne(new LambdaQueryWrapper<com.blued.system.entity.SysRole>()
                .eq(com.blued.system.entity.SysRole::getCode, roleCode)).getId();
        roleMapper.bindRole(u.getId(), roleId);
    }
}
