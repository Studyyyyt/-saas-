-- 为 appointment 表增加 doctor_account_id，并按当前 doctor 账号做安全回填
-- 只做结构补齐 + 可确定映射的数据回填；不猜测历史脏值。

ALTER TABLE appointment
    ADD COLUMN IF NOT EXISTS doctor_account_id BIGINT DEFAULT NULL COMMENT '医生账号ID' AFTER appointment_time,
    ADD KEY IF NOT EXISTS idx_appointment_doctor_account_id (doctor_account_id);

-- 自动回填：仅当 doctor_name 能唯一映射到 users.role='doctor' 的姓名时才写入
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
  AND TRIM(a.doctor_name) <> '未指定医生';

-- 回填结果预览
SELECT a.id,
       a.patient_name,
       a.doctor_name,
       a.doctor_account_id,
       a.appointment_date,
       a.appointment_time,
       a.status
FROM appointment a
ORDER BY a.id DESC;

-- 查看仍未回填 doctor_account_id 的预约（需人工确认或保留为空）
SELECT a.id,
       a.patient_name,
       a.doctor_name,
       a.appointment_date,
       a.appointment_time,
       a.status
FROM appointment a
WHERE a.doctor_account_id IS NULL
ORDER BY a.id DESC;
