package com.example.springboot.mapper;

import com.example.springboot.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT id, code, name, description, sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt FROM roles WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Role> selectAllActive();

    @Select("SELECT id, code, name, description, sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt FROM roles ORDER BY sort_order ASC, id ASC")
    List<Role> selectAll();

    @Select("SELECT id, code, name, description, sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt FROM roles WHERE id = #{id}")
    Role selectById(@Param("id") Long id);

    @Select("SELECT id, code, name, description, sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt FROM roles WHERE code = #{code} LIMIT 1")
    Role selectByCode(@Param("code") String code);

    @Insert("INSERT INTO roles(code, name, description, sort_order, status) " +
            "VALUES(#{code}, #{name}, #{description}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    @Update("UPDATE roles SET " +
            "code = #{code}, " +
            "name = #{name}, " +
            "description = #{description}, " +
            "sort_order = #{sortOrder}, " +
            "status = #{status} " +
            "WHERE id = #{id}")
    int update(Role role);

    @Delete("DELETE FROM roles WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
