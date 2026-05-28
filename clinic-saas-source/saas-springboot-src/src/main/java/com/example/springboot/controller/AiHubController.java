package com.example.springboot.controller;

import com.example.springboot.common.AesEncryptor;
import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.mapper.AiAgentConfigMapper;
import com.example.springboot.service.AiProxyService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * AI Hub 纯转发控制器
 * 提供 GET /api/ai/stream/{agentKey} SSE 流式接口
 * 删除后端 protocol_version 包装逻辑，原样透传前端请求到 n8n webhook
 */
@RestController
@RequestMapping("/api/ai")
public class AiHubController {

    private final AiAgentConfigMapper aiAgentConfigMapper;
    private final AiProxyService aiProxyService;

    public AiHubController(AiAgentConfigMapper aiAgentConfigMapper,
                           AiProxyService aiProxyService) {
        this.aiAgentConfigMapper = aiAgentConfigMapper;
        this.aiProxyService = aiProxyService;
    }

    /**
     * SSE 流式 AI 接口
     * 登录态校验通过检查 account_id 请求参数实现
     * 查询 ai_agent_config 表获取 endpoint_url，原样转发 {message, clinic_id, account_id, account_name, session_id} 到外部 webhook
     *
     * @param agentKey    Agent 标识
     * @param message     用户消息
     * @param sessionId   会话ID
     * @param accountId   当前用户ID
     * @param accountName 当前用户名称
     * @param clinicId    诊所ID
     * @return SSE 流式响应
     */
    @GetMapping(value = "/stream/{agentKey}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @PathVariable String agentKey,
            @RequestParam String message,
            @RequestParam(required = false) String sessionId,
            @RequestParam(name = "account_id") String accountId,
            @RequestParam(name = "account_name", required = false) String accountName,
            @RequestParam(name = "clinic_id", required = false) String clinicId) {

        SseEmitter emitter = new SseEmitter(300_000L);

        // 登录态校验：检查 account_id 是否存在
        if (accountId == null || accountId.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().data("[ERROR]未登录"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 将 account_id 转为 Long
        Long accountIdLong;
        try {
            accountIdLong = Long.valueOf(accountId);
        } catch (NumberFormatException e) {
            try {
                emitter.send(SseEmitter.event().data("[ERROR]无效的用户ID"));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
            return emitter;
        }

        // 查询 ai_agent_config 表获取配置（优先匹配指定 account，其次系统默认 account_id=0 或 NULL）
        AiAgentConfig agentConfig = aiAgentConfigMapper.selectBestMatchByAccountIdAndKey(accountIdLong, agentKey);
        if (agentConfig == null || !StringUtils.hasText(agentConfig.getEndpointUrl())) {
            try {
                emitter.send(SseEmitter.event().data("[ERROR]AI服务未配置，请在系统设置中配置Webhook地址"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 构造请求体（扁平JSON，不包装）
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("message", message);
        payload.put("session_id", sessionId != null ? sessionId : java.util.UUID.randomUUID().toString());
        payload.put("account_id", accountId);
        payload.put("account_name", accountName != null ? accountName : "");
        payload.put("clinic_id", clinicId != null ? clinicId : "1");

        // 异步转发 SSE 流（使用兼容格式，与旧 AiForwardService 输出一致）
        new Thread(() -> aiProxyService.forwardStreamSseCompat(agentKey, payload, emitter)).start();
        return emitter;
    }
}
