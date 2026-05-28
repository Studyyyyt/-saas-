CREATE TABLE IF NOT EXISTS portal_access_token (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    token VARCHAR(128) NOT NULL COMMENT '访问令牌',
    token_type VARCHAR(64) NOT NULL COMMENT '令牌类型',
    subject_id BIGINT DEFAULT NULL COMMENT '主体ID，如patient/account',
    payload VARCHAR(1000) DEFAULT NULL COMMENT '附加数据',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    consumed_at DATETIME DEFAULT NULL COMMENT '消费时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_portal_access_token (token),
    KEY idx_portal_access_token_type (token_type),
    KEY idx_portal_access_token_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门户访问令牌';
