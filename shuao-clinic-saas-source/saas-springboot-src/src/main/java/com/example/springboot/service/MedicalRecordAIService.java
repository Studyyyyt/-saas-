package com.example.springboot.service;

import com.example.springboot.entity.*;
import com.example.springboot.mapper.AiFewShotExampleMapper;
import com.example.springboot.mapper.AiPromptTemplateMapper;
import com.example.springboot.mapper.MedicalRecordAIFieldMapper;
import com.example.springboot.mapper.TreatmentSceneMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 病历 AI 扩写服务
 * 改造后：不再直接调用 AI 模型，所有 AI 逻辑外包到外部工作流平台
 */
@Service
public class MedicalRecordAIService {

    private final AiPromptTemplateMapper promptMapper;
    private final AiFewShotExampleMapper fewShotMapper;
    private final MedicalRecordAIFieldMapper fieldMapper;
    private final TreatmentSceneMapper sceneMapper;
    private final AiConfigService aiConfigService;
    private final AiProxyService aiProxyService;
    private final ObjectMapper objectMapper;

    public MedicalRecordAIService(AiPromptTemplateMapper promptMapper,
                                  AiFewShotExampleMapper fewShotMapper,
                                  MedicalRecordAIFieldMapper fieldMapper,
                                  TreatmentSceneMapper sceneMapper,
                                  AiConfigService aiConfigService,
                                  AiProxyService aiProxyService,
                                  ObjectMapper objectMapper) {
        this.promptMapper = promptMapper;
        this.fewShotMapper = fewShotMapper;
        this.fieldMapper = fieldMapper;
        this.sceneMapper = sceneMapper;
        this.aiConfigService = aiConfigService;
        this.aiProxyService = aiProxyService;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取病历扩写完整配置
     */
    public Map<String, Object> getFullConfig() {
        Map<String, Object> result = new HashMap<>();

        // 1. 读取提示词模板
        AiPromptTemplate template = promptMapper.selectByScene("medical_expand");
        if (template == null) {
            template = new AiPromptTemplate();
            template.setScene("medical_expand");
            template.setName("病历扩写模板");
            template.setSystemPrompt(getDefaultSystemPrompt());
            template.setTemperature(0.2);
            template.setMaxTokens(2000);
            template.setResponseFormat("json");
            template.setIsActive(true);
            template.setVersion(1);
        }

        // 解析 extra_config 到前端 config 结构
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", Boolean.TRUE.equals(template.getIsActive()));
        config.put("temperature", template.getTemperature() != null ? template.getTemperature() : 0.2);
        config.put("maxTokens", template.getMaxTokens() != null ? template.getMaxTokens() : 2000);
        String systemPrompt = template.getSystemPrompt();
        config.put("systemPrompt", (systemPrompt != null && !systemPrompt.trim().isEmpty()) ? systemPrompt : getDefaultSystemPrompt());

        // 解析 extraConfig JSON
        if (StringUtils.hasText(template.getExtraConfig())) {
            try {
                Map<String, Object> extra = objectMapper.readValue(template.getExtraConfig(), Map.class);
                config.put("emptyFieldStrategy", extra.getOrDefault("emptyFieldStrategy", "leave"));
                config.put("forbidAssertion", extra.getOrDefault("forbidAssertion", true));
                config.put("sensitiveWords", extra.getOrDefault("sensitiveWords", "确诊,绝对,保证,100%,肯定"));
                config.put("checkDiagnosisTone", extra.getOrDefault("checkDiagnosisTone", true));
                config.put("checkChiefComplaintLength", extra.getOrDefault("checkChiefComplaintLength", true));
                config.put("checkHistoryTime", extra.getOrDefault("checkHistoryTime", true));
            } catch (Exception e) {
                config.put("emptyFieldStrategy", "leave");
                config.put("forbidAssertion", true);
                config.put("sensitiveWords", "确诊,绝对,保证,100%,肯定");
                config.put("checkDiagnosisTone", true);
                config.put("checkChiefComplaintLength", true);
                config.put("checkHistoryTime", true);
            }
        } else {
            config.put("emptyFieldStrategy", "leave");
            config.put("forbidAssertion", true);
            config.put("sensitiveWords", "确诊,绝对,保证,100%,肯定");
            config.put("checkDiagnosisTone", true);
            config.put("checkChiefComplaintLength", true);
            config.put("checkHistoryTime", true);
        }

        result.put("config", config);

        // 2. 读取字段配置
        List<MedicalRecordAIField> fields = fieldMapper.selectAll();
        if (fields == null || fields.isEmpty()) {
            fields = getDefaultFields();
        }
        result.put("fields", fields);

        // 3. 读取 Few-shot 示例
        List<AiFewShotExample> fewShots = fewShotMapper.selectByTemplateId(template.getId() != null ? template.getId() : 0L);
        List<Map<String, Object>> fewShotList = new ArrayList<>();
        for (AiFewShotExample ex : fewShots) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ex.getId());
            item.put("input", ex.getInputContent());
            item.put("output", ex.getOutputContent());
            fewShotList.add(item);
        }
        result.put("fewShots", fewShotList);

