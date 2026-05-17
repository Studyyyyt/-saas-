package com.example.springboot.mapper;

import com.example.springboot.entity.ApiKey;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * API Key 数据访问层
 */
@Mapper
public interface ApiKeyMapper {

    /**
     * 根据 key 值查询启用的 API Key
     *
     * @param key API Key 字符串
     * @return API Key 实体
     */
    @Select("SELECT * FROM api_key WHERE `key` = #{key} AND is_enabled = 1 LIMIT 1")
    ApiKey findByKey(@Param("key") String key);

    /**
     * 根据诊所ID查询该诊所下的所有 API Key
     *
     * @param clinicId 诊所ID
     * @return API Key 列表
     */
    @Select("SELECT * FROM api_key WHERE clinic_id = #{clinicId} ORDER BY created_at DESC")
    List<ApiKey> findByClinicId(@Param("clinicId") Long clinicId);

    /**
     * 插入一条 API Key 记录
     *
     * @param apiKey API Key 实体
     */
    @Insert("INSERT INTO api_key (clinic_id, `key`, name, is_enabled, created_at, updated_at) " +
            "VALUES (#{clinicId}, #{key}, #{name}, #{isEnabled}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ApiKey apiKey);

    /**
     * 根据诊所ID删除该诊所下的所有 API Key
     *
     * @param clinicId 诊所ID
     */
    @Delete("DELETE FROM api_key WHERE clinic_id = #{clinicId}")
    void deleteByClinicId(@Param("clinicId") Long clinicId);

    /**
     * 更新 API Key 记录
     *
     * @param apiKey API Key 实体
     */
    @Update("UPDATE api_key SET clinic_id = #{clinicId}, `key` = #{key}, name = #{name}, " +
            "is_enabled = #{isEnabled}, updated_at = NOW() WHERE id = #{id}")
    void update(ApiKey apiKey);
}
