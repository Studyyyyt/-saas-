package com.example.springboot.mapper;

import com.example.springboot.entity.TreatmentOperationAllocation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TreatmentOperationAllocationMapper {

    @Select({
            "<script>",
            "SELECT id, treatment_id, medical_record_id, medical_record_operation_id, patient_id, doctor_account_id, doctor_name,",
            "       project_id, project_name, operation_id, operation_name, performance_weight, allocation_ratio, allocated_turnover_amount, created_at, updated_at",
            "FROM treatment_operation_allocations",
            "WHERE treatment_id IN ",
            "<foreach item='id' collection='treatmentIds' open='(' separator=',' close=')'>#{id}</foreach>",
            "ORDER BY treatment_id ASC, id ASC",
            "</script>"
    })
    List<TreatmentOperationAllocation> selectByTreatmentIds(@Param("treatmentIds") List<Long> treatmentIds);

    @Select("SELECT id, treatment_id, medical_record_id, medical_record_operation_id, patient_id, doctor_account_id, doctor_name, project_id, project_name, operation_id, operation_name, performance_weight, allocation_ratio, allocated_turnover_amount, created_at, updated_at FROM treatment_operation_allocations WHERE treatment_id = #{treatmentId} ORDER BY id ASC")
    List<TreatmentOperationAllocation> selectByTreatmentId(@Param("treatmentId") Long treatmentId);

    @Insert("INSERT INTO treatment_operation_allocations(treatment_id, medical_record_id, medical_record_operation_id, patient_id, doctor_account_id, doctor_name, project_id, project_name, operation_id, operation_name, performance_weight, allocation_ratio, allocated_turnover_amount) " +
            "VALUES(#{treatment_id}, #{medical_record_id}, #{medical_record_operation_id}, #{patient_id}, #{doctor_account_id}, #{doctor_name}, #{project_id}, #{project_name}, #{operation_id}, #{operation_name}, #{performance_weight}, #{allocation_ratio}, #{allocated_turnover_amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TreatmentOperationAllocation item);

    @Delete("DELETE FROM treatment_operation_allocations WHERE treatment_id = #{treatmentId}")
    void deleteByTreatmentId(@Param("treatmentId") Long treatmentId);
}
