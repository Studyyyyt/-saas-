-- 第四阶段：删除已废弃的 ai_webhook 表
-- 该表数据已通过 V55 迁移到 ai_agent_config，相关代码已清理
DROP TABLE IF EXISTS ai_webhook;
