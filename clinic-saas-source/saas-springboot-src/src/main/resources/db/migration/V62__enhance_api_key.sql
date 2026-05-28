-- 扩展 api_key 表，支持过期时间、使用统计和描述
ALTER TABLE api_key
    ADD COLUMN expires_at TIMESTAMP NULL COMMENT '过期时间',
    ADD COLUMN last_used_at TIMESTAMP NULL COMMENT '最后使用时间',
    ADD COLUMN usage_count INT DEFAULT 0 COMMENT '使用次数',
    ADD COLUMN description VARCHAR(255) DEFAULT '' COMMENT '描述';

-- 移除 clinic_id 的唯一约束，允许多个 Key 共存
ALTER TABLE api_key DROP INDEX uk_clinic;

-- 添加查询索引
ALTER TABLE api_key ADD INDEX idx_clinic_id (clinic_id);
ALTER TABLE api_key ADD INDEX idx_is_enabled (is_enabled);
