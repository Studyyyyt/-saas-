-- 历史数据回填脚本：为 finances / treatment 回填 patient_id
-- 执行前请先选择数据库：USE clinic_system_new;
-- 建议先完整备份相关表。

START TRANSACTION;

-- 1) 如果库里还没有 patient_id 字段，请先执行 schema-extra.sql 中的 ALTER TABLE

-- 2) finances 表：优先使用 name 精确匹配患者姓名回填 patient_id
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
  AND dup.name IS NULL;

-- 3) treatment 表：优先使用 patient_name 精确匹配患者姓名回填 patient_id
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
  AND dup.name IS NULL;

-- 4) 查看未回填成功的 finances 记录（通常是姓名为空、姓名不一致或同名患者）
SELECT id, name, amount, date, type, remark
FROM finances
WHERE patient_id IS NULL
ORDER BY date DESC, id DESC;

-- 5) 查看未回填成功的 treatment 记录
SELECT id, patient_name, appointment_purpose, doctor_name, treatment_date, treatment_fee
FROM treatment
WHERE patient_id IS NULL
ORDER BY treatment_date DESC, id DESC;

-- 6) 查看同名患者，避免误绑
SELECT name, COUNT(*) AS cnt, GROUP_CONCAT(id ORDER BY id) AS patient_ids
FROM patients
GROUP BY name
HAVING COUNT(*) > 1
ORDER BY cnt DESC, name;

-- 7) 对于同名患者或姓名不一致数据，请人工确认后再单条修正，例如：
-- UPDATE finances SET patient_id = 123 WHERE id = 1001;
-- UPDATE treatment SET patient_id = 123 WHERE id = 2001;

COMMIT;
