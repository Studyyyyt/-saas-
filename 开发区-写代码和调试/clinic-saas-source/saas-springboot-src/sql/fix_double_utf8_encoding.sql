-- ============================================================
-- 修复双重 UTF-8 编码（Mojibake）数据
-- 问题：数据被错误地以 Latin1 读取后又以 UTF-8 存储
-- 修复原理：将当前 UTF-8 字符串先转回 Latin1 字节，再按 UTF-8 解析
-- 兼容：MySQL 8.0+
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- 1. 治疗项目分类表 (treatment_project_categories)
-- --------------------------------------------------------
UPDATE treatment_project_categories
SET name = CONVERT(CAST(CONVERT(name USING latin1) AS BINARY) USING utf8mb4)
WHERE name IS NOT NULL AND name != '';

-- --------------------------------------------------------
-- 2. 治疗项目库表 (treatment_projects)
-- --------------------------------------------------------
UPDATE treatment_projects
SET project_name = CONVERT(CAST(CONVERT(project_name USING latin1) AS BINARY) USING utf8mb4),
    category_path = CONVERT(CAST(CONVERT(category_path USING latin1) AS BINARY) USING utf8mb4),
    remark = CONVERT(CAST(CONVERT(remark USING latin1) AS BINARY) USING utf8mb4)
WHERE project_name IS NOT NULL AND project_name != '';

-- --------------------------------------------------------
-- 3. 检查是否有其他表存在类似问题（可选扩展）
-- 以下为已知可能受影响的表，根据实际情况启用
-- --------------------------------------------------------

-- 3.1 治疗操作字典表（如果保留）
-- UPDATE treatment_operations
-- SET operation_name = CONVERT(CAST(CONVERT(operation_name USING latin1) AS BINARY) USING utf8mb4),
--     description = CONVERT(CAST(CONVERT(description USING latin1) AS BINARY) USING utf8mb4)
-- WHERE operation_name IS NOT NULL AND operation_name != '';

-- 3.2 项目与操作关联表
-- UPDATE project_operation_relations
-- SET operation_name = CONVERT(CAST(CONVERT(operation_name USING latin1) AS BINARY) USING utf8mb4)
-- WHERE operation_name IS NOT NULL AND operation_name != '';

SET FOREIGN_KEY_CHECKS = 1;

-- 修复完成后建议验证：
-- SELECT id, name FROM treatment_project_categories LIMIT 5;
-- SELECT id, project_name, category_path FROM treatment_projects LIMIT 5;
