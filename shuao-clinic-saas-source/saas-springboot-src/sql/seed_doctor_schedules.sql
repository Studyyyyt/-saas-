-- 医生排班测试数据生成脚本
-- 使用方式：在 MySQL 中执行，或在 Docker 启动后通过 docker exec 执行

-- 先确保有医生账号（如果没有，插入模拟医生到 users 表）
INSERT IGNORE INTO users (username, password, name, role) VALUES
('doctor01', '123456', '王医生', 'doctor'),
('doctor02', '123456', '李医生', 'doctor'),
('doctor03', '123456', '张医生', 'doctor'),
('doctor04', '123456', '刘医生', 'doctor'),
('doctor05', '123456', '陈医生', 'doctor'),
('doctor06', '123456', '杨医生', 'doctor'),
('doctor07', '123456', '赵医生', 'doctor'),
('doctor08', '123456', '黄医生', 'doctor');

-- 清除旧测试排班（可选，保留真实数据请注释掉）
-- DELETE FROM doctors WHERE schedule_date BETWEEN '2025-04-01' AND '2025-06-30';

-- 使用存储过程批量生成排班数据
DELIMITER $$

DROP PROCEDURE IF EXISTS GenerateTestSchedules$$
CREATE PROCEDURE GenerateTestSchedules()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_doctor_name VARCHAR(50);
    DECLARE v_date DATE;
    DECLARE v_day_of_week INT;
    DECLARE v_shift VARCHAR(20);
    DECLARE v_start TIME;
    DECLARE v_end TIME;
    DECLARE v_status VARCHAR(20);
    DECLARE v_rand DECIMAL(10,8);
    DECLARE cur_doctor CURSOR FOR
        SELECT name FROM users WHERE role = 'doctor' AND name IS NOT NULL AND name != '' LIMIT 8;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur_doctor;
    read_loop: LOOP
        FETCH cur_doctor INTO v_doctor_name;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET v_date = '2025-04-01';
        WHILE v_date <= '2025-06-30' DO
            SET v_day_of_week = DAYOFWEEK(v_date);
            SET v_rand = RAND();

            IF v_day_of_week = 1 OR v_day_of_week = 7 THEN
                IF v_rand < 0.7 THEN
                    SET v_shift = 'rest';
                    SET v_start = NULL;
                    SET v_end = NULL;
                    SET v_status = '休息';
                ELSEIF v_rand < 0.85 THEN
                    SET v_shift = 'morning';
                    SET v_start = '09:00:00';
                    SET v_end = '18:00:00';
                    SET v_status = '早班';
                ELSE
                    SET v_shift = 'evening';
                    SET v_start = '13:00:00';
                    SET v_end = '21:00:00';
                    SET v_status = '晚班';
                END IF;
            ELSE
                IF v_rand < 0.45 THEN
                    SET v_shift = 'morning';
                    SET v_start = '09:00:00';
                    SET v_end = '18:00:00';
                    SET v_status = '早班';
                ELSEIF v_rand < 0.80 THEN
                    SET v_shift = 'evening';
                    SET v_start = '13:00:00';
                    SET v_end = '21:00:00';
                    SET v_status = '晚班';
                ELSE
                    SET v_shift = 'rest';
                    SET v_start = NULL;
                    SET v_end = NULL;
                    SET v_status = '休息';
                END IF;
            END IF;

            IF NOT EXISTS (SELECT 1 FROM doctors WHERE doctor_name = v_doctor_name AND schedule_date = v_date) THEN
                INSERT INTO doctors (doctor_name, schedule_date, start_time, end_time, status, shift_type)
                VALUES (v_doctor_name, v_date, v_start, v_end, v_status, v_shift);
            END IF;

            SET v_date = DATE_ADD(v_date, INTERVAL 1 DAY);
        END WHILE;
    END LOOP;
    CLOSE cur_doctor;
END$$

DELIMITER ;

CALL GenerateTestSchedules();
DROP PROCEDURE IF EXISTS GenerateTestSchedules;
