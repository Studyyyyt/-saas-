ALTER TABLE patient_followup ADD COLUMN followup_project VARCHAR(100) NULL COMMENT '回访项目' AFTER followup_type;
