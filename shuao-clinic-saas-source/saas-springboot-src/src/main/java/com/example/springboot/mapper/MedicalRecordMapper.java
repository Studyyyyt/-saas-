package com.example.springboot.mapper;

import com.example.springboot.entity.MedicalRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MedicalRecordMapper {

    @Select("SELECT * FROM medical_records ORDER BY visit_date DESC")
    List<MedicalRecord> selectAll();

    @Select("<script>SELECT * FROM medical_records WHERE 1=1 " +
            "<if test='doctorAccountId != null'> AND doctor_account_id = #{doctorAccountId} </if>" +
            "<if test='recordStatus != null and recordStatus != \"\"'> AND record_status = #{recordStatus} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND visit_date &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND visit_date &lt;= #{endDate} </if>" +
            "ORDER BY visit_date DESC</script>")
    List<MedicalRecord> selectAllWithFilter(@Param("doctorAccountId") Long doctorAccountId,
                                             @Param("recordStatus") String recordStatus,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    @Select("SELECT * FROM medical_records WHERE patient_id = #{patientId} ORDER BY visit_date DESC")
    List<MedicalRecord> selectByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT * FROM medical_records WHERE patient_name LIKE CONCAT('%', #{name}, '%') ORDER BY visit_date DESC")
    List<MedicalRecord> selectByPatientName(@Param("name") String name);

    @Select("SELECT * FROM medical_records WHERE id = #{id}")
    MedicalRecord selectById(@Param("id") Long id);

    @Insert("INSERT INTO medical_records (patient_id, patient_name, doctor_account_id, doctor_name, nurse_name, assistant_name, visit_date, record_type, chief_complaint, present_illness_history, past_history, infectious_history, allergy_history, general_condition, examination, auxiliary_examination, diagnosis, treatment_plan, treatment, tooth_positions, medical_advice, prescription, record_tags, image_summary, notes, record_status) " +
            "VALUES (#{patient_id}, #{patient_name}, #{doctor_account_id}, #{doctor_name}, #{nurse_name}, #{assistant_name}, #{visit_date}, #{record_type}, #{chief_complaint}, #{present_illness_history}, #{past_history}, #{infectious_history}, #{allergy_history}, #{general_condition}, #{examination}, #{auxiliary_examination}, #{diagnosis}, #{treatment_plan}, #{treatment}, #{tooth_positions}, #{medical_advice}, #{prescription}, #{record_tags}, #{image_summary}, #{notes}, #{record_status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MedicalRecord record);

    @Update("UPDATE medical_records SET patient_id=#{patient_id}, patient_name=#{patient_name}, doctor_account_id=#{doctor_account_id}, doctor_name=#{doctor_name}, nurse_name=#{nurse_name}, assistant_name=#{assistant_name}, " +
            "visit_date=#{visit_date}, record_type=#{record_type}, chief_complaint=#{chief_complaint}, present_illness_history=#{present_illness_history}, past_history=#{past_history}, infectious_history=#{infectious_history}, allergy_history=#{allergy_history}, general_condition=#{general_condition}, examination=#{examination}, auxiliary_examination=#{auxiliary_examination}, diagnosis=#{diagnosis}, treatment_plan=#{treatment_plan}, " +
            "treatment=#{treatment}, tooth_positions=#{tooth_positions}, medical_advice=#{medical_advice}, prescription=#{prescription}, record_tags=#{record_tags}, image_summary=#{image_summary}, notes=#{notes}, record_status=#{record_status} WHERE id=#{id}")
    void update(MedicalRecord record);

    @Delete("DELETE FROM medical_records WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM medical_records WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);

    @Update("UPDATE medical_records SET patient_name = #{patientName} WHERE patient_id = #{patientId}")
    void updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName);
}
