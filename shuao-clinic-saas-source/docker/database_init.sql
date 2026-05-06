-- MySQL dump 10.13  Distrib 9.6.0, for macos26.3 (arm64)
--
-- Host: localhost    Database: clinic_system
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `advertising_spending`
--

DROP TABLE IF EXISTS `advertising_spending`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `advertising_spending` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(30) NOT NULL COMMENT '投放平台',
  `campaign_name` varchar(100) DEFAULT NULL COMMENT '活动名称',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '投放金额',
  `target_project` varchar(50) DEFAULT NULL COMMENT '目标项目',
  `target_audience` varchar(100) DEFAULT NULL COMMENT '目标人群',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `finance_record_id` bigint DEFAULT NULL COMMENT '同步财务记录ID',
  `created_by` bigint DEFAULT NULL COMMENT '录入人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '录入人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ad_platform_start` (`platform`,`start_date`),
  KEY `idx_ad_period` (`start_date`,`end_date`),
  KEY `idx_ad_created_by` (`created_by`),
  KEY `idx_ad_finance_record_id` (`finance_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='广告投放记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `appointment_date` date DEFAULT NULL COMMENT '预约日期',
  `appointment_time` time DEFAULT NULL COMMENT '预约时间',
  `duration_minutes` int NOT NULL DEFAULT '60' COMMENT '预约时长（分钟）',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '医生账号ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '医生姓名',
  `appointment_purpose` varchar(255) DEFAULT NULL COMMENT '预约目的',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `status` varchar(20) DEFAULT NULL COMMENT '状态：预约中/已完成/已取消',
  PRIMARY KEY (`id`),
  KEY `idx_appointment_date` (`appointment_date`),
  KEY `idx_patient_name` (`patient_name`),
  KEY `idx_status` (`status`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_appointment_doctor_account_id` (`doctor_account_id`),
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business_alert_log`
--

DROP TABLE IF EXISTS `business_alert_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_alert_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alert_date` date NOT NULL COMMENT '告警日期',
  `alert_code` varchar(64) NOT NULL COMMENT '告警编码',
  `alert_level` varchar(16) NOT NULL COMMENT '级别：HIGH/MEDIUM/LOW',
  `alert_title` varchar(200) NOT NULL COMMENT '告警标题',
  `alert_message` varchar(1000) DEFAULT NULL COMMENT '告警说明',
  `metric_name` varchar(64) DEFAULT NULL COMMENT '指标名',
  `current_value` decimal(12,2) DEFAULT NULL COMMENT '当前值',
  `baseline_value` decimal(12,2) DEFAULT NULL COMMENT '基线值',
  `change_rate` decimal(12,2) DEFAULT NULL COMMENT '变化幅度',
  `suggested_action` varchar(500) DEFAULT NULL COMMENT '建议动作',
  `source_type` varchar(32) NOT NULL DEFAULT 'RULE_BASED' COMMENT '来源：RULE_BASED/OPENAI',
  `trigger_type` varchar(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发方式：SCHEDULED/MANUAL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_business_alert_date` (`alert_date`),
  KEY `idx_business_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营异常波动告警日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business_daily_analysis`
--

DROP TABLE IF EXISTS `business_daily_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_daily_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `analysis_date` date NOT NULL COMMENT '分析对应日期',
  `analysis_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '分析状态：SUCCESS/FALLBACK/FAILED/PENDING',
  `source_type` varchar(32) NOT NULL DEFAULT 'RULE_BASED' COMMENT '分析来源：OPENAI/RULE_BASED',
  `trigger_type` varchar(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发方式：SCHEDULED/MANUAL',
  `model_name` varchar(64) DEFAULT NULL COMMENT '使用模型',
  `operating_score` int DEFAULT NULL COMMENT '经营评分',
  `trend` varchar(16) DEFAULT NULL COMMENT '趋势：up/flat/down',
  `headline` varchar(255) DEFAULT NULL COMMENT '日报标题',
  `summary` text COMMENT '分析摘要',
  `metrics_json` mediumtext COMMENT '经营指标JSON',
  `analysis_json` mediumtext COMMENT '结构化分析JSON',
  `raw_response` mediumtext COMMENT '模型原始输出',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_analysis_date` (`analysis_date`),
  KEY `idx_business_analysis_status` (`analysis_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日经营AI分析日报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `business_period_report`
--

DROP TABLE IF EXISTS `business_period_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_period_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_type` varchar(16) NOT NULL COMMENT '报表类型：WEEKLY/MONTHLY',
  `period_key` varchar(32) NOT NULL COMMENT '周期键：如2026-W17、2026-04',
  `period_start` date NOT NULL COMMENT '周期开始日期',
  `period_end` date NOT NULL COMMENT '周期结束日期',
  `report_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：SUCCESS/FALLBACK/FAILED/PENDING',
  `source_type` varchar(32) NOT NULL DEFAULT 'RULE_BASED' COMMENT '分析来源：OPENAI/RULE_BASED',
  `trigger_type` varchar(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发方式：SCHEDULED/MANUAL',
  `model_name` varchar(64) DEFAULT NULL COMMENT '使用模型',
  `operating_score` int DEFAULT NULL COMMENT '经营评分',
  `trend` varchar(16) DEFAULT NULL COMMENT '趋势：up/flat/down',
  `headline` varchar(255) DEFAULT NULL COMMENT '标题',
  `summary` text COMMENT '摘要',
  `metrics_json` mediumtext COMMENT '指标JSON',
  `analysis_json` mediumtext COMMENT '结构化分析JSON',
  `raw_response` mediumtext COMMENT '模型原始输出',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_period_report` (`report_type`,`period_key`),
  KEY `idx_business_period_report_status` (`report_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营周报月报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `consent_template`
--

DROP TABLE IF EXISTS `consent_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consent_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '模板标题',
  `content` text NOT NULL COMMENT '模板正文',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_consent_template_status` (`status`),
  KEY `idx_consent_template_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知情同意书模板库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `consultation_records`
--

DROP TABLE IF EXISTS `consultation_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int DEFAULT NULL COMMENT '关联患者ID',
  `consultation_time` datetime NOT NULL COMMENT '咨询时间',
  `consultation_channel` varchar(30) NOT NULL COMMENT '咨询渠道',
  `referrer_type` varchar(20) DEFAULT NULL COMMENT '介绍人类型：patient/external',
  `referrer_patient_id` bigint DEFAULT NULL COMMENT '介绍患者ID',
  `referrer_patient_name` varchar(50) DEFAULT NULL COMMENT '介绍患者姓名',
  `external_referrer_type` varchar(30) DEFAULT NULL COMMENT '外部介绍人类型',
  `external_referrer_name` varchar(50) DEFAULT NULL COMMENT '外部介绍人姓名',
  `external_referrer_contact` varchar(50) DEFAULT NULL COMMENT '外部介绍人联系方式',
  `chief_project` varchar(30) NOT NULL COMMENT '主诉项目',
  `intent_level` varchar(10) NOT NULL COMMENT '意向强度：高/中/低',
  `handling_result` varchar(20) NOT NULL DEFAULT '待跟进' COMMENT '处理结果：已预约到店/待跟进/不再跟进',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '咨询人姓名/昵称',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系方式',
  `remarks` varchar(200) DEFAULT NULL COMMENT '备注',
  `arrived_at` datetime DEFAULT NULL COMMENT '首次进入已预约到店时间',
  `deal_at` datetime DEFAULT NULL COMMENT '首次成交时间',
  `created_by` bigint NOT NULL COMMENT '录入人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '录入人姓名',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_consultation_time` (`consultation_time`),
  KEY `idx_consultation_channel_time` (`consultation_channel`,`consultation_time`),
  KEY `idx_contact_phone` (`contact_phone`),
  KEY `idx_intent_result` (`intent_level`,`handling_result`),
  KEY `idx_handling_result` (`handling_result`),
  KEY `idx_arrived_at` (`arrived_at`),
  KEY `idx_deal_at` (`deal_at`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_consultation_referrer_patient_id` (`referrer_patient_id`),
  CONSTRAINT `fk_consultation_records_patient_relaxed` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='咨询记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctor_home_reminder_dismissal`
--

DROP TABLE IF EXISTS `doctor_home_reminder_dismissal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_home_reminder_dismissal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doctor_account_id` bigint NOT NULL COMMENT '医生账号ID',
  `doctor_name` varchar(64) DEFAULT NULL COMMENT '医生姓名',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_name` varchar(64) DEFAULT NULL COMMENT '患者姓名',
  `reminder_key` varchar(191) NOT NULL COMMENT '提醒唯一键',
  `dismissed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '标记完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_home_reminder_dismissal` (`doctor_account_id`,`reminder_key`),
  KEY `idx_doctor_home_reminder_patient` (`doctor_account_id`,`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生首页提醒消失状态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctors`
--

DROP TABLE IF EXISTS `doctors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctors` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `schedule_date` date DEFAULT NULL COMMENT '排班日期',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) DEFAULT NULL COMMENT '状态：available/busy/off等',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_name` (`doctor_name`),
  KEY `idx_schedule_date` (`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生排班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `finances`
--

DROP TABLE IF EXISTS `finances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finances` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int DEFAULT NULL COMMENT '患者ID',
  `treatment_id` bigint DEFAULT NULL COMMENT '处置记录ID',
  `payment_channel_id` bigint DEFAULT NULL COMMENT '收款渠道ID',
  `payment_channel_name` varchar(100) DEFAULT NULL COMMENT '收款渠道名称',
  `name` varchar(100) DEFAULT NULL COMMENT '财务项名称',
  `amount` double DEFAULT NULL COMMENT '金额',
  `date` varchar(20) DEFAULT NULL COMMENT '日期（字符串格式）',
  `type` varchar(30) DEFAULT NULL COMMENT '类型：收入/支出',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_date` (`date`),
  KEY `idx_type` (`type`),
  KEY `idx_finances_patient_id` (`patient_id`),
  KEY `idx_finances_treatment_id` (`treatment_id`),
  KEY `idx_finances_biz_type` (`biz_type`),
  KEY `idx_finances_payment_channel_id` (`payment_channel_id`),
  CONSTRAINT `fk_finances_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_finances_payment_channel` FOREIGN KEY (`payment_channel_id`) REFERENCES `payment_channel` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_finances_treatment` FOREIGN KEY (`treatment_id`) REFERENCES `treatment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `insurance_config`
--

DROP TABLE IF EXISTS `insurance_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insurance_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform_code` varchar(64) NOT NULL COMMENT '医保平台编码',
  `platform_name` varchar(100) NOT NULL COMMENT '医保平台名称',
  `api_base_url` varchar(255) DEFAULT NULL COMMENT '接口基础地址',
  `org_code` varchar(64) DEFAULT NULL COMMENT '机构编码',
  `org_name` varchar(100) DEFAULT NULL COMMENT '机构名称',
  `app_id` varchar(128) DEFAULT NULL COMMENT '应用ID',
  `app_secret` varchar(255) DEFAULT NULL COMMENT '应用密钥',
  `sign_key` varchar(255) DEFAULT NULL COMMENT '签名密钥',
  `encryption_type` varchar(50) DEFAULT NULL COMMENT '加密方式',
  `region_code` varchar(64) DEFAULT NULL COMMENT '统筹区编码',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用',
  `ext_json` text COMMENT '扩展配置JSON',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保平台配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `insurance_operation_log`
--

DROP TABLE IF EXISTS `insurance_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insurance_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operation_type` varchar(64) NOT NULL COMMENT '操作类型',
  `ref_type` varchar(64) DEFAULT NULL COMMENT '关联业务类型',
  `ref_id` varchar(64) DEFAULT NULL COMMENT '关联业务ID',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求地址',
  `request_method` varchar(20) DEFAULT NULL COMMENT '请求方法',
  `request_payload` mediumtext COMMENT '请求报文',
  `response_payload` mediumtext COMMENT '响应报文',
  `response_code` varchar(64) DEFAULT NULL COMMENT '响应编码',
  `response_message` varchar(255) DEFAULT NULL COMMENT '响应信息',
  `status` varchar(32) DEFAULT NULL COMMENT '执行状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_insurance_log_operation` (`operation_type`),
  KEY `idx_insurance_log_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保接口操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `insurance_patient_profile`
--

DROP TABLE IF EXISTS `insurance_patient_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insurance_patient_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `insurance_person_no` varchar(64) DEFAULT NULL COMMENT '医保人员编号',
  `id_card_no` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `insured_region_code` varchar(64) DEFAULT NULL COMMENT '参保地编码',
  `insured_type` varchar(64) DEFAULT NULL COMMENT '参保类型',
  `card_no` varchar(64) DEFAULT NULL COMMENT '医保卡号',
  `card_type` varchar(32) DEFAULT NULL COMMENT '卡类型',
  `person_name` varchar(100) DEFAULT NULL COMMENT '医保登记姓名',
  `gender` varchar(16) DEFAULT NULL COMMENT '性别',
  `birthday` varchar(20) DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1有效 0停用',
  `last_auth_no` varchar(64) DEFAULT NULL COMMENT '最近认证流水号',
  `last_verified_at` datetime DEFAULT NULL COMMENT '最近校验时间',
  `ext_json` text COMMENT '扩展字段JSON',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_insurance_patient` (`patient_id`),
  KEY `idx_insurance_person_no` (`insurance_person_no`),
  CONSTRAINT `fk_insurance_patient_profile_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者医保档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `insurance_settlement`
--

DROP TABLE IF EXISTS `insurance_settlement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insurance_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `finance_id` int DEFAULT NULL,
  `treatment_id` bigint DEFAULT NULL COMMENT '治疗记录ID',
  `settlement_no` varchar(64) DEFAULT NULL COMMENT '医保结算单号',
  `visit_no` varchar(64) DEFAULT NULL COMMENT '就诊流水号',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型',
  `settlement_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '结算状态',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `insurance_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '医保支付金额',
  `personal_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '个人账户/自付金额',
  `cash_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '现金金额',
  `upload_status` varchar(32) NOT NULL DEFAULT 'NOT_UPLOADED' COMMENT '上传状态',
  `upload_payload` mediumtext COMMENT '上传报文',
  `response_payload` mediumtext COMMENT '返回报文',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_insurance_settlement_patient` (`patient_id`),
  KEY `idx_insurance_settlement_status` (`settlement_status`),
  KEY `fk_insurance_settlement_finance` (`finance_id`),
  KEY `fk_insurance_settlement_treatment` (`treatment_id`),
  CONSTRAINT `fk_insurance_settlement_finance` FOREIGN KEY (`finance_id`) REFERENCES `finances` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_insurance_settlement_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_insurance_settlement_treatment` FOREIGN KEY (`treatment_id`) REFERENCES `treatment` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保结算记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供应商',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `quantity` int DEFAULT '0' COMMENT '库存数量',
  `selectedQuantity` int DEFAULT '0' COMMENT '已选数量',
  `price` varchar(50) DEFAULT NULL COMMENT '价格',
  `product_batch` varchar(50) DEFAULT NULL COMMENT '产品批次',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_category` (`category`),
  KEY `idx_supplier` (`supplier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_bill_items`
--

DROP TABLE IF EXISTS `lab_bill_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_bill_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID',
  `raw_row_number` int DEFAULT NULL COMMENT '原始Excel行号',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名称',
  `product_spec` varchar(100) DEFAULT NULL COMMENT '产品规格',
  `quantity` int DEFAULT '0' COMMENT '数量',
  `unit_price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '金额',
  `delivery_date` date DEFAULT NULL COMMENT '送货日期',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `match_status` varchar(20) NOT NULL DEFAULT '仅账单有' COMMENT '匹配状态：完全匹配/数量不符/金额不符/仅账单有',
  `matched_lab_order_id` bigint DEFAULT NULL COMMENT '匹配到的系统订单ID',
  `resolution_status` varchar(20) NOT NULL DEFAULT '待处理' COMMENT '异常处理状态：待处理/已处理/已忽略/无需处理',
  `resolution_remark` varchar(200) DEFAULT NULL COMMENT '处理备注',
  `resolved_by` bigint DEFAULT NULL COMMENT '处理人ID',
  `resolved_by_name` varchar(50) DEFAULT NULL COMMENT '处理人姓名',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_bill_items_bill_id` (`bill_id`),
  KEY `idx_lab_bill_items_match_status` (`match_status`),
  KEY `idx_lab_bill_items_matched_order` (`matched_lab_order_id`),
  KEY `idx_lab_bill_items_resolution_status` (`resolution_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿账单条目';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_bill_templates`
--

DROP TABLE IF EXISTS `lab_bill_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_bill_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `factory_id` bigint NOT NULL COMMENT '加工厂ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `column_mapping` text NOT NULL COMMENT '列映射JSON',
  `header_row` int NOT NULL DEFAULT '1' COMMENT '表头行号',
  `data_start_row` int NOT NULL DEFAULT '2' COMMENT '数据起始行号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_bill_templates_factory_id` (`factory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿账单模板配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_bill_unmatched_orders`
--

DROP TABLE IF EXISTS `lab_bill_unmatched_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_bill_unmatched_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID',
  `lab_order_id` bigint NOT NULL COMMENT '系统订单ID',
  `resolution_status` varchar(20) NOT NULL DEFAULT '待处理' COMMENT '异常处理状态：待处理/已处理/已忽略',
  `resolution_remark` varchar(200) DEFAULT NULL COMMENT '处理备注',
  `resolved_by` bigint DEFAULT NULL COMMENT '处理人ID',
  `resolved_by_name` varchar(50) DEFAULT NULL COMMENT '处理人姓名',
  `resolved_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_bill_unmatched_orders_bill_id` (`bill_id`),
  KEY `idx_lab_bill_unmatched_orders_order_id` (`lab_order_id`),
  KEY `idx_lab_bill_unmatched_orders_resolution_status` (`resolution_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿账单仅系统有订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_bills`
--

DROP TABLE IF EXISTS `lab_bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_bills` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `factory_id` bigint NOT NULL COMMENT '加工厂ID',
  `factory_name` varchar(100) NOT NULL COMMENT '加工厂名称冗余',
  `template_id` bigint DEFAULT NULL COMMENT '使用模板ID',
  `bill_month` varchar(7) NOT NULL COMMENT '账单月份YYYY-MM',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账单总金额',
  `bill_file_url` varchar(255) DEFAULT NULL COMMENT '原始文件路径',
  `status` varchar(20) NOT NULL DEFAULT '待对账' COMMENT '状态：待对账/对账中/已完成对账',
  `matched_count` int NOT NULL DEFAULT '0' COMMENT '完全匹配条数',
  `mismatched_count` int NOT NULL DEFAULT '0' COMMENT '数量/金额不符条数',
  `only_in_system_count` int NOT NULL DEFAULT '0' COMMENT '仅系统有条数',
  `only_in_bill_count` int NOT NULL DEFAULT '0' COMMENT '仅账单有条数',
  `imported_by` bigint DEFAULT NULL COMMENT '导入人ID',
  `imported_by_name` varchar(50) DEFAULT NULL COMMENT '导入人姓名',
  `imported_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
  `confirmed_by` bigint DEFAULT NULL COMMENT '确认人ID',
  `confirmed_by_name` varchar(50) DEFAULT NULL COMMENT '确认人姓名',
  `confirmed_at` datetime DEFAULT NULL COMMENT '确认时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_bills_factory_month` (`factory_id`,`bill_month`),
  KEY `idx_lab_bills_status` (`status`),
  KEY `idx_lab_bills_bill_month` (`bill_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿月度账单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_factories`
--

DROP TABLE IF EXISTS `lab_factories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_factories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '加工厂名称',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(30) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `cooperation_start_date` date DEFAULT NULL COMMENT '合作开始日期',
  `status` varchar(20) NOT NULL DEFAULT '合作中' COMMENT '状态：合作中/已停止合作',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_factories_name` (`name`),
  KEY `idx_lab_factories_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿加工厂档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_factory_products`
--

DROP TABLE IF EXISTS `lab_factory_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_factory_products` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `factory_id` bigint NOT NULL COMMENT '加工厂ID',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_spec` varchar(100) DEFAULT NULL COMMENT '产品规格',
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `status` varchar(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_factory_products_factory_id` (`factory_id`),
  KEY `idx_lab_factory_products_name` (`product_name`),
  KEY `idx_lab_factory_products_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='加工厂产品价格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_orders`
--

DROP TABLE IF EXISTS `lab_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `factory_id` bigint NOT NULL COMMENT '加工厂ID',
  `factory_name` varchar(100) NOT NULL COMMENT '加工厂名称冗余',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名冗余',
  `treatment_id` bigint DEFAULT NULL COMMENT '关联治疗ID',
  `medical_record_operation_id` bigint DEFAULT NULL COMMENT '关联病历操作记录ID',
  `medical_record_id` bigint DEFAULT NULL COMMENT '关联病历ID冗余',
  `project_id` bigint DEFAULT NULL COMMENT '项目库ID冗余',
  `project_name` varchar(100) DEFAULT NULL COMMENT '项目名称冗余',
  `operation_id` bigint DEFAULT NULL COMMENT '操作字典ID冗余',
  `operation_name` varchar(100) DEFAULT NULL COMMENT '操作名称冗余',
  `tooth_positions` varchar(255) DEFAULT NULL COMMENT '牙位冗余',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_spec` varchar(100) DEFAULT NULL COMMENT '产品规格',
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `order_date` date NOT NULL COMMENT '下单日期',
  `expected_delivery_date` date DEFAULT NULL COMMENT '预计完成日期',
  `actual_delivery_date` date DEFAULT NULL COMMENT '实际收货日期',
  `status` varchar(20) NOT NULL DEFAULT '已下单' COMMENT '状态：已下单/加工中/已完成/已收货/已对账',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_lab_orders_factory_id` (`factory_id`),
  KEY `idx_lab_orders_patient_id` (`patient_id`),
  KEY `idx_lab_orders_treatment_id` (`treatment_id`),
  KEY `idx_lab_orders_status` (`status`),
  KEY `idx_lab_orders_order_date` (`order_date`),
  KEY `idx_lab_orders_expected_delivery_date` (`expected_delivery_date`),
  KEY `idx_lab_orders_actual_delivery_date` (`actual_delivery_date`),
  KEY `idx_lab_orders_patient_name` (`patient_name`),
  KEY `idx_lab_orders_product_name` (`product_name`),
  KEY `idx_lab_orders_medical_record_operation_id` (`medical_record_operation_id`),
  KEY `idx_lab_orders_medical_record_id` (`medical_record_id`),
  KEY `idx_lab_orders_project_id` (`project_id`),
  KEY `idx_lab_orders_operation_id` (`operation_id`),
  CONSTRAINT `fk_lab_orders_medical_record` FOREIGN KEY (`medical_record_id`) REFERENCES `medical_records` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_mro` FOREIGN KEY (`medical_record_operation_id`) REFERENCES `medical_record_operations` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_operation` FOREIGN KEY (`operation_id`) REFERENCES `treatment_operations` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `fk_lab_orders_project` FOREIGN KEY (`project_id`) REFERENCES `treatment_projects` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_treatment` FOREIGN KEY (`treatment_id`) REFERENCES `treatment` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿加工订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `material_categories`
--

DROP TABLE IF EXISTS `material_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID，0表示一级分类',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` varchar(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用/已删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_categories_parent_id` (`parent_id`),
  KEY `idx_material_categories_status` (`status`),
  KEY `idx_material_categories_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `material_purchase_items`
--

DROP TABLE IF EXISTS `material_purchase_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_purchase_items` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `purchase_id` bigint NOT NULL COMMENT '采购单ID',
  `material_id` bigint NOT NULL COMMENT '耗材ID',
  `material_name` varchar(100) DEFAULT NULL COMMENT '耗材名称冗余',
  `material_spec` varchar(100) DEFAULT NULL COMMENT '耗材规格冗余',
  `unit_price` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '数量',
  `subtotal` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '小计',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_purchase_items_purchase_id` (`purchase_id`),
  KEY `idx_material_purchase_items_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材采购单明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `material_purchases`
--

DROP TABLE IF EXISTS `material_purchases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_purchases` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_name` varchar(100) DEFAULT NULL COMMENT '供应商名称',
  `purchase_date` date NOT NULL COMMENT '采购日期',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '付款方式',
  `invoice_image_url` varchar(255) DEFAULT NULL COMMENT '发票/采购单图片',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `finance_record_id` bigint DEFAULT NULL COMMENT '关联财务记录ID',
  `status` varchar(20) NOT NULL DEFAULT '有效' COMMENT '状态：有效/已作废',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `voided_by` bigint DEFAULT NULL COMMENT '作废人ID',
  `voided_by_name` varchar(50) DEFAULT NULL COMMENT '作废人姓名',
  `voided_at` datetime DEFAULT NULL COMMENT '作废时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_purchases_purchase_date` (`purchase_date`),
  KEY `idx_material_purchases_supplier_name` (`supplier_name`),
  KEY `idx_material_purchases_status` (`status`),
  KEY `idx_material_purchases_finance_record_id` (`finance_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材采购单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `materials`
--

DROP TABLE IF EXISTS `materials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `materials` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '耗材名称',
  `spec` varchar(100) DEFAULT NULL COMMENT '规格',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称冗余',
  `unit` varchar(20) DEFAULT NULL COMMENT '计量单位',
  `min_stock_alert` int NOT NULL DEFAULT '0' COMMENT '最低库存预警值',
  `current_stock` int NOT NULL DEFAULT '0' COMMENT '当前库存',
  `status` varchar(20) NOT NULL DEFAULT '在用' COMMENT '状态：在用/停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_materials_category_id` (`category_id`),
  KEY `idx_materials_status` (`status`),
  KEY `idx_materials_name` (`name`),
  KEY `idx_materials_brand` (`brand`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medical_record_operations`
--

DROP TABLE IF EXISTS `medical_record_operations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record_operations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `medical_record_id` bigint NOT NULL COMMENT '病历ID',
  `project_id` bigint DEFAULT NULL COMMENT '所属项目ID',
  `project_name` varchar(100) DEFAULT NULL COMMENT '所属项目名称冗余',
  `operation_id` bigint NOT NULL COMMENT '操作字典ID',
  `operation_name` varchar(100) NOT NULL COMMENT '操作名称冗余',
  `factory_id` bigint DEFAULT NULL COMMENT '目标加工厂ID',
  `factory_name` varchar(100) DEFAULT NULL COMMENT '目标加工厂名称冗余',
  `tooth_positions` varchar(255) DEFAULT NULL COMMENT '牙位，可空',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注，可空',
  `lab_order_status` tinyint NOT NULL DEFAULT '0' COMMENT '加工登记状态：0未登记1已登记2本次跳过',
  `skip_reason` varchar(100) DEFAULT NULL COMMENT '跳过原因',
  `lab_order_registered_at` datetime DEFAULT NULL COMMENT '首次登记加工时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_mro_medical_record_id` (`medical_record_id`),
  KEY `idx_mro_project_id` (`project_id`),
  KEY `idx_mro_operation_id` (`operation_id`),
  KEY `idx_mro_lab_order_status` (`lab_order_status`),
  KEY `idx_mro_pending_lab` (`lab_order_status`,`created_at`),
  KEY `idx_mro_medical_record_project` (`medical_record_id`,`project_id`),
  KEY `idx_mro_factory_id` (`factory_id`),
  CONSTRAINT `fk_mro_medical_record` FOREIGN KEY (`medical_record_id`) REFERENCES `medical_records` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历操作记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medical_record_template`
--

DROP TABLE IF EXISTS `medical_record_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_category` varchar(64) DEFAULT '常用模板' COMMENT '模板分类',
  `chief_complaint` varchar(500) DEFAULT NULL COMMENT '主诉',
  `present_illness_history` text COMMENT '现病史',
  `past_history` text COMMENT '既往史',
  `infectious_history` text COMMENT '流行病史',
  `allergy_history` text COMMENT '过敏史',
  `general_condition` varchar(255) DEFAULT NULL COMMENT '一般情况',
  `examination` text COMMENT '检查',
  `auxiliary_examination` text COMMENT '辅助检查',
  `diagnosis` text COMMENT '诊断',
  `treatment_plan` text COMMENT '治疗方案',
  `treatment` text COMMENT '治疗文稿',
  `tooth_positions` varchar(255) DEFAULT NULL COMMENT '牙位',
  `medical_advice` text COMMENT '医嘱',
  `prescription` text COMMENT '处方',
  `record_tags` varchar(255) DEFAULT NULL COMMENT '病历标签',
  `image_summary` text COMMENT '影像说明',
  `notes` text COMMENT '备注',
  `record_type` varchar(20) DEFAULT '初诊' COMMENT '病历类型',
  `operation_items_json` longtext COMMENT '结构化操作JSON',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `created_by` bigint DEFAULT NULL COMMENT '创建人账号ID',
  `created_by_name` varchar(64) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_medical_record_template_status` (`status`,`id`),
  KEY `idx_medical_record_template_creator` (`created_by`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历模板库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medical_records`
--

DROP TABLE IF EXISTS `medical_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名（冗余）',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '医生账号ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '医生姓名',
  `nurse_name` varchar(64) DEFAULT NULL COMMENT '护士',
  `assistant_name` varchar(64) DEFAULT NULL COMMENT '助理',
  `visit_date` datetime DEFAULT NULL COMMENT '就诊日期',
  `record_type` varchar(20) DEFAULT '初诊' COMMENT '病历类型：初诊/复诊',
  `chief_complaint` text COMMENT '主诉',
  `present_illness_history` text COMMENT '现病史',
  `past_history` text COMMENT '既往史',
  `infectious_history` text COMMENT '流行病史',
  `allergy_history` text COMMENT '过敏史',
  `general_condition` varchar(255) DEFAULT NULL COMMENT '一般情况',
  `examination` text COMMENT '检查',
  `auxiliary_examination` text COMMENT '辅助检查',
  `diagnosis` text COMMENT '诊断',
  `treatment_plan` text COMMENT '治疗方案',
  `treatment` text COMMENT '治疗',
  `tooth_positions` varchar(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔',
  `medical_advice` text COMMENT '医嘱',
  `prescription` text COMMENT '处方',
  `record_tags` varchar(255) DEFAULT NULL COMMENT '病历标签',
  `image_summary` text COMMENT '影像说明',
  `notes` text COMMENT '备注',
  `record_status` varchar(20) DEFAULT 'final' COMMENT '保存状态：draft/final',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_visit_date` (`visit_date`),
  KEY `idx_medical_records_doctor_account_id` (`doctor_account_id`),
  CONSTRAINT `fk_medical_records_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_consent`
--

DROP TABLE IF EXISTS `patient_consent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_consent` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '医生账号ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '医生姓名',
  `title` varchar(100) NOT NULL COMMENT '同意书标题',
  `content` text NOT NULL COMMENT '同意书正文快照',
  `status` varchar(20) NOT NULL DEFAULT '待签署' COMMENT '状态：待签署/已签署',
  `issued_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下发时间',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `signed_at` datetime DEFAULT NULL COMMENT '签署时间',
  `signature_name` varchar(50) DEFAULT NULL COMMENT '签署姓名',
  `signature_data` longtext COMMENT '签名图片(base64)',
  `signature_remark` varchar(500) DEFAULT NULL COMMENT '签署备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_consent_patient_id` (`patient_id`),
  KEY `idx_patient_consent_status` (`status`),
  KEY `idx_patient_consent_doctor_account_id` (`doctor_account_id`),
  KEY `idx_patient_consent_signed_at` (`signed_at`),
  CONSTRAINT `fk_patient_consent_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者电子知情同意书';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_custom_group`
--

DROP TABLE IF EXISTS `patient_custom_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_custom_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_key` varchar(64) NOT NULL COMMENT '分组键',
  `group_name` varchar(50) NOT NULL COMMENT '分组名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用1启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序值',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_custom_group_key` (`group_key`),
  KEY `idx_patient_custom_group_status_sort` (`status`,`sort_order`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者自定义分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_custom_group_member`
--

DROP TABLE IF EXISTS `patient_custom_group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_custom_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint NOT NULL COMMENT '分组ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_custom_group_member` (`group_id`,`patient_id`),
  KEY `idx_patient_custom_group_member_patient_id` (`patient_id`),
  CONSTRAINT `fk_patient_custom_group_member_group_id` FOREIGN KEY (`group_id`) REFERENCES `patient_custom_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者自定义分组成员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_followup`
--

DROP TABLE IF EXISTS `patient_followup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_followup` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '负责医生账号ID',
  `doctor_name` varchar(100) DEFAULT NULL COMMENT '负责医生姓名',
  `followup_date` datetime DEFAULT NULL COMMENT '随访时间',
  `followup_type` varchar(50) DEFAULT NULL COMMENT '随访类型：电话/复诊/线上',
  `summary` varchar(500) DEFAULT NULL COMMENT '随访摘要',
  `next_followup_date` datetime DEFAULT NULL COMMENT '下次随访时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_followup_date` (`followup_date`),
  KEY `idx_next_followup_date` (`next_followup_date`),
  KEY `idx_followup_doctor_account_id` (`doctor_account_id`),
  CONSTRAINT `fk_patient_followup_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者随访记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_images`
--

DROP TABLE IF EXISTS `patient_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_images` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `patient_name` varchar(100) DEFAULT NULL COMMENT '患者姓名',
  `image_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `image_type` varchar(50) DEFAULT NULL COMMENT '影像类型：X光/CT/口内照/其他',
  `image_date` datetime DEFAULT NULL COMMENT '拍摄日期',
  `file_path` varchar(500) DEFAULT NULL COMMENT '存储文件名',
  `notes` varchar(500) DEFAULT NULL COMMENT '备注',
  `sent_to_patient` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已发送给患者',
  `sent_at` datetime DEFAULT NULL COMMENT '发送时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_patient_images_sent` (`patient_id`,`sent_to_patient`),
  CONSTRAINT `fk_patient_images_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者影像记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_insight_summary`
--

DROP TABLE IF EXISTS `patient_insight_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_insight_summary` (
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `last_visit_date` datetime DEFAULT NULL COMMENT '最近到店时间',
  `total_visit_count` int NOT NULL DEFAULT '0' COMMENT '累计到店次数',
  `total_spent` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计净消费',
  `last_treatment_date` date DEFAULT NULL COMMENT '最近治疗日期',
  `visit_count_last_6m` int NOT NULL DEFAULT '0' COMMENT '近6月到店次数',
  `high_value_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '高价值客户标记',
  `lost_risk_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '流失风险标记',
  `referred_count` int NOT NULL DEFAULT '0' COMMENT '累计转介绍人数',
  `referred_revenue` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '转介绍累计净消费',
  `word_of_mouth_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '口碑客户标记',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`patient_id`),
  KEY `idx_pis_last_visit_date` (`last_visit_date`),
  KEY `idx_pis_total_spent` (`total_spent`),
  KEY `idx_pis_high_value_flag` (`high_value_flag`),
  KEY `idx_pis_lost_risk_flag` (`lost_risk_flag`),
  KEY `idx_pis_word_of_mouth_flag` (`word_of_mouth_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者洞察汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_referral_records`
--

DROP TABLE IF EXISTS `patient_referral_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_referral_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '被介绍患者ID',
  `consultation_record_id` bigint DEFAULT NULL COMMENT '关联咨询记录ID',
  `referrer_type` varchar(20) DEFAULT NULL COMMENT '介绍人类型：patient/external',
  `referrer_patient_id` bigint DEFAULT NULL COMMENT '介绍患者ID',
  `referrer_patient_name` varchar(50) DEFAULT NULL COMMENT '介绍患者姓名',
  `external_referrer_type` varchar(30) DEFAULT NULL COMMENT '外部介绍人类型',
  `external_referrer_name` varchar(50) DEFAULT NULL COMMENT '外部介绍人姓名',
  `external_referrer_contact` varchar(50) DEFAULT NULL COMMENT '外部介绍人联系方式',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '录入人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '录入人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_referral_patient_id` (`patient_id`),
  KEY `idx_referrer_patient_id` (`referrer_patient_id`),
  KEY `idx_referral_created_at` (`created_at`),
  KEY `idx_referral_consultation_id` (`consultation_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者转介绍记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_risk_tag`
--

DROP TABLE IF EXISTS `patient_risk_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_risk_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `tag_code` varchar(64) NOT NULL COMMENT '风险标签编码',
  `tag_name` varchar(100) NOT NULL COMMENT '风险标签名称',
  `risk_level` tinyint DEFAULT '1' COMMENT '风险等级：1低 2中 3高',
  `source` varchar(100) DEFAULT NULL COMMENT '标签来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1有效 0失效',
  `note` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_risk_patient_tag` (`patient_id`,`tag_code`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_risk_status` (`status`),
  CONSTRAINT `fk_patient_risk_tag_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者风险标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_timeline`
--

DROP TABLE IF EXISTS `patient_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_timeline` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `event_time` datetime DEFAULT NULL COMMENT '事件时间',
  `event_type` varchar(50) DEFAULT NULL COMMENT '事件类型',
  `event_title` varchar(200) DEFAULT NULL COMMENT '事件标题',
  `event_content` text COMMENT '事件内容',
  `source_table` varchar(100) DEFAULT NULL COMMENT '来源表',
  `source_id` bigint DEFAULT NULL COMMENT '来源记录ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_event_time` (`event_time`),
  CONSTRAINT `fk_patient_timeline_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者时间线';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patient_wechat_bind_scene`
--

DROP TABLE IF EXISTS `patient_wechat_bind_scene`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_wechat_bind_scene` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `scene_key` varchar(128) NOT NULL,
  `qr_ticket` varchar(255) DEFAULT NULL,
  `qr_url` varchar(512) DEFAULT NULL,
  `expire_seconds` int DEFAULT NULL,
  `status` varchar(32) DEFAULT 'pending',
  `bound_at` datetime DEFAULT NULL,
  `bound_openid` varchar(100) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_key` (`scene_key`),
  KEY `idx_patient_wechat_bind_scene_patient_id` (`patient_id`),
  CONSTRAINT `fk_patient_wechat_bind_scene_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '患者姓名',
  `name_pinyin` varchar(200) DEFAULT NULL COMMENT '姓名全拼搜索字段',
  `name_initials` varchar(80) DEFAULT NULL COMMENT '姓名首拼搜索字段',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `date_of_birth` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `relation_type` varchar(30) DEFAULT NULL COMMENT '患者关系类型',
  `related_patient_id` int DEFAULT NULL COMMENT '关联患者ID',
  `related_patient_name` varchar(50) DEFAULT NULL COMMENT '关联患者姓名',
  `wechat_openid` varchar(100) DEFAULT NULL COMMENT '微信 openid',
  `customer_source` varchar(30) DEFAULT NULL COMMENT '客户来源',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_wechat_openid` (`wechat_openid`),
  KEY `idx_patients_customer_source` (`customer_source`),
  KEY `idx_patients_created_at` (`created_at`),
  KEY `idx_patients_name_pinyin` (`name_pinyin`),
  KEY `idx_patients_name_initials` (`name_initials`),
  KEY `fk_patients_related_patient` (`related_patient_id`),
  CONSTRAINT `fk_patients_related_patient` FOREIGN KEY (`related_patient_id`) REFERENCES `patients` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_channel`
--

DROP TABLE IF EXISTS `payment_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_name` varchar(100) NOT NULL COMMENT '收款渠道名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收款渠道表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `portal_access_token`
--

DROP TABLE IF EXISTS `portal_access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portal_access_token` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `token` varchar(128) NOT NULL COMMENT '访问令牌',
  `token_type` varchar(64) NOT NULL COMMENT '令牌类型',
  `subject_id` bigint DEFAULT NULL COMMENT '主体ID，如patient/account',
  `payload` varchar(1000) DEFAULT NULL COMMENT '附加数据',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `consumed_at` datetime DEFAULT NULL COMMENT '消费时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_portal_access_token` (`token`),
  KEY `idx_portal_access_token_type` (`token_type`),
  KEY `idx_portal_access_token_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门户访问令牌';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_operation_relations`
--

DROP TABLE IF EXISTS `project_operation_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_operation_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `operation_id` bigint NOT NULL COMMENT '操作ID',
  `operation_order` int NOT NULL DEFAULT '0' COMMENT '项目内操作顺序',
  `is_required` tinyint NOT NULL DEFAULT '1' COMMENT '是否必经：0否1是',
  `performance_weight` decimal(10,4) NOT NULL DEFAULT '1.0000' COMMENT '业绩权重，0=不参与业绩',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_operation_relations` (`project_id`,`operation_id`),
  KEY `idx_project_operation_relations_operation_id` (`operation_id`),
  KEY `idx_project_operation_relations_order` (`project_id`,`operation_order`),
  CONSTRAINT `fk_por_operation` FOREIGN KEY (`operation_id`) REFERENCES `treatment_operations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_por_project` FOREIGN KEY (`project_id`) REFERENCES `treatment_projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目-操作关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchases`
--

DROP TABLE IF EXISTS `purchases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchases` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品名',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
  `supplier` varchar(100) DEFAULT NULL COMMENT '供应商',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `quantity` int DEFAULT '0' COMMENT '采购数量',
  `price` varchar(50) DEFAULT NULL COMMENT '价格',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `createdate` datetime DEFAULT NULL COMMENT '创建日期',
  `purchasedate` datetime DEFAULT NULL COMMENT '采购日期',
  `indate` datetime DEFAULT NULL COMMENT '入库日期',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_category` (`category`),
  KEY `idx_supplier` (`supplier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_menu_permissions`
--

DROP TABLE IF EXISTS `role_menu_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_menu_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(20) NOT NULL COMMENT '角色编码：admin/doctor/nurse',
  `menu_key` varchar(64) NOT NULL COMMENT '导航键，使用前端路径标识',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用：0否1是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu_permissions_role_menu` (`role_code`,`menu_key`),
  KEY `idx_role_menu_permissions_role_code` (`role_code`),
  KEY `idx_role_menu_permissions_menu_key` (`menu_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色导航权限配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment`
--

DROP TABLE IF EXISTS `treatment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '同次处置批次号',
  `medical_record_id` bigint DEFAULT NULL COMMENT '来源病历ID',
  `project_id` bigint DEFAULT NULL COMMENT '项目库ID',
  `appointment_purpose` varchar(255) DEFAULT NULL COMMENT '预约目的',
  `status` varchar(20) DEFAULT NULL COMMENT '治疗状态',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '医生账号ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '医生姓名',
  `treatment_date` date DEFAULT NULL COMMENT '治疗日期',
  `treatment_content` text COMMENT '治疗内容',
  `tooth_positions` varchar(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔',
  `treatment_product` varchar(500) DEFAULT NULL COMMENT '使用材料',
  `treatment_fee` varchar(50) DEFAULT NULL COMMENT '治疗费用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_patient_name` (`patient_name`),
  KEY `idx_treatment_date` (`treatment_date`),
  KEY `idx_treatment_patient_id` (`patient_id`),
  KEY `idx_treatment_doctor_account_id` (`doctor_account_id`),
  KEY `idx_treatment_batch_no` (`batch_no`),
  KEY `idx_treatment_project_id` (`project_id`),
  KEY `idx_treatment_medical_record_id` (`medical_record_id`),
  CONSTRAINT `fk_treatment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `fk_treatment_project` FOREIGN KEY (`project_id`) REFERENCES `treatment_projects` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_catalog`
--

DROP TABLE IF EXISTS `treatment_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_catalog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_name` varchar(100) NOT NULL COMMENT '处置收费项目名称',
  `default_fee` varchar(50) DEFAULT NULL COMMENT '默认收费',
  `default_content` varchar(500) DEFAULT NULL COMMENT '默认治疗内容',
  `default_product` varchar(255) DEFAULT NULL COMMENT '默认使用材料',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用 0停用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `medical_insurance_code` varchar(64) DEFAULT NULL COMMENT '医保项目编码',
  `medical_insurance_name` varchar(100) DEFAULT NULL COMMENT '医保项目名称',
  `medical_insurance_category` varchar(50) DEFAULT NULL COMMENT '医保分类：甲类/乙类/丙类/自费',
  `self_pay_ratio` decimal(10,4) DEFAULT NULL COMMENT '自付比例',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处置收费项目库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_operation_allocations`
--

DROP TABLE IF EXISTS `treatment_operation_allocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_operation_allocations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `treatment_id` bigint NOT NULL COMMENT '治疗记录ID',
  `medical_record_id` bigint DEFAULT NULL COMMENT '来源病历ID',
  `medical_record_operation_id` bigint DEFAULT NULL COMMENT '来源病历操作ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '业绩归属医生ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '业绩归属医生姓名',
  `project_id` bigint DEFAULT NULL COMMENT '项目ID',
  `project_name` varchar(100) DEFAULT NULL COMMENT '项目名称冗余',
  `operation_id` bigint DEFAULT NULL COMMENT '操作字典ID',
  `operation_name` varchar(100) DEFAULT NULL COMMENT '操作名称冗余',
  `performance_weight` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '业绩权重快照',
  `allocation_ratio` decimal(12,6) NOT NULL DEFAULT '0.000000' COMMENT '分摊比例快照',
  `allocated_turnover_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '折后产值分摊金额',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_toa_treatment_id` (`treatment_id`),
  KEY `idx_toa_doctor_account_id` (`doctor_account_id`),
  KEY `idx_toa_medical_record_id` (`medical_record_id`),
  KEY `idx_toa_medical_record_operation_id` (`medical_record_operation_id`),
  KEY `idx_toa_project_id` (`project_id`),
  KEY `idx_toa_operation_id` (`operation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗记录-病历操作业绩分摊快照';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_operations`
--

DROP TABLE IF EXISTS `treatment_operations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_operations` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operation_code` varchar(64) NOT NULL COMMENT '操作编码',
  `operation_name` varchar(100) NOT NULL COMMENT '操作名称',
  `operation_category` varchar(100) NOT NULL DEFAULT '' COMMENT '操作大类',
  `need_lab_processing` tinyint NOT NULL DEFAULT '0' COMMENT '是否触发外加工：0否1是',
  `default_processing_days` int NOT NULL DEFAULT '0' COMMENT '默认加工天数',
  `status` varchar(20) NOT NULL DEFAULT '在用' COMMENT '状态：在用/停用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_operations_code` (`operation_code`),
  KEY `idx_treatment_operations_name` (`operation_name`),
  KEY `idx_treatment_operations_category` (`operation_category`),
  KEY `idx_treatment_operations_need_lab` (`need_lab_processing`),
  KEY `idx_treatment_operations_status` (`status`),
  KEY `idx_treatment_operations_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗操作字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_plans`
--

DROP TABLE IF EXISTS `treatment_plans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_plans` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `treatment_content` varchar(500) DEFAULT NULL COMMENT '治疗内容',
  `treatment_free` varchar(50) DEFAULT NULL COMMENT '治疗费用（原代码字段名）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗方案表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_project_categories`
--

DROP TABLE IF EXISTS `treatment_project_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_project_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID，0=一级分类',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` varchar(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用/已删除',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_project_categories_parent_name` (`parent_id`,`name`),
  KEY `idx_treatment_project_categories_parent_id` (`parent_id`),
  KEY `idx_treatment_project_categories_status` (`status`),
  KEY `idx_treatment_project_categories_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `treatment_projects`
--

DROP TABLE IF EXISTS `treatment_projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_projects` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `legacy_treatment_catalog_id` bigint DEFAULT NULL COMMENT '历史treatment_catalog ID',
  `project_code` varchar(64) NOT NULL COMMENT '项目编码',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `category_id` bigint DEFAULT NULL COMMENT '所属分类ID',
  `category_path` varchar(200) DEFAULT NULL COMMENT '分类路径',
  `default_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '默认价格',
  `estimated_visit_count` int NOT NULL DEFAULT '1' COMMENT '预计治疗次数',
  `estimated_cycle_days` int NOT NULL DEFAULT '0' COMMENT '预计周期天数',
  `status` varchar(20) NOT NULL DEFAULT '在用' COMMENT '状态：在用/停用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `updated_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_projects_code` (`project_code`),
  KEY `idx_treatment_projects_legacy_catalog_id` (`legacy_treatment_catalog_id`),
  KEY `idx_treatment_projects_category_id` (`category_id`),
  KEY `idx_treatment_projects_status` (`status`),
  KEY `idx_treatment_projects_sort_order` (`sort_order`),
  KEY `idx_treatment_projects_name` (`project_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '登录用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '登录密码',
  `name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(30) DEFAULT NULL COMMENT '角色：admin/doctor/nurse等',
  `wechat_openid` varchar(100) DEFAULT NULL COMMENT '微信 openid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_wechat_openid` (`wechat_openid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'clinic_system'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-06 22:35:50

-- ============================================================
-- 数据库补丁：新增 AI / 系统运维 / 业务闭环所需的表和字段
-- ============================================================

-- ============================================================
-- 口腔门诊 SaaS 数据库结构补丁
-- 用途：补齐 AI Agent、系统运维、业务闭环所需的新表和新字段
-- 执行顺序：直接执行或在 Flyway 中作为 V33~V38 分批执行
-- 兼容：MySQL 8.0+
-- ============================================================

-- --------------------------------------------------------
-- V33: 系统全局配置 + 安全加固
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(2000) DEFAULT NULL COMMENT '配置值',
  `config_type` varchar(20) DEFAULT 'string' COMMENT '值类型：string/int/boolean/json',
  `description` varchar(500) DEFAULT NULL COMMENT '配置说明',
  `category` varchar(50) DEFAULT 'general' COMMENT '分类：security/ai/appointment/wechat',
  `editable` tinyint DEFAULT 1 COMMENT '是否可编辑：0-只读 1-可编辑',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_config_key` (`config_key`),
  KEY `idx_system_config_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统全局配置表';

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `category`, `editable`) VALUES
('security.secondary_password', '246810', 'string', '患者敏感操作二级密码', 'security', 1),
('ai.enabled', 'false', 'boolean', 'AI 功能总开关', 'ai', 1),
('ai.model.default', 'gpt-5.4-mini', 'string', '默认 AI 模型', 'ai', 1),
('appointment.reminder.minutes', '30', 'int', '预约到达前提醒时间（分钟）', 'appointment', 1)
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- --------------------------------------------------------
-- V34: 外部 AI Agent 配置
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `external_agent_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT 'Agent 名称',
  `description` varchar(500) DEFAULT NULL COMMENT 'Agent 描述',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '图标 URL',
  `endpoint_url` varchar(500) NOT NULL COMMENT 'Agent 接入地址',
  `auth_type` varchar(20) NOT NULL DEFAULT 'none' COMMENT '认证类型：none/bearer/apikey/basic',
  `auth_token` varchar(500) DEFAULT NULL COMMENT '认证令牌',
  `auth_username` varchar(100) DEFAULT NULL COMMENT 'Basic 认证用户名',
  `protocol` varchar(20) NOT NULL DEFAULT 'http_api' COMMENT '协议：http_api/webhook/sse/sdk',
  `input_schema` json DEFAULT NULL COMMENT '输入参数 JSON Schema',
  `output_schema` json DEFAULT NULL COMMENT '输出参数 JSON Schema',
  `timeout_seconds` int DEFAULT 30 COMMENT '请求超时时间（秒）',
  `retry_times` int DEFAULT 1 COMMENT '失败重试次数',
  `enabled` tinyint DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
  `sort_order` int DEFAULT 0 COMMENT '排序权重',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_external_agent_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部 AI Agent 配置表';

-- --------------------------------------------------------
-- V35: AI 对话会话 + 消息表
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_type` varchar(32) NOT NULL DEFAULT 'BUSINESS' COMMENT '会话类型：BUSINESS/MEDICAL/FOLLOWUP/EXTERNAL_AGENT',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务子类型',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户姓名',
  `patient_id` int DEFAULT NULL COMMENT '关联患者ID（临床类会话）',
  `title` varchar(200) DEFAULT NULL COMMENT '会话标题',
  `context_json` mediumtext DEFAULT NULL COMMENT '会话上下文 JSON（如长期记忆）',
  `last_message_at` datetime DEFAULT NULL COMMENT '最后消息时间',
  `message_count` int DEFAULT 0 COMMENT '消息数量',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active/archived/deleted',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_session_user` (`user_id`),
  KEY `idx_ai_chat_session_patient` (`patient_id`),
  KEY `idx_ai_chat_session_type` (`session_type`, `business_type`),
  KEY `idx_ai_chat_session_last_msg` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 对话会话表';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '所属会话ID',
  `message_role` varchar(20) NOT NULL COMMENT '角色：user/assistant/system/tool',
  `content` mediumtext COMMENT '消息内容',
  `content_type` varchar(20) DEFAULT 'text' COMMENT '内容类型：text/markdown/image/file',
  `model_name` varchar(64) DEFAULT NULL COMMENT '使用的 AI 模型',
  `tokens_used` int DEFAULT NULL COMMENT 'Token 消耗数',
  `latency_ms` int DEFAULT NULL COMMENT '响应延迟（毫秒）',
  `function_calls` json DEFAULT NULL COMMENT 'Function Calling 调用记录 JSON',
  `metadata_json` json DEFAULT NULL COMMENT '扩展元数据 JSON',
  `status` varchar(20) DEFAULT 'success' COMMENT '状态：success/error/streaming',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_session` (`session_id`),
  KEY `idx_ai_chat_message_role` (`message_role`),
  KEY `idx_ai_chat_message_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 对话消息表';

-- --------------------------------------------------------
-- V36: 业务闭环关联字段
-- --------------------------------------------------------

ALTER TABLE `appointment`
ADD COLUMN `medical_record_id` bigint DEFAULT NULL COMMENT '关联病历ID' AFTER `status`,
ADD KEY `idx_appointment_medical_record_id` (`medical_record_id`);

ALTER TABLE `treatment`
ADD COLUMN `finance_id` bigint DEFAULT NULL COMMENT '关联收费单ID' AFTER `medical_record_id`,
ADD KEY `idx_treatment_finance_id` (`finance_id`);

ALTER TABLE `medical_records`
ADD COLUMN `treatment_generated` tinyint DEFAULT 0 COMMENT '是否已生成处置记录：0-否 1-是' AFTER `record_status`;

ALTER TABLE `patient_followup`
ADD COLUMN `source_type` varchar(20) DEFAULT 'manual' COMMENT '来源：manual/ai' AFTER `summary`;

-- --------------------------------------------------------
-- V37: AI 临床辅助表
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `ai_medical_record_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `medical_record_id` bigint NOT NULL COMMENT '关联病历ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `chief_complaint_summary` varchar(500) DEFAULT NULL COMMENT '主诉摘要',
  `diagnosis_summary` varchar(500) DEFAULT NULL COMMENT '诊断摘要',
  `treatment_plan_summary` varchar(500) DEFAULT NULL COMMENT '治疗计划摘要',
  `medical_advice_summary` varchar(500) DEFAULT NULL COMMENT '医嘱摘要',
  `full_summary` mediumtext DEFAULT NULL COMMENT '完整摘要文本',
  `model_name` varchar(64) DEFAULT NULL COMMENT '使用模型',
  `status` varchar(20) DEFAULT 'generated' COMMENT '状态：generated/regenerated/error',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_medical_summary_record` (`medical_record_id`),
  KEY `idx_ai_medical_summary_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 病历摘要表';

CREATE TABLE IF NOT EXISTS `ai_patient_risk_assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `patient_id` int NOT NULL COMMENT '患者ID',
  `risk_level` varchar(20) NOT NULL COMMENT '风险等级：LOW/MEDIUM/HIGH',
  `risk_tags` varchar(500) DEFAULT NULL COMMENT '风险标签，逗号分隔',
  `assessment_reason` mediumtext DEFAULT NULL COMMENT '评估理由',
  `suggestions` mediumtext DEFAULT NULL COMMENT '建议关注事项',
  `source_data_summary` mediumtext DEFAULT NULL COMMENT '来源数据摘要',
  `model_name` varchar(64) DEFAULT NULL COMMENT '使用模型',
  `valid_until` date DEFAULT NULL COMMENT '评估有效期至',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_risk_assessment_patient` (`patient_id`),
  KEY `idx_ai_risk_assessment_level` (`risk_level`),
  KEY `idx_ai_risk_assessment_valid` (`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 患者风险评估表';

CREATE TABLE IF NOT EXISTS `ai_function_call_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint DEFAULT NULL COMMENT '关联会话ID',
  `message_id` bigint DEFAULT NULL COMMENT '关联消息ID',
  `function_name` varchar(100) NOT NULL COMMENT '函数名',
  `function_args` json DEFAULT NULL COMMENT '调用参数',
  `function_result` mediumtext DEFAULT NULL COMMENT '返回结果',
  `latency_ms` int DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `status` varchar(20) DEFAULT 'success' COMMENT '状态：success/error',
  `error_message` varchar(1000) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_function_call_session` (`session_id`),
  KEY `idx_ai_function_call_name` (`function_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Function Calling 调用日志';

-- --------------------------------------------------------
-- V38: 审计日志表
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operator_id` int DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `operator_role` varchar(30) DEFAULT NULL COMMENT '操作人角色',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `target_type` varchar(50) DEFAULT NULL COMMENT '操作对象类型',
  `target_id` varchar(100) DEFAULT NULL COMMENT '操作对象ID',
  `target_name` varchar(200) DEFAULT NULL COMMENT '操作对象名称（冗余）',
  `operation_desc` varchar(1000) DEFAULT NULL COMMENT '操作描述',
  `old_value` mediumtext DEFAULT NULL COMMENT '修改前数据 JSON',
  `new_value` mediumtext DEFAULT NULL COMMENT '修改后数据 JSON',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器UA',
  `status` varchar(20) DEFAULT 'success' COMMENT '状态：success/failure',
  `error_message` varchar(1000) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operator` (`operator_id`),
  KEY `idx_operation_log_type` (`operation_type`),
  KEY `idx_operation_log_target` (`target_type`, `target_id`),
  KEY `idx_operation_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局操作审计日志表';

CREATE TABLE IF NOT EXISTS `login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `login_type` varchar(20) DEFAULT 'password' COMMENT '登录方式：password/wechat',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '浏览器UA',
  `login_status` varchar(20) DEFAULT 'success' COMMENT '状态：success/failure',
  `failure_reason` varchar(200) DEFAULT NULL COMMENT '失败原因',
  `logout_at` datetime DEFAULT NULL COMMENT '登出时间',
  `session_duration_seconds` int DEFAULT NULL COMMENT '会话时长（秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_login_log_user` (`user_id`),
  KEY `idx_login_log_status` (`login_status`),
  KEY `idx_login_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录日志表';

-- --------------------------------------------------------
-- V39: 数据类型修正（按需执行，需先备份数据并验证兼容性）
-- --------------------------------------------------------
-- 以下语句默认注释掉，请在确认数据格式无误后手动执行

-- ALTER TABLE `finances` MODIFY COLUMN `date` DATE DEFAULT NULL COMMENT '日期';
-- ALTER TABLE `finances` MODIFY COLUMN `amount` DECIMAL(12,2) DEFAULT NULL COMMENT '金额';
-- ALTER TABLE `treatment` MODIFY COLUMN `treatment_fee` DECIMAL(12,2) DEFAULT NULL COMMENT '治疗费用';
-- ALTER TABLE `treatment_catalog` MODIFY COLUMN `default_fee` DECIMAL(12,2) DEFAULT NULL COMMENT '默认收费';

-- ============================================================
-- 基础数据初始化
-- ============================================================

-- MySQL dump 10.13  Distrib 9.6.0, for macos26.3 (arm64)
--
-- Host: localhost    Database: clinic_system
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin123','管理员','admin',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `material_categories`
--

LOCK TABLES `material_categories` WRITE;
/*!40000 ALTER TABLE `material_categories` DISABLE KEYS */;
INSERT INTO `material_categories` VALUES (1,'种植类',0,10,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(2,'正畸类',0,20,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(3,'修复类',0,30,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(4,'基础耗材',0,40,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(5,'其他',0,50,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19');
/*!40000 ALTER TABLE `material_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `payment_channel`
--

LOCK TABLES `payment_channel` WRITE;
/*!40000 ALTER TABLE `payment_channel` DISABLE KEYS */;
INSERT INTO `payment_channel` VALUES (1,'现金',1,10,'2026-05-06 19:35:19','2026-05-06 19:35:19'),(2,'微信',1,20,'2026-05-06 19:35:19','2026-05-06 19:35:19'),(3,'支付宝',1,30,'2026-05-06 19:35:19','2026-05-06 19:35:19'),(4,'银行卡',1,40,'2026-05-06 19:35:19','2026-05-06 19:35:19');
/*!40000 ALTER TABLE `payment_channel` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-06 22:35:52
