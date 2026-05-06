package com.example.springboot.mapper;

import com.example.springboot.entity.DoctorHomeReminderDismissal;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorHomeReminderDismissalMapper {

    @Select("select reminder_key from doctor_home_reminder_dismissal where doctor_account_id = #{doctorAccountId}")
    List<String> selectReminderKeysByDoctorAccountId(@Param("doctorAccountId") Long doctorAccountId);

    @Insert("insert into doctor_home_reminder_dismissal (doctor_account_id, doctor_name, patient_id, patient_name, reminder_key, dismissed_at) " +
            "values (#{doctor_account_id}, #{doctor_name}, #{patient_id}, #{patient_name}, #{reminder_key}, now()) " +
            "on duplicate key update doctor_name=values(doctor_name), patient_id=values(patient_id), patient_name=values(patient_name), dismissed_at=values(dismissed_at)")
    void upsert(DoctorHomeReminderDismissal item);

    @Delete("delete from doctor_home_reminder_dismissal where doctor_account_id = #{doctorAccountId} and reminder_key = #{reminderKey}")
    void delete(@Param("doctorAccountId") Long doctorAccountId, @Param("reminderKey") String reminderKey);
}
