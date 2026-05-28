package com.example.springboot.mapper;

import com.example.springboot.entity.AiFunctionConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiFunctionConfigMapper {

    @Results(id = "functionConfigMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "functionKey", column = "function_key"),
            @Result(property = "functionName", column = "function_name"),
            @Result(property = "pagePath", column = "page_path"),
            @Result(property = "icon", column = "icon"),
            @Result(property = "isEnabled", column = "is_enabled"),
            @Result(property = "modelName", column = "model_name"),
            @Result(property = "promptTemplateId", column = "prompt_template_id"),
            @Result(property = "extraConfig", column = "extra_config"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT id, function_key, function_name, page_path, icon, is_enabled, model_name, prompt_template_id, extra_config, sort_order, create_time, update_time FROM ai_function_config ORDER BY sort_order")
    List<AiFunctionConfig> selectAll();

    @ResultMap("functionConfigMap")
    @Select("SELECT id, function_key, function_name, page_path, icon, is_enabled, model_name, prompt_template_id, extra_config, sort_order, create_time, update_time FROM ai_function_config WHERE function_key = #{functionKey} LIMIT 1")
    AiFunctionConfig selectByKey(@Param("functionKey") String functionKey);

    @Update("UPDATE ai_function_config SET is_enabled = #{isEnabled} WHERE function_key = #{functionKey}")
    int updateEnabledByKey(@Param("functionKey") String functionKey, @Param("isEnabled") Boolean isEnabled);

    @Select("SELECT COUNT(*) FROM ai_function_config WHERE is_enabled = 1")
    int countActive();

    @Select("SELECT COUNT(*) FROM ai_function_config")
    int countTotal();
}
