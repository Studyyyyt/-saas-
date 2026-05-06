-- 患者360数据层初始化脚本
-- 执行前请先选择数据库：USE <your_database>;

CREATE TABLE IF NOT EXISTS patient_followup (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID（对应patients.id）',
    followup_date DATETIME NOT NULL COMMENT '随访时间',
    followup_type VARCHAR(50) DEFAULT NULL COMMENT '随访类型：电话/复诊/线上',
    summary VARCHAR(500) DEFAULT NULL COMMENT '随访摘要',
    next_followup_date DATETIME DEFAULT NULL COMMENT '下次随访时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_followup_patient_id (patient_id),
    KEY idx_followup_date (followup_date),
    KEY idx_followup_next_date (next_followup_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者随访记录';

CREATE TABLE IF NOT EXISTS patient_risk_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID（对应patients.id）',
    tag_code VARCHAR(64) NOT NULL COMMENT '风险标签编码',
    tag_name VARCHAR(100) NOT NULL COMMENT '风险标签名称',
    risk_level TINYINT DEFAULT 1 COMMENT '风险等级：1低 2中 3高',
    source VARCHAR(100) DEFAULT NULL COMMENT '标签来源',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 0失效',
    note VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_risk_patient_id (patient_id),
    KEY idx_risk_level (risk_level),
    KEY idx_risk_status (status),
    UNIQUE KEY uk_risk_patient_tag (patient_id, tag_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者风险标签';

CREATE TABLE IF NOT EXISTS patient_timeline (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID（对应patients.id）',
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型：预约/就诊/治疗/随访/预警',
    event_title VARCHAR(200) NOT NULL COMMENT '事件标题',
    event_content TEXT COMMENT '事件详情',
    source_table VARCHAR(100) DEFAULT NULL COMMENT '来源表',
    source_id BIGINT DEFAULT NULL COMMENT '来源业务ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_timeline_patient_id (patient_id),
    KEY idx_timeline_event_time (event_time),
    KEY idx_timeline_event_type (event_type),
    KEY idx_timeline_source (source_table, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者时间线事件';
