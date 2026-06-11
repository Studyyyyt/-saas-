package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.ApiKey;
import com.example.springboot.mapper.ApiKeyMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API Key 管理接口控制器（单 Key 模式）
 * 每个诊所仅有一个 API Key，供系统设置页面查看和重新生成
 */
@RestController
@RequestMapping("/api/api-key")
public class ApiKeyController {

    private final ApiKeyMapper apiKeyMapper;

    public ApiKeyController(ApiKeyMapper apiKeyMapper) {
        this.apiKeyMapper = apiKeyMapper;
    }

    /**
     * 获取当前诊所的 API Key（单 Key 模式，自动创建如果不存在）
     *
     * @param clinicId 诊所ID（可选，不传则查询唯一的那条记录）
     * @return 包含 maskedKey 的 API Key 信息
     */
    @GetMapping
    public Result getApiKey(@RequestParam(name = "clinicId", required = false) String clinicId) {
        ApiKey apiKey;

        if (clinicId == null || clinicId.isBlank()) {
            // 不传 clinicId：单 Key 模式下直接查询唯一记录
            apiKey = apiKeyMapper.findFirst();
            if (apiKey == null) {
                apiKey = createNewApiKey("1");
                apiKeyMapper.insert(apiKey);
            }
        } else {
            String cid = clinicId.trim();
            List<ApiKey> keys = apiKeyMapper.findByClinicId(cid);
            if (keys == null || keys.isEmpty()) {
                // 传了 clinicId 但查不到：回退到查询唯一记录，避免重复创建
                apiKey = apiKeyMapper.findFirst();
                if (apiKey == null) {
                    apiKey = createNewApiKey(cid);
                    apiKeyMapper.insert(apiKey);
                }
            } else {
                apiKey = keys.get(0);
            }
        }

        return Result.success(toMap(apiKey, false));
    }

    /**
     * 重新生成 API Key（删除旧的并创建新的）
     *
     * @param clinicId 诊所ID（可选，默认 "1"）
     * @return 新生成的 API Key 信息（含明文 key，仅展示一次）
     */
    @PostMapping("/regenerate")
    public Result regenerateApiKey(@RequestParam(name = "clinicId", required = false) String clinicId) {
        String cid = clinicId == null || clinicId.isBlank() ? "1" : clinicId.trim();

        apiKeyMapper.deleteByClinicId(cid);

        ApiKey newKey = createNewApiKey(cid);
        apiKeyMapper.insert(newKey);

        return Result.success(toMap(newKey, false));
    }

    /**
     * 更新诊所ID（支持自定义字符串如门诊拼音）
     *
     * @param clinicId    当前诊所ID（可选，默认 "1"）
     * @param newClinicId 新的诊所ID（必填，只允许字母、数字、下划线、横线）
     * @return 更新后的 API Key 信息
     */
    @PostMapping("/update-clinic-id")
    public Result updateClinicId(@RequestParam(name = "clinicId", required = false) String clinicId,
                                 @RequestParam("newClinicId") String newClinicId) {
        String cid = clinicId == null || clinicId.isBlank() ? "1" : clinicId.trim();
        String newCid = newClinicId == null ? "" : newClinicId.trim();

        if (!newCid.matches("^[a-zA-Z0-9_\\-]+$")) {
            return Result.error("诊所ID只能包含字母、数字、下划线和横线");
        }
        if (newCid.length() > 64) {
            return Result.error("诊所ID最长64个字符");
        }

        List<ApiKey> keys = apiKeyMapper.findByClinicId(cid);
        if (keys == null || keys.isEmpty()) {
            return Result.error("当前诊所不存在API Key");
        }

        apiKeyMapper.updateClinicId(cid, newCid);

        // 返回更新后的记录
        List<ApiKey> updated = apiKeyMapper.findByClinicId(newCid);
        return Result.success(toMap(updated.get(0), false));
    }

    /**
     * 创建一个新的 API Key 实体
     *
     * @param clinicId 诊所ID（支持字符串）
     * @return API Key 实体
     */
    private ApiKey createNewApiKey(String clinicId) {
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
     * 将 ApiKey 实体转换为 Map 响应
     *
     * @param apiKey API Key 实体
     * @param mask 是否对 key 值进行掩码处理
     * @return Map 结构
     */
    private Map<String, Object> toMap(ApiKey apiKey, boolean mask) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", apiKey.getId());
        result.put("clinicId", apiKey.getClinicId());
        result.put("name", apiKey.getName());
        result.put("key", mask ? maskKey(apiKey.getKey()) : apiKey.getKey());
        result.put("maskedKey", maskKey(apiKey.getKey()));
        result.put("isEnabled", apiKey.getIsEnabled());
        result.put("description", apiKey.getDescription());
        result.put("expiresAt", apiKey.getExpiresAt());
        result.put("lastUsedAt", apiKey.getLastUsedAt());
        result.put("usageCount", apiKey.getUsageCount());
        result.put("createdAt", apiKey.getCreatedAt());
        result.put("updatedAt", apiKey.getUpdatedAt());
        return result;
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
