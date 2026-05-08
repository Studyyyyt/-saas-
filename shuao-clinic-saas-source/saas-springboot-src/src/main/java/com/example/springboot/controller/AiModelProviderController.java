package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiModelProvider;
import com.example.springboot.service.AiModelProviderService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/model-providers")
public class AiModelProviderController {

    private final AiModelProviderService service;

    public AiModelProviderController(AiModelProviderService service) {
        this.service = service;
    }

    @GetMapping
    public Result get() {
        AiModelProvider provider = service.getActiveProvider();
        if (provider == null) {
            return Result.success(null);
        }
        return Result.success(maskProvider(provider));
    }

    @PostMapping
    public Result save(@RequestBody AiModelProvider provider) {
        service.save(provider);
        return Result.success(maskProvider(provider));
    }

    @PostMapping("/test")
    public Result test(@RequestBody AiModelProvider provider) {
        try {
            String result = service.testConnection(provider);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        } catch (RuntimeException e) {
            return Result.error("400", e.getMessage());
        } catch (Exception e) {
            return Result.error("500", "连接测试异常：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    private AiModelProvider maskProvider(AiModelProvider provider) {
        if (provider == null || !StringUtils.hasText(provider.getApiKey())) {
            return provider;
        }
        String key = provider.getApiKey();
        if (key.length() <= 8) {
            provider.setApiKey("****");
        } else {
            provider.setApiKey(key.substring(0, 4) + "****" + key.substring(key.length() - 4));
        }
        return provider;
    }
}
