ALTER TABLE medical_records
    ADD COLUMN IF NOT EXISTS doctor_account_id BIGINT DEFAULT NULL COMMENT '接诊医生账号ID' AFTER doctor_name,
    ADD COLUMN IF NOT EXISTS tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位，多个以逗号分隔' AFTER treatment;

ALTER TABLE treatment
    ADD COLUMN IF NOT EXISTS patient_id BIGINT DEFAULT NULL COMMENT '患者ID' AFTER id,
    ADD COLUMN IF NOT EXISTS tooth_positions VARCHAR(255) DEFAULT NULL COMMENT '牙位，多个以逗号分隔' AFTER treatment_content;

CREATE INDEX idx_medical_records_doctor_account_id ON medical_records (doctor_account_id);
CREATE INDEX idx_treatment_patient_id ON treatment (patient_id);
