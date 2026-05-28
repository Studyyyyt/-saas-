package com.example.springboot.mapper;

import com.example.springboot.entity.AiFunctionMapping;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI 系统功能与 Agent 绑定映射 Mapper
 */
@Mapper
public interface AiFunctionMappingMapper {

    @Results(id = "functionMappingMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "functionCode", column = "function_code"),
            @Result(property = "functionName", column = "function_name"),
            @Result(property = "agentKey", column = "agent_key"),
            @Result(property = "isVisibleOnPage", column = "is_visible_on_page"),
            @Result(property = "isVisibleOnHome", column = "is_visible_on_home"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT id, account_id, function_code, function_name, agent_key, is_visible_on_page, is_visible_on_home, sort_order, created_at, updated_at FROM ai_function_mapping WHERE id = #{id}")
    AiFunctionMapping selectById(Long id);

    @ResultMap("functionMappingMap")
    @Select("SELECT id, account_id, function_code, function_name, agent_key, is_visible_on_page, is_visible_on_home, sort_order, created_at, updated_at FROM ai_function_mapping WHERE account_id = #{accountId} ORDER BY sort_order, id")
    List<AiFunctionMapping> selectByAccountId(@Param("accountId") Long accountId);

    @ResultMap("functionMappingMap")
    @Select("SELECT id, account_id, function_code, function_name, agent_key, is_visible_on_page, is_visible_on_home, sort_order, created_at, updated_at FROM ai_function_mapping WHERE (account_id = #{accountId} OR account_id IS NULL) AND function_code = #{functionCode} ORDER BY account_id DESC LIMIT 1")
    AiFunctionMapping selectByAccountAndCode(@Param("accountId") Long accountId, @Param("functionCode") String functionCode);

    @ResultMap("functionMappingMap")
    @Select("SELECT id, account_id, function_code, function_name, agent_key, is_visible_on_page, is_visible_on_home, sort_order, created_at, updated_at FROM ai_function_mapping WHERE account_id IS NULL ORDER BY sort_order, id")
    List<AiFunctionMapping> selectSystemDefaults();

    @Insert("INSERT INTO ai_function_mapping (account_id, function_code, function_name, agent_key, is_visible_on_page, is_visible_on_home, sort_order) VALUES (#{accountId}, #{functionCode}, #{functionName}, #{agentKey}, #{isVisibleOnPage}, #{isVisibleOnHome}, #{sortOrder}) ON DUPLICATE KEY UPDATE agent_key = #{agentKey}, is_visible_on_page = #{isVisibleOnPage}, is_visible_on_home = #{isVisibleOnHome}, sort_order = #{sortOrder}")
    int upsert(AiFunctionMapping mapping);

    @Update("UPDATE ai_function_mapping SET agent_key = #{agentKey}, is_visible_on_page = #{isVisibleOnPage}, is_visible_on_home = #{isVisibleOnHome}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(AiFunctionMapping mapping);

    @Delete("DELETE FROM ai_function_mapping WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Insert("INSERT INTO ai_function_mapping (account_id, function_code, function_name, agent_key, is_visible_on_page, is_visible_on_home, sort_order) VALUES (#{accountId}, #{functionCode}, #{functionName}, #{agentKey}, #{isVisibleOnPage}, #{isVisibleOnHome}, #{sortOrder})")
    int insert(AiFunctionMapping mapping);

    @Delete("DELETE FROM ai_function_mapping WHERE account_id IS NULL AND function_code = #{functionCode}")
    int deleteSystemDefaultByCode(@Param("functionCode") String functionCode);
}
