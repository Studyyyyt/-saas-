-- 为 ai_agent_config 增加首页显示控制字段
-- 让自定义 Agent 也能独立控制是否在首页 AI 面板中展示

ALTER TABLE ai_agent_config
    ADD COLUMN is_visible_on_home TINYINT(1) DEFAULT 1 COMMENT '是否在首页 AI 面板显示，1=显示，0=隐藏';
