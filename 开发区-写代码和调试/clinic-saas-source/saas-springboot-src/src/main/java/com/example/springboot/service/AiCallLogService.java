package com.example.springboot.service;

import com.example.springboot.entity.AiCallLog;
import com.example.springboot.mapper.AiCallLogMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AiCallLogService {

    private final AiCallLogMapper aiCallLogMapper;

    public AiCallLogService(AiCallLogMapper aiCallLogMapper) {
        this.aiCallLogMapper = aiCallLogMapper;
    }

    public void save(AiCallLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }
        aiCallLogMapper.insert(log);
    }

    public List<AiCallLog> list(Long accountId, String agentKey, String responseStatus, Date startDate, Date endDate) {
        return aiCallLogMapper.selectList(accountId, agentKey, responseStatus, startDate, endDate);
    }

    public List<Map<String, Object>> agentStats(Long accountId, Date startDate, Date endDate) {
        return aiCallLogMapper.selectAgentStats(accountId, startDate, endDate);
    }

    public List<Map<String, Object>> dailyStats(Long accountId, Date startDate, Date endDate) {
        return aiCallLogMapper.selectDailyStats(accountId, startDate, endDate);
    }

    public Map<String, Object> summary(Long accountId, Date startDate, Date endDate) {
        return aiCallLogMapper.selectSummary(accountId, startDate, endDate);
    }
}
