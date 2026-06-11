package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.entity.AiFunctionConfig;
import com.example.springboot.entity.AiFunctionMapping;
import com.example.springboot.mapper.AiAgentConfigMapper;
import com.example.springboot.mapper.AiFunctionConfigMapper;
import com.example.springboot.mapper.AiFunctionMappingMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * AI 系统功能与 Agent 绑定映射控制器
 * 支持将系统功能动态绑定到任意 AgentKey，并分别控制所在页面和首页的显示
 */
@RestController
@RequestMapping("/api/ai/function-mappings")
public class AiFunctionMappingController {

    private final AiFunctionMappingMapper aiFunctionMappingMapper;
    private final AiFunctionConfigMapper aiFunctionConfigMapper;
    private final AiAgentConfigMapper aiAgentConfigMapper;

    public AiFunctionMappingController(AiFunctionMappingMapper aiFunctionMappingMapper,
                                       AiFunctionConfigMapper aiFunctionConfigMapper,
                                       AiAgentConfigMapper aiAgentConfigMapper) {
        this.aiFunctionMappingMapper = aiFunctionMappingMapper;
        this.aiFunctionConfigMapper = aiFunctionConfigMapper;
        this.aiAgentConfigMapper = aiAgentConfigMapper;
    }

    /**
     * 获取系统功能映射列表
     * 优先返回当前账户的自定义配置，如果没有则返回系统默认配置
     */
    @GetMapping
    public Result list(@RequestParam(required = false) Long accountId) {
        // 获取系统默认配置
        List<AiFunctionMapping> defaults = aiFunctionMappingMapper.selectSystemDefaults();
        if (accountId == null) {
            return Result.success(defaults);
        }

        // 获取当前账户的自定义配置
        List<AiFunctionMapping> customs = aiFunctionMappingMapper.selectByAccountId(accountId);
        Map<String, AiFunctionMapping> customMap = new HashMap<>();
        for (AiFunctionMapping custom : customs) {
            if (custom.getFunctionCode() != null) {
                customMap.put(custom.getFunctionCode(), custom);
            }
        }

        // 合并：以系统默认为基础，用自定义配置覆盖
        List<AiFunctionMapping> result = new ArrayList<>();
        for (AiFunctionMapping def : defaults) {
            AiFunctionMapping item = new AiFunctionMapping();
            item.setId(def.getId());
            item.setAccountId(accountId);
            item.setFunctionCode(def.getFunctionCode());
            item.setFunctionName(def.getFunctionName());
            item.setSortOrder(def.getSortOrder());

            AiFunctionMapping custom = customMap.get(def.getFunctionCode());
            if (custom != null) {
                // 用户有自定义配置，使用自定义的 agentKey 和显示设置
                item.setAgentKey(custom.getAgentKey());
                item.setIsVisibleOnPage(custom.getIsVisibleOnPage());
                item.setIsVisibleOnHome(custom.getIsVisibleOnHome());
            } else {
                // 使用系统默认配置
                item.setAgentKey(def.getAgentKey());
                item.setIsVisibleOnPage(def.getIsVisibleOnPage());
                item.setIsVisibleOnHome(def.getIsVisibleOnHome());
            }
            result.add(item);
        }

        // 按排序号排序
        result.sort(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 0));
        return Result.success(result);
    }

    /**
     * 更新某个系统功能的绑定配置
     */
    @PutMapping("/{functionCode}")
    public Result update(@PathVariable String functionCode,
                         @RequestBody Map<String, Object> body) {
        Long accountId = body.get("accountId") instanceof Number
                ? ((Number) body.get("accountId")).longValue() : null;
        if (accountId == null) {
            return Result.error("401", "未登录");
        }

        String agentKey = body.get("agentKey") != null ? String.valueOf(body.get("agentKey")) : null;
        Boolean isVisibleOnPage = body.get("isVisibleOnPage") instanceof Boolean
                ? (Boolean) body.get("isVisibleOnPage") : true;
        Boolean isVisibleOnHome = body.get("isVisibleOnHome") instanceof Boolean
                ? (Boolean) body.get("isVisibleOnHome") : true;
        Integer sortOrder = body.get("sortOrder") instanceof Number
                ? ((Number) body.get("sortOrder")).intValue() : 0;

        // 获取系统默认配置以获取 functionName
        AiFunctionMapping def = aiFunctionMappingMapper.selectSystemDefaults().stream()
                .filter(m -> functionCode.equals(m.getFunctionCode()))
                .findFirst().orElse(null);
        String functionName = def != null ? def.getFunctionName() : functionCode;

        AiFunctionMapping mapping = new AiFunctionMapping();
        mapping.setAccountId(accountId);
        mapping.setFunctionCode(functionCode);
        mapping.setFunctionName(functionName);
        mapping.setAgentKey(agentKey);
        mapping.setIsVisibleOnPage(isVisibleOnPage);
        mapping.setIsVisibleOnHome(isVisibleOnHome);
        mapping.setSortOrder(sortOrder);

        aiFunctionMappingMapper.upsert(mapping);
        return Result.success("保存成功");
    }

    /**
     * 创建新的系统功能编码（系统默认配置）
     */
    @PostMapping
    public Result create(@RequestBody Map<String, Object> body) {
        String functionCode = body.get("functionCode") != null ? String.valueOf(body.get("functionCode")) : null;
        String functionName = body.get("functionName") != null ? String.valueOf(body.get("functionName")) : null;
        if (functionCode == null || functionCode.trim().isEmpty()) {
            return Result.error("400", "功能编码不能为空");
        }
        if (functionName == null || functionName.trim().isEmpty()) {
            return Result.error("400", "功能名称不能为空");
        }

        // 检查是否已存在
        AiFunctionMapping existing = aiFunctionMappingMapper.selectByAccountAndCode(null, functionCode);
        if (existing != null) {
            return Result.error("409", "功能编码已存在");
        }

        AiFunctionMapping mapping = new AiFunctionMapping();
        mapping.setAccountId(null);
        mapping.setFunctionCode(functionCode.trim());
        mapping.setFunctionName(functionName.trim());
        mapping.setAgentKey("");
        mapping.setIsVisibleOnPage(true);
        mapping.setIsVisibleOnHome(true);
        mapping.setSortOrder(0);

        aiFunctionMappingMapper.insert(mapping);
        return Result.success("创建成功");
    }

    /**
     * 删除系统功能编码（仅系统默认配置可删除）
     */
    @DeleteMapping("/{functionCode}")
    public Result delete(@PathVariable String functionCode) {
        // 删除系统默认配置
        int rows = aiFunctionMappingMapper.deleteSystemDefaultByCode(functionCode);
        if (rows == 0) {
            return Result.error("404", "功能编码不存在或不可删除");
        }
        return Result.success("删除成功");
    }

    /**
     * AI 功能总览：聚合系统功能定义、绑定关系与 Agent 信息
     * 返回所有预设功能的完整配置状态，供 AI 总览页表格展示
     */
    @GetMapping("/function-overview")
    public Result functionOverview(@RequestParam(required = false) Long accountId) {
        // 1. 获取系统默认的功能映射（这些是实际出现在前端页面的系统功能）
        List<AiFunctionMapping> mappings = aiFunctionMappingMapper.selectSystemDefaults();

        // 2. 获取 ai_function_config 作为元数据补充（pagePath、功能名称等）
        List<AiFunctionConfig> configs = aiFunctionConfigMapper.selectAll();
        Map<String, AiFunctionConfig> configMap = new HashMap<>();
        for (AiFunctionConfig c : configs) {
            if (c.getFunctionKey() != null) {
                configMap.put(c.getFunctionKey(), c);
            }
        }

        // 3. 如传了 accountId，获取用户自定义映射用于覆盖
        Map<String, AiFunctionMapping> customMap = new HashMap<>();
        if (accountId != null) {
            List<AiFunctionMapping> customs = aiFunctionMappingMapper.selectByAccountId(accountId);
            for (AiFunctionMapping custom : customs) {
                if (custom.getFunctionCode() != null) {
                    customMap.put(custom.getFunctionCode(), custom);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (AiFunctionMapping def : mappings) {
            String functionCode = def.getFunctionCode();
            String agentKey = def.getAgentKey();
            Boolean isVisibleOnPage = def.getIsVisibleOnPage();
            Boolean isVisibleOnHome = def.getIsVisibleOnHome();

            // 用户自定义配置覆盖系统默认
            AiFunctionMapping custom = customMap.get(functionCode);
            if (custom != null) {
                if (custom.getAgentKey() != null) agentKey = custom.getAgentKey();
                if (custom.getIsVisibleOnPage() != null) isVisibleOnPage = custom.getIsVisibleOnPage();
                if (custom.getIsVisibleOnHome() != null) isVisibleOnHome = custom.getIsVisibleOnHome();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("functionCode", functionCode);
            item.put("functionName", def.getFunctionName());
            item.put("agentKey", agentKey);
            item.put("isVisibleOnPage", isVisibleOnPage);
            item.put("isVisibleOnHome", isVisibleOnHome);
            item.put("sortOrder", def.getSortOrder() != null ? def.getSortOrder() : 0);

            // 用 ai_function_config 补充 pagePath 和更友好的功能名称
            AiFunctionConfig config = configMap.get(functionCode);
            if (config == null && agentKey != null && !agentKey.isEmpty()) {
                config = configMap.get(agentKey);
            }
            if (config != null) {
                if (config.getPagePath() != null && !config.getPagePath().contains("?")) {
                    item.put("pagePath", config.getPagePath());
                }
                if (config.getFunctionName() != null && !config.getFunctionName().contains("?")) {
                    item.put("functionName", config.getFunctionName());
                }
            }

            // 补充 Agent 显示名称
            if (agentKey != null && !agentKey.isEmpty()) {
                Long queryAccountId = accountId != null ? accountId : 0L;
                AiAgentConfig agent = aiAgentConfigMapper.selectBestMatchByAccountIdAndKey(queryAccountId, agentKey);
                if (agent != null) {
                    item.put("agentName", agent.getName());
                }
            }

            result.add(item);
        }

        result.sort(Comparator.comparingInt(m -> (Integer) m.getOrDefault("sortOrder", 0)));
        return Result.success(result);
    }

    /**
     * 查询某个 AgentKey 被哪些系统功能引用
     * 用于删除 Agent 前的引用检测
     */
    @GetMapping("/agent-usages/{agentKey}")
    public Result agentUsages(@PathVariable String agentKey) {
        List<AiFunctionMapping> mappings = aiFunctionMappingMapper.selectSystemDefaults();
        List<Map<String, Object>> usages = new ArrayList<>();
        for (AiFunctionMapping m : mappings) {
            if (agentKey != null && agentKey.equals(m.getAgentKey())) {
                Map<String, Object> item = new HashMap<>();
                item.put("functionCode", m.getFunctionCode());
                item.put("functionName", m.getFunctionName());
                usages.add(item);
            }
        }
        return Result.success(usages);
    }

    /**
     * 获取某个系统功能当前绑定的 AgentKey
     * 前端页面在调用 AI 前先查此接口，获取当前应使用的 agentKey
     */
    @GetMapping("/{functionCode}/agent-key")
    public Result getAgentKey(@PathVariable String functionCode,
                              @RequestParam(required = false) Long accountId) {
        AiFunctionMapping mapping = aiFunctionMappingMapper.selectByAccountAndCode(accountId, functionCode);
        if (mapping == null) {
            // 没有配置时返回系统默认值
            mapping = aiFunctionMappingMapper.selectSystemDefaults().stream()
                    .filter(m -> functionCode.equals(m.getFunctionCode()))
                    .findFirst().orElse(null);
        }
        if (mapping == null) {
            return Result.error("404", "功能配置不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("functionCode", mapping.getFunctionCode());
        result.put("functionName", mapping.getFunctionName());
        result.put("agentKey", mapping.getAgentKey());
        result.put("isVisibleOnPage", mapping.getIsVisibleOnPage());
        result.put("isVisibleOnHome", mapping.getIsVisibleOnHome());
        return Result.success(result);
    }
}
