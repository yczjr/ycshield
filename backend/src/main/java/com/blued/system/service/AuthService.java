package com.blued.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blued.common.BizException;
import com.blued.security.JwtUtil;
import com.blued.security.LoginUser;
import com.blued.security.UserContext;
import com.blued.system.dto.AuthDto;
import com.blued.system.entity.SysUser;
import com.blued.system.mapper.SysUserMapper;
import com.blued.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final Map<String, String> ROLE_NAMES = Map.of(
            "MONITOR", "监测员", "ANALYST", "分析员", "HANDLER", "处置员", "ADMIN", "管理员");

    public AuthDto.LoginResp login(AuthDto.LoginReq req) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername().trim()));
        // 密码容错：复制粘贴常带首尾空格/换行，统一去掉再校验
        String rawPwd = req.getPassword() == null ? "" : req.getPassword().trim();
        if (user == null || !passwordEncoder.matches(rawPwd, user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "账号已停用");
        }
        List<String> roles = userMapper.selectRoleCodes(user.getId());
        LoginUser lu = new LoginUser(user.getId(), user.getUsername(),
                user.getRealName() == null ? user.getUsername() : user.getRealName(),
                new HashSet<>(roles));
        SysUser upd = new SysUser();
        upd.setId(user.getId());
        upd.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(upd);

        AuthDto.LoginResp resp = new AuthDto.LoginResp();
        resp.setToken(jwtUtil.generate(lu));
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(lu.getRealName());
        resp.setRoles(lu.getRoles());
        resp.setRoleNames(roles.stream().map(r -> ROLE_NAMES.getOrDefault(r, r)).collect(Collectors.toList()));
        return resp;
    }

    public AuthDto.LoginResp me() {
        LoginUser u = UserContext.require();
        AuthDto.LoginResp resp = new AuthDto.LoginResp();
        resp.setUserId(u.getId());
        resp.setUsername(u.getUsername());
        resp.setRealName(u.getRealName());
        resp.setRoles(u.getRoles());
        resp.setRoleNames(u.getRoles().stream().map(r -> ROLE_NAMES.getOrDefault(r, r)).collect(Collectors.toList()));
        return resp;
    }

    public List<AuthDto.UserItem> listUsers() {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId));
        List<AuthDto.UserItem> list = new ArrayList<>();
        for (SysUser u : users) {
            AuthDto.UserItem item = new AuthDto.UserItem();
            item.setId(u.getId());
            item.setUsername(u.getUsername());
            item.setRealName(u.getRealName());
            item.setPhone(u.getPhone());
            item.setStatus(u.getStatus());
            item.setRoles(userMapper.selectRoleCodes(u.getId()));
            item.setLastLoginTime(u.getLastLoginTime() == null ? null : u.getLastLoginTime().toString().replace('T', ' '));
            list.add(item);
        }
        return list;
    }

    @Transactional
    public Long createUser(AuthDto.CreateUserReq req) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException("账号已存在");
        }
        SysUser u = new SysUser();
        u.setUsername(req.getUsername().trim());
        u.setRealName(req.getRealName());
        u.setPhone(req.getPhone());
        u.setStatus(1);
        String pwd = (req.getPassword() == null || req.getPassword().isBlank()) ? "Blued@123456" : req.getPassword();
        u.setPassword(passwordEncoder.encode(pwd));
        u.setPwdUpdateTime(LocalDateTime.now());
        userMapper.insert(u);

        Long roleId = roleMapper.selectOne(new LambdaQueryWrapper<com.blued.system.entity.SysRole>()
                .eq(com.blued.system.entity.SysRole::getCode, req.getRoleCode())).getId();
        roleMapper.bindRole(u.getId(), roleId);
        return u.getId();
    }

    public void toggleUser(Long id, Integer status) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        userMapper.updateById(u);
    }

    /** 管理员重置指定用户密码 */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BizException(404, "用户不存在");
        }
        String pwd = newPassword == null ? "" : newPassword.trim();
        if (pwd.length() < 6) {
            throw new BizException("密码至少 6 位");
        }
        SysUser u = new SysUser();
        u.setId(id);
        u.setPassword(passwordEncoder.encode(pwd));
        u.setPwdUpdateTime(LocalDateTime.now());
        userMapper.updateById(u);
    }

    /** 修改自己的密码 */
    @Transactional
    public void changeOwnPassword(String oldPassword, String newPassword) {
        LoginUser cur = UserContext.require();
        SysUser user = userMapper.selectById(cur.getId());
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        String oldPwd = oldPassword == null ? "" : oldPassword.trim();
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        String pwd = newPassword == null ? "" : newPassword.trim();
        if (pwd.length() < 6) {
            throw new BizException("新密码至少 6 位");
        }
        SysUser u = new SysUser();
        u.setId(cur.getId());
        u.setPassword(passwordEncoder.encode(pwd));
        u.setPwdUpdateTime(LocalDateTime.now());
        userMapper.updateById(u);
    }
}
