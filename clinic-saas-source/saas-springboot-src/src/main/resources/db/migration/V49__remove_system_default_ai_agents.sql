-- 移除系统预设的 AI Agent 配置
-- 原因：首页助手应完全由用户自行添加和管理，不应存在不可删除的系统默认值
DELETE FROM ai_agent_config WHERE is_system_default = 1;
