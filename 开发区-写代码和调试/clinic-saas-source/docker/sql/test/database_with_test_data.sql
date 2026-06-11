-- MySQL dump 10.13  Distrib 9.6.0, for Linux (aarch64)
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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_ad_platform_start` (`platform`,`start_date`),
  KEY `idx_ad_period` (`start_date`,`end_date`),
  KEY `idx_ad_created_by` (`created_by`),
  KEY `idx_ad_finance_record_id` (`finance_record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='广告投放记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advertising_spending`
--

LOCK TABLES `advertising_spending` WRITE;
/*!40000 ALTER TABLE `advertising_spending` DISABLE KEYS */;
INSERT INTO `advertising_spending` VALUES (1,'美团','美团团购推广','2026-05-01','2026-05-31',5000.00,'洗牙',NULL,'月度推广',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,'抖音','抖音本地推','2026-05-01','2026-05-31',8000.00,'种植',NULL,'月度推广',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,'百度','百度搜索推广','2026-05-01','2026-05-31',3000.00,'矫正',NULL,'月度推广',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(4,'小红书','小红书种草','2026-05-01','2026-05-31',2000.00,'美白',NULL,'月度推广',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `advertising_spending` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_agent_config`
--

DROP TABLE IF EXISTS `ai_agent_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint DEFAULT NULL COMMENT 'æ‰€å±žç”¨æˆ·IDï¼ŒNULLè¡¨ç¤ºç³»ç»Ÿé»˜è®¤',
  `agent_key` varchar(64) NOT NULL COMMENT 'Agentæ ‡è¯†: default/finance/patient/schedule/è‡ªå®šä¹‰',
  `name` varchar(32) NOT NULL COMMENT 'æ˜¾ç¤ºåç§°',
  `icon` varchar(8) DEFAULT 'ðŸ¤–' COMMENT 'å›¾æ ‡emoji',
  `description` varchar(256) DEFAULT NULL COMMENT 'æè¿°',
  `gradient` varchar(256) DEFAULT 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)' COMMENT 'ä¸»é¢˜è‰²CSSæ¸å˜',
  `chips` json DEFAULT NULL COMMENT 'å¿«æ·æŒ‡ä»¤JSONæ•°ç»„',
  `preset_message` varchar(500) DEFAULT NULL COMMENT '快捷卡片预设消息',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åº',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `endpoint_url` varchar(500) DEFAULT NULL COMMENT '外部工作流端点地址',
  `auth_type` varchar(50) DEFAULT NULL COMMENT '认证类型：bearer / basic / api_key / 自定义header名',
  `auth_token` varchar(500) DEFAULT NULL COMMENT '认证令牌',
  `request_template` text COMMENT '请求体模板，支持 {{变量}} 替换',
  `response_type` varchar(20) DEFAULT 'json' COMMENT '响应类型：sse（流式）/ json（一次性）',
  `timeout_seconds` int DEFAULT '60' COMMENT '超时秒数',
  `ui_mode` varchar(32) DEFAULT 'json' COMMENT '展示模式: json/chat/card/table',
  `ui_config_json` text COMMENT '前端UI配置JSON',
  `usage_location` varchar(100) DEFAULT NULL COMMENT '用途位置标注，如新增病历页、咨询分析',
  `is_visible_on_home` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否在首页AI面板显示',
  `is_system_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为系统默认配置',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_agent` (`account_id`,`agent_key`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Agenté…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_agent_config`
--

LOCK TABLES `ai_agent_config` WRITE;
/*!40000 ALTER TABLE `ai_agent_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_agent_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_message`
--

DROP TABLE IF EXISTS `ai_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `session_id` bigint NOT NULL COMMENT 'æ‰€å±žä¼šè¯ID',
  `message_role` varchar(20) NOT NULL COMMENT 'è§’è‰²ï¼šuser/assistant/system/tool',
  `content` mediumtext COMMENT 'æ¶ˆæ¯å†…å®¹',
  `content_type` varchar(20) DEFAULT 'text' COMMENT 'å†…å®¹ç±»åž‹ï¼štext/markdown/image/file',
  `model_name` varchar(64) DEFAULT NULL COMMENT 'ä½¿ç”¨çš„ AI æ¨¡åž‹',
  `tokens_used` int DEFAULT NULL COMMENT 'Token æ¶ˆè€—æ•°',
  `latency_ms` int DEFAULT NULL COMMENT 'å“åº”å»¶è¿Ÿï¼ˆæ¯«ç§’ï¼‰',
  `function_calls` json DEFAULT NULL COMMENT 'Function Calling è°ƒç”¨è®°å½• JSON',
  `metadata_json` json DEFAULT NULL COMMENT 'æ‰©å±•å…ƒæ•°æ® JSON',
  `status` varchar(20) DEFAULT 'success' COMMENT 'çŠ¶æ€ï¼šsuccess/error/streaming',
  `error_message` varchar(1000) DEFAULT NULL COMMENT 'é”™è¯¯ä¿¡æ¯',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_session` (`session_id`),
  KEY `idx_ai_chat_message_role` (`message_role`),
  KEY `idx_ai_chat_message_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI å¯¹è¯æ¶ˆæ¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_message`
--

LOCK TABLES `ai_chat_message` WRITE;
/*!40000 ALTER TABLE `ai_chat_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_session`
--

DROP TABLE IF EXISTS `ai_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `session_type` varchar(32) NOT NULL DEFAULT 'BUSINESS' COMMENT 'ä¼šè¯ç±»åž‹ï¼šBUSINESS/MEDICAL/FOLLOWUP/EXTERNAL_AGENT',
  `business_type` varchar(32) DEFAULT NULL COMMENT 'ä¸šåŠ¡å­ç±»åž‹',
  `user_id` int DEFAULT NULL COMMENT 'ç”¨æˆ·ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT 'ç”¨æˆ·å§“å',
  `patient_id` int DEFAULT NULL COMMENT 'å…³è”æ‚£è€…IDï¼ˆä¸´åºŠç±»ä¼šè¯ï¼‰',
  `title` varchar(200) DEFAULT NULL COMMENT 'ä¼šè¯æ ‡é¢˜',
  `context_json` mediumtext COMMENT 'ä¼šè¯ä¸Šä¸‹æ–‡ JSONï¼ˆå¦‚é•¿æœŸè®°å¿†ï¼‰',
  `last_message_at` datetime DEFAULT NULL COMMENT 'æœ€åŽæ¶ˆæ¯æ—¶é—´',
  `message_count` int DEFAULT '0' COMMENT 'æ¶ˆæ¯æ•°é‡',
  `status` varchar(20) DEFAULT 'active' COMMENT 'çŠ¶æ€ï¼šactive/archived/deleted',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_session_user` (`user_id`),
  KEY `idx_ai_chat_session_patient` (`patient_id`),
  KEY `idx_ai_chat_session_type` (`session_type`,`business_type`),
  KEY `idx_ai_chat_session_last_msg` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI å¯¹è¯ä¼šè¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_session`
--

LOCK TABLES `ai_chat_session` WRITE;
/*!40000 ALTER TABLE `ai_chat_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chat_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_few_shot_example`
--

DROP TABLE IF EXISTS `ai_few_shot_example`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_few_shot_example` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL COMMENT '关联模板ID',
  `input_content` text COMMENT '输入示例',
  `output_content` text COMMENT '输出示例',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Few-shot 示例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_few_shot_example`
--

LOCK TABLES `ai_few_shot_example` WRITE;
/*!40000 ALTER TABLE `ai_few_shot_example` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_few_shot_example` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_function_call_log`
--

DROP TABLE IF EXISTS `ai_function_call_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_function_call_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `session_id` bigint DEFAULT NULL COMMENT 'å…³è”ä¼šè¯ID',
  `message_id` bigint DEFAULT NULL COMMENT 'å…³è”æ¶ˆæ¯ID',
  `function_name` varchar(100) NOT NULL COMMENT 'å‡½æ•°å',
  `function_args` json DEFAULT NULL COMMENT 'è°ƒç”¨å‚æ•°',
  `function_result` mediumtext COMMENT 'è¿”å›žç»“æžœ',
  `latency_ms` int DEFAULT NULL COMMENT 'æ‰§è¡Œè€—æ—¶ï¼ˆæ¯«ç§’ï¼‰',
  `status` varchar(20) DEFAULT 'success' COMMENT 'çŠ¶æ€ï¼šsuccess/error',
  `error_message` varchar(1000) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_function_call_session` (`session_id`),
  KEY `idx_ai_function_call_name` (`function_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Function Calling 调用日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_function_call_log`
--

LOCK TABLES `ai_function_call_log` WRITE;
/*!40000 ALTER TABLE `ai_function_call_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_function_call_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_function_config`
--

DROP TABLE IF EXISTS `ai_function_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_function_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `function_key` varchar(50) NOT NULL COMMENT '功能标识',
  `function_name` varchar(100) NOT NULL COMMENT '功能名称',
  `page_path` varchar(100) DEFAULT NULL COMMENT '所属页面路径',
  `icon` varchar(20) DEFAULT 0xF09FA496 COMMENT '图标（emoji）',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `model_name` varchar(50) DEFAULT NULL COMMENT '使用模型名称',
  `prompt_template_id` bigint DEFAULT NULL COMMENT '关联提示词模板ID',
  `extra_config` json DEFAULT NULL COMMENT '额外配置（各功能私有参数）',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_function_key` (`function_key`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 功能配置主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_function_config`
--

LOCK TABLES `ai_function_config` WRITE;
/*!40000 ALTER TABLE `ai_function_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_function_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_function_mapping`
--

DROP TABLE IF EXISTS `ai_function_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_function_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` bigint DEFAULT NULL COMMENT '所属用户ID，NULL表示系统默认',
  `function_code` varchar(64) NOT NULL COMMENT '系统功能编码，如 medical-record-expand',
  `function_name` varchar(100) NOT NULL COMMENT '功能名称，如 病历扩写',
  `agent_key` varchar(64) DEFAULT NULL COMMENT '绑定的 AgentKey，NULL表示未绑定',
  `is_visible_on_page` tinyint(1) DEFAULT '1' COMMENT '是否在所在页面显示入口 0-隐藏 1-显示',
  `is_visible_on_home` tinyint(1) DEFAULT '1' COMMENT '是否在首页AI下拉框显示 0-隐藏 1-显示',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_function` (`account_id`,`function_code`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI系统功能与Agent绑定映射表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_function_mapping`
--

LOCK TABLES `ai_function_mapping` WRITE;
/*!40000 ALTER TABLE `ai_function_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_function_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_global_config`
--

DROP TABLE IF EXISTS `ai_global_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_global_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(255) DEFAULT NULL COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 全局配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_global_config`
--

LOCK TABLES `ai_global_config` WRITE;
/*!40000 ALTER TABLE `ai_global_config` DISABLE KEYS */;
INSERT INTO `ai_global_config` VALUES (1,'global_enabled','true','AI 功能总开关','2026-05-13 16:23:36'),(2,'debug_mode','true','调试模式开关','2026-05-12 10:30:43'),(3,'response_field','data','AI å“åº”å†…å®¹å­—æ®µå','2026-05-17 15:33:08'),(4,'model_provider','openai','默认模型提供商','2026-06-11 04:23:38'),(5,'max_tokens','2000','最大token数','2026-06-11 04:23:38'),(6,'temperature','0.7','温度参数','2026-06-11 04:23:38');
/*!40000 ALTER TABLE `ai_global_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_medical_record_summary`
--

DROP TABLE IF EXISTS `ai_medical_record_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_medical_record_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `medical_record_id` bigint NOT NULL COMMENT 'å…³è”ç—…åŽ†ID',
  `patient_id` int NOT NULL COMMENT 'æ‚£è€…ID',
  `chief_complaint_summary` varchar(500) DEFAULT NULL COMMENT 'ä¸»è¯‰æ‘˜è¦',
  `diagnosis_summary` varchar(500) DEFAULT NULL COMMENT 'è¯Šæ–­æ‘˜è¦',
  `treatment_plan_summary` varchar(500) DEFAULT NULL COMMENT 'æ²»ç–—è®¡åˆ’æ‘˜è¦',
  `medical_advice_summary` varchar(500) DEFAULT NULL COMMENT 'åŒ»å˜±æ‘˜è¦',
  `full_summary` mediumtext COMMENT 'å®Œæ•´æ‘˜è¦æ–‡æœ¬',
  `model_name` varchar(64) DEFAULT NULL COMMENT 'ä½¿ç”¨æ¨¡åž‹',
  `status` varchar(20) DEFAULT 'generated' COMMENT 'çŠ¶æ€ï¼šgenerated/regenerated/error',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_medical_summary_record` (`medical_record_id`),
  KEY `idx_ai_medical_summary_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI ç—…åŽ†æ‘˜è¦è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_medical_record_summary`
--

LOCK TABLES `ai_medical_record_summary` WRITE;
/*!40000 ALTER TABLE `ai_medical_record_summary` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_medical_record_summary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_model_provider`
--

DROP TABLE IF EXISTS `ai_model_provider`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_model_provider` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_name` varchar(64) NOT NULL COMMENT 'ä¾›åº”å•†åç§°ï¼Œå¦‚ OpenAIã€DeepSeek',
  `base_url` varchar(256) NOT NULL COMMENT 'API åŸºç¡€åœ°å€',
  `api_key` varchar(512) NOT NULL COMMENT 'API å¯†é’¥',
  `model_name` varchar(64) NOT NULL COMMENT 'æ¨¡åž‹åç§°',
  `reasoning_effort` varchar(16) DEFAULT 'medium' COMMENT 'æŽ¨ç†åŠ›åº¦ï¼šlow/medium/high',
  `max_output_tokens` int DEFAULT '3000' COMMENT 'æœ€å¤§è¾“å‡º token æ•°',
  `enabled` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `api_type` varchar(32) DEFAULT 'chat_completions' COMMENT 'API 类型：chat_completions（通用）或 responses（OpenAI 原生）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI æ¨¡åž‹ä¾›åº”å•†é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_model_provider`
--

LOCK TABLES `ai_model_provider` WRITE;
/*!40000 ALTER TABLE `ai_model_provider` DISABLE KEYS */;
INSERT INTO `ai_model_provider` VALUES (2,'测试供应商','http://localhost:8080','test-key','test-model','medium',3000,1,'2026-05-07 05:23:07','2026-05-07 05:23:07','chat_completions');
/*!40000 ALTER TABLE `ai_model_provider` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_operation_log`
--

DROP TABLE IF EXISTS `ai_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `function_key` varchar(50) DEFAULT NULL COMMENT '功能标识',
  `account_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `input_snapshot` json DEFAULT NULL COMMENT '输入数据快照',
  `ai_output` text COMMENT 'AI输出内容',
  `is_adopted` tinyint(1) DEFAULT NULL COMMENT '医生是否采纳',
  `token_used` int DEFAULT '0' COMMENT '消耗token数',
  `error_msg` text COMMENT '错误信息',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_function_key_time` (`function_key`,`create_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=275 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_operation_log`
--

LOCK TABLES `ai_operation_log` WRITE;
/*!40000 ALTER TABLE `ai_operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_patient_risk_assessment`
--

DROP TABLE IF EXISTS `ai_patient_risk_assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_patient_risk_assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `patient_id` int NOT NULL COMMENT 'æ‚£è€…ID',
  `risk_level` varchar(20) NOT NULL COMMENT 'é£Žé™©ç­‰çº§ï¼šLOW/MEDIUM/HIGH',
  `risk_tags` varchar(500) DEFAULT NULL COMMENT 'é£Žé™©æ ‡ç­¾ï¼Œé€—å·åˆ†éš”',
  `assessment_reason` mediumtext COMMENT 'è¯„ä¼°ç†ç”±',
  `suggestions` mediumtext COMMENT 'å»ºè®®å…³æ³¨äº‹é¡¹',
  `source_data_summary` mediumtext COMMENT 'æ¥æºæ•°æ®æ‘˜è¦',
  `model_name` varchar(64) DEFAULT NULL COMMENT 'ä½¿ç”¨æ¨¡åž‹',
  `valid_until` date DEFAULT NULL COMMENT 'è¯„ä¼°æœ‰æ•ˆæœŸè‡³',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_risk_assessment_patient` (`patient_id`),
  KEY `idx_ai_risk_assessment_level` (`risk_level`),
  KEY `idx_ai_risk_assessment_valid` (`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 患者风险评估表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_patient_risk_assessment`
--

LOCK TABLES `ai_patient_risk_assessment` WRITE;
/*!40000 ALTER TABLE `ai_patient_risk_assessment` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_patient_risk_assessment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_prompt_template`
--

DROP TABLE IF EXISTS `ai_prompt_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_prompt_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `scene` varchar(50) NOT NULL COMMENT '场景标识',
  `name` varchar(100) DEFAULT NULL COMMENT '模板名称',
  `system_prompt` text COMMENT '系统提示词',
  `temperature` decimal(3,2) DEFAULT '0.20' COMMENT '温度参数',
  `max_tokens` int DEFAULT '2000' COMMENT '最大输出Token数',
  `response_format` varchar(20) DEFAULT 'json' COMMENT '响应格式',
  `json_schema` text COMMENT 'JSON Schema约束',
  `extra_config` json DEFAULT NULL COMMENT '扩展配置（空字段策略等）',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `version` int DEFAULT '1' COMMENT '版本号',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene` (`scene`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 提示词模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_prompt_template`
--

LOCK TABLES `ai_prompt_template` WRITE;
/*!40000 ALTER TABLE `ai_prompt_template` DISABLE KEYS */;
INSERT INTO `ai_prompt_template` VALUES (2,'medical_expand','病历扩写模板','你是一位资深口腔科医生助手...',0.20,2000,NULL,NULL,'{\"sensitiveWords\": \"确诊,绝对,保证,100%,肯定\", \"forbidAssertion\": true, \"checkHistoryTime\": true, \"checkDiagnosisTone\": true, \"emptyFieldStrategy\": \"skip\", \"checkChiefComplaintLength\": true}',1,1,'2026-05-11 09:10:10','2026-05-16 00:57:18');
/*!40000 ALTER TABLE `ai_prompt_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `api_key`
--

DROP TABLE IF EXISTS `api_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_key` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `clinic_id` varchar(64) NOT NULL COMMENT '诊所ID',
  `key` varchar(512) NOT NULL COMMENT 'API 密钥',
  `name` varchar(64) DEFAULT '默认Key' COMMENT 'Key名称',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT '最后使用时间',
  `usage_count` int DEFAULT '0' COMMENT '使用次数',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`key`),
  UNIQUE KEY `uk_clinic` (`clinic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='API Key管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `api_key`
--

LOCK TABLES `api_key` WRITE;
/*!40000 ALTER TABLE `api_key` DISABLE KEYS */;
INSERT INTO `api_key` VALUES (1,'1','sk-saas-a8f06f47a73e4273','默认Key',1,NULL,NULL,'2026-06-11 04:28:16',39,'2026-05-15 06:36:34','2026-06-11 04:28:16');
/*!40000 ALTER TABLE `api_key` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_status` varchar(50) DEFAULT '已预约' COMMENT '接诊状态：已预约/已挂号/等待中/就诊中/已完成',
  `check_in_time` datetime DEFAULT NULL COMMENT '挂号/签到时间',
  `medical_record_id` bigint DEFAULT NULL COMMENT 'å…³è”ç—…åŽ†ID',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_appointment_date` (`appointment_date`),
  KEY `idx_patient_name` (`patient_name`),
  KEY `idx_status` (`status`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_appointment_doctor_account_id` (`doctor_account_id`),
  KEY `idx_appointment_medical_record_id` (`medical_record_id`),
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (1,1,'张三','2026-06-12','09:00:00',60,1,'王医生','复诊-根管治疗',NULL,'已确认','已预约',NULL,NULL,'default'),(2,2,'李四','2026-06-12','10:30:00',60,2,'李医生','矫正复诊',NULL,'已确认','已预约',NULL,NULL,'default'),(3,3,'王五','2026-06-12','14:00:00',60,4,'刘医生','种植复查',NULL,'已确认','已预约',NULL,NULL,'default'),(4,4,'赵六','2026-06-13','09:00:00',60,1,'王医生','牙周治疗',NULL,'待确认','已预约',NULL,NULL,'default'),(5,5,'孙七','2026-06-13','11:00:00',60,3,'张医生','拔智齿',NULL,'已取消','已预约',NULL,NULL,'default'),(6,1,'张三','2026-06-12','09:00:00',60,1,'王医生','复诊-根管治疗',NULL,'已确认','已预约',NULL,NULL,'default'),(7,2,'李四','2026-06-12','10:30:00',60,2,'李医生','矫正复诊',NULL,'已确认','已预约',NULL,NULL,'default'),(8,3,'王五','2026-06-12','14:00:00',60,4,'刘医生','种植复查',NULL,'已确认','已预约',NULL,NULL,'default'),(9,4,'赵六','2026-06-13','09:00:00',60,1,'王医生','牙周治疗',NULL,'待确认','已预约',NULL,NULL,'default'),(10,5,'孙七','2026-06-13','11:00:00',60,3,'张医生','拔智齿',NULL,'已取消','已预约',NULL,NULL,'default');
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_business_alert_date` (`alert_date`),
  KEY `idx_business_alert_level` (`alert_level`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营异常波动告警日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_alert_log`
--

LOCK TABLES `business_alert_log` WRITE;
/*!40000 ALTER TABLE `business_alert_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `business_alert_log` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_analysis_date` (`analysis_date`),
  KEY `idx_business_analysis_status` (`analysis_status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日经营AI分析日报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_daily_analysis`
--

LOCK TABLES `business_daily_analysis` WRITE;
/*!40000 ALTER TABLE `business_daily_analysis` DISABLE KEYS */;
INSERT INTO `business_daily_analysis` VALUES (1,'2026-06-01','completed','RULE_BASED','SCHEDULED',NULL,85,NULL,'今日营收良好','就诊15人次，新客5人',NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,'2026-06-02','completed','RULE_BASED','SCHEDULED',NULL,90,NULL,'种植牙大单','就诊18人次，营收1.2万',NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,'2026-06-03','completed','RULE_BASED','SCHEDULED',NULL,75,NULL,'平稳运营','就诊12人次，以基础治疗为主',NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(4,'2026-06-04','completed','RULE_BASED','SCHEDULED',NULL,95,NULL,'种植高峰期','就诊20人次，营收1.5万',NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(5,'2026-06-05','completed','RULE_BASED','SCHEDULED',NULL,88,NULL,'复诊为主','就诊16人次，以复诊为主',NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `business_daily_analysis` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_period_report` (`report_type`,`period_key`),
  KEY `idx_business_period_report_status` (`report_status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营周报月报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_period_report`
--

LOCK TABLES `business_period_report` WRITE;
/*!40000 ALTER TABLE `business_period_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `business_period_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clinic`
--

DROP TABLE IF EXISTS `clinic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clinic` (
  `id` varchar(64) NOT NULL COMMENT '诊所唯一标识',
  `name` varchar(100) NOT NULL COMMENT '诊所名称',
  `address` varchar(500) DEFAULT NULL COMMENT '地址',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `activation_code` varchar(255) DEFAULT NULL COMMENT '绑定的激活码',
  `license_expires_at` timestamp NULL DEFAULT NULL COMMENT '授权过期时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='诊所管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clinic`
--

LOCK TABLES `clinic` WRITE;
/*!40000 ALTER TABLE `clinic` DISABLE KEYS */;
INSERT INTO `clinic` VALUES ('default','测试诊所',NULL,NULL,1,'TEST123',NULL,'2026-06-11 04:11:42','2026-06-11 04:11:42');
/*!40000 ALTER TABLE `clinic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clinic_info`
--

DROP TABLE IF EXISTS `clinic_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clinic_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `clinic_name` varchar(100) NOT NULL DEFAULT 'èˆ’æ¾³å£è…”' COMMENT 'è¯Šæ‰€åç§°',
  `clinic_code` varchar(64) DEFAULT NULL COMMENT 'è¯Šæ‰€ç¼–ç ',
  `phone` varchar(20) DEFAULT NULL COMMENT 'è”ç³»ç”µè¯',
  `address` varchar(500) DEFAULT NULL COMMENT 'åœ°å€',
  `logo_url` varchar(500) DEFAULT NULL COMMENT 'Logoåœ°å€',
  `business_hours` varchar(200) DEFAULT NULL COMMENT 'è¥ä¸šæ—¶é—´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è¯Šæ‰€æœºæž„ä¿¡æ¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clinic_info`
--

LOCK TABLES `clinic_info` WRITE;
/*!40000 ALTER TABLE `clinic_info` DISABLE KEYS */;
INSERT INTO `clinic_info` VALUES (1,'èˆ’æ¾³å£è…”',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-07 05:03:11','2026-05-07 05:03:11');
/*!40000 ALTER TABLE `clinic_info` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_consent_template_status` (`status`),
  KEY `idx_consent_template_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知情同意书模板库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consent_template`
--

LOCK TABLES `consent_template` WRITE;
/*!40000 ALTER TABLE `consent_template` DISABLE KEYS */;
INSERT INTO `consent_template` VALUES (1,'根管治疗知情同意书','我已了解根管治疗的流程、风险及预后，同意接受治疗。可能出现术后疼痛、器械分离、根管侧穿等并发症。','',1,1,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,'种植牙手术知情同意书','我已了解种植牙手术的流程、风险及预后，同意接受手术。可能出现术后肿胀、感染、种植体失败等情况。','',1,2,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,'拔牙手术知情同意书','我已了解拔牙手术的流程、风险及预后，同意接受手术。可能出现术后出血、感染、干槽症等情况。','',1,3,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `consent_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation_followups`
--

DROP TABLE IF EXISTS `consultation_followups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation_followups` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `consultation_id` bigint NOT NULL COMMENT 'å…³è”å’¨è¯¢è®°å½•ID',
  `followup_time` datetime NOT NULL COMMENT 'è·Ÿè¿›æ—¶é—´',
  `content` varchar(1000) NOT NULL COMMENT 'è·Ÿè¿›å†…å®¹',
  `next_followup_time` datetime DEFAULT NULL COMMENT 'ä¸‹æ¬¡è®¡åˆ’è·Ÿè¿›æ—¶é—´',
  `created_by` bigint NOT NULL COMMENT 'è·Ÿè¿›äººID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT 'è·Ÿè¿›äººå§“å',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_consultation_id` (`consultation_id`),
  CONSTRAINT `fk_followup_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation_records` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='咨询跟进记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_followups`
--

LOCK TABLES `consultation_followups` WRITE;
/*!40000 ALTER TABLE `consultation_followups` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation_followups` ENABLE KEYS */;
UNLOCK TABLES;

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
  `estimated_amount` decimal(12,2) DEFAULT NULL COMMENT 'é¢„è®¡æ¶ˆè´¹é‡‘é¢ï¼ˆå…ƒï¼‰',
  `customer_concerns` varchar(500) DEFAULT NULL COMMENT 'å®¢æˆ·é¡¾è™‘ã€ç«žå“å¯¹æ¯”ã€çŠ¹è±«åŽŸå› ç­‰',
  `ai_analysis_summary` varchar(1000) DEFAULT NULL COMMENT 'AIåˆ†æžæ‘˜è¦',
  `ai_analysis_score` int DEFAULT NULL COMMENT 'AIæ„å‘è¯„åˆ† 0-100',
  `arrived_at` datetime DEFAULT NULL COMMENT '首次进入已预约到店时间',
  `deal_at` datetime DEFAULT NULL COMMENT '首次成交时间',
  `created_by` bigint DEFAULT '0' COMMENT '录入人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '录入人姓名',
  `updated_by` bigint DEFAULT NULL COMMENT '最后更新人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
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
  CONSTRAINT `fk_consultation_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `fk_consultation_records_patient_relaxed` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='咨询记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_records`
--

LOCK TABLES `consultation_records` WRITE;
/*!40000 ALTER TABLE `consultation_records` DISABLE KEYS */;
INSERT INTO `consultation_records` VALUES (1,NULL,'2026-06-11 04:23:38','电话',NULL,NULL,NULL,NULL,NULL,NULL,'种植牙','高','已预约','陈八','13700137001',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'客服小张',NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,NULL,'2026-06-11 04:23:38','微信',NULL,NULL,NULL,NULL,NULL,NULL,'矫正','中','待跟进','周九','13700137002',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'客服小张',NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,NULL,'2026-06-11 04:23:38','美团',NULL,NULL,NULL,NULL,NULL,NULL,'洗牙','低','已回复','吴十','13700137003',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'客服小李',NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(4,NULL,'2026-06-11 04:23:38','电话',NULL,NULL,NULL,NULL,NULL,NULL,'种植术后','中','已安抚','郑十一','13700137004',NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'客服小李',NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `consultation_records` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_home_reminder_dismissal` (`doctor_account_id`,`reminder_key`),
  KEY `idx_doctor_home_reminder_patient` (`doctor_account_id`,`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生首页提醒消失状态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_home_reminder_dismissal`
--

LOCK TABLES `doctor_home_reminder_dismissal` WRITE;
/*!40000 ALTER TABLE `doctor_home_reminder_dismissal` DISABLE KEYS */;
/*!40000 ALTER TABLE `doctor_home_reminder_dismissal` ENABLE KEYS */;
UNLOCK TABLES;

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
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型：morning/evening/rest/custom',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_name` (`doctor_name`),
  KEY `idx_schedule_date` (`schedule_date`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生排班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctors`
--

LOCK TABLES `doctors` WRITE;
/*!40000 ALTER TABLE `doctors` DISABLE KEYS */;
INSERT INTO `doctors` VALUES (1,'王医生',NULL,NULL,NULL,'在职','全职','default'),(2,'李医生',NULL,NULL,NULL,'在职','全职','default'),(3,'张医生',NULL,NULL,NULL,'在职','兼职','default'),(4,'刘医生',NULL,NULL,NULL,'在职','全职','default'),(5,'王医生',NULL,NULL,NULL,'在职','全职','default'),(6,'李医生',NULL,NULL,NULL,'在职','全职','default'),(7,'张医生',NULL,NULL,NULL,'在职','兼职','default'),(8,'刘医生',NULL,NULL,NULL,'在职','全职','default');
/*!40000 ALTER TABLE `doctors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `external_agent_config`
--

DROP TABLE IF EXISTS `external_agent_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `external_agent_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `name` varchar(100) NOT NULL COMMENT 'Agent åç§°',
  `description` varchar(500) DEFAULT NULL COMMENT 'Agent æè¿°',
  `icon_url` varchar(500) DEFAULT NULL COMMENT 'å›¾æ ‡ URL',
  `endpoint_url` varchar(500) NOT NULL COMMENT 'Agent æŽ¥å…¥åœ°å€',
  `auth_type` varchar(20) NOT NULL DEFAULT 'none' COMMENT 'è®¤è¯ç±»åž‹ï¼šnone/bearer/apikey/basic',
  `auth_token` varchar(500) DEFAULT NULL COMMENT 'è®¤è¯ä»¤ç‰Œ',
  `auth_username` varchar(100) DEFAULT NULL COMMENT 'Basic è®¤è¯ç”¨æˆ·å',
  `protocol` varchar(20) NOT NULL DEFAULT 'http_api' COMMENT 'åè®®ï¼šhttp_api/webhook/sse/sdk',
  `input_schema` json DEFAULT NULL COMMENT 'è¾“å…¥å‚æ•° JSON Schema',
  `output_schema` json DEFAULT NULL COMMENT 'è¾“å‡ºå‚æ•° JSON Schema',
  `timeout_seconds` int DEFAULT '30' COMMENT 'è¯·æ±‚è¶…æ—¶æ—¶é—´ï¼ˆç§’ï¼‰',
  `retry_times` int DEFAULT '1' COMMENT 'å¤±è´¥é‡è¯•æ¬¡æ•°',
  `enabled` tinyint DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨ï¼š0-ç¦ç”¨ 1-å¯ç”¨',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºæƒé‡',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_external_agent_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å¤–éƒ¨ AI Agent é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `external_agent_config`
--

LOCK TABLES `external_agent_config` WRITE;
/*!40000 ALTER TABLE `external_agent_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `external_agent_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_attachment`
--

DROP TABLE IF EXISTS `file_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `biz_type` varchar(50) NOT NULL COMMENT 'ä¸šåŠ¡ç±»åž‹ï¼špatient/medical_record/material/purchaseç­‰',
  `biz_id` bigint NOT NULL COMMENT 'ä¸šåŠ¡è®°å½•ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT 'åŽŸå§‹æ–‡ä»¶å',
  `file_url` varchar(500) DEFAULT NULL COMMENT 'æ–‡ä»¶å­˜å‚¨è·¯å¾„',
  `file_size` bigint DEFAULT NULL COMMENT 'æ–‡ä»¶å¤§å°(å­—èŠ‚)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'æ–‡ä»¶ç±»åž‹',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åº',
  `created_by` bigint DEFAULT NULL COMMENT 'ä¸Šä¼ äººID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_file_attachment_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用文件附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_attachment`
--

LOCK TABLES `file_attachment` WRITE;
/*!40000 ALTER TABLE `file_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_attachment` ENABLE KEYS */;
UNLOCK TABLES;

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
  `amount` decimal(12,2) DEFAULT NULL COMMENT 'é‡‘é¢',
  `date` date DEFAULT NULL COMMENT 'æ—¥æœŸ',
  `type` varchar(30) DEFAULT NULL COMMENT '类型：收入/支出',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_date` (`date`),
  KEY `idx_type` (`type`),
  KEY `idx_finances_patient_id` (`patient_id`),
  KEY `idx_finances_treatment_id` (`treatment_id`),
  KEY `idx_finances_biz_type` (`biz_type`),
  KEY `idx_finances_payment_channel_id` (`payment_channel_id`),
  KEY `idx_finances_patient_date` (`patient_id`,`date`),
  CONSTRAINT `fk_finances_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_finances_payment_channel` FOREIGN KEY (`payment_channel_id`) REFERENCES `payment_channel` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_finances_treatment` FOREIGN KEY (`treatment_id`) REFERENCES `treatment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `finances`
--

LOCK TABLES `finances` WRITE;
/*!40000 ALTER TABLE `finances` DISABLE KEYS */;
INSERT INTO `finances` VALUES (1,1,NULL,NULL,NULL,'根管治疗',600.00,'2026-06-10','收入','治疗','首付','default'),(2,2,NULL,NULL,NULL,'矫正复诊',0.00,'2026-06-08','收入','治疗','包含在套餐内','default'),(3,3,NULL,NULL,NULL,'种植复查',0.00,'2026-06-05','收入','治疗','术后复查免费','default'),(4,4,NULL,NULL,NULL,'深度洁牙',500.00,'2026-06-01','收入','治疗',NULL,'default'),(5,5,NULL,NULL,NULL,'消炎处理',150.00,'2026-06-10','收入','治疗',NULL,'default'),(6,1,NULL,NULL,NULL,'根管治疗',600.00,'2026-06-10','收入','治疗','首付','default'),(7,2,NULL,NULL,NULL,'矫正复诊',0.00,'2026-06-08','收入','治疗','包含在套餐内','default'),(8,3,NULL,NULL,NULL,'种植复查',0.00,'2026-06-05','收入','治疗','术后复查免费','default'),(9,4,NULL,NULL,NULL,'深度洁牙',500.00,'2026-06-01','收入','治疗',NULL,'default'),(10,5,NULL,NULL,NULL,'消炎处理',150.00,'2026-06-10','收入','治疗',NULL,'default');
/*!40000 ALTER TABLE `finances` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'0','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(2,'1','init','SQL','V1__init.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(3,'2','portal access tokens','SQL','V2__PortalAccessTokens.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(4,'3','patient foreign keys','SQL','V3__PatientForeignKeys.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(5,'4','patient foreign keys phase2','SQL','V4__PatientForeignKeysPhase2.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(6,'5','business reference foreign keys','SQL','V5__BusinessReferenceForeignKeys.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(7,'6','medical record template table','SQL','V6__MedicalRecordTemplateTable.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(8,'7','doctor home reminder dismissal table','SQL','V7__DoctorHomeReminderDismissalTable.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(9,'8','medical record workbench fields','SQL','V8__MedicalRecordWorkbenchFields.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(10,'9','treatment operation performance allocation','SQL','V9__TreatmentOperationPerformanceAllocation.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(11,'10','advertising spending module','SQL','V10__AdvertisingSpendingModule.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(12,'11','patient insight summary','SQL','V11__PatientInsightSummary.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(13,'12','patient referral records and consultation referrer','SQL','V12__PatientReferralRecordsAndConsultationReferrer.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(14,'13','cleanup orphan patient data','SQL','V13__CleanupOrphanPatientData.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(15,'14','add ai agent config','SQL','V14__add_ai_agent_config.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(16,'15','add ai model provider','SQL','V15__add_ai_model_provider.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(17,'16','add api type to model provider','SQL','V16__add_api_type_to_model_provider.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(18,'17','medical record phrases','SQL','V17__MedicalRecordPhrases.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(19,'18','add followup project','SQL','V18__add_followup_project.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(20,'19','add appointment clinic status','SQL','V19__add_appointment_clinic_status.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(21,'20','enhance consultation','SQL','V20__enhance_consultation.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(22,'21','relax consultation created by','SQL','V21__relax_consultation_created_by.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(23,'22','ai medical record expand','SQL','V22__ai_medical_record_expand.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(24,'23','doctor schedule enhancements','SQL','V23__DoctorScheduleEnhancements.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(25,'24','add treatment scene','SQL','V24__add_treatment_scene.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(26,'25','medical record ai field default value','SQL','V25__medical_record_ai_field_default_value.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(27,'26','ai overview','SQL','V26__ai_overview.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(28,'27','ai function extensions','SQL','V27__ai_function_extensions.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(29,'28','ai agent config add endpoint','SQL','V28__ai_agent_config_add_endpoint.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(30,'29','remove system default ai agents','SQL','V29__remove_system_default_ai_agents.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(31,'30','patient referral records and consultation referrer','SQL','V30__PatientReferralRecordsAndConsultationReferrer.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(32,'31','patient insight summary','SQL','V31__PatientInsightSummary.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(33,'32','advertising spending module','SQL','V32__AdvertisingSpendingModule.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(34,'33','cleanup orphan patient data','SQL','V33__CleanupOrphanPatientData.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(35,'34','add ai agent config','SQL','V34__add_ai_agent_config.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(36,'35','add ai model provider','SQL','V35__add_ai_model_provider.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(37,'36','add api type to model provider','SQL','V36__add_api_type_to_model_provider.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(38,'37','medical record phrases','SQL','V37__MedicalRecordPhrases.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(39,'38','add followup project','SQL','V38__add_followup_project.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(40,'39','add appointment clinic status','SQL','V39__add_appointment_clinic_status.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(41,'40','enhance consultation','SQL','V40__enhance_consultation.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(42,'41','relax consultation created by','SQL','V41__relax_consultation_created_by.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(43,'42','ai medical record expand','SQL','V42__ai_medical_record_expand.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(44,'43','doctor schedule enhancements','SQL','V43__DoctorScheduleEnhancements.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(45,'44','add treatment scene','SQL','V44__add_treatment_scene.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(46,'45','medical record ai field default value','SQL','V45__medical_record_ai_field_default_value.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(47,'46','ai overview','SQL','V46__ai_overview.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(48,'47','ai function extensions','SQL','V47__ai_function_extensions.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(49,'48','ai agent config add endpoint','SQL','V48__ai_agent_config_add_endpoint.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(50,'49','remove system default ai agents','SQL','V49__remove_system_default_ai_agents.sql',NULL,'clinic_user','2026-05-13 19:23:51',0,1),(51,'50','ai function full coverage','SQL','V50__ai_function_full_coverage.sql',-545258733,'clinic_user','2026-05-14 04:40:21',4,1),(52,'52','clean ai agent config','SQL','V52__clean_ai_agent_config.sql',-1355986314,'clinic_user','2026-05-14 14:16:45',14,1),(53,'53','ai agent ui config','SQL','V53__ai_agent_ui_config.sql',1095227845,'clinic_user','2026-05-14 14:20:03',11,1),(54,'54','api key and ai webhook','SQL','V54__api_key_and_ai_webhook.sql',-1530366941,'clinic_user','2026-05-15 03:31:55',16,1),(55,'55','migrate ai webhook to agent config','SQL','V55__migrate_ai_webhook_to_agent_config.sql',-652311053,'clinic_user','2026-05-15 14:37:02',1,1),(56,'56','drop ai webhook','SQL','V56__drop_ai_webhook.sql',-1106492470,'clinic_user','2026-05-15 14:55:23',5,1),(57,'57','drop is system default','SQL','V57__drop_is_system_default.sql',-478947987,'clinic_user','2026-05-15 14:55:26',4,1),(58,'58','add usage location','SQL','V58__add_usage_location.sql',116641501,'clinic_user','2026-05-15 15:26:47',9,1),(59,'59','add ai function mapping','SQL','V59__add_ai_function_mapping.sql',-2060140645,'clinic_user','2026-05-18 01:40:07',9,1),(60,'60','update ai function mapping visibility','SQL','V60__update_ai_function_mapping_visibility.sql',-1911157359,'clinic_user','2026-05-18 02:14:08',3,1),(61,'61','fix ai function config data','SQL','V61__fix_ai_function_config_data.sql',-475741908,'clinic_user','2026-05-18 11:34:47',6,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保平台配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insurance_config`
--

LOCK TABLES `insurance_config` WRITE;
/*!40000 ALTER TABLE `insurance_config` DISABLE KEYS */;
INSERT INTO `insurance_config` VALUES (1,'bp_picc','中国人民保险','https://api.picc.com.cn/dental','PICC-BJ-001','人保北京分公司','picc_app_001','secret_key_001','sign_key_001','RSA','110000',1,'{\"contractNo\":\"2026-BJ-001\",\"settleCycle\":\"monthly\"}','2026-05-08 17:25:35','2026-05-08 17:25:35',NULL),(2,'bp_paic','中国平安保险','https://api.pingan.com/dental','PAIC-SH-001','平安上海分公司','paic_app_001','secret_key_002','sign_key_002','RSA','310000',1,'{\"contractNo\":\"2026-SH-001\",\"settleCycle\":\"monthly\"}','2026-05-08 17:25:35','2026-05-08 17:25:35',NULL),(3,'bp_cpic','中国太平洋保险','https://api.cpic.com.cn/dental','CPIC-GZ-001','太保广州分公司','cpic_app_001','secret_key_003','sign_key_003','AES','440100',0,'{\"contractNo\":\"2026-GZ-001\",\"settleCycle\":\"quarterly\"}','2026-05-08 17:25:35','2026-05-08 17:25:35',NULL);
/*!40000 ALTER TABLE `insurance_config` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_insurance_log_operation` (`operation_type`),
  KEY `idx_insurance_log_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保接口操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insurance_operation_log`
--

LOCK TABLES `insurance_operation_log` WRITE;
/*!40000 ALTER TABLE `insurance_operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `insurance_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_insurance_patient` (`patient_id`),
  KEY `idx_insurance_person_no` (`insurance_person_no`),
  CONSTRAINT `fk_insurance_patient_profile_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者医保档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insurance_patient_profile`
--

LOCK TABLES `insurance_patient_profile` WRITE;
/*!40000 ALTER TABLE `insurance_patient_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `insurance_patient_profile` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
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
-- Dumping data for table `insurance_settlement`
--

LOCK TABLES `insurance_settlement` WRITE;
/*!40000 ALTER TABLE `insurance_settlement` DISABLE KEYS */;
/*!40000 ALTER TABLE `insurance_settlement` ENABLE KEYS */;
UNLOCK TABLES;

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
  `price` decimal(12,2) DEFAULT NULL COMMENT 'ä»·æ ¼',
  `product_batch` varchar(50) DEFAULT NULL COMMENT '产品批次',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_category` (`category`),
  KEY `idx_supplier` (`supplier`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,'树脂材料','耗材','3M','3M中国','Z350','支',50,0,80.00,NULL,'default'),(2,'麻药','药品','赛诺菲','赛诺菲','斯康杜尼','支',30,0,25.00,NULL,'default'),(3,'牙线','护理','宝洁','宝洁','欧乐B','盒',100,0,15.00,NULL,'default'),(4,'手套','耗材','稳健','稳健医疗','一次性乳胶M号','双',500,0,0.50,NULL,'default'),(5,'口罩','耗材','稳健','稳健医疗','医用外科','个',200,0,0.30,NULL,'default'),(6,'树脂材料','耗材','3M','3M中国','Z350','支',50,0,80.00,NULL,'default'),(7,'麻药','药品','赛诺菲','赛诺菲','斯康杜尼','支',30,0,25.00,NULL,'default'),(8,'牙线','护理','宝洁','宝洁','欧乐B','盒',100,0,15.00,NULL,'default'),(9,'手套','耗材','稳健','稳健医疗','一次性乳胶M号','双',500,0,0.50,NULL,'default'),(10,'口罩','耗材','稳健','稳健医疗','医用外科','个',200,0,0.30,NULL,'default');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_lab_bill_items_bill_id` (`bill_id`),
  KEY `idx_lab_bill_items_match_status` (`match_status`),
  KEY `idx_lab_bill_items_matched_order` (`matched_lab_order_id`),
  KEY `idx_lab_bill_items_resolution_status` (`resolution_status`),
  CONSTRAINT `fk_lab_bill_items_bill` FOREIGN KEY (`bill_id`) REFERENCES `lab_bills` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿账单条目';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_bill_items`
--

LOCK TABLES `lab_bill_items` WRITE;
/*!40000 ALTER TABLE `lab_bill_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_bill_items` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_lab_bill_templates_factory_id` (`factory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿账单模板配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_bill_templates`
--

LOCK TABLES `lab_bill_templates` WRITE;
/*!40000 ALTER TABLE `lab_bill_templates` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_bill_templates` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_lab_bill_unmatched_orders_bill_id` (`bill_id`),
  KEY `idx_lab_bill_unmatched_orders_order_id` (`lab_order_id`),
  KEY `idx_lab_bill_unmatched_orders_resolution_status` (`resolution_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿账单仅系统有订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_bill_unmatched_orders`
--

LOCK TABLES `lab_bill_unmatched_orders` WRITE;
/*!40000 ALTER TABLE `lab_bill_unmatched_orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_bill_unmatched_orders` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_bills_factory_month` (`factory_id`,`bill_month`),
  KEY `idx_lab_bills_status` (`status`),
  KEY `idx_lab_bills_bill_month` (`bill_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿月度账单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_bills`
--

LOCK TABLES `lab_bills` WRITE;
/*!40000 ALTER TABLE `lab_bills` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_bills` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_lab_factories_name` (`name`),
  KEY `idx_lab_factories_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿加工厂档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_factories`
--

LOCK TABLES `lab_factories` WRITE;
/*!40000 ALTER TABLE `lab_factories` DISABLE KEYS */;
INSERT INTO `lab_factories` VALUES (1,'精艺齿科','张师傅','13600136001','北京市昌平区',NULL,'1','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,'美牙工坊','李师傅','13600136002','北京市通州区',NULL,'1','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,'康泰义齿','王师傅','13600136003','北京市大兴区',NULL,'1','2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `lab_factories` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_lab_factory_products_factory_id` (`factory_id`),
  KEY `idx_lab_factory_products_name` (`product_name`),
  KEY `idx_lab_factory_products_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='加工厂产品价格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_factory_products`
--

LOCK TABLES `lab_factory_products` WRITE;
/*!40000 ALTER TABLE `lab_factory_products` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_factory_products` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
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
  KEY `idx_lab_orders_factory_status_date` (`factory_id`,`status`,`order_date`),
  CONSTRAINT `fk_lab_orders_medical_record` FOREIGN KEY (`medical_record_id`) REFERENCES `medical_records` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_mro` FOREIGN KEY (`medical_record_operation_id`) REFERENCES `medical_record_operations` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_operation` FOREIGN KEY (`operation_id`) REFERENCES `treatment_operations` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `fk_lab_orders_project` FOREIGN KEY (`project_id`) REFERENCES `treatment_projects` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lab_orders_treatment` FOREIGN KEY (`treatment_id`) REFERENCES `treatment` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿加工订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_orders`
--

LOCK TABLES `lab_orders` WRITE;
/*!40000 ALTER TABLE `lab_orders` DISABLE KEYS */;
INSERT INTO `lab_orders` VALUES (1,1,'精艺齿科',1,'张三',NULL,NULL,NULL,NULL,'全瓷冠',NULL,NULL,NULL,'上颌左6全瓷冠',NULL,1200.00,1,1200.00,'2026-06-01','2026-06-08',NULL,'已完工',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,2,'美牙工坊',2,'李四',NULL,NULL,NULL,NULL,'保持器',NULL,NULL,NULL,'下颌保持器',NULL,300.00,1,300.00,'2026-06-05','2026-06-12',NULL,'制作中',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,1,'精艺齿科',3,'王五',NULL,NULL,NULL,NULL,'种植冠',NULL,NULL,NULL,'上颌右5种植冠',NULL,1500.00,1,1500.00,'2026-06-03','2026-06-10',NULL,'已送件',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `lab_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login_log`
--

DROP TABLE IF EXISTS `login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `user_id` int NOT NULL COMMENT 'ç”¨æˆ·ID',
  `username` varchar(50) NOT NULL COMMENT 'ç”¨æˆ·å',
  `login_type` varchar(20) DEFAULT 'password' COMMENT 'ç™»å½•æ–¹å¼ï¼špassword/wechat',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'ç™»å½•IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'æµè§ˆå™¨UA',
  `login_status` varchar(20) DEFAULT 'success' COMMENT 'çŠ¶æ€ï¼šsuccess/failure',
  `failure_reason` varchar(200) DEFAULT NULL COMMENT 'å¤±è´¥åŽŸå› ',
  `logout_at` datetime DEFAULT NULL COMMENT 'ç™»å‡ºæ—¶é—´',
  `session_duration_seconds` int DEFAULT NULL COMMENT 'ä¼šè¯æ—¶é•¿ï¼ˆç§’ï¼‰',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_login_log_user` (`user_id`),
  KEY `idx_login_log_status` (`login_status`),
  KEY `idx_login_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login_log`
--

LOCK TABLES `login_log` WRITE;
/*!40000 ALTER TABLE `login_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `login_log` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_material_categories_parent_id` (`parent_id`),
  KEY `idx_material_categories_status` (`status`),
  KEY `idx_material_categories_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_categories`
--

LOCK TABLES `material_categories` WRITE;
/*!40000 ALTER TABLE `material_categories` DISABLE KEYS */;
INSERT INTO `material_categories` VALUES (1,'种植类',0,10,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19',NULL),(2,'正畸类',0,20,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19',NULL),(3,'修复类',0,30,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19',NULL),(4,'基础耗材',0,40,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19',NULL),(5,'其他',0,50,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19',NULL);
/*!40000 ALTER TABLE `material_categories` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_material_purchase_items_purchase_id` (`purchase_id`),
  KEY `idx_material_purchase_items_material_id` (`material_id`),
  CONSTRAINT `fk_material_purchase_items_material` FOREIGN KEY (`material_id`) REFERENCES `materials` (`id`),
  CONSTRAINT `fk_material_purchase_items_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `material_purchases` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材采购单明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_purchase_items`
--

LOCK TABLES `material_purchase_items` WRITE;
/*!40000 ALTER TABLE `material_purchase_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `material_purchase_items` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_material_purchases_purchase_date` (`purchase_date`),
  KEY `idx_material_purchases_supplier_name` (`supplier_name`),
  KEY `idx_material_purchases_status` (`status`),
  KEY `idx_material_purchases_finance_record_id` (`finance_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材采购单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_purchases`
--

LOCK TABLES `material_purchases` WRITE;
/*!40000 ALTER TABLE `material_purchases` DISABLE KEYS */;
/*!40000 ALTER TABLE `material_purchases` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_materials_category_id` (`category_id`),
  KEY `idx_materials_status` (`status`),
  KEY `idx_materials_name` (`name`),
  KEY `idx_materials_brand` (`brand`),
  KEY `idx_materials_category_status_name` (`category_id`,`status`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `materials`
--

LOCK TABLES `materials` WRITE;
/*!40000 ALTER TABLE `materials` DISABLE KEYS */;
INSERT INTO `materials` VALUES (1,'种植体','韩国奥齿泰 4.0*10mm','奥齿泰',1,'种植类','颗',5,20,'1','常用型号','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(2,'基台','钛合金','奥齿泰',1,'种植类','个',5,30,'1','配套种植体','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(3,'矫正托槽','金属自锁','ORMCO',2,'矫正类','副',3,15,'1','Damon Q','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(4,'镍钛丝','0.014英寸','ORMCO',2,'矫正类','根',10,50,'1','矫正用','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(5,'印模材','藻酸盐','登士柏',3,'耗材类','包',10,40,'1','常规取模','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(6,'种植体','韩国奥齿泰 4.0*10mm','奥齿泰',1,'种植类','颗',5,20,'1','常用型号','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(7,'基台','钛合金','奥齿泰',1,'种植类','个',5,30,'1','配套种植体','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(8,'矫正托槽','金属自锁','ORMCO',2,'矫正类','副',3,15,'1','Damon Q','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(9,'镍钛丝','0.014英寸','ORMCO',2,'矫正类','根',10,50,'1','矫正用','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(10,'印模材','藻酸盐','登士柏',3,'耗材类','包',10,40,'1','常规取模','2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `materials` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_record_ai_field`
--

DROP TABLE IF EXISTS `medical_record_ai_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record_ai_field` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `field_key` varchar(50) NOT NULL COMMENT '字段标识',
  `field_name` varchar(50) DEFAULT NULL COMMENT '字段中文名',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用扩写',
  `max_length` int DEFAULT NULL COMMENT '最大长度',
  `is_required` tinyint(1) DEFAULT '0' COMMENT '是否必填',
  `validation_rule` varchar(255) DEFAULT NULL COMMENT '校验规则正则',
  `validation_hint` varchar(255) DEFAULT NULL COMMENT '校验失败提示',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `default_value` varchar(500) DEFAULT NULL COMMENT '字段默认值，医生未填写时直接返回此值',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_field_key` (`field_key`)
) ENGINE=InnoDB AUTO_INCREMENT=394 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历扩写字段规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_record_ai_field`
--

LOCK TABLES `medical_record_ai_field` WRITE;
/*!40000 ALTER TABLE `medical_record_ai_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `medical_record_ai_field` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
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
-- Dumping data for table `medical_record_operations`
--

LOCK TABLES `medical_record_operations` WRITE;
/*!40000 ALTER TABLE `medical_record_operations` DISABLE KEYS */;
/*!40000 ALTER TABLE `medical_record_operations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_record_phrases`
--

DROP TABLE IF EXISTS `medical_record_phrases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record_phrases` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `field_type` varchar(50) NOT NULL COMMENT 'å­—æ®µç±»åž‹',
  `content` varchar(500) NOT NULL COMMENT 'è¯æ¡å†…å®¹',
  `category` varchar(50) DEFAULT '' COMMENT 'è¯æ¡åˆ†ç±»',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT 'æŽ’åºå€¼',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ï¼š1=å¯ç”¨ï¼Œ0=åœç”¨',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_field_type_status` (`field_type`,`status`),
  KEY `idx_category` (`category`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç—…åŽ†å¸¸ç”¨è¯æ¡';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_record_phrases`
--

LOCK TABLES `medical_record_phrases` WRITE;
/*!40000 ALTER TABLE `medical_record_phrases` DISABLE KEYS */;
INSERT INTO `medical_record_phrases` VALUES (1,'chief_complaint','牙齿疼痛','主诉',1,1,'2026-06-11 04:23:38','2026-06-11 04:23:38',NULL),(2,'chief_complaint','矫正复诊','主诉',2,1,'2026-06-11 04:23:38','2026-06-11 04:23:38',NULL),(3,'diagnosis','急性牙髓炎','诊断',1,1,'2026-06-11 04:23:38','2026-06-11 04:23:38',NULL),(4,'diagnosis','慢性牙周炎','诊断',2,1,'2026-06-11 04:23:38','2026-06-11 04:23:38',NULL),(5,'treatment_plan','根管治疗','治疗方案',1,1,'2026-06-11 04:23:38','2026-06-11 04:23:38',NULL),(6,'treatment_plan','拔除患牙','治疗方案',2,1,'2026-06-11 04:23:38','2026-06-11 04:23:38',NULL);
/*!40000 ALTER TABLE `medical_record_phrases` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_medical_record_template_status` (`status`,`id`),
  KEY `idx_medical_record_template_creator` (`created_by`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历模板库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_record_template`
--

LOCK TABLES `medical_record_template` WRITE;
/*!40000 ALTER TABLE `medical_record_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `medical_record_template` ENABLE KEYS */;
UNLOCK TABLES;

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
  `treatment_generated` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦å·²ç”Ÿæˆå¤„ç½®è®°å½•ï¼š0-å¦ 1-æ˜¯',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_visit_date` (`visit_date`),
  KEY `idx_medical_records_doctor_account_id` (`doctor_account_id`),
  KEY `idx_medical_records_patient_visit` (`patient_id`,`visit_date`),
  CONSTRAINT `fk_medical_records_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_records`
--

LOCK TABLES `medical_records` WRITE;
/*!40000 ALTER TABLE `medical_records` DISABLE KEYS */;
INSERT INTO `medical_records` VALUES (1,1,'张三',1,'王医生',NULL,NULL,'2026-06-10 00:00:00','初诊','牙痛3天',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'急性牙髓炎','根管治疗','开髓引流，缓解疼痛',NULL,NULL,NULL,NULL,NULL,'预约下次复诊','final',0,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(2,2,'李四',2,'李医生',NULL,NULL,'2026-06-08 00:00:00','初诊','矫正器脱落',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'正畸治疗中','重新粘接托槽','更换弓丝，调整力度',NULL,NULL,NULL,NULL,NULL,NULL,'final',0,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(3,3,'王五',4,'刘医生',NULL,NULL,'2026-06-05 00:00:00','初诊','种植牙复查',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'种植术后3个月','二期手术评估','愈合良好，准备二期',NULL,NULL,NULL,NULL,NULL,NULL,'final',0,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(4,4,'赵六',1,'王医生',NULL,NULL,'2026-06-01 00:00:00','初诊','牙龈出血',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'慢性牙周炎','牙周基础治疗','洗牙+龈下刮治',NULL,NULL,NULL,NULL,NULL,'建议3个月复查','final',0,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(5,5,'孙七',3,'张医生',NULL,NULL,'2026-06-10 00:00:00','初诊','智齿疼痛',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'智齿冠周炎','消炎后拔除','冲洗上药，预约拔牙',NULL,NULL,NULL,NULL,NULL,NULL,'final',0,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(6,1,'张三',1,'王医生',NULL,NULL,'2026-06-10 00:00:00','初诊','牙痛3天',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'急性牙髓炎','根管治疗','开髓引流，缓解疼痛',NULL,NULL,NULL,NULL,NULL,'预约下次复诊','final',0,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(7,2,'李四',2,'李医生',NULL,NULL,'2026-06-08 00:00:00','初诊','矫正器脱落',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'正畸治疗中','重新粘接托槽','更换弓丝，调整力度',NULL,NULL,NULL,NULL,NULL,NULL,'final',0,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(8,3,'王五',4,'刘医生',NULL,NULL,'2026-06-05 00:00:00','初诊','种植牙复查',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'种植术后3个月','二期手术评估','愈合良好，准备二期',NULL,NULL,NULL,NULL,NULL,NULL,'final',0,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(9,4,'赵六',1,'王医生',NULL,NULL,'2026-06-01 00:00:00','初诊','牙龈出血',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'慢性牙周炎','牙周基础治疗','洗牙+龈下刮治',NULL,NULL,NULL,NULL,NULL,'建议3个月复查','final',0,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(10,5,'孙七',3,'张医生',NULL,NULL,'2026-06-10 00:00:00','初诊','智齿疼痛',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'智齿冠周炎','消炎后拔除','冲洗上药，预约拔牙',NULL,NULL,NULL,NULL,NULL,NULL,'final',0,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `medical_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `operator_id` int DEFAULT NULL COMMENT 'æ“ä½œäººID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT 'æ“ä½œäººå§“å',
  `operator_role` varchar(30) DEFAULT NULL COMMENT 'æ“ä½œäººè§’è‰²',
  `operation_type` varchar(50) NOT NULL COMMENT 'æ“ä½œç±»åž‹',
  `target_type` varchar(50) DEFAULT NULL COMMENT 'æ“ä½œå¯¹è±¡ç±»åž‹',
  `target_id` varchar(100) DEFAULT NULL COMMENT 'æ“ä½œå¯¹è±¡ID',
  `target_name` varchar(200) DEFAULT NULL COMMENT 'æ“ä½œå¯¹è±¡åç§°ï¼ˆå†—ä½™ï¼‰',
  `operation_desc` varchar(1000) DEFAULT NULL COMMENT 'æ“ä½œæè¿°',
  `old_value` mediumtext COMMENT 'ä¿®æ”¹å‰æ•°æ® JSON',
  `new_value` mediumtext COMMENT 'ä¿®æ”¹åŽæ•°æ® JSON',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'æ“ä½œIP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'æµè§ˆå™¨UA',
  `status` varchar(20) DEFAULT 'success' COMMENT 'çŠ¶æ€ï¼šsuccess/failure',
  `error_message` varchar(1000) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operator` (`operator_id`),
  KEY `idx_operation_log_type` (`operation_type`),
  KEY `idx_operation_log_target` (`target_type`,`target_id`),
  KEY `idx_operation_log_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å…¨å±€æ“ä½œå®¡è®¡æ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
INSERT INTO `operation_log` VALUES (1,1,'admin','管理员','新增','患者',NULL,'张三','新增患者信息',NULL,NULL,'127.0.0.1',NULL,'1',NULL,'2026-06-11 04:23:38',NULL),(2,1,'admin','管理员','新增','预约',NULL,'张三-王医生','预约2026-06-12 09:00',NULL,NULL,'127.0.0.1',NULL,'1',NULL,'2026-06-11 04:23:38',NULL),(3,1,'admin','管理员','收款','财务',NULL,'张三','根管治疗收款600元',NULL,NULL,'127.0.0.1',NULL,'1',NULL,'2026-06-11 04:23:38',NULL);
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_patient_consent_patient_id` (`patient_id`),
  KEY `idx_patient_consent_status` (`status`),
  KEY `idx_patient_consent_doctor_account_id` (`doctor_account_id`),
  KEY `idx_patient_consent_signed_at` (`signed_at`),
  CONSTRAINT `fk_patient_consent_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者电子知情同意书';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_consent`
--

LOCK TABLES `patient_consent` WRITE;
/*!40000 ALTER TABLE `patient_consent` DISABLE KEYS */;
INSERT INTO `patient_consent` VALUES (1,1,'张三',1,'王医生','根管治疗知情同意书','我已了解根管治疗的流程、风险及预后，同意接受治疗。','1','2026-06-11 04:23:38',NULL,'2026-06-10 10:00:00',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,3,'王五',4,'刘医生','种植牙手术知情同意书','我已了解种植牙手术的流程、风险及预后，同意接受手术。','1','2026-06-11 04:23:38',NULL,'2026-03-10 09:00:00',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,5,'孙七',3,'张医生','拔牙手术知情同意书','我已了解拔牙手术的流程、风险及预后，同意接受手术。','1','2026-06-11 04:23:38',NULL,'2026-06-10 14:00:00',NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `patient_consent` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_custom_group_key` (`group_key`),
  KEY `idx_patient_custom_group_status_sort` (`status`,`sort_order`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者自定义分组';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_custom_group`
--

LOCK TABLES `patient_custom_group` WRITE;
/*!40000 ALTER TABLE `patient_custom_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_custom_group` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_custom_group_member` (`group_id`,`patient_id`),
  KEY `idx_patient_custom_group_member_patient_id` (`patient_id`),
  CONSTRAINT `fk_patient_custom_group_member_group_id` FOREIGN KEY (`group_id`) REFERENCES `patient_custom_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者自定义分组成员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_custom_group_member`
--

LOCK TABLES `patient_custom_group_member` WRITE;
/*!40000 ALTER TABLE `patient_custom_group_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_custom_group_member` ENABLE KEYS */;
UNLOCK TABLES;

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
  `followup_project` varchar(100) DEFAULT NULL COMMENT '回访项目',
  `summary` varchar(500) DEFAULT NULL COMMENT '随访摘要',
  `source_type` varchar(20) DEFAULT 'manual' COMMENT 'æ¥æºï¼šmanual/ai',
  `next_followup_date` datetime DEFAULT NULL COMMENT '下次随访时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_followup_date` (`followup_date`),
  KEY `idx_next_followup_date` (`next_followup_date`),
  KEY `idx_followup_doctor_account_id` (`doctor_account_id`),
  CONSTRAINT `fk_patient_followup_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者随访记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_followup`
--

LOCK TABLES `patient_followup` WRITE;
/*!40000 ALTER TABLE `patient_followup` DISABLE KEYS */;
INSERT INTO `patient_followup` VALUES (1,1,1,'王医生','2026-06-11 00:00:00','治疗后随访',NULL,'术后疼痛缓解，无不适','manual','2026-06-18 00:00:00','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,3,4,'刘医生','2026-06-06 00:00:00','种植术后随访',NULL,'愈合良好，无红肿','manual','2026-06-20 00:00:00','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,4,1,'王医生','2026-06-02 00:00:00','牙周随访',NULL,'牙龈出血减少','manual','2026-09-02 00:00:00','2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `patient_followup` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_patient_images_sent` (`patient_id`,`sent_to_patient`),
  CONSTRAINT `fk_patient_images_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者影像记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_images`
--

LOCK TABLES `patient_images` WRITE;
/*!40000 ALTER TABLE `patient_images` DISABLE KEYS */;
INSERT INTO `patient_images` VALUES (1,1,'张三','全景片','X光片',NULL,'/uploads/patient-images/1_panorama.jpg','初诊拍摄',0,NULL,'2026-06-11 04:23:38','default'),(2,3,'王五','种植CT','CT',NULL,'/uploads/patient-images/3_ct.jpg','种植术前',0,NULL,'2026-06-11 04:23:38','default'),(3,5,'孙七','智齿片','X光片',NULL,'/uploads/patient-images/5_xray.jpg','拔智齿前',0,NULL,'2026-06-11 04:23:38','default');
/*!40000 ALTER TABLE `patient_images` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`patient_id`),
  KEY `idx_pis_last_visit_date` (`last_visit_date`),
  KEY `idx_pis_total_spent` (`total_spent`),
  KEY `idx_pis_high_value_flag` (`high_value_flag`),
  KEY `idx_pis_lost_risk_flag` (`lost_risk_flag`),
  KEY `idx_pis_word_of_mouth_flag` (`word_of_mouth_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者洞察汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_insight_summary`
--

LOCK TABLES `patient_insight_summary` WRITE;
/*!40000 ALTER TABLE `patient_insight_summary` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_insight_summary` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_referral_patient_id` (`patient_id`),
  KEY `idx_referrer_patient_id` (`referrer_patient_id`),
  KEY `idx_referral_created_at` (`created_at`),
  KEY `idx_referral_consultation_id` (`consultation_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者转介绍记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_referral_records`
--

LOCK TABLES `patient_referral_records` WRITE;
/*!40000 ALTER TABLE `patient_referral_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_referral_records` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_risk_patient_tag` (`patient_id`,`tag_code`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_risk_status` (`status`),
  CONSTRAINT `fk_patient_risk_tag_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者风险标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_risk_tag`
--

LOCK TABLES `patient_risk_tag` WRITE;
/*!40000 ALTER TABLE `patient_risk_tag` DISABLE KEYS */;
INSERT INTO `patient_risk_tag` VALUES (1,1,'HYPERTENSION','高血压',0,'患者自述',1,'需监测血压','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,1,'PENICILLIN_ALLERGY','青霉素过敏',0,'患者自述',1,'禁用青霉素类药物','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,4,'PERIODONTITIS','牙周炎',0,'医生诊断',1,'定期牙周维护','2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `patient_risk_tag` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_event_time` (`event_time`),
  CONSTRAINT `fk_patient_timeline_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者时间线';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_timeline`
--

LOCK TABLES `patient_timeline` WRITE;
/*!40000 ALTER TABLE `patient_timeline` DISABLE KEYS */;
INSERT INTO `patient_timeline` VALUES (1,1,'2026-01-15 09:00:00','初诊','首次就诊','口腔检查',NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(2,1,'2026-06-10 10:00:00','治疗','根管治疗','根管治疗第一次',NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(3,2,'2026-02-20 09:00:00','初诊','矫正咨询','矫正方案制定',NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(4,2,'2026-06-08 10:30:00','治疗','矫正复诊','更换弓丝，调整力度',NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(5,3,'2026-03-10 09:00:00','初诊','种植手术','单颗种植手术',NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(6,3,'2026-06-05 14:00:00','复查','种植复查','术后3个月复查',NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `patient_timeline` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scene_key` (`scene_key`),
  KEY `idx_patient_wechat_bind_scene_patient_id` (`patient_id`),
  CONSTRAINT `fk_patient_wechat_bind_scene_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_wechat_bind_scene`
--

LOCK TABLES `patient_wechat_bind_scene` WRITE;
/*!40000 ALTER TABLE `patient_wechat_bind_scene` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_wechat_bind_scene` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_phone` (`phone`),
  KEY `idx_wechat_openid` (`wechat_openid`),
  KEY `idx_patients_customer_source` (`customer_source`),
  KEY `idx_patients_created_at` (`created_at`),
  KEY `idx_patients_name_pinyin` (`name_pinyin`),
  KEY `idx_patients_name_initials` (`name_initials`),
  KEY `fk_patients_related_patient` (`related_patient_id`),
  KEY `idx_patients_name_phone` (`name`,`phone`),
  CONSTRAINT `fk_patients_related_patient` FOREIGN KEY (`related_patient_id`) REFERENCES `patients` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (1,'张三',NULL,NULL,'男',35,NULL,'13900139001',NULL,'北京市朝阳区',NULL,NULL,NULL,NULL,'美团','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(2,'李四',NULL,NULL,'女',28,NULL,'13900139002',NULL,'北京市海淀区',NULL,NULL,NULL,NULL,'转介绍','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(3,'王五',NULL,NULL,'男',45,NULL,'13900139003',NULL,'北京市东城区',NULL,NULL,NULL,NULL,'美团','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(4,'赵六',NULL,NULL,'女',52,NULL,'13900139004',NULL,'北京市西城区',NULL,NULL,NULL,NULL,'自然到店','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(5,'孙七',NULL,NULL,'男',18,NULL,'13900139005',NULL,'北京市丰台区',NULL,NULL,NULL,NULL,'抖音','2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(6,'张三',NULL,NULL,'男',35,NULL,'13900139001',NULL,'北京市朝阳区',NULL,NULL,NULL,NULL,'美团','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(7,'李四',NULL,NULL,'女',28,NULL,'13900139002',NULL,'北京市海淀区',NULL,NULL,NULL,NULL,'转介绍','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(8,'王五',NULL,NULL,'男',45,NULL,'13900139003',NULL,'北京市东城区',NULL,NULL,NULL,NULL,'美团','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(9,'赵六',NULL,NULL,'女',52,NULL,'13900139004',NULL,'北京市西城区',NULL,NULL,NULL,NULL,'自然到店','2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(10,'孙七',NULL,NULL,'男',18,NULL,'13900139005',NULL,'北京市丰台区',NULL,NULL,NULL,NULL,'抖音','2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收款渠道表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_channel`
--

LOCK TABLES `payment_channel` WRITE;
/*!40000 ALTER TABLE `payment_channel` DISABLE KEYS */;
INSERT INTO `payment_channel` VALUES (1,'现金',1,10,'2026-05-06 19:35:19','2026-06-11 04:27:36','default'),(2,'微信',1,20,'2026-05-06 19:35:19','2026-06-11 04:27:36','default'),(3,'支付宝',1,30,'2026-05-06 19:35:19','2026-06-11 04:27:36','default'),(4,'银行卡',1,40,'2026-05-06 19:35:19','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `payment_channel` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_portal_access_token` (`token`),
  KEY `idx_portal_access_token_type` (`token_type`),
  KEY `idx_portal_access_token_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门户访问令牌';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portal_access_token`
--

LOCK TABLES `portal_access_token` WRITE;
/*!40000 ALTER TABLE `portal_access_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `portal_access_token` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_operation_relations` (`project_id`,`operation_id`),
  KEY `idx_project_operation_relations_operation_id` (`operation_id`),
  KEY `idx_project_operation_relations_order` (`project_id`,`operation_order`),
  CONSTRAINT `fk_por_operation` FOREIGN KEY (`operation_id`) REFERENCES `treatment_operations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_por_project` FOREIGN KEY (`project_id`) REFERENCES `treatment_projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目-操作关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_operation_relations`
--

LOCK TABLES `project_operation_relations` WRITE;
/*!40000 ALTER TABLE `project_operation_relations` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_operation_relations` ENABLE KEYS */;
UNLOCK TABLES;

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
  `price` decimal(12,2) DEFAULT NULL COMMENT 'ä»·æ ¼',
  `status` varchar(20) DEFAULT NULL COMMENT '状态',
  `createdate` datetime DEFAULT NULL COMMENT '创建日期',
  `purchasedate` datetime DEFAULT NULL COMMENT '采购日期',
  `indate` datetime DEFAULT NULL COMMENT '入库日期',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_category` (`category`),
  KEY `idx_supplier` (`supplier`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchases`
--

LOCK TABLES `purchases` WRITE;
/*!40000 ALTER TABLE `purchases` DISABLE KEYS */;
INSERT INTO `purchases` VALUES (1,'树脂材料','耗材','3M','3M中国','Z350','支',20,80.00,'已入库','2026-05-20 00:00:00','2026-05-20 00:00:00','2026-05-21 00:00:00','default'),(2,'种植体','种植','奥齿泰','奥齿泰','4.0*10mm','颗',10,1200.00,'已入库','2026-05-25 00:00:00','2026-05-25 00:00:00','2026-05-26 00:00:00','default'),(3,'手套','耗材','稳健','稳健医疗','乳胶M号','双',200,0.50,'已入库','2026-06-01 00:00:00','2026-06-01 00:00:00','2026-06-02 00:00:00','default'),(4,'树脂材料','耗材','3M','3M中国','Z350','支',20,80.00,'已入库','2026-05-20 00:00:00','2026-05-20 00:00:00','2026-05-21 00:00:00','default'),(5,'种植体','种植','奥齿泰','奥齿泰','4.0*10mm','颗',10,1200.00,'已入库','2026-05-25 00:00:00','2026-05-25 00:00:00','2026-05-26 00:00:00','default'),(6,'手套','耗材','稳健','稳健医疗','乳胶M号','双',200,0.50,'已入库','2026-06-01 00:00:00','2026-06-01 00:00:00','2026-06-02 00:00:00','default');
/*!40000 ALTER TABLE `purchases` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色导航权限配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_menu_permissions`
--

LOCK TABLES `role_menu_permissions` WRITE;
/*!40000 ALTER TABLE `role_menu_permissions` DISABLE KEYS */;
INSERT INTO `role_menu_permissions` VALUES (1,'admin','/home',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(2,'admin','/Patient',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(3,'admin','/MedicalRecord',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(4,'admin','/Followup',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(5,'admin','/Consultation',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(6,'admin','/ConsultationDashboard',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(7,'admin','/advertising-spending',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(8,'admin','/lab-factories',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(9,'admin','/lab-orders',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(10,'admin','/lab-bills',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(11,'admin','/lab-statistics',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(12,'admin','/material-categories',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(13,'admin','/materials',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(14,'admin','/material-purchases',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(15,'admin','/material-statistics',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(16,'admin','/Appointment',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(17,'admin','/Doctor',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(18,'admin','/Financial',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(19,'admin','/Financial2',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(20,'admin','/financial-expenses',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(21,'admin','/InsuranceOverview',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(22,'admin','/InsuranceConfig',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(23,'admin','/InsurancePatientProfile',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(24,'admin','/InsuranceSettlement',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(25,'admin','/InsuranceLog',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(26,'admin','/InsuranceMockPayload',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(27,'admin','/SystemTreatmentCatalog',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(28,'admin','/SystemPaymentChannel',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(29,'admin','/SystemConsentTemplate',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(30,'admin','/SystemAccountPermission',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(31,'admin','/SystemAccountManage',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(32,'admin','/SystemSettings/basic/roles',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(33,'admin','/SystemSettings/basic/clinics',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(34,'admin','/SystemSettings',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(35,'doctor','/home',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(36,'doctor','/Patient',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(37,'doctor','/MedicalRecord',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(38,'doctor','/Followup',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(39,'doctor','/Consultation',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(40,'doctor','/lab-orders',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(41,'doctor','/materials',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(42,'doctor','/material-purchases',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(43,'doctor','/Appointment',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(44,'doctor','/Doctor',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(45,'doctor','/InsuranceOverview',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(46,'doctor','/InsuranceConfig',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(47,'doctor','/InsurancePatientProfile',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(48,'doctor','/InsuranceSettlement',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(49,'doctor','/InsuranceLog',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(50,'doctor','/InsuranceMockPayload',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(51,'doctor','/SystemSettings',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(52,'nurse','/home',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(53,'nurse','/Patient',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(54,'nurse','/MedicalRecord',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(55,'nurse','/Followup',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(56,'nurse','/Consultation',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(57,'nurse','/ConsultationDashboard',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(58,'nurse','/advertising-spending',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(59,'nurse','/lab-orders',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(60,'nurse','/lab-bills',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(61,'nurse','/materials',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(62,'nurse','/material-purchases',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(63,'nurse','/Appointment',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(64,'nurse','/Doctor',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(65,'nurse','/Financial',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(66,'nurse','/Financial2',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(67,'nurse','/financial-expenses',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(68,'nurse','/InsuranceOverview',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(69,'nurse','/InsuranceConfig',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(70,'nurse','/InsurancePatientProfile',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(71,'nurse','/InsuranceSettlement',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(72,'nurse','/InsuranceLog',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(73,'nurse','/InsuranceMockPayload',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(74,'nurse','/SystemTreatmentCatalog',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(75,'nurse','/SystemPaymentChannel',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(76,'nurse','/SystemConsentTemplate',1,'2026-06-03 10:00:00','2026-06-03 10:00:00'),(77,'nurse','/SystemSettings',1,'2026-06-03 10:00:00','2026-06-03 10:00:00');
/*!40000 ALTER TABLE `role_menu_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'admin','管理员','系统管理员，拥有所有权限',1,1,'2026-06-11 04:20:47','2026-06-11 04:20:47'),(2,'doctor','医生','门诊医生，负责诊疗工作',2,1,'2026-06-11 04:20:47','2026-06-11 04:20:47'),(3,'nurse','护士','护士，负责辅助诊疗和护理',3,1,'2026-06-11 04:20:47','2026-06-11 04:20:47');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shift_template`
--

DROP TABLE IF EXISTS `shift_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '来源医生姓名',
  `pattern_json` text NOT NULL COMMENT '模板模式JSON，如{"1":"morning","2":"evening"}',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排班模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shift_template`
--

LOCK TABLES `shift_template` WRITE;
/*!40000 ALTER TABLE `shift_template` DISABLE KEYS */;
INSERT INTO `shift_template` VALUES (1,'早班','王医生','{\"days\":[\"周一\",\"周二\",\"周三\",\"周四\",\"周五\"],\"startTime\":\"09:00\",\"endTime\":\"12:00\"}','2026-06-11 04:23:38','default'),(2,'午班','王医生','{\"days\":[\"周一\",\"周二\",\"周三\",\"周四\",\"周五\"],\"startTime\":\"14:00\",\"endTime\":\"18:00\"}','2026-06-11 04:23:38','default'),(3,'早班','李医生','{\"days\":[\"周一\",\"周三\",\"周五\"],\"startTime\":\"09:00\",\"endTime\":\"12:00\"}','2026-06-11 04:23:38','default'),(4,'午班','张医生','{\"days\":[\"周二\",\"周四\"],\"startTime\":\"14:00\",\"endTime\":\"18:00\"}','2026-06-11 04:23:38','default');
/*!40000 ALTER TABLE `shift_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `config_key` varchar(100) NOT NULL COMMENT 'é…ç½®é”®',
  `config_value` varchar(2000) DEFAULT NULL COMMENT 'é…ç½®å€¼',
  `config_type` varchar(20) DEFAULT 'string' COMMENT 'å€¼ç±»åž‹ï¼šstring/int/boolean/json',
  `description` varchar(500) DEFAULT NULL COMMENT 'é…ç½®è¯´æ˜Ž',
  `category` varchar(50) DEFAULT 'general' COMMENT 'åˆ†ç±»ï¼šsecurity/ai/appointment/wechat',
  `editable` tinyint DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç¼–è¾‘ï¼š0-åªè¯» 1-å¯ç¼–è¾‘',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_config_key` (`config_key`),
  KEY `idx_system_config_category` (`category`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç³»ç»Ÿå…¨å±€é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'security.secondary_password','246810','string','æ‚£è€…æ•æ„Ÿæ“ä½œäºŒçº§å¯†ç ','security',1,'2026-05-07 05:03:10','2026-05-07 05:03:10'),(2,'ai.enabled','false','boolean','AI åŠŸèƒ½æ€»å¼€å…³','ai',1,'2026-05-07 05:03:10','2026-05-07 05:03:10'),(3,'ai.model.default','gpt-5.4-mini','string','é»˜è®¤ AI æ¨¡åž‹','ai',1,'2026-05-07 05:03:10','2026-05-07 05:03:10'),(4,'appointment.reminder.minutes','30','int','é¢„çº¦åˆ°è¾¾å‰æé†’æ—¶é—´ï¼ˆåˆ†é’Ÿï¼‰','appointment',1,'2026-05-07 05:03:10','2026-05-07 05:03:10');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

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
  `finance_id` bigint DEFAULT NULL COMMENT 'å…³è”æ”¶è´¹å•ID',
  `project_id` bigint DEFAULT NULL COMMENT '项目库ID',
  `appointment_purpose` varchar(255) DEFAULT NULL COMMENT '预约目的',
  `status` varchar(20) DEFAULT NULL COMMENT '治疗状态',
  `doctor_account_id` bigint DEFAULT NULL COMMENT '医生账号ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '医生姓名',
  `treatment_date` date DEFAULT NULL COMMENT '治疗日期',
  `treatment_content` text COMMENT '治疗内容',
  `tooth_positions` varchar(255) DEFAULT NULL COMMENT '牙位列表，逗号分隔',
  `treatment_product` varchar(500) DEFAULT NULL COMMENT '使用材料',
  `treatment_fee` decimal(12,2) DEFAULT NULL COMMENT 'æ²»ç–—è´¹ç”¨',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_patient_name` (`patient_name`),
  KEY `idx_treatment_date` (`treatment_date`),
  KEY `idx_treatment_patient_id` (`patient_id`),
  KEY `idx_treatment_doctor_account_id` (`doctor_account_id`),
  KEY `idx_treatment_batch_no` (`batch_no`),
  KEY `idx_treatment_project_id` (`project_id`),
  KEY `idx_treatment_medical_record_id` (`medical_record_id`),
  KEY `idx_treatment_finance_id` (`finance_id`),
  CONSTRAINT `fk_treatment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `fk_treatment_project` FOREIGN KEY (`project_id`) REFERENCES `treatment_projects` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment`
--

LOCK TABLES `treatment` WRITE;
/*!40000 ALTER TABLE `treatment` DISABLE KEYS */;
INSERT INTO `treatment` VALUES (1,1,'张三',NULL,NULL,NULL,NULL,NULL,'进行中',1,'王医生','2026-06-10','开髓引流',NULL,NULL,600.00,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(2,2,'李四',NULL,NULL,NULL,NULL,NULL,'进行中',2,'李医生','2026-06-08','复诊调整',NULL,NULL,0.00,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(3,3,'王五',NULL,NULL,NULL,NULL,NULL,'已完成',4,'刘医生','2026-06-05','术后复查',NULL,NULL,0.00,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(4,4,'赵六',NULL,NULL,NULL,NULL,NULL,'已完成',1,'王医生','2026-06-01','龈下刮治',NULL,NULL,500.00,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(5,5,'孙七',NULL,NULL,NULL,NULL,NULL,'进行中',3,'张医生','2026-06-10','消炎处理',NULL,NULL,150.00,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(6,1,'张三',NULL,NULL,NULL,NULL,NULL,'进行中',1,'王医生','2026-06-10','开髓引流',NULL,NULL,600.00,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(7,2,'李四',NULL,NULL,NULL,NULL,NULL,'进行中',2,'李医生','2026-06-08','复诊调整',NULL,NULL,0.00,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(8,3,'王五',NULL,NULL,NULL,NULL,NULL,'已完成',4,'刘医生','2026-06-05','术后复查',NULL,NULL,0.00,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(9,4,'赵六',NULL,NULL,NULL,NULL,NULL,'已完成',1,'王医生','2026-06-01','龈下刮治',NULL,NULL,500.00,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(10,5,'孙七',NULL,NULL,NULL,NULL,NULL,'进行中',3,'张医生','2026-06-10','消炎处理',NULL,NULL,150.00,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `treatment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `treatment_catalog`
--

DROP TABLE IF EXISTS `treatment_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_catalog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_name` varchar(100) NOT NULL COMMENT '处置收费项目名称',
  `default_fee` decimal(12,2) DEFAULT NULL COMMENT 'é»˜è®¤æ”¶è´¹',
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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处置收费项目库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_catalog`
--

LOCK TABLES `treatment_catalog` WRITE;
/*!40000 ALTER TABLE `treatment_catalog` DISABLE KEYS */;
INSERT INTO `treatment_catalog` VALUES (1,'口腔检查',50.00,'常规口腔检查','检查服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(2,'拍片检查',100.00,'全景片/小牙片','影像服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(3,'CT检查',300.00,'口腔CT','影像服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(4,'根管治疗',1200.00,'前牙根管治疗','治疗服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(5,'拔牙',200.00,'普通拔牙','治疗服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(6,'智齿拔除',800.00,'阻生智齿拔除','治疗服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(7,'口腔检查',50.00,'常规口腔检查','检查服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(8,'拍片检查',100.00,'全景片/小牙片','影像服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(9,'CT检查',300.00,'口腔CT','影像服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(10,'根管治疗',1200.00,'前牙根管治疗','治疗服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(11,'拔牙',200.00,'普通拔牙','治疗服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default'),(12,'智齿拔除',800.00,'阻生智齿拔除','治疗服务',1,0,NULL,NULL,NULL,NULL,'2026-06-11 04:23:38','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `treatment_catalog` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
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
-- Dumping data for table `treatment_operation_allocations`
--

LOCK TABLES `treatment_operation_allocations` WRITE;
/*!40000 ALTER TABLE `treatment_operation_allocations` DISABLE KEYS */;
/*!40000 ALTER TABLE `treatment_operation_allocations` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
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
-- Dumping data for table `treatment_operations`
--

LOCK TABLES `treatment_operations` WRITE;
/*!40000 ALTER TABLE `treatment_operations` DISABLE KEYS */;
/*!40000 ALTER TABLE `treatment_operations` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗方案表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_plans`
--

LOCK TABLES `treatment_plans` WRITE;
/*!40000 ALTER TABLE `treatment_plans` DISABLE KEYS */;
/*!40000 ALTER TABLE `treatment_plans` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_project_categories_parent_name` (`parent_id`,`name`),
  KEY `idx_treatment_project_categories_parent_id` (`parent_id`),
  KEY `idx_treatment_project_categories_status` (`status`),
  KEY `idx_treatment_project_categories_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_project_categories`
--

LOCK TABLES `treatment_project_categories` WRITE;
/*!40000 ALTER TABLE `treatment_project_categories` DISABLE KEYS */;
INSERT INTO `treatment_project_categories` VALUES (20,'基础治疗',0,1,'1',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:20:47',NULL),(21,'修复治疗',0,2,'1',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:20:47',NULL),(22,'正畸治疗',0,3,'1',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:20:47',NULL),(23,'种植治疗',0,4,'1',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:20:47',NULL),(24,'牙周治疗',0,5,'1',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:20:47',NULL);
/*!40000 ALTER TABLE `treatment_project_categories` ENABLE KEYS */;
UNLOCK TABLES;

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
  `clinic_id` varchar(64) DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_projects_code` (`project_code`),
  KEY `idx_treatment_projects_legacy_catalog_id` (`legacy_treatment_catalog_id`),
  KEY `idx_treatment_projects_category_id` (`category_id`),
  KEY `idx_treatment_projects_status` (`status`),
  KEY `idx_treatment_projects_sort_order` (`sort_order`),
  KEY `idx_treatment_projects_name` (`project_name`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_projects`
--

LOCK TABLES `treatment_projects` WRITE;
/*!40000 ALTER TABLE `treatment_projects` DISABLE KEYS */;
INSERT INTO `treatment_projects` VALUES (1,NULL,'XC001','普通洗牙',1,NULL,200.00,1,0,'1',0,'超声波洁牙',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(2,NULL,'XC002','深度洁牙',1,NULL,500.00,1,0,'1',0,'龈下刮治',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(3,NULL,'XF001','树脂补牙',2,NULL,300.00,1,0,'1',0,'光固化树脂充填',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(4,NULL,'XF002','全瓷冠',2,NULL,3500.00,1,0,'1',0,'二氧化锆全瓷冠',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(5,NULL,'ZO001','金属托槽矫正',3,NULL,15000.00,1,0,'1',0,'传统金属托槽',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(6,NULL,'ZO002','隐形矫正',3,NULL,35000.00,1,0,'1',0,'隐形矫治器',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(7,NULL,'ZZ001','单颗种植',4,NULL,8000.00,1,0,'1',0,'韩国种植体',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default'),(8,NULL,'ZZ002','全口种植',4,NULL,150000.00,1,0,'1',0,'All-on-4/6',NULL,NULL,NULL,NULL,'2026-06-11 04:20:47','2026-06-11 04:27:36','default');
/*!40000 ALTER TABLE `treatment_projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `treatment_scene`
--

DROP TABLE IF EXISTS `treatment_scene`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_scene` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景名称，如：根管治疗',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '其他' COMMENT '分类，如：牙体牙髓、口腔外科、修复科',
  `level` int DEFAULT '1' COMMENT '复杂度：1简单 2中等 3复杂',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `clinic_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗场景配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_scene`
--

LOCK TABLES `treatment_scene` WRITE;
/*!40000 ALTER TABLE `treatment_scene` DISABLE KEYS */;
/*!40000 ALTER TABLE `treatment_scene` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `treatment_scene_step`
--

DROP TABLE IF EXISTS `treatment_scene_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `treatment_scene_step` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scene_id` bigint NOT NULL COMMENT '关联场景ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '步骤名称，如：开髓引流',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `forbidden_keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '禁止关键词，逗号分隔',
  `required_keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '必须包含关键词，逗号分隔',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `clinic_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '诊所ID',
  PRIMARY KEY (`id`),
  KEY `idx_scene_id` (`scene_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗场景步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_scene_step`
--

LOCK TABLES `treatment_scene_step` WRITE;
/*!40000 ALTER TABLE `treatment_scene_step` DISABLE KEYS */;
/*!40000 ALTER TABLE `treatment_scene_step` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_clinic`
--

DROP TABLE IF EXISTS `user_clinic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_clinic` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `clinic_id` varchar(64) NOT NULL COMMENT '诊所ID',
  `role` varchar(50) NOT NULL COMMENT '角色：admin/doctor/nurse',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认诊所：0=否，1=是',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_clinic` (`user_id`,`clinic_id`),
  KEY `idx_clinic_id` (`clinic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-诊所关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_clinic`
--

LOCK TABLES `user_clinic` WRITE;
/*!40000 ALTER TABLE `user_clinic` DISABLE KEYS */;
INSERT INTO `user_clinic` VALUES (1,5,'default','admin',1,'2026-06-11 04:11:42','2026-06-11 04:11:42');
/*!40000 ALTER TABLE `user_clinic` ENABLE KEYS */;
UNLOCK TABLES;

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
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'çŠ¶æ€ï¼š1å¯ç”¨ 0åœç”¨',
  `phone` varchar(20) DEFAULT NULL COMMENT 'æ‰‹æœºå·',
  `avatar` varchar(500) DEFAULT NULL COMMENT 'å¤´åƒåœ°å€',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `last_login_at` datetime DEFAULT NULL COMMENT 'æœ€åŽç™»å½•æ—¶é—´',
  `wechat_openid` varchar(100) DEFAULT NULL COMMENT '微信 openid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_wechat_openid` (`wechat_openid`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (5,'admin','123456','管理员','admin',1,NULL,NULL,'2026-06-11 04:11:42','2026-06-11 04:11:42',NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'clinic_system'
--

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

-- Dump completed on 2026-06-11  4:28:39
