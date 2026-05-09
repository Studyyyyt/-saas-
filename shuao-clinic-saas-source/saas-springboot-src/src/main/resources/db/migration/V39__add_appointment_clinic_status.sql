ALTER TABLE appointment ADD COLUMN clinic_status VARCHAR(50) DEFAULT '已预约' COMMENT '接诊状态：已预约/已挂号/等待中/就诊中/已完成' AFTER status;
ALTER TABLE appointment ADD COLUMN check_in_time DATETIME DEFAULT NULL COMMENT '挂号/签到时间' AFTER clinic_status;
