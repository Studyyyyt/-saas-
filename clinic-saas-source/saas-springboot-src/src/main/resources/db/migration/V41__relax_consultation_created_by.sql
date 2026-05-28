-- 放宽咨询记录录入人ID约束，允许手动输入姓名时不关联系统账号
ALTER TABLE consultation_records MODIFY created_by BIGINT DEFAULT 0 COMMENT '录入人ID';
