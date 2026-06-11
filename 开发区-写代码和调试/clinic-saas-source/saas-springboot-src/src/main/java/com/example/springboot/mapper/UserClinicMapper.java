package com.example.springboot.mapper;

import com.example.springboot.entity.UserClinic;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserClinicMapper {

    @Select("SELECT id, user_id AS userId, clinic_id AS clinicId, role, is_default AS isDefault, created_at AS createdAt, updated_at AS updatedAt FROM user_clinic WHERE user_id = #{userId}")
    List<UserClinic> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT id, user_id AS userId, clinic_id AS clinicId, role, is_default AS isDefault, created_at AS createdAt, updated_at AS updatedAt FROM user_clinic WHERE user_id = #{userId} AND clinic_id = #{clinicId} LIMIT 1")
    UserClinic selectByUserAndClinic(@Param("userId") Integer userId, @Param("clinicId") String clinicId);

    @Select("SELECT id, user_id AS userId, clinic_id AS clinicId, role, is_default AS isDefault, created_at AS createdAt, updated_at AS updatedAt FROM user_clinic WHERE clinic_id = #{clinicId}")
    List<UserClinic> selectByClinicId(@Param("clinicId") String clinicId);

    @Insert("INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES (#{userId}, #{clinicId}, #{role}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserClinic userClinic);

    @Update("UPDATE user_clinic SET role = #{role}, is_default = #{isDefault} WHERE id = #{id}")
    int update(UserClinic userClinic);

    @Delete("DELETE FROM user_clinic WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM user_clinic WHERE user_id = #{userId} AND clinic_id = #{clinicId}")
    int deleteByUserAndClinic(@Param("userId") Integer userId, @Param("clinicId") String clinicId);

    @Delete("DELETE FROM user_clinic WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);

    @Update("UPDATE user_clinic SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefaultByUserId(@Param("userId") Integer userId);

    @Update("UPDATE user_clinic SET is_default = 1 WHERE user_id = #{userId} AND clinic_id = #{clinicId}")
    int setDefaultClinic(@Param("userId") Integer userId, @Param("clinicId") String clinicId);

    @Select("SELECT COUNT(*) FROM user_clinic WHERE clinic_id = #{clinicId}")
    int countByClinicId(@Param("clinicId") String clinicId);
}
