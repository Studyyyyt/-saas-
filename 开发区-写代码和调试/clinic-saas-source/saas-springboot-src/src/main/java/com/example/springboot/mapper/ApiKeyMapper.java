package com.example.springboot.mapper;

import com.example.springboot.entity.ApiKey;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * API Key 数据访问层（单 Key 模式）
 */
@Mapper
public interface ApiKeyMapper {

    /**
     * 根据 key 值查询 API Key（不校验启用状态，由业务层判断）
     *
     * @param key API Key 字符串
     * @return API Key 实体
     */
    @Select("SELECT id, clinic_id AS clinicId, `key`, name, is_enabled AS isEnabled, " +
            "description, expires_at AS expiresAt, last_used_at AS lastUsedAt, usage_count AS usageCount, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM api_key WHERE `key` = #{key} LIMIT 1")
    ApiKey findByKey(@Param("key") String key);

    /**
     * 查询所有 API Key（单 Key 模式下返回 1 条）
     */
    @Select("SELECT id, clinic_id AS clinicId, `key`, name, is_enabled AS isEnabled, " +
            "description, expires_at AS expiresAt, last_used_at AS lastUsedAt, usage_count AS usageCount, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM api_key ORDER BY created_at DESC LIMIT 1")
    ApiKey findFirst();

    /**
     * 根据诊所ID查询该诊所下的 API Key（单 Key 模式下最多返回 1 条）
     *
     * @param clinicId 诊所ID（支持字符串如门诊拼音）
     * @return API Key 列表
     */
    @Select("SELECT id, clinic_id AS clinicId, `key`, name, is_enabled AS isEnabled, " +
            "description, expires_at AS expiresAt, last_used_at AS lastUsedAt, usage_count AS usageCount, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM api_key WHERE clinic_id = #{clinicId} ORDER BY created_at DESC")
    List<ApiKey> findByClinicId(@Param("clinicId") String clinicId);

    /**
     * 插入一条 API Key 记录
     *
     * @param apiKey API Key 实体
     */
    @Insert("INSERT INTO api_key (clinic_id, `key`, name, is_enabled, description, expires_at, created_at, updated_at) " +
            "VALUES (#{clinicId}, #{key}, #{name}, #{isEnabled}, #{description}, #{expiresAt}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ApiKey apiKey);

    /**
     * 根据诊所ID删除该诊所下的所有 API Key（单 Key 模式下即删除当前 Key）
     *
     * @param clinicId 诊所ID（支持字符串）
     */
    @Delete("DELETE FROM api_key WHERE clinic_id = #{clinicId}")
    void deleteByClinicId(@Param("clinicId") String clinicId);

    /**
     * 更新诊所ID
     *
     * @param oldClinicId 旧诊所ID
     * @param newClinicId 新诊所ID
     */
    @Update("UPDATE api_key SET clinic_id = #{newClinicId}, updated_at = NOW() WHERE clinic_id = #{oldClinicId}")
    void updateClinicId(@Param("oldClinicId") String oldClinicId, @Param("newClinicId") String newClinicId);

    /**
     * 更新 API Key 的最后使用时间和使用次数
     *
     * @param id 主键ID
     */
    @Update("UPDATE api_key SET last_used_at = NOW(), usage_count = usage_count + 1 WHERE id = #{id}")
    void updateUsage(@Param("id") Long id);
}
