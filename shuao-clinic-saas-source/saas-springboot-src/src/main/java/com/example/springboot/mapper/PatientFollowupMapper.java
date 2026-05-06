package com.example.springboot.mapper;

import com.example.springboot.entity.PatientFollowup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PatientFollowupMapper {

    @Select("SELECT * FROM patient_followup ORDER BY followup_date DESC")
    List<PatientFollowup> selectAll();

    @Select("""
            SELECT pf.*,
                   p.name AS patient_name,
                   p.phone AS patient_phone
            FROM patient_followup pf
            LEFT JOIN patients p ON p.id = pf.patient_id
            ORDER BY
              CASE WHEN TRIM(COALESCE(pf.summary, '')) = '' THEN 0 ELSE 1 END ASC,
              CASE WHEN COALESCE(pf.followup_date, pf.next_followup_date) IS NULL THEN 1 ELSE 0 END ASC,
              COALESCE(pf.followup_date, pf.next_followup_date) ASC,
              pf.id DESC
            """)
    List<PatientFollowup> selectAllDetail();

    @Select("SELECT * FROM patient_followup WHERE id = #{id}")
    PatientFollowup selectById(@Param("id") Long id);

    @Select("SELECT * FROM patient_followup WHERE patient_id = #{patientId} ORDER BY followup_date DESC")
    List<PatientFollowup> selectByPatientId(@Param("patientId") Long patientId);

    @Insert("INSERT INTO patient_followup (patient_id, doctor_account_id, doctor_name, followup_date, followup_type, summary, next_followup_date) " +
            "VALUES (#{patient_id}, #{doctor_account_id}, #{doctor_name}, #{followup_date}, #{followup_type}, #{summary}, #{next_followup_date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientFollowup followup);

    @Update("UPDATE patient_followup SET doctor_account_id = #{doctor_account_id}, doctor_name = #{doctor_name}, " +
            "followup_date = #{followup_date}, followup_type = #{followup_type}, summary = #{summary}, " +
            "next_followup_date = #{next_followup_date} WHERE id = #{id}")
    void update(PatientFollowup followup);

    @Delete("DELETE FROM patient_followup WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM patient_followup WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
