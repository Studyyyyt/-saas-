-- 咨询记录增强字段：提升成单效率
ALTER TABLE consultation_records
    ADD COLUMN estimated_amount DECIMAL(12, 2) DEFAULT NULL COMMENT '预计消费金额（元）' AFTER remarks,
    ADD COLUMN customer_concerns VARCHAR(500) DEFAULT NULL COMMENT '客户顾虑、竞品对比、犹豫原因等' AFTER estimated_amount,
    ADD COLUMN ai_analysis_summary VARCHAR(1000) DEFAULT NULL COMMENT 'AI分析摘要' AFTER customer_concerns,
    ADD COLUMN ai_analysis_score INT DEFAULT NULL COMMENT 'AI意向评分 0-100' AFTER ai_analysis_summary;

-- 咨询跟进历史子表：与 patient_followups（医疗回访）不同，本表聚焦咨询销售跟进
CREATE TABLE consultation_followups (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    consultation_id BIGINT NOT NULL COMMENT '关联咨询记录ID',
    followup_time DATETIME NOT NULL COMMENT '跟进时间',
    content VARCHAR(1000) NOT NULL COMMENT '跟进内容',
    next_followup_time DATETIME DEFAULT NULL COMMENT '下次计划跟进时间',
    created_by BIGINT NOT NULL COMMENT '跟进人ID',
    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '跟进人姓名',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_consultation_id (consultation_id),
    CONSTRAINT fk_followup_consultation FOREIGN KEY (consultation_id) REFERENCES consultation_records(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询跟进记录表';
