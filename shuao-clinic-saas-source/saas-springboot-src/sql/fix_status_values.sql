-- ============================================================
-- 修复 treatment_project_categories 和 treatment_projects 表的状态值
-- 使数据库中的状态与后端 Service 期望的值保持一致
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- 1. 治疗项目分类表 (treatment_project_categories)
-- 原值：1 (数字字符串) -> 新值：启用
-- --------------------------------------------------------
UPDATE treatment_project_categories
SET status = '启用'
WHERE status = '1' OR status = 'active' OR status IS NULL OR status = '';

-- --------------------------------------------------------
-- 2. 治疗项目库表 (treatment_projects)
-- 原值：active -> 新值：在用
-- --------------------------------------------------------
UPDATE treatment_projects
SET status = '在用'
WHERE status = 'active' OR status = '1' OR status IS NULL OR status = '';

SET FOREIGN_KEY_CHECKS = 1;

-- 验证：
-- SELECT DISTINCT status FROM treatment_project_categories;
-- SELECT DISTINCT status FROM treatment_projects;
