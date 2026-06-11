package com.example.springboot.mapper;

import com.example.springboot.entity.PatientConsent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

@Mapper
public interface PatientConsentMapper {

    @Select("SELECT * FROM patient_consent WHERE patient_id = #{patientId} ORDER BY issued_at DESC, id DESC")
    List<PatientConsent> selectByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT * FROM patient_consent WHERE id = #{id}")
    PatientConsent selectById(@Param("id") Long id);

    @Insert("INSERT INTO patient_consent (patient_id, patient_name, doctor_account_id, doctor_name, title, content, status, issued_at, read_at, signed_at, signature_name, signature_data, signature_remark) " +
            "VALUES (#{patient_id}, #{patient_name}, #{doctor_account_id}, #{doctor_name}, #{title}, #{content}, #{status}, #{issued_at}, #{read_at}, #{signed_at}, #{signature_name}, #{signature_data}, #{signature_remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientConsent consent);

    @Update("UPDATE patient_consent SET read_at = #{readAt} WHERE id = #{id} AND read_at IS NULL")
    void updateReadAt(@Param("id") Long id, @Param("readAt") Date readAt);

    @Update("UPDATE patient_consent SET status = #{status}, read_at = #{read_at}, signed_at = #{signed_at}, signature_name = #{signature_name}, signature_data = #{signature_data}, signature_remark = #{signature_remark} WHERE id = #{id}")
    void updateSigned(PatientConsent consent);

    @Delete("DELETE FROM patient_consent WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);

    @Update("UPDATE patient_consent SET patient_name = #{patientName} WHERE patient_id = #{patientId}")
    void updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName);
}
