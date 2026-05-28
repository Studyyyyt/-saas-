package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.controller.dto.FunctionStatusDTO;
import com.example.springboot.controller.dto.GlobalConfigDTO;
import com.example.springboot.entity.AiFunctionConfig;
import com.example.springboot.service.AiConfigService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-config")
public class AiConfigController {

    private final AiConfigService aiConfigService;

    public AiConfigController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @GetMapping("/overview")
    public Result getOverview(@RequestHeader(value = "X-Operator-Account-Id", required = false) String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return Result.error("401", "未登录");
        }
        return Result.success(aiConfigService.getOverview());
    }

    @GetMapping("/functions")
    public Result getFunctions(@RequestHeader(value = "X-Operator-Account-Id", required = false) String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return Result.error("401", "未登录");
        }
        List<AiFunctionConfig> functions = aiConfigService.getFunctionList();
        return Result.success(functions);
    }

    @PutMapping("/global")
    public Result updateGlobalConfig(@RequestHeader(value = "X-Operator-Account-Id", required = false) String accountId,
                                     @RequestBody GlobalConfigDTO dto) {
        if (!StringUtils.hasText(accountId)) {
            return Result.error("401", "未登录");
        }
        aiConfigService.updateGlobalConfig(dto);
        return Result.success("保存成功");
    }

    @PutMapping("/functions/{key}")
    public Result updateFunctionStatus(@RequestHeader(value = "X-Operator-Account-Id", required = false) String accountId,
                                       @PathVariable String key,
                                       @RequestBody FunctionStatusDTO dto) {
        if (!StringUtils.hasText(accountId)) {
            return Result.error("401", "未登录");
        }
        aiConfigService.updateFunctionStatus(key, dto.getEnabled());
        return Result.success("更新成功");
    }
}
