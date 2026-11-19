package com.blued.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blued.system.entity.SysRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Insert("insert into sys_user_role(user_id, role_id) values(#{userId}, #{roleId})")
    int bindRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
