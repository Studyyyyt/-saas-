package com.example.springboot.mapper;

import com.example.springboot.entity.PatientInsightSummary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PatientInsightSummaryMapper {

    @Results(id = "patientInsightSummaryMap", value = {
            @Result(column = "patient_id", property = "patient_id"),
            @Result(column = "last_visit_date", property = "last_visit_date"),
            @Result(column = "total_visit_count", property = "total_visit_count"),
            @Result(column = "total_spent", property = "total_spent"),
            @Result(column = "last_treatment_date", property = "last_treatment_date"),
            @Result(column = "visit_count_last_6m", property = "visit_count_last_6m"),
            @Result(column = "high_value_flag", property = "high_value_flag"),
            @Result(column = "lost_risk_flag", property = "lost_risk_flag"),
            @Result(column = "referred_count", property = "referred_count"),
            @Result(column = "referred_revenue", property = "referred_revenue"),
            @Result(column = "word_of_mouth_flag", property = "word_of_mouth_flag"),
            @Result(column = "updated_at", property = "updated_at")
    })
    @Select("SELECT * FROM patient_insight_summary ORDER BY patient_id DESC")
    List<PatientInsightSummary> selectAll();

    @ResultMap("patientInsightSummaryMap")
    @Select("SELECT * FROM patient_insight_summary WHERE patient_id = #{patientId}")
    PatientInsightSummary selectByPatientId(@Param("patientId") Long patientId);

    @ResultMap("patientInsightSummaryMap")
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
