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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '0978c65a-49d2-11f1-9f98-7e8ce781ca9c:1-2522';

--
-- Current Database: `clinic_system`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `clinic_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `clinic_system`;

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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='广告投放记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advertising_spending`
--

LOCK TABLES `advertising_spending` WRITE;
/*!40000 ALTER TABLE `advertising_spending` DISABLE KEYS */;
INSERT INTO `advertising_spending` VALUES (1,'微信','种植牙春季优惠活动','2026-04-01','2026-04-30',5000.00,'种植牙','25-55岁缺牙人群','朋友圈广告投放',NULL,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(2,'抖音','隐形矫正科普推广','2026-04-15','2026-05-15',8000.00,'隐形矫正','18-35岁年轻群体','短视频达人合作',NULL,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,'百度','口腔门诊SEM推广','2026-05-01','2026-05-31',12000.00,'综合治疗','本地搜索用户','百度搜索竞价排名',NULL,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,'小红书','美白贴面种草笔记','2026-05-10','2026-06-10',6000.00,'瓷贴面','20-40岁女性','KOL种草推广',NULL,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,'美团','洁牙套餐团购推广','2026-04-20','2026-05-20',3000.00,'洁牙','本地生活用户','团购平台推广',NULL,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_agent` (`account_id`,`agent_key`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Agenté…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_agent_config`
--

LOCK TABLES `ai_agent_config` WRITE;
/*!40000 ALTER TABLE `ai_agent_config` DISABLE KEYS */;
INSERT INTO `ai_agent_config` VALUES (55,NULL,'default','智能助手','🤖','通用门诊查询与数据汇总','linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)','[\"今日预约\", \"我的待办\", \"本月收入\", \"患者查询\", \"今日患者\", \"待收费\"]',0,'2026-05-13 19:23:34','2026-05-13 19:23:34',NULL,NULL,NULL,NULL,'json',60,'json',NULL,NULL),(56,NULL,'finance','经营分析','📊','财务、收入与经营数据分析','linear-gradient(135deg, #d97706 0%, #f59e0b 100%)','[\"本月收入\", \"近7天趋势\", \"待收费\", \"加工费\", \"耗材支出\", \"高价值客户\"]',1,'2026-05-13 19:23:34','2026-05-13 19:23:34',NULL,NULL,NULL,NULL,'json',60,'json',NULL,NULL),(57,NULL,'patient','患者管理','🏥','患者档案、随访与病历查询','linear-gradient(135deg, #059669 0%, #10b981 100%)','[\"患者查询\", \"待回访\", \"流失风险\", \"转介绍\", \"待写病历\", \"今日患者\"]',2,'2026-05-13 19:23:34','2026-05-13 19:23:34',NULL,NULL,NULL,NULL,'json',60,'json',NULL,NULL),(58,NULL,'schedule','预约调度','📅','预约排班、医生日程与调度','linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)','[\"今日预约\", \"明日预约\", \"医生排班\", \"待接诊\", \"已取消\", \"预约趋势\"]',3,'2026-05-13 19:23:34','2026-05-13 19:23:34',NULL,NULL,NULL,NULL,'json',60,'json',NULL,NULL),(60,0,'123','1','🤖',NULL,'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)','[\"发送消息\"]',0,'2026-05-15 11:53:14','2026-05-15 11:53:44','阿萨','bearer','as',NULL,'sse',60,'chat',NULL,NULL),(61,0,'test','测试','🤖',NULL,'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)','[\"测试\"]',0,'2026-05-15 12:38:34','2026-05-15 12:38:34','https://webhook.site/67e96c85-0321-4a5d-b0d9-0b30b088f032','bearer','',NULL,'sse',60,'chat',NULL,NULL),(62,0,'business-analysis-test','测试经营分析','🤖',NULL,'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)','[\"分析本月经营数据\"]',0,'2026-05-15 13:53:29','2026-05-15 13:53:29','https://webhook.site/test-business','bearer',NULL,NULL,'sse',60,'chat',NULL,NULL),(70,1,'binglikuoxie','病历扩写',NULL,'',NULL,'[]',0,'2026-05-18 11:54:46','2026-05-18 15:59:04','https://wn8n.smallcherry.cn/webhook-test/test','none','','','json',60,'json',NULL,'新增病历页'),(72,1,'yuyue','预约测试',NULL,'',NULL,'[]',0,'2026-05-19 07:38:47','2026-05-19 07:38:47','','none','','','json',60,'json',NULL,'');
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
INSERT INTO `ai_few_shot_example` VALUES (3,1,'主诉：牙痛3天\n现病史：3天前开始牙痛，吃了止痛药没好','主诉：右下后牙自发痛3天\n现病史：患者3天前无明显诱因出现右下后牙自发性疼痛，呈阵发性发作，每次持续约10-15分钟，冷热刺激可加重疼痛，夜间疼痛明显，伴同侧头面部放射痛。自行口服止痛药物（具体不详）后症状无明显缓解，为求进一步诊治来我院就诊。',0,'2026-05-11 05:34:45'),(10,2,'患者因牙痛就诊','主诉：右下后牙持续性跳痛3天...',0,'2026-05-16 00:57:18');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Function Calling è°ƒç”¨æ—¥å¿—';
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
INSERT INTO `ai_function_config` VALUES (1,'home-assistant','首页 AI 助手','首页 AI 助手','🤖',1,'DeepSeek-V3',NULL,NULL,1,'2026-05-12 01:35:46','2026-05-18 11:34:47'),(2,'medical-expand','病历扩写','病历编辑页、患者详情页','📝',1,'DeepSeek-V3',NULL,NULL,2,'2026-05-12 01:35:46','2026-05-18 11:34:47'),(3,'patient-insight','患者分析','首页 AI 助手','🔍',1,'-',NULL,NULL,3,'2026-05-12 01:35:46','2026-05-18 11:34:47'),(4,'followup-generate','回访统计','首页 AI 助手','📞',1,'-',NULL,NULL,4,'2026-05-12 01:35:46','2026-05-18 11:34:47'),(5,'business-analysis','经营分析','首页 AI 助手','📊',1,'-',NULL,NULL,5,'2026-05-12 01:35:46','2026-05-18 11:34:47'),(6,'lab-order-analysis','加工订单分析','加工订单页','🤖',1,'-',NULL,NULL,6,'2026-05-12 04:20:45','2026-05-18 11:34:47'),(7,'lab-factory-analysis','加工厂分析','加工厂页','🤖',1,'-',NULL,NULL,7,'2026-05-12 04:20:45','2026-05-18 11:34:47'),(8,'ad-spending-analysis','广告投放分析','广告投放页','🤖',1,'-',NULL,NULL,8,'2026-05-12 04:20:45','2026-05-18 11:34:47'),(9,'doctor-schedule','医生排班','医生排班页','🤖',1,'-',NULL,NULL,9,'2026-05-12 04:20:45','2026-05-18 11:34:47'),(10,'appointment-assist','预约辅助','预约视图页','📅',1,'-',NULL,NULL,10,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(11,'followup-assist','回访辅助','回访管理页','📞',1,'-',NULL,NULL,11,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(12,'consultation-assist','咨询辅助','咨询记录页','💬',1,'-',NULL,NULL,12,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(13,'consultation-dashboard','咨询看板','咨询看板页','📈',1,'-',NULL,NULL,13,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(14,'treatment-assist','治疗辅助','治疗页面','🩺',1,'-',NULL,NULL,14,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(15,'treatment-record-assist','治疗记录辅助','治疗记录页','📝',1,'-',NULL,NULL,15,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(16,'financial-analysis','财务分析','财务分析页','💰',1,'-',NULL,NULL,16,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(17,'monthly-bill-analysis','月度账单分析','月度账单页','📊',1,'-',NULL,NULL,17,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(18,'lab-statistics-analysis','加工统计','加工统计页','🔧',1,'-',NULL,NULL,18,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(19,'material-category-assist','耗材分类辅助','耗材分类页','📦',1,'-',NULL,NULL,19,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(20,'material-inventory-assist','耗材库存辅助','耗材档案页','🏭',1,'-',NULL,NULL,20,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(21,'material-purchase-assist','耗材采购辅助','采购记录页','🛒',1,'-',NULL,NULL,21,'2026-05-14 04:40:21','2026-05-18 11:34:47'),(22,'material-statistics-analysis','耗材统计','耗材统计页','📉',1,'-',NULL,NULL,22,'2026-05-14 04:40:21','2026-05-18 11:34:47');
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
INSERT INTO `ai_function_mapping` VALUES (1,NULL,'medical-record-expand','病历扩写','medical-expand',1,1,1,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(2,NULL,'consultation-assist','咨询辅助','consultation-assist',1,1,2,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(3,NULL,'consultation-dashboard','咨询分析','consultation-dashboard',1,1,3,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(4,NULL,'appointment-assist','预约辅助','appointment-assist',1,1,4,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(5,NULL,'followup-assist','回访辅助','followup-assist',1,1,5,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(6,NULL,'treatment-assist','治疗辅助','treatment-assist',1,1,6,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(7,NULL,'treatment-record-assist','治疗记录辅助','treatment-record-assist',1,1,7,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(8,NULL,'financial-analysis','财务分析','financial-analysis',1,1,8,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(9,NULL,'monthly-bill-analysis','月度账单分析','monthly-bill-analysis',1,1,9,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(10,NULL,'lab-statistics-analysis','加工统计','lab-statistics-analysis',1,1,10,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(11,NULL,'material-category-assist','耗材分类辅助','material-category-assist',1,1,11,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(12,NULL,'material-inventory-assist','库存辅助','material-inventory-assist',1,1,12,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(13,NULL,'material-purchase-assist','采购辅助','material-purchase-assist',1,1,13,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(14,NULL,'material-statistics-analysis','耗材统计','material-statistics-analysis',1,1,14,'2026-05-18 01:40:07','2026-05-18 01:40:07'),(15,1,'medical-record-expand','病历扩写','binglikuoxie',1,0,1,'2026-05-18 01:56:05','2026-05-19 11:41:14'),(25,1,'consultation-assist','咨询辅助','qwer',1,0,2,'2026-05-18 02:27:13','2026-05-18 13:14:38'),(26,1,'consultation-dashboard','咨询分析','consultation-dashboard',1,0,3,'2026-05-18 02:27:13','2026-05-18 02:42:18'),(27,1,'material-statistics-analysis','耗材统计','material-statistics-analysis',0,0,14,'2026-05-18 02:27:17','2026-05-18 03:20:13'),(28,1,'material-purchase-assist','采购辅助','material-purchase-assist',1,0,13,'2026-05-18 02:27:18','2026-05-18 03:20:14'),(29,1,'material-inventory-assist','库存辅助','material-inventory-assist',1,0,12,'2026-05-18 02:27:18','2026-05-18 03:20:15'),(30,1,'material-category-assist','耗材分类辅助','material-category-assist',1,0,11,'2026-05-18 02:27:20','2026-05-18 03:20:15'),(31,1,'lab-statistics-analysis','加工统计','lab-statistics-analysis',1,0,10,'2026-05-18 02:27:20','2026-05-18 02:42:27'),(32,1,'monthly-bill-analysis','月度账单分析','monthly-bill-analysis',1,0,9,'2026-05-18 02:27:21','2026-05-18 02:42:26'),(33,1,'financial-analysis','财务分析','financial-analysis',1,0,8,'2026-05-18 02:27:23','2026-05-18 02:42:25'),(34,1,'treatment-record-assist','治疗记录辅助','treatment-record-assist',1,0,7,'2026-05-18 02:27:24','2026-05-18 02:42:24'),(37,1,'treatment-assist','治疗辅助','treatment-assist',1,0,6,'2026-05-18 02:27:27','2026-05-18 02:42:23'),(38,1,'followup-assist','回访辅助','binglikuoxie',1,0,5,'2026-05-18 02:27:28','2026-05-19 07:54:23'),(39,1,'appointment-assist','预约辅助','yuyue',1,0,4,'2026-05-18 02:27:30','2026-05-19 07:38:52'),(83,1,'abcd','测试测试','aasdsa',1,1,0,'2026-05-18 04:34:23','2026-05-18 04:34:23');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 全局配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_global_config`
--

LOCK TABLES `ai_global_config` WRITE;
/*!40000 ALTER TABLE `ai_global_config` DISABLE KEYS */;
INSERT INTO `ai_global_config` VALUES (1,'global_enabled','true','AI 功能总开关','2026-05-13 16:23:36'),(2,'debug_mode','true','调试模式开关','2026-05-12 10:30:43'),(3,'response_field','data','AI å“åº”å†…å®¹å­—æ®µå','2026-05-17 15:33:08');
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
INSERT INTO `ai_operation_log` VALUES (1,'appointment-assist',1,'{\"context\": {\"scene_id\": \"appointment-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 预约分析 · 测试患者A\", \"account_name\": \"管理员\"}, \"function\": \"appointment-assist\", \"account_id\": 1, \"functionKey\": \"appointment-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"id\": {\"label\": \"id\", \"value\": 22, \"enabled\": true}, \"phone\": {\"label\": \"phone\", \"enabled\": true}, \"status\": {\"label\": \"状态\", \"value\": \"已完成\", \"enabled\": true}, \"patient_id\": {\"label\": \"patient_id\", \"value\": 11, \"enabled\": true}, \"doctor_name\": {\"label\": \"医生\", \"value\": \"王医生\", \"enabled\": true}, \"patient_name\": {\"label\": \"患者姓名\", \"value\": \"测试患者A\", \"enabled\": true}, \"appointment_date\": {\"label\": \"预约日期\", \"value\": \"2026-05-14\", \"enabled\": true}, \"appointment_time\": {\"label\": \"预约时间\", \"value\": \"10:00:00\", \"enabled\": true}, \"duration_minutes\": {\"label\": \"duration_minutes\", \"value\": 60, \"enabled\": true}, \"appointment_purpose\": {\"label\": \"预约目的\", \"value\": \"洁牙\", \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「appointment-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「appointment-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 15:56:30'),(2,'followup-assist',1,'{\"context\": {\"scene_id\": \"followup-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 回访分析 · 李四\", \"account_name\": \"管理员\"}, \"function\": \"followup-assist\", \"account_id\": 1, \"functionKey\": \"followup-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"status\": {\"label\": \"状态\", \"value\": \"已回访\", \"enabled\": true}, \"doctor_name\": {\"label\": \"医生\", \"value\": \"孔凡瑞\", \"enabled\": true}, \"patient_name\": {\"label\": \"患者姓名\", \"value\": \"李四\", \"enabled\": true}, \"followup_date\": {\"label\": \"followup_date\", \"value\": \"2026-05-02T06:30:00.000+00:00\", \"enabled\": true}, \"followup_type\": {\"label\": \"followup_type\", \"value\": \"微信随访\", \"enabled\": true}, \"patient_phone\": {\"label\": \"patient_phone\", \"value\": \"13800138002\", \"enabled\": true}, \"followup_result\": {\"label\": \"followup_result\", \"enabled\": true}, \"followup_project\": {\"label\": \"followup_project\", \"value\": \"\", \"enabled\": true}, \"next_followup_date\": {\"label\": \"next_followup_date\", \"value\": \"2026-05-09T06:30:00.000+00:00\", \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「followup-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「followup-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 15:57:20'),(3,'followup-assist',1,'{\"context\": {\"scene_id\": \"followup-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 回访分析 · \", \"account_name\": \"管理员\"}, \"function\": \"followup-assist\", \"account_id\": 1, \"functionKey\": \"followup-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"status\": {\"label\": \"状态\", \"value\": \"待回访\", \"enabled\": true}, \"doctor_name\": {\"label\": \"医生\", \"enabled\": true}, \"patient_name\": {\"label\": \"患者姓名\", \"enabled\": true}, \"followup_date\": {\"label\": \"followup_date\", \"enabled\": true}, \"followup_type\": {\"label\": \"followup_type\", \"enabled\": true}, \"patient_phone\": {\"label\": \"patient_phone\", \"enabled\": true}, \"followup_result\": {\"label\": \"followup_result\", \"enabled\": true}, \"followup_project\": {\"label\": \"followup_project\", \"enabled\": true}, \"next_followup_date\": {\"label\": \"next_followup_date\", \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「followup-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「followup-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 15:58:53'),(4,'consultation-assist',1,'{\"context\": {\"scene_id\": \"consultation-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 咨询分析 · 张三\", \"account_name\": \"管理员\"}, \"function\": \"consultation-assist\", \"account_id\": 1, \"functionKey\": \"consultation-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"id\": {\"label\": \"id\", \"value\": 18, \"enabled\": true}, \"remark\": {\"label\": \"remark\", \"enabled\": true}, \"deal_at\": {\"label\": \"deal_at\", \"value\": null, \"enabled\": true}, \"contact_name\": {\"label\": \"contact_name\", \"value\": \"张三\", \"enabled\": true}, \"intent_level\": {\"label\": \"intent_level\", \"value\": \"低\", \"enabled\": true}, \"chief_project\": {\"label\": \"chief_project\", \"value\": \"补牙\", \"enabled\": true}, \"contact_phone\": {\"label\": \"contact_phone\", \"value\": \"13800138001\", \"enabled\": true}, \"followup_count\": {\"label\": \"followup_count\", \"enabled\": true}, \"created_by_name\": {\"label\": \"created_by_name\", \"value\": \"管理员\", \"enabled\": true}, \"handling_result\": {\"label\": \"handling_result\", \"value\": \"待跟进\", \"enabled\": true}, \"estimated_amount\": {\"label\": \"estimated_amount\", \"value\": null, \"enabled\": true}, \"ai_analysis_score\": {\"label\": \"ai_analysis_score\", \"value\": null, \"enabled\": true}, \"consultation_time\": {\"label\": \"consultation_time\", \"value\": \"2026-05-09 12:59:00\", \"enabled\": true}, \"total_deal_amount\": {\"label\": \"total_deal_amount\", \"value\": 0, \"enabled\": true}, \"next_followup_time\": {\"label\": \"next_followup_time\", \"value\": null, \"enabled\": true}, \"consultation_channel\": {\"label\": \"consultation_channel\", \"value\": \"微信\", \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「consultation-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「consultation-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 16:01:49'),(5,'followup-assist',1,'{\"context\": {\"scene_id\": \"followup-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 回访分析 · \", \"account_name\": \"管理员\"}, \"function\": \"followup-assist\", \"account_id\": 1, \"functionKey\": \"followup-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"status\": {\"label\": \"状态\", \"value\": \"待回访\", \"enabled\": true}, \"doctor_name\": {\"label\": \"医生\", \"enabled\": true}, \"patient_name\": {\"label\": \"患者姓名\", \"enabled\": true}, \"followup_date\": {\"label\": \"followup_date\", \"enabled\": true}, \"followup_type\": {\"label\": \"followup_type\", \"enabled\": true}, \"patient_phone\": {\"label\": \"patient_phone\", \"enabled\": true}, \"followup_result\": {\"label\": \"followup_result\", \"enabled\": true}, \"followup_project\": {\"label\": \"followup_project\", \"enabled\": true}, \"next_followup_date\": {\"label\": \"next_followup_date\", \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「followup-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「followup-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 16:04:35'),(6,'followup-assist',1,'{\"context\": {\"scene_id\": \"followup-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 回访分析 · \", \"account_name\": \"管理员\"}, \"function\": \"followup-assist\", \"account_id\": 1, \"functionKey\": \"followup-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"status\": {\"label\": \"状态\", \"value\": \"待回访\", \"enabled\": true}, \"doctor_name\": {\"label\": \"医生\", \"enabled\": true}, \"patient_name\": {\"label\": \"患者姓名\", \"enabled\": true}, \"followup_date\": {\"label\": \"followup_date\", \"enabled\": true}, \"followup_type\": {\"label\": \"followup_type\", \"enabled\": true}, \"patient_phone\": {\"label\": \"patient_phone\", \"enabled\": true}, \"followup_result\": {\"label\": \"followup_result\", \"enabled\": true}, \"followup_project\": {\"label\": \"followup_project\", \"enabled\": true}, \"next_followup_date\": {\"label\": \"next_followup_date\", \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「followup-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「followup-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 16:04:48'),(7,'material-inventory-assist',1,'{\"context\": {\"scene_id\": \"material-inventory-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 库存分析 · 临时冠材料\", \"account_name\": \"管理员\"}, \"function\": \"material-inventory-assist\", \"account_id\": 1, \"functionKey\": \"material-inventory-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"id\": {\"label\": \"id\", \"value\": 12, \"enabled\": true}, \"name\": {\"label\": \"name\", \"value\": \"临时冠材料\", \"enabled\": true}, \"spec\": {\"label\": \"spec\", \"value\": \"50ml\", \"enabled\": true}, \"unit\": {\"label\": \"unit\", \"value\": \"瓶\", \"enabled\": true}, \"brand\": {\"label\": \"brand\", \"value\": \"义获嘉\", \"enabled\": true}, \"remark\": {\"label\": \"remark\", \"value\": \"临时冠桥树脂\", \"enabled\": true}, \"status\": {\"label\": \"状态\", \"value\": \"active\", \"enabled\": true}, \"alert_gap\": {\"label\": \"alert_gap\", \"value\": 0, \"enabled\": true}, \"created_at\": {\"label\": \"created_at\", \"value\": \"2026-05-08 17:25:35\", \"enabled\": true}, \"updated_at\": {\"label\": \"updated_at\", \"value\": \"2026-05-08 17:25:35\", \"enabled\": true}, \"category_name\": {\"label\": \"category_name\", \"value\": \"修复材料\", \"enabled\": true}, \"current_stock\": {\"label\": \"current_stock\", \"value\": 12, \"enabled\": true}, \"min_stock_alert\": {\"label\": \"min_stock_alert\", \"value\": 3, \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「material-inventory-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「material-inventory-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 16:05:37'),(8,'material-inventory-assist',1,'{\"context\": {\"scene_id\": \"material-inventory-assist\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 库存分析 · 氧化锆瓷块\", \"account_name\": \"管理员\"}, \"function\": \"material-inventory-assist\", \"account_id\": 1, \"functionKey\": \"material-inventory-assist\", \"account_name\": \"管理员\", \"input_fields\": {\"id\": {\"label\": \"id\", \"value\": 11, \"enabled\": true}, \"name\": {\"label\": \"name\", \"value\": \"氧化锆瓷块\", \"enabled\": true}, \"spec\": {\"label\": \"spec\", \"value\": \"C2色\", \"enabled\": true}, \"unit\": {\"label\": \"unit\", \"value\": \"块\", \"enabled\": true}, \"brand\": {\"label\": \"brand\", \"value\": \"威兰德\", \"enabled\": true}, \"remark\": {\"label\": \"remark\", \"value\": \"CAD/CAM全瓷冠材料\", \"enabled\": true}, \"status\": {\"label\": \"状态\", \"value\": \"active\", \"enabled\": true}, \"alert_gap\": {\"label\": \"alert_gap\", \"value\": 0, \"enabled\": true}, \"created_at\": {\"label\": \"created_at\", \"value\": \"2026-05-08 17:25:35\", \"enabled\": true}, \"updated_at\": {\"label\": \"updated_at\", \"value\": \"2026-05-08 17:25:35\", \"enabled\": true}, \"category_name\": {\"label\": \"category_name\", \"value\": \"修复材料\", \"enabled\": true}, \"current_stock\": {\"label\": \"current_stock\", \"value\": 6, \"enabled\": true}, \"min_stock_alert\": {\"label\": \"min_stock_alert\", \"value\": 2, \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「material-inventory-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「material-inventory-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 16:05:45'),(9,'financial-analysis',1,'{\"context\": {\"scene_id\": \"financial-analysis\", \"clinic_id\": \"\", \"account_id\": \"1\", \"scene_name\": \"AI 财务分析 · 周九\", \"account_name\": \"管理员\"}, \"function\": \"financial-analysis\", \"account_id\": 1, \"functionKey\": \"financial-analysis\", \"account_name\": \"管理员\", \"input_fields\": {\"id\": {\"label\": \"id\", \"value\": 7, \"enabled\": true}, \"date\": {\"label\": \"date\", \"value\": \"2026-05-07\", \"enabled\": true}, \"name\": {\"label\": \"name\", \"value\": \"周九\", \"enabled\": true}, \"type\": {\"label\": \"type\", \"value\": \"income\", \"enabled\": true}, \"amount\": {\"label\": \"amount\", \"value\": 200, \"enabled\": true}, \"remark\": {\"label\": \"remark\", \"value\": \"复诊检查费\", \"enabled\": true}, \"patient_id\": {\"label\": \"patient_id\", \"value\": 8, \"enabled\": true}, \"treatment_id\": {\"label\": \"treatment_id\", \"value\": null, \"enabled\": true}, \"payment_channel_name\": {\"label\": \"payment_channel_name\", \"value\": null, \"enabled\": true}}, \"protocol_version\": \"1.0\"}','⚠️ AI Agent 未配置\n\n当前 Agent「financial-analysis」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「financial-analysis」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-14 16:06:11'),(10,'medical-expand',1,'{\"message\": \"test\", \"account_id\": 1}','⚠️ AI Agent 未配置\n\n当前 Agent「medical-expand」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「medical-expand」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-15 14:46:58'),(11,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"123\", \"visit_date\": \"2026-05-15 23:12:49\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"牙痛三天 \", \"patient_name\": \"张三\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"牙痛三天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','This URL has no default content configured. <a href=\"https://webhook.site/#!/edit/67e96c85-0321-4a5d-b0d9-0b30b088f032\">Change response in Webhook.site</a>.',0,0,NULL,'2026-05-15 15:15:00'),(12,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-15 23:49:45\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"阿斗\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','This URL has no default content configured. <a href=\"https://webhook.site/#!/edit/67e96c85-0321-4a5d-b0d9-0b30b088f032\">Change response in Webhook.site</a>.',0,0,NULL,'2026-05-15 15:50:17'),(13,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-15 23:49:45\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"阿斗\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}',NULL,0,0,'AI 代理调用异常：request timed out','2026-05-15 15:56:19'),(14,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:03:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\n  \"code\": \"200\",\n  \"data\": {\n    \"allergy_history\": \"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\n    \"auxiliary_examination\": \"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\n    \"chief_complaint\": \"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\n    \"diagnosis\": \"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\n    \"examination_findings\": \"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\n    \"general_condition\": \"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\n    \"notes\": \"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\n    \"past_medical_history\": \"\\u5426\\u8ba4\\u9ad8\\u8840\\u538b\\u3001\\u7cd6\\u5c3f\\u75c5\\u3001\\u5fc3\\u810f\\u75c5\\u3001\\u809d\\u708e\\u3001\\u7ed3\\u6838\\u7b49\\u7cfb\\u7edf\\u6027\\u75be\\u75c5\\u53f2\\u3002\\u5426\\u8ba4\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\",\n    \"present_illness_history\": \"3\\u5929\\u524d\\u65e0\\u660e\\u663e\\u8bf',0,0,NULL,'2026-05-15 16:04:07'),(15,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:03:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"notes\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"past_medical_history\":\"\\u5426\\u8ba4\\u9ad8\\u8840\\u538b\\u3001\\u7cd6\\u5c3f\\u75c5\\u3001\\u5fc3\\u810f\\u75c5\\u3001\\u809d\\u708e\\u3001\\u7ed3\\u6838\\u7b49\\u7cfb\\u7edf\\u6027\\u75be\\u75c5\\u53f2\\u3002\\u5426\\u8ba4\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\",\"present_illness_history\":\"3\\u5929\\u524d\\u65e0\\u660e\\u663e\\u8bf1\\u56e0\\u51fa\\u73b0\\u53f3\\u4e0b\\u540e\\u7259\\u75bc\\u75db\\uff0c\\',0,0,NULL,'2026-05-15 16:05:59'),(16,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:03:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"notes\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"past_medical_history\":\"\\u5426\\u8ba4\\u9ad8\\u8840\\u538b\\u3001\\u7cd6\\u5c3f\\u75c5\\u3001\\u5fc3\\u810f\\u75c5\\u3001\\u809d\\u708e\\u3001\\u7ed3\\u6838\\u7b49\\u7cfb\\u7edf\\u6027\\u75be\\u75c5\\u53f2\\u3002\\u5426\\u8ba4\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\",\"present_illness_history\":\"3\\u5929\\u524d\\u65e0\\u660e\\u663e\\u8bf1\\u56e0\\u51fa\\u73b0\\u53f3\\u4e0b\\u540e\\u7259\\u75bc\\u75db\\uff0c\\',0,0,NULL,'2026-05-15 16:06:13'),(17,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:03:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"notes\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"past_medical_history\":\"\\u5426\\u8ba4\\u9ad8\\u8840\\u538b\\u3001\\u7cd6\\u5c3f\\u75c5\\u3001\\u5fc3\\u810f\\u75c5\\u3001\\u809d\\u708e\\u3001\\u7ed3\\u6838\\u7b49\\u7cfb\\u7edf\\u6027\\u75be\\u75c5\\u53f2\\u3002\\u5426\\u8ba4\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\",\"present_illness_history\":\"3\\u5929\\u524d\\u65e0\\u660e\\u663e\\u8bf1\\u56e0\\u51fa\\u73b0\\u53f3\\u4e0b\\u540e\\u7259\\u75bc\\u75db\\uff0c\\',0,0,NULL,'2026-05-15 16:10:41'),(18,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:18:30\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 16:25:18'),(19,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：外部端点返回非 SSE 格式（Content-Type: application/json），响应：{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\','2026-05-15 16:33:46'),(20,'medical-expand',1,'{\"message\": \"张三\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：外部端点返回非 SSE 格式（Content-Type: application/json），响应：{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\','2026-05-15 16:35:15'),(21,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：外部端点返回非 SSE 格式（Content-Type: application/json），响应：{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\','2026-05-15 16:35:58'),(22,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：外部端点返回非 SSE 格式（Content-Type: application/json），响应：{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\','2026-05-15 16:36:13'),(23,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:47:47\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"1\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 16:47:51'),(24,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:49:10\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"12\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 16:49:13'),(25,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-16 00:50:03\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"12121\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 16:50:08'),(26,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b95d346d-2c40-46f6-a6aa-652374f4f13d\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:02:07'),(27,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b95d346d-2c40-46f6-a6aa-652374f4f13d\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:03:10'),(28,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b95d346d-2c40-46f6-a6aa-652374f4f13d\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:07:58'),(29,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:08:20'),(30,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b95d346d-2c40-46f6-a6aa-652374f4f13d\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:12:18'),(31,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:13:37'),(32,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:17:30'),(33,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\u907f\\u514d\\u60a3\\u4fa7\\u5480\\u56bc\\u786c\\u7269\\uff1b2. \\u6ce8\\u610f\\u53e3\\u8154\\u536b\\u751f\\uff0c\\u996d\\u540e\\u6f31\\u53e3\\uff1b3. \\u5982\\u6709\\u80bf\\u75db\\u52a0\\u5267\\u8bf7\\u53ca\\u65f6\\u5c31\\u8bca\\uff1b4. \\u6309\\u65f6\\u590d\\u8bca\\u5b8c\\u6210\\u6839\\u7ba1\\u6cbb\\u7597\\u3002\",\"notes\":\"\\u5df2\\u54',0,0,NULL,'2026-05-15 17:19:54'),(34,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-15 17:21:00'),(35,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"c4b98b4c-8c8b-43b0-9468-5843298b4c7a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-15 17:21:00'),(36,'medical-expand',1,'{\"fields\": {\"diagnosis\": \"右下6深龋\", \"patient_id\": \"1\", \"visit_date\": \"2026-05-16 09:30:00\", \"doctor_name\": \"李医生\", \"record_type\": \"初诊\", \"draft_record\": \"牙痛 3天前开始右下后牙疼痛\", \"patient_name\": \"张三\", \"record_status\": \"draft\", \"treatment_plan\": \"根管治疗后冠修复\", \"allergy_history\": \"无\", \"chief_complaint\": \"牙痛\", \"operation_items\": [], \"tooth_positions\": \"36\", \"doctor_account_id\": \"1\", \"general_condition\": \"良好\", \"infectious_history\": \"无\", \"examination_findings\": \"右下6远中龋坏，探诊敏感\", \"past_medical_history\": \"无特殊\", \"auxiliary_examination\": \"X线示龋坏近髓\", \"present_illness_history\": \"3天前开始右下后牙疼痛\"}, \"account_id\": \"1\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-16 00:56:38'),(37,'medical-expand',1,'{\"fields\": {\"diagnosis\": \"右下6深龋\", \"patient_id\": \"1\", \"visit_date\": \"2026-05-16 09:30:00\", \"doctor_name\": \"李医生\", \"record_type\": \"初诊\", \"draft_record\": \"牙痛 3天前开始右下后牙疼痛\", \"patient_name\": \"张三\", \"record_status\": \"draft\", \"treatment_plan\": \"根管治疗后冠修复\", \"allergy_history\": \"无\", \"chief_complaint\": \"牙痛\", \"operation_items\": [], \"tooth_positions\": \"36\", \"doctor_account_id\": \"1\", \"general_condition\": \"良好\", \"infectious_history\": \"无\", \"examination_findings\": \"右下6远中龋坏，探诊敏感\", \"past_medical_history\": \"无特殊\", \"auxiliary_examination\": \"X线示龋坏近髓\", \"present_illness_history\": \"3天前开始右下后牙疼痛\"}, \"account_id\": \"1\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-16 01:08:12'),(38,'medical-expand',1,'{\"fields\": {}, \"account_id\": \"1\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-16 06:47:17'),(39,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-17 02:44:51'),(40,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 03:53:20'),(41,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 03:53:32'),(42,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 03:53:45'),(43,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 03:55:34'),(44,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 03:55:53'),(45,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 03:55:53'),(46,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:02:08'),(47,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:02:17'),(48,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:02:17'),(49,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:02:45'),(50,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:04:40'),(51,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:05:01'),(52,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:05:01'),(53,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:05:16'),(54,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:10:36'),(55,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:11:10'),(56,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:11:21'),(57,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:11:21'),(58,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:14:46'),(59,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:14:57'),(60,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 04:14:57'),(61,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用异常：request timed out','2026-05-17 04:16:37'),(62,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用异常：request timed out','2026-05-17 04:17:08'),(63,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:18:21'),(64,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:21:31'),(65,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:21:31'),(66,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:22:16'),(67,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:22:16'),(68,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:23:32'),(69,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:27:06'),(70,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:35:12'),(71,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:35:26'),(72,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:35:31'),(73,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{}',0,0,NULL,'2026-05-17 04:36:00'),(74,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"account_name\": \"测试\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:40:19'),(75,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 04:44:03'),(76,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:49:06'),(77,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:49:06'),(78,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:49:18'),(79,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:49:18'),(80,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:49:32'),(81,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:49:32'),(82,'agent_1778711139163',1,'{\"message\": \"你哈\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:50:41'),(83,'agent_1778711139163',1,'{\"message\": \"你哈\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:50:41'),(84,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:52:28'),(85,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 04:52:28'),(86,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 04:53:13'),(87,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 04:53:20'),(88,'agent_1778711139163',1,'{\"message\": \"你好啊\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 04:53:22'),(89,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 04:56:58'),(90,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 04:57:16'),(91,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"14\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11598\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"message\":\"\"},\"webhookUrl\":\"https://wn8n.smallcherry.cn/webhook/test\",\"executionMode\":\"production\"}',0,0,NULL,'2026-05-17 04:57:25'),(92,'agent_1778711139163',1,'{\"message\": \"ni h\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"14\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11593\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"message\":\"\"},\"webhookUrl\":\"https://wn8n.smallcherry.cn/webhook/test\",\"executionMode\":\"production\"}',0,0,NULL,'2026-05-17 05:19:21'),(93,'agent_1778711139163',1,'{\"message\": \"查患者查患者\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"14\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11593\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"message\":\"\"},\"webhookUrl\":\"https://wn8n.smallcherry.cn/webhook/test\",\"executionMode\":\"production\"}',0,0,NULL,'2026-05-17 05:19:49'),(94,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"14\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11593\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"message\":\"\"},\"webhookUrl\":\"https://wn8n.smallcherry.cn/webhook/test\",\"executionMode\":\"production\"}',0,0,NULL,'2026-05-17 05:20:09'),(95,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:20:30'),(96,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:22:26'),(97,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:22:36'),(98,'agent_1778711139163',1,'{\"message\": \"测试\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"test\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:23:20'),(99,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"beed6c8d-3213-4869-96f5-5c55fceea1a5\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:25:21'),(100,'agent_1778711139163',1,'{\"message\": \"测试123\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"test-session\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:26:29'),(101,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:33:08'),(102,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"21\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11604\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"message\":\"你好\"},\"webhookUrl\":\"https://wn8n.smallcherry.cn/webhook/test\",\"executionMode\":\"production\"}',0,0,NULL,'2026-05-17 05:33:40'),(103,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"}}',0,0,NULL,'2026-05-17 05:35:27'),(104,'agent_1778711139163',1,'{\"message\": \"抓包测试消息\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"test\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用异常：request timed out','2026-05-17 05:38:08'),(105,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":{\"message\":\"你好\"}}',0,0,NULL,'2026-05-17 05:41:51'),(106,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":{\"message\":\"你好\"}}',0,0,NULL,'2026-05-17 05:42:52'),(107,'agent_1778711139163',1,'{\"message\": \"123\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":{\"message\":\"123\"}}',0,0,NULL,'2026-05-17 05:42:56'),(108,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":{\"message\":\"你好\"}}',0,0,NULL,'2026-05-17 05:47:28'),(109,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 05:48:11'),(110,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 05:48:11'),(111,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','[{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"21\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11442\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"message\":\"你好\"},\"webhookUrl\":\"https://wn8n.smallcherry.cn/webhook/test\",\"executionMode\":\"production\"}]',0,0,NULL,'2026-05-17 05:49:27'),(112,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":{\"message\":\"=$json.body.message\"}}]',0,0,NULL,'2026-05-17 05:51:00'),(113,'agent_1778711139163',1,'{\"message\": \"123456\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":\"{\\\"message\\\":\\\"123456\\\"}\"}]',0,0,NULL,'2026-05-17 06:01:36'),(114,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":\"{\\\"message\\\":\\\"1\\\"}\"}]',0,0,NULL,'2026-05-17 08:06:18'),(115,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"你好！n8n通路已打通，这是一条测试回复。\"},\"body\":\"{\\\"message\\\":\\\"1\\\"}\"}]',0,0,NULL,'2026-05-17 08:06:45'),(116,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 08:07:16'),(117,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 08:07:16'),(118,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 08:07:28'),(119,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 08:07:28'),(120,'agent_1778711139163',1,'{\"message\": \"12\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 08:08:50'),(121,'agent_1778711139163',1,'{\"message\": \"12\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 08:08:50'),(122,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 08:09:21'),(123,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"Unused Respond to Webhook node found in the workflow\"}','2026-05-17 08:09:21'),(124,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:10:15'),(125,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:10:50'),(126,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:11:23'),(127,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:22:26'),(128,'agent_1778711139163',1,'{\"message\": \"测试\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:23:16'),(129,'agent_1778711139163',1,'{\"message\": \"测试\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:25:20'),(130,'agent_1778711139163',1,'{\"message\": \"测试\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 08:26:18'),(131,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 09:17:08'),(132,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"36ca79c5-e255-4d1d-8d49-4eb901ee5a6a\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"POST test\\\" is not registered.\",\"hint\":\"The workflow must be active for a production URL to run successfully. You can activate the workflow using the toggle in the top-right of the editor. Note that unlike test URL calls, production URL calls aren\'t shown on the canvas (only in the executions list)\"}','2026-05-17 09:17:08'),(133,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"query\":{\"abc\":\"这是响应内容:\"}}]',0,0,NULL,'2026-05-17 12:54:32'),(134,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 <!DOCTYPE html>\n<html>\n<head>\n<title>Not Found</title>\n<style>\n    body {\n        width: 35em;\n        margin: 0 auto;\n        font-family: Tahoma, Verdana, Arial, sans-serif;\n    }\n</style>\n</head>\n<body>\n<h1>The page you requested was not found.</h1>\n<p>Sorry, the page you are looking for is currently unavailable.<br/>\nPlease try again later.</p>\n<p>The server is powered by <a href=\"https://github.com/fatedier/frp\">frp</a>.</p>\n<p><em>Faithfully yours, frp.</em></p>\n</body>\n','2026-05-17 12:57:11'),(135,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 <!DOCTYPE html>\n<html>\n<head>\n<title>Not Found</title>\n<style>\n    body {\n        width: 35em;\n        margin: 0 auto;\n        font-family: Tahoma, Verdana, Arial, sans-serif;\n    }\n</style>\n</head>\n<body>\n<h1>The page you requested was not found.</h1>\n<p>Sorry, the page you are looking for is currently unavailable.<br/>\nPlease try again later.</p>\n<p>The server is powered by <a href=\"https://github.com/fatedier/frp\">frp</a>.</p>\n<p><em>Faithfully yours, frp.</em></p>\n</body>\n','2026-05-17 12:57:11'),(136,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:\"}]',0,0,NULL,'2026-05-17 12:58:22'),(137,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:\"}]',0,0,NULL,'2026-05-17 13:00:13'),(138,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:\"}]',0,0,NULL,'2026-05-17 13:00:26'),(139,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:\"}]',0,0,NULL,'2026-05-17 13:00:59'),(140,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:\"}]',0,0,NULL,'2026-05-17 13:01:18'),(141,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:03:34'),(142,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:03:34'),(143,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用异常：request timed out','2026-05-17 13:34:31'),(144,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No Respond to Webhook node found in the workflow\"}','2026-05-17 13:35:18'),(145,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No Respond to Webhook node found in the workflow\"}','2026-05-17 13:35:18'),(146,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No Respond to Webhook node found in the workflow\"}','2026-05-17 13:35:45'),(147,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No Respond to Webhook node found in the workflow\"}','2026-05-17 13:35:45'),(148,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','{\"message\":\"Workflow was started\"}',0,0,NULL,'2026-05-17 13:37:09'),(149,'agent_1778711139163',1,'{\"message\": \"你好 啊\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:你好 啊\"}]',0,0,NULL,'2026-05-17 13:38:05'),(150,'agent_1778711139163',1,'{\"message\": \"你好啊\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"message\":\"这是响应内容:你好啊\",\"body\":{\"message\":\"你好啊\"}}]',0,0,NULL,'2026-05-17 13:39:05'),(151,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:42:46'),(152,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:42:46'),(153,'agent_1778711139163',1,'{\"message\": \"你是猪\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"code\":\"500\",\"msg\":\"\",\"data\":\"这是自定义响应  你是猪\"}]',0,0,NULL,'2026-05-17 13:48:33'),(154,'agent_1778711139163',1,'{\"message\": \"猪\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"code\":\"500\",\"msg\":\"\",\"data\":\"这是自定义响应  猪\"}]',0,0,NULL,'2026-05-17 13:49:09'),(155,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:49:38'),(156,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:49:38'),(157,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:49:44'),(158,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:49:44'),(159,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"这是响应内容:测试\"}}]',0,0,NULL,'2026-05-17 13:49:57'),(160,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:50:17'),(161,'agent_1778711139163',1,'{\"message\": \"你是\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:50:17'),(162,'agent_1778711139163',1,'{\"message\": \"你\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"这是响应内容:测试\"}}]',0,0,NULL,'2026-05-17 13:51:53'),(163,'agent_1778711139163',1,'{\"message\": \"你\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:52:15'),(164,'agent_1778711139163',1,'{\"message\": \"你\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"99526a7c-cdf0-4358-825b-71843d192eb9\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 13:52:15'),(165,'agent_1778711139163',1,'{\"message\": \"测试消息\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 14:03:53'),(166,'agent_1778711139163',1,'{\"message\": \"测试消息\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 14:03:53'),(167,'agent_1778711139163',1,'{\"message\": \"测试消息2\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 14:09:00'),(168,'agent_1778711139163',1,'{\"message\": \"测试消息2\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 14:09:00'),(169,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"这是响应内容:测试\"}}]',0,0,NULL,'2026-05-17 14:12:04'),(170,'agent_1778711139163',1,'{\"message\": \"抓包测试\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"这是响应内容:测试\"}}]',0,0,NULL,'2026-05-17 14:14:01'),(171,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','{\"code\":\"200\",\"msg\":\"success\",\"data\":{\"content\":\"这是响应内容:测试\"}}',0,0,NULL,'2026-05-17 14:17:09'),(172,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 14:27:48'),(173,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 14:27:48'),(174,'medical-expand',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-17 23:01:46\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"尹\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": 2, \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','{\"code\":\"200\",\"data\":{\"allergy_history\":\"\\u5426\\u8ba4\\u836f\\u7269\\u8fc7\\u654f\\u53f2\\u53ca\\u98df\\u7269\\u8fc7\\u654f\\u53f2\\u3002\",\"apple\":\"\\u5df2\\u5411\\u60a3\\u8005\\u8be6\\u7ec6\\u4ea4\\u4ee3\\u75c5\\u60c5\\u3001\\u6cbb\\u7597\\u65b9\\u6848\\u3001\\u9884\\u671f\\u6548\\u679c\\u53ca\\u8d39\\u7528\\uff0c\\u60a3\\u8005\\u8868\\u793a\\u7406\\u89e3\\u5e76\\u77e5\\u60c5\\u540c\\u610f\\u3002\\u4e0b\\u6b21\\u9884\\u7ea6\\u6839\\u7ba1\\u9884\\u5907\\u6cbb\\u7597\\u3002\",\"auxiliary_examination\":\"\\u53e3\\u8154\\u5168\\u666f\\u7247\\u793a\\uff1a46\\u7259\\u9f8b\\u574f\\u8fbe\\u7259\\u672c\\u8d28\\u6df1\\u5c42\\uff0c\\u8fd1\\u9ad3\\u89d2\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u4f4e\\u5bc6\\u5ea6\\u5f71\\u50cf\\uff0c\\u7259\\u5468\\u819c\\u95f4\\u9699\\u6b63\\u5e38\\u3002\",\"chief_complaint\":\"\\u60a3\\u8005\\u56e0\\u53f3\\u4e0b\\u540e\\u7259\\u6301\\u7eed\\u6027\\u75bc\\u75db3\\u5929\\u5c31\\u8bca\\uff0c\\u75bc\\u75db\\u4e3a\\u949d\\u75db\\uff0c\\u51b7\\u70ed\\u523a\\u6fc0\\u52a0\\u91cd\\uff0c\\u591c\\u95f4\\u5e73\\u5367\\u65f6\\u75bc\\u75db\\u660e\\u663e\\u52a0\\u5267\\u3002\",\"diagnosis\":\"1. 46\\u7259\\u6162\\u6027\\u7259\\u9ad3\\u708e\\u6025\\u6027\\u53d1\\u4f5c\\uff1b2. 46\\u7259\\u6df1\\u9f8b\",\"examination_findings\":\"46\\u7259\\u988c\\u9762\\u53ef\\u89c1\\u6df1\\u9f8b\\u6d1e\\uff0c\\u63a2\\u8bca\\u654f\\u611f\\uff0c\\u9f8b\\u574f\\u7ec4\\u7ec7\\u8f6f\\uff0c\\u53bb\\u8150\\u540e\\u8fd1\\u9ad3\\u3002\\u53e9\\u8bca(+)\\uff0c\\u677e\\u52a8\\u5ea6(-)\\u3002\\u7259\\u9f88\\u672a\\u89c1\\u660e\\u663e\\u7ea2\\u80bf\\uff0c\\u65e0\\u7aa6\\u9053\\u3002\\u51b7\\u8bca\\u654f\\u611f\\uff0c\\u70ed\\u8bca\\u75bc\\u75db\\u52a0\\u5267\\u3002\",\"general_condition\":\"\\u60a3\\u8005\\u795e\\u5fd7\\u6e05\\u695a\\uff0c\\u7cbe\\u795e\\u5c1a\\u53ef\\uff0c\\u9762\\u8272\\u6b63\\u5e38\\uff0c\\u53d1\\u80b2\\u6b63\\u5e38\\uff0c\\u8425\\u517b\\u4e2d\\u7b49\\u3002\",\"image_summary\":\"\\u5168\\u666f\\u7247\\u663e\\u793a46\\u7259\\u6df1\\u9f8b\\u8fd1\\u9ad3\\uff0c\\u6839\\u5c16\\u5468\\u672a\\u89c1\\u660e\\u663e\\u5f02\\u5e38\\u3002\",\"infectious_history\":\"\\u5426\\u8ba4\\u4e59\\u809d\\u3001\\u4e19\\u809d\\u3001\\u6885\\u6bd2\\u3001\\u827e\\u6ecb\\u75c5\\u7b49\\u4f20\\u67d3\\u75c5\\u53f2\\u3002\\u65e0\\u7ed3\\u6838\\u63a5\\u89e6\\u53f2\\u3002\",\"medical_advice\":\"1. \\',0,0,NULL,'2026-05-17 15:02:16'),(175,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:05:10'),(176,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:05:10'),(177,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','',0,0,NULL,'2026-05-17 15:34:01'),(178,'agent_1778711139163',1,'{\"message\": \"您好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:34:31'),(179,'agent_1778711139163',1,'{\"message\": \"您好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:34:31'),(180,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','',0,0,NULL,'2026-05-17 15:34:51'),(181,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"你好aaaa\"}]',0,0,NULL,'2026-05-17 15:35:18'),(182,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','{\"data\":\"你好aaaa\"}',0,0,NULL,'2026-05-17 15:37:00'),(183,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:37:12'),(184,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:37:12'),(185,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','{\"data\":\"你好aaaa\"}',0,0,NULL,'2026-05-17 15:38:47'),(186,'agent_1778711139163',1,'{\"message\": \"你是谁\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','{\"data\":\"你是谁aaaa\"}',0,0,NULL,'2026-05-17 15:38:50'),(187,'agent_1778711139163',1,'{\"message\": \"啦啦啦\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','{\"data\":\"啦啦啦aaaa\"}',0,0,NULL,'2026-05-17 15:38:55'),(188,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"你好aaaa\"}]',0,0,NULL,'2026-05-17 15:41:16'),(189,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:42:16'),(190,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:42:16'),(191,'agent_1778711139163',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','',0,0,NULL,'2026-05-17 15:42:23'),(192,'agent_1778711139163',1,'{\"message\": \"123\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"There was a problem executing the workflow\"}','2026-05-17 15:44:10'),(193,'agent_1778711139163',1,'{\"message\": \"123\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"There was a problem executing the workflow\"}','2026-05-17 15:44:10'),(194,'agent_1778711139163',1,'{\"message\": \"11212\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"There was a problem executing the workflow\"}','2026-05-17 15:44:19'),(195,'agent_1778711139163',1,'{\"message\": \"11212\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"There was a problem executing the workflow\"}','2026-05-17 15:44:19'),(196,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"1\"}]',0,0,NULL,'2026-05-17 15:44:57'),(197,'agent_1778711139163',1,'{\"message\": \"·1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"第1条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第2条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第3条消息\",\"data1\":\"agent_1778711139163\"}]',0,0,NULL,'2026-05-17 15:52:57'),(198,'agent_1778711139163',1,'{\"message\": \"21\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:53:03'),(199,'agent_1778711139163',1,'{\"message\": \"21\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-17 15:53:03'),(200,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"第1条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第2条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第3条消息\",\"data1\":\"agent_1778711139163\"}]',0,0,NULL,'2026-05-17 15:53:45'),(201,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"第1条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第2条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第3条消息\",\"data1\":\"agent_1778711139163\"}]',0,0,NULL,'2026-05-17 15:53:48'),(202,'agent_1778711139163',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}','[{\"data\":\"第1条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第2条消息\",\"data1\":\"agent_1778711139163\"},{\"data\":\"第3条消息\",\"data1\":\"agent_1778711139163\"}]',0,0,NULL,'2026-05-17 15:53:52'),(203,'abcd',1,'{\"message\": \"1\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"aafbff5f-3ab4-429d-861f-6ad8f1c32f45\", \"account_name\": \"管理员\"}','',0,0,NULL,'2026-05-18 05:04:30'),(204,'consultation-assist',NULL,'{\"message\": \"【咨询信息】\\n主诉项目：-\\n意向强度：-\\n处理结果：待跟进\\n客户顾虑：-\\n备注：-\\n预计金额：-\\n咨询渠道：微信\\n联系方式：- \", \"account_id\": \"\"}','⚠️ AI Agent 未配置\n\n当前 Agent「consultation-assist」尚未配置有效的外部工作流端点，或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n请前往【系统设置 > AI 智能中心】完成以下配置：\n1. 添加或编辑「consultation-assist」Agent\n2. 填写真实的工作流端点 URL\n3. 配置认证信息（如 Bearer Token、API Key 等）\n\n配置完成后重新发起对话即可正常使用 AI 功能。',0,0,NULL,'2026-05-18 12:57:31'),(205,'qwer',1,'{\"message\": \"【咨询信息】\\n主诉项目：-\\n意向强度：-\\n处理结果：待跟进\\n客户顾虑：-\\n备注：-\\n预计金额：-\\n咨询渠道：微信\\n联系方式：- \", \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 13:05:22'),(206,'qwer',1,'{\"message\": \"【咨询信息】\\n主诉项目：-\\n意向强度：-\\n处理结果：待跟进\\n客户顾虑：-\\n备注：-\\n预计金额：-\\n咨询渠道：微信\\n联系方式：- \", \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 13:05:22'),(207,'qwer',1,'{\"message\": \"【咨询信息】\\n主诉项目：-\\n意向强度：-\\n处理结果：待跟进\\n客户顾虑：-\\n备注：-\\n预计金额：-\\n咨询渠道：微信\\n联系方式：- \", \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 15:58:15'),(208,'qwer',1,'{\"message\": \"【咨询信息】\\n主诉项目：-\\n意向强度：-\\n处理结果：待跟进\\n客户顾虑：-\\n备注：-\\n预计金额：-\\n咨询渠道：微信\\n联系方式：- \", \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 15:58:15'),(209,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-18 23:59:19\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"11\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','',0,0,NULL,'2026-05-18 15:59:30'),(210,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-18 23:59:19\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"11\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 16:02:15'),(211,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-18 23:59:19\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"11\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 16:02:15'),(212,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-18 23:59:19\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"11\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"11\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-18 23:59:19\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:07:25'),(213,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-18 23:59:19\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"测试123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-18 23:59:19\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:08:07'),(214,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 16:08:30'),(215,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-18 16:08:30'),(216,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"测试123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-18 23:59:19\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:08:47'),(217,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"测试123 \", \"patient_name\": \"测试\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"测试123\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"测试123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-18 23:59:19\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:10:48'),(218,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"测试12345 \", \"patient_name\": \"测试\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"测试12345\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"测试12113\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-18 23:59:19\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:11:35'),(219,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"测试12345 12345\", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"测试12345\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"12345\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"尹涛\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:08:25\",\"record_type\":\"初诊\",\"chief_complaint\":\"测试12345\",\"present_illness_history\":\"12345\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\"测试12345 12345\",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:13:46'),(220,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"测试12345 12345\", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"测试12345\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"12345\"}, \"account_id\": 1}','[{\"patient_id\":\"13\",\"patient_name\":\"尹涛\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:08:25\",\"record_type\":\"初诊\",\"chief_complaint\":\"测试12345\",\"present_illness_history\":\"12345\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\"测试12345 12345\",\"data1\":\"binglikuoxie\"}]',0,0,NULL,'2026-05-18 16:14:33'),(221,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:08:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"测试12345 12345\", \"patient_name\": \"尹涛\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"测试12345\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"12345\"}, \"account_id\": 1}','',0,0,NULL,'2026-05-18 16:18:11'),(222,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"patient_id\":\"14\"}]',0,0,NULL,'2026-05-18 16:21:31'),(223,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','',0,0,NULL,'2026-05-18 16:26:08'),(224,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','',0,0,NULL,'2026-05-18 16:30:38'),(225,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"15测试\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','',0,0,NULL,'2026-05-18 16:31:00'),(226,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"15测试\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"patient_id\":\"15测试\",\"patient_name\":\"123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:20:54\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \"}}]',0,0,NULL,'2026-05-18 16:31:22'),(227,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"15测试\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"patient_id\":\"15测试\",\"patient_name\":\"123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:20:54\",\"record_type\":\"初诊\",\"chief_complaint\":\"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗\",\"present_illness_history\":\"【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重，伴放射性疼痛\",\"past_medical_history\":\"【AI测试-既往史】否认高血压、糖尿病等系统性疾病，否认药物过敏史\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"【AI测试-检查】16牙远中邻面龋坏，探诊敏感，冷热测试敏感，叩诊(-)\",\"auxiliary_examination\":\"【AI测试-辅助检查】X线示16牙远中邻面透射影，未及髓腔\",\"diagnosis\":\"【AI测试-诊断】16深龋\",\"treatment_plan\":\"【AI测试-治疗方案】1. 16牙根管治疗 2. 冠修复\",\"treatment\":\"【AI测试-治疗】局麻下开髓，拔髓，根管预备，封药\",\"tooth_positions\":\"\",\"medical_advice\":\"【AI测试-医嘱】避免患侧咀嚼，一周后复诊\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"【AI测试-备注】患者配合度良好，治疗顺利完成\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \"}}]',0,0,NULL,'2026-05-18 16:38:07'),(228,'binglikuoxie',1,'{\"fields\": {\"notes\": \"【AI测试-备注】患者配合度良好，治疗顺利完成\", \"diagnosis\": \"【AI测试-诊断】16深龋\", \"treatment\": \"【AI测试-治疗】局麻下开髓，拔髓，根管预备，封药\", \"nurse_name\": \"\", \"patient_id\": \"15测试\", \"visit_date\": \"2026-05-19 00:20:54\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗 【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重，伴放射性疼痛\", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"【AI测试-医嘱】避免患侧咀嚼，一周后复诊\", \"treatment_plan\": \"【AI测试-治疗方案】1. 16牙根管治疗 2. 冠修复\", \"allergy_history\": \"\", \"chief_complaint\": \"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"【AI测试-检查】16牙远中邻面龋坏，探诊敏感，冷热测试敏感，叩诊(-)\", \"past_medical_history\": \"【AI测试-既往史】否认高血压、糖尿病等系统性疾病，否认药物过敏史\", \"auxiliary_examination\": \"【AI测试-辅助检查】X线示16牙远中邻面透射影，未及髓腔\", \"present_illness_history\": \"【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重，伴放射性疼痛\"}, \"account_id\": 1}','[{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"patient_id\":\"15测试\",\"patient_name\":\"123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:20:54\",\"record_type\":\"初诊\",\"chief_complaint\":\"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗\",\"present_illness_history\":\"【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重，伴放射性疼痛\",\"past_medical_history\":\"【AI测试-既往史】否认高血压、糖尿病等系统性疾病，否认药物过敏史\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"【AI测试-检查】16牙远中邻面龋坏，探诊敏感，冷热测试敏感，叩诊(-)\",\"auxiliary_examination\":\"【AI测试-辅助检查】X线示16牙远中邻面透射影，未及髓腔\",\"diagnosis\":\"【AI测试-诊断】16深龋\",\"treatment_plan\":\"【AI测试-治疗方案】1. 16牙根管治疗 2. 冠修复\",\"treatment\":\"【AI测试-治疗】局麻下开髓，拔髓，根管预备，封药\",\"tooth_positions\":\"\",\"medical_advice\":\"【AI测试-医嘱】避免患侧咀嚼，一周后复诊\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"【AI测试-备注】患者配合度良好，治疗顺利完成\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗 【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重，伴放射性疼痛\"}}]',0,0,NULL,'2026-05-18 16:40:40'),(229,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:41:07\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"123\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"patient_id\":\"13\",\"patient_name\":\"123\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:41:07\",\"record_type\":\"初诊\",\"chief_complaint\":\"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗\",\"present_illness_history\":\"【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重，伴放射性疼痛\",\"past_medical_history\":\"【AI测试-既往史】否认高血压、糖尿病等系统性疾病，否认药物过敏史\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"【AI测试-检查】16牙远中邻面龋坏，探诊敏感，冷热测试敏感，叩诊(-)\",\"auxiliary_examination\":\"【AI测试-辅助检查】X线示16牙远中邻面透射影，未及髓腔\",\"diagnosis\":\"【AI测试-诊断】16深龋\",\"treatment_plan\":\"【AI测试-治疗方案】1. 16牙根管治疗 2. 冠修复\",\"treatment\":\"【AI测试-治疗】局麻下开髓，拔髓，根管预备，封药\",\"tooth_positions\":\"\",\"medical_advice\":\"【AI测试-医嘱】避免患侧咀嚼，一周后复诊\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"【AI测试-备注】患者配合度良好，治疗顺利完成\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \"}}]',0,0,NULL,'2026-05-18 16:41:27'),(230,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:44:45\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"11\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"patient_id\":\"13\",\"patient_name\":\"11\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 00:44:45\",\"record_type\":\"初诊\",\"chief_complaint\":\"【AI测试-主诉】患者因牙齿疼痛就诊，要求检查治疗\",\"present_illness_history\":\"【AI测试-现病史】患者3天前开始出现右上后牙冷热刺激痛，夜间加重\",\"past_medical_history\":\"【AI测试-既往史】否认高血压、糖尿病等系统性疾病\",\"infectious_history\":\"【AI测试-流行病史】无传染病接触史，无疫区旅居史\",\"allergy_history\":\"【AI测试-过敏史】否认药物及食物过敏史\",\"general_condition\":\"【AI测试-一般情况】神志清楚，精神可，营养中等\",\"examination_findings\":\"【AI测试-检查】16牙远中邻面龋坏，探诊敏感，叩诊(-)\",\"auxiliary_examination\":\"【AI测试-辅助检查】X线示16牙远中邻面透射影，未及髓腔\",\"diagnosis\":\"【AI测试-诊断】16深龋\",\"treatment_plan\":\"【AI测试-治疗方案】1. 16牙根管治疗 2. 冠修复\",\"treatment\":\"【AI测试-治疗】局麻下开髓，拔髓，根管预备，封药\",\"tooth_positions\":\"\",\"medical_advice\":\"【AI测试-医嘱】避免患侧咀嚼，一周后复诊\",\"prescription\":\"【AI测试-处方】布洛芬缓释胶囊 0.3g×2盒，必要时口服\",\"record_tags\":\"\",\"image_summary\":\"【AI测试-影像说明】全景片显示16牙远中龋损，根尖周未见明显异常\",\"notes\":\"【AI测试-备注】患者配合度良好，治疗顺利完成\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \"}}]',0,0,NULL,'2026-05-18 16:44:58'),(231,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 00:46:56\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"11\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"account_id\": 1}','[{\"data\":{\"patient_id\":\"13\",\"patient_name\":\"11\",\"doctor_name\":\"管理员\",\"past_medical_history\":\"【AI测试-既往史】否认高血压、糖尿病等系统性疾病\",\"general_condition\":\"【AI测试-一般情况】神志清楚，精神可，营养中等\"}}]',0,0,NULL,'2026-05-18 16:47:09'),(232,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 17:37:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"scene_id\": 1, \"account_id\": 1, \"scene_name\": \"根管治疗\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:40:26'),(233,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 17:37:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"scene_id\": 1, \"account_id\": 1, \"scene_name\": \"根管治疗\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:40:26'),(234,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 17:37:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"scene_id\": 3, \"account_id\": 1, \"scene_name\": \"树脂充填\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:40:49'),(235,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 17:37:43\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"scene_id\": 3, \"account_id\": 1, \"scene_name\": \"树脂充填\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:40:49'),(236,'binglikuoxie',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:42:52'),(237,'binglikuoxie',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:42:52'),(238,'binglikuoxie',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:43:39'),(239,'binglikuoxie',1,'{\"message\": \"你好\", \"clinic_id\": \"1\", \"account_id\": \"1\", \"session_id\": \"b6e80ba6-a7f2-490a-9027-590043428756\", \"account_name\": \"管理员\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 09:43:39'),(240,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 19:41:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"27\", \"doctor_account_id\": 2, \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"1723\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11644\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"protocol_version\":\"1.0\",\"_original_payload\":{\"fields\":{\"patient_id\":\"13\",\"patient_name\":\"链路测试患者\",\"doctor_account_id\":2,\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 19:41:35\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"27\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \"},\"account_id\":1,\"scene_id\":1,\"scene_name\":\"根管治疗\",\"step_id\":1,\"step_name\":\"开髓引流\"},\"account_id\":1,\"function\":\"binglikuoxie\",\"context\":{\"account_id\":1,\"step_name\":\"开髓引流\",\"account_name\":\"\",\"scene_id\":1,\"step_id\":1,\"clinic_id\":\"\",\"scene_name\":\"根管治疗\",\"timestamp\":1779191182808},\"input_fields\":{\"fields\":{\"label\":\"fields\",\"value\":{\"patient_id\":\"13\",\"patient_name\":\"链路测试患者\",\"doctor_account_id\":2,\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 19:41:35\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"27\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation',0,0,NULL,'2026-05-19 11:46:23'),(241,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 19:41:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"27\", \"doctor_account_id\": 2, \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"output\":{\"chief_complaint\":\"27牙自发痛3天，夜间加剧\",\"present_illness_history\":\"患者3天前无明显诱因出现27牙自发性疼痛，呈阵发性锐痛，夜间疼痛加剧，影响睡眠。冷热刺激可诱发或加重疼痛，伴放射至同侧头面部。自行服用止痛药（具体不详）效果欠佳，遂来就诊。患牙既往有龋病史，未行根管治疗。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史，否认肝炎、结核等传染病史，否认药物及食物过敏史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"患者精神可，饮食睡眠因牙痛稍受影响，大小便正常。\",\"examination_findings\":\"27牙牙体见大面积龋坏，探诊（+），探及穿髓孔，有少量暗红色血液渗出，触痛明显。叩诊（+），无明显松动。冷热测：冷刺激反应敏感，持续疼痛，去除刺激后疼痛仍持续数秒；热刺激诱发剧痛。牙龈无明显红肿，未见瘘管口。\",\"auxiliary_examination\":\"建议完善27牙根尖片检查，预期可见27牙龋坏近髓腔，髓角暴露，根尖周未见明显低密度影或可见牙周膜间隙增宽。\",\"diagnosis\":\"考虑27牙急性牙髓炎\",\"treatment_plan\":\"1. 治疗思路：该牙诊断为急性牙髓炎，需行根管治疗。当前阶段行开髓引流术，旨在建立髓腔引流通道，降低髓腔压力，缓解疼痛。\\n2. 操作要点：局部麻醉（阿替卡因肾上腺素注射液），上橡皮障隔离患牙，使用高速涡轮手机配合裂钻去除龋坏组织及髓室顶，暴露髓腔，用挖匙或拔髓针去除冠髓，疏通根管口，髓腔内置樟脑苯酚棉球开放引流。\\n3. 后续步骤：待急性症状缓解后（约3-7天），行根管预备、根管消毒及根管充填。\",\"treatment\":\"拟行27牙开髓引流术：常规消毒铺巾，1.7ml阿替卡因肾上腺素注射液行27牙局部浸润麻醉，上橡皮障隔离患牙。高速涡轮手机配合裂钻去尽龋坏组织，暴露髓腔，揭净髓室顶，用挖匙去除冠髓，1%次氯酸钠溶液冲洗髓腔，疏通根管口，髓腔内放置樟脑苯酚棉球，以暂封材料暂封窝洞，开放引流。\",\"medical_advice\":\"1. 术后2小时内禁食，麻药消退后方可进食，避免患侧咀嚼。\\n2. 术后可能出现轻微胀痛或咬合不适，属正常术后反应，可服用布洛芬等止痛药缓解。\\n3. 保持口腔卫生，轻柔刷牙，避免食物嵌塞。\\n4. 若疼痛加剧、面部肿胀或发热，请及时复诊。\\n5. 请于3-7天后复诊进行下一步根管预备治疗。\",\"prescription\":\"布洛芬缓释胶囊 300mg×10粒 口服 每日2次 每次1粒（疼痛时服用）\",\"record_tags\":\"初诊,根管治疗,开髓引流,急性牙髓炎,27牙\",\"image_summary\":\"无\",\"notes\":\"已向患者说明患牙为急性牙髓炎，需行根管治疗方能保留患牙，告知根管治疗流程、费用、疗程及可能的并发症（如术后疼痛、根管台阶、器械分离、根管侧穿等），患者表示理解并同意治疗。医患沟通记录已签署。\",\"draft_record\":\"初诊病历\"}}',0,0,NULL,'2026-05-19 13:43:14'),(242,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"牙髓炎\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:43:42\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼痛三天 夜间痛 冷热刺激痛 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼痛三天 夜间痛 冷热刺激痛\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"无\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 13:44:34'),(243,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"牙髓炎\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:43:42\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼痛三天 夜间痛 冷热刺激痛 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼痛三天 夜间痛 冷热刺激痛\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"无\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-19 13:44:34'),(244,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"牙髓炎\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:43:42\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼痛三天 夜间痛 冷热刺激痛 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼痛三天 夜间痛 冷热刺激痛\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"无\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}','{\"output\":{\"chief_complaint\":\"27牙自发痛3天，夜间痛加重1天\",\"present_illness_history\":\"患者自述3天前无明显诱因出现27牙自发性疼痛，呈阵发性锐痛，遇冷热刺激疼痛加剧，夜间疼痛明显，影响睡眠。近1天疼痛呈持续性加重，伴放射至同侧头面部。患者未行任何治疗，自行服用消炎药（具体不详）效果不佳，今来我院就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史，否认肝炎、结核等传染病史，否认药物及食物过敏史，否认外伤及手术史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"患者精神状态可，饮食因疼痛受影响，睡眠因夜间痛欠佳，大小便正常。\",\"examination_findings\":\"颌面部对称，无肿胀。27牙牙体完整，牙合面见深龋洞，探诊（+），探及穿髓孔，有少量暗红色血液渗出；叩诊（++），有明显叩痛；松动度Ⅰ度；冷刺激测试（+++），呈剧烈疼痛，去除刺激后疼痛持续；热刺激测试（++），疼痛明显；牙龈轻度红肿，根尖区扪诊不适。\",\"auxiliary_examination\":\"建议拍摄27牙根尖片：预期可见27牙牙体低密度影累及髓腔，根尖周组织无明显低密度影，或可见牙周膜间隙增宽。需进一步完善根尖片以评估根管形态、根尖周情况及根管预备参考长度。\",\"diagnosis\":\"考虑27牙急性牙髓炎（不可复性）\",\"treatment_plan\":\"1. 治疗思路：针对27牙急性牙髓炎，当前首要行开髓引流术，以建立髓腔引流通道，降低髓腔内压力，缓解患者剧烈疼痛。\\n2. 操作要点：在局部麻醉下（必兰麻或利多卡因），常规消毒术区，使用高速涡轮手机配合裂钻或金刚砂钻，从牙合面龋坏处进入，沿牙体长轴方向穿通髓腔，揭除髓室顶，充分暴露根管口，用挖匙去除冠髓，用冲洗液（生理盐水/次氯酸钠）冲洗髓腔，通畅根管，用无菌棉球置于髓腔内开放引流。\\n3. 后续步骤：待急性期症状缓解后（约1-2天），择期行根管预备、根管消毒及根管充填术，完成根管治疗。\",\"treatment\":\"拟行27牙开髓引流术。治疗方案已向患者说明，患者签署知情同意书后，予盐酸阿替卡因肾上腺素注射液（必兰麻）1.7ml局部浸润麻醉，麻醉生效后，常规消毒铺巾，高速涡轮手机配合金刚砂球钻从牙合面深龋处进入，穿通髓腔，揭净髓室顶，可见髓腔内暗红色血性渗出，压力较高，用挖匙去除冠髓组织，3%次氯酸钠溶液+生理盐水交替冲洗髓腔，用15号K锉探查并通畅根管，根管内见少量渗血，无脓性分泌物。棉球拭干髓腔，无菌干棉球开放置于髓腔内1-2天引流。压迫止血，嘱患者30分钟后吐棉球。\",\"medical_advice\":\"1. 术后医嘱：开髓引流术后24小时内可能会有轻微不适感，可适当服用布洛芬等镇痛药缓解；请勿用患侧咀嚼；保持口腔卫生，饭后漱口。\\n2. 复诊安排：请于1-2天后复诊（建议2026-05-20或2026-05-21），由医生评估症状缓解情况后，继续后续根管预备及根管治疗。\\n3. 注意事项：如出现面部肿胀、发热、疼痛加剧等异常情况，请及时就诊。\",\"prescription\":\"布洛芬缓释胶囊 0.3g×10粒，Sig：必要时口服1粒。\",\"record_tags\":\"初诊,根管治疗,开髓引流,急性牙髓炎,27牙\",\"image_summary\":\"待完善根尖片\",\"notes\":\"1. 已向患者说明27牙急性牙髓炎的诊断依据及病情严重性，告知现阶段需行开髓引流缓解疼痛。\\n2. 已向患者说明根管治疗的必要性、治疗流程（开髓引流→根管预备→根管消毒→根管充填）、所需复诊次数（约3-4次）及治疗费用，患者表示知情理解并同意治疗。\\n3. 告知根管治疗后建议行全冠修复以保护患牙，患者表示了解。\\n4. 医患沟通顺畅，患者配合度高。\",\"draft_record\":\"链路测试患者，27牙，初诊。主诉27牙自发痛3天，夜间痛加重1天。现病史如上。检查：27牙深龋，探穿髓，叩痛（++），冷热测（+++），松动Ⅰ度。诊断：考虑27牙急性牙髓炎（不可复性）。处理：行开髓引流术，髓腔开放，嘱1-2天后复诊。\"}}',0,0,NULL,'2026-05-19 13:44:59'),(245,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:46:16\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼痛3天 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼痛3天\", \"operation_items\": [], \"tooth_positions\": \"15\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}','{\"output\":{\"chief_complaint\":\"27牙自发性疼痛3天，加重1天\",\"present_illness_history\":\"患者自述3天前无明显诱因出现27牙自发性疼痛，呈阵发性锐痛，夜间加重，冷热刺激可诱发剧痛，1天前疼痛明显加剧，呈持续性跳痛，影响进食及睡眠，无放射痛。自服布洛芬后效果欠佳，未于他院行其他特殊治疗，今来我科就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史；否认肝炎、结核等传染性疾病史；否认药物及食物过敏史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"患者精神可，饮食因牙痛受影响，睡眠欠佳，大小便正常。\",\"examination_findings\":\"颌面部对称，无肿胀；27牙牙体完整，可见深龋洞，探诊（+），探及穿髓孔，疼痛明显；叩诊（++），松动度Ⅰ度；冷热测疼痛加剧，去除刺激后疼痛持续数秒；牙龈无红肿，无窦道口。\",\"auxiliary_examination\":\"建议完善27牙根尖X线片，预期可见27牙冠部低密度影累及牙髓腔，根尖周膜间隙可无明显增宽或呈轻度增宽表现，根管影像清晰，未见明显根尖周低密度影。\",\"diagnosis\":\"考虑27牙急性牙髓炎\",\"treatment_plan\":\"1. 治疗思路：针对27牙急性牙髓炎，当前治疗阶段为开髓引流，目的在于建立根管引流通路，降低牙髓腔内压力，迅速缓解疼痛。\\n2. 操作要点：在局麻下（阿替卡因肾上腺素注射液），使用高速涡轮手机配合裂钻或金刚砂车针于27牙合面开髓，揭净髓室顶，充分暴露髓腔，探查根管口，用锐利挖匙或小号根管锉去除冠髓，建立根管上段通畅，引流减压，置樟脑酚棉球或CP棉球于髓腔内开放引流。\\n3. 后续步骤：待急性症状缓解后（约1-3天），预约复诊行根管预备、根管消毒及根管充填。\",\"treatment\":\"拟行：1%阿替卡因肾上腺素注射液局部浸润麻醉下，27牙上橡皮障隔湿，高速涡轮手机裂钻于牙合面开髓，揭净髓室顶，暴露髓腔，探查根管口，去除冠髓，可见暗红色血性渗出，根管上段预备通畅，减压引流，生理盐水冲洗，棉球吸干，髓腔内放CP棉球，丁香油氧化锌暂封膏开放引流。\",\"medical_advice\":\"1. 术后2小时内勿进食，麻药消退前勿咬硬物，避免烫食；\\n2. 开髓后髓腔内放置药物棉球，请勿自行取出，若棉球脱落请及时复诊重新放置；\\n3. 暂用对侧咀嚼，注意口腔卫生，餐后漱口；\\n4. 若术后出现剧烈疼痛、面部肿胀或发热等异常情况，请及时复诊或联系我科；\\n5. 请在1-3天后复诊行根管预备，复诊时间：2026-05-21至2026-05-22。\",\"prescription\":\"1%阿替卡因肾上腺素注射液 1.7ml × 1支，局部浸润麻醉用（术中已用）。\",\"record_tags\":\"初诊,根管治疗,开髓引流,27牙,急性牙髓炎\",\"image_summary\":\"暂缺根尖片，建议完善X线检查\",\"notes\":\"1. 已向患者解释病情及治疗方案，告知根管治疗需多次复诊，患者表示理解并同意治疗；\\n2. 告知治疗费用及风险，患者签署知情同意书；\\n3. 嘱患者按时复诊，避免治疗间隔过长导致根管再感染。\",\"draft_record\":\"已完成初诊病历撰写，待医生审核确认\"}}',0,0,NULL,'2026-05-19 13:47:11'),(246,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:48:14\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"12\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"headers\":{\"host\":\"wn8n.smallcherry.cn\",\"user-agent\":\"Java-http-client/17.0.18\",\"content-length\":\"1725\",\"accept\":\"application/json, text/event-stream\",\"content-type\":\"application/json\",\"remote-host\":\"117.155.159.70\",\"x-forwarded-for\":\"117.155.159.70, 127.0.0.1\",\"x-forwarded-host\":\"wn8n.smallcherry.cn\",\"x-forwarded-port\":\"443\",\"x-forwarded-proto\":\"http\",\"x-real-ip\":\"117.155.159.70\",\"x-real-port\":\"11537\",\"accept-encoding\":\"gzip\"},\"params\":{},\"query\":{},\"body\":{\"protocol_version\":\"1.0\",\"_original_payload\":{\"fields\":{\"patient_id\":\"13\",\"patient_name\":\"链路测试患者\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 21:48:14\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"12\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\" \"},\"account_id\":1,\"scene_id\":1,\"scene_name\":\"根管治疗\",\"step_id\":1,\"step_name\":\"开髓引流\"},\"account_id\":1,\"function\":\"binglikuoxie\",\"context\":{\"account_id\":1,\"step_name\":\"开髓引流\",\"account_name\":\"\",\"scene_id\":1,\"step_id\":1,\"clinic_id\":\"\",\"scene_name\":\"根管治疗\",\"timestamp\":1779198511885},\"input_fields\":{\"fields\":{\"label\":\"fields\",\"value\":{\"patient_id\":\"13\",\"patient_name\":\"链路测试患者\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 21:48:14\",\"record_type\":\"初诊\",\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"12\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\",\"record_status\":\"final\",\"operati',0,0,NULL,'2026-05-19 13:48:32'),(247,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:48:14\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼痛三天 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼痛三天\", \"operation_items\": [], \"tooth_positions\": \"15\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"output\":{\"chief_complaint\":\"15牙自发性疼痛3天，夜间加重1天\",\"present_illness_history\":\"患者自述近3天来15牙出现自发性疼痛，呈阵发性锐痛，夜间疼痛加重，影响睡眠。冷热刺激可诱发剧烈疼痛，去除刺激后疼痛持续数秒后方可缓解。近1日疼痛呈持续性加重，伴有牵涉性头痛。未经任何治疗，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。否认肝炎、结核等传染病史。否认药物及食物过敏史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"患者精神可，饮食因牙痛受影响，睡眠欠佳，大小便正常。\",\"examination_findings\":\"15牙牙合面见深龋洞，探诊（+），探及穿髓点，探痛明显。叩诊（±），无明显松动。冷热测：冷热刺激疼痛明显，刺激去除后疼痛持续数秒后缓解。牙龈无明显红肿，无窦道口。\",\"auxiliary_examination\":\"建议完善15牙根尖片检查，预期可见15牙牙合面深龋近髓腔，髓室顶完整性欠佳，根尖周未见明显低密度影，牙周膜间隙未见明显增宽。\",\"diagnosis\":\"考虑15牙急性牙髓炎\",\"treatment_plan\":\"本次治疗计划行15牙开髓引流术：1）局部麻醉后，去除龋坏组织，制备开髓洞形；2）揭净髓室顶，暴露髓腔；3）拔除冠髓，建立引流通道；4）髓腔内放置丁香油棉球开放引流，暂封材料封闭窝洞；5）待急性症状缓解后，择期行根管预备及根管充填治疗。\",\"treatment\":\"拟行15牙开髓引流术：阿替卡因肾上腺素注射液局部浸润麻醉，上橡皮障隔离，高速涡轮手机去除龋坏组织，揭净髓室顶，暴露髓腔，拔髓针拔除冠髓，髓腔内放置丁香油棉球，暂封材料暂封。\",\"medical_advice\":\"1）术后2小时内禁食，2小时后可进温凉软食；2）避免患侧咀嚼；3）如术后疼痛加剧或出现面部肿胀，请及时复诊；4）遵医嘱服用抗生素及止痛药（如有开具）；5）约1周后复诊行根管预备，请勿自行取出暂封物；6）保持口腔卫生，常规刷牙时避开患牙区域。\",\"prescription\":\"阿替卡因肾上腺素注射液 1.7ml×1支，局部浸润麻醉用。术后如需用药：头孢克肟胶囊 100mg×6粒，Sig：100mg bid po（需皮试阴性后使用）；布洛芬缓释胶囊 300mg×6粒，Sig：300mg po prn（疼痛时服用）。\",\"record_tags\":\"初诊,根管治疗,开髓引流,15牙,急性牙髓炎\",\"image_summary\":\"\",\"notes\":\"已向患者说明患牙诊断及治疗方案，告知开髓引流为应急处理，后续需完成根管预备及根管充填方可彻底控制感染。告知根管治疗后建议行全冠修复以保护患牙。患者表示理解并同意本次治疗，已签署知情同意书。\",\"draft_record\":\"初诊记录\\n就诊日期：2026-05-19 21:48:14\\n患者姓名：链路测试患者\\n医生：管理员\\n\\n主诉：15牙自发性疼痛3天，夜间加重1天\\n\\n现病史：患者自述近3天来15牙出现自发性疼痛，呈阵发性锐痛，夜间疼痛加重，影响睡眠。冷热刺激可诱发剧烈疼痛，去除刺激后疼痛持续数秒后方可缓解。近1日疼痛呈持续性加重，伴有牵涉性头痛。未经任何治疗，今来就诊。\\n\\n既往史：否认高血压、糖尿病、心脏病等全身系统性疾病史。否认药物及食物过敏史。\\n\\n检查所见：15牙牙合面见深龋洞，探诊（+），探及穿髓点，探痛明显。叩诊（±），无明显松动。冷热测：冷热刺激疼痛明显，刺激去除后疼痛持续数秒后缓解。\\n\\n辅助检查：建议完善15牙根尖片检查，预期可见15牙牙合面深龋近髓腔，髓室顶完整性欠佳。\\n\\n诊断：考虑15牙急性牙髓炎\\n\\n治疗计划：15牙开髓引流术，待急性症状缓解后择期行根管治疗。\\n\\n治疗：拟行15牙开髓引流术。\\n\\n医嘱：术后注意事项及1周后复诊。\\n\\n医生签名：管理员\"}}',0,0,NULL,'2026-05-19 13:49:44'),(248,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明患牙诊断及治疗方案，告知开髓引流为应急处理，后续需完成根管预备及根管充填方可彻底控制感染。告知根管治疗后建议行全冠修复以保护患牙。患者表示理解并同意本次治疗，已签署知情同意书。\", \"diagnosis\": \"考虑15牙急性牙髓炎\", \"treatment\": \"拟行15牙开髓引流术：阿替卡因肾上腺素注射液局部浸润麻醉，上橡皮障隔离，高速涡轮手机去除龋坏组织，揭净髓室顶，暴露髓腔，拔髓针拔除冠髓，髓腔内放置丁香油棉球，暂封材料暂封。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-19 21:48:14\", \"doctor_name\": \"管理员\", \"record_tags\": \"初诊,根管治疗,开髓引流,15牙,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"15牙自发性疼痛3天，夜间加重1天 患者自述近3天来15牙出现自发性疼痛，呈阵发性锐痛，夜间疼痛加重，影响睡眠。冷热刺激可诱发剧烈疼痛，去除刺激后疼痛持续数秒后方可缓解。近1日疼痛呈持续性加重，伴有牵涉性头痛。未经任何治疗，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"阿替卡因肾上腺素注射液 1.7ml×1支，局部浸润麻醉用。术后如需用药：头孢克肟胶囊 100mg×6粒，Sig：100mg bid po（需皮试阴性后使用）；布洛芬缓释胶囊 300mg×6粒，Sig：300mg po prn（疼痛时服用）。\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1）术后2小时内禁食，2小时后可进温凉软食；2）避免患侧咀嚼；3）如术后疼痛加剧或出现面部肿胀，请及时复诊；4）遵医嘱服用抗生素及止痛药（如有开具）；5）约1周后复诊行根管预备，请勿自行取出暂封物；6）保持口腔卫生，常规刷牙时避开患牙区域。\", \"treatment_plan\": \"本次治疗计划行15牙开髓引流术：1）局部麻醉后，去除龋坏组织，制备开髓洞形；2）揭净髓室顶，暴露髓腔；3）拔除冠髓，建立引流通道；4）髓腔内放置丁香油棉球开放引流，暂封材料封闭窝洞；5）待急性症状缓解后，择期行根管预备及根管充填治疗。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"15牙自发性疼痛3天，夜间加重1天\", \"operation_items\": [], \"tooth_positions\": \"15\", \"doctor_account_id\": \"\", \"general_condition\": \"患者精神可，饮食因牙痛受影响，睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"15牙牙合面见深龋洞，探诊（+），探及穿髓点，探痛明显。叩诊（±），无明显松动。冷热测：冷热刺激疼痛明显，刺激去除后疼痛持续数秒后缓解。牙龈无明显红肿，无窦道口。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。否认肝炎、结核等传染病史。否认药物及食物过敏史。\", \"auxiliary_examination\": \"建议完善15牙根尖片检查，预期可见15牙牙合面深龋近髓腔，髓室顶完整性欠佳，根尖周未见明显低密度影，牙周膜间隙未见明显增宽。\", \"present_illness_history\": \"患者自述近3天来15牙出现自发性疼痛，呈阵发性锐痛，夜间疼痛加重，影响睡眠。冷热刺激可诱发剧烈疼痛，去除刺激后疼痛持续数秒后方可缓解。近1日疼痛呈持续性加重，伴有牵涉性头痛。未经任何治疗，今来就诊。\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"治疗项目\":\"根管治疗\",\"治疗步骤\":\"开髓引流\",\"病历表单\":{\"patient_id\":\"13\",\"patient_name\":\"链路测试患者\",\"doctor_account_id\":\"\",\"doctor_name\":\"管理员\",\"nurse_name\":\"\",\"assistant_name\":\"\",\"visit_date\":\"2026-05-19 21:48:14\",\"record_type\":\"初诊\",\"chief_complaint\":\"15牙自发性疼痛3天，夜间加重1天\",\"present_illness_history\":\"患者自述近3天来15牙出现自发性疼痛，呈阵发性锐痛，夜间疼痛加重，影响睡眠。冷热刺激可诱发剧烈疼痛，去除刺激后疼痛持续数秒后方可缓解。近1日疼痛呈持续性加重，伴有牵涉性头痛。未经任何治疗，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。否认肝炎、结核等传染病史。否认药物及食物过敏史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"患者精神可，饮食因牙痛受影响，睡眠欠佳，大小便正常。\",\"examination_findings\":\"15牙牙合面见深龋洞，探诊（+），探及穿髓点，探痛明显。叩诊（±），无明显松动。冷热测：冷热刺激疼痛明显，刺激去除后疼痛持续数秒后缓解。牙龈无明显红肿，无窦道口。\",\"auxiliary_examination\":\"建议完善15牙根尖片检查，预期可见15牙牙合面深龋近髓腔，髓室顶完整性欠佳，根尖周未见明显低密度影，牙周膜间隙未见明显增宽。\",\"diagnosis\":\"考虑15牙急性牙髓炎\",\"treatment_plan\":\"本次治疗计划行15牙开髓引流术：1）局部麻醉后，去除龋坏组织，制备开髓洞形；2）揭净髓室顶，暴露髓腔；3）拔除冠髓，建立引流通道；4）髓腔内放置丁香油棉球开放引流，暂封材料封闭窝洞；5）待急性症状缓解后，择期行根管预备及根管充填治疗。\",\"treatment\":\"拟行15牙开髓引流术：阿替卡因肾上腺素注射液局部浸润麻醉，上橡皮障隔离，高速涡轮手机去除龋坏组织，揭净髓室顶，暴露髓腔，拔髓针拔除冠髓，髓腔内放置丁香油棉球，暂封材料暂封。\",\"tooth_positions\":\"15\",\"medical_advice\":\"1）术后2小时内禁食，2小时后可进温凉软食；2）避免患侧咀嚼；3）如术后疼痛加剧或出现面部肿胀，请及时复诊；4）遵医嘱服用抗生素及止痛药（如有开具）；5）约1周后复诊行根管预备，请勿自行取出暂封物；6）保持口腔卫生，常规刷牙时避开患牙区域。\",\"prescription\":\"阿替卡因肾上腺素注射液 1.7ml×1支，局部浸润麻醉用。术后如需用药：头孢克肟胶囊 100mg×6粒，Sig：100mg bid po（需皮试阴性后使用）；布洛芬缓释胶囊 300mg×6粒，Sig：300mg po prn（疼痛时服用）。\",\"record_tags\":\"初诊,根管治疗,开髓引流,15牙,急性牙髓炎\",\"image_summary\":\"\",\"notes\":\"已向患者说明患牙诊断及治疗方案，告知开髓引流为应急处理，后续需完成根管预备及根管充填方可彻底控制感染。告知根管治疗后建议行全冠修复以保护患牙。患者表示理解并同意本次治疗，已签署知情同意书。\",\"record_status\":\"final\",\"operation_items\":[],\"draft_record\":\"15牙自发性疼痛3天，夜间加重1天 患者自述近3天来15牙出现自发性疼痛，呈阵发性锐痛，夜间疼痛加重，影响睡眠。冷热刺激可诱发剧烈疼痛，去除刺激后疼痛持续数秒后方可缓解。近1日疼痛呈持续性加重，伴有牵涉性头痛。未经任何治疗，今来就诊。\"}}',0,0,NULL,'2026-05-20 02:54:09'),(249,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-20 02:54:51'),(250,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-20 02:54:51'),(251,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:54:55'),(252,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:54:55'),(253,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:55:05'),(254,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:55:05'),(255,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:55:36'),(256,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:55:36'),(257,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-20 02:55:54'),(258,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 404 {\"code\":404,\"message\":\"The requested webhook \\\"test\\\" is not registered.\",\"hint\":\"Click the \'Execute workflow\' button on the canvas, then try again. (In test mode, the webhook only works for one call after you click this button)\"}','2026-05-20 02:55:54'),(259,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:56:03'),(260,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:56:03'),(261,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:56:18'),(262,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": \"\", \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"\"}',NULL,0,0,'AI 代理调用失败：HTTP 500 {\"code\":0,\"message\":\"No item to return was found\"}','2026-05-20 02:56:18'),(263,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"治疗项目\":\"根管治疗\",\"治疗步骤\":\"开髓引流\",\"病历表单\":{\"chief_complaint\":\"\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\"}}',0,0,NULL,'2026-05-20 03:10:36'),(264,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \" \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"output\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"XX牙\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 03:43:39'),(265,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": \"\", \"scene_id\": 14, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"拔牙\"}','{\"json\":{\"output\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"XX牙\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}},\"pairedItem\":{\"item\":0}}',0,0,NULL,'2026-05-20 05:52:02'),(266,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": \"\", \"scene_id\": 14, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"拔牙\"}','{\"治疗项目\":\"拔牙\",\"治疗步骤\":\"\",\"病历表单\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 05:56:04'),(267,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"治疗项目\":\"根管治疗\",\"治疗步骤\":\"开髓引流\",\"病历表单\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 05:56:17'),(268,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": \"\", \"scene_id\": 3, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"树脂充填\"}','{\"治疗项目\":\"树脂充填\",\"治疗步骤\":\"\",\"病历表单\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 05:56:30'),(269,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": \"\", \"scene_id\": 4, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"种植修复\"}','{\"治疗项目\":\"种植修复\",\"治疗步骤\":\"\",\"病历表单\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 05:56:45'),(270,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": \"\", \"scene_id\": 6, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"洁牙\"}','{\"治疗项目\":\"洁牙\",\"治疗步骤\":\"\",\"病历表单\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 05:56:53'),(271,'binglikuoxie',1,'{\"fields\": {\"notes\": \"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\", \"diagnosis\": \"考虑XX牙急性牙髓炎\", \"treatment\": \"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 10:54:35\", \"doctor_name\": \"管理员\", \"record_tags\": \"根管治疗,开髓引流,急性牙髓炎\", \"record_type\": \"初诊\", \"draft_record\": \"XX牙自发痛3天 患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\", \"patient_name\": \"链路测试患者\", \"prescription\": \"暂无\", \"image_summary\": \"无\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\", \"treatment_plan\": \"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\", \"allergy_history\": \"否认药物及食物过敏史。\", \"chief_complaint\": \"XX牙自发痛3天\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"精神可，饮食、睡眠欠佳，大小便正常。\", \"infectious_history\": \"否认肝炎、结核等传染病史。\", \"examination_findings\": \"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\", \"past_medical_history\": \"否认高血压、糖尿病、心脏病等全身系统性疾病史。\", \"auxiliary_examination\": \"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\", \"present_illness_history\": \"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\"}, \"step_id\": \"\", \"scene_id\": 7, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"全冠修复\"}','{\"治疗项目\":\"全冠修复\",\"治疗步骤\":\"\",\"病历表单\":{\"chief_complaint\":\"XX牙自发痛3天\",\"present_illness_history\":\"患者自述XX牙近3天出现自发痛、夜间痛，遇冷热刺激疼痛加剧，疼痛呈放射性，无法定位，口服止痛药效果不佳，今来就诊。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食、睡眠欠佳，大小便正常。\",\"examination_findings\":\"视诊见XX牙颌面深龋洞，探诊（+）深达髓腔，探痛明显；叩诊（±），无明显松动；冷热测呈激发痛，刺激去除后疼痛持续数秒；牙龈未见明显红肿及瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖片以评估龋坏深度、根尖周情况及根管形态。\",\"diagnosis\":\"考虑XX牙急性牙髓炎\",\"treatment_plan\":\"拟行根管治疗，本次先行开髓引流以缓解疼痛。治疗思路：局麻下去龋、揭髓室顶、拔除冠髓、疏通根管上段，开放引流降低髓腔压力。后续复诊行根管预备、消毒及充填。\",\"treatment\":\"1. 阿替卡因肾上腺素注射液局麻下，上橡皮障隔离患牙；2. 高速手机去净龋坏组织，揭髓室顶，暴露髓腔；3. 用拔髓针拔除冠髓，疏通根管上段；4. 3%次氯酸钠冲洗髓腔；5. 髓腔内放置丁香油棉球，开放引流。\",\"tooth_positions\":\"\",\"medical_advice\":\"1. 治疗当日勿用患侧咀嚼，避免过冷过热饮食；2. 遵医嘱口服消炎止痛药（如需）；3. 保持口腔卫生；4. 开放引流期间若疼痛加剧或出现面部肿胀，请及时复诊；5. 1周后复诊行根管预备。\",\"prescription\":\"暂无\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"已向患者说明根管治疗的必要性、疗程、费用及预后，患者表示理解并同意治疗。\"}}',0,0,NULL,'2026-05-20 05:57:05'),(272,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 16:05:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": \"\", \"scene_id\": 14, \"step_name\": \"\", \"account_id\": 1, \"scene_name\": \"拔牙\"}','{\"治疗项目\":\"拔牙\",\"治疗步骤\":\"\",\"病历表单\":{\"chief_complaint\":\"右上后牙疼\",\"present_illness_history\":\"\",\"past_medical_history\":\"\",\"infectious_history\":\"\",\"allergy_history\":\"\",\"general_condition\":\"\",\"examination_findings\":\"\",\"auxiliary_examination\":\"\",\"diagnosis\":\"\",\"treatment_plan\":\"\",\"treatment\":\"\",\"tooth_positions\":\"\",\"medical_advice\":\"\",\"prescription\":\"\",\"record_tags\":\"\",\"image_summary\":\"\",\"notes\":\"\"}}',0,0,NULL,'2026-05-20 08:05:54'),(273,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 16:05:25\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"右上后牙疼 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"右上后牙疼\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"output\":{\"chief_complaint\":\"右上后牙自发痛3天\",\"present_illness_history\":\"患者3天前右上后牙出现自发痛，呈阵发性，夜间加重，冷热刺激可诱发剧烈疼痛，疼痛放射至同侧头面部，无法定位，未经特殊处理。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食欠佳，睡眠因疼痛受影响，大小便正常。\",\"examination_findings\":\"视诊：右上后牙牙合面深龋洞，探诊（+），探及穿髓孔，叩诊（±），无明显松动，冷热测敏感且疼痛持续，牙龈无明显红肿，未见瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖X线片，预期可见牙体低密度影接近穿髓，根尖周膜间隙可无明显增宽。\",\"diagnosis\":\"考虑右上后牙急性牙髓炎\",\"treatment_plan\":\"行根管治疗（开髓引流）以缓解疼痛，后续待症状缓解后行根管预备、消毒及根管充填。\",\"treatment\":\"局麻下（阿替卡因肾上腺素注射液），去尽龋坏腐质，揭髓室顶，暴露髓腔，拔除牙髓，疏通根管，根管冲洗后置CP棉球开放引流。\",\"tooth_positions\":\"右上后牙\",\"medical_advice\":\"1.避免患侧咀嚼，勿用患牙咬硬物。2.术后可能出现轻微胀痛，可口服布洛芬止痛。3.保持口腔清洁，复诊前勿填塞开放口。4.遵医嘱按时复诊，1周后复诊行根管预备。\",\"prescription\":\"布洛芬缓释胶囊 300mg po qd×3d（必要时）\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"术前已向患者解释根管治疗的必要性、过程、费用及可能风险（如器械分离、根管侧穿、术后疼痛等），患者表示理解并签署知情同意书。\"}}',0,0,NULL,'2026-05-20 08:06:39'),(274,'binglikuoxie',1,'{\"fields\": {\"notes\": \"\", \"diagnosis\": \"\", \"treatment\": \"\", \"nurse_name\": \"\", \"patient_id\": \"13\", \"visit_date\": \"2026-05-20 17:05:34\", \"doctor_name\": \"管理员\", \"record_tags\": \"\", \"record_type\": \"初诊\", \"draft_record\": \"牙疼 \", \"patient_name\": \"链路测试患者\", \"prescription\": \"\", \"image_summary\": \"\", \"record_status\": \"final\", \"assistant_name\": \"\", \"medical_advice\": \"\", \"treatment_plan\": \"\", \"allergy_history\": \"\", \"chief_complaint\": \"牙疼\", \"operation_items\": [], \"tooth_positions\": \"\", \"doctor_account_id\": \"\", \"general_condition\": \"\", \"infectious_history\": \"\", \"examination_findings\": \"\", \"past_medical_history\": \"\", \"auxiliary_examination\": \"\", \"present_illness_history\": \"\"}, \"step_id\": 1, \"scene_id\": 1, \"step_name\": \"开髓引流\", \"account_id\": 1, \"scene_name\": \"根管治疗\"}','{\"output\":{\"chief_complaint\":\"右上后牙自发痛3天\",\"present_illness_history\":\"患者3天前右上后牙出现自发痛，呈阵发性，夜间加重，冷热刺激可诱发剧烈疼痛，疼痛放射至同侧头面部，无法定位，未经特殊处理。\",\"past_medical_history\":\"否认高血压、糖尿病、心脏病等全身系统性疾病史。\",\"infectious_history\":\"否认肝炎、结核等传染病史。\",\"allergy_history\":\"否认药物及食物过敏史。\",\"general_condition\":\"精神可，饮食欠佳，睡眠因疼痛受影响，大小便正常。\",\"examination_findings\":\"视诊：右上后牙牙合面深龋洞，探诊（+），探及穿髓孔，叩诊（±），无明显松动，冷热测敏感且疼痛持续，牙龈无明显红肿，未见瘘管。\",\"auxiliary_examination\":\"暂缺，建议完善根尖X线片，预期可见牙体低密度影接近穿髓，根尖周膜间隙可无明显增宽。\",\"diagnosis\":\"考虑右上后牙急性牙髓炎\",\"treatment_plan\":\"行根管治疗（开髓引流）以缓解疼痛，后续待症状缓解后行根管预备、消毒及根管充填。\",\"treatment\":\"局麻下（阿替卡因肾上腺素注射液），去尽龋坏腐质，揭髓室顶，暴露髓腔，拔除牙髓，疏通根管，根管冲洗后置CP棉球开放引流。\",\"tooth_positions\":\"右上后牙\",\"medical_advice\":\"1.避免患侧咀嚼，勿用患牙咬硬物。2.术后可能出现轻微胀痛，可口服布洛芬止痛。3.保持口腔清洁，复诊前勿填塞开放口。4.遵医嘱按时复诊，1周后复诊行根管预备。\",\"prescription\":\"布洛芬缓释胶囊 300mg po qd×3d（必要时）\",\"record_tags\":\"根管治疗,开髓引流,急性牙髓炎\",\"image_summary\":\"无\",\"notes\":\"术前已向患者解释根管治疗的必要性、过程、费用及可能风险（如器械分离、根管侧穿、术后疼痛等），患者表示理解并签署知情同意书。\"}}',0,0,NULL,'2026-05-20 09:06:06');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_risk_assessment_patient` (`patient_id`),
  KEY `idx_ai_risk_assessment_level` (`risk_level`),
  KEY `idx_ai_risk_assessment_valid` (`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI æ‚£è€…é£Žé™©è¯„ä¼°è¡¨';
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
  `clinic_id` bigint NOT NULL COMMENT '诊所ID',
  `key` varchar(64) NOT NULL COMMENT 'API Key值',
  `name` varchar(64) DEFAULT '默认API Key' COMMENT 'Key名称',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
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
INSERT INTO `api_key` VALUES (1,1,'sk-saas-a8f06f47a73e4273','默认Key',1,'2026-05-15 06:36:34','2026-05-15 06:36:34');
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
  PRIMARY KEY (`id`),
  KEY `idx_appointment_date` (`appointment_date`),
  KEY `idx_patient_name` (`patient_name`),
  KEY `idx_status` (`status`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_appointment_doctor_account_id` (`doctor_account_id`),
  KEY `idx_appointment_medical_record_id` (`medical_record_id`),
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (3,3,'李四','2026-05-06','08:30:00',30,3,'李医生','牙齿矫正检查',NULL,'已完成','已预约',NULL,NULL),(4,3,'李四','2026-05-09','14:00:00',60,3,'李医生','矫正器调整',NULL,'待治疗','已挂号','2026-05-09 09:46:08',NULL),(6,4,'王五','2026-05-07','11:00:00',60,2,'王医生','根管治疗复诊',NULL,'已治疗','已挂号','2026-05-13 11:51:54',NULL),(7,5,'赵六','2026-05-07','08:30:00',60,3,'李医生','洗牙',NULL,'已治疗','已预约',NULL,NULL),(8,5,'赵六','2026-05-10','15:00:00',60,3,'李医生','牙周检查','测试取消','已取消',NULL,NULL,NULL),(9,6,'陈七','2026-05-05','09:00:00',60,2,'王医生','拔牙',NULL,'已完成','已预约',NULL,NULL),(10,6,'陈七','2026-05-08','16:00:00',60,2,'王医生','拔牙复查',NULL,'待治疗','已预约',NULL,NULL),(11,7,'刘八','2026-05-07','15:00:00',60,3,'李医生','补牙',NULL,'待治疗','已预约',NULL,NULL),(12,7,'刘八','2026-05-07','09:30:00',60,3,'李医生','补牙复查',NULL,'已取消','已预约',NULL,NULL),(13,8,'周九','2026-05-07','16:00:00',60,2,'王医生','种植牙二期',NULL,'已治疗','已预约',NULL,NULL),(14,8,'周九','2026-05-08','12:30:00',60,2,'王医生','种植牙拆线',NULL,'待治疗','已预约',NULL,NULL),(15,9,'吴十','2026-05-07','17:00:00',60,3,'李医生','正畸初诊',NULL,'待治疗','已预约',NULL,NULL),(16,9,'吴十','2026-05-09','08:00:00',30,3,'李医生','正畸方案确认',NULL,'治疗中','已挂号','2026-05-09 10:36:35',NULL),(17,9,'吴十','2026-05-08','05:10:00',60,2,'王医生','12',NULL,'已就诊','已预约',NULL,NULL),(18,3,'李四','2026-05-25','09:00:00',60,3,'李医生','测试创建预约',NULL,'待治疗',NULL,NULL,NULL),(19,3,'李四','2026-05-09','14:00:00',60,3,'李医生','冲突测试',NULL,'待治疗',NULL,NULL,NULL),(20,4,'王五','2026-05-09','14:00:00',60,3,'李医生','冲突测试不同患者',NULL,'待治疗',NULL,NULL,NULL),(21,3,'李四','2026-05-30','10:00:00',60,3,'李医生','doctor越权创建',NULL,'待治疗',NULL,NULL,NULL),(22,11,'测试患者A','2026-05-14','10:00:00',60,2,'王医生','洁牙',NULL,'已完成',NULL,NULL,NULL),(23,11,'测试患者A','2026-05-14','10:00:00',60,2,'王医生','洁牙冲突测试',NULL,'待治疗',NULL,NULL,NULL),(24,11,'测试患者A','2026-05-14','10:00:00',60,2,'王医生','契约测试',NULL,'待治疗',NULL,NULL,NULL),(25,13,'链路测试患者','2026-05-13','09:00:00',90,NULL,'admin','初诊检查',NULL,'已完成','已到诊',NULL,NULL),(26,13,'链路测试患者','2026-05-13','11:07:00',30,2,'王医生','根管治疗',NULL,'已就诊',NULL,NULL,NULL),(27,13,'链路测试患者','2026-05-14','10:00:00',60,2,'王医生','洗牙检查',NULL,'待治疗',NULL,NULL,NULL),(28,13,'链路测试患者','2026-05-21','10:30:00',60,2,'王医生','根管盒子里',NULL,'待治疗',NULL,NULL,NULL),(29,11,'测试患者A','2026-05-21','13:30:00',60,3,'李医生','根管治疗',NULL,'待治疗',NULL,NULL,NULL);
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
INSERT INTO `business_alert_log` VALUES (1,'2026-05-07','RECORD_COMPLETION_DROP','MEDIUM','病历完成率偏低','病历完成率偏低：当前值 0.00，近7日均值 0.00。','record_completion_rate',0.00,0.00,0.00,'要求接诊当日补齐病历，避免诊疗记录断档。','RULE_BASED','SCHEDULED','2026-05-07 16:40:00'),(2,'2026-05-08','RECORD_COMPLETION_DROP','MEDIUM','病历完成率偏低','病历完成率偏低：当前值 33.33，近7日均值 21.09。','record_completion_rate',33.33,21.09,58.04,'要求接诊当日补齐病历，避免诊疗记录断档。','RULE_BASED','SCHEDULED','2026-05-08 16:39:59'),(3,'2026-05-14','RECORD_COMPLETION_DROP','MEDIUM','病历完成率偏低','病历完成率偏低：当前值 0.00，近7日均值 16.67。','record_completion_rate',0.00,16.67,-100.00,'要求接诊当日补齐病历，避免诊疗记录断档。','RULE_BASED','SCHEDULED','2026-05-14 16:40:00');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_analysis_date` (`analysis_date`),
  KEY `idx_business_analysis_status` (`analysis_status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日经营AI分析日报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_daily_analysis`
--

LOCK TABLES `business_daily_analysis` WRITE;
/*!40000 ALTER TABLE `business_daily_analysis` DISABLE KEYS */;
INSERT INTO `business_daily_analysis` VALUES (1,'2026-05-07','FALLBACK','RULE_BASED','SCHEDULED','gpt-5.4-mini',51,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-07 共预约 8 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 12.50%，未来7日有效预约 8 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。','{\"analysis_date\":\"2026-05-07\",\"total_patients\":8,\"today_appointments\":8,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":8,\"future_7_day_appointments\":8,\"appointment_status_breakdown\":{\"待治疗\":4,\"已预约\":0,\"待就诊\":0,\"已取消\":1,\"其他\":3},\"cancellation_rate\":12.5,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":0.0,\"current_month_net_income\":0.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[{\"doctor_name\":\"李医生\",\"appointment_count\":4,\"treatment_count\":0,\"treatment_revenue\":0.0},{\"doctor_name\":\"王医生\",\"appointment_count\":4,\"treatment_count\":0,\"treatment_revenue\":0.0}],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-07 共预约 8 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 12.50%，未来7日有效预约 8 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。\",\"operating_score\":51,\"trend\":\"flat\",\"highlights\":[\"当日预约 8 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 8 人次，需提前做好排班和回访准备。\",\"当日接诊负荷最高医生为 李医生，预约 4 人次。\"],\"risks\":[{\"title\":\"病历留存偏低\",\"severity\":\"medium\",\"finding\":\"病历完成率仅 0.00%，存在医疗记录闭环不足风险。\",\"recommendation\":\"要求接诊结束当日补齐病历，设置护士/前台复核。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"未来一周到诊基础良好\",\"impact\":\"medium\",\"finding\":\"未来7日已有 8 条有效预约，可形成稳定流水。\",\"recommendation\":\"提前做分层提醒和项目预匹配，提升到诊率与转化率。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P1\",\"action\":\"围绕 李医生 的接诊高峰优化排班和椅位分配。\",\"owner\":\"门诊经理\",\"due\":\"7days\",\"expected_result\":\"缓解高峰拥堵，提高翻台效率。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。\"}','','OpenAI 未启用或未配置 API Key，已生成规则分析','2026-05-07 16:10:00','2026-05-07 16:10:00'),(2,'2026-05-10','FALLBACK','RULE_BASED','SCHEDULED','gpt-5.4-mini',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-10 共预约 1 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。','{\"analysis_date\":\"2026-05-10\",\"total_patients\":8,\"today_appointments\":1,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":1,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":1,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":29300.0,\"current_month_net_income\":-29300.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[{\"doctor_name\":\"李医生\",\"appointment_count\":1,\"treatment_count\":0,\"treatment_revenue\":0.0}],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-10 共预约 1 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 1 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\",\"当日接诊负荷最高医生为 李医生，预约 1 人次。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P1\",\"action\":\"围绕 李医生 的接诊高峰优化排班和椅位分配。\",\"owner\":\"门诊经理\",\"due\":\"7days\",\"expected_result\":\"缓解高峰拥堵，提高翻台效率。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。\"}','','OpenAI 未启用或未配置 API Key，已生成规则分析','2026-05-10 16:28:17','2026-05-10 16:28:17'),(3,'2026-05-11','FALLBACK','RULE_BASED','SCHEDULED','gpt-5.4-mini',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-11 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。','{\"analysis_date\":\"2026-05-11\",\"total_patients\":8,\"today_appointments\":0,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":0,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":29300.0,\"current_month_net_income\":-29300.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-11 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 0 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。\"}','','OpenAI 未启用或未配置 API Key，已生成规则分析','2026-05-11 16:09:59','2026-05-11 16:10:00'),(4,'2026-05-12','FALLBACK','RULE_BASED','SCHEDULED','gpt-5.4-mini',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-12 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。','{\"analysis_date\":\"2026-05-12\",\"total_patients\":8,\"today_appointments\":0,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":0,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":29300.0,\"current_month_net_income\":-29300.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-12 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 0 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。\"}','','OpenAI 未启用或未配置 API Key，已生成规则分析','2026-05-12 16:09:59','2026-05-12 16:10:00'),(5,'2026-05-13','FALLBACK','RULE_BASED','MANUAL','gpt-5.4-mini',57,'flat','当日接诊仍有产出，但现金流承压，需要优先修正收费与取消问题','2026-05-13 共预约 2 人次，完成病历 2 份、治疗 0 例，收入 ¥0.00、支出 ¥1300.00、净现金流 ¥-1300.00。预约取消率 0.00%，未来7日有效预约 4 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。','{\"analysis_date\":\"2026-05-13\",\"total_patients\":10,\"today_appointments\":2,\"today_medical_records\":2,\"today_treatments\":0,\"today_unique_patients\":2,\"future_7_day_appointments\":4,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":2},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":1300.0,\"today_net_income\":-1300.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":100.0,\"current_month_income\":0.0,\"current_month_expense\":2600.0,\"current_month_net_income\":-2600.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[{\"doctor_name\":\"admin\",\"appointment_count\":1,\"treatment_count\":0,\"treatment_revenue\":0.0},{\"doctor_name\":\"王医生\",\"appointment_count\":1,\"treatment_count\":0,\"treatment_revenue\":0.0}],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日接诊仍有产出，但现金流承压，需要优先修正收费与取消问题\",\"summary\":\"2026-05-13 共预约 2 人次，完成病历 2 份、治疗 0 例，收入 ¥0.00、支出 ¥1300.00、净现金流 ¥-1300.00。预约取消率 0.00%，未来7日有效预约 4 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。\",\"operating_score\":57,\"trend\":\"flat\",\"highlights\":[\"当日预约 2 人次，病历记录 2 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥1300.00，净现金流 ¥-1300.00。\",\"未来7日未取消预约 4 人次，需提前做好排班和回访准备。\",\"当日接诊负荷最高医生为 admin，预约 1 人次。\"],\"risks\":[{\"title\":\"现金流为负\",\"severity\":\"high\",\"finding\":\"当日净现金流为负，收入无法覆盖支出。\",\"recommendation\":\"优先核对支出构成和未收费治疗，必要时调整采购与收款节奏。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"接诊闭环基础较好\",\"impact\":\"low\",\"finding\":\"当日接诊患者与病历记录匹配度较高，说明流程执行较稳定。\",\"recommendation\":\"继续把病历、治疗、收费三张表做联动复盘，沉淀标准流程。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P1\",\"action\":\"围绕 admin 的接诊高峰优化排班和椅位分配。\",\"owner\":\"门诊经理\",\"due\":\"7days\",\"expected_result\":\"缓解高峰拥堵，提高翻台效率。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。 当日现金流为负，应同步复盘支出与未收费项目。\"}','','OpenAI 未启用或未配置 API Key，已生成规则分析','2026-05-13 16:09:59','2026-05-13 16:23:02'),(6,'2026-05-14','FALLBACK','RULE_BASED','SCHEDULED','gpt-5.4-mini',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-14 共预约 4 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。','{\"analysis_date\":\"2026-05-14\",\"total_patients\":10,\"today_appointments\":4,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":2,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":3,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":1},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":2600.0,\"current_month_net_income\":-2600.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[{\"doctor_name\":\"王医生\",\"appointment_count\":4,\"treatment_count\":0,\"treatment_revenue\":0.0}],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-14 共预约 4 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 当前尚未启用 OpenAI，暂由系统生成规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 4 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\",\"当日接诊负荷最高医生为 王医生，预约 4 人次。\"],\"risks\":[{\"title\":\"病历留存偏低\",\"severity\":\"medium\",\"finding\":\"病历完成率仅 0.00%，存在医疗记录闭环不足风险。\",\"recommendation\":\"要求接诊结束当日补齐病历，设置护士/前台复核。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P1\",\"action\":\"围绕 王医生 的接诊高峰优化排班和椅位分配。\",\"owner\":\"门诊经理\",\"due\":\"7days\",\"expected_result\":\"缓解高峰拥堵，提高翻台效率。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。\"}','','OpenAI 未启用或未配置 API Key，已生成规则分析','2026-05-14 16:09:59','2026-05-14 16:10:00'),(7,'2026-05-15','FALLBACK','RULE_BASED','SCHEDULED','deepseek-v4-pro',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-15 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 本次因模型调用异常，已自动回退为规则分析。','{\"analysis_date\":\"2026-05-15\",\"total_patients\":10,\"today_appointments\":0,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":0,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":2600.0,\"current_month_net_income\":-2600.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-15 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 本次因模型调用异常，已自动回退为规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 0 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。 当前展示内容为规则分析，建议在配置 OpenAI Key 后启用深度分析。\"}','','OpenAI 分析失败，已回退规则分析：HTTP 404','2026-05-15 16:10:00','2026-05-15 16:10:02'),(8,'2026-05-16','FALLBACK','RULE_BASED','SCHEDULED','test-model',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-16 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 本次因模型调用异常，已自动回退为规则分析。','{\"analysis_date\":\"2026-05-16\",\"total_patients\":10,\"today_appointments\":0,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":0,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":2600.0,\"current_month_net_income\":-2600.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-16 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 本次因模型调用异常，已自动回退为规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 0 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。 当前展示内容为规则分析，建议在配置 OpenAI Key 后启用深度分析。\"}','','OpenAI 分析失败，已回退规则分析：HTTP 404 {\"code\":\"404\",\"msg\":\"请求路径不存在：/responses\",\"data\":null}','2026-05-16 16:48:39','2026-05-16 16:48:40'),(9,'2026-05-17','FALLBACK','RULE_BASED','SCHEDULED','test-model',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-17 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 本次因模型调用异常，已自动回退为规则分析。','{\"analysis_date\":\"2026-05-17\",\"total_patients\":10,\"today_appointments\":0,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":0,\"future_7_day_appointments\":0,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":2600.0,\"current_month_net_income\":-2600.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-17 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 0 人次。 本次因模型调用异常，已自动回退为规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 0 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 0 人次，需提前做好排班和回访准备。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。 当前展示内容为规则分析，建议在配置 OpenAI Key 后启用深度分析。\"}','','OpenAI 分析失败，已回退规则分析：HTTP 404 {\"code\":\"404\",\"msg\":\"请求路径不存在：/responses\",\"data\":null}','2026-05-17 21:08:45','2026-05-17 21:08:46'),(10,'2026-05-19','FALLBACK','RULE_BASED','SCHEDULED','test-model',55,'flat','当日经营总体平稳，需继续提升病历闭环与高价值项目转化','2026-05-19 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 1 人次。 本次因模型调用异常，已自动回退为规则分析。','{\"analysis_date\":\"2026-05-19\",\"total_patients\":10,\"today_appointments\":0,\"today_medical_records\":0,\"today_treatments\":0,\"today_unique_patients\":0,\"future_7_day_appointments\":1,\"appointment_status_breakdown\":{\"待治疗\":0,\"已预约\":0,\"待就诊\":0,\"已取消\":0,\"其他\":0},\"cancellation_rate\":0.0,\"today_income\":0.0,\"today_expense\":0.0,\"today_net_income\":0.0,\"today_operating_expense\":0.0,\"today_material_expense\":0.0,\"today_lab_expense\":0.0,\"today_other_expense\":0.0,\"today_treatment_revenue\":0.0,\"today_treatment_received_amount\":0.0,\"today_treatment_unreceived_amount\":0.0,\"completed_treatment_count\":0,\"avg_income_per_appointment\":0.0,\"record_completion_rate\":0.0,\"current_month_income\":0.0,\"current_month_expense\":2600.0,\"current_month_net_income\":-2600.0,\"current_month_operating_expense\":0.0,\"current_month_material_expense\":0.0,\"current_month_lab_expense\":0.0,\"current_month_other_expense\":0.0,\"previous_month_net_income\":0.0,\"month_net_change_rate\":0.0,\"top_doctors\":[],\"top_projects\":[],\"data_limitations\":[\"患者主数据缺少建档时间，无法准确统计当日新增患者\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"当日经营总体平稳，需继续提升病历闭环与高价值项目转化\",\"summary\":\"2026-05-19 共预约 0 人次，完成病历 0 份、治疗 0 例，收入 ¥0.00、支出 ¥0.00、净现金流 ¥0.00。预约取消率 0.00%，未来7日有效预约 1 人次。 本次因模型调用异常，已自动回退为规则分析。\",\"operating_score\":55,\"trend\":\"flat\",\"highlights\":[\"当日预约 0 人次，病历记录 0 份，治疗 0 例。\",\"当日收入 ¥0.00，支出 ¥0.00，净现金流 ¥0.00。\",\"未来7日未取消预约 1 人次，需提前做好排班和回访准备。\"],\"risks\":[{\"title\":\"数据覆盖有限\",\"severity\":\"low\",\"finding\":\"当前日报未纳入库存与新增患者建档数据，部分判断偏保守。\",\"recommendation\":\"后续补充库存预警和患者建档时间字段，提升经营判断完整性。\"},{\"title\":\"收费与治疗口径存在时间差\",\"severity\":\"low\",\"finding\":\"治疗执行日期与收费入账日期可能跨天，导致治疗金额和当日实收不完全一致。\",\"recommendation\":\"复盘日报时同步查看当日处置金额、已收费金额和未收费金额。\"}],\"opportunities\":[{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"},{\"title\":\"经营复盘空间明确\",\"impact\":\"low\",\"finding\":\"现有预约、治疗、收费数据已具备日级分析基础。\",\"recommendation\":\"持续沉淀日报，逐步形成周报和月报趋势管理。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"对明后两天预约患者做二次到诊确认，重点跟进高取消风险患者。\",\"owner\":\"前台/客服\",\"due\":\"today\",\"expected_result\":\"降低临时取消，稳定接诊节奏。\"},{\"priority\":\"P1\",\"action\":\"核对当日未完成病历与治疗收费记录，确保诊疗留痕和收费闭环。\",\"owner\":\"护士长\",\"due\":\"3days\",\"expected_result\":\"提升病历完整率，减少漏记漏收费。\"},{\"priority\":\"P2\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入经营日报。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目分析可读性和复盘准确性。\"}],\"management_brief\":\"门诊当日核心关注点应放在预约兑现、收费闭环和未来7日排班稳定性。 当前展示内容为规则分析，建议在配置 OpenAI Key 后启用深度分析。\"}','','OpenAI 分析失败，已回退规则分析：HTTP 404 {\"code\":\"404\",\"msg\":\"请求路径不存在：/responses\",\"data\":null}','2026-05-20 06:22:23','2026-05-20 06:22:25');
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
INSERT INTO `business_period_report` VALUES (1,'WEEKLY','2026-W20','2026-05-11','2026-05-17','FALLBACK','RULE_BASED','SCHEDULED','gpt',54,'down','周报经营承压，净收入 ¥-1300.00','2026-05-11 至 2026-05-17 共预约 6 人次，治疗 0 例，收入 ¥0.00，净收入 ¥-1300.00。 本次为规则分析结果。','{\"report_type\":\"WEEKLY\",\"report_type_label\":\"周报\",\"period_start\":\"2026-05-11\",\"period_end\":\"2026-05-17\",\"period_label\":\"2026-05-11 至 2026-05-17\",\"model_name\":\"gpt\",\"total_patients\":10,\"total_appointments\":6,\"total_treatments\":0,\"total_unique_patients\":2,\"completed_treatments\":0,\"total_income\":0.0,\"total_expense\":1300.0,\"total_operating_expense\":0.0,\"material_expense\":0.0,\"lab_expense\":0.0,\"other_expense\":0.0,\"net_income\":-1300.0,\"avg_daily_income\":0.0,\"avg_daily_appointments\":0.86,\"cancellation_rate\":0.0,\"previous_net_income\":-500.0,\"previous_appointments\":16,\"net_income_change_rate\":-160.0,\"appointment_change_rate\":-62.5,\"top_doctors\":[{\"doctor_name\":\"王医生\",\"appointment_count\":5,\"treatment_count\":0,\"treatment_revenue\":0.0},{\"doctor_name\":\"admin\",\"appointment_count\":1,\"treatment_count\":0,\"treatment_revenue\":0.0}],\"top_projects\":[],\"data_limitations\":[\"周期项目与医生维度金额按治疗记录标价汇总，不等同于周期内实收；实收请以总收入和净收入为准。\",\"经营支出已拆分为耗材采购、义齿加工和非耗材支出；治疗退款等其他现金流出仍计入总支出，但不计入经营支出三项汇总。\"]}','{\"headline\":\"周报经营承压，净收入 ¥-1300.00\",\"summary\":\"2026-05-11 至 2026-05-17 共预约 6 人次，治疗 0 例，收入 ¥0.00，净收入 ¥-1300.00。 本次为规则分析结果。\",\"operating_score\":54,\"trend\":\"down\",\"highlights\":[\"周报预约总量 6，日均 0.86。\",\"周报收入 ¥0.00，净收入 ¥-1300.00。\",\"对比上周期净收入变动 -160.00%。\"],\"risks\":[{\"title\":\"净收入下滑\",\"severity\":\"high\",\"finding\":\"净收入较上周期下降 160.00%。\",\"recommendation\":\"优先复盘客单价和项目结构变化。\"}],\"opportunities\":[{\"title\":\"管理节奏可前移\",\"impact\":\"medium\",\"finding\":\"周期报表已具备趋势管理基础。\",\"recommendation\":\"将周报用于周例会，将月报用于经营目标复盘。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"复盘本周期取消率和未完成治疗原因。\",\"owner\":\"店长\",\"due\":\"3days\",\"expected_result\":\"降低下周期无效预约和空档。\"},{\"priority\":\"P1\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入周月报分析。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目结构分析的可读性与可信度。\"},{\"priority\":\"P2\",\"action\":\"在周会或月会上固定复盘经营日报、周报、月报。\",\"owner\":\"管理层\",\"due\":\"14days\",\"expected_result\":\"形成稳定经营节奏。\"}],\"management_brief\":\"重点关注高价值项目延续性、医生负荷均衡和取消率控制，避免经营波动放大。\"}','{\"headline\":\"周报经营承压，净收入 ¥-1300.00\",\"summary\":\"2026-05-11 至 2026-05-17 共预约 6 人次，治疗 0 例，收入 ¥0.00，净收入 ¥-1300.00。 本次为规则分析结果。\",\"operating_score\":54,\"trend\":\"down\",\"highlights\":[\"周报预约总量 6，日均 0.86。\",\"周报收入 ¥0.00，净收入 ¥-1300.00。\",\"对比上周期净收入变动 -160.00%。\"],\"risks\":[{\"title\":\"净收入下滑\",\"severity\":\"high\",\"finding\":\"净收入较上周期下降 160.00%。\",\"recommendation\":\"优先复盘客单价和项目结构变化。\"}],\"opportunities\":[{\"title\":\"管理节奏可前移\",\"impact\":\"medium\",\"finding\":\"周期报表已具备趋势管理基础。\",\"recommendation\":\"将周报用于周例会，将月报用于经营目标复盘。\"}],\"actions\":[{\"priority\":\"P0\",\"action\":\"复盘本周期取消率和未完成治疗原因。\",\"owner\":\"店长\",\"due\":\"3days\",\"expected_result\":\"降低下周期无效预约和空档。\"},{\"priority\":\"P1\",\"action\":\"清洗处置项目名称和项目字典，避免异常名称进入周月报分析。\",\"owner\":\"信息管理员\",\"due\":\"7days\",\"expected_result\":\"提升项目结构分析的可读性与可信度。\"},{\"priority\":\"P2\",\"action\":\"在周会或月会上固定复盘经营日报、周报、月报。\",\"owner\":\"管理层\",\"due\":\"14days\",\"expected_result\":\"形成稳定经营节奏。\"}],\"management_brief\":\"重点关注高价值项目延续性、医生负荷均衡和取消率控制，避免经营波动放大。\"}','OpenAI 分析失败，已回退规则分析：HTTP 404 {\"code\":\"404\",\"msg\":\"请求路径不存在：/responses\",\"data\":null}','2026-05-18 01:17:35','2026-05-18 01:17:37');
/*!40000 ALTER TABLE `business_period_report` ENABLE KEYS */;
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
  PRIMARY KEY (`id`),
  KEY `idx_consent_template_status` (`status`),
  KEY `idx_consent_template_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知情同意书模板库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consent_template`
--

LOCK TABLES `consent_template` WRITE;
/*!40000 ALTER TABLE `consent_template` DISABLE KEYS */;
INSERT INTO `consent_template` VALUES (1,'根管治疗知情同意书','我已了解根管治疗的流程、风险及预后，同意接受治疗。可能出现术后疼痛、器械分离、根管侧穿等并发症。','',1,1,'2026-05-08 17:25:35','2026-05-08 17:25:35'),(2,'种植牙手术知情同意书','我已了解种植牙手术的流程、风险及预后，同意接受手术。可能出现术后肿胀、感染、种植体失败等情况。','',1,2,'2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,'拔牙手术知情同意书','我已了解拔牙手术的流程、风险及预后，同意接受手术。可能出现术后出血、感染、干槽症等情况。','',1,3,'2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,'正畸治疗知情同意书','我已了解正畸治疗的流程、风险及预后，同意接受治疗。可能出现牙齿松动、牙龈萎缩、牙根吸收等情况。','',1,4,'2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,'牙周治疗知情同意书','我已了解牙周治疗的流程、风险及预后，同意接受治疗。可能出现术后出血、牙齿敏感、牙龈退缩等情况。','',1,5,'2026-05-08 17:25:35','2026-05-08 17:25:35'),(6,'美白治疗知情同意书','我已了解美白治疗的流程、风险及预后，同意接受治疗。可能出现牙齿敏感、牙龈刺激、效果不理想等情况。','',1,6,'2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  KEY `idx_consultation_id` (`consultation_id`),
  CONSTRAINT `fk_followup_consultation` FOREIGN KEY (`consultation_id`) REFERENCES `consultation_records` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å’¨è¯¢è·Ÿè¿›è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_followups`
--

LOCK TABLES `consultation_followups` WRITE;
/*!40000 ALTER TABLE `consultation_followups` DISABLE KEYS */;
INSERT INTO `consultation_followups` VALUES (1,1,'2026-05-09 11:46:30','首次电话沟通，客户对种植牙价格比较关心，已发送报价单','2026-05-10 10:00:00',1,'管理员','2026-05-09 03:46:30','2026-05-09 03:46:30'),(2,5,'2026-05-09 11:51:13','微信跟进，发送报价单','2026-05-11 11:51:13',1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13'),(3,6,'2026-05-09 11:51:13','微信跟进，发送报价单','2026-05-11 11:51:13',1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13'),(4,7,'2026-05-09 11:51:13','首次电话沟通，了解客户需求','2026-05-11 11:51:13',1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13'),(5,9,'2026-05-09 11:51:13','微信跟进，发送报价单',NULL,1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13'),(6,11,'2026-05-09 11:51:13','首次电话沟通，了解客户需求',NULL,1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13'),(7,12,'2026-05-09 11:51:13','微信跟进，发送报价单','2026-05-11 11:51:13',1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13'),(8,13,'2026-05-09 11:51:13','首次电话沟通，了解客户需求','2026-05-11 11:51:13',1,'管理员','2026-05-09 03:51:13','2026-05-09 03:51:13');
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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='咨询记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation_records`
--

LOCK TABLES `consultation_records` WRITE;
/*!40000 ALTER TABLE `consultation_records` DISABLE KEYS */;
INSERT INTO `consultation_records` VALUES (1,NULL,'2026-05-09 10:00:00','微信',NULL,NULL,NULL,NULL,NULL,NULL,'种植','高','待跟进','测试客户','13800138001','测试备注',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:43:29','2026-05-09 03:43:29'),(2,NULL,'2026-05-09 11:00:00','微信',NULL,NULL,NULL,NULL,NULL,NULL,'正畸','中','待跟进','测试2','13800138002','测试',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:44:42','2026-05-09 03:44:42'),(3,NULL,'2026-05-09 12:00:00','微信',NULL,NULL,NULL,NULL,NULL,NULL,'种植','高','待跟进','测试客户3','13800138003','测试备注',12000.00,'价格敏感，对比竞品',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:46:15','2026-05-09 03:46:15'),(4,NULL,'2026-05-02 13:51:00','电话',NULL,NULL,NULL,NULL,NULL,NULL,'补牙','低','不再跟进','赵女士','13886558555','客户咨询拔牙，仍在犹豫',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(5,NULL,'2026-05-02 21:51:00','大众点评',NULL,NULL,NULL,NULL,NULL,NULL,'种植','中','已预约到店','张先生','13892952022','客户咨询修复，仍在犹豫',7468.75,NULL,NULL,NULL,'2026-05-09 11:51:13',NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(6,NULL,'2026-05-01 20:51:00','大众点评',NULL,NULL,NULL,NULL,NULL,NULL,'美白','中','不再跟进','孙先生','13815633723',NULL,26940.40,'需要家人同意',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(7,NULL,'2026-05-09 04:51:00','小红书',NULL,NULL,NULL,NULL,NULL,NULL,'美白','低','已预约到店','吴先生','13849918889',NULL,5571.17,'对比竞品',NULL,NULL,'2026-05-09 11:51:13',NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(8,NULL,'2026-05-02 00:51:00','微信',NULL,NULL,NULL,NULL,NULL,NULL,'修复','低','不再跟进','吴先生','13893330460','客户咨询儿童齿科，有明确意向',27323.43,'距离太远',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(9,NULL,'2026-05-09 07:51:00','电话',NULL,NULL,NULL,NULL,NULL,NULL,'补牙','高','待跟进','李先生','13893309447',NULL,25878.87,'需要家人同意',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(10,3,'2026-05-03 08:51:00','大众点评',NULL,NULL,NULL,NULL,NULL,NULL,'修复','高','已成交','孙先生','13818345212','客户咨询拔牙，仍在犹豫',10100.56,NULL,NULL,NULL,'2026-05-09 11:51:13','2026-05-09 11:51:13',1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(11,NULL,'2026-05-02 01:51:00','电话',NULL,NULL,NULL,NULL,NULL,NULL,'儿童齿科','低','不再跟进','赵女士','13876879179','客户咨询修复，有明确意向',26610.60,'距离太远',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(12,NULL,'2026-05-04 04:51:00','自然到店',NULL,NULL,NULL,NULL,NULL,NULL,'拔牙','高','不再跟进','王女士','13843273281',NULL,24426.51,'价格敏感',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(13,NULL,'2026-05-06 09:51:00','电话',NULL,NULL,NULL,NULL,NULL,NULL,'正畸','低','待跟进','张先生','13852125909','客户咨询种植，仍在犹豫',18272.41,'对比竞品',NULL,NULL,NULL,NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(14,NULL,'2026-05-08 18:51:00','大众点评',NULL,NULL,NULL,NULL,NULL,NULL,'儿童齿科','低','已预约到店','刘女士','13867628770','客户咨询美白，有明确意向',NULL,'价格敏感',NULL,NULL,'2026-05-09 11:51:13',NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(15,NULL,'2026-05-04 06:51:00','自然到店',NULL,NULL,NULL,NULL,NULL,NULL,'美白','低','已预约到店','吴先生','13873006171','客户咨询拔牙，仍在犹豫',4148.53,'担心疼痛',NULL,NULL,'2026-05-09 11:51:13',NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(16,NULL,'2026-05-01 12:51:00','小红书',NULL,NULL,NULL,NULL,NULL,NULL,'儿童齿科','中','已预约到店','孙先生','13868889106','客户咨询美白，仍在犹豫',15027.23,'距离太远',NULL,NULL,'2026-05-09 11:51:13',NULL,1,'管理员',1,'2026-05-09 03:51:13','2026-05-09 03:51:13'),(17,NULL,'2026-05-09 12:03:00','微信',NULL,NULL,NULL,NULL,NULL,NULL,'正畸','中','待跟进','张三','13800138001','123123',10.00,'21112',NULL,NULL,NULL,NULL,1,'管理员',NULL,'2026-05-09 04:04:04','2026-05-13 03:51:10'),(18,NULL,'2026-05-09 12:59:00','微信',NULL,NULL,NULL,NULL,NULL,NULL,'补牙','低','待跟进','张三','13800138001','收到阿斯顿阿萨',NULL,'收到阿斯顿',NULL,NULL,NULL,NULL,1,'管理员',NULL,'2026-05-09 05:00:24','2026-05-13 03:51:10');
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
  PRIMARY KEY (`id`),
  KEY `idx_doctor_name` (`doctor_name`),
  KEY `idx_schedule_date` (`schedule_date`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生排班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctors`
--

LOCK TABLES `doctors` WRITE;
/*!40000 ALTER TABLE `doctors` DISABLE KEYS */;
INSERT INTO `doctors` VALUES (1,'王医生','2026-05-08','08:00:00','12:00:00','working',NULL),(2,'王医生','2026-05-08','14:00:00','18:00:00','working',NULL),(3,'李医生','2026-05-08','09:00:00','12:00:00','working',NULL),(4,'李医生','2026-05-08','14:00:00','17:00:00','working',NULL);
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
  PRIMARY KEY (`id`),
  KEY `idx_file_attachment_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='é€šç”¨æ–‡ä»¶é™„ä»¶è¡¨';
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
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `finances`
--

LOCK TABLES `finances` WRITE;
/*!40000 ALTER TABLE `finances` DISABLE KEYS */;
INSERT INTO `finances` VALUES (7,8,NULL,NULL,NULL,'周九',200.00,'2026-05-07','income','treatment','复诊检查费'),(8,9,NULL,NULL,NULL,'吴十',300.00,'2026-05-08','income','treatment','种植牙复查费'),(10,3,NULL,NULL,NULL,'李四',800.00,'2026-05-02','expense','material','设备维护费'),(19,3,NULL,NULL,NULL,'李四',500.00,'2026-05-13','income','treatment','测试收费单'),(20,NULL,NULL,NULL,NULL,'测试',100.00,'2026-05-13','income',NULL,NULL),(22,NULL,NULL,NULL,NULL,'验证测试收费',350.00,'2026-05-13','income',NULL,'端到端验证备注'),(23,NULL,NULL,NULL,NULL,'验证测试收费',350.00,'2026-05-13','income',NULL,'端到端验证备注');
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医保平台配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insurance_config`
--

LOCK TABLES `insurance_config` WRITE;
/*!40000 ALTER TABLE `insurance_config` DISABLE KEYS */;
INSERT INTO `insurance_config` VALUES (1,'bp_picc','中国人民保险','https://api.picc.com.cn/dental','PICC-BJ-001','人保北京分公司','picc_app_001','secret_key_001','sign_key_001','RSA','110000',1,'{\"contractNo\":\"2026-BJ-001\",\"settleCycle\":\"monthly\"}','2026-05-08 17:25:35','2026-05-08 17:25:35'),(2,'bp_paic','中国平安保险','https://api.pingan.com/dental','PAIC-SH-001','平安上海分公司','paic_app_001','secret_key_002','sign_key_002','RSA','310000',1,'{\"contractNo\":\"2026-SH-001\",\"settleCycle\":\"monthly\"}','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,'bp_cpic','中国太平洋保险','https://api.cpic.com.cn/dental','CPIC-GZ-001','太保广州分公司','cpic_app_001','secret_key_003','sign_key_003','AES','440100',0,'{\"contractNo\":\"2026-GZ-001\",\"settleCycle\":\"quarterly\"}','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_category` (`category`),
  KEY `idx_supplier` (`supplier`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,'3M纳米树脂 Z350','充填材料','3M','上海口腔器材公司','4g/支','支',45,0,120.00,'2026-A001'),(2,'氢氧化钙根管消毒剂','根管材料','登士柏','北京牙科器材公司','2g/支','支',20,0,35.00,'2026-B002'),(3,'牙胶尖 02锥度','根管材料','登士柏','北京牙科器材公司','120支/盒','盒',12,0,80.00,'2026-B003'),(4,'种植体 4.0*10mm','种植耗材','奥齿泰','韩国奥齿泰中国总代','颗','颗',8,0,2500.00,'2026-C001'),(5,'正畸托槽 MBT','正畸耗材','奥美科','上海正畸器材公司','副','副',10,0,450.00,'2026-D001'),(6,'可吸收缝合线 4-0','手术耗材','强生','上海医疗器械公司','包','包',25,0,65.00,'2026-E001'),(7,'超声洁牙机工作尖 P10','牙周器械','赛特力','广州口腔器械公司','支','支',10,0,180.00,'2026-F001'),(8,'氧化锆瓷块 C2色','修复材料','威兰德','深圳牙科材料公司','块','块',6,0,680.00,'2026-G001');
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
  PRIMARY KEY (`id`),
  KEY `idx_lab_factories_name` (`name`),
  KEY `idx_lab_factories_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿加工厂档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_factories`
--

LOCK TABLES `lab_factories` WRITE;
/*!40000 ALTER TABLE `lab_factories` DISABLE KEYS */;
INSERT INTO `lab_factories` VALUES (1,'精工义齿加工厂','张工','13800138001','北京市朝阳区牙科技工园区A座','2025-01-15','active','2026-05-08 17:25:35','2026-05-08 17:25:35'),(2,'美齿数字化中心','李经理','13900139002','上海市浦东新区口腔产业园B区','2025-03-20','active','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,'华南义齿制作中心','王师傅','13700137003','广州市白云区牙科工业区C栋','2025-06-10','active','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,'瑞尔数字义齿','陈总监','13600136004','深圳市南山区科技园D座','2025-08-05','active','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,'东北口腔技工所','刘主任','13500135005','沈阳市铁西区口腔产业园E区','2025-10-12','active','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='义齿加工订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_orders`
--

LOCK TABLES `lab_orders` WRITE;
/*!40000 ALTER TABLE `lab_orders` DISABLE KEYS */;
INSERT INTO `lab_orders` VALUES (2,2,'美齿数字化中心',3,'李四',NULL,NULL,2,2,'瓷贴面',NULL,'贴面修复','11,21','超薄瓷贴面','A2色',1800.00,2,3600.00,'2026-05-03','2026-05-10',NULL,'in_progress','前牙美学贴面',1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,3,'华南义齿制作中心',4,'王五',NULL,NULL,NULL,3,'局部活动义齿',NULL,'活动义齿修复','46,47','钴铬支架义齿','标准',1500.00,1,1500.00,'2026-05-05','2026-05-12',NULL,'pending','下颌局部义齿',1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,1,'精工义齿加工厂',5,'赵六',NULL,NULL,4,1,'二氧化锆全瓷冠',NULL,'全冠修复','16','氧化锆全瓷冠','C3色',1200.00,1,1200.00,'2026-05-06','2026-05-13',NULL,'completed','后牙全瓷冠修复已完成',1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,4,'瑞尔数字义齿',6,'陈七',NULL,NULL,5,4,'即刻种植临时冠',NULL,'即刻种植','36','临时树脂冠','标准',300.00,1,300.00,'2026-05-07','2026-05-09',NULL,'in_progress','即刻种植临时修复',1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  KEY `idx_login_log_user` (`user_id`),
  KEY `idx_login_log_status` (`login_status`),
  KEY `idx_login_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç”¨æˆ·ç™»å½•æ—¥å¿—è¡¨';
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
INSERT INTO `material_categories` VALUES (1,'种植类',0,10,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(2,'正畸类',0,20,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(3,'修复类',0,30,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(4,'基础耗材',0,40,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19'),(5,'其他',0,50,'启用','2026-05-06 19:35:19','2026-05-06 19:35:19');
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
  PRIMARY KEY (`id`),
  KEY `idx_materials_category_id` (`category_id`),
  KEY `idx_materials_status` (`status`),
  KEY `idx_materials_name` (`name`),
  KEY `idx_materials_brand` (`brand`),
  KEY `idx_materials_category_status_name` (`category_id`,`status`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='耗材档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `materials`
--

LOCK TABLES `materials` WRITE;
/*!40000 ALTER TABLE `materials` DISABLE KEYS */;
INSERT INTO `materials` VALUES (1,'3M纳米树脂','Z350 XT','3M',1,'充填材料','支',10,45,'active','美国进口纳米树脂','2026-05-08 17:25:35','2026-05-08 17:25:35'),(2,'氢氧化钙根管消毒剂','2g/支','登士柏',2,'根管材料','支',5,20,'active','根管消毒封药','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,'牙胶尖','02锥度','登士柏',2,'根管材料','盒',3,12,'active','根管充填材料','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,'种植体（韩国）','4.0*10mm','奥齿泰',3,'种植耗材','颗',2,8,'active','韩国进口种植体','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,'种植体（瑞士）','4.3*10mm','士卓曼',3,'种植耗材','颗',2,5,'active','瑞士ITI种植体','2026-05-08 17:25:35','2026-05-08 17:25:35'),(6,'正畸托槽','MBT','奥美科',4,'正畸耗材','副',3,10,'active','金属自锁托槽','2026-05-08 17:25:35','2026-05-08 17:25:35'),(7,'隐形矫治器膜片','0.75mm','时代天使',4,'正畸耗材','张',20,80,'active','隐形矫正膜片','2026-05-08 17:25:35','2026-05-08 17:25:35'),(8,'可吸收缝合线','4-0','强生',5,'手术耗材','包',5,25,'active','薇乔可吸收线','2026-05-08 17:25:35','2026-05-08 17:25:35'),(9,'牙周探针','WHO标准','豪孚迪',6,'牙周器械','支',2,6,'active','牙周袋深度测量','2026-05-08 17:25:35','2026-05-08 17:25:35'),(10,'超声洁牙机工作尖','P10','赛特力',6,'牙周器械','支',3,10,'active','超声波洁治工作尖','2026-05-08 17:25:35','2026-05-08 17:25:35'),(11,'氧化锆瓷块','C2色','威兰德',7,'修复材料','块',2,6,'active','CAD/CAM全瓷冠材料','2026-05-08 17:25:35','2026-05-08 17:25:35'),(12,'临时冠材料','50ml','义获嘉',7,'修复材料','瓶',3,12,'active','临时冠桥树脂','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_field_key` (`field_key`)
) ENGINE=InnoDB AUTO_INCREMENT=394 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历扩写字段规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_record_ai_field`
--

LOCK TABLES `medical_record_ai_field` WRITE;
/*!40000 ALTER TABLE `medical_record_ai_field` DISABLE KEYS */;
INSERT INTO `medical_record_ai_field` VALUES (12,'chiefComplaint','主诉',1,30,1,'','主诉必须包含部位+症状+时间',NULL,''),(13,'historyOfPresentIllness','现病史',1,500,1,'','现病史必须包含时间描述',NULL,''),(14,'pastHistory','既往史',1,300,0,'','',NULL,'否认全身系统性疾病史，否认药物过敏史'),(15,'generalCondition','一般情况',1,100,0,'','',NULL,'精神可，饮食睡眠尚可，大小便正常'),(16,'examinationFindings','检查所见',1,500,0,'','',NULL,''),(17,'auxiliaryExamination','辅助检查',1,300,0,'','',NULL,'暂缺，建议完善'),(18,'diagnosis','诊断',1,100,1,'','诊断必须用建议性语气',NULL,''),(19,'treatmentPlan','治疗计划',1,300,0,'','',NULL,''),(20,'treatment','治疗文稿',1,500,0,'','',NULL,''),(21,'medicalAdvice','医嘱',1,300,0,'','',NULL,''),(22,'prescription','处方',1,200,0,'','',NULL,'暂无'),(23,'notes','病历备注',1,500,0,'','',NULL,''),(393,'chief_complaint','主诉',1,500,1,NULL,NULL,1,NULL);
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
  PRIMARY KEY (`id`),
  KEY `idx_field_type_status` (`field_type`,`status`),
  KEY `idx_category` (`category`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç—…åŽ†å¸¸ç”¨è¯æ¡';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_record_phrases`
--

LOCK TABLES `medical_record_phrases` WRITE;
/*!40000 ALTER TABLE `medical_record_phrases` DISABLE KEYS */;
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
INSERT INTO `medical_record_template` VALUES (1,'根管治疗初诊模板','口腔内科','右下后牙疼痛2周','患者自诉右下后牙冷热刺激痛，夜间加重','否认高血压、糖尿病等系统性病史',NULL,NULL,NULL,'36叩痛(+)，冷热测试敏感，牙髓活力电测迟钝',NULL,'36慢性牙髓炎','根管治疗+全冠修复','开髓引流，根管预备，氢氧化钙封药',NULL,'避免患侧咀嚼，按时复诊',NULL,NULL,NULL,NULL,'初诊',NULL,1,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(2,'种植咨询模板','种植科','缺失牙半年，要求种植修复','46缺失半年，邻牙无明显倾斜','无特殊既往史',NULL,NULL,NULL,'缺牙区牙槽嵴高度充足，CBCT示骨宽度6mm',NULL,'46缺失','种植修复（植体+基台+冠）','一期手术植入种植体',NULL,'术后口服抗生素3天，2周拆线',NULL,NULL,NULL,NULL,'初诊',NULL,1,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,'隐形矫正初诊模板','正畸科','牙齿不齐，要求隐形矫正','牙齿拥挤不齐，影响美观','否认拔牙史',NULL,NULL,NULL,'全口牙列拥挤，磨牙关系中性偏远中',NULL,'安氏I类错合畸形','隐形矫正，预计疗程18个月','取模、拍片、制定矫正方案',NULL,'保持口腔卫生，定期复诊',NULL,NULL,NULL,NULL,'初诊',NULL,1,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,'牙周基础治疗模板','牙周科','牙龈出血3月','牙龈出血3月，伴口臭','无特殊既往史',NULL,NULL,NULL,'牙龈红肿，PD 4-6mm，BOP(+)',NULL,'中度牙周炎','牙周基础治疗（洁治+刮治）','全口超声波洁治，龈下刮治',NULL,'使用软毛牙刷及牙线，1月后复查',NULL,NULL,NULL,NULL,'初诊',NULL,1,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,'拔牙术后模板','口腔外科','智齿发炎，要求拔除','右下智齿反复发炎1月','无特殊既往史',NULL,NULL,NULL,'右下8近中阻生，局部充血',NULL,'右下8阻生齿','局麻下拔除右下8','拔除右下8，缝合止血',NULL,'术后24小时内勿漱口，避免过热食物',NULL,NULL,NULL,NULL,'复诊',NULL,1,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35'),(6,'全瓷冠修复模板','修复科','前牙缺损，要求修复','11外伤缺损1周','无特殊既往史',NULL,NULL,NULL,'11缺损位于切1/3，未累及牙髓',NULL,'11牙体缺损','二氧化锆全瓷冠修复','牙体预备，取模，临时冠修复',NULL,'避免咬硬物，2周后戴永久冠',NULL,NULL,NULL,NULL,'初诊',NULL,1,1,'管理员','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_visit_date` (`visit_date`),
  KEY `idx_medical_records_doctor_account_id` (`doctor_account_id`),
  KEY `idx_medical_records_patient_visit` (`patient_id`,`visit_date`),
  CONSTRAINT `fk_medical_records_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_records`
--

LOCK TABLES `medical_records` WRITE;
/*!40000 ALTER TABLE `medical_records` DISABLE KEYS */;
INSERT INTO `medical_records` VALUES (2,3,'李四',1,'王医生',NULL,NULL,'2026-05-06 14:58:13','初诊','牙齿不齐要求矫正','自觉前牙突，影响美观，无疼痛','体健',NULL,NULL,NULL,'上颌前突，牙列拥挤II度','头影测量分析','安氏I类错颌畸形','固定矫治器矫正','拔除14、24、34、44','14,24,34,44','保持口腔卫生，使用正畸专用牙刷','',NULL,NULL,'患者为舞蹈演员，对美观要求高，要求隐形矫治；需详细沟通方案后再决定托槽类型。','final',0,'2026-05-08 14:58:13','2026-05-08 14:58:13'),(4,5,'钱六',1,'王医生',NULL,NULL,'2026-05-07 14:58:13','初诊','要求洗牙','自觉牙石多，牙龈出血','体健',NULL,NULL,NULL,'全口牙石II度，牙龈红肿','血常规正常','慢性牙龈炎','全口洁治','超声洁牙，抛光，口腔卫生指导','','使用牙线，定期复查','',NULL,NULL,'患者孕期4个月，洁治时避免使用肾上腺素；操作轻柔，减少刺激；建议孕中期完成治疗。','final',0,'2026-05-08 14:58:13','2026-05-08 14:58:13'),(5,6,'孙七',2,'李医生',NULL,NULL,'2026-05-01 14:58:13','初诊','牙齿缺失要求修复','右上后牙缺失半年，影响咀嚼','体健',NULL,NULL,NULL,'16缺失，牙槽嵴中度吸收','CBCT评估骨量','牙列缺损','种植修复或活动义齿修复','患者选择种植修复方案，待骨增量术后种植','16','保持口腔卫生','',NULL,NULL,'患者糖尿病史，空腹血糖8.5，建议内分泌科调整血糖至7以下再考虑手术；需评估全身状况。','final',0,'2026-05-08 14:58:13','2026-05-08 14:58:13'),(6,7,'周八',1,'王医生',NULL,NULL,'2026-05-05 14:58:13','复诊','根管治疗后复查','根管治疗后无疼痛','体健',NULL,NULL,NULL,'36根充完善，暂封材料在位','X光片显示根充恰填','根管治疗后','全冠修复','牙体预备，取模，临时冠修复','36','避免咬硬物，注意临时冠脱落','',NULL,NULL,'患者咬合紧，建议全锆冠而非烤瓷冠；对金属过敏，需使用全瓷材料；加工厂需特别注意。','final',0,'2026-05-08 14:58:13','2026-05-08 14:58:13'),(7,8,'吴九',2,'李医生',NULL,NULL,'2026-04-28 14:58:13','初诊','智齿发炎','右下智齿反复肿痛2周','体健',NULL,NULL,NULL,'48垂直阻生，龈瓣覆盖，探诊出血','全景片显示48近中阻生','急性智齿冠周炎','消炎后拔除48','局部冲洗，碘甘油上药，口服抗生素','48','淡盐水漱口，避免辛辣刺激','甲硝唑0.2g tid，罗红霉素0.15g bid',NULL,NULL,'患者即将出国留学，要求尽快拔除；时间安排紧张，建议一周后复诊评估；需提前告知术后恢复时间。','final',0,'2026-05-08 14:58:13','2026-05-08 14:58:13'),(8,9,'郑十',1,'王医生',NULL,NULL,'2026-05-04 14:58:13','初诊','牙齿敏感','冷热酸甜刺激痛，尤其左上后牙明显','体健',NULL,NULL,NULL,'26、27牙颈部楔状缺损','牙髓活力测试正常','牙本质过敏症','脱敏治疗+充填修复','35%氟化氨银脱敏，玻璃离子充填','26,27','使用抗敏感牙膏，避免横刷','',NULL,NULL,'患者刷牙方式错误，水平横刷导致楔状缺损；已进行口腔卫生宣教，建议使用巴氏刷牙法；需定期复查。','final',0,'2026-05-08 14:58:13','2026-05-08 14:58:13'),(11,5,'赵六',2,'王医生','','','2026-05-13 10:00:00','初诊','验证测试：牙痛','验证测试现病史','','','','','','','验证测试诊断','验证测试治疗计划','','','','','','','','final',0,'2026-05-13 03:54:08','2026-05-13 03:54:08'),(12,13,'链路测试患者',2,'王医生','','','2026-05-13 00:00:00','初诊','牙齿疼痛','左侧磨牙疼痛3天','','','','','','','龋齿','根管治疗','','','','','','','链路测试病历','final',0,'2026-05-13 11:06:32','2026-05-13 11:06:32');
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
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operator` (`operator_id`),
  KEY `idx_operation_log_type` (`operation_type`),
  KEY `idx_operation_log_target` (`target_type`,`target_id`),
  KEY `idx_operation_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å…¨å±€æ“ä½œå®¡è®¡æ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
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
  PRIMARY KEY (`id`),
  KEY `idx_patient_consent_patient_id` (`patient_id`),
  KEY `idx_patient_consent_status` (`status`),
  KEY `idx_patient_consent_doctor_account_id` (`doctor_account_id`),
  KEY `idx_patient_consent_signed_at` (`signed_at`),
  CONSTRAINT `fk_patient_consent_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者电子知情同意书';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_consent`
--

LOCK TABLES `patient_consent` WRITE;
/*!40000 ALTER TABLE `patient_consent` DISABLE KEYS */;
INSERT INTO `patient_consent` VALUES (1,9,'吴十',NULL,'管理员','种植牙手术知情同意书','我已了解种植牙手术的流程、风险及预后，同意接受手术。可能出现术后肿胀、感染、种植体失败等情况。','待签署','2026-05-09 09:14:32',NULL,NULL,'','','','2026-05-09 01:14:32','2026-05-09 01:14:32');
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
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_followup_date` (`followup_date`),
  KEY `idx_next_followup_date` (`next_followup_date`),
  KEY `idx_followup_doctor_account_id` (`doctor_account_id`),
  CONSTRAINT `fk_patient_followup_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者随访记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_followup`
--

LOCK TABLES `patient_followup` WRITE;
/*!40000 ALTER TABLE `patient_followup` DISABLE KEYS */;
INSERT INTO `patient_followup` VALUES (2,3,2,'孔凡瑞','2026-05-02 14:30:00','微信随访',NULL,'患者根管治疗后无不适，预约下周复诊','manual','2026-05-09 14:30:00','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,4,3,'李医生','2026-05-03 09:00:00','到院复查',NULL,'种植体愈合良好，无松动，牙龈健康','manual','2026-06-03 09:00:00','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,5,2,'孔凡瑞','2026-05-04 11:00:00','电话随访',NULL,'矫正器佩戴正常，牙齿移动顺利','manual','2026-06-04 11:00:00','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,6,3,'李医生','2026-05-05 16:00:00','微信随访',NULL,'牙周治疗后出血减少，口腔卫生改善','manual','2026-05-20 16:00:00','2026-05-08 17:25:35','2026-05-08 17:25:35'),(6,7,2,'孔凡瑞','2026-05-08 10:00:00','到院复查',NULL,'全冠修复后咬合正常，患者满意','manual','2026-11-08 10:00:00','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_patient_images_sent` (`patient_id`,`sent_to_patient`),
  CONSTRAINT `fk_patient_images_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者影像记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_images`
--

LOCK TABLES `patient_images` WRITE;
/*!40000 ALTER TABLE `patient_images` DISABLE KEYS */;
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
INSERT INTO `patient_insight_summary` VALUES (3,'2026-05-09 11:51:13',2,12300.00,'2026-05-09',2,1,0,0,0.00,0,'2026-05-09 03:51:13'),(4,NULL,0,0.00,NULL,0,0,0,0,0.00,0,'2026-05-13 03:53:52'),(5,'2026-05-13 10:00:00',2,0.00,NULL,2,0,0,0,0.00,0,'2026-05-13 03:54:08'),(6,'2026-05-01 14:58:13',1,0.00,NULL,1,0,0,0,0.00,0,'2026-05-08 18:14:59'),(7,'2026-05-05 14:58:13',1,0.00,NULL,1,0,0,0,0.00,0,'2026-05-08 18:14:59'),(8,'2026-04-28 14:58:13',1,0.00,NULL,1,0,0,0,0.00,0,'2026-05-08 18:14:59'),(9,'2026-05-04 14:58:13',1,0.00,NULL,1,0,0,0,0.00,0,'2026-05-09 00:35:58'),(11,NULL,0,0.00,NULL,0,0,0,0,0.00,0,'2026-05-13 03:56:37'),(12,NULL,0,0.00,NULL,0,0,0,0,0.00,0,'2026-05-13 03:57:16'),(13,'2026-05-13 00:00:00',1,0.00,NULL,1,0,0,0,0.00,0,'2026-05-13 15:35:39');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_risk_patient_tag` (`patient_id`,`tag_code`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_risk_status` (`status`),
  CONSTRAINT `fk_patient_risk_tag_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者风险标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_risk_tag`
--

LOCK TABLES `patient_risk_tag` WRITE;
/*!40000 ALTER TABLE `patient_risk_tag` DISABLE KEYS */;
INSERT INTO `patient_risk_tag` VALUES (2,3,'diabetes','糖尿病',2,'病历记录',1,'糖尿病患者，伤口愈合较慢','2026-05-08 17:25:35','2026-05-08 17:25:35'),(3,4,'drug_allergy','药物过敏',3,'病历记录',1,'青霉素过敏史','2026-05-08 17:25:35','2026-05-08 17:25:35'),(4,5,'pregnancy','妊娠期',2,'患者自述',1,'怀孕5个月，避免X光检查','2026-05-08 17:25:35','2026-05-08 17:25:35'),(5,6,'smoking','吸烟史',1,'病历记录',1,'每日吸烟1包，影响牙周健康','2026-05-08 17:25:35','2026-05-08 17:25:35'),(6,7,'delayed_healing','愈合延迟',2,'随访记录',1,'术后愈合较慢，需加强随访','2026-05-08 17:25:35','2026-05-08 17:25:35'),(7,8,'high_risk_caries','高龋风险',2,'检查记录',1,'多颗龋齿，需加强口腔卫生指导','2026-05-08 17:25:35','2026-05-08 17:25:35');
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
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_event_time` (`event_time`),
  CONSTRAINT `fk_patient_timeline_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者时间线';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_timeline`
--

LOCK TABLES `patient_timeline` WRITE;
/*!40000 ALTER TABLE `patient_timeline` DISABLE KEYS */;
INSERT INTO `patient_timeline` VALUES (1,9,'2026-05-09 09:14:32','知情同意','下发电子知情同意书','已向患者下发《种植牙手术知情同意书》，医生：管理员','patient_consent',1,'2026-05-09 01:14:32','2026-05-09 01:14:32');
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
INSERT INTO `patient_wechat_bind_scene` VALUES (1,9,'patient_bind_9',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 03:14:45','2026-05-08 03:14:45'),(2,6,'patient_bind_6',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 03:35:23','2026-05-08 03:35:23'),(4,7,'patient_bind_7',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 03:37:48','2026-05-08 03:37:48'),(5,8,'patient_bind_8',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 04:58:16','2026-05-08 04:58:16'),(6,4,'patient_bind_4',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 15:12:41','2026-05-08 15:12:41'),(7,3,'patient_bind_3',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 16:41:50','2026-05-08 16:41:50'),(8,5,'patient_bind_5',NULL,NULL,NULL,'pending',NULL,NULL,'2026-05-08 18:09:50','2026-05-08 18:09:50');
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (3,'李四','lisi','ls','女',28,NULL,'13800138002',NULL,NULL,NULL,NULL,NULL,NULL,'转介绍','2026-05-07 14:46:16','2026-05-13 19:16:23'),(4,'王五','wangwu','ww','男',42,NULL,'13800138003',NULL,NULL,NULL,NULL,NULL,NULL,'美团','2026-05-07 14:46:16','2026-05-13 19:16:23'),(5,'赵六','zhaoliu','zl','女',31,NULL,'13800138004',NULL,NULL,NULL,NULL,NULL,NULL,'自然到店','2026-05-07 14:46:16','2026-05-13 19:16:23'),(6,'陈七','chenqi','cq','男',55,NULL,'13800138005',NULL,NULL,NULL,NULL,NULL,NULL,'老患者','2026-05-07 14:46:16','2026-05-13 19:16:23'),(7,'刘八','liuba','lb','女',26,NULL,'13800138006',NULL,NULL,NULL,NULL,NULL,NULL,'百度','2026-05-07 14:46:16','2026-05-13 19:16:23'),(8,'周九','zhoujiu','zj','男',38,NULL,'13800138007',NULL,NULL,NULL,NULL,NULL,NULL,'自然到店','2026-05-07 14:46:16','2026-05-13 19:16:23'),(9,'吴十','wushi','ws','女',45,NULL,'13800138008',NULL,NULL,NULL,NULL,NULL,NULL,'转介绍','2026-05-07 14:46:16','2026-05-13 19:16:23'),(11,'测试患者A','ceshihuanzhea','cshza','男',30,NULL,'13900001111',NULL,NULL,NULL,NULL,NULL,NULL,'自然到店','2026-05-13 03:56:37','2026-05-13 03:56:37'),(12,'测试患者B','ceshihuanzheb','cshzb','女',25,NULL,'13900001111',NULL,NULL,NULL,NULL,NULL,NULL,'自然到店','2026-05-13 03:57:16','2026-05-13 03:57:16'),(13,'链路测试患者','lianluceshihuanzhe','llcshz','男',30,NULL,'13800138001',NULL,'测试地址',NULL,NULL,NULL,NULL,'自然到店','2026-05-13 11:02:03','2026-05-13 11:02:03');
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收款渠道表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_channel`
--

LOCK TABLES `payment_channel` WRITE;
/*!40000 ALTER TABLE `payment_channel` DISABLE KEYS */;
INSERT INTO `payment_channel` VALUES (1,'现金',1,10,'2026-05-06 19:35:19','2026-05-06 19:35:19'),(2,'微信',1,20,'2026-05-06 19:35:19','2026-05-06 19:35:19'),(3,'支付宝',1,30,'2026-05-06 19:35:19','2026-05-06 19:35:19'),(4,'银行卡',1,40,'2026-05-06 19:35:19','2026-05-06 19:35:19');
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
INSERT INTO `purchases` VALUES (1,'3M纳米树脂 Z350 XT','充填材料','3M','上海口腔器材公司','4g/支','支',50,120.00,'completed','2026-04-01 00:00:00','2026-04-05 00:00:00','2026-04-06 00:00:00'),(2,'登士柏氢氧化钙','根管材料','登士柏','北京牙科器材公司','2g/支','支',30,35.00,'completed','2026-04-02 00:00:00','2026-04-06 00:00:00','2026-04-07 00:00:00'),(3,'奥齿泰种植体','种植耗材','奥齿泰','韩国奥齿泰中国总代','4.0*10mm','颗',10,2500.00,'completed','2026-04-03 00:00:00','2026-04-08 00:00:00','2026-04-09 00:00:00'),(4,'强生可吸收缝合线','手术耗材','强生','上海医疗器械公司','4-0','包',20,65.00,'pending','2026-05-01 00:00:00',NULL,NULL),(5,'威兰德氧化锆瓷块','修复材料','威兰德','深圳牙科材料公司','C2色','块',5,680.00,'pending','2026-05-02 00:00:00',NULL,NULL),(6,'时代天使隐形膜片','正畸耗材','时代天使','北京时代天使公司','0.75mm','张',100,45.00,'completed','2026-04-10 00:00:00','2026-04-12 00:00:00','2026-04-13 00:00:00');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色导航权限配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_menu_permissions`
--

LOCK TABLES `role_menu_permissions` WRITE;
/*!40000 ALTER TABLE `role_menu_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `role_menu_permissions` ENABLE KEYS */;
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
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排班模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shift_template`
--

LOCK TABLES `shift_template` WRITE;
/*!40000 ALTER TABLE `shift_template` DISABLE KEYS */;
INSERT INTO `shift_template` VALUES (1,'标准早班周',NULL,'{\"1\":\"morning\",\"2\":\"morning\",\"3\":\"morning\",\"4\":\"morning\",\"5\":\"morning\",\"6\":\"rest\",\"7\":\"rest\"}','2026-05-11 05:03:52'),(2,'早晚轮班周',NULL,'{\"1\":\"morning\",\"2\":\"evening\",\"3\":\"morning\",\"4\":\"evening\",\"5\":\"morning\",\"6\":\"rest\",\"7\":\"rest\"}','2026-05-11 05:03:52'),(3,'全晚班周',NULL,'{\"1\":\"evening\",\"2\":\"evening\",\"3\":\"evening\",\"4\":\"evening\",\"5\":\"evening\",\"6\":\"rest\",\"7\":\"rest\"}','2026-05-11 05:03:52'),(4,'做二休一',NULL,'{\"1\":\"morning\",\"2\":\"morning\",\"3\":\"rest\",\"4\":\"evening\",\"5\":\"evening\",\"6\":\"rest\",\"7\":\"morning\"}','2026-05-11 05:03:52'),(5,'周末值班',NULL,'{\"1\":\"rest\",\"2\":\"morning\",\"3\":\"morning\",\"4\":\"morning\",\"5\":\"morning\",\"6\":\"morning\",\"7\":\"evening\"}','2026-05-11 05:03:52');
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment`
--

LOCK TABLES `treatment` WRITE;
/*!40000 ALTER TABLE `treatment` DISABLE KEYS */;
INSERT INTO `treatment` VALUES (9,3,'李四','TB7C6C8E4F67264DD4',2,NULL,17,'瑞士种植体（单颗）','进行中',2,'王医生','2026-05-09','','23','',12000.00,'2026-05-08 18:21:54','2026-05-08 18:21:54'),(10,3,'李四','TB7C6C8E4F67264DD4',2,NULL,23,'喷砂洁牙','进行中',3,'李医生','2026-05-09','','23','',300.00,'2026-05-08 18:21:54','2026-05-08 18:21:54');
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='处置收费项目库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_catalog`
--

LOCK TABLES `treatment_catalog` WRITE;
/*!40000 ALTER TABLE `treatment_catalog` DISABLE KEYS */;
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_project_categories_parent_name` (`parent_id`,`name`),
  KEY `idx_treatment_project_categories_parent_id` (`parent_id`),
  KEY `idx_treatment_project_categories_status` (`status`),
  KEY `idx_treatment_project_categories_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_project_categories`
--

LOCK TABLES `treatment_project_categories` WRITE;
/*!40000 ALTER TABLE `treatment_project_categories` DISABLE KEYS */;
INSERT INTO `treatment_project_categories` VALUES (1,'口腔内科',0,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(2,'口腔外科',0,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(3,'修复科',0,3,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(4,'种植科',0,4,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(5,'正畸科',0,5,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(6,'牙周科',0,6,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(7,'根管治疗',1,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(8,'龋齿充填',1,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(9,'拔牙',2,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(10,'阻生齿拔除',2,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(11,'全冠修复',3,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(12,'贴面修复',3,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(13,'活动义齿',3,3,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(14,'单颗种植',4,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(15,'多颗种植',4,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(16,'隐形矫正',5,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(17,'固定矫正',5,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(18,'洁治',6,1,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(19,'刮治',6,2,'启用',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_treatment_projects_code` (`project_code`),
  KEY `idx_treatment_projects_legacy_catalog_id` (`legacy_treatment_catalog_id`),
  KEY `idx_treatment_projects_category_id` (`category_id`),
  KEY `idx_treatment_projects_status` (`status`),
  KEY `idx_treatment_projects_sort_order` (`sort_order`),
  KEY `idx_treatment_projects_name` (`project_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗项目库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_projects`
--

LOCK TABLES `treatment_projects` WRITE;
/*!40000 ALTER TABLE `treatment_projects` DISABLE KEYS */;
INSERT INTO `treatment_projects` VALUES (1,NULL,'RCT001','前牙根管治疗',7,NULL,800.00,3,14,'在用',0,'包含根管预备、消毒、充填，不含全冠修复',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(2,NULL,'RCT002','后牙根管治疗',7,NULL,1200.00,3,21,'在用',0,'包含根管预备、消毒、充填，不含全冠修复',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(3,NULL,'RCT003','显微根管再治疗',7,NULL,2000.00,4,30,'在用',0,'显微镜辅助下根管再治疗',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(4,NULL,'FILL001','3M树脂充填（单面）',8,NULL,300.00,1,0,'在用',0,'美国3M纳米树脂材料',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(5,NULL,'FILL002','3M树脂充填（双面）',8,NULL,500.00,1,0,'在用',0,'美国3M纳米树脂材料',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(6,NULL,'FILL003','嵌体修复',8,NULL,1500.00,2,7,'在用',0,'CAD/CAM瓷嵌体',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(7,NULL,'EXT001','普通拔牙',9,NULL,100.00,1,0,'在用',0,'松动牙、乳牙拔除',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(8,NULL,'EXT002','残根拔除',9,NULL,200.00,1,0,'在用',0,'残根残冠拔除',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(9,NULL,'IMP001','阻生齿拔除（简单）',10,NULL,600.00,1,0,'在用',0,'垂直位或近中位阻生',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(10,NULL,'IMP002','阻生齿拔除（复杂）',10,NULL,1200.00,1,0,'在用',0,'水平位或倒置位阻生',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(11,NULL,'CROWN001','钴铬合金烤瓷冠',11,NULL,1500.00,3,14,'在用',0,'钴铬合金基底，瓷粉饰面',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(12,NULL,'CROWN002','二氧化锆全瓷冠',11,NULL,3500.00,3,14,'在用',0,'进口二氧化锆材料',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(13,NULL,'VENEER001','瓷贴面（单颗）',12,NULL,2800.00,3,14,'在用',0,'超薄瓷贴面，微创美学修复',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(14,NULL,'DENTURE001','局部活动义齿',13,NULL,2500.00,4,30,'在用',0,'钴铬合金支架+树脂牙',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(15,NULL,'DENTURE002','全口活动义齿',13,NULL,5000.00,5,45,'在用',0,'树脂基托+树脂牙',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(16,NULL,'IMPLANT001','韩国种植体（单颗）',14,NULL,6000.00,5,90,'在用',0,'含种植体+基台+全瓷冠',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(17,NULL,'IMPLANT002','瑞士种植体（单颗）',14,NULL,12000.00,5,90,'在用',0,'含种植体+基台+全瓷冠',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(18,NULL,'IMPLANT003','即刻种植',15,NULL,15000.00,4,60,'在用',0,'拔牙后即刻植入种植体',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(19,NULL,'ORTH001','隐形矫正（简单）',16,NULL,25000.00,10,365,'在用',0,'国产隐形矫治器',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(20,NULL,'ORTH002','隐形矫正（复杂）',16,NULL,40000.00,15,545,'在用',0,'进口隐形矫治器',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(21,NULL,'ORTH003','金属托槽矫正',17,NULL,15000.00,15,545,'在用',0,'传统金属托槽固定矫正',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(22,NULL,'PERIO001','超声波洁治',18,NULL,200.00,1,0,'在用',0,'全口超声波洁治',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(23,NULL,'PERIO002','喷砂洁牙',18,NULL,300.00,1,0,'在用',0,'全口喷砂抛光',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53'),(24,NULL,'PERIO003','龈下刮治（单区）',19,NULL,500.00,2,7,'在用',0,'牙周袋深度>3mm区域刮治',NULL,NULL,NULL,NULL,'2026-05-08 17:14:04','2026-05-08 17:55:53');
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
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景名称，如：根管治疗',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '其他' COMMENT '分类，如：牙体牙髓、口腔外科、修复科',
  `level` int DEFAULT '1' COMMENT '复杂度：1简单 2中等 3复杂',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗场景配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_scene`
--

LOCK TABLES `treatment_scene` WRITE;
/*!40000 ALTER TABLE `treatment_scene` DISABLE KEYS */;
INSERT INTO `treatment_scene` VALUES (1,'根管治疗','牙体牙髓',3,1,1,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(3,'树脂充填','牙体牙髓',1,1,3,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(4,'种植修复','种植科',3,1,4,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(6,'洁牙','预防科',1,1,6,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(7,'全冠修复','修复科',2,1,7,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(14,'拔牙','其他',1,1,0,'2026-05-20 05:49:02','2026-05-20 05:49:02');
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
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '步骤名称，如：开髓引流',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `forbidden_keywords` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '禁止关键词，逗号分隔',
  `required_keywords` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '必须包含关键词，逗号分隔',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scene_id` (`scene_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗场景步骤表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `treatment_scene_step`
--

LOCK TABLES `treatment_scene_step` WRITE;
/*!40000 ALTER TABLE `treatment_scene_step` DISABLE KEYS */;
INSERT INTO `treatment_scene_step` VALUES (1,1,'开髓引流',1,'根管预备完成,根管充填完成,永久充填,冠修复完成','开髓,拔髓,麻醉',1,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(2,1,'根管预备',2,'根管充填完成,永久修复,冠修复完成','根管,预备,工作长度,冲洗',1,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(3,1,'根管充填',3,'','根充,试尖,充填,严密',1,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(4,1,'永久修复',4,'','冠修复,桩核,永久',1,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(8,3,'去腐备洞',1,'充填完成','去腐,备洞,隔湿',1,'2026-05-11 08:24:13','2026-05-11 08:24:13'),(9,3,'树脂充填',2,'','树脂,充填,固化,调颌',1,'2026-05-11 08:24:13','2026-05-11 08:24:13');
/*!40000 ALTER TABLE `treatment_scene_step` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin123','管理员','admin',1,NULL,NULL,'2026-05-07 05:03:11','2026-05-07 05:03:11',NULL,NULL),(2,'wangyisheng','123456','王医生','doctor',1,NULL,NULL,'2026-05-08 04:22:44','2026-05-08 04:22:44',NULL,NULL),(3,'liyisheng','123456','李医生','doctor',1,NULL,NULL,'2026-05-08 04:22:44','2026-05-08 04:22:44',NULL,NULL),(4,'hushizhang','123456','å¼ æŠ¤å£«','nurse',1,NULL,NULL,'2026-05-13 03:49:25','2026-05-13 03:49:25',NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'clinic_system'
--

--
-- Dumping routines for database 'clinic_system'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-21  9:42:08
