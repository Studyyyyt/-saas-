-- AI Agent 配置表增加外部工作流端点字段
ALTER TABLE ai_agent_config
    ADD COLUMN endpoint_url VARCHAR(500) NULL COMMENT '外部工作流端点地址',
    ADD COLUMN auth_type VARCHAR(50) NULL COMMENT '认证类型：bearer / basic / api_key / 自定义header名',
    ADD COLUMN auth_token VARCHAR(500) NULL COMMENT '认证令牌',
    ADD COLUMN request_template TEXT NULL COMMENT '请求体模板，支持 {{变量}} 替换',
    ADD COLUMN response_type VARCHAR(20) NULL DEFAULT 'json' COMMENT '响应类型：sse（流式）/ json（一次性）',
    ADD COLUMN timeout_seconds INT NULL DEFAULT 60 COMMENT '超时秒数';

-- 删除不再使用的字段（保留数据但不再使用，或根据实际需要删除）
-- 如果确定不再需要 system_prompt 和 enabled_tools，可以取消下面注释执行删除
-- ALTER TABLE ai_agent_config DROP COLUMN system_prompt;
-- ALTER TABLE ai_agent_config DROP COLUMN enabled_tools;
