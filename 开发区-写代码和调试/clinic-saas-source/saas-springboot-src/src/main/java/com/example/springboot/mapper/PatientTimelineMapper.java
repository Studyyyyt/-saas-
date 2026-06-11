package com.example.springboot.mapper;

import com.example.springboot.entity.PatientTimeline;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PatientTimelineMapper {

    @Select("SELECT * FROM patient_timeline ORDER BY event_time DESC")
    List<PatientTimeline> selectAll();

    @Select("SELECT * FROM patient_timeline WHERE id = #{id}")
    PatientTimeline selectById(@Param("id") Long id);

    @Select("SELECT * FROM patient_timeline WHERE patient_id = #{patientId} ORDER BY event_time DESC")
    List<PatientTimeline> selectByPatientId(@Param("patientId") Long patientId);

    @Insert("INSERT INTO patient_timeline (patient_id, event_time, event_type, event_title, event_content, source_table, source_id) " +
            "VALUES (#{patient_id}, #{event_time}, #{event_type}, #{event_title}, #{event_content}, #{source_table}, #{source_id})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientTimeline timeline);

    @Update("UPDATE patient_timeline SET event_time = #{event_time}, event_type = #{event_type}, event_title = #{event_title}, " +
            "event_content = #{event_content}, source_table = #{source_table}, source_id = #{source_id} WHERE id = #{id}")
    void update(PatientTimeline timeline);

    @Delete("DELETE FROM patient_timeline WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM patient_timeline WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);
}
