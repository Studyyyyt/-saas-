package com.example.springboot.service;

import com.example.springboot.common.AesEncryptor;
import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.mapper.AiAgentConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAgentConfigService {

    private final AiAgentConfigMapper aiAgentConfigMapper;

    public AiAgentConfigService(AiAgentConfigMapper aiAgentConfigMapper) {
        this.aiAgentConfigMapper = aiAgentConfigMapper;
    }

    public List<AiAgentConfig> listByAccount(Long accountId) {
        List<AiAgentConfig> list = aiAgentConfigMapper.selectByAccountId(accountId);
        if (list != null) {
            for (AiAgentConfig config : list) {
                maskAuthToken(config);
            }
        }
        return list;
    }

    public AiAgentConfig getById(Long id) {
        AiAgentConfig config = aiAgentConfigMapper.selectById(id);
        return maskAuthToken(config);
    }

    public AiAgentConfig getByAccountAndKey(Long accountId, String agentKey) {
        AiAgentConfig config = aiAgentConfigMapper.selectByAccountIdAndKey(accountId, agentKey);
        return maskAuthToken(config);
    }

    /**
     * 查询最佳匹配配置（优先指定 account，其次系统默认 account_id=0 或 NULL）
     * 返回原始对象（不脱敏），供后端服务内部调用
     */
    public AiAgentConfig getBestMatchByAccountAndKey(Long accountId, String agentKey) {
        return aiAgentConfigMapper.selectBestMatchByAccountIdAndKey(accountId, agentKey);
    }

    public void create(AiAgentConfig config) {
        if (config.getAgentKey() == null || config.getAgentKey().trim().isEmpty()) {
            config.setAgentKey("agent_" + System.currentTimeMillis());
        }
        if (config.getSortOrder() == null) {
            config.setSortOrder(10);
        }
        if (config.getUiMode() == null || config.getUiMode().trim().isEmpty()) {
            config.setUiMode("json");
        }
        if (config.getAuthToken() != null && !config.getAuthToken().isEmpty() && !"****".equals(config.getAuthToken())) {
            config.setAuthToken(AesEncryptor.encrypt(config.getAuthToken()));
        }
        aiAgentConfigMapper.insert(config);
    }

    public void update(AiAgentConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }
        String token = config.getAuthToken();
        if (token != null && !token.isEmpty() && !"****".equals(token)) {
            config.setAuthToken(AesEncryptor.encrypt(token));
        } else if ("****".equals(token) || (token != null && token.isEmpty())) {
            // 前端未修改 token，保留原值
            AiAgentConfig existing = aiAgentConfigMapper.selectById(config.getId());
            if (existing != null) {
                config.setAuthToken(existing.getAuthToken());
            }
        }
        aiAgentConfigMapper.update(config);
    }

    public void delete(Long id) {
        aiAgentConfigMapper.deleteById(id);
    }

    private AiAgentConfig maskAuthToken(AiAgentConfig config) {
        if (config != null && config.getAuthToken() != null && !config.getAuthToken().isEmpty()) {
            config.setAuthToken("****");
        }
        return config;
    }
}
