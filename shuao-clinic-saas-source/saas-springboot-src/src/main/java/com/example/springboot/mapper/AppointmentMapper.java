package com.example.springboot.mapper;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Inventory;
import org.apache.ibatis.annotations.*;

import java.sql.Date;
import java.util.List;

@Mapper
public interface AppointmentMapper {

    @Select("SELECT id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status, clinic_status, check_in_time FROM appointment")
    List<Appointment> selectAll();

    @Select("SELECT id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status, clinic_status, check_in_time FROM appointment where status = #{status}")
    List<Appointment> findAllByStatus(String status);

    @Select("SELECT id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status, clinic_status, check_in_time FROM appointment WHERE appointment_date = #{appointmentDate}")
    List<Appointment> selectByAppointmentDate(@Param("appointmentDate") Date appointmentDate);


    @Select("select id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status from appointment where id = #{id}")
    List<Appointment> selectById(@Param("id") Long id);

    @Select("select id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status from appointment where id = #{id} and status = #{status}")
    List<Appointment> findByIdAndStatus(Long id, String status);


    @Select("select id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status from appointment where patient_name LIKE CONCAT('%', #{name}, '%')")
    List<Appointment> selectByName(@Param("name") String name);

    @Select("select id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status from appointment where patient_name LIKE CONCAT('%', #{name}, '%') and status = #{status}")
    List<Appointment> findByNameAndStatus(@Param("name") String name, @Param("status") String status);

    @Select("select id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status from appointment where patient_id = #{patientId}")
    List<Appointment> selectByPatientReference(@Param("patientId") Long patientId);

    @Insert("INSERT INTO appointment (patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, cancel_reason, status, clinic_status, check_in_time) " +
            "VALUES (#{patient_id}, #{patient_name}, #{appointment_date}, #{appointment_time}, #{duration_minutes}, #{doctor_account_id}, #{doctor_name}, #{appointment_purpose}, #{cancel_reason}, #{status}, #{clinic_status}, #{check_in_time})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Appointment appointment);

    @Update("UPDATE appointment SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE appointment SET patient_id = #{patient_id}, patient_name = #{patient_name}, appointment_date = #{appointment_date}, appointment_time = #{appointment_time}, duration_minutes = #{duration_minutes}, " +
            "doctor_account_id = #{doctor_account_id}, doctor_name = #{doctor_name}, appointment_purpose = #{appointment_purpose}, cancel_reason = #{cancel_reason}, status = #{status}, clinic_status = #{clinic_status}, check_in_time = #{check_in_time} WHERE id = #{id}")
    void update(Appointment appointment);

    @Delete("DELETE FROM appointment WHERE id = #{id}")
    void delete(@Param("id") int id);

    @Delete("DELETE FROM appointment WHERE patient_id = #{patientId}")
    void deleteByPatientReference(@Param("patientId") Long patientId);

    @Update("UPDATE appointment SET patient_name = #{patientName} WHERE patient_id = #{patientId}")
    void updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName);

    @Delete({
            "<script>",
            "DELETE FROM appointment WHERE id IN ",
            "<foreach item='id' collection='list' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    void deleteAppointmentBatch(@Param("list") List<Long> ids);

}
