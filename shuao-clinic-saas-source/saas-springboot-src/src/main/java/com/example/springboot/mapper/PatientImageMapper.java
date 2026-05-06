package com.example.springboot.mapper;

import com.example.springboot.entity.PatientImage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PatientImageMapper {

    @Select("SELECT * FROM patient_images WHERE patient_id = #{patientId} ORDER BY image_date DESC, id DESC")
    List<PatientImage> selectByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT * FROM patient_images WHERE patient_id = #{patientId} AND sent_to_patient = 1 ORDER BY sent_at DESC, image_date DESC, id DESC")
    List<PatientImage> selectSentByPatientId(@Param("patientId") Long patientId);

    @Select("SELECT * FROM patient_images WHERE id = #{id}")
    PatientImage selectById(@Param("id") Long id);

    @Insert("INSERT INTO patient_images (patient_id, patient_name, image_name, image_type, image_date, file_path, notes, sent_to_patient, sent_at) " +
            "VALUES (#{patient_id}, #{patient_name}, #{image_name}, #{image_type}, #{image_date}, #{file_path}, #{notes}, #{sent_to_patient}, #{sent_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientImage image);

    @Update("UPDATE patient_images SET sent_to_patient = #{sent_to_patient}, sent_at = #{sent_at} WHERE id = #{id}")
    void updateSendStatus(PatientImage image);

    @Delete("DELETE FROM patient_images WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM patient_images WHERE patient_id = #{patientId}")
    void deleteByPatientId(@Param("patientId") Long patientId);

    @Update("UPDATE patient_images SET patient_name = #{patientName} WHERE patient_id = #{patientId}")
    void updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName);
}
