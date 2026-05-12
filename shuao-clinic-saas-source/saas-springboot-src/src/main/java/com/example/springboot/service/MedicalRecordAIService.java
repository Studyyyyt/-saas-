package com.example.springboot.service;

import com.example.springboot.entity.*;
import com.example.springboot.mapper.AiFewShotExampleMapper;
import com.example.springboot.mapper.AiPromptTemplateMapper;
import com.example.springboot.mapper.MedicalRecordAIFieldMapper;
import com.example.springboot.mapper.TreatmentSceneMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 病历 AI 扩写服务
 */
@Service
public class MedicalRecordAIService {

    private final AiPromptTemplateMapper promptMapper;
    private final AiFewShotExampleMapper fewShotMapper;
    private final MedicalRecordAIFieldMapper fieldMapper;
    private final TreatmentSceneMapper sceneMapper;
    private final AiModelProviderService modelProviderService;
    private final ObjectMapper objectMapper;

    public MedicalRecordAIService(AiPromptTemplateMapper promptMapper,
                                  AiFewShotExampleMapper fewShotMapper,
                                  MedicalRecordAIFieldMapper fieldMapper,
                                  TreatmentSceneMapper sceneMapper,
                                  AiModelProviderService modelProviderService,
                                  ObjectMapper objectMapper) {
        this.promptMapper = promptMapper;
        this.fewShotMapper = fewShotMapper;
        this.fieldMapper = fieldMapper;
        this.sceneMapper = sceneMapper;
        this.modelProviderService = modelProviderService;
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
        if (configMap == null) {
            throw new IllegalArgumentException("配置不能为空");
        }

        // 1. 保存/更新提示词模板
        AiPromptTemplate template = promptMapper.selectByScene("medical_expand");
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

        // 2. 保存字段配置
        List<MedicalRecordAIField> fields = dto.getFields();
        if (fields != null) {
            for (MedicalRecordAIField field : fields) {
                fieldMapper.upsert(field);
            }
        }

        // 3. 保存 Few-shot 示例（先删后插）
        Long templateId = template.getId();
        if (templateId != null) {
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
    public Map<String, String> expand(MedicalRecordExpandDTO dto) {
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
     */
    public Map<String, String> expand(TreatmentSceneExpandRequest dto) {
        Map<String, String> inputFields = dto.getFields();
        if (inputFields == null || inputFields.isEmpty()) {
            throw new IllegalArgumentException("输入字段不能为空");
        }

        // 1. 获取配置
        AiPromptTemplate template = promptMapper.selectByScene("medical_expand");
        if (template == null) {
            template = new AiPromptTemplate();
            template.setSystemPrompt(getDefaultSystemPrompt());
        }
        List<MedicalRecordAIField> fields = fieldMapper.selectAllEnabled();
        if (fields == null || fields.isEmpty()) {
            fields = getDefaultFields();
        }

        // 读取 extraConfig 中的空字段策略
        String emptyFieldStrategy = "leave";
        if (StringUtils.hasText(template.getExtraConfig())) {
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

        // 3. 读取 Few-shot 示例
        List<AiFewShotExample> fewShots = null;
        if (template != null && template.getId() != null) {
            fewShots = fewShotMapper.selectByTemplateId(template.getId());
        }

        // 4. 渲染提示词（传入启用的字段列表、空字段策略和 Few-shot 示例）
        String systemPrompt = renderPrompt(template, inputFields, fields, emptyFieldStrategy, constraint, fewShots);

        // 4. 调用大模型（失败时回退到 mock；测试模式直接 mock）
        String aiResponse;
        if (Boolean.TRUE.equals(dto.getTestMode())) {
            aiResponse = getMockResponse(inputFields, fields, emptyFieldStrategy, constraint);
        } else {
            try {
                aiResponse = callModel(systemPrompt, inputFields, template, fields, emptyFieldStrategy);
            } catch (Exception e) {
                aiResponse = getMockResponse(inputFields, fields, emptyFieldStrategy, constraint);
            }
        }

        // 5. 解析 JSON
        Map<String, String> result = parseResponse(aiResponse);

        // 5.5 过滤结果：只保留启用的字段（防止 AI 返回未启用的字段内容）
        Set<String> enabledKeys = new HashSet<>();
        for (MedicalRecordAIField field : fields) {
            enabledKeys.add(field.getFieldKey());
        }
        result.keySet().retainAll(enabledKeys);

        // 5.6 默认值覆盖：医生未填写且配置了默认值的字段，强制用默认值覆盖 AI 生成结果
        // 原因：AI 可能无视 prompt 中的默认值指令，自行根据上下文生成内容
        for (MedicalRecordAIField field : fields) {
            String inputValue = inputFields.get(field.getFieldKey());
            if (!StringUtils.hasText(inputValue) && StringUtils.hasText(field.getDefaultValue())) {
                result.put(field.getFieldKey(), field.getDefaultValue());
            }
        }

        // 5.7 补充缺失的启用字段（AI 可能漏掉某些字段）
        for (MedicalRecordAIField field : fields) {
            if (result.containsKey(field.getFieldKey())) continue;
            if (StringUtils.hasText(field.getDefaultValue())) {
                result.put(field.getFieldKey(), field.getDefaultValue());
            } else {
                switch (emptyFieldStrategy) {
                    case "leave":
                        result.put(field.getFieldKey(), "");
                        break;
                    case "prompt":
                        result.put(field.getFieldKey(), "请医生手动填写");
                        break;
                    case "generate":
                    default:
                        result.put(field.getFieldKey(), "");
                        break;
                }
            }
        }

        // 6. 安全校验
        List<String> errors = validateOutput(result, fields, template, constraint, emptyFieldStrategy);
        if (!errors.isEmpty()) {
            throw new AIValidationException(errors);
        }

        return result;
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

    private SceneConstraint buildSceneConstraint(Long sceneId, List<String> selectedOperations) {
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

    private String callModel(String systemPrompt, Map<String, String> fields, AiPromptTemplate template,
                             List<MedicalRecordAIField> enabledFields, String emptyFieldStrategy) {
        AiModelProvider provider = modelProviderService.getActiveProvider();
        if (provider == null) {
            // 无模型配置时返回模拟数据（开发测试用）
            return getMockResponse(fields, enabledFields, emptyFieldStrategy, null);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // 构建 OpenAI-compatible 请求体
            Map<String, Object> body = new HashMap<>();
            body.put("model", provider.getModelName());
            body.put("temperature", template.getTemperature() != null ? template.getTemperature() : 0.2);
            body.put("max_tokens", template.getMaxTokens() != null ? template.getMaxTokens() : 2000);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            // OpenAI 要求使用 json_object 时 prompt 中必须包含 "json" 一词
            userMsg.put("content", formatFields(fields) + "\n\n请严格按上述 JSON 格式返回结果。");
            messages.add(userMsg);

            body.put("messages", messages);
            // 为兼容 DeepSeek 等国产模型，不发送 response_format，仅靠 prompt 约束 JSON 输出

            String jsonBody = objectMapper.writeValueAsString(body);
            String url = resolveChatCompletionsUrl(provider.getBaseUrl());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + provider.getApiKey())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> respMap = objectMapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.get("content") != null) {
                        return String.valueOf(message.get("content"));
                    }
                }
                throw new RuntimeException("AI 返回格式异常");
            } else {
                throw new RuntimeException("AI 调用失败：HTTP " + response.statusCode() + " " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("AI 调用异常：" + e.getMessage(), e);
        }
    }

    private Map<String, String> parseResponse(String aiResponse) {
        // 第1步：尝试直接解析
        Map<String, String> parsed = tryParseJson(aiResponse);
        if (parsed != null) return parsed;

        // 第2步：清理并提取 JSON（处理 Markdown 代码块、前后说明文字、多余逗号等）
        String cleaned = cleanAndExtractJson(aiResponse);
        parsed = tryParseJson(cleaned);
        if (parsed != null) return parsed;

        // 第3步：尝试从最外层 {} 中提取
        String extracted = extractOutermostJson(aiResponse);
        if (extracted != null && !extracted.equals(cleaned)) {
            parsed = tryParseJson(extracted);
            if (parsed != null) return parsed;
        }

        throw new RuntimeException("AI 输出不是有效的 JSON，已尝试自动修复但未成功。原始输出：" + aiResponse);
    }

    /**
     * 尝试将字符串解析为 Map<String, String>
     */
    private Map<String, String> tryParseJson(String text) {
        if (!StringUtils.hasText(text)) return null;
        try {
            Map<String, Object> raw = objectMapper.readValue(text, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                if (entry.getValue() instanceof String) {
                    result.put(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() != null) {
                    result.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
                } else {
                    result.put(entry.getKey(), "");
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 清理常见 JSON 格式问题：去除 Markdown 代码块标记、前后说明文字、修复多余逗号
     */
    private String cleanAndExtractJson(String text) {
        if (!StringUtils.hasText(text)) return "";
        String cleaned = text.trim();

        // 去除 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        // 去除首尾可能的说明文字，只保留 {} 包裹的内容
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        // 修复对象和数组中的多余逗号（如 "key": "value",\n} 或 [1,2,]）
        cleaned = fixTrailingCommas(cleaned);

        return cleaned;
    }

    /**
     * 修复 JSON 中末尾多余逗号（对象和数组）
     */
    private String fixTrailingCommas(String json) {
        if (!StringUtils.hasText(json)) return json;
        // 匹配对象中 "...",\s*} 的模式，将 ,} 替换为 }
        String result = json.replaceAll(",\\s*}", "}");
        // 匹配数组中 ,\s*] 的模式，将 ,] 替换为 ]
        result = result.replaceAll(",\\s*\\]", "]");
        return result;
    }

    /**
     * 从文本中提取最外层匹配的 {} 内容（处理嵌套情况）
     */
    private String extractOutermostJson(String text) {
        if (!StringUtils.hasText(text)) return null;
        int start = text.indexOf('{');
        if (start < 0) return null;

        int depth = 0;
        int end = -1;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    end = i;
                    break;
                }
            }
        }

        if (end > start) {
            String extracted = text.substring(start, end + 1);
            return fixTrailingCommas(extracted);
        }
        return null;
    }

    private boolean isPlaceholderValue(String value, String emptyFieldStrategy) {
        if (!StringUtils.hasText(value)) return true;
        return "prompt".equals(emptyFieldStrategy) && "请医生手动填写".equals(value);
    }

    private List<String> validateOutput(Map<String, String> output, List<MedicalRecordAIField> fields,
                                        AiPromptTemplate template, SceneConstraint constraint,
                                        String emptyFieldStrategy) {
        List<String> errors = new ArrayList<>();

        // 读取安全策略
        boolean forbidAssertion = true;
        String sensitiveWords = "确诊,绝对,保证,100%,肯定";
        boolean checkDiagnosisTone = true;
        boolean checkChiefComplaintLength = true;
        boolean checkHistoryTime = true;

        if (StringUtils.hasText(template.getExtraConfig())) {
            try {
                Map<String, Object> extra = objectMapper.readValue(template.getExtraConfig(), Map.class);
                Object fa = extra.get("forbidAssertion");
                forbidAssertion = fa == null || Boolean.parseBoolean(String.valueOf(fa));
                Object sw = extra.get("sensitiveWords");
                sensitiveWords = sw != null ? String.valueOf(sw) : "确诊,绝对,保证,100%,肯定";
                Object cdt = extra.get("checkDiagnosisTone");
                checkDiagnosisTone = cdt == null || Boolean.parseBoolean(String.valueOf(cdt));
                Object cccl = extra.get("checkChiefComplaintLength");
                checkChiefComplaintLength = cccl == null || Boolean.parseBoolean(String.valueOf(cccl));
                Object cht = extra.get("checkHistoryTime");
                checkHistoryTime = cht == null || Boolean.parseBoolean(String.valueOf(cht));
            } catch (Exception ignored) {}
        }

        // 字段级校验（只校验启用的字段）
        for (MedicalRecordAIField field : fields) {
            String value = output.get(field.getFieldKey());

            // 空字段策略为 leave 时，未填写字段允许为空，不校验必填
            boolean isLeave = "leave".equals(emptyFieldStrategy);
            if (!isLeave && Boolean.TRUE.equals(field.getIsRequired()) && !StringUtils.hasText(value)) {
                errors.add(field.getFieldName() + "不能为空");
            }

            // 跳过占位符值的长度校验
            if (field.getMaxLength() != null && StringUtils.hasText(value)
                    && !isPlaceholderValue(value, emptyFieldStrategy)
                    && value.length() > field.getMaxLength()) {
                errors.add(field.getFieldName() + "超过最大长度" + field.getMaxLength() + "字");
            }

            // 字段级关键词校验（跳过占位符值）
            if (StringUtils.hasText(field.getValidationRule()) && StringUtils.hasText(value)
                    && !isPlaceholderValue(value, emptyFieldStrategy)) {
                String[] keywords = field.getValidationRule().split(",");
                for (String kw : keywords) {
                    String trimmed = kw.trim();
                    if (trimmed.isEmpty()) continue;
                    if (!value.contains(trimmed)) {
                        errors.add(field.getValidationHint() != null ? field.getValidationHint() : field.getFieldName() + "必须包含：" + trimmed);
                        break;
                    }
                }
            }
        }

        // 诊断语气校验（只在诊断字段启用且值非占位符时校验）
        if (checkDiagnosisTone && fields.stream().anyMatch(f -> "diagnosis".equals(f.getFieldKey()))) {
            String diagnosis = output.get("diagnosis");
            if (StringUtils.hasText(diagnosis) && !isPlaceholderValue(diagnosis, emptyFieldStrategy)
                    && !diagnosis.matches(".*(考虑|疑似|待排|鉴别诊断|可能).*")) {
                errors.add("诊断字段必须使用建议性语气（考虑/疑似/待排）");
            }
        }

        // 主诉长度校验（只在主诉字段启用且值非占位符时校验）
        if (checkChiefComplaintLength) {
            String chiefComplaint = output.get("chiefComplaint");
            MedicalRecordAIField chiefField = fields.stream()
                    .filter(f -> "chiefComplaint".equals(f.getFieldKey()))
                    .findFirst().orElse(null);
            if (chiefField != null && chiefField.getMaxLength() != null
                    && StringUtils.hasText(chiefComplaint) && !isPlaceholderValue(chiefComplaint, emptyFieldStrategy)
                    && chiefComplaint.length() > chiefField.getMaxLength()) {
                errors.add("主诉超过最大长度" + chiefField.getMaxLength() + "字");
            }
        }

        // 现病史时间描述校验（只在现病史字段启用且值非占位符时校验）
        if (checkHistoryTime && fields.stream().anyMatch(f -> "historyOfPresentIllness".equals(f.getFieldKey()))) {
            String history = output.get("historyOfPresentIllness");
            if (StringUtils.hasText(history) && !isPlaceholderValue(history, emptyFieldStrategy)
                    && !history.matches(".*(天|周|月|年|小时|分钟|前|以来).*")) {
                errors.add("现病史必须包含时间描述");
            }
        }

        // 敏感词校验
        if (forbidAssertion && StringUtils.hasText(sensitiveWords)) {
            for (String word : sensitiveWords.split(",")) {
                String trimmed = word.trim();
                if (trimmed.isEmpty()) continue;
                for (String value : output.values()) {
                    if (value != null && value.contains(trimmed)) {
                        errors.add("输出包含敏感词：" + trimmed);
                        break;
                    }
                }
            }
        }

        // 场景约束校验（禁止关键词/必须包含关键词）
        if (constraint != null) {
            for (String forbidden : constraint.forbiddenKeywords) {
                for (String value : output.values()) {
                    if (value != null && value.contains(forbidden)) {
                        errors.add("生成内容包含场景禁止词汇：" + forbidden);
                        break;
                    }
                }
            }
            for (String required : constraint.requiredKeywords) {
                boolean found = false;
                for (String value : output.values()) {
                    if (value != null && value.contains(required)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    errors.add("生成内容未包含场景要求词汇：" + required);
                }
            }
        }

        return errors;
    }

    private String getMockResponse(Map<String, String> inputFields, List<MedicalRecordAIField> enabledFields,
                                   String emptyFieldStrategy, SceneConstraint constraint) {
        if (inputFields == null) inputFields = new HashMap<>();
        if (enabledFields == null) enabledFields = getDefaultFields();
        if (emptyFieldStrategy == null) emptyFieldStrategy = "generate";

        String chiefComplaint = inputFields.getOrDefault("chiefComplaint", "牙痛");
        String history = inputFields.getOrDefault("historyOfPresentIllness", "");

        String mockChief = chiefComplaint.contains("天") || chiefComplaint.contains("痛")
                ? chiefComplaint.replace("牙痛", "右下后牙自发痛") : chiefComplaint;

        String mockHistory;
        if (StringUtils.hasText(history)) {
            mockHistory = "患者3天前无明显诱因出现" + history + "，疼痛呈阵发性发作，冷热刺激加重，夜间明显，伴同侧头面部放射痛。自行口服止痛药物后症状无明显缓解，为求进一步诊治来我院就诊。";
        } else {
            mockHistory = "患者3天前无明显诱因出现右下后牙自发性疼痛，呈阵发性发作，每次持续约10-15分钟，冷热刺激可加重疼痛，夜间疼痛明显，伴同侧头面部放射痛。自行口服止痛药物（具体不详）后症状无明显缓解，为求进一步诊治来我院就诊。";
        }

        // 根据场景约束调整 mock 数据
        String mockTreatment;
        String mockTreatmentPlan;
        String mockAuxiliary;
        if (constraint != null && "根管治疗".equals(constraint.sceneName)) {
            if (constraint.operations.contains("开髓引流")) {
                mockTreatment = "局麻下46牙开髓，揭顶，拔髓，探及3个根管口，封入无砷失活剂，氧化锌暂封。";
                mockTreatmentPlan = "择期行根管疏通及预备。建议术前完善根尖片检查。";
                mockAuxiliary = "46牙根尖片示：龋坏达牙本质深层，近髓，根尖周未见明显异常。";
            } else if (constraint.operations.contains("根管预备")) {
                mockTreatment = "去除暂封，探查3个根管，疏通根管，测工作长度，镍钛器械预备至F2，次氯酸钠溶液冲洗，封入氢氧化钙，氧化锌暂封。";
                mockTreatmentPlan = "择期行根管充填。建议充填前再次确认根管预备到位。";
                mockAuxiliary = "46牙根尖片示：根管预备到位，根尖周未见明显异常。";
            } else if (constraint.operations.contains("根管充填")) {
                mockTreatment = "去除暂封，试尖合适，牙胶尖+AH Plus糊剂侧压充填，充填严密，调颌，抛光。";
                mockTreatmentPlan = "择期行永久修复（全冠修复）。建议观察1-2周无症状后复诊。";
                mockAuxiliary = "46牙根充片示：根管充填严密，适充，根尖周未见明显异常。";
            } else {
                mockTreatment = "本次就诊向患者交代病情及治疗方案，患者知情同意并签署知情同意书。拟于下次就诊开始根管治疗。";
                mockTreatmentPlan = "1. 根管治疗；2. 术后行冠修复。建议术前完善影像学检查。";
                mockAuxiliary = "建议完善根尖片或CBCT检查，以明确龋坏范围及根尖周情况。";
            }
        } else if (constraint != null && "简单拔牙".equals(constraint.sceneName)) {
            if (constraint.operations.contains("拔牙操作")) {
                mockTreatment = "局麻下拔除48牙，牙槽窝完整，出血少，咬棉球止血。";
                mockTreatmentPlan = "无需进一步治疗，嘱患者按医嘱护理。";
                mockAuxiliary = "48牙根尖片示：牙根完整拔除，牙槽窝无残留。";
            } else {
                mockTreatment = "检查48牙，向患者交代拔牙方案及风险，患者知情同意。";
                mockTreatmentPlan = "择期拔除48牙。建议术前完善血常规检查。";
                mockAuxiliary = "48牙根尖片示：近中阻生，双根，根尖周未见明显异常。";
            }
        } else {
            mockTreatment = "本次就诊向患者交代病情及治疗方案，患者知情同意并签署知情同意书。拟于下次就诊开始根管治疗。";
            mockTreatmentPlan = "1. 根管治疗；2. 术后行冠修复；3. 必要时拔除患牙。建议术前完善影像学检查。";
            mockAuxiliary = "建议完善根尖片或CBCT检查，以明确龋坏范围及根尖周情况。";
        }

        // 构建完整 mock 值
        Map<String, String> fullMock = new LinkedHashMap<>();
        fullMock.put("chiefComplaint", mockChief);
        fullMock.put("historyOfPresentIllness", mockHistory);
        fullMock.put("pastHistory", "否认全身系统性疾病史，否认传染病史，否认手术外伤史，否认药物及食物过敏史。");
        fullMock.put("generalCondition", "精神可，饮食睡眠尚可，大小便正常。");
        fullMock.put("examinationFindings", "面部对称，张口度正常。右下后牙对应牙位牙龈轻度红肿，叩诊（+），冷热测敏感，探诊可见深龋洞，松动度（-）。");
        fullMock.put("auxiliaryExamination", mockAuxiliary);
        fullMock.put("diagnosis", "考虑：1. 急性牙髓炎（右下后牙）；2. 深龋（右下后牙）");
        fullMock.put("treatmentPlan", mockTreatmentPlan);
        fullMock.put("treatment", mockTreatment);
        fullMock.put("medicalAdvice", "1. 避免患侧咀嚼硬物；2. 注意口腔卫生，饭后漱口；3. 如疼痛加重可口服止痛药；4. 按预约时间复诊。");
        fullMock.put("prescription", "暂无");
        fullMock.put("notes", "患者对治疗方案表示理解，配合度良好。");

        // 根据启用字段和空字段策略过滤结果
        Map<String, String> result = new LinkedHashMap<>();
        for (MedicalRecordAIField field : enabledFields) {
            String key = field.getFieldKey();
            String inputValue = inputFields.get(key);
            if (StringUtils.hasText(inputValue)) {
                // 医生已填写：返回 mock 扩展内容
                result.put(key, fullMock.getOrDefault(key, inputValue));
            } else {
                // 医生未填写：优先使用字段默认值，否则按策略处理
                if (StringUtils.hasText(field.getDefaultValue())) {
                    result.put(key, field.getDefaultValue());
                } else {
                    switch (emptyFieldStrategy) {
                        case "leave":
                            result.put(key, "");
                            break;
                        case "prompt":
                            result.put(key, "请医生手动填写");
                            break;
                        case "generate":
                        default:
                            result.put(key, fullMock.getOrDefault(key, ""));
                            break;
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, String> entry : result.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": \"").append(escapeJson(entry.getValue())).append("\"");
            if (i < result.size() - 1) sb.append(",");
            sb.append("\n");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) return "";
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String resolveChatCompletionsUrl(String baseUrl) {
        String normalized = trimTrailingSlash(baseUrl);
        // 自动补全 /v1 路径：若末尾没有版本号路径（如 /v1 /v2），则追加 /v1
        if (!normalized.matches(".*/v\\d+$")) {
            normalized = normalized + "/v1";
        }
        return normalized + "/chat/completions";
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
