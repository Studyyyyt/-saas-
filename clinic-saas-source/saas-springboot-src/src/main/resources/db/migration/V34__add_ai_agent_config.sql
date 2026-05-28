-- AI Agent 配置表
CREATE TABLE IF NOT EXISTS ai_agent_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT DEFAULT NULL COMMENT '所属用户ID，NULL表示系统默认',
    agent_key VARCHAR(64) NOT NULL COMMENT 'Agent标识: default/finance/patient/schedule/自定义',
    name VARCHAR(32) NOT NULL COMMENT '显示名称',
    icon VARCHAR(8) DEFAULT '🤖' COMMENT '图标emoji',
    description VARCHAR(256) DEFAULT NULL COMMENT '描述',
    gradient VARCHAR(256) DEFAULT 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)' COMMENT '主题色CSS渐变',
    chips JSON DEFAULT NULL COMMENT '快捷指令JSON数组',
    system_prompt TEXT DEFAULT NULL COMMENT 'Agent专属System Prompt',
    enabled_tools JSON DEFAULT NULL COMMENT '该Agent可使用的工具列表',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_system_default TINYINT(1) DEFAULT 0 COMMENT '是否为系统预设Agent',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_account_agent (account_id, agent_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent配置表';

-- 先清空系统默认Agent，避免重复插入（account_id为NULL时唯一索引不生效）
DELETE FROM ai_agent_config WHERE is_system_default = 1;

-- 插入默认Agent配置
INSERT INTO ai_agent_config (account_id, agent_key, name, icon, description, gradient, chips, system_prompt, enabled_tools, sort_order, is_system_default) VALUES
(NULL, 'default', '智能助手', '🤖', '通用门诊查询与数据汇总', 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)',
 '["今日预约", "我的待办", "本月收入", "患者查询", "今日患者", "待收费"]',
 '你是口腔门诊的智能助手，可以查询患者、预约、收费、病历等数据，用中文简洁回答。',
 '["query_patients", "query_appointments", "query_finances", "query_medical_records", "query_treatments"]',
 0, 1),
(NULL, 'finance', '经营分析', '📊', '财务、收入与经营数据分析', 'linear-gradient(135deg, #d97706 0%, #f59e0b 100%)',
 '["本月收入", "近7天趋势", "待收费", "加工费", "耗材支出", "高价值客户"]',
 '你是口腔门诊的财务分析专家。专注于收入趋势、收费结构、支出分析、经营效率。用数据说话，给出可落地的建议。',
 '["query_finances", "query_treatments", "query_materials"]',
 1, 1),
(NULL, 'patient', '患者管理', '🏥', '患者档案、随访与病历查询', 'linear-gradient(135deg, #059669 0%, #10b981 100%)',
 '["患者查询", "待回访", "流失风险", "转介绍", "待写病历", "今日患者"]',
 '你是口腔门诊的患者管理专家。专注于患者档案、随访提醒、病历分析、患者满意度。关注患者全生命周期管理。',
 '["query_patients", "query_medical_records", "query_appointments", "query_treatments"]',
 2, 1),
(NULL, 'schedule', '预约调度', '📅', '预约排班、医生日程与调度', 'linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)',
 '["今日预约", "明日预约", "医生排班", "待接诊", "已取消", "预约趋势"]',
 '你是口腔门诊的预约调度专家。专注于预约管理、医生排班、资源分配、患者到诊率。帮助优化预约流程。',
 '["query_appointments", "query_treatments"]',
 3, 1);
