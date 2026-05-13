package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.AiProxyService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * AI 统一代理控制器
 * 所有 AI 请求通过此接口转发到外部工作流平台
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:7070")
public class AiProxyController {

    private final AiProxyService aiProxyService;

    public AiProxyController(AiProxyService aiProxyService) {
        this.aiProxyService = aiProxyService;
    }

    /**
     * 统一 AI 代理接口
     *
     * @param agentKey    代理标识，如 medical-expand、default、finance
     * @param payload     请求体数据
     * @param acceptHeader Accept 请求头，用于判断 SSE 或 JSON 模式
     * @return JSON 模式返回 Result，SSE 模式返回 SseEmitter
     */
    @PostMapping("/proxy/{agentKey}")
    public Object proxy(
            @PathVariable String agentKey,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Accept", defaultValue = "application/json") String acceptHeader) {

        boolean isSse = acceptHeader != null && acceptHeader.contains("text/event-stream");

        if (isSse) {
            SseEmitter emitter = new SseEmitter(120000L);
            new Thread(() -> aiProxyService.forwardStream(agentKey, payload, emitter)).start();
            return emitter;
        }

        try {
            String response = aiProxyService.forward(agentKey, payload);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("AI 代理调用失败：" + e.getMessage());
        }
    }
}
