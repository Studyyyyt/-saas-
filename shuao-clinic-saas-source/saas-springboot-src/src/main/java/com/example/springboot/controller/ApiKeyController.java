package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ApiKey;
import com.example.springboot.mapper.ApiKeyMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API Key 管理接口控制器
 * 供前端系统设置页面展示和重新生成 API Key
 */
@RestController
@RequestMapping("/api/api-key")
public class ApiKeyController {

    private final ApiKeyMapper apiKeyMapper;

    public ApiKeyController(ApiKeyMapper apiKeyMapper) {
        this.apiKeyMapper = apiKeyMapper;
    }

    /**
     * 获取当前诊所的 API Key（自动创建如果不存在）
     *
     * @param clinicId 诊所ID（可选，默认 1）
     * @return 包含 maskedKey 的 API Key 信息
     */
    @GetMapping
    public Result getApiKey(@RequestParam(name = "clinicId", required = false) Long clinicId) {
        Long cid = clinicId == null ? 1L : clinicId;

        List<ApiKey> keys = apiKeyMapper.findByClinicId(cid);
        ApiKey apiKey;
        if (keys == null || keys.isEmpty()) {
            // 自动创建新的 API Key
            apiKey = createNewApiKey(cid);
            apiKeyMapper.insert(apiKey);
        } else {
            apiKey = keys.get(0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", apiKey.getId());
        result.put("clinicId", apiKey.getClinicId());
        result.put("name", apiKey.getName());
        result.put("maskedKey", maskKey(apiKey.getKey()));
        result.put("isEnabled", apiKey.getIsEnabled());
        result.put("createdAt", apiKey.getCreatedAt());
        result.put("updatedAt", apiKey.getUpdatedAt());

        return Result.success(result);
    }

    /**
     * 重新生成 API Key
     * 删除旧的并创建新的
     *
     * @param clinicId 诊所ID（可选，默认 1）
     * @return 新生成的 API Key 信息（含明文 key，仅展示一次）
     */
    @PostMapping("/regenerate")
    public Result regenerateApiKey(@RequestParam(name = "clinicId", required = false) Long clinicId) {
        Long cid = clinicId == null ? 1L : clinicId;

        // 删除该诊所下所有旧 Key
        apiKeyMapper.deleteByClinicId(cid);

        // 创建新 Key
        ApiKey newKey = createNewApiKey(cid);
        apiKeyMapper.insert(newKey);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", newKey.getId());
        result.put("clinicId", newKey.getClinicId());
        result.put("name", newKey.getName());
        result.put("key", newKey.getKey()); // 明文 key，仅展示一次
        result.put("maskedKey", maskKey(newKey.getKey()));
        result.put("isEnabled", newKey.getIsEnabled());
        result.put("createdAt", newKey.getCreatedAt());
        result.put("updatedAt", newKey.getUpdatedAt());

        return Result.success(result);
    }

    /**
     * 创建一个新的 API Key 实体
     *
     * @param clinicId 诊所ID
     * @return API Key 实体
     */
    private ApiKey createNewApiKey(Long clinicId) {
        ApiKey apiKey = new ApiKey();
        apiKey.setClinicId(clinicId);
        apiKey.setKey(generateKey());
        apiKey.setName("默认Key");
        apiKey.setIsEnabled(true);
        return apiKey;
    }

    /**
     * 生成 API Key 字符串
     * 格式：sk-saas- + 16 位随机字符（UUID 截取）
     *
     * @return API Key 字符串
     */
    private String generateKey() {
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return "sk-saas-" + randomPart;
    }

    /**
     * 对 API Key 进行掩码处理
     * 显示前 6 位 + ****** + 后 3 位
     *
     * @param key 原始 API Key
     * @return 掩码后的字符串
     */
    private String maskKey(String key) {
        if (!StringUtils.hasText(key) || key.length() <= 9) {
            return "******";
        }
        return key.substring(0, 6) + "******" + key.substring(key.length() - 3);
    }
}
