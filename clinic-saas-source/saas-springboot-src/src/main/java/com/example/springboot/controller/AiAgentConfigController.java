package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.service.AiAgentConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-agent-configs")
public class AiAgentConfigController {

    private final AiAgentConfigService aiAgentConfigService;

    public AiAgentConfigController(AiAgentConfigService aiAgentConfigService) {
        this.aiAgentConfigService = aiAgentConfigService;
    }

    @GetMapping
    public Result list(@RequestParam(required = false) Long accountId) {
        if (accountId == null) {
            return Result.error("401", "未登录");
        }
        return Result.success(aiAgentConfigService.listByAccount(accountId));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.success(aiAgentConfigService.getById(id));
    }

    @GetMapping("/by-key")
    public Result byKey(@RequestParam(required = false) Long accountId,
                        @RequestParam String agentKey) {
        if (accountId == null) {
            return Result.error("401", "未登录");
        }
        return Result.success(aiAgentConfigService.getByAccountAndKey(accountId, agentKey));
    }

    @PostMapping
    public Result create(@RequestBody AiAgentConfig config) {
        if (config == null || config.getAccountId() == null) {
            return Result.error("401", "未登录");
        }
        try {
            aiAgentConfigService.create(config);
            if (config.getAuthToken() != null && !config.getAuthToken().isEmpty()) {
                config.setAuthToken("****");
            }
            return Result.success(config);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody AiAgentConfig config) {
        if (config == null || config.getAccountId() == null) {
            return Result.error("401", "未登录");
        }
        config.setId(id);
        aiAgentConfigService.update(config);
        if (config.getAuthToken() != null && !config.getAuthToken().isEmpty()) {
            config.setAuthToken("****");
        }
        return Result.success(config);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        // 先查询原配置，若不存在则返回错误，起到一定的权限隔离作用
        AiAgentConfig existing = aiAgentConfigService.getById(id);
        if (existing == null) {
            return Result.error("404", "配置不存在");
        }
        aiAgentConfigService.delete(id);
        return Result.success("删除成功");
    }
}
