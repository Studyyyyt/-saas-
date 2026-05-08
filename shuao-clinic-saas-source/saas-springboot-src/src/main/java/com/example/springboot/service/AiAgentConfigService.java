package com.example.springboot.service;

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
        List<AiAgentConfig> configs = aiAgentConfigMapper.selectByAccountId(accountId);
        // 如果用户没有自定义配置，返回系统默认
        if (configs == null || configs.isEmpty()) {
            configs = aiAgentConfigMapper.selectSystemDefaults();
        }
        return configs;
    }

    public AiAgentConfig getById(Long id) {
        return aiAgentConfigMapper.selectById(id);
    }

    public AiAgentConfig getByAccountAndKey(Long accountId, String agentKey) {
        AiAgentConfig config = aiAgentConfigMapper.selectByAccountIdAndKey(accountId, agentKey);
        if (config == null) {
            config = aiAgentConfigMapper.selectByAccountIdAndKey(null, agentKey);
        }
        return config;
    }

    public void create(AiAgentConfig config) {
        if (config.getAgentKey() == null || config.getAgentKey().trim().isEmpty()) {
            config.setAgentKey("agent_" + System.currentTimeMillis());
        }
        if (config.getSortOrder() == null) {
            config.setSortOrder(10);
        }
        if (config.getIsSystemDefault() == null) {
            config.setIsSystemDefault(false);
        }
        aiAgentConfigMapper.insert(config);
    }

    public void update(AiAgentConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }
        aiAgentConfigMapper.update(config);
    }

    public void delete(Long id) {
        aiAgentConfigMapper.deleteById(id);
    }
}
