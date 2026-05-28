-- 病历 AI 扩写相关表

-- 提示词模板表
CREATE TABLE IF NOT EXISTS ai_prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene VARCHAR(50) NOT NULL COMMENT '场景标识',
    name VARCHAR(100) DEFAULT NULL COMMENT '模板名称',
    system_prompt TEXT DEFAULT NULL COMMENT '系统提示词',
    temperature DECIMAL(3,2) DEFAULT 0.20 COMMENT '温度参数',
    max_tokens INT DEFAULT 2000 COMMENT '最大输出Token数',
    response_format VARCHAR(20) DEFAULT 'json' COMMENT '响应格式',
    json_schema TEXT DEFAULT NULL COMMENT 'JSON Schema约束',
    extra_config JSON DEFAULT NULL COMMENT '扩展配置（空字段策略等）',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_scene (scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 提示词模板表';

-- Few-shot 示例表
CREATE TABLE IF NOT EXISTS ai_few_shot_example (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '关联模板ID',
    input_content TEXT DEFAULT NULL COMMENT '输入示例',
    output_content TEXT DEFAULT NULL COMMENT '输出示例',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Few-shot 示例表';

-- 病历扩写字段规则表
CREATE TABLE IF NOT EXISTS medical_record_ai_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    field_key VARCHAR(50) NOT NULL COMMENT '字段标识',
    field_name VARCHAR(50) DEFAULT NULL COMMENT '字段中文名',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用扩写',
    max_length INT DEFAULT NULL COMMENT '最大长度',
    is_required TINYINT(1) DEFAULT 0 COMMENT '是否必填',
    validation_rule VARCHAR(255) DEFAULT NULL COMMENT '校验规则正则',
    validation_hint VARCHAR(255) DEFAULT NULL COMMENT '校验失败提示',
    sort_order INT DEFAULT 0 COMMENT '排序',
    UNIQUE KEY uk_field_key (field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历扩写字段规则表';

-- 插入默认提示词模板（病历扩写场景）
INSERT INTO ai_prompt_template (scene, name, system_prompt, temperature, max_tokens, response_format, extra_config, is_active, version) VALUES
('medical_expand', '病历扩写模板', '你是一位资深口腔全科医生助理，擅长将简要病历记录扩写为规范的专业病历。

【任务】
根据医生填写的简要信息，扩写为符合口腔医学规范的病历内容。

【知识库参考】（仅参考格式和术语，不编造未提及的病情）
{kb_content}

【输入信息】
{input_fields}

【输出格式要求】
严格按以下 JSON 格式返回，不要包含任何其他内容：
{
  "chiefComplaint": "主诉，≤30字，格式：部位+症状+时间",
  "historyOfPresentIllness": "现病史，必须包含起病时间、主要症状、演变过程",
  "examinationFindings": "检查所见",
  "diagnosis": "诊断，必须用建议性语气（考虑/疑似/待排）",
  "treatmentPlan": "治疗计划"
}

【绝对禁止】
1. 禁止编造患者未提及的症状、检查结果
2. 禁止输出确诊性断言
3. 禁止输出具体药物剂量
4. 禁止输出"建议到上级医院"等推诿用语

【语气要求】
专业、客观、严谨，使用标准口腔医学术语。', 0.20, 2000, 'json', '{"emptyFieldStrategy":"leave","forbidAssertion":true,"sensitiveWords":"确诊,绝对,保证,100%,肯定","checkDiagnosisTone":true,"checkChiefComplaintLength":true,"checkHistoryTime":true}', 1, 1)
ON DUPLICATE KEY UPDATE
system_prompt = VALUES(system_prompt),
extra_config = VALUES(extra_config);

-- 插入默认字段规则
INSERT INTO medical_record_ai_field (field_key, field_name, is_enabled, max_length, is_required, validation_rule, validation_hint, sort_order) VALUES
('chiefComplaint', '主诉', 1, 30, 1, '', '主诉必须包含部位+症状+时间', 1),
('historyOfPresentIllness', '现病史', 1, 500, 1, '', '现病史必须包含时间描述', 2),
('examinationFindings', '检查所见', 1, 500, 0, '', '', 3),
('diagnosis', '诊断', 1, 100, 1, '', '诊断必须用建议性语气', 4),
('treatmentPlan', '治疗计划', 1, 300, 0, '', '', 5)
ON DUPLICATE KEY UPDATE
field_name = VALUES(field_name),
is_enabled = VALUES(is_enabled),
max_length = VALUES(max_length),
is_required = VALUES(is_required),
validation_hint = VALUES(validation_hint),
sort_order = VALUES(sort_order);

-- 插入默认 Few-shot 示例
SET @template_id = (SELECT id FROM ai_prompt_template WHERE scene = 'medical_expand' LIMIT 1);
DELETE FROM ai_few_shot_example WHERE template_id = @template_id;
INSERT INTO ai_few_shot_example (template_id, input_content, output_content, sort_order) VALUES
(@template_id, '主诉：牙痛3天\n现病史：3天前开始牙痛，吃了止痛药没好', '主诉：右下后牙自发痛3天\n现病史：患者3天前无明显诱因出现右下后牙自发性疼痛，呈阵发性发作，每次持续约10-15分钟，冷热刺激可加重疼痛，夜间疼痛明显，伴同侧头面部放射痛。自行口服止痛药物（具体不详）后症状无明显缓解，为求进一步诊治来我院就诊。', 0);
