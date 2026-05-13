-- AI 总览页面相关表

-- AI 功能配置主表
CREATE TABLE IF NOT EXISTS ai_function_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    function_key VARCHAR(50) NOT NULL COMMENT '功能标识',
    function_name VARCHAR(100) NOT NULL COMMENT '功能名称',
    page_path VARCHAR(100) DEFAULT NULL COMMENT '所属页面路径',
    icon VARCHAR(20) DEFAULT '🤖' COMMENT '图标（emoji）',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    model_name VARCHAR(50) DEFAULT NULL COMMENT '使用模型名称',
    prompt_template_id BIGINT DEFAULT NULL COMMENT '关联提示词模板ID',
    extra_config JSON DEFAULT NULL COMMENT '额外配置（各功能私有参数）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_function_key (function_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 功能配置主表';

-- AI 操作日志表（用于统计）
CREATE TABLE IF NOT EXISTS ai_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    function_key VARCHAR(50) DEFAULT NULL COMMENT '功能标识',
    account_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    input_snapshot JSON DEFAULT NULL COMMENT '输入数据快照',
    ai_output TEXT DEFAULT NULL COMMENT 'AI输出内容',
    is_adopted TINYINT(1) DEFAULT NULL COMMENT '医生是否采纳',
    token_used INT DEFAULT 0 COMMENT '消耗token数',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY idx_function_key_time (function_key, create_time),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 操作日志表';

-- AI 全局配置表（存储全局开关、调试模式等）
CREATE TABLE IF NOT EXISTS ai_global_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value VARCHAR(255) DEFAULT NULL COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 全局配置表';

-- 初始化 AI 功能配置数据
INSERT INTO ai_function_config (function_key, function_name, page_path, icon, is_enabled, model_name, sort_order) VALUES
('home-assistant', '首页 AI 助手', '首页', '🤖', 1, 'DeepSeek-V3', 1),
('medical-expand', '病历 AI 扩写', '病历编辑', '📝', 1, 'DeepSeek-V3', 2),
('patient-insight', '患者 AI 洞察', '患者列表', '🔍', 0, '-', 3),
('followup-generate', '智能随访生成', '随访管理', '📞', 0, '-', 4),
('business-analysis', '经营 AI 分析', '经营分析', '📊', 0, '-', 5)
ON DUPLICATE KEY UPDATE
function_name = VALUES(function_name),
page_path = VALUES(page_path),
icon = VALUES(icon),
sort_order = VALUES(sort_order);

-- 初始化 AI 全局配置
INSERT INTO ai_global_config (config_key, config_value, description) VALUES
('global_enabled', 'true', 'AI 功能总开关'),
('debug_mode', 'false', '调试模式开关')
ON DUPLICATE KEY UPDATE
config_value = VALUES(config_value);