        return result;
    }

    /**
     * 保存病历扩写配置
     */
    public void saveConfig(MedicalRecordAIConfigDTO dto) {
        Map<String, Object> configMap = dto.getConfig();
        // 允许只更新字段配置或 fewShots，configMap 为 null 时不更新模板
        if (configMap == null && (dto.getFields() == null || dto.getFields().isEmpty()) && (dto.getFewShots() == null || dto.getFewShots().isEmpty())) {
            throw new IllegalArgumentException("配置不能为空");
        }

        // 1. 保存/更新提示词模板（仅在 configMap 不为 null 时更新）
        AiPromptTemplate template = promptMapper.selectByScene("medical_expand");
        if (configMap != null) {
            boolean isNew = false;
            if (template == null) {
                template = new AiPromptTemplate();
                template.setScene("medical_expand");
                template.setName("病历扩写模板");
                template.setVersion(1);
                isNew = true;
            }

            Object enabled = configMap.get("enabled");
            template.setIsActive(enabled != null ? Boolean.valueOf(String.valueOf(enabled)) : true);

            Object temperature = configMap.get("temperature");
            template.setTemperature(temperature != null ? Double.valueOf(String.valueOf(temperature)) : 0.2);

            Object maxTokens = configMap.get("maxTokens");
            template.setMaxTokens(maxTokens != null ? Integer.valueOf(String.valueOf(maxTokens)) : 2000);

            Object systemPrompt = configMap.get("systemPrompt");
            if (systemPrompt != null) {
                template.setSystemPrompt(String.valueOf(systemPrompt));
            }

            // 组装 extraConfig JSON
            Map<String, Object> extra = new HashMap<>();
            extra.put("emptyFieldStrategy", configMap.getOrDefault("emptyFieldStrategy", "leave"));
            extra.put("forbidAssertion", configMap.getOrDefault("forbidAssertion", true));
            extra.put("sensitiveWords", configMap.getOrDefault("sensitiveWords", "确诊,绝对,保证,100%,肯定"));
            extra.put("checkDiagnosisTone", configMap.getOrDefault("checkDiagnosisTone", true));
            extra.put("checkChiefComplaintLength", configMap.getOrDefault("checkChiefComplaintLength", true));
            extra.put("checkHistoryTime", configMap.getOrDefault("checkHistoryTime", true));
            try {
                template.setExtraConfig(objectMapper.writeValueAsString(extra));
            } catch (Exception e) {
                template.setExtraConfig("{}");
            }

            if (isNew) {
                promptMapper.insert(template);
            } else {
                promptMapper.update(template);
            }
        }

        // 2. 保存字段配置
        List<MedicalRecordAIField> fields = dto.getFields();
        if (fields != null) {
            for (MedicalRecordAIField field : fields) {
                fieldMapper.upsert(field);
            }
        }

        // 3. 保存 Few-shot 示例（先删后插）
        if (template != null && template.getId() != null) {
            Long templateId = template.getId();
            fewShotMapper.deleteByTemplateId(templateId);
            List<MedicalRecordAIConfigDTO.FewShotItem> fewShots = dto.getFewShots();
            if (fewShots != null) {
                int sortOrder = 0;
                for (MedicalRecordAIConfigDTO.FewShotItem item : fewShots) {
                    if (!StringUtils.hasText(item.getInput()) && !StringUtils.hasText(item.getOutput())) {
                        continue;
                    }
                    AiFewShotExample ex = new AiFewShotExample();
                    ex.setTemplateId(templateId);
                    ex.setInputContent(item.getInput());
                    ex.setOutputContent(item.getOutput());
                    ex.setSortOrder(sortOrder++);
                    fewShotMapper.insert(ex);
                }
            }
        }
    }

    /**
     * 执行病历扩写（兼容旧接口）
     */
    public Map<String, Object> expand(MedicalRecordExpandDTO dto) {
        TreatmentSceneExpandRequest req = new TreatmentSceneExpandRequest();
        req.setFields(dto.getFields());
        req.setTestMode(dto.getTestMode());
        return expand(req);
    }

    /**
     * 预览渲染后的 Prompt（不调用 AI）
     * 支持预览未保存的配置：systemPrompt、fewShots、emptyFieldStrategy
     */
    public String previewPrompt(TreatmentSceneExpandRequest dto) {
        Map<String, String> inputFields = dto.getFields();
        if (inputFields == null) {
            inputFields = new HashMap<>();
        }

        // 1. 获取配置（优先使用传入的未保存配置）
        AiPromptTemplate template = promptMapper.selectByScene("medical_expand");
        if (template == null) {
            template = new AiPromptTemplate();
            template.setSystemPrompt(getDefaultSystemPrompt());
        }
        // 如果传入了未保存的 systemPrompt，覆盖数据库中的模板
        if (StringUtils.hasText(dto.getSystemPrompt())) {
            template.setSystemPrompt(dto.getSystemPrompt());
        }

        List<MedicalRecordAIField> fields = fieldMapper.selectAllEnabled();
        if (fields == null || fields.isEmpty()) {
            fields = getDefaultFields();
        }

        // 读取空字段策略（优先使用传入的）
        String emptyFieldStrategy = "leave";
        if (StringUtils.hasText(dto.getEmptyFieldStrategy())) {
            emptyFieldStrategy = dto.getEmptyFieldStrategy();
        } else if (StringUtils.hasText(template.getExtraConfig())) {
            try {
                Map<String, Object> extra = objectMapper.readValue(template.getExtraConfig(), Map.class);
                Object strategy = extra.get("emptyFieldStrategy");
                if (strategy != null) {
                    emptyFieldStrategy = String.valueOf(strategy);
                }
            } catch (Exception ignored) {}
        }

        // 2. 读取场景约束
        SceneConstraint constraint = null;
        if (dto.getSceneId() != null) {
            constraint = buildSceneConstraint(dto.getSceneId(), dto.getOperations());
        }

        // 3. 读取 Few-shot 示例（优先使用传入的未保存示例）
        List<AiFewShotExample> fewShots = null;
        if (dto.getFewShots() != null && !dto.getFewShots().isEmpty()) {
            fewShots = new ArrayList<>();
            for (Map<String, String> fs : dto.getFewShots()) {
                AiFewShotExample ex = new AiFewShotExample();
                ex.setInputContent(fs.get("input"));
                ex.setOutputContent(fs.get("output"));
                fewShots.add(ex);
            }
        } else if (template != null && template.getId() != null) {
            fewShots = fewShotMapper.selectByTemplateId(template.getId());
        }

        // 4. 渲染并返回提示词
        return renderPrompt(template, inputFields, fields, emptyFieldStrategy, constraint, fewShots);
    }

    /**
     * 执行病历扩写（支持治疗场景驱动）
     * 改造后：组装 payload 调用 AiProxyService 转发到外部工作流
     */
    public Map<String, Object> expand(TreatmentSceneExpandRequest dto) {
        // 检查 AI 全局开关与病历扩写功能开关
        aiConfigService.assertAiEnabled("medical-expand");
        boolean debug = Boolean.TRUE.equals(aiConfigService.getDebugMode());

        Map<String, String> inputFields = dto.getFields();
        if (inputFields == null || inputFields.isEmpty()) {
            throw new IllegalArgumentException("输入字段不能为空");
        }

        // 1. 获取配置
        List<MedicalRecordAIField> fields = fieldMapper.selectAllEnabled();
        if (fields == null || fields.isEmpty()) {
            fields = getDefaultFields();
        }

        // 读取 extraConfig 中的空字段策略
        String emptyFieldStrategy = "leave";
        AiPromptTemplate template = promptMapper.selectByScene("medical_expand");
        if (StringUtils.hasText(template != null ? template.getExtraConfig() : null)) {
            try {
                Map<String, Object> extra = objectMapper.readValue(template.getExtraConfig(), Map.class);
                Object strategy = extra.get("emptyFieldStrategy");
                if (strategy != null) {
                    emptyFieldStrategy = String.valueOf(strategy);
                }
            } catch (Exception ignored) {}
        }

        // 2. 读取场景约束（如果传了场景ID）
        SceneConstraint constraint = null;
        if (dto.getSceneId() != null) {
            constraint = buildSceneConstraint(dto.getSceneId(), dto.getOperations());
        }

        // 3. 组装 payload 转发到外部工作流
        Map<String, Object> payload = new HashMap<>();
        // 将病历字段平铺到 payload 顶层，使 extractInputFields 能正确提取每个字段
        if (inputFields != null) {
            payload.putAll(inputFields);
        }
        payload.put("scene_id", dto.getSceneId());
        payload.put("scene_name", constraint != null ? constraint.sceneName : "");
        payload.put("operations", dto.getOperations());
        payload.put("empty_field_strategy", emptyFieldStrategy);

        // 收集启用的字段元数据
        List<Map<String, Object>> enabledFields = new ArrayList<>();
        for (MedicalRecordAIField field : fields) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("key", field.getFieldKey());
            meta.put("name", field.getFieldName());
            meta.put("required", field.getIsRequired());
            meta.put("max_length", field.getMaxLength());
            meta.put("default_value", field.getDefaultValue());
            meta.put("validation_rule", field.getValidationRule());
            meta.put("validation_hint", field.getValidationHint());
            enabledFields.add(meta);
        }
        payload.put("enabled_fields", enabledFields);

        if (debug) {
            System.out.println("[AI Debug] medical-expand payload: " + payload);
        }

        // 4. 调用代理服务，直接透传外部返回的 JSON
        String response = aiProxyService.forward("medical-expand", payload);

        if (debug) {
            System.out.println("[AI Debug] medical-expand proxy response: " + response);
        }

        // 5. 将外部返回的 JSON 解析为 Map<String, Object> 返回给前端（L6: 保留原始类型，避免嵌套对象被强制序列化）
        try {
            Map<String, Object> result = objectMapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            return result;
        } catch (Exception e) {
            throw new RuntimeException("外部工作流返回格式异常：" + e.getMessage() + "，原始响应：" + response);
        }
    }

    /**
     * 场景约束对象
     */
    private static class SceneConstraint {
        String sceneName;
        List<String> operations = new ArrayList<>();
        List<String> forbiddenKeywords = new ArrayList<>();
        List<String> requiredKeywords = new ArrayList<>();
    }

    public SceneConstraint buildSceneConstraint(Long sceneId, List<String> selectedOperations) {
        SceneConstraint constraint = new SceneConstraint();
        TreatmentScene scene = sceneMapper.selectById(sceneId);
        if (scene == null) {
            return null;
        }
        constraint.sceneName = scene.getName();

        List<TreatmentSceneStep> steps = sceneMapper.selectStepsBySceneId(sceneId);
        if (steps == null || steps.isEmpty()) {
            return constraint;
        }

        // 如果没有选择具体操作，默认选择第一个步骤
        if (selectedOperations == null || selectedOperations.isEmpty()) {
            selectedOperations = new ArrayList<>();
            selectedOperations.add(steps.get(0).getName());
        }

        for (TreatmentSceneStep step : steps) {
            if (selectedOperations.contains(step.getName())) {
                constraint.operations.add(step.getName());
                if (StringUtils.hasText(step.getForbiddenKeywords())) {
                    for (String kw : step.getForbiddenKeywords().split(",")) {
                        String trimmed = kw.trim();
                        if (!trimmed.isEmpty()) {
                            constraint.forbiddenKeywords.add(trimmed);
                        }
                    }
                }
                if (StringUtils.hasText(step.getRequiredKeywords())) {
                    for (String kw : step.getRequiredKeywords().split(",")) {
                        String trimmed = kw.trim();
                        if (!trimmed.isEmpty()) {
                            constraint.requiredKeywords.add(trimmed);
                        }
                    }
                }
            }
        }
        return constraint;
    }

    private String renderPrompt(AiPromptTemplate template, Map<String, String> inputFields,
                                List<MedicalRecordAIField> enabledFields, String emptyFieldStrategy,
                                SceneConstraint constraint, List<AiFewShotExample> fewShots) {
        String prompt = template.getSystemPrompt();
        if (prompt == null || prompt.trim().isEmpty()) {
            prompt = getDefaultSystemPrompt();
        }
        prompt = prompt.replace("{input_fields}", formatFields(inputFields));
        // 知识库注入（简化实现）
        prompt = prompt.replace("{kb_content}", "【口腔医学术语参考】牙位记录采用FDI系统，常见症状描述规范...");

        // 根据空字段策略替换任务描述
        String strategyDesc;
        switch (emptyFieldStrategy) {
            case "leave":
                strategyDesc = "- 对于医生已填写的字段：基于已有内容进行扩充、规范化和专业化\n" +
                        "- 对于医生未填写的字段：保持空字符串，不要生成任何内容\n" +
                        "- 允许字段返回空字符串，但已填写字段必须有实质内容";
                break;
            case "prompt":
                strategyDesc = "- 对于医生已填写的字段：基于已有内容进行扩充、规范化和专业化\n" +
                        "- 对于医生未填写的字段：返回「请医生手动填写」\n" +
                        "- 不允许已填写字段返回空字符串";
                break;
            case "generate":
            default:
                strategyDesc = "- 对于医生已填写的字段：基于已有内容进行扩充、规范化和专业化\n" +
                        "- 对于医生未填写的字段：根据主诉和已填信息，结合口腔医学常识，智能推断并生成合理内容\n" +
                        "- 不允许有任何字段为空字符串，每个字段都必须有实质内容";
                break;
        }
        // 替换任务描述中的策略部分（使用更精确的匹配）
        prompt = prompt.replaceAll(
                "【任务】\n根据医生填写的简要信息，为病历所有字段生成完整、规范的内容。\n" +
                "- 对于医生已填写的字段：基于已有内容进行扩充、规范化和专业化\n" +
                "- 对于医生未填写的字段：根据主诉和已填信息，结合口腔医学常识，智能推断并生成合理内容\n" +
                "- 不允许有任何字段为空字符串，每个字段都必须有实质内容",
                "【任务】\n根据医生填写的简要信息，为病历字段生成规范内容。\n" + strategyDesc);

        // 根据启用的字段列表重新生成 JSON 格式要求（只包含启用的字段）
        StringBuilder jsonBlock = new StringBuilder();
        jsonBlock.append("{\n");
        for (MedicalRecordAIField field : enabledFields) {
            String desc = getFieldJsonDescription(field.getFieldKey());
            if (StringUtils.hasText(field.getDefaultValue())) {
                desc += "，若医生未填写则返回默认值：" + field.getDefaultValue();
            }
            jsonBlock.append("  \"").append(field.getFieldKey()).append("\": \"").append(desc).append("\",\n");
        }
        // 移除最后一个逗号
        if (jsonBlock.length() > 2 && jsonBlock.charAt(jsonBlock.length() - 2) == ',') {
            jsonBlock.delete(jsonBlock.length() - 2, jsonBlock.length() - 1);
        }
        jsonBlock.append("}");

        // 替换 Prompt 中的 JSON 格式块
        String jsonPattern = "\\{\\n" +
                "  \"chiefComplaint\".*?" +
                "  \"notes\".*?" +
                "\\}";
        prompt = prompt.replaceAll(jsonPattern, jsonBlock.toString().replace("$", "\\$"));

        // 根据空字段策略调整绝对禁止第5条
        if ("leave".equals(emptyFieldStrategy)) {
            prompt = prompt.replace(
                    "5. 禁止任何字段返回空字符串，未填写字段必须智能生成合理内容",
                    "5. 已填写字段禁止返回空字符串，未填写字段必须返回空字符串");
        } else if ("prompt".equals(emptyFieldStrategy)) {
            prompt = prompt.replace(
                    "5. 禁止任何字段返回空字符串，未填写字段必须智能生成合理内容",
                    "5. 已填写字段禁止返回空字符串，未填写字段返回「请医生手动填写」");
        }

        // 注入场景约束
        if (constraint != null) {
            StringBuilder sceneBlock = new StringBuilder();
            sceneBlock.append("\n\n【当前诊疗场景】\n");
            sceneBlock.append("治疗类型：").append(constraint.sceneName).append("\n");
            if (!constraint.operations.isEmpty()) {
                sceneBlock.append("本次已做操作：").append(String.join("、", constraint.operations)).append("\n");
            }
            if (!constraint.forbiddenKeywords.isEmpty()) {
                sceneBlock.append("【绝对禁止】返回内容中严禁出现以下词汇或含义：")
                        .append(String.join("、", constraint.forbiddenKeywords)).append("\n");
            }
            if (!constraint.requiredKeywords.isEmpty()) {
                sceneBlock.append("【必须包含】治疗记录中必须提及以下要点：")
                        .append(String.join("、", constraint.requiredKeywords)).append("\n");
            }
            sceneBlock.append("\n【核心约束】你只允许描述医生本次实际完成的操作，严禁编造未做步骤。治疗计划只能写「择期行下一步操作」，不能跳过阶段。\n");

            prompt = prompt + sceneBlock.toString();
        } else {
            prompt = prompt.replace("{disease_type}", "");
        }

        // 注入 Few-shot 示例
        if (fewShots != null && !fewShots.isEmpty()) {
            StringBuilder fsBlock = new StringBuilder();
            fsBlock.append("\n\n【扩写示例】（请严格参照以下示例的格式、风格和详细程度进行扩写）\n");
            int idx = 1;
            for (AiFewShotExample ex : fewShots) {
                fsBlock.append("\n示例").append(idx++).append("：\n");
                if (StringUtils.hasText(ex.getInputContent())) {
                    fsBlock.append("【输入】\n").append(ex.getInputContent()).append("\n");
                }
                if (StringUtils.hasText(ex.getOutputContent())) {
                    fsBlock.append("【输出】\n").append(ex.getOutputContent()).append("\n");
                }
            }
            fsBlock.append("\n请参照上述示例的风格和格式，为当前输入生成输出。\n");
            prompt = prompt + fsBlock.toString();
        }

        return prompt;
    }

    private String getFieldJsonDescription(String fieldKey) {
        Map<String, String> map = new HashMap<>();
        map.put("chiefComplaint", "主诉，≤30字，格式：部位+症状+时间");
        map.put("historyOfPresentIllness", "现病史，必须包含起病时间、主要症状、演变过程、诊疗经过");
        map.put("pastHistory", "既往史，包括全身疾病史、口腔疾病史、手术外伤史、过敏史等，无特殊可写'否认全身系统性疾病史，否认药物过敏史'");
        map.put("generalCondition", "一般情况，包括精神、饮食、睡眠、大小便等");
        map.put("examinationFindings", "口腔专科检查所见，包括视诊、叩诊、探诊、松动度、冷热测等");
        map.put("auxiliaryExamination", "辅助检查结果，如X线片、CBCT、血常规等，未做可写'暂缺，建议完善'");
        map.put("diagnosis", "诊断，必须用建议性语气（考虑/疑似/待排/可能），严禁使用确诊性词汇");
        map.put("treatmentPlan", "治疗计划，包括拟行治疗方案、步骤和预期效果");
        map.put("treatment", "治疗文稿，记录本次就诊实际进行的处置操作");
        map.put("medicalAdvice", "医嘱，包括注意事项、用药建议、复诊时间等");
        map.put("prescription", "处方，如有用药则记录药物名称和用法，无则写'暂无'");
        map.put("notes", "病历备注，记录特殊情况、患者诉求、沟通要点等");
        return map.getOrDefault(fieldKey, fieldKey);
    }

    private String formatFields(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private String getDefaultSystemPrompt() {
        return "你是一位资深口腔全科医生助理，擅长将简要病历记录扩写为规范的专业病历。\n\n" +
                "【任务】\n根据医生填写的简要信息，为病历所有字段生成完整、规范的内容。\n" +
                "- 对于医生已填写的字段：基于已有内容进行扩充、规范化和专业化\n" +
                "- 对于医生未填写的字段：根据主诉和已填信息，结合口腔医学常识，智能推断并生成合理内容\n" +
                "- 不允许有任何字段为空字符串，每个字段都必须有实质内容\n\n" +
                "【知识库参考】（仅参考格式和术语，不编造未提及的病情）\n{kb_content}\n\n" +
                "【输入信息】\n{input_fields}\n\n" +
                "【输出格式要求】\n严格按以下 JSON 格式返回，不要包含任何其他内容，必须包含全部字段：\n" +
                "{\n" +
                "  \"chiefComplaint\": \"主诉，≤30字，格式：部位+症状+时间\",\n" +
                "  \"historyOfPresentIllness\": \"现病史，必须包含起病时间、主要症状、演变过程、诊疗经过\",\n" +
                "  \"pastHistory\": \"既往史，包括全身疾病史、口腔疾病史、手术外伤史、过敏史等，无特殊可写'否认全身系统性疾病史，否认药物过敏史'\",\n" +
                "  \"generalCondition\": \"一般情况，包括精神、饮食、睡眠、大小便等\",\n" +
                "  \"examinationFindings\": \"口腔专科检查所见，包括视诊、叩诊、探诊、松动度、冷热测等\",\n" +
                "  \"auxiliaryExamination\": \"辅助检查结果，如X线片、CBCT、血常规等，未做可写'暂缺，建议完善'\",\n" +
                "  \"diagnosis\": \"诊断，必须用建议性语气（考虑/疑似/待排/可能），严禁使用确诊性词汇\",\n" +
                "  \"treatmentPlan\": \"治疗计划，包括拟行治疗方案、步骤和预期效果\",\n" +
                "  \"treatment\": \"治疗文稿，记录本次就诊实际进行的处置操作\",\n" +
                "  \"medicalAdvice\": \"医嘱，包括注意事项、用药建议、复诊时间等\",\n" +
                "  \"prescription\": \"处方，如有用药则记录药物名称和用法，无则写'暂无'\",\n" +
                "  \"notes\": \"病历备注，记录特殊情况、患者诉求、沟通要点等\"\n" +
                "}\n\n" +
                "【绝对禁止】\n" +
                "1. 禁止编造患者未提及的症状、检查结果\n" +
                "2. 禁止输出确诊性断言，诊断严禁使用'确诊'、'明确诊断'、'肯定'等词汇\n" +
                "3. 禁止输出具体药物剂量\n" +
                "4. 禁止输出\"建议到上级医院\"等推诿用语\n" +
                "5. 禁止任何字段返回空字符串，未填写字段必须智能生成合理内容\n\n" +
                "【语气要求】\n专业、客观、严谨，使用标准口腔医学术语。";
    }

    private List<MedicalRecordAIField> getDefaultFields() {
        List<MedicalRecordAIField> list = new ArrayList<>();
        list.add(createField("chiefComplaint", "主诉", true, 30, true, "", "主诉必须包含部位+症状+时间", 1));
        list.add(createField("historyOfPresentIllness", "现病史", true, 500, true, "", "现病史必须包含时间描述", 2));
        list.add(createField("pastHistory", "既往史", true, 300, false, "", "", 3));
        list.add(createField("generalCondition", "一般情况", true, 100, false, "", "", 4));
        list.add(createField("examinationFindings", "检查所见", true, 500, false, "", "", 5));
        list.add(createField("auxiliaryExamination", "辅助检查", true, 300, false, "", "", 6));
        list.add(createField("diagnosis", "诊断", true, 100, true, "", "诊断必须用建议性语气", 7));
        list.add(createField("treatmentPlan", "治疗计划", true, 300, false, "", "", 8));
        list.add(createField("treatment", "治疗文稿", true, 500, false, "", "", 9));
        list.add(createField("medicalAdvice", "医嘱", true, 300, false, "", "", 10));
        list.add(createField("prescription", "处方", true, 200, false, "", "", 11));
        list.add(createField("notes", "病历备注", true, 500, false, "", "", 12));
        return list;
    }

    private MedicalRecordAIField createField(String key, String name, boolean enabled, int maxLen, boolean required,
                                              String rule, String hint, int sort) {
        MedicalRecordAIField f = new MedicalRecordAIField();
        f.setFieldKey(key);
        f.setFieldName(name);
        f.setIsEnabled(enabled);
        f.setMaxLength(maxLen);
        f.setIsRequired(required);
        f.setValidationRule(rule);
        f.setValidationHint(hint);
        f.setSortOrder(sort);
        return f;
    }

    /**
     * AI 输出校验异常
     */
    public static class AIValidationException extends RuntimeException {
        private final List<String> errors;

        public AIValidationException(List<String> errors) {
            super("AI 输出未通过安全校验");
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
