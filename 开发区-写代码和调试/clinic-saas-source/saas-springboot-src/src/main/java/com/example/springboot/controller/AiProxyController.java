package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiCallLog;
import com.example.springboot.entity.AiFunctionConfig;
import com.example.springboot.entity.AiGlobalConfig;
import com.example.springboot.mapper.AiAgentConfigMapper;
import com.example.springboot.mapper.AiFunctionConfigMapper;
import com.example.springboot.mapper.AiGlobalConfigMapper;
import com.example.springboot.service.AiCallLogService;
import com.example.springboot.service.AiConfigService;
import com.example.springboot.service.AiProxyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AI 统一代理控制器
 * 所有 AI 请求通过此接口转发到外部工作流平台
 */
@Slf4j
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
            "schedule-suggestion", "doctor-schedule", "medical-record-expand");

    // 动态白名单缓存，启动时从 ai_function_config 表加载
    private final Set<String> allowedAgentKeys = new CopyOnWriteArraySet<>();

    // 全局响应字段缓存，避免每次 AI 请求都查数据库
    private volatile String globalResponseField;

    // 缓存刷新冷却间隔（毫秒），防止缓存未命中时频繁查库
    private static final long CACHE_REFRESH_INTERVAL_MS = 5000L;
    private volatile long lastCacheRefresh = 0L;

    // SSE 专用线程池，避免每次请求都 new Thread（有界线程池，防止无限增长）
    private final ExecutorService sseExecutor = new ThreadPoolExecutor(
            4, 16, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100));

    private final AiProxyService aiProxyService;
    private final AiConfigService aiConfigService;
    private final AiCallLogService aiCallLogService;
    private final AiFunctionConfigMapper aiFunctionConfigMapper;
    private final AiAgentConfigMapper aiAgentConfigMapper;
    private final AiGlobalConfigMapper aiGlobalConfigMapper;
    private final ObjectMapper objectMapper;

    public AiProxyController(AiProxyService aiProxyService,
            AiConfigService aiConfigService,
            AiCallLogService aiCallLogService,
            AiFunctionConfigMapper aiFunctionConfigMapper,
            AiAgentConfigMapper aiAgentConfigMapper,
            AiGlobalConfigMapper aiGlobalConfigMapper,
            ObjectMapper objectMapper) {
        this.aiProxyService = aiProxyService;
        this.aiConfigService = aiConfigService;
        this.aiCallLogService = aiCallLogService;
        this.aiFunctionConfigMapper = aiFunctionConfigMapper;
        this.aiAgentConfigMapper = aiAgentConfigMapper;
        this.aiGlobalConfigMapper = aiGlobalConfigMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 应用关闭时优雅关闭 SSE 线程池
     */
    @PreDestroy
    public void shutdownExecutor() {
        sseExecutor.shutdown();
    }

    /**
     * 启动时从 ai_function_config 和 ai_agent_config 表加载 agentKey 作为动态白名单
     * 同时加载全局配置 response_field 到内存，避免每次 AI 请求都查数据库
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
            } catch (Exception e) {
                // ai_agent_config 表可能不存在，忽略
                log.warn("加载 ai_agent_config 失败，使用 fallback", e);
            }
            // 如果两表都为空，使用 fallback
            if (allowedAgentKeys.isEmpty()) {
                allowedAgentKeys.addAll(FALLBACK_ALLOWED_AGENT_KEYS);
            }
        } catch (Exception e) {
            // 表不存在或查询异常时，使用硬编码白名单
            log.warn("加载 ai_function_config 失败，使用 fallback", e);
            allowedAgentKeys.addAll(FALLBACK_ALLOWED_AGENT_KEYS);
        }

        // 3. 加载全局配置 response_field 到内存
        refreshGlobalResponseField();
    }

    /**
     * 刷新全局响应字段缓存
     * 配置更新后可通过 /global-config 接口触发刷新
     */
    private void refreshGlobalResponseField() {
        try {
            AiGlobalConfig globalConfig = aiGlobalConfigMapper.selectByKey("response_field");
            if (globalConfig != null && globalConfig.getConfigValue() != null) {
                globalResponseField = globalConfig.getConfigValue();
            }
        } catch (Exception e) {
            log.error("加载全局配置 response_field 失败", e);
            globalResponseField = null;
        }
    }

    /**
     * 白名单校验：优先查内存缓存，缓存未命中时回退查数据库并刷新缓存
     * 支持新增 Agent 后无需重启服务即可生效
     * 增加冷却机制，防止缓存未命中时频繁查库导致 DoS
     */
    private boolean isAgentKeyAllowed(String agentKey) {
        if (allowedAgentKeys.contains(agentKey)) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - lastCacheRefresh < CACHE_REFRESH_INTERVAL_MS) {
            return false;
        }
        lastCacheRefresh = now;
        try {
            List<String> agentKeys = aiAgentConfigMapper.selectAllAgentKeys();
            if (agentKeys != null && !agentKeys.isEmpty()) {
                allowedAgentKeys.addAll(agentKeys);
            }
        } catch (Exception e) {
            log.warn("缓存未命中时加载 ai_agent_config 失败", e);
        }
        return allowedAgentKeys.contains(agentKey);
    }

    /**
     * 创建 SSE 错误发射器并发送错误事件
     */
    private SseEmitter createErrorSseEmitter(String errorMessage) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("error").data(errorMessage));
            emitter.complete();
        } catch (Exception e) {
            log.warn("SSE 发送错误失败: {}", errorMessage, e);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 智能提取 n8n 返回的内容
     * 支持标准格式 {code, data}、常见内容字段、数组拼接，自动识别无需固定格式
     */
    @SuppressWarnings("unchecked")
    private Object smartExtract(Object parsed) {
        // 1. 如果 n8n 返回数组，提取每个元素的目标字段并拼接
        if (parsed instanceof List) {
            List<?> list = (List<?>) parsed;
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
        if (!(parsed instanceof Map)) {
            return tryParseStringValue(parsed);
        }
        Map<String, Object> map = (Map<String, Object>) parsed;

        // 1. 标准格式：有 code + data，直接提取 data
        if (map.containsKey("code") && map.containsKey("data")) {
            return tryParseStringValue(map.get("data"));
        }

        // 2. 全局配置的字段名优先（用户自定义）
        String customKey = globalResponseField;
        if (customKey != null && !customKey.isEmpty()
                && map.containsKey(customKey) && map.get(customKey) != null) {
            return tryParseStringValue(map.get(customKey));
        }

        // 3. 常见内容字段（按优先级）
        List<String> contentKeys = Arrays.asList(
                "content", "reply", "message", "result",
                "text", "output", "answer", "response");
        for (String key : contentKeys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return tryParseStringValue(map.get(key));
            }
        }

        // 4. 如果只有一个字段，直接返回它
        if (map.size() == 1) {
            return tryParseStringValue(map.values().iterator().next());
        }

        // 5. 兜底：原样返回
        return map;
    }

    /**
     * 尝试解析字符串值：支持标准 JSON 字符串和 Java Map.toString 格式
     * 当外部 Webhook 把对象序列化为字符串放入 data 字段时，本方法可将其还原为 Map
     */
    private Object tryParseStringValue(Object value) {
        if (!(value instanceof String)) {
            return value;
        }
        String str = ((String) value).trim();
        if (str.isEmpty()) {
            return str;
        }
        // 尝试 JSON 解析（标准 JSON 对象或数组）
        if (str.startsWith("{") || str.startsWith("[")) {
            try {
                return objectMapper.readValue(str, Object.class);
            } catch (Exception e1) {
                // JSON 解析失败，尝试解析 Java Map.toString 格式
                java.util.Map<String, Object> map = tryParseMapString(str);
                if (map != null && !map.isEmpty()) {
                    return map;
                }
            }
        }
        return str;
    }

    /**
     * 解析 Java Map.toString 格式字符串为 Map
     * 格式示例: {patient_id=13, patient_name=测试, operation_items=[]}
     */
    private Map<String, Object> tryParseMapString(String str) {
        if (!str.startsWith("{") || !str.endsWith("}")) {
            return null;
        }
        String content = str.substring(1, str.length() - 1).trim();
        if (content.isEmpty()) {
            return new java.util.HashMap<>();
        }
        Map<String, Object> result = new java.util.HashMap<>();
        // 按逗号+空格或逗号分割键值对（Map.toString 的标准分隔符是 ", "）
        String[] pairs = content.split(",\\s+");
        for (String pair : pairs) {
            int eqIdx = pair.indexOf('=');
            if (eqIdx > 0) {
                String key = pair.substring(0, eqIdx).trim();
                String val = pair.substring(eqIdx + 1).trim();
                result.put(key, val);
            }
        }
        return result.isEmpty() ? null : result;
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
     * 后端直接 Result.success(parsed) 会导致前端收到：
     * {code:"200", data:{code:"200", msg:"success", data:{chief_complaint:"..."}}}
     * 前端 res.data.data 拿到的是外层结构，无法直接读取字段。
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
                return createErrorSseEmitter("未登录");
            }
            return Result.error("401", "未登录");
        }

        // H8: agentKey 白名单校验（优先内存缓存，未命中时自动查库刷新）
        // 只允许已配置的 Agent 通过，防止任意 URL 被代理访问
        if (!isAgentKeyAllowed(agentKey)) {
            if (isSse) {
                return createErrorSseEmitter("非法的 agentKey");
            }
            return Result.error("403", "非法的 agentKey");
        }

        // 检查 AI 功能是否在该诊所/账户下启用
        aiConfigService.assertAiEnabled(agentKey);

        // SSE 流式模式：创建长连接发射器，在线程池中执行流式转发
        if (isSse) {
            SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时
            sseExecutor.submit(() -> aiProxyService.forwardStream(agentKey, payload, emitter));
            return emitter;
        }

        // JSON 同步模式：调用代理服务转发请求，并对响应做解包处理
        long startTime = System.currentTimeMillis();
        String requestMessage = payload.get("message") != null ? String.valueOf(payload.get("message")) : "";
        String accountIdStr = payload.get("account_id") != null ? String.valueOf(payload.get("account_id")) : null;
        String accountName = payload.get("account_name") != null ? String.valueOf(payload.get("account_name")) : null;
        String sessionId = payload.get("session_id") != null ? String.valueOf(payload.get("session_id")) : null;
        Long accountIdLong = null;
        try {
            accountIdLong = accountIdStr != null ? Long.valueOf(accountIdStr) : null;
        } catch (NumberFormatException ignored) {
        }

        try {
            // forward() 内部会将 payload 包装为标准协议，发送到外部 Webhook，返回原始响应字符串
            String response = aiProxyService.forward(agentKey, payload);

            // L4: 尝试将 JSON 字符串解析为对象，减少前端二次解析
            try {
                Object parsed = objectMapper.readValue(response, Object.class);
                Object extracted = smartExtract(parsed);
                String responseContent = extracted != null ? String.valueOf(extracted) : "";
                saveCallLog(accountIdLong, accountName, agentKey, sessionId, requestMessage,
                        "success", responseContent, null, startTime, "web");
                return Result.success(extracted);
            } catch (Exception parseEx) {
                // 非 JSON 格式（如纯文本），原样返回字符串
                saveCallLog(accountIdLong, accountName, agentKey, sessionId, requestMessage,
                        "success", response, null, startTime, "web");
                return Result.success(response);
            }
        } catch (IllegalArgumentException e) {
            saveCallLog(accountIdLong, accountName, agentKey, sessionId, requestMessage,
                    "error", null, e.getMessage(), startTime, "web");
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            saveCallLog(accountIdLong, accountName, agentKey, sessionId, requestMessage,
                    "error", null, e.getMessage(), startTime, "web");
            return Result.error(e.getMessage());
        } catch (Exception e) {
            saveCallLog(accountIdLong, accountName, agentKey, sessionId, requestMessage,
                    "error", null, e.getMessage(), startTime, "web");
            return Result.error("AI 代理调用失败：" + e.getMessage());
        }
    }

    /**
     * 异步保存调用日志，不影响主请求流程
     */
    private void saveCallLog(Long accountId, String accountName, String agentKey, String sessionId,
                             String requestMessage, String status, String responseContent,
                             String errorMsg, long startTime, String source) {
        try {
            AiCallLog log = new AiCallLog();
            log.setAccountId(accountId);
            log.setAccountName(accountName);
            log.setAgentKey(agentKey);
            log.setSessionId(sessionId);
            log.setRequestMessage(truncate(requestMessage, 500));
            log.setResponseStatus(status);
            log.setResponseContent(truncate(responseContent, 500));
            log.setErrorMsg(truncate(errorMsg, 500));
            log.setDurationMs((int) (System.currentTimeMillis() - startTime));
            log.setSource(source);
            aiCallLogService.save(log);
        } catch (Exception e) {
            log.warn("保存 AI 调用日志失败", e);
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
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
     * 更新成功后自动刷新内存缓存
     */
    @PutMapping("/global-config/{configKey}")
    public Result updateGlobalConfig(@PathVariable String configKey, @RequestBody Map<String, String> body) {
        String configValue = body.get("configValue");
        if (configValue == null) {
            return Result.error("configValue 不能为空");
        }
        int updated = aiGlobalConfigMapper.updateValueByKey(configKey, configValue);
        if (updated > 0) {
            // 如果更新的是 response_field，刷新内存缓存
            if ("response_field".equals(configKey)) {
                refreshGlobalResponseField();
            }
            return Result.success();
        }
        return Result.error("更新失败");
    }
}
