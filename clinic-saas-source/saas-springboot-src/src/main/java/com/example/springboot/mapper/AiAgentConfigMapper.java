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
            @Result(property = "presetMessage", column = "preset_message"),
            @Result(property = "endpointUrl", column = "endpoint_url"),
            @Result(property = "authType", column = "auth_type"),
            @Result(property = "authToken", column = "auth_token"),
            @Result(property = "requestTemplate", column = "request_template"),
            @Result(property = "responseType", column = "response_type"),
            @Result(property = "timeoutSeconds", column = "timeout_seconds"),
            @Result(property = "uiMode", column = "ui_mode"),
            @Result(property = "uiConfigJson", column = "ui_config_json"),
            @Result(property = "usageLocation", column = "usage_location"),
            @Result(property = "isVisibleOnHome", column = "is_visible_on_home"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, preset_message, endpoint_url, auth_type, auth_token, request_template, response_type, timeout_seconds, ui_mode, ui_config_json, usage_location, is_visible_on_home, sort_order, created_at, updated_at FROM ai_agent_config WHERE id = #{id}")
    AiAgentConfig selectById(Long id);

    @ResultMap("agentConfigMap")
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, preset_message, endpoint_url, auth_type, auth_token, request_template, response_type, timeout_seconds, ui_mode, ui_config_json, usage_location, is_visible_on_home, sort_order, created_at, updated_at FROM ai_agent_config WHERE account_id = #{accountId} ORDER BY sort_order, id")
    List<AiAgentConfig> selectByAccountId(@Param("accountId") Long accountId);

    @Select("SELECT DISTINCT agent_key FROM ai_agent_config")
    List<String> selectAllAgentKeys();

    @ResultMap("agentConfigMap")
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, preset_message, endpoint_url, auth_type, auth_token, request_template, response_type, timeout_seconds, ui_mode, ui_config_json, usage_location, is_visible_on_home, sort_order, created_at, updated_at FROM ai_agent_config WHERE account_id = #{accountId} AND agent_key = #{agentKey} LIMIT 1")
    AiAgentConfig selectByAccountIdAndKey(@Param("accountId") Long accountId, @Param("agentKey") String agentKey);

    @Insert("INSERT INTO ai_agent_config (account_id, agent_key, name, icon, description, gradient, chips, preset_message, endpoint_url, auth_type, auth_token, request_template, response_type, timeout_seconds, ui_mode, ui_config_json, usage_location, is_visible_on_home, sort_order) VALUES (#{accountId}, #{agentKey}, #{name}, #{icon}, #{description}, #{gradient}, #{chips, typeHandler=com.example.springboot.config.JsonListTypeHandler}, #{presetMessage}, #{endpointUrl}, #{authType}, #{authToken}, #{requestTemplate}, #{responseType}, #{timeoutSeconds}, #{uiMode}, #{uiConfigJson}, #{usageLocation}, #{isVisibleOnHome}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiAgentConfig config);

    @Update({
            "<script>",
            "UPDATE ai_agent_config",
            "<set>",
            "<if test='agentKey != null'>agent_key = #{agentKey},</if>",
            "<if test='name != null'>name = #{name},</if>",
            "<if test='icon != null'>icon = #{icon},</if>",
            "<if test='description != null'>description = #{description},</if>",
            "<if test='gradient != null'>gradient = #{gradient},</if>",
            "<if test='chips != null'>chips = #{chips, typeHandler=com.example.springboot.config.JsonListTypeHandler},</if>",
            "<if test='presetMessage != null'>preset_message = #{presetMessage},</if>",
            "<if test='endpointUrl != null'>endpoint_url = #{endpointUrl},</if>",
            "<if test='authType != null'>auth_type = #{authType},</if>",
            "<if test='authToken != null'>auth_token = #{authToken},</if>",
            "<if test='requestTemplate != null'>request_template = #{requestTemplate},</if>",
            "<if test='responseType != null'>response_type = #{responseType},</if>",
            "<if test='timeoutSeconds != null'>timeout_seconds = #{timeoutSeconds},</if>",
            "<if test='uiMode != null'>ui_mode = #{uiMode},</if>",
            "<if test='uiConfigJson != null'>ui_config_json = #{uiConfigJson},</if>",
            "<if test='usageLocation != null'>usage_location = #{usageLocation},</if>",
            "<if test='isVisibleOnHome != null'>is_visible_on_home = #{isVisibleOnHome},</if>",
            "<if test='sortOrder != null'>sort_order = #{sortOrder},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void update(AiAgentConfig config);

    @Delete("DELETE FROM ai_agent_config WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    /**
     * 按 accountId 和 agentKey 查询最佳匹配配置
     * 优先返回指定 account_id 的配置，没有则返回系统默认（account_id = 0 或 NULL）
     */
    @ResultMap("agentConfigMap")
    @Select("SELECT id, account_id, agent_key, name, icon, description, gradient, chips, preset_message, endpoint_url, auth_type, auth_token, request_template, response_type, timeout_seconds, ui_mode, ui_config_json, usage_location, is_visible_on_home, sort_order, created_at, updated_at FROM ai_agent_config WHERE (account_id = #{accountId} OR account_id = 0 OR account_id IS NULL) AND agent_key = #{agentKey} ORDER BY account_id DESC LIMIT 1")
    AiAgentConfig selectBestMatchByAccountIdAndKey(@Param("accountId") Long accountId, @Param("agentKey") String agentKey);
}
