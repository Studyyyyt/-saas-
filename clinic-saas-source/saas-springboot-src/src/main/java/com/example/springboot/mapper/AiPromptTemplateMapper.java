package com.example.springboot.mapper;

import com.example.springboot.entity.AiPromptTemplate;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiPromptTemplateMapper {

    @Results(id = "promptTemplateMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "scene", column = "scene"),
            @Result(property = "name", column = "name"),
            @Result(property = "systemPrompt", column = "system_prompt"),
            @Result(property = "temperature", column = "temperature"),
            @Result(property = "maxTokens", column = "max_tokens"),
            @Result(property = "responseFormat", column = "response_format"),
            @Result(property = "jsonSchema", column = "json_schema"),
            @Result(property = "extraConfig", column = "extra_config"),
            @Result(property = "isActive", column = "is_active"),
            @Result(property = "version", column = "version"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT id, scene, name, system_prompt, temperature, max_tokens, response_format, json_schema, extra_config, is_active, version, create_time, update_time FROM ai_prompt_template WHERE scene = #{scene} LIMIT 1")
    AiPromptTemplate selectByScene(String scene);

    @ResultMap("promptTemplateMap")
    @Select("SELECT id, scene, name, system_prompt, temperature, max_tokens, response_format, json_schema, extra_config, is_active, version, create_time, update_time FROM ai_prompt_template WHERE id = #{id}")
    AiPromptTemplate selectById(Long id);

    @Insert("INSERT INTO ai_prompt_template (scene, name, system_prompt, temperature, max_tokens, response_format, json_schema, extra_config, is_active, version) VALUES (#{scene}, #{name}, #{systemPrompt}, #{temperature}, #{maxTokens}, #{responseFormat}, #{jsonSchema}, #{extraConfig}, #{isActive}, #{version})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiPromptTemplate template);

    @Update({
            "<script>",
            "UPDATE ai_prompt_template",
            "<set>",
            "<if test='name != null'>name = #{name},</if>",
            "<if test='systemPrompt != null'>system_prompt = #{systemPrompt},</if>",
            "<if test='temperature != null'>temperature = #{temperature},</if>",
            "<if test='maxTokens != null'>max_tokens = #{maxTokens},</if>",
            "<if test='responseFormat != null'>response_format = #{responseFormat},</if>",
            "<if test='jsonSchema != null'>json_schema = #{jsonSchema},</if>",
            "<if test='extraConfig != null'>extra_config = #{extraConfig},</if>",
            "<if test='isActive != null'>is_active = #{isActive},</if>",
            "<if test='version != null'>version = #{version},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void update(AiPromptTemplate template);
}
