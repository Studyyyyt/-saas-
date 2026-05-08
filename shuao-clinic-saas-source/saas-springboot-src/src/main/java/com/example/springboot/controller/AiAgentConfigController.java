package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.service.AiAgentConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/ai-agent-configs")
public class AiAgentConfigController {

    private final AiAgentConfigService aiAgentConfigService;

    public AiAgentConfigController(AiAgentConfigService aiAgentConfigService) {
        this.aiAgentConfigService = aiAgentConfigService;
    }

    @GetMapping
    public Result list(@RequestParam(required = false) Long accountId) {
        return Result.success(aiAgentConfigService.listByAccount(accountId));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.success(aiAgentConfigService.getById(id));
    }

    @GetMapping("/by-key")
    public Result byKey(@RequestParam(required = false) Long accountId,
                        @RequestParam String agentKey) {
        return Result.success(aiAgentConfigService.getByAccountAndKey(accountId, agentKey));
    }

    @PostMapping
    public Result create(@RequestBody AiAgentConfig config) {
        try {
            aiAgentConfigService.create(config);
            return Result.success(config);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody AiAgentConfig config) {
        config.setId(id);
        aiAgentConfigService.update(config);
        return Result.success(config);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        aiAgentConfigService.delete(id);
        return Result.success("删除成功");
    }
}
