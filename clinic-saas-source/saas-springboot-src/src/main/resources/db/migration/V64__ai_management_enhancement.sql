-- V64: AI 管理增强
-- 1. ai_agent_config 新增快捷卡片预设消息字段
-- 2. 新增 AI 调用日志表
-- 3. 将未接入功能的系统默认显示状态关闭

-- ========== 1. Agent 配置新增预设消息字段 ==========
ALTER TABLE ai_agent_config
    ADD COLUMN preset_message VARCHAR(500) DEFAULT NULL COMMENT '快捷卡片点击后填入输入框的预设消息' AFTER chips;

-- ========== 2. AI 调用日志表 ==========
CREATE TABLE IF NOT EXISTS ai_call_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT DEFAULT NULL COMMENT '调用者用户ID',
    account_name VARCHAR(64) DEFAULT NULL COMMENT '调用者姓名',
    agent_key VARCHAR(64) NOT NULL COMMENT '使用的 AgentKey',
    agent_name VARCHAR(100) DEFAULT NULL COMMENT 'Agent 名称',
    session_id VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    request_message TEXT COMMENT '用户发送的消息内容（前500字）',
    response_status VARCHAR(20) DEFAULT NULL COMMENT '响应状态：success / error / timeout',
    response_content TEXT COMMENT 'AI 回复内容摘要（前500字）',
    error_msg VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    duration_ms INT DEFAULT 0 COMMENT '调用耗时（毫秒）',
    source VARCHAR(32) DEFAULT 'web' COMMENT '调用来源：web / page / api',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_agent (account_id, agent_key),
    INDEX idx_created_at (created_at),
    INDEX idx_status (response_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志表';

-- ========== 3. 未接入功能默认关闭显示 ==========
-- 先更新系统默认配置：未接入功能的 is_visible_on_page 和 is_visible_on_home 设为 0
UPDATE ai_function_mapping
SET is_visible_on_page = 0,
    is_visible_on_home = 0
WHERE account_id IS NULL
  AND function_code IN (
      'consultation-dashboard',
      'followup-assist',
      'treatment-assist',
      'treatment-record-assist',
      'financial-analysis',
      'monthly-bill-analysis',
      'lab-statistics-analysis',
      'material-category-assist',
      'material-inventory-assist',
      'material-purchase-assist',
      'material-statistics-analysis'
  );
