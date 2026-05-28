package com.example.springboot.mapper;

import com.example.springboot.entity.Patient;
import org.apache.ibatis.annotations.*;


import java.util.List;

@Mapper
public interface PatientMapper {

    @Select("""
            SELECT p.*,
                   pr.referrer_type,
                   pr.referrer_patient_id,
                   pr.referrer_patient_name,
                   pr.external_referrer_type,
                   pr.external_referrer_name,
                   pr.external_referrer_contact,
                   pr.remark AS referral_remark,
                   COALESCE(
                       (
                           SELECT mr.doctor_name
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           ORDER BY mr.visit_date DESC, mr.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT t.doctor_name
                           FROM treatment t
                           WHERE t.patient_id = p.id
                           ORDER BY t.treatment_date DESC, t.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT a.doctor_name
                           FROM appointment a
                           WHERE a.patient_id = p.id
                           ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC
                           LIMIT 1
                       )
                   ) AS latest_visit_doctor,
                   (
                       SELECT COALESCE(NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, ''))
                       FROM treatment t
                       WHERE t.patient_id = p.id
                       ORDER BY t.treatment_date DESC, t.id DESC
                       LIMIT 1
                   ) AS latest_treatment
            FROM patients p
            LEFT JOIN patient_referral_records pr ON pr.patient_id = p.id
            ORDER BY p.id DESC
            """)
    List<Patient> selectAll();

    @Select("""
            SELECT p.*,
                   pr.referrer_type,
                   pr.referrer_patient_id,
                   pr.referrer_patient_name,
                   pr.external_referrer_type,
                   pr.external_referrer_name,
                   pr.external_referrer_contact,
                   pr.remark AS referral_remark,
                   COALESCE(
                       (
                           SELECT mr.doctor_name
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           ORDER BY mr.visit_date DESC, mr.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT t.doctor_name
                           FROM treatment t
                           WHERE t.patient_id = p.id
                           ORDER BY t.treatment_date DESC, t.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT a.doctor_name
                           FROM appointment a
                           WHERE a.patient_id = p.id
                           ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC
                           LIMIT 1
                       )
                   ) AS latest_visit_doctor,
                   (
                       SELECT COALESCE(NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, ''))
                       FROM treatment t
                       WHERE t.patient_id = p.id
                       ORDER BY t.treatment_date DESC, t.id DESC
                       LIMIT 1
                   ) AS latest_treatment
            FROM patients p
            LEFT JOIN patient_referral_records pr ON pr.patient_id = p.id
            WHERE p.id = #{id}
            """)
    List<Patient> selectById(Long id);

    @Select("""
            SELECT p.*,
                   pr.referrer_type,
                   pr.referrer_patient_id,
                   pr.referrer_patient_name,
                   pr.external_referrer_type,
                   pr.external_referrer_name,
                   pr.external_referrer_contact,
                   pr.remark AS referral_remark,
                   COALESCE(
                       (
                           SELECT mr.doctor_name
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           ORDER BY mr.visit_date DESC, mr.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT t.doctor_name
                           FROM treatment t
                           WHERE t.patient_id = p.id
                           ORDER BY t.treatment_date DESC, t.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT a.doctor_name
                           FROM appointment a
                           WHERE a.patient_id = p.id
                           ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC
                           LIMIT 1
                       )
                   ) AS latest_visit_doctor,
                   (
                       SELECT COALESCE(NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, ''))
                       FROM treatment t
                       WHERE t.patient_id = p.id
                       ORDER BY t.treatment_date DESC, t.id DESC
                       LIMIT 1
                   ) AS latest_treatment
            FROM patients p
            LEFT JOIN patient_referral_records pr ON pr.patient_id = p.id
            WHERE p.name = #{name}
            ORDER BY p.id DESC
            """)
    List<Patient> selectByName(String name);

    @Select("""
            SELECT p.*,
                   pr.referrer_type,
                   pr.referrer_patient_id,
                   pr.referrer_patient_name,
                   pr.external_referrer_type,
                   pr.external_referrer_name,
                   pr.external_referrer_contact,
                   pr.remark AS referral_remark,
                   COALESCE(
                       (
                           SELECT mr.doctor_name
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           ORDER BY mr.visit_date DESC, mr.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT t.doctor_name
                           FROM treatment t
                           WHERE t.patient_id = p.id
                           ORDER BY t.treatment_date DESC, t.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT a.doctor_name
                           FROM appointment a
                           WHERE a.patient_id = p.id
                           ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC
                           LIMIT 1
                       )
                   ) AS latest_visit_doctor,
                   (
                       SELECT COALESCE(NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, ''))
                       FROM treatment t
                       WHERE t.patient_id = p.id
                       ORDER BY t.treatment_date DESC, t.id DESC
                       LIMIT 1
                   ) AS latest_treatment
            FROM patients p
            LEFT JOIN patient_referral_records pr ON pr.patient_id = p.id
            WHERE p.wechat_openid = #{openid}
            """)
    List<Patient> selectByWechatOpenid(String openid);

