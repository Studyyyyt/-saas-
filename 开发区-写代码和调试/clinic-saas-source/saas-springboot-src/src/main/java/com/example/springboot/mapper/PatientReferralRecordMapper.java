package com.example.springboot.mapper;

import com.example.springboot.entity.PatientReferralRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PatientReferralRecordMapper {

    @Select("SELECT * FROM patient_referral_records ORDER BY created_at DESC, id DESC")
    List<PatientReferralRecord> selectAll();

    @Select("SELECT * FROM patient_referral_records WHERE patient_id = #{patientId}")
    PatientReferralRecord selectByPatientId(@Param("patientId") Long patientId);

    @Select({
            "<script>",
            "SELECT * FROM patient_referral_records WHERE patient_id IN ",
            "<foreach item='patientId' collection='patientIds' open='(' separator=',' close=')'>#{patientId}</foreach>",
            "</script>"
    })
    List<PatientReferralRecord> selectByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Select("SELECT * FROM patient_referral_records WHERE referrer_patient_id = #{referrerPatientId} ORDER BY created_at DESC, id DESC")
    List<PatientReferralRecord> selectByReferrerPatientId(@Param("referrerPatientId") Long referrerPatientId);

    @Insert("""
            INSERT INTO patient_referral_records (
                patient_id, consultation_record_id, referrer_type,
                referrer_patient_id, referrer_patient_name,
                external_referrer_type, external_referrer_name, external_referrer_contact,
                remark, created_by, created_by_name
            ) VALUES (
                #{patient_id}, #{consultation_record_id}, #{referrer_type},
                #{referrer_patient_id}, #{referrer_patient_name},
                #{external_referrer_type}, #{external_referrer_name}, #{external_referrer_contact},
                #{remark}, #{created_by}, #{created_by_name}
            )
            ON DUPLICATE KEY UPDATE
                consultation_record_id = VALUES(consultation_record_id),
                referrer_type = VALUES(referrer_type),
                referrer_patient_id = VALUES(referrer_patient_id),
                referrer_patient_name = VALUES(referrer_patient_name),
                external_referrer_type = VALUES(external_referrer_type),
                external_referrer_name = VALUES(external_referrer_name),
                external_referrer_contact = VALUES(external_referrer_contact),
                remark = VALUES(remark),
                created_by = VALUES(created_by),
                created_by_name = VALUES(created_by_name)
            """)
    void upsert(PatientReferralRecord record);

    @Delete("DELETE FROM patient_referral_records WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
