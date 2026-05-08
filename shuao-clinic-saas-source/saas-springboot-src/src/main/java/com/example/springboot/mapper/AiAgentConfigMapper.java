package com.example.springboot.mapper;

import com.example.springboot.config.JsonListTypeHandler;
import com.example.springboot.entity.AiAgentConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiAgentConfigMapper {

    @Results(id = "agentConfigMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "agentKey", column = "agent_key"),
            @Result(property = "name", column = "name"),
            @Result(property = "icon", column = "icon"),
            @Result(property = "description", column = "description"),
            @Result(property = "gradient", column = "gradient"),
            @Result(property = "chips", column = "chips", typeHandler = JsonListTypeHandler.class),
            @Result(property = "systemPrompt", column = "system_prompt"),
            @Result(property = "enabledTools", column = "enabled_tools", typeHandler = JsonListTypeHandler.class),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "isSystemDefault", column = "is_system_default"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, system_prompt, enabled_tools, sort_order, is_system_default, created_at, updated_at FROM ai_agent_config WHERE id = #{id}")
    AiAgentConfig selectById(Long id);

    @ResultMap("agentConfigMap")
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, system_prompt, enabled_tools, sort_order, is_system_default, created_at, updated_at FROM ai_agent_config WHERE account_id = #{accountId} OR account_id IS NULL ORDER BY sort_order, id")
    List<AiAgentConfig> selectByAccountId(@Param("accountId") Long accountId);

    @ResultMap("agentConfigMap")
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, system_prompt, enabled_tools, sort_order, is_system_default, created_at, updated_at FROM ai_agent_config WHERE (account_id = #{accountId} OR account_id IS NULL) AND agent_key = #{agentKey} LIMIT 1")
    AiAgentConfig selectByAccountIdAndKey(@Param("accountId") Long accountId, @Param("agentKey") String agentKey);

    @Insert("INSERT INTO ai_agent_config (account_id, agent_key, name, icon, description, gradient, chips, system_prompt, enabled_tools, sort_order, is_system_default) VALUES (#{accountId}, #{agentKey}, #{name}, #{icon}, #{description}, #{gradient}, #{chips, typeHandler=com.example.springboot.config.JsonListTypeHandler}, #{systemPrompt}, #{enabledTools, typeHandler=com.example.springboot.config.JsonListTypeHandler}, #{sortOrder}, #{isSystemDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiAgentConfig config);

    @Update({
            "<script>",
            "UPDATE ai_agent_config",
            "<set>",
            "<if test='name != null'>name = #{name},</if>",
            "<if test='icon != null'>icon = #{icon},</if>",
            "<if test='description != null'>description = #{description},</if>",
            "<if test='gradient != null'>gradient = #{gradient},</if>",
            "<if test='chips != null'>chips = #{chips, typeHandler=com.example.springboot.config.JsonListTypeHandler},</if>",
            "<if test='systemPrompt != null'>system_prompt = #{systemPrompt},</if>",
            "<if test='enabledTools != null'>enabled_tools = #{enabledTools, typeHandler=com.example.springboot.config.JsonListTypeHandler},</if>",
            "<if test='sortOrder != null'>sort_order = #{sortOrder},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void update(AiAgentConfig config);

    @Delete("DELETE FROM ai_agent_config WHERE id = #{id} AND is_system_default = 0")
    void deleteById(@Param("id") Long id);

    @ResultMap("agentConfigMap")
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, system_prompt, enabled_tools, sort_order, is_system_default, created_at, updated_at FROM ai_agent_config WHERE is_system_default = 1 ORDER BY sort_order")
    List<AiAgentConfig> selectSystemDefaults();
}
