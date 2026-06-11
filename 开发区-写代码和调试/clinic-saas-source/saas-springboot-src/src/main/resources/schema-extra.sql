CREATE TABLE IF NOT EXISTS treatment_catalog (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    item_name VARCHAR(100) NOT NULL COMMENT '处置收费项目名称',
    default_fee VARCHAR(50) DEFAULT NULL COMMENT '默认收费',
    default_content VARCHAR(500) DEFAULT NULL COMMENT '默认治疗内容',
    default_product VARCHAR(255) DEFAULT NULL COMMENT '默认使用材料',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    medical_insurance_code VARCHAR(64) DEFAULT NULL COMMENT '医保项目编码',
    medical_insurance_name VARCHAR(100) DEFAULT NULL COMMENT '医保项目名称',
    medical_insurance_category VARCHAR(50) DEFAULT NULL COMMENT '医保分类：甲类/乙类/丙类/自费',
    self_pay_ratio DECIMAL(10,4) DEFAULT NULL COMMENT '自付比例',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处置收费项目库';

CREATE TABLE IF NOT EXISTS medical_records (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(100) DEFAULT NULL COMMENT '患者姓名（冗余）',
    doctor_name VARCHAR(100) DEFAULT NULL COMMENT '接诊医生',
    visit_date DATE NOT NULL COMMENT '就诊日期',
    chief_complaint VARCHAR(500) DEFAULT NULL COMMENT '主诉',
    diagnosis VARCHAR(500) DEFAULT NULL COMMENT '诊断',
    treatment VARCHAR(1000) DEFAULT NULL COMMENT '处置方案',
    prescription TEXT DEFAULT NULL COMMENT '处方',
    notes VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_mr_patient_id (patient_id),
    KEY idx_mr_visit_date (visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历记录';

ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS age INT DEFAULT NULL COMMENT '年龄' AFTER gender;

ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS wechat_openid VARCHAR(128) DEFAULT NULL COMMENT '微信openid';

CREATE TABLE IF NOT EXISTS patient_followup (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    followup_date DATETIME NOT NULL COMMENT '随访时间',
    followup_type VARCHAR(50) DEFAULT NULL COMMENT '随访类型：电话/复诊/线上',
    summary VARCHAR(500) DEFAULT NULL COMMENT '随访摘要',
    next_followup_date DATETIME DEFAULT NULL COMMENT '下次随访时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_followup_patient_id (patient_id),
    KEY idx_followup_date (followup_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者随访记录';

CREATE TABLE IF NOT EXISTS patient_risk_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    tag_code VARCHAR(64) NOT NULL COMMENT '风险标签编码',
    tag_name VARCHAR(100) NOT NULL COMMENT '风险标签名称',
    risk_level TINYINT DEFAULT 1 COMMENT '风险等级：1低 2中 3高',
    source VARCHAR(100) DEFAULT NULL COMMENT '标签来源',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 0失效',
    note VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_risk_patient_id (patient_id),
    UNIQUE KEY uk_risk_patient_tag (patient_id, tag_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者风险标签';

CREATE TABLE IF NOT EXISTS patient_timeline (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    event_time DATETIME NOT NULL COMMENT '事件时间',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_title VARCHAR(200) NOT NULL COMMENT '事件标题',
    event_content TEXT COMMENT '事件详情',
    source_table VARCHAR(100) DEFAULT NULL COMMENT '来源表',
    source_id BIGINT DEFAULT NULL COMMENT '来源业务ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_timeline_patient_id (patient_id),
    KEY idx_timeline_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者时间线';

CREATE TABLE IF NOT EXISTS insurance_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    platform_code VARCHAR(64) NOT NULL COMMENT '医保平台编码',
    platform_name VARCHAR(100) NOT NULL COMMENT '医保平台名称',
    api_base_url VARCHAR(255) DEFAULT NULL COMMENT '接口基础地址',
    org_code VARCHAR(64) DEFAULT NULL COMMENT '机构编码',
    org_name VARCHAR(100) DEFAULT NULL COMMENT '机构名称',
    app_id VARCHAR(128) DEFAULT NULL COMMENT '应用ID',
    app_secret VARCHAR(255) DEFAULT NULL COMMENT '应用密钥',
    sign_key VARCHAR(255) DEFAULT NULL COMMENT '签名密钥',
    encryption_type VARCHAR(50) DEFAULT NULL COMMENT '加密方式',
    region_code VARCHAR(64) DEFAULT NULL COMMENT '统筹区编码',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    ext_json TEXT DEFAULT NULL COMMENT '扩展配置JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保平台配置';

CREATE TABLE IF NOT EXISTS insurance_patient_profile (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    insurance_person_no VARCHAR(64) DEFAULT NULL COMMENT '医保人员编号',
    id_card_no VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
    insured_region_code VARCHAR(64) DEFAULT NULL COMMENT '参保地编码',
    insured_type VARCHAR(64) DEFAULT NULL COMMENT '参保类型',
    card_no VARCHAR(64) DEFAULT NULL COMMENT '医保卡号',
    card_type VARCHAR(32) DEFAULT NULL COMMENT '卡类型',
    person_name VARCHAR(100) DEFAULT NULL COMMENT '医保登记姓名',
    gender VARCHAR(16) DEFAULT NULL COMMENT '性别',
    birthday VARCHAR(20) DEFAULT NULL COMMENT '出生日期',
    phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1有效 0停用',
    last_auth_no VARCHAR(64) DEFAULT NULL COMMENT '最近认证流水号',
    last_verified_at DATETIME DEFAULT NULL COMMENT '最近校验时间',
    ext_json TEXT DEFAULT NULL COMMENT '扩展字段JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_insurance_patient (patient_id),
    KEY idx_insurance_person_no (insurance_person_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者医保档案';

CREATE TABLE IF NOT EXISTS insurance_settlement (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    finance_id BIGINT DEFAULT NULL COMMENT '财务记录ID',
    treatment_id BIGINT DEFAULT NULL COMMENT '治疗记录ID',
    settlement_no VARCHAR(64) DEFAULT NULL COMMENT '医保结算单号',
    visit_no VARCHAR(64) DEFAULT NULL COMMENT '就诊流水号',
    biz_type VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
    settlement_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '结算状态',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    insurance_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '医保支付金额',
    personal_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '个人账户/自付金额',
    cash_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '现金金额',
    upload_status VARCHAR(32) NOT NULL DEFAULT 'NOT_UPLOADED' COMMENT '上传状态',
    upload_payload MEDIUMTEXT DEFAULT NULL COMMENT '上传报文',
    response_payload MEDIUMTEXT DEFAULT NULL COMMENT '返回报文',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    settlement_time DATETIME DEFAULT NULL COMMENT '结算时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_insurance_settlement_patient (patient_id),
    KEY idx_insurance_settlement_status (settlement_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保结算记录';

CREATE TABLE IF NOT EXISTS insurance_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    ref_type VARCHAR(64) DEFAULT NULL COMMENT '关联业务类型',
    ref_id VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
    request_method VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
    request_payload MEDIUMTEXT DEFAULT NULL COMMENT '请求报文',
    response_payload MEDIUMTEXT DEFAULT NULL COMMENT '响应报文',
    response_code VARCHAR(64) DEFAULT NULL COMMENT '响应编码',
    response_message VARCHAR(255) DEFAULT NULL COMMENT '响应信息',
    status VARCHAR(32) DEFAULT NULL COMMENT '执行状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_insurance_log_operation (operation_type),
    KEY idx_insurance_log_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保接口操作日志';

ALTER TABLE finances
    ADD COLUMN IF NOT EXISTS patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
    ADD COLUMN IF NOT EXISTS treatment_id BIGINT DEFAULT NULL COMMENT '处置记录ID',
    ADD COLUMN IF NOT EXISTS biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
    ADD KEY IF NOT EXISTS idx_finances_patient_id (patient_id);

ALTER TABLE finances
    ADD KEY IF NOT EXISTS idx_finances_treatment_id (treatment_id),
    ADD KEY IF NOT EXISTS idx_finances_biz_type (biz_type);

ALTER TABLE treatment
    ADD COLUMN IF NOT EXISTS patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
    ADD KEY IF NOT EXISTS idx_treatment_patient_id (patient_id);

ALTER TABLE appointment
    ADD COLUMN IF NOT EXISTS patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
    ADD KEY IF NOT EXISTS idx_appointment_patient_id (patient_id);

ALTER TABLE appointment
    ADD COLUMN IF NOT EXISTS duration_minutes INT NOT NULL DEFAULT 60 COMMENT '预约时长（分钟）' AFTER appointment_time;

UPDATE appointment
SET duration_minutes = 60
WHERE duration_minutes IS NULL OR duration_minutes <= 0;

CREATE TABLE IF NOT EXISTS patient_images (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(100) DEFAULT NULL COMMENT '患者姓名',
    image_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    image_type VARCHAR(50) DEFAULT NULL COMMENT '影像类型：X光/CT/口内照/其他',
    image_date DATE DEFAULT NULL COMMENT '拍摄日期',
    file_path VARCHAR(500) DEFAULT NULL COMMENT '存储文件名',
    notes VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_img_patient_id (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者影像记录';

ALTER TABLE patient_images
    ADD COLUMN IF NOT EXISTS sent_to_patient TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送给患者' AFTER notes,
    ADD COLUMN IF NOT EXISTS sent_at DATETIME DEFAULT NULL COMMENT '发送时间' AFTER sent_to_patient,
    ADD KEY IF NOT EXISTS idx_patient_images_sent (patient_id, sent_to_patient);

CREATE TABLE IF NOT EXISTS business_daily_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    analysis_date DATE NOT NULL COMMENT '分析对应日期',
    analysis_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '分析状态：SUCCESS/FALLBACK/FAILED/PENDING',
    source_type VARCHAR(32) NOT NULL DEFAULT 'RULE_BASED' COMMENT '分析来源：OPENAI/RULE_BASED',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发方式：SCHEDULED/MANUAL',
    model_name VARCHAR(64) DEFAULT NULL COMMENT '使用模型',
    operating_score INT DEFAULT NULL COMMENT '经营评分',
    trend VARCHAR(16) DEFAULT NULL COMMENT '趋势：up/flat/down',
    headline VARCHAR(255) DEFAULT NULL COMMENT '日报标题',
    summary TEXT DEFAULT NULL COMMENT '分析摘要',
    metrics_json MEDIUMTEXT DEFAULT NULL COMMENT '经营指标JSON',
    analysis_json MEDIUMTEXT DEFAULT NULL COMMENT '结构化分析JSON',
    raw_response MEDIUMTEXT DEFAULT NULL COMMENT '模型原始输出',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_business_analysis_date (analysis_date),
    KEY idx_business_analysis_status (analysis_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日经营AI分析日报';

CREATE TABLE IF NOT EXISTS business_period_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    report_type VARCHAR(16) NOT NULL COMMENT '报表类型：WEEKLY/MONTHLY',
    period_key VARCHAR(32) NOT NULL COMMENT '周期键：如2026-W17、2026-04',
    period_start DATE NOT NULL COMMENT '周期开始日期',
    period_end DATE NOT NULL COMMENT '周期结束日期',
    report_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：SUCCESS/FALLBACK/FAILED/PENDING',
    source_type VARCHAR(32) NOT NULL DEFAULT 'RULE_BASED' COMMENT '分析来源：OPENAI/RULE_BASED',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发方式：SCHEDULED/MANUAL',
    model_name VARCHAR(64) DEFAULT NULL COMMENT '使用模型',
    operating_score INT DEFAULT NULL COMMENT '经营评分',
    trend VARCHAR(16) DEFAULT NULL COMMENT '趋势：up/flat/down',
    headline VARCHAR(255) DEFAULT NULL COMMENT '标题',
    summary TEXT DEFAULT NULL COMMENT '摘要',
    metrics_json MEDIUMTEXT DEFAULT NULL COMMENT '指标JSON',
    analysis_json MEDIUMTEXT DEFAULT NULL COMMENT '结构化分析JSON',
    raw_response MEDIUMTEXT DEFAULT NULL COMMENT '模型原始输出',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_business_period_report (report_type, period_key),
    KEY idx_business_period_report_status (report_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营周报月报';

CREATE TABLE IF NOT EXISTS business_alert_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    alert_date DATE NOT NULL COMMENT '告警日期',
    alert_code VARCHAR(64) NOT NULL COMMENT '告警编码',
    alert_level VARCHAR(16) NOT NULL COMMENT '级别：HIGH/MEDIUM/LOW',
    alert_title VARCHAR(200) NOT NULL COMMENT '告警标题',
    alert_message VARCHAR(1000) DEFAULT NULL COMMENT '告警说明',
    metric_name VARCHAR(64) DEFAULT NULL COMMENT '指标名',
    current_value DECIMAL(12,2) DEFAULT NULL COMMENT '当前值',
    baseline_value DECIMAL(12,2) DEFAULT NULL COMMENT '基线值',
    change_rate DECIMAL(12,2) DEFAULT NULL COMMENT '变化幅度',
    suggested_action VARCHAR(500) DEFAULT NULL COMMENT '建议动作',
    source_type VARCHAR(32) NOT NULL DEFAULT 'RULE_BASED' COMMENT '来源：RULE_BASED/OPENAI',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发方式：SCHEDULED/MANUAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_business_alert_date (alert_date),
    KEY idx_business_alert_level (alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营异常波动告警日志';
