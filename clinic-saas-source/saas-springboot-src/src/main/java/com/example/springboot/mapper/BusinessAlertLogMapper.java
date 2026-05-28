package com.example.springboot.mapper;

import com.example.springboot.entity.BusinessAlertLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

@Mapper
public interface BusinessAlertLogMapper {

    @Select("SELECT * FROM business_alert_log WHERE alert_date = #{alertDate} ORDER BY created_at DESC, id DESC")
    List<BusinessAlertLog> selectByAlertDate(@Param("alertDate") Date alertDate);

    @Select("SELECT * FROM business_alert_log ORDER BY alert_date DESC, created_at DESC, id DESC LIMIT #{limit}")
    List<BusinessAlertLog> selectRecent(@Param("limit") int limit);

    @Insert("INSERT INTO business_alert_log (" +
            "alert_date, alert_code, alert_level, alert_title, alert_message, metric_name, current_value, baseline_value, change_rate, suggested_action, source_type, trigger_type" +
            ") VALUES (" +
            "#{alert_date}, #{alert_code}, #{alert_level}, #{alert_title}, #{alert_message}, #{metric_name}, #{current_value}, #{baseline_value}, #{change_rate}, #{suggested_action}, #{source_type}, #{trigger_type}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BusinessAlertLog alertLog);
}