    @Select("""
            SELECT p.*,
                   pr.referrer_type,
                   pr.referrer_patient_id,
                   pr.referrer_patient_name,
                   pr.external_referrer_type,
                   pr.external_referrer_name,
                   pr.external_referrer_contact,
                   pr.remark AS referral_remark
            FROM patients p
            LEFT JOIN patient_referral_records pr ON pr.patient_id = p.id
            WHERE p.phone = #{phone}
            ORDER BY p.id DESC
            """)
    List<Patient> selectByPhoneExact(@Param("phone") String phone);

    @Select("""
            SELECT p.*,
                   pr.referrer_type,
                   pr.referrer_patient_id,
                   pr.referrer_patient_name,
                   pr.external_referrer_type,
                   pr.external_referrer_name,
                   pr.external_referrer_contact,
                   pr.remark AS referral_remark,
                   COALESCE(
                       (
                           SELECT mr.doctor_name
                           FROM medical_records mr
                           WHERE mr.patient_id = p.id
                           ORDER BY mr.visit_date DESC, mr.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT t.doctor_name
                           FROM treatment t
                           WHERE t.patient_id = p.id
                           ORDER BY t.treatment_date DESC, t.id DESC
                           LIMIT 1
                       ),
                       (
                           SELECT a.doctor_name
                           FROM appointment a
                           WHERE a.patient_id = p.id
                           ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC
                           LIMIT 1
                       )
                   ) AS latest_visit_doctor,
                   (
                       SELECT COALESCE(NULLIF(t.appointment_purpose, ''), NULLIF(t.treatment_content, ''), NULLIF(t.treatment_product, ''))
                       FROM treatment t
                       WHERE t.patient_id = p.id
                       ORDER BY t.treatment_date DESC, t.id DESC
                       LIMIT 1
                   ) AS latest_treatment
            FROM patients p
            LEFT JOIN patient_referral_records pr ON pr.patient_id = p.id
            WHERE CAST(p.id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.name, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.phone, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.email, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.address, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.relation_type, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.related_patient_name, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(pr.referrer_patient_name, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(pr.external_referrer_name, '')) LIKE CONCAT('%', #{keyword}, '%')
               OR TRIM(COALESCE(p.name_pinyin, '')) LIKE CONCAT(#{keyword}, '%')
               OR TRIM(COALESCE(p.name_initials, '')) LIKE CONCAT(#{keyword}, '%')
            ORDER BY p.id DESC
            """)
    List<Patient> searchByKeyword(@Param("keyword") String keyword);

    @Insert("INSERT INTO patients (name, name_pinyin, name_initials, gender, age, date_of_birth, phone, email, address, relation_type, related_patient_id, related_patient_name, wechat_openid, customer_source) VALUES (#{name}, #{name_pinyin}, #{name_initials}, #{gender}, #{age}, #{date_of_birth}, #{phone}, #{email}, #{address}, #{relation_type}, #{related_patient_id}, #{related_patient_name}, #{wechat_openid}, #{customer_source})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addPatient(Patient patient);

    @Update("UPDATE patients SET name = #{name}, name_pinyin = #{name_pinyin}, name_initials = #{name_initials}, gender = #{gender}, age = #{age}, date_of_birth = #{date_of_birth}, phone = #{phone}, email = #{email}, address = #{address}, relation_type = #{relation_type}, related_patient_id = #{related_patient_id}, related_patient_name = #{related_patient_name}, wechat_openid = #{wechat_openid}, customer_source = #{customer_source} WHERE id = #{id}")
    void updatePatient(Patient patient);

    @Update("UPDATE patients SET wechat_openid = #{wechat_openid} WHERE id = #{id}")
    void bindWechatOpenid(Patient patient);

    @Update("UPDATE patients SET customer_source = #{customer_source} WHERE id = #{id}")
    void updateCustomerSource(@Param("id") Long id, @Param("customer_source") String customerSource);

    @Select("<script>" +
            "SELECT * FROM patients " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (name LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%') OR name_pinyin LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test='gender != null and gender != \"\"'>AND gender = #{gender}</if>" +
            "  <if test='ageMin != null'>AND age &gt;= #{ageMin}</if>" +
            "  <if test='ageMax != null'>AND age &lt;= #{ageMax}</if>" +
            "  <if test='customerSource != null and customerSource != \"\"'>AND customer_source = #{customerSource}</if>" +
            "</where>" +
            "ORDER BY updated_at DESC" +
            "</script>")
    List<Patient> searchPatients(@Param("keyword") String keyword,
                                @Param("gender") String gender,
                                @Param("ageMin") Integer ageMin,
                                @Param("ageMax") Integer ageMax,
                                @Param("customerSource") String customerSource);

    @Delete("DELETE FROM patients WHERE id = #{id}")
    void deletePatient(@Param("id") int id);

    @Delete({
            "<script>",
            "DELETE FROM patients WHERE id IN ",
            "<foreach item='id' collection='list' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    void deletePatientBatch(List<Long> ids);

    @Update("""
            UPDATE patients
            SET relation_type = NULL,
                related_patient_id = NULL,
                related_patient_name = NULL
            WHERE related_patient_id = #{relatedPatientId}
            """)
    void clearRelatedPatientReference(@Param("relatedPatientId") Long relatedPatientId);

}
