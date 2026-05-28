-- 历史 appointment.patient_id 回填脚本
-- 规则：仅对“患者姓名唯一”的记录自动回填；重名患者不自动绑定，避免串档。

-- 1) 自动回填：按患者姓名唯一匹配患者主表
UPDATE appointment a
JOIN (
    SELECT p.name, MAX(p.id) AS patient_id
    FROM patient p
    GROUP BY p.name
    HAVING COUNT(*) = 1
) u ON u.name = a.patient_name
SET a.patient_id = u.patient_id
WHERE a.patient_id IS NULL
  AND a.patient_name IS NOT NULL
  AND TRIM(a.patient_name) <> '';

-- 2) 查看仍未回填成功的预约记录
SELECT a.id, a.patient_name, a.appointment_date, a.appointment_time, a.doctor_name, a.status
FROM appointment a
WHERE a.patient_id IS NULL
ORDER BY a.id DESC;

-- 3) 查看重名风险患者，需人工确认后处理
SELECT p.name, COUNT(*) AS patient_count, GROUP_CONCAT(p.id ORDER BY p.id) AS patient_ids
FROM patient p
GROUP BY p.name
HAVING COUNT(*) > 1
ORDER BY patient_count DESC, p.name;

-- 4) 手工修复示例
-- UPDATE appointment SET patient_id = 123 WHERE id = 456;
