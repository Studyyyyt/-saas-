package com.example.springboot.mapper;

import com.example.springboot.entity.PatientRiskTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PatientRiskTagMapper {

    @Select("SELECT * FROM patient_risk_tag ORDER BY risk_level DESC, id DESC")
    List<PatientRiskTag> selectAll();

    @Select("SELECT * FROM patient_risk_tag WHERE id = #{id}")
    PatientRiskTag selectById(@Param("id") Long id);

    @Select("SELECT * FROM patient_risk_tag WHERE patient_id = #{patientId} AND status = 1 ORDER BY risk_level DESC")
    List<PatientRiskTag> selectActiveByPatientId(@Param("patientId") Long patientId);

    @Select({
            "<script>",
            "SELECT * FROM patient_risk_tag",
            "WHERE status = 1",
            "AND patient_id IN ",
            "<foreach item='id' collection='patientIds' open='(' separator=',' close=')'>#{id}</foreach>",
            "ORDER BY patient_id ASC, risk_level DESC, id DESC",
            "</script>"
    })
    List<PatientRiskTag> selectActiveByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Insert("INSERT INTO patient_risk_tag (patient_id, tag_code, tag_name, risk_level, source, status, note) " +
            "VALUES (#{patient_id}, #{tag_code}, #{tag_name}, #{risk_level}, #{source}, #{status}, #{note})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientRiskTag riskTag);

    @Update("UPDATE patient_risk_tag SET tag_code = #{tag_code}, tag_name = #{tag_name}, risk_level = #{risk_level}, " +
            "source = #{source}, status = #{status}, note = #{note} WHERE id = #{id}")
    void update(PatientRiskTag riskTag);

    @Delete("DELETE FROM patient_risk_tag WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM patient_risk_tag WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
