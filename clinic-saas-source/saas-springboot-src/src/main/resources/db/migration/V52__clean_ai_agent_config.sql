-- 删除 ai_agent_config 表中不再使用的冗余字段
ALTER TABLE ai_agent_config DROP COLUMN system_prompt;
ALTER TABLE ai_agent_config DROP COLUMN enabled_tools;
