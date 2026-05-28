package com.example.springboot.mapper;

import com.example.springboot.entity.PatientInsightSummary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PatientInsightSummaryMapper {

    @Select("SELECT * FROM patient_insight_summary ORDER BY patient_id DESC")
    List<PatientInsightSummary> selectAll();

    @Select("SELECT * FROM patient_insight_summary WHERE patient_id = #{patientId}")
    PatientInsightSummary selectByPatientId(@Param("patientId") Long patientId);

    @Select({
            "<script>",
            "SELECT * FROM patient_insight_summary WHERE patient_id IN ",
            "<foreach item='patientId' collection='patientIds' open='(' separator=',' close=')'>#{patientId}</foreach>",
            "</script>"
    })
    List<PatientInsightSummary> selectByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Insert("""
            INSERT INTO patient_insight_summary (
                patient_id, last_visit_date, total_visit_count, total_spent,
                last_treatment_date, visit_count_last_6m, high_value_flag,
                lost_risk_flag, referred_count, referred_revenue,
                word_of_mouth_flag
            ) VALUES (
                #{patient_id}, #{last_visit_date}, #{total_visit_count}, #{total_spent},
                #{last_treatment_date}, #{visit_count_last_6m}, #{high_value_flag},
                #{lost_risk_flag}, #{referred_count}, #{referred_revenue},
                #{word_of_mouth_flag}
            )
            ON DUPLICATE KEY UPDATE
                last_visit_date = VALUES(last_visit_date),
                total_visit_count = VALUES(total_visit_count),
                total_spent = VALUES(total_spent),
                last_treatment_date = VALUES(last_treatment_date),
                visit_count_last_6m = VALUES(visit_count_last_6m),
                high_value_flag = VALUES(high_value_flag),
                lost_risk_flag = VALUES(lost_risk_flag),
                referred_count = VALUES(referred_count),
                referred_revenue = VALUES(referred_revenue),
                word_of_mouth_flag = VALUES(word_of_mouth_flag)
            """)
    void upsert(PatientInsightSummary summary);

    @Delete("DELETE FROM patient_insight_summary WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
