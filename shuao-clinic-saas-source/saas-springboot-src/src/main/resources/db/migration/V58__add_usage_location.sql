-- 为 ai_agent_config 表增加用途位置标注字段，便于区分不同页面/功能的 AI Agent 配置
ALTER TABLE ai_agent_config ADD COLUMN usage_location VARCHAR(100) NULL COMMENT '用途位置标注，如新增病历页、咨询分析' AFTER ui_config_json;
