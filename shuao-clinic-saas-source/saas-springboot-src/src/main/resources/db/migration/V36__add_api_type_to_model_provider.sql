-- 为 ai_model_provider 表增加 api_type 字段，支持 chat_completions / responses 两种 API 类型
-- MySQL 8.0.16+ 才支持 IF NOT EXISTS，这里用 idempotent 写法
SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'ai_model_provider' AND column_name = 'api_type');
SET @sql := IF(@exist = 0, 'ALTER TABLE ai_model_provider ADD COLUMN api_type VARCHAR(32) DEFAULT \'chat_completions\' COMMENT \'API 类型：chat_completions（通用）或 responses（OpenAI 原生）\'', 'SELECT \'api_type already exists\'');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
