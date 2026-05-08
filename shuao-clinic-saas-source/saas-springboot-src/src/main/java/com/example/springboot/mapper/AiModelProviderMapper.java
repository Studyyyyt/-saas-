package com.example.springboot.mapper;

import com.example.springboot.entity.AiModelProvider;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiModelProviderMapper {

    @Results(id = "modelProviderMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "providerName", column = "provider_name"),
            @Result(property = "baseUrl", column = "base_url"),
            @Result(property = "apiKey", column = "api_key"),
            @Result(property = "modelName", column = "model_name"),
            @Result(property = "reasoningEffort", column = "reasoning_effort"),
            @Result(property = "maxOutputTokens", column = "max_output_tokens"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "apiType", column = "api_type"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT id, provider_name, base_url, api_key, model_name, reasoning_effort, max_output_tokens, enabled, api_type, created_at, updated_at FROM ai_model_provider ORDER BY id LIMIT 1")
    AiModelProvider selectFirst();

    @ResultMap("modelProviderMap")
    @Select("SELECT id, provider_name, base_url, api_key, model_name, reasoning_effort, max_output_tokens, enabled, api_type, created_at, updated_at FROM ai_model_provider WHERE id = #{id}")
    AiModelProvider selectById(Long id);

    @Insert("INSERT INTO ai_model_provider (provider_name, base_url, api_key, model_name, reasoning_effort, max_output_tokens, enabled, api_type) VALUES (#{providerName}, #{baseUrl}, #{apiKey}, #{modelName}, #{reasoningEffort}, #{maxOutputTokens}, #{enabled}, #{apiType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiModelProvider provider);

    @Update("UPDATE ai_model_provider SET provider_name = #{providerName}, base_url = #{baseUrl}, model_name = #{modelName}, reasoning_effort = #{reasoningEffort}, max_output_tokens = #{maxOutputTokens}, enabled = #{enabled}, api_type = #{apiType} WHERE id = #{id}")
    void update(AiModelProvider provider);

    @Update("UPDATE ai_model_provider SET api_key = #{apiKey} WHERE id = #{id}")
    void updateApiKey(@Param("id") Long id, @Param("apiKey") String apiKey);

    @Delete("DELETE FROM ai_model_provider WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM ai_model_provider")
    int count();
}
