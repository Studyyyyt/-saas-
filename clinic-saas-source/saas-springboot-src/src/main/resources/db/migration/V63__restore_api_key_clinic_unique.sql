-- 单 Key 模式：恢复 clinic_id 唯一约束
-- 先清理同一诊所下的重复 Key（保留最新创建的一条）
DELETE ak1 FROM api_key ak1
INNER JOIN api_key ak2
    ON ak1.clinic_id = ak2.clinic_id AND ak1.id < ak2.id;

-- 恢复 clinic_id 唯一约束
ALTER TABLE api_key ADD UNIQUE INDEX uk_clinic (clinic_id);
