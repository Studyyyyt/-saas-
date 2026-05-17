-- 第四阶段：删除 ai_agent_config 表中已废弃的 is_system_default 字段
-- 系统默认配置逻辑已改为通过 account_id = 0 或 NULL 标识
ALTER TABLE ai_agent_config DROP COLUMN is_system_default;
