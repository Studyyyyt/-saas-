-- V55: 将 ai_webhook 数据迁移到 ai_agent_config
-- 第一阶段：数据层统一

-- 将 ai_webhook 中已有的配置迁移到 ai_agent_config
-- account_id = 0 表示系统默认配置（供所有用户回退使用）
INSERT IGNORE INTO ai_agent_config (
    account_id, agent_key, name, icon, endpoint_url, auth_token,
    auth_type, response_type, ui_mode, chips, sort_order, created_at, updated_at
)
SELECT
    0 as account_id,
    agent_key,
    agent_name as name,
    '🤖' as icon,
    endpoint_url,
    auth_token,
    'bearer' as auth_type,
    'sse' as response_type,
    'chat' as ui_mode,
    CASE
        WHEN chip_text IS NOT NULL AND chip_text != '' THEN JSON_ARRAY(chip_text)
        ELSE NULL
    END as chips,
    sort_order,
    created_at,
    updated_at
FROM ai_webhook;
