package com.example.springboot.mapper;

import com.example.springboot.entity.RoleMenuPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMenuPermissionMapper {

    @Select("SELECT * FROM role_menu_permissions ORDER BY role_code ASC, menu_key ASC")
    List<RoleMenuPermission> selectAll();

    @Select("SELECT * FROM role_menu_permissions WHERE role_code = #{roleCode} ORDER BY menu_key ASC")
    List<RoleMenuPermission> selectByRoleCode(@Param("roleCode") String roleCode);

    @Delete("DELETE FROM role_menu_permissions WHERE role_code = #{roleCode}")
    void deleteByRoleCode(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO role_menu_permissions(role_code, menu_key, enabled) VALUES(#{role_code}, #{menu_key}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RoleMenuPermission item);
}
