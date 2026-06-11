package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V1__ClinicSchema extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        createTables(connection);
        ensureColumns(connection);
        ensureIndexes(connection);
        backfillData(connection);
    }

    private void createTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS users (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
                    password VARCHAR(100) DEFAULT NULL COMMENT '登录密码',
                    name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
                    role VARCHAR(30) DEFAULT NULL COMMENT '角色：admin/doctor/nurse等',
                    wechat_openid VARCHAR(100) DEFAULT NULL COMMENT '微信 openid',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_username (username),
                    KEY idx_wechat_openid (wechat_openid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS patients (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    name VARCHAR(50) NOT NULL COMMENT '患者姓名',
                    gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
                    date_of_birth DATE DEFAULT NULL COMMENT '出生日期',
                    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                    address VARCHAR(500) DEFAULT NULL COMMENT '地址',
                    wechat_openid VARCHAR(100) DEFAULT NULL COMMENT '微信 openid',
                    PRIMARY KEY (id),
                    KEY idx_name (name),
                    KEY idx_phone (phone),
                    KEY idx_wechat_openid (wechat_openid)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者信息表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS appointment (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
                    patient_name VARCHAR(50) DEFAULT NULL COMMENT '患者姓名',
                    appointment_date DATE DEFAULT NULL COMMENT '预约日期',
                    appointment_time TIME DEFAULT NULL COMMENT '预约时间',
                    doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID',
                    doctor_name VARCHAR(50) DEFAULT NULL COMMENT '医生姓名',
                    appointment_purpose VARCHAR(255) DEFAULT NULL COMMENT '预约目的',
                    cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
                    status VARCHAR(20) DEFAULT NULL COMMENT '状态：预约中/已完成/已取消',
                    PRIMARY KEY (id),
                    KEY idx_appointment_date (appointment_date),
                    KEY idx_patient_name (patient_name),
                    KEY idx_status (status),
                    KEY idx_patient_id (patient_id),
                    KEY idx_appointment_doctor_account_id (doctor_account_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS doctors (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    doctor_name VARCHAR(50) NOT NULL COMMENT '医生姓名',
                    schedule_date DATE DEFAULT NULL COMMENT '排班日期',
                    start_time TIME DEFAULT NULL COMMENT '开始时间',
                    end_time TIME DEFAULT NULL COMMENT '结束时间',
                    status VARCHAR(20) DEFAULT NULL COMMENT '状态：available/busy/off等',
                    PRIMARY KEY (id),
                    KEY idx_doctor_name (doctor_name),
                    KEY idx_schedule_date (schedule_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS treatment_plans (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    treatment_content VARCHAR(500) DEFAULT NULL COMMENT '治疗内容',
                    treatment_free VARCHAR(50) DEFAULT NULL COMMENT '治疗费用（原代码字段名）',
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗方案表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS inventory (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名',
                    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
                    brand VARCHAR(50) DEFAULT NULL COMMENT '品牌',
                    supplier VARCHAR(100) DEFAULT NULL COMMENT '供应商',
                    specification VARCHAR(100) DEFAULT NULL COMMENT '规格',
                    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
                    quantity INT DEFAULT 0 COMMENT '库存数量',
                    selectedQuantity INT DEFAULT 0 COMMENT '已选数量',
                    price VARCHAR(50) DEFAULT NULL COMMENT '价格',
                    product_batch VARCHAR(50) DEFAULT NULL COMMENT '产品批次',
                    PRIMARY KEY (id),
                    KEY idx_product_name (product_name),
                    KEY idx_category (category),
                    KEY idx_supplier (supplier)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS purchases (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名',
                    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
                    brand VARCHAR(50) DEFAULT NULL COMMENT '品牌',
                    supplier VARCHAR(100) DEFAULT NULL COMMENT '供应商',
                    specification VARCHAR(100) DEFAULT NULL COMMENT '规格',
                    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
                    quantity INT DEFAULT 0 COMMENT '采购数量',
                    price VARCHAR(50) DEFAULT NULL COMMENT '价格',
                    status VARCHAR(20) DEFAULT NULL COMMENT '状态',
                    createdate DATETIME DEFAULT NULL COMMENT '创建日期',
                    purchasedate DATETIME DEFAULT NULL COMMENT '采购日期',
                    indate DATETIME DEFAULT NULL COMMENT '入库日期',
                    PRIMARY KEY (id),
                    KEY idx_product_name (product_name),
                    KEY idx_category (category),
                    KEY idx_supplier (supplier)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS finances (
                    id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
                    name VARCHAR(100) DEFAULT NULL COMMENT '财务项名称',
                    amount DOUBLE DEFAULT NULL COMMENT '金额',
                    date VARCHAR(20) DEFAULT NULL COMMENT '日期（字符串格式）',
                    type VARCHAR(30) DEFAULT NULL COMMENT '类型：收入/支出',
                    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    PRIMARY KEY (id),
                    KEY idx_date (date),
                    KEY idx_type (type),
                    KEY idx_finances_patient_id (patient_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS treatment (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT DEFAULT NULL COMMENT '患者ID',
                    patient_name VARCHAR(50) DEFAULT NULL COMMENT '患者姓名',
                    appointment_purpose VARCHAR(255) DEFAULT NULL COMMENT '预约目的',
                    status VARCHAR(20) DEFAULT NULL COMMENT '治疗状态',
                    doctor_name VARCHAR(50) DEFAULT NULL COMMENT '医生姓名',
                    treatment_date DATE DEFAULT NULL COMMENT '治疗日期',
                    treatment_content TEXT COMMENT '治疗内容',
                    tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔',
                    treatment_product VARCHAR(500) DEFAULT NULL COMMENT '使用材料',
                    treatment_fee VARCHAR(50) DEFAULT NULL COMMENT '治疗费用',
                    PRIMARY KEY (id),
                    KEY idx_patient_name (patient_name),
                    KEY idx_treatment_date (treatment_date),
                    KEY idx_treatment_patient_id (patient_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗记录表'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处置收费项目库'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS medical_records (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    patient_name VARCHAR(50) DEFAULT NULL COMMENT '患者姓名（冗余）',
                    doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID',
                    doctor_name VARCHAR(50) DEFAULT NULL COMMENT '医生姓名',
                    visit_date DATETIME DEFAULT NULL COMMENT '就诊日期',
                    chief_complaint TEXT COMMENT '主诉',
                    diagnosis TEXT COMMENT '诊断',
                    treatment TEXT COMMENT '治疗',
                    tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔',
                    prescription TEXT COMMENT '处方',
                    notes TEXT COMMENT '备注',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_patient_id (patient_id),
                    KEY idx_visit_date (visit_date),
                    KEY idx_medical_records_doctor_account_id (doctor_account_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历表'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_followup (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    followup_date DATETIME DEFAULT NULL COMMENT '随访时间',
                    followup_type VARCHAR(50) DEFAULT NULL COMMENT '随访类型：电话/复诊/线上',
                    summary VARCHAR(500) DEFAULT NULL COMMENT '随访摘要',
                    next_followup_date DATETIME DEFAULT NULL COMMENT '下次随访时间',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_patient_id (patient_id),
                    KEY idx_followup_date (followup_date),
                    KEY idx_next_followup_date (next_followup_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者随访记录'
                """);

        execute(connection, """
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
                    KEY idx_patient_id (patient_id),
                    KEY idx_risk_level (risk_level),
                    KEY idx_risk_status (status),
                    UNIQUE KEY uk_risk_patient_tag (patient_id, tag_code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者风险标签'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_timeline (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    event_time DATETIME DEFAULT NULL COMMENT '事件时间',
                    event_type VARCHAR(50) DEFAULT NULL COMMENT '事件类型',
                    event_title VARCHAR(200) DEFAULT NULL COMMENT '事件标题',
                    event_content TEXT COMMENT '事件内容',
                    source_table VARCHAR(100) DEFAULT NULL COMMENT '来源表',
                    source_id BIGINT DEFAULT NULL COMMENT '来源记录ID',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_patient_id (patient_id),
                    KEY idx_event_time (event_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者时间线'
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_wechat_bind_scene (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    patient_id BIGINT NOT NULL,
                    scene_key VARCHAR(128) NOT NULL,
                    qr_ticket VARCHAR(255) DEFAULT NULL,
                    qr_url VARCHAR(512) DEFAULT NULL,
                    expire_seconds INT DEFAULT NULL,
                    status VARCHAR(32) DEFAULT 'pending',
                    bound_at DATETIME DEFAULT NULL,
                    bound_openid VARCHAR(100) DEFAULT NULL,
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_scene_key (scene_key),
                    KEY idx_patient_wechat_bind_scene_patient_id (patient_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        execute(connection, """
                CREATE TABLE IF NOT EXISTS patient_images (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    patient_id BIGINT NOT NULL COMMENT '患者ID',
                    patient_name VARCHAR(100) DEFAULT NULL COMMENT '患者姓名',
                    image_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
                    image_type VARCHAR(50) DEFAULT NULL COMMENT '影像类型：X光/CT/口内照/其他',
                    image_date DATETIME DEFAULT NULL COMMENT '拍摄日期',
                    file_path VARCHAR(500) DEFAULT NULL COMMENT '存储文件名',
                    notes VARCHAR(500) DEFAULT NULL COMMENT '备注',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_patient_id (patient_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者影像记录'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保平台配置'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者医保档案'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保结算记录'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保接口操作日志'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日经营AI分析日报'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营周报月报'
                """);

        execute(connection, """
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
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营异常波动告警日志'
                """);
    }

    private void ensureColumns(Connection connection) throws SQLException {
        ensureColumn(connection, "patients", "wechat_openid",
                "ALTER TABLE patients ADD COLUMN wechat_openid VARCHAR(100) DEFAULT NULL COMMENT '微信 openid'");
        ensureColumn(connection, "appointment", "patient_id",
                "ALTER TABLE appointment ADD COLUMN patient_id BIGINT DEFAULT NULL COMMENT '患者ID' AFTER id");
        ensureColumn(connection, "appointment", "doctor_account_id",
                "ALTER TABLE appointment ADD COLUMN doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID' AFTER appointment_time");
        ensureColumn(connection, "appointment", "cancel_reason",
                "ALTER TABLE appointment ADD COLUMN cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因' AFTER appointment_purpose");

        ensureColumn(connection, "finances", "patient_id",
                "ALTER TABLE finances ADD COLUMN patient_id BIGINT DEFAULT NULL COMMENT '患者ID' AFTER id");

        ensureColumn(connection, "treatment", "patient_id",
                "ALTER TABLE treatment ADD COLUMN patient_id BIGINT DEFAULT NULL COMMENT '患者ID' AFTER id");
        ensureColumn(connection, "treatment", "tooth_positions",
                "ALTER TABLE treatment ADD COLUMN tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔' AFTER treatment_content");

        ensureColumn(connection, "medical_records", "doctor_account_id",
                "ALTER TABLE medical_records ADD COLUMN doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID' AFTER patient_name");
        ensureColumn(connection, "medical_records", "tooth_positions",
                "ALTER TABLE medical_records ADD COLUMN tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔' AFTER treatment");

        ensureColumn(connection, "treatment_catalog", "medical_insurance_code",
                "ALTER TABLE treatment_catalog ADD COLUMN medical_insurance_code VARCHAR(64) DEFAULT NULL COMMENT '医保项目编码' AFTER sort_order");
        ensureColumn(connection, "treatment_catalog", "medical_insurance_name",
                "ALTER TABLE treatment_catalog ADD COLUMN medical_insurance_name VARCHAR(100) DEFAULT NULL COMMENT '医保项目名称' AFTER medical_insurance_code");
        ensureColumn(connection, "treatment_catalog", "medical_insurance_category",
                "ALTER TABLE treatment_catalog ADD COLUMN medical_insurance_category VARCHAR(50) DEFAULT NULL COMMENT '医保分类：甲类/乙类/丙类/自费' AFTER medical_insurance_name");
        ensureColumn(connection, "treatment_catalog", "self_pay_ratio",
                "ALTER TABLE treatment_catalog ADD COLUMN self_pay_ratio DECIMAL(10,4) DEFAULT NULL COMMENT '自付比例' AFTER medical_insurance_category");
    }

    private void ensureIndexes(Connection connection) throws SQLException {
        ensureIndex(connection, "users", "uk_username",
                "ALTER TABLE users ADD UNIQUE KEY uk_username (username)");
        ensureIndex(connection, "users", "idx_wechat_openid",
                "CREATE INDEX idx_wechat_openid ON users (wechat_openid)");

        ensureIndex(connection, "patients", "idx_name",
                "CREATE INDEX idx_name ON patients (name)");
        ensureIndex(connection, "patients", "idx_phone",
                "CREATE INDEX idx_phone ON patients (phone)");
        ensureIndex(connection, "patients", "idx_wechat_openid",
                "CREATE INDEX idx_wechat_openid ON patients (wechat_openid)");

        ensureIndex(connection, "appointment", "idx_appointment_date",
                "CREATE INDEX idx_appointment_date ON appointment (appointment_date)");
        ensureIndex(connection, "appointment", "idx_patient_name",
                "CREATE INDEX idx_patient_name ON appointment (patient_name)");
        ensureIndex(connection, "appointment", "idx_status",
                "CREATE INDEX idx_status ON appointment (status)");
        ensureIndex(connection, "appointment", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON appointment (patient_id)");
        ensureIndex(connection, "appointment", "idx_appointment_doctor_account_id",
                "CREATE INDEX idx_appointment_doctor_account_id ON appointment (doctor_account_id)");

        ensureIndex(connection, "doctors", "idx_doctor_name",
                "CREATE INDEX idx_doctor_name ON doctors (doctor_name)");
        ensureIndex(connection, "doctors", "idx_schedule_date",
                "CREATE INDEX idx_schedule_date ON doctors (schedule_date)");

        ensureIndex(connection, "inventory", "idx_product_name",
                "CREATE INDEX idx_product_name ON inventory (product_name)");
        ensureIndex(connection, "inventory", "idx_category",
                "CREATE INDEX idx_category ON inventory (category)");
        ensureIndex(connection, "inventory", "idx_supplier",
                "CREATE INDEX idx_supplier ON inventory (supplier)");

        ensureIndex(connection, "purchases", "idx_product_name",
                "CREATE INDEX idx_product_name ON purchases (product_name)");
        ensureIndex(connection, "purchases", "idx_category",
                "CREATE INDEX idx_category ON purchases (category)");
        ensureIndex(connection, "purchases", "idx_supplier",
                "CREATE INDEX idx_supplier ON purchases (supplier)");

        ensureIndex(connection, "finances", "idx_date",
                "CREATE INDEX idx_date ON finances (date)");
        ensureIndex(connection, "finances", "idx_type",
                "CREATE INDEX idx_type ON finances (type)");
        ensureIndex(connection, "finances", "idx_finances_patient_id",
                "CREATE INDEX idx_finances_patient_id ON finances (patient_id)");

        ensureIndex(connection, "treatment", "idx_patient_name",
                "CREATE INDEX idx_patient_name ON treatment (patient_name)");
        ensureIndex(connection, "treatment", "idx_treatment_date",
                "CREATE INDEX idx_treatment_date ON treatment (treatment_date)");
        ensureIndex(connection, "treatment", "idx_treatment_patient_id",
                "CREATE INDEX idx_treatment_patient_id ON treatment (patient_id)");

        ensureIndex(connection, "medical_records", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON medical_records (patient_id)");
        ensureIndex(connection, "medical_records", "idx_visit_date",
                "CREATE INDEX idx_visit_date ON medical_records (visit_date)");
        ensureIndex(connection, "medical_records", "idx_medical_records_doctor_account_id",
                "CREATE INDEX idx_medical_records_doctor_account_id ON medical_records (doctor_account_id)");

        ensureIndex(connection, "patient_followup", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON patient_followup (patient_id)");
        ensureIndex(connection, "patient_followup", "idx_followup_date",
                "CREATE INDEX idx_followup_date ON patient_followup (followup_date)");
        ensureIndex(connection, "patient_followup", "idx_next_followup_date",
                "CREATE INDEX idx_next_followup_date ON patient_followup (next_followup_date)");

        ensureIndex(connection, "patient_risk_tag", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON patient_risk_tag (patient_id)");
        ensureIndex(connection, "patient_risk_tag", "idx_risk_level",
                "CREATE INDEX idx_risk_level ON patient_risk_tag (risk_level)");
        ensureIndex(connection, "patient_risk_tag", "idx_risk_status",
                "CREATE INDEX idx_risk_status ON patient_risk_tag (status)");
        ensureIndex(connection, "patient_risk_tag", "uk_risk_patient_tag",
                "ALTER TABLE patient_risk_tag ADD UNIQUE KEY uk_risk_patient_tag (patient_id, tag_code)");

        ensureIndex(connection, "patient_timeline", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON patient_timeline (patient_id)");
        ensureIndex(connection, "patient_timeline", "idx_event_time",
                "CREATE INDEX idx_event_time ON patient_timeline (event_time)");

        ensureIndex(connection, "patient_wechat_bind_scene", "uk_scene_key",
                "ALTER TABLE patient_wechat_bind_scene ADD UNIQUE KEY uk_scene_key (scene_key)");
        ensureIndex(connection, "patient_wechat_bind_scene", "idx_patient_wechat_bind_scene_patient_id",
                "CREATE INDEX idx_patient_wechat_bind_scene_patient_id ON patient_wechat_bind_scene (patient_id)");

        ensureIndex(connection, "patient_images", "idx_patient_id",
                "CREATE INDEX idx_patient_id ON patient_images (patient_id)");

        ensureIndex(connection, "insurance_patient_profile", "uk_insurance_patient",
                "ALTER TABLE insurance_patient_profile ADD UNIQUE KEY uk_insurance_patient (patient_id)");
        ensureIndex(connection, "insurance_patient_profile", "idx_insurance_person_no",
                "CREATE INDEX idx_insurance_person_no ON insurance_patient_profile (insurance_person_no)");

        ensureIndex(connection, "insurance_settlement", "idx_insurance_settlement_patient",
                "CREATE INDEX idx_insurance_settlement_patient ON insurance_settlement (patient_id)");
        ensureIndex(connection, "insurance_settlement", "idx_insurance_settlement_status",
                "CREATE INDEX idx_insurance_settlement_status ON insurance_settlement (settlement_status)");

        ensureIndex(connection, "insurance_operation_log", "idx_insurance_log_operation",
                "CREATE INDEX idx_insurance_log_operation ON insurance_operation_log (operation_type)");
        ensureIndex(connection, "insurance_operation_log", "idx_insurance_log_status",
                "CREATE INDEX idx_insurance_log_status ON insurance_operation_log (status)");

        ensureIndex(connection, "business_daily_analysis", "uk_business_analysis_date",
                "ALTER TABLE business_daily_analysis ADD UNIQUE KEY uk_business_analysis_date (analysis_date)");
        ensureIndex(connection, "business_daily_analysis", "idx_business_analysis_status",
                "CREATE INDEX idx_business_analysis_status ON business_daily_analysis (analysis_status)");

        ensureIndex(connection, "business_period_report", "uk_business_period_report",
                "ALTER TABLE business_period_report ADD UNIQUE KEY uk_business_period_report (report_type, period_key)");
        ensureIndex(connection, "business_period_report", "idx_business_period_report_status",
                "CREATE INDEX idx_business_period_report_status ON business_period_report (report_status)");

        ensureIndex(connection, "business_alert_log", "idx_business_alert_date",
                "CREATE INDEX idx_business_alert_date ON business_alert_log (alert_date)");
        ensureIndex(connection, "business_alert_log", "idx_business_alert_level",
                "CREATE INDEX idx_business_alert_level ON business_alert_log (alert_level)");
    }

    private void backfillData(Connection connection) throws SQLException {
        if (tableExists(connection, "appointment")
                && tableExists(connection, "patients")
                && columnExists(connection, "appointment", "patient_id")
                && columnExists(connection, "appointment", "patient_name")) {
            execute(connection, """
                    UPDATE appointment a
                    JOIN (
                        SELECT p.name, MAX(p.id) AS patient_id
                        FROM patients p
                        GROUP BY p.name
                        HAVING COUNT(*) = 1
                    ) u ON u.name = a.patient_name
                    SET a.patient_id = u.patient_id
                    WHERE a.patient_id IS NULL
                      AND a.patient_name IS NOT NULL
                      AND TRIM(a.patient_name) <> ''
                    """);
        }

        if (tableExists(connection, "appointment")
                && tableExists(connection, "users")
                && columnExists(connection, "appointment", "doctor_account_id")
                && columnExists(connection, "appointment", "doctor_name")) {
            execute(connection, """
                    UPDATE appointment a
                    JOIN (
                        SELECT u.name, MAX(u.id) AS doctor_account_id
                        FROM users u
                        WHERE u.role = 'doctor'
                          AND u.name IS NOT NULL
                          AND TRIM(u.name) <> ''
                        GROUP BY u.name
                        HAVING COUNT(*) = 1
                    ) d ON d.name = a.doctor_name
                    SET a.doctor_account_id = d.doctor_account_id
                    WHERE a.doctor_account_id IS NULL
                      AND a.doctor_name IS NOT NULL
                      AND TRIM(a.doctor_name) <> ''
                      AND TRIM(a.doctor_name) <> '未指定医生'
                    """);
        }

        if (tableExists(connection, "finances")
                && tableExists(connection, "patients")
                && columnExists(connection, "finances", "patient_id")
                && columnExists(connection, "finances", "name")) {
            execute(connection, """
                    UPDATE finances f
                    JOIN patients p ON p.name = f.name
                    LEFT JOIN (
                        SELECT name
                        FROM patients
                        GROUP BY name
                        HAVING COUNT(*) > 1
                    ) dup ON dup.name = p.name
                    SET f.patient_id = p.id
                    WHERE f.patient_id IS NULL
                      AND dup.name IS NULL
                    """);
        }

        if (tableExists(connection, "treatment")
                && tableExists(connection, "patients")
                && columnExists(connection, "treatment", "patient_id")
                && columnExists(connection, "treatment", "patient_name")) {
            execute(connection, """
                    UPDATE treatment t
                    JOIN patients p ON p.name = t.patient_name
                    LEFT JOIN (
                        SELECT name
                        FROM patients
                        GROUP BY name
                        HAVING COUNT(*) > 1
                    ) dup ON dup.name = p.name
                    SET t.patient_id = p.id
                    WHERE t.patient_id IS NULL
                      AND dup.name IS NULL
                    """);
        }
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || columnExists(connection, tableName, columnName)) {
            return;
        }
        execute(connection, sql);
    }

    private void ensureIndex(Connection connection, String tableName, String indexName, String sql) throws SQLException {
        if (!tableExists(connection, tableName) || indexExists(connection, tableName, indexName)) {
            return;
        }
        execute(connection, sql);
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (rs.next()) {
                String currentName = rs.getString("INDEX_NAME");
                if (currentName != null && indexName.equalsIgnoreCase(currentName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
