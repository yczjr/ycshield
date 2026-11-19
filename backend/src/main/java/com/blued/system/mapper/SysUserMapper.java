package com.blued.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blued.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("select r.code from sys_role r join sys_user_role ur on ur.role_id = r.id where ur.user_id = #{userId}")
    List<String> selectRoleCodes(@Param("userId") Long userId);
}
