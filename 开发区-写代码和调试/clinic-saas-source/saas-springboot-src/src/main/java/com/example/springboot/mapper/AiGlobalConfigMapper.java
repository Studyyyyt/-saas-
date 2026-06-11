package com.example.springboot.mapper;

import com.example.springboot.entity.AiGlobalConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiGlobalConfigMapper {

    @Results(id = "globalConfigMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "configKey", column = "config_key"),
            @Result(property = "configValue", column = "config_value"),
            @Result(property = "description", column = "description"),
            @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT id, config_key AS configKey, config_value AS configValue, description, update_time AS updateTime FROM ai_global_config WHERE config_key = #{configKey} LIMIT 1")
    AiGlobalConfig selectByKey(@Param("configKey") String configKey);

    @Update("UPDATE ai_global_config SET config_value = #{configValue} WHERE config_key = #{configKey}")
    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
