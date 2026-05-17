package com.example.springboot.service;

import com.example.springboot.entity.AiAgentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.springboot.entity.AiOperationLog;
import com.example.springboot.mapper.AiOperationLogMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 统一代理服务
 * 负责将前端请求转发到配置的外部工作流端点
 */
@Service
public class AiProxyService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    private final AiAgentConfigService aiAgentConfigService;
    private final AiMockService aiMockService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AiOperationLogMapper aiOperationLogMapper;

    public AiProxyService(AiAgentConfigService aiAgentConfigService,
                          AiMockService aiMockService,
                          ObjectMapper objectMapper,
                          AiOperationLogMapper aiOperationLogMapper) {
        this.aiAgentConfigService = aiAgentConfigService;
        this.aiMockService = aiMockService;
        this.objectMapper = objectMapper;
        this.aiOperationLogMapper = aiOperationLogMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 统一转发方法（JSON 同步模式）
     *
     * 【完整调用链路】
     * 1. 根据 agentKey 和 account_id 从 ai_agent_config 表查找对应的 Webhook 配置
     * 2. 如果配置缺失或端点是测试地址，走 Mock 响应（本地模拟，不发送 HTTP 请求）
     * 3. 调用 wrapProtocolIfNeeded() 将前端原始 payload 包装为标准协议格式
     * 4. 调用 buildRequestBody() 根据配置的 request_template 构建最终请求体（支持 {{变量}} 替换）
     * 5. 使用 Java HttpClient 发送 POST 请求到外部 Webhook
     * 6. 返回 Webhook 的原始响应字符串（JSON 格式）
     *
     * 【标准协议格式说明】（由 wrapProtocolIfNeeded 生成）
     * {
     *   "protocol_version": "1.0",
     *   "function": "medical-expand",
     *   "context": { "account_id": 1, "clinic_id": "", "scene_id": "medical-expand", "timestamp": 1778860210475 },
     *   "input_fields": { "fields": { "label": "fields", "value": { ... }, "enabled": true } },
     *   "output_schema": { "format": "json", "required": [], "optional": [] },
     *   "_original_payload": { "fields": { ... }, "account_id": 1 }
     * }
     *
     * @param agentKey 代理标识，如 medical-expand、default、finance
     * @param payload  请求体数据，包含 account_id、fields 等前端原始字段
     * @return 外部端点返回的原始 JSON 字符串（通常为 {code, msg, data} 格式）
     */
    public String forward(String agentKey, Map<String, Object> payload) {
        // 解析 Agent 配置：按 accountId + agentKey 查找，找不到则回退到系统默认配置
        AiAgentConfig config = resolveConfig(agentKey, payload);

        // 当配置缺失或端点为测试地址时，使用 Mock 响应（用于本地开发和测试）
        if (config == null || aiMockService.shouldMock(config.getEndpointUrl())) {
            String result = aiMockService.mockResponse(agentKey, payload);
            logAiOperation(agentKey, payload, result, null);
            return result;
        }

        // 构建最终请求体：先包装协议，再根据 request_template 做变量替换
        String requestBody = buildRequestBody(config, wrapProtocolIfNeeded(agentKey, payload));
        HttpRequest request = buildHttpRequest(config, requestBody, false);

        try {
            // 发送 HTTP POST 请求到外部 Webhook 地址
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                logAiOperation(agentKey, payload, body, null);
                return body;  // 返回原始响应字符串，由调用方（AiProxyController）解析
            }
            String errorBody = response.body();
            RuntimeException ex = new RuntimeException("AI 代理调用失败：HTTP " + response.statusCode() + " " + errorBody);
            logAiOperation(agentKey, payload, null, ex);
            throw ex;
        } catch (RuntimeException e) {
            logAiOperation(agentKey, payload, null, e);
            throw e;
        } catch (Exception e) {
            RuntimeException ex = new RuntimeException("AI 代理调用异常：" + e.getMessage(), e);
            logAiOperation(agentKey, payload, null, ex);
            throw ex;
        }
    }

    /**
     * SSE 兼容流式转发方法（与旧 AiForwardService 输出格式一致）
     * 前端 EventSource 只认 data 字段，不识别 event name，因此不使用 .name()
     *
     * @param agentKey 代理标识
     * @param payload  请求体数据
     * @param emitter  SSE 推送器
     */
    public void forwardStreamSseCompat(String agentKey, Map<String, Object> payload, SseEmitter emitter) {
        AiAgentConfig config = resolveConfig(agentKey, payload);

        // 当配置缺失或端点为测试地址时，使用 Mock 流式响应
        if (config == null || aiMockService.shouldMock(config.getEndpointUrl())) {
            try {
                aiMockService.mockStreamResponse(agentKey, payload, emitter);
            } catch (Exception e) {
                logAiOperation(agentKey, payload, null, e);
                emitSseCompatError(emitter, "AI 代理调用异常：" + e.getMessage());
            }
            logAiOperation(agentKey, payload, "流式响应完成", null);
            return;
        }

        String requestBody = buildRequestBody(config, wrapProtocolIfNeeded(agentKey, payload));
        HttpRequest request = buildHttpRequest(config, requestBody, true);

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                String errorMsg = "AI 代理调用失败：HTTP " + response.statusCode() + " " + body;
                logAiOperation(agentKey, payload, null, new RuntimeException(errorMsg));
                emitSseCompatError(emitter, errorMsg);
                return;
            }

            // 检查 Content-Type，非 SSE 格式时直接返回错误事件
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.contains("text/event-stream")) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                String errorMsg = "AI 代理调用失败：外部端点返回非 SSE 格式（Content-Type: " + contentType + "），响应：" + body;
                logAiOperation(agentKey, payload, null, new RuntimeException(errorMsg));
                emitSseCompatError(emitter, errorMsg);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data.trim())) {
                            logAiOperation(agentKey, payload, "流式响应完成", null);
                            emitSseCompatDone(emitter);
                            return;
                        }
                        emitSseCompatData(emitter, data);
                    } else if (!line.trim().isEmpty()) {
                        // 非 SSE 格式行，直接透传
                        emitSseCompatData(emitter, line);
                    }
                }
            }
            logAiOperation(agentKey, payload, "流式响应完成", null);
            emitSseCompatDone(emitter);
        } catch (Exception e) {
            logAiOperation(agentKey, payload, null, e);
            emitSseCompatError(emitter, "AI 代理流式调用异常：" + e.getMessage());
        }
    }

    /**
     * SSE 流式转发方法（标准格式，带 event name）
     *
     * @param agentKey 代理标识
     * @param payload  请求体数据
     * @param emitter  SSE 推送器
     */
    public void forwardStream(String agentKey, Map<String, Object> payload, SseEmitter emitter) {
        AiAgentConfig config = resolveConfig(agentKey, payload);

        // 当配置缺失或端点为测试地址时，使用 Mock 流式响应
        if (config == null || aiMockService.shouldMock(config.getEndpointUrl())) {
            try {
                aiMockService.mockStreamResponse(agentKey, payload, emitter);
            } catch (Exception e) {
                logAiOperation(agentKey, payload, null, e);
                throw e;
            }
            logAiOperation(agentKey, payload, "流式响应完成", null);
            return;
        }

        String requestBody = buildRequestBody(config, wrapProtocolIfNeeded(agentKey, payload));
        HttpRequest request = buildHttpRequest(config, requestBody, true);

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                String errorMsg = "AI 代理调用失败：HTTP " + response.statusCode() + " " + body;
                logAiOperation(agentKey, payload, null, new RuntimeException(errorMsg));
                emitError(emitter, errorMsg);
                return;
            }

            // M11: 检查 Content-Type，非 SSE 格式时直接返回错误事件
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.contains("text/event-stream")) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                String errorMsg = "AI 代理调用失败：外部端点返回非 SSE 格式（Content-Type: " + contentType + "），响应：" + body;
                logAiOperation(agentKey, payload, null, new RuntimeException(errorMsg));
                emitError(emitter, errorMsg);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data.trim())) {
                            logAiOperation(agentKey, payload, "流式响应完成", null);
                            emitDone(emitter);
                            return;
                        }
                        emitData(emitter, data);
                    } else if (!line.trim().isEmpty()) {
                        // 非 SSE 格式行，直接透传
                        emitData(emitter, line);
                    }
                }
            }
            logAiOperation(agentKey, payload, "流式响应完成", null);
            emitDone(emitter);
        } catch (Exception e) {
            logAiOperation(agentKey, payload, null, e);
            emitError(emitter, "AI 代理流式调用异常：" + e.getMessage());
        }
    }

    /**
     * 解析 Agent 配置，优先按 accountId 查找，找不到回退到系统默认（account_id = 0 或 NULL）
     */
    private AiAgentConfig resolveConfig(String agentKey, Map<String, Object> payload) {
        Long accountId = extractAccountId(payload);
        AiAgentConfig config = aiAgentConfigService.getByAccountAndKey(accountId, agentKey);
        if (config == null) {
            // 回退到系统默认配置
            config = aiAgentConfigService.getBestMatchByAccountAndKey(accountId, agentKey);
        }
        return config;
    }

    private Long extractAccountId(Map<String, Object> payload) {
        Object accountId = payload != null ? payload.get("account_id") : null;
        if (accountId == null && payload != null && payload.get("context") instanceof Map) {
            Map<String, Object> ctx = (Map<String, Object>) payload.get("context");
            accountId = ctx.get("account_id");
        }
        if (accountId instanceof Number) {
            return ((Number) accountId).longValue();
        }
        if (accountId instanceof String) {
            try {
                return Long.valueOf((String) accountId);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * 将前端原始 payload 包装为标准 AI 协议格式
     *
     * 【协议格式说明】（版本 1.0）
     * {
     *   "protocol_version": "1.0",           // 协议版本号
     *   "function": "medical-expand",        // 功能标识（即 agentKey）
     *   "context": {                         // 业务上下文
     *     "account_id": 1,                   // 当前操作用户ID
     *     "clinic_id": "",                   // 诊所ID
     *     "scene_id": "medical-expand",      // 场景标识
     *     "scene_name": "病历编辑器",         // 场景中文名
     *     "timestamp": 1778860210475         // 毫秒时间戳
     *   },
     *   "input_fields": {                    // 输入字段包装
     *     "fields": {
     *       "label": "fields",
     *       "value": { "patient_id": "13", "patient_name": "尹涛", ... },
     *       "enabled": true
     *     }
     *   },
     *   "output_schema": {                   // 输出格式要求
     *     "format": "json",
     *     "required": [],
     *     "optional": []
     *   },
     *   "_original_payload": { ... },        // 保留原始 payload 便于调试
     *   "account_id": 1                      // 顶层冗余字段，兼容旧模板
     * }
     *
     * 【设计意图】
     * 1. 统一不同 AI 功能的请求格式，让外部 Webhook 平台只需对接一种协议
     * 2. input_fields 将每个字段包装为 {label, value, enabled} 元数据，便于 Webhook 理解字段含义
     * 3. _original_payload 保留原始数据，方便第三方平台按自己的方式解析
     *
     * @param agentKey 功能标识，如 medical-expand
     * @param payload  前端原始请求体，包含 account_id、fields 等
     * @return 包装后的标准协议 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> wrapProtocolIfNeeded(String agentKey, Map<String, Object> payload) {
        if (payload == null) {
            payload = new java.util.HashMap<>();
        }
        // 如果已经被包装过（例如前端直接传了标准协议），直接返回，避免重复包装
        if (payload.containsKey("protocol_version")) {
            return payload;
        }

        // 所有 agentKey 统一包装为标准协议
        Map<String, Object> wrapped = new java.util.HashMap<>();
        wrapped.put("protocol_version", "1.0");
        wrapped.put("function", agentKey);

        // 构建 context：包含账户、诊所、场景等上下文信息
        Map<String, Object> context = new java.util.HashMap<>();
        context.put("clinic_id", payload.getOrDefault("clinic_id", ""));
        context.put("account_id", payload.getOrDefault("account_id", ""));
        context.put("account_name", payload.getOrDefault("account_name", ""));
        context.put("scene_id", payload.getOrDefault("scene_id", agentKey));
        context.put("scene_name", payload.getOrDefault("scene_name", mapSceneName(agentKey)));
        context.put("timestamp", System.currentTimeMillis());
        wrapped.put("context", context);

        // 构建 input_fields：将 payload 中所有非保留字段转为标准字段元数据
        Map<String, Object> inputFields = extractInputFields(agentKey, payload);
        wrapped.put("input_fields", inputFields);

        // 构建 output_schema：描述期望的输出格式
        Map<String, Object> outputSchema = buildOutputSchema(agentKey, payload);
        wrapped.put("output_schema", outputSchema);

        // 保留原始 payload 中的其他字段，便于第三方平台按原始格式解析
        wrapped.put("_original_payload", payload);
        // 保留 message、session_id 等常用字段在顶层，兼容旧的 request_template 模板
        if (payload.containsKey("message")) {
            wrapped.put("message", payload.get("message"));
        }
        if (payload.containsKey("session_id")) {
            wrapped.put("session_id", payload.get("session_id"));
        }
        if (payload.containsKey("account_id")) {
            wrapped.put("account_id", payload.get("account_id"));
        }
        if (payload.containsKey("account_name")) {
            wrapped.put("account_name", payload.get("account_name"));
        }

        return wrapped;
    }

    /**
     * 提取 input_fields：将 payload 中所有非保留字段转为 input_fields
     * 不做任何 agentKey 级别的特殊处理，完全由前端决定传入哪些字段
     */
    private Map<String, Object> extractInputFields(String agentKey, Map<String, Object> payload) {
        Map<String, Object> inputFields = new java.util.HashMap<>();

        java.util.Set<String> reservedKeys = java.util.Set.of(
            "protocol_version", "function", "context", "input_fields", "output_schema",
            "_original_payload", "message", "session_id", "account_id", "account_name",
            "clinic_id", "scene_id", "scene_name", "functionKey"
        );

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (reservedKeys.contains(key)) continue;
            if (value instanceof Map || value instanceof java.util.List) {
                inputFields.put(key, buildFieldMeta(key, value));
            } else if (value != null) {
                inputFields.put(key, buildFieldMeta(key, value));
            }
        }

        return inputFields;
    }

    private Map<String, Object> buildFieldMeta(String key, Object value) {
        Map<String, Object> fieldMeta = new java.util.HashMap<>();
        fieldMeta.put("label", mapFieldLabel(key));
        fieldMeta.put("value", value);
        fieldMeta.put("enabled", true);
        return fieldMeta;
    }

    /**
     * 构建 output_schema
     * 优先使用前端传入的 output_schema，不做任何 agentKey 级别的预设
     * 第三方平台自由决定返回哪些字段
     */
    private Map<String, Object> buildOutputSchema(String agentKey, Map<String, Object> payload) {
        if (payload.containsKey("output_schema") && payload.get("output_schema") instanceof Map) {
            return (Map<String, Object>) payload.get("output_schema");
        }

        // 前端未传时返回空 schema，由第三方平台自由决定返回字段
        Map<String, Object> outputSchema = new java.util.HashMap<>();
        outputSchema.put("required", java.util.Collections.emptyList());
        outputSchema.put("optional", java.util.Collections.emptyList());
        outputSchema.put("format", "json");
        return outputSchema;
    }

    private String mapSceneName(String agentKey) {
        return switch (agentKey) {
            case "medical-record-expand" -> "病历编辑器";
            case "appointment-assist" -> "预约视图";
            case "followup-assist" -> "回访管理";
            case "consultation-assist" -> "咨询记录";
            case "consultation-dashboard" -> "咨询看板";
            case "treatment-assist" -> "治疗管理";
            case "treatment-record-assist" -> "治疗记录";
            case "financial-analysis" -> "财务流水";
            case "monthly-bill-analysis" -> "月度账单";
            case "lab-statistics-analysis" -> "加工统计";
            case "material-category-assist" -> "耗材分类";
            case "material-inventory-assist" -> "耗材档案";
            case "material-purchase-assist" -> "采购记录";
            case "material-statistics-analysis" -> "耗材统计";
            default -> agentKey;
        };
    }

    private String mapFieldLabel(String key) {
        return switch (key) {
            case "chiefComplaint" -> "主诉";
            case "presentIllnessHistory" -> "现病史";
            case "pastHistory" -> "既往史";
            case "examination" -> "检查";
            case "auxiliaryExamination" -> "辅助检查";
            case "diagnosis" -> "诊断";
            case "treatmentPlan" -> "治疗方案";
            case "treatment" -> "治疗文稿";
            case "medicalAdvice" -> "医嘱";
            case "prescription" -> "处方";
            case "patient_name" -> "患者姓名";
            case "doctor_name" -> "医生";
            case "appointment_date" -> "预约日期";
            case "appointment_time" -> "预约时间";
            case "appointment_purpose" -> "预约目的";
            case "status" -> "状态";
            case "treatment_content" -> "治疗内容";
            case "treatment_date" -> "治疗日期";
            case "followup_content" -> "随访内容";
            case "next_date" -> "下次随访日期";
            case "consultation_content" -> "咨询内容";
            case "channel" -> "渠道";
            case "intent_level" -> "意向强度";
            case "finance_records" -> "财务记录";
            case "date_range" -> "日期范围";
            case "month" -> "月份";
            case "bill_data" -> "账单数据";
            case "lab_stats" -> "加工统计";
            case "material_name" -> "耗材名称";
            case "inventory_list" -> "库存清单";
            case "stock_level" -> "库存水平";
            case "purchase_history" -> "采购历史";
            case "supplier_data" -> "供应商数据";
            case "consumption_data" -> "消耗数据";
            default -> key;
        };
    }

    /**
     * 构建最终 HTTP 请求体（支持两种模式）
     *
     * 【模式一：模板替换模式】（当 ai_agent_config.request_template 不为空时）
     * 在 AI 总览页面配置的 request_template 字段中，可以使用 {{变量名}} 占位符。
     * 本方法会将占位符替换为 payload 中对应的值。
     *
     * 示例模板：
     * {
     *   "prompt": "请根据以下病历信息扩写：{{fields}}",
     *   "account_id": "{{account_id}}",
     *   "scene": "{{function}}"
     * }
     * 其中 {{fields}} 会被替换为 payload.get("fields") 的值（即 JSON 字符串）。
     *
     * 【模式二：直接序列化模式】（当 request_template 为空时）
     * 直接将包装后的标准协议 payload 序列化为 JSON 字符串发送。
     * 这是默认行为，适用于大多数 n8n/Dify/Coze 等外部平台。
     *
     * @param config  AI Agent 配置，包含 request_template
     * @param payload 经过 wrapProtocolIfNeeded 包装后的标准协议 Map
     * @return 最终发送给外部 Webhook 的 HTTP 请求体字符串
     */
    private String buildRequestBody(AiAgentConfig config, Map<String, Object> payload) {
        String template = config.getRequestTemplate();
        // 模式二：没有配置模板时，直接序列化整个标准协议 payload 为 JSON
        if (!StringUtils.hasText(template)) {
            try {
                return objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                throw new RuntimeException("序列化请求体失败", e);
            }
        }

        // 模式一：使用正则匹配 {{变量名}} 并进行替换
        // PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}")
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();  // 提取变量名，如 "fields"、"account_id"
            Object val = payload != null ? payload.get(key) : null;
            // 如果变量值不存在则替换为空字符串；存在则转为字符串
            String replacement = val != null ? Matcher.quoteReplacement(String.valueOf(val)) : "";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private HttpRequest buildHttpRequest(AiAgentConfig config, String body, boolean stream) {
        String url = config.getEndpointUrl();
        int timeoutSeconds = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 60;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        // 认证头处理
        String authType = config.getAuthType();
        String authToken = config.getAuthToken();
        if (StringUtils.hasText(authType) && StringUtils.hasText(authToken)) {
            if ("bearer".equalsIgnoreCase(authType)) {
                builder.header("Authorization", "Bearer " + authToken);
            } else if ("basic".equalsIgnoreCase(authType)) {
                builder.header("Authorization", "Basic " + authToken);
            } else if ("api_key".equalsIgnoreCase(authType)) {
                builder.header("X-API-Key", authToken);
            } else {
                // 自定义 header 名，authType 作为 header 名
                builder.header(authType, authToken);
            }
        }

        if (stream) {
            builder.header("Accept", "text/event-stream");
        } else {
            // MCP Streamable HTTP 要求 Accept 同时包含 application/json 和 text/event-stream
            builder.header("Accept", "application/json, text/event-stream");
        }

        return builder.build();
    }

    private void emitData(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().name("data").data(data));
        } catch (Exception e) {
            // 客户端断开时忽略
        }
    }

    private void emitDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private void emitError(SseEmitter emitter, String error) {
        try {
            emitter.send(SseEmitter.event().name("error").data(error));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    // ==================== SSE 兼容格式方法（与旧 AiForwardService 一致） ====================

    /**
     * 发送 data 事件（兼容格式，不带 event name，前端 EventSource 直接读取 data 字段）
     */
    private void emitSseCompatData(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (Exception e) {
            // 客户端断开时忽略
        }
    }

    /**
     * 发送结束标记（兼容格式）
     */
    private void emitSseCompatDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 发送错误事件（兼容格式）
     */
    private void emitSseCompatError(SseEmitter emitter, String error) {
        try {
            emitter.send(SseEmitter.event().data("[ERROR]" + error));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 记录 AI 操作日志
     */
    private void logAiOperation(String agentKey, Map<String, Object> payload, String response, Throwable error) {
        try {
            AiOperationLog log = new AiOperationLog();
            log.setFunctionKey(agentKey);
            log.setAccountId(extractAccountId(payload));
            String input = objectMapper.writeValueAsString(payload);
            log.setInputSnapshot(input.length() > 2000 ? input.substring(0, 2000) : input);
            if (error != null) {
                String msg = error.getMessage();
                log.setErrorMsg(msg != null && msg.length() > 500 ? msg.substring(0, 500) : msg);
            } else {
                log.setAiOutput(response != null && response.length() > 2000 ? response.substring(0, 2000) : response);
            }
            log.setTokenUsed(0);
            log.setIsAdopted(false);
            log.setCreateTime(new Date());
            aiOperationLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志记录失败不影响主流程
        }
    }
}
