-- AI Agent 配置表扩展前端展示配置字段
ALTER TABLE ai_agent_config ADD COLUMN ui_mode VARCHAR(32) DEFAULT 'json' COMMENT '展示模式: json/chat/card/table';
ALTER TABLE ai_agent_config ADD COLUMN ui_config_json TEXT COMMENT '前端UI配置JSON';
