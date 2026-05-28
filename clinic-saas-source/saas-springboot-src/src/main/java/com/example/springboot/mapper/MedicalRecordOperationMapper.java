package com.example.springboot.mapper;

import com.example.springboot.entity.MedicalRecordOperation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MedicalRecordOperationMapper {

    String BASE_COLUMNS = "id, medical_record_id, project_id, project_name, operation_id, operation_name, factory_id, factory_name, tooth_positions, remark, lab_order_status, skip_reason, " +
            "lab_order_registered_at, created_by, created_by_name, updated_by, updated_by_name, created_at, updated_at";

    @Select("""
            SELECT mro.id,
                   mro.medical_record_id,
                   mro.project_id,
                   mro.project_name,
                   mro.operation_id,
                   mro.operation_name,
                   mro.factory_id,
                   mro.factory_name,
                   mro.tooth_positions,
                   mro.remark,
                   mro.lab_order_status,
                   mro.skip_reason,
                   mro.lab_order_registered_at,
                   mro.created_by,
                   mro.created_by_name,
                   mro.updated_by,
                   mro.updated_by_name,
                   mro.created_at,
                   mro.updated_at,
                   mr.visit_date,
                   mr.patient_id,
                   mr.patient_name,
                   mr.doctor_account_id,
                   mr.doctor_name,
                   o.operation_category,
                   o.need_lab_processing,
                   o.default_processing_days
            FROM medical_record_operations mro
            LEFT JOIN medical_records mr ON mr.id = mro.medical_record_id
            LEFT JOIN treatment_operations o ON o.id = mro.operation_id
            WHERE mro.medical_record_id = #{medicalRecordId}
            ORDER BY mro.created_at ASC, mro.id ASC
            """)
    List<MedicalRecordOperation> selectByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

    @Select({
            "<script>",
            "SELECT mro.id,",
            "       mro.medical_record_id,",
            "       mro.project_id,",
            "       mro.project_name,",
            "       mro.operation_id,",
            "       mro.operation_name,",
            "       mro.factory_id,",
            "       mro.factory_name,",
            "       mro.tooth_positions,",
            "       mro.remark,",
            "       mro.lab_order_status,",
            "       mro.skip_reason,",
            "       mro.lab_order_registered_at,",
            "       mro.created_by,",
            "       mro.created_by_name,",
            "       mro.updated_by,",
            "       mro.updated_by_name,",
            "       mro.created_at,",
            "       mro.updated_at,",
            "       mr.visit_date,",
            "       mr.patient_id,",
            "       mr.patient_name,",
            "       mr.doctor_account_id,",
            "       mr.doctor_name,",
            "       o.operation_category,",
            "       o.need_lab_processing,",
            "       o.default_processing_days",
            "FROM medical_record_operations mro",
            "LEFT JOIN medical_records mr ON mr.id = mro.medical_record_id",
            "LEFT JOIN treatment_operations o ON o.id = mro.operation_id",
            "WHERE mro.medical_record_id IN ",
            "<foreach item='id' collection='medicalRecordIds' open='(' separator=',' close=')'>#{id}</foreach>",
            "ORDER BY mro.medical_record_id ASC, mro.created_at ASC, mro.id ASC",
            "</script>"
    })
    List<MedicalRecordOperation> selectByMedicalRecordIds(@Param("medicalRecordIds") List<Long> medicalRecordIds);

    @Select("select " + BASE_COLUMNS + " from medical_record_operations where id = #{id} limit 1")
    MedicalRecordOperation selectPlainById(@Param("id") Long id);

    @Insert("INSERT INTO medical_record_operations(medical_record_id, project_id, project_name, operation_id, operation_name, factory_id, factory_name, tooth_positions, remark, lab_order_status, skip_reason, lab_order_registered_at, created_by, created_by_name, updated_by, updated_by_name) " +
            "VALUES(#{medical_record_id}, #{project_id}, #{project_name}, #{operation_id}, #{operation_name}, #{factory_id}, #{factory_name}, #{tooth_positions}, #{remark}, #{lab_order_status}, #{skip_reason}, #{lab_order_registered_at}, #{created_by}, #{created_by_name}, #{updated_by}, #{updated_by_name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MedicalRecordOperation item);

    @Update("UPDATE medical_record_operations SET project_id = #{project_id}, project_name = #{project_name}, operation_id = #{operation_id}, operation_name = #{operation_name}, factory_id = #{factory_id}, factory_name = #{factory_name}, tooth_positions = #{tooth_positions}, remark = #{remark}, lab_order_status = #{lab_order_status}, skip_reason = #{skip_reason}, lab_order_registered_at = #{lab_order_registered_at}, updated_by = #{updated_by}, updated_by_name = #{updated_by_name} WHERE id = #{id}")
    void update(MedicalRecordOperation item);

    @Delete("DELETE FROM medical_record_operations WHERE id = #{id}")
    void deleteById(@Param("id") Long id);

    @Delete("DELETE FROM medical_record_operations WHERE medical_record_id = #{medicalRecordId}")
    void deleteByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);

    @Select({
            "<script>",
            "SELECT mro.id,",
            "       mro.medical_record_id,",
            "       mro.project_id,",
            "       mro.project_name,",
            "       mro.operation_id,",
            "       mro.operation_name,",
            "       mro.factory_id,",
            "       mro.factory_name,",
            "       mro.tooth_positions,",
            "       mro.remark,",
            "       mro.lab_order_status,",
            "       mro.skip_reason,",
            "       mro.lab_order_registered_at,",
            "       mro.created_by,",
            "       mro.created_by_name,",
            "       mro.updated_by,",
            "       mro.updated_by_name,",
            "       mro.created_at,",
            "       mro.updated_at,",
            "       mr.visit_date,",
            "       mr.patient_id,",
            "       mr.patient_name,",
            "       mr.doctor_account_id,",
            "       mr.doctor_name,",
            "       o.operation_category,",
            "       o.need_lab_processing,",
            "       o.default_processing_days",
            "FROM medical_record_operations mro",
            "JOIN medical_records mr ON mr.id = mro.medical_record_id",
            "LEFT JOIN treatment_operations o ON o.id = mro.operation_id",
            "WHERE COALESCE(o.need_lab_processing, 0) = 1",
            "  AND mro.lab_order_status = 0",
            "<if test='patientId != null and patientId &gt; 0'>",
            "  AND mr.patient_id = #{patientId}",
            "</if>",
            "<if test='doctorAccountId != null and doctorAccountId &gt; 0'>",
            "  AND mr.doctor_account_id = #{doctorAccountId}",
            "</if>",
            "ORDER BY mr.visit_date DESC, mro.id DESC",
            "</script>"
    })
    List<MedicalRecordOperation> selectPendingLabList(@Param("patientId") Long patientId,
                                                      @Param("doctorAccountId") Long doctorAccountId);
}
