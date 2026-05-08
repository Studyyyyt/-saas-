CREATE TABLE IF NOT EXISTS ai_model_provider (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(64) NOT NULL COMMENT '供应商名称，如 OpenAI、Azure',
    base_url VARCHAR(256) NOT NULL COMMENT 'API 基础地址',
    api_key VARCHAR(512) NOT NULL COMMENT 'API 密钥',
    model_name VARCHAR(64) NOT NULL COMMENT '模型名称',
    reasoning_effort VARCHAR(16) DEFAULT 'medium' COMMENT '推理力度：low/medium/high',
    max_output_tokens INT DEFAULT 3000 COMMENT '最大输出 token 数',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型供应商配置表';
