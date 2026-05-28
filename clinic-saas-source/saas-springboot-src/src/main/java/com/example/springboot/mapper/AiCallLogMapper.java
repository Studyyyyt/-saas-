package com.example.springboot.mapper;

import com.example.springboot.entity.AiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface AiCallLogMapper {

    int insert(AiCallLog log);

    List<AiCallLog> selectList(@Param("accountId") Long accountId,
                               @Param("agentKey") String agentKey,
                               @Param("responseStatus") String responseStatus,
                               @Param("startDate") Date startDate,
                               @Param("endDate") Date endDate);

    /**
     * 按 Agent 统计调用次数与平均耗时
     */
    List<Map<String, Object>> selectAgentStats(@Param("accountId") Long accountId,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    /**
     * 按日期统计调用次数
     */
    List<Map<String, Object>> selectDailyStats(@Param("accountId") Long accountId,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    /**
     * 汇总统计：总次数、成功率、平均耗时
     */
    Map<String, Object> selectSummary(@Param("accountId") Long accountId,
                                      @Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate);
}
