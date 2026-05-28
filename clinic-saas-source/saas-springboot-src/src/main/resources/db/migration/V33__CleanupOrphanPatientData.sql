-- ============================================================
-- V33: 清理历史孤儿数据
-- 删除 patient_id 在 patients 表中不存在的关联记录
-- ============================================================

-- 清理 patient_referral_records 中的孤儿数据
DELETE FROM patient_referral_records
WHERE patient_id IS NOT NULL
  AND patient_id NOT IN (SELECT id FROM patients);

-- 清理 patient_insight_summary 中的孤儿数据
DELETE FROM patient_insight_summary
WHERE patient_id IS NOT NULL
  AND patient_id NOT IN (SELECT id FROM patients);
