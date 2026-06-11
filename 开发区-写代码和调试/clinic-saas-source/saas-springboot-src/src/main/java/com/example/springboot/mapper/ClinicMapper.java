package com.example.springboot.mapper;

import com.example.springboot.entity.Clinic;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClinicMapper {

    @Select("SELECT id, name, address, contact_phone AS contactPhone, status, activation_code AS activationCode, license_expires_at AS licenseExpiresAt, created_at AS createdAt, updated_at AS updatedAt FROM clinic WHERE id = #{id}")
    Clinic selectById(String id);

    @Select("SELECT id, name, address, contact_phone AS contactPhone, status, activation_code AS activationCode, license_expires_at AS licenseExpiresAt, created_at AS createdAt, updated_at AS updatedAt FROM clinic WHERE status = 1 ORDER BY created_at DESC")
    List<Clinic> selectAllActive();

    @Select({
            "<script>",
            "SELECT id, name, address, contact_phone AS contactPhone, status, activation_code AS activationCode, license_expires_at AS licenseExpiresAt, created_at AS createdAt, updated_at AS updatedAt FROM clinic WHERE 1=1",
            "<if test='name != null and name != \"\"'> AND name LIKE CONCAT('%', #{name}, '%') </if>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    List<Clinic> selectList(@Param("name") String name);

    @Insert("INSERT INTO clinic (id, name, address, contact_phone, status, activation_code, license_expires_at) VALUES (#{id}, #{name}, #{address}, #{contactPhone}, #{status}, #{activationCode}, #{licenseExpiresAt})")
    int insert(Clinic clinic);

    @Update({
            "<script>",
            "UPDATE clinic",
            "<set>",
            "<if test='name != null'> name = #{name}, </if>",
            "<if test='address != null'> address = #{address}, </if>",
            "<if test='contactPhone != null'> contact_phone = #{contactPhone}, </if>",
            "<if test='status != null'> status = #{status}, </if>",
            "<if test='activationCode != null'> activation_code = #{activationCode}, </if>",
            "<if test='licenseExpiresAt != null'> license_expires_at = #{licenseExpiresAt}, </if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int update(Clinic clinic);

    @Delete("DELETE FROM clinic WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM clinic WHERE name = #{name}")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM clinic WHERE id = #{id}")
    int countById(@Param("id") String id);
}
