package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.controller.dto.FunctionStatusDTO;
import com.example.springboot.controller.dto.GlobalConfigDTO;
import com.example.springboot.entity.AiFunctionConfig;
import com.example.springboot.service.AiConfigService;
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
    public Result getOverview() {
        // TODO: 补充登录态校验，当前因前端未传accountId暂保留
        return Result.success(aiConfigService.getOverview());
    }

    @GetMapping("/functions")
    public Result getFunctions() {
        // TODO: 补充登录态校验，当前因前端未传accountId暂保留
        List<AiFunctionConfig> functions = aiConfigService.getFunctionList();
        return Result.success(functions);
    }

    @PutMapping("/global")
    public Result updateGlobalConfig(@RequestBody GlobalConfigDTO dto) {
        // TODO: 补充登录态校验，当前因前端未传accountId暂保留
        aiConfigService.updateGlobalConfig(dto);
        return Result.success("保存成功");
    }

    @PutMapping("/functions/{key}")
    public Result updateFunctionStatus(@PathVariable String key,
                                       @RequestBody FunctionStatusDTO dto) {
        // TODO: 补充登录态校验，当前因前端未传accountId暂保留
        aiConfigService.updateFunctionStatus(key, dto.getEnabled());
        return Result.success("更新成功");
    }
}
