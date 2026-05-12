-- 病历扩写字段规则表增加默认值字段
ALTER TABLE medical_record_ai_field ADD COLUMN default_value VARCHAR(500) DEFAULT NULL COMMENT '字段默认值，医生未填写时直接返回此值';
