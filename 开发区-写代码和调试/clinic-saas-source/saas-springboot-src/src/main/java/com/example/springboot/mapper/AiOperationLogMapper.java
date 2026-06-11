package com.example.springboot.mapper;

import com.example.springboot.entity.AiOperationLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface AiOperationLogMapper {

    @Results(id = "operationLogMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "functionKey", column = "function_key"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "inputSnapshot", column = "input_snapshot"),
            @Result(property = "aiOutput", column = "ai_output"),
            @Result(property = "isAdopted", column = "is_adopted"),
            @Result(property = "tokenUsed", column = "token_used"),
            @Result(property = "errorMsg", column = "error_msg"),
            @Result(property = "createTime", column = "create_time")
    })
    @Select("SELECT id, function_key AS functionKey, account_id AS accountId, input_snapshot AS inputSnapshot, ai_output AS aiOutput, is_adopted AS isAdopted, token_used AS tokenUsed, error_msg AS errorMsg, create_time AS createTime FROM ai_operation_log WHERE id = #{id}")
    AiOperationLog selectById(Long id);

    @Select("SELECT COUNT(*) FROM ai_operation_log WHERE create_time >= #{todayStart}")
    int countTodayCalls(@Param("todayStart") LocalDateTime todayStart);

    @Select("SELECT COALESCE(SUM(token_used), 0) FROM ai_operation_log WHERE create_time >= #{todayStart}")
    int sumTodayTokens(@Param("todayStart") LocalDateTime todayStart);

    @Select("SELECT COUNT(*) FROM ai_operation_log WHERE create_time >= #{todayStart} AND error_msg IS NOT NULL AND error_msg != ''")
    int countTodayErrors(@Param("todayStart") LocalDateTime todayStart);

    @Insert("INSERT INTO ai_operation_log (function_key, account_id, input_snapshot, ai_output, is_adopted, token_used, error_msg) VALUES (#{functionKey}, #{accountId}, #{inputSnapshot}, #{aiOutput}, #{isAdopted}, #{tokenUsed}, #{errorMsg})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiOperationLog log);
}
