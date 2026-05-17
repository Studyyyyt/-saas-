package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiFunctionConfig;
import com.example.springboot.entity.AiGlobalConfig;
import com.example.springboot.mapper.AiFunctionConfigMapper;
import com.example.springboot.mapper.AiGlobalConfigMapper;
import com.example.springboot.service.AiConfigService;
import com.example.springboot.service.AiProxyService;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * AI 统一代理控制器
 * 所有 AI 请求通过此接口转发到外部工作流平台
 */
@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    // 硬编码白名单作为 fallback，当 ai_function_config 表不可用时使用
    private static final Set<String> FALLBACK_ALLOWED_AGENT_KEYS = Set.of(
            "medical-expand", "patient-insight", "consultation-assist", "home-assistant",
            "business-analysis", "appointment-assist", "followup-assist", "consultation-dashboard",
            "treatment-assist", "treatment-record-assist", "financial-analysis", "monthly-bill-analysis",
            "lab-statistics-analysis", "material-category-assist", "material-inventory-assist",
            "material-purchase-assist", "material-statistics-analysis", "default", "chat-assistant",
            "schedule-suggestion", "doctor-schedule", "medical-record-expand"
    );

    // 动态白名单缓存，启动时从 ai_function_config 表加载
    private final Set<String> allowedAgentKeys = new CopyOnWriteArraySet<>();

    private final AiProxyService aiProxyService;
    private final AiConfigService aiConfigService;
    private final AiFunctionConfigMapper aiFunctionConfigMapper;
    private final com.example.springboot.mapper.AiAgentConfigMapper aiAgentConfigMapper;
    private final AiGlobalConfigMapper aiGlobalConfigMapper;

    public AiProxyController(AiProxyService aiProxyService,
                             AiConfigService aiConfigService,
                             AiFunctionConfigMapper aiFunctionConfigMapper,
                             com.example.springboot.mapper.AiAgentConfigMapper aiAgentConfigMapper,
                             AiGlobalConfigMapper aiGlobalConfigMapper) {
        this.aiProxyService = aiProxyService;
        this.aiConfigService = aiConfigService;
        this.aiFunctionConfigMapper = aiFunctionConfigMapper;
        this.aiAgentConfigMapper = aiAgentConfigMapper;
        this.aiGlobalConfigMapper = aiGlobalConfigMapper;
    }

    /**
     * 启动时从 ai_function_config 和 ai_agent_config 表加载 agentKey 作为动态白名单
     * 如果表不存在或查询失败，保留硬编码 fallback
     */
    @PostConstruct
    public void initAllowedAgentKeys() {
        try {
            // 1. 从 ai_function_config 加载 function_key
            List<AiFunctionConfig> funcConfigs = aiFunctionConfigMapper.selectAll();
            if (funcConfigs != null && !funcConfigs.isEmpty()) {
                for (AiFunctionConfig config : funcConfigs) {
                    if (config.getFunctionKey() != null) {
                        allowedAgentKeys.add(config.getFunctionKey());
                    }
                }
            }
            // 2. 从 ai_agent_config 加载用户自定义的 agent_key
            try {
                List<String> agentKeys = aiAgentConfigMapper.selectAllAgentKeys();
                if (agentKeys != null && !agentKeys.isEmpty()) {
                    allowedAgentKeys.addAll(agentKeys);
                }
            } catch (Exception ignored) {
                // ai_agent_config 表可能不存在，忽略
            }
            // 如果两表都为空，使用 fallback
            if (allowedAgentKeys.isEmpty()) {
                allowedAgentKeys.addAll(FALLBACK_ALLOWED_AGENT_KEYS);
            }
        } catch (Exception e) {
            // 表不存在或查询异常时，使用硬编码白名单
            allowedAgentKeys.addAll(FALLBACK_ALLOWED_AGENT_KEYS);
        }
    }

    /**
     * 智能提取 n8n 返回的内容
     * 支持标准格式 {code, data}、常见内容字段、数组拼接，自动识别无需固定格式
     */
    @SuppressWarnings("unchecked")
    private Object smartExtract(Object parsed) {
        // 1. 如果 n8n 返回数组，提取每个元素的目标字段并拼接
        if (parsed instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) parsed;
            if (list.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                Object extracted = extractFromMap(item);
                if (extracted != null) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(extracted);
                }
            }
            return sb.toString();
        }

        // 2. 单对象提取
        return extractFromMap(parsed);
    }

    /**
     * 从单个 Map 对象中提取目标字段
     */
    @SuppressWarnings("unchecked")
    private Object extractFromMap(Object parsed) {
        if (!(parsed instanceof java.util.Map)) {
            return parsed;
        }
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) parsed;

        // 1. 标准格式：有 code + data，直接提取 data
        if (map.containsKey("code") && map.containsKey("data")) {
            return map.get("data");
        }

        // 2. 全局配置的字段名优先（用户自定义）
        try {
            AiGlobalConfig globalConfig = aiGlobalConfigMapper.selectByKey("response_field");
            if (globalConfig != null && globalConfig.getConfigValue() != null && !globalConfig.getConfigValue().isEmpty()) {
                String customKey = globalConfig.getConfigValue();
                if (map.containsKey(customKey) && map.get(customKey) != null) {
                    return map.get(customKey);
                }
            }
        } catch (Exception ignored) {
            // 全局配置表可能不存在，忽略
        }

        // 3. 常见内容字段（按优先级）
        List<String> contentKeys = Arrays.asList(
            "content", "reply", "message", "result",
            "text", "output", "answer", "response"
        );
        for (String key : contentKeys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
        }

        // 4. 如果只有一个字段，直接返回它
        if (map.size() == 1) {
            return map.values().iterator().next();
        }

        // 5. 兜底：原样返回
        return map;
    }

    /**
     * 统一 AI 代理接口（核心入口）
     *
     * 【完整数据流说明】
     * 1. 前端发送请求到 /api/ai/proxy/{agentKey}，payload 包含原始业务字段
     * 2. 本方法校验登录态和 agentKey 白名单
     * 3. 调用 AiProxyService.forward() 将请求包装为标准协议后发送给外部 Webhook
     * 4. Webhook 返回 JSON 响应（通常为 {code, msg, data} 标准格式）
     * 5. 本方法对响应做"解包"处理：如果外层已经是标准格式，提取内层 data，避免双重嵌套
     * 6. 最终包装为 Result 返回给前端
     *
     * 【双重嵌套问题与修复】（2026-05-16 修复）
     * 问题：Webhook 返回 {code:"200", data:{chief_complaint:"..."}}，
     *       后端直接 Result.success(parsed) 会导致前端收到：
     *       {code:"200", data:{code:"200", msg:"success", data:{chief_complaint:"..."}}}
     *       前端 res.data.data 拿到的是外层结构，无法直接读取字段。
     * 修复：检测到 parsed 包含 "code"+"data" 键时，直接取 parsedMap.get("data") 作为 Result.data。
     *
     * 【字段映射】前端 ↔ 后端 ↔ Webhook
     * 前端发送字段（如 chief_complaint）→ 后端包装到 input_fields → Webhook 接收
     * Webhook 返回字段（如 chief_complaint）→ 后端解包 → 前端回填到 this.form.chief_complaint
     *
     * @param agentKey     代理标识，如 medical-expand、default、finance
     * @param payload      请求体数据，必须包含 account_id 字段用于登录校验
     * @param acceptHeader Accept 请求头，包含 "text/event-stream" 时启用 SSE 流式模式
     * @return JSON 模式返回 Result（统一响应结构），SSE 模式返回 SseEmitter
     */
    @PostMapping("/proxy/{agentKey}")
    public Object proxy(
            @PathVariable String agentKey,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Accept", defaultValue = "application/json") String acceptHeader) {

        // 根据 Accept 头判断是否为 SSE 流式请求
        boolean isSse = acceptHeader != null && acceptHeader.contains("text/event-stream");

        // H9: 登录态校验 — 检查 account_id 是否存在
        // account_id 从前端 payload 中直接获取，用于标识当前操作用户
        if (payload == null || payload.get("account_id") == null) {
            if (isSse) {
                SseEmitter emitter = new SseEmitter(0L);
                try {
                    emitter.send(SseEmitter.event().name("error").data("未登录"));
                    emitter.complete();
                } catch (Exception ignored) {
                    emitter.completeWithError(ignored);
                }
                return emitter;
            }
            return Result.error("401", "未登录");
        }

        // H8: agentKey 白名单校验（查动态缓存）
        // 只允许预定义的功能标识通过，防止任意 URL 被代理访问
        if (!allowedAgentKeys.contains(agentKey)) {
            if (isSse) {
                SseEmitter emitter = new SseEmitter(0L);
                try {
                    emitter.send(SseEmitter.event().name("error").data("非法的 agentKey"));
                    emitter.complete();
                } catch (Exception ignored) {
                    emitter.completeWithError(ignored);
                }
                return emitter;
            }
            return Result.error("403", "非法的 agentKey");
        }

        // 检查 AI 功能是否在该诊所/账户下启用
        aiConfigService.assertAiEnabled(agentKey);

        // SSE 流式模式：创建长连接发射器，在新线程中执行流式转发
        if (isSse) {
            SseEmitter emitter = new SseEmitter(120000L);  // 2分钟超时
            new Thread(() -> aiProxyService.forwardStream(agentKey, payload, emitter)).start();
            return emitter;
        }

        // JSON 同步模式：调用代理服务转发请求，并对响应做解包处理
        try {
            // forward() 内部会将 payload 包装为标准协议，发送到外部 Webhook，返回原始响应字符串
            String response = aiProxyService.forward(agentKey, payload);

            // L4: 尝试将 JSON 字符串解析为对象，减少前端二次解析
            try {
                Object parsed = new com.fasterxml.jackson.databind.ObjectMapper().readValue(response, Object.class);

                // 【智能解包】支持标准格式 {code, data} 及常见内容字段
                // 自动识别 content / reply / message / result / text / output / answer / response
                return Result.success(smartExtract(parsed));
            } catch (Exception parseEx) {
                // 非 JSON 格式（如纯文本），原样返回字符串
                return Result.success(response);
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("AI 代理调用失败：" + e.getMessage());
        }
    }

    /**
     * 获取 AI 全局配置
     */
    @GetMapping("/global-config/{configKey}")
    public Result getGlobalConfig(@PathVariable String configKey) {
        AiGlobalConfig config = aiGlobalConfigMapper.selectByKey(configKey);
        return Result.success(config);
    }

    /**
     * 更新 AI 全局配置
     */
    @PutMapping("/global-config/{configKey}")
    public Result updateGlobalConfig(@PathVariable String configKey, @RequestBody Map<String, String> body) {
        String configValue = body.get("configValue");
        if (configValue == null) {
            return Result.error("configValue 不能为空");
        }
        int updated = aiGlobalConfigMapper.updateValueByKey(configKey, configValue);
        if (updated > 0) {
            return Result.success();
        }
        return Result.error("更新失败");
    }
}
