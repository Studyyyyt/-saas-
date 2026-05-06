-- appointment.doctor_account_id 历史脏数据清理模板
-- 注意：以下语句默认注释，仅供业务确认后手工执行。

-- 1) 将“未指定医生”这类历史文本预约保留为空，不自动绑定任何 doctor_account_id
-- UPDATE appointment
-- SET doctor_account_id = NULL
-- WHERE TRIM(COALESCE(doctor_name, '')) = '未指定医生';

-- 2) 对于无法唯一映射的历史医生姓名，逐条人工确认后修复
-- UPDATE appointment
-- SET doctor_account_id = {{CONFIRMED_DOCTOR_ACCOUNT_ID}},
--     doctor_name = '{{CONFIRMED_DOCTOR_NAME}}'
-- WHERE id = {{APPOINTMENT_ID}}
--   AND doctor_account_id IS NULL;

-- 3) 若未来需要统一修正展示冗余，可在 doctor_account_id 已确认后回刷 doctor_name
-- UPDATE appointment a
-- JOIN users u ON u.id = a.doctor_account_id AND u.role = 'doctor'
-- SET a.doctor_name = u.name
-- WHERE a.doctor_account_id IS NOT NULL
--   AND (a.doctor_name IS NULL OR TRIM(a.doctor_name) <> TRIM(u.name));
