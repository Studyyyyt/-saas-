package com.example.springboot.service;

import com.example.springboot.controller.dto.GlobalConfigDTO;
import com.example.springboot.controller.vo.OverviewVO;
import com.example.springboot.entity.AiFunctionConfig;
import com.example.springboot.entity.AiGlobalConfig;
import com.example.springboot.entity.AiOperationLog;
import com.example.springboot.mapper.AiFunctionConfigMapper;
import com.example.springboot.mapper.AiGlobalConfigMapper;
import com.example.springboot.mapper.AiOperationLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiConfigService {

    private final AiFunctionConfigMapper functionMapper;
    private final AiOperationLogMapper logMapper;
    private final AiGlobalConfigMapper globalConfigMapper;
    private final com.example.springboot.mapper.AiAgentConfigMapper aiAgentConfigMapper;

    public AiConfigService(AiFunctionConfigMapper functionMapper,
                           AiOperationLogMapper logMapper,
                           AiGlobalConfigMapper globalConfigMapper,
                           com.example.springboot.mapper.AiAgentConfigMapper aiAgentConfigMapper) {
        this.functionMapper = functionMapper;
        this.logMapper = logMapper;
        this.globalConfigMapper = globalConfigMapper;
        this.aiAgentConfigMapper = aiAgentConfigMapper;
    }

    /**
     * 获取 AI 总览统计数据
     */
    public OverviewVO getOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        int todayCalls = logMapper.countTodayCalls(todayStart);
        int todayTokens = logMapper.sumTodayTokens(todayStart);
        int activeFunctions = functionMapper.countActive();
        int totalFunctions = functionMapper.countTotal();
        int errorCount = logMapper.countTodayErrors(todayStart);
        double errorRate = todayCalls > 0 ? (errorCount * 100.0 / todayCalls) : 0.0;

        OverviewVO vo = new OverviewVO();
        vo.setTodayCalls(todayCalls);
        vo.setTodayTokens(todayTokens);
        vo.setTodayTokensFormatted(formatTokens(todayTokens));
        vo.setActiveFunctions(activeFunctions);
        vo.setTotalFunctions(totalFunctions);
        vo.setErrorRate(Math.round(errorRate * 10.0) / 10.0);
        vo.setGlobalEnabled(getGlobalEnabled());
        vo.setDebugMode(getDebugMode());

        return vo;
    }

    /**
     * 获取所有 AI 功能配置列表
     */
    public List<AiFunctionConfig> getFunctionList() {
        return functionMapper.selectAll();
    }

    /**
     * 更新全局配置
     */
    public void updateGlobalConfig(GlobalConfigDTO dto) {
        if (dto.getGlobalEnabled() != null) {
            globalConfigMapper.updateValueByKey("global_enabled", String.valueOf(dto.getGlobalEnabled()));
        }
        if (dto.getDebugMode() != null) {
            globalConfigMapper.updateValueByKey("debug_mode", String.valueOf(dto.getDebugMode()));
        }
    }

    /**
     * 更新单个功能启用状态
     */
    public void updateFunctionStatus(String key, Boolean enabled) {
        functionMapper.updateEnabledByKey(key, enabled);
    }

    /**
     * 获取全局 AI 开关状态
     */
    public Boolean getGlobalEnabled() {
        AiGlobalConfig config = globalConfigMapper.selectByKey("global_enabled");
        return config != null && Boolean.parseBoolean(config.getConfigValue());
    }

    /**
     * 获取调试模式状态
     */
    public Boolean getDebugMode() {
        AiGlobalConfig config = globalConfigMapper.selectByKey("debug_mode");
        return config != null && Boolean.parseBoolean(config.getConfigValue());
    }

    /**
     * 断言指定 AI 功能可用：先检查全局开关，再检查功能开关
     * 任一关闭则抛出 IllegalStateException，阻止后续 AI 调用
     *
     * 【设计说明】
     * 1. ai_function_config 表中的记录对应系统预定义功能（如病历扩写、经营分析等），
     *    管理员可在 AI 智能中心总览页统一开启/关闭。
     * 2. 用户自定义 Agent（配置在 ai_agent_config 表）不受 ai_function_config 开关限制，
     *    只要全局开关开启且 Agent 在白名单中即视为可用。
     */
    public void assertAiEnabled(String functionKey) {
        if (!getGlobalEnabled()) {
            throw new IllegalStateException("AI 功能已全局关闭，请在「系统设置 - AI 智能中心」开启后再试");
        }
        AiFunctionConfig funcConfig = functionMapper.selectByKey(functionKey);
        if (funcConfig != null && Boolean.TRUE.equals(funcConfig.getIsEnabled())) {
            return;
        }
        if (funcConfig != null && !Boolean.TRUE.equals(funcConfig.getIsEnabled())) {
            throw new IllegalStateException("该 AI 功能已禁用，请在「系统设置 - AI 智能中心」开启后再试");
        }
        // funcConfig == null：ai_function_config 无此功能记录，说明是用户自定义 Agent，直接放行
    }

    /**
     * 获取指定功能是否启用
     */
    public Boolean getFunctionEnabled(String key) {
        AiFunctionConfig config = functionMapper.selectByKey(key);
        return config != null && Boolean.TRUE.equals(config.getIsEnabled());
    }

    /**
     * 记录 AI 操作日志（支撑今日调用统计与错误率计算）
     */
    public void logAiOperation(String functionKey, Long accountId, String inputSnapshot,
                               String aiOutput, Integer tokenUsed, String errorMsg) {
        AiOperationLog log = new AiOperationLog();
        log.setFunctionKey(functionKey);
        log.setAccountId(accountId);
        log.setInputSnapshot(inputSnapshot);
        log.setAiOutput(aiOutput);
        log.setTokenUsed(tokenUsed != null ? tokenUsed : 0);
        log.setErrorMsg(errorMsg);
        logMapper.insert(log);
    }

    /**
     * 格式化 Token 数量显示
     */
    private String formatTokens(int tokens) {
        if (tokens >= 10000) {
            return String.format("%.1fK", tokens / 1000.0);
        } else if (tokens >= 1000) {
            return String.format("%.1fK", tokens / 1000.0);
        }
        return String.valueOf(tokens);
    }
}
