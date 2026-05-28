-- AI 系统功能与 Agent 绑定映射表
-- 支持将系统功能（如病历扩写）动态绑定到任意 AgentKey，并控制是否在页面显示入口
CREATE TABLE IF NOT EXISTS ai_function_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT DEFAULT NULL COMMENT '所属用户ID，NULL表示系统默认',
    function_code VARCHAR(64) NOT NULL COMMENT '系统功能编码，如 medical-record-expand',
    function_name VARCHAR(100) NOT NULL COMMENT '功能名称，如 病历扩写',
    agent_key VARCHAR(64) DEFAULT NULL COMMENT '绑定的 AgentKey，NULL表示未绑定',
    is_visible TINYINT(1) DEFAULT 1 COMMENT '是否在页面显示入口 0-隐藏 1-显示',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_account_function (account_id, function_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI系统功能与Agent绑定映射表';

-- 插入系统预置功能列表（系统默认配置，account_id 为 NULL）
-- 这些是所有系统层面可用的 AI 功能入口
INSERT INTO ai_function_mapping (account_id, function_code, function_name, agent_key, is_visible, sort_order) VALUES
(NULL, 'medical-record-expand', '病历扩写', 'medical-expand', 1, 1),
(NULL, 'consultation-assist', '咨询辅助', 'consultation-assist', 1, 2),
(NULL, 'consultation-dashboard', '咨询分析', 'consultation-dashboard', 1, 3),
(NULL, 'appointment-assist', '预约辅助', 'appointment-assist', 1, 4),
(NULL, 'followup-assist', '回访辅助', 'followup-assist', 1, 5),
(NULL, 'treatment-assist', '治疗辅助', 'treatment-assist', 1, 6),
(NULL, 'treatment-record-assist', '治疗记录辅助', 'treatment-record-assist', 1, 7),
(NULL, 'financial-analysis', '财务分析', 'financial-analysis', 1, 8),
(NULL, 'monthly-bill-analysis', '月度账单分析', 'monthly-bill-analysis', 1, 9),
(NULL, 'lab-statistics-analysis', '加工统计', 'lab-statistics-analysis', 1, 10),
(NULL, 'material-category-assist', '耗材分类辅助', 'material-category-assist', 1, 11),
(NULL, 'material-inventory-assist', '库存辅助', 'material-inventory-assist', 1, 12),
(NULL, 'material-purchase-assist', '采购辅助', 'material-purchase-assist', 1, 13),
(NULL, 'material-statistics-analysis', '耗材统计', 'material-statistics-analysis', 1, 14)
ON DUPLICATE KEY UPDATE
function_name = VALUES(function_name),
agent_key = VALUES(agent_key),
is_visible = VALUES(is_visible),
sort_order = VALUES(sort_order);
