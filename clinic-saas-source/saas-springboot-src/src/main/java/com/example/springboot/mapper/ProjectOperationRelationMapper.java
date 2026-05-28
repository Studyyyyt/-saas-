package com.example.springboot.mapper;

import com.example.springboot.entity.ProjectOperationRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectOperationRelationMapper {

    @Select("""
            SELECT por.id,
                   por.project_id,
                   por.operation_id,
                   por.operation_order,
                   por.is_required,
                   por.performance_weight,
                   por.created_at,
                   por.updated_at,
                   o.operation_code,
                   o.operation_name,
                   o.operation_category,
                   o.need_lab_processing,
                   o.default_processing_days
            FROM project_operation_relations por
            LEFT JOIN treatment_operations o ON o.id = por.operation_id
            WHERE por.project_id = #{projectId}
            ORDER BY por.operation_order ASC, por.id ASC
            """)
    List<ProjectOperationRelation> selectByProjectId(@Param("projectId") Long projectId);

    @Insert("INSERT INTO project_operation_relations(project_id, operation_id, operation_order, is_required, performance_weight) VALUES(#{project_id}, #{operation_id}, #{operation_order}, #{is_required}, #{performance_weight})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ProjectOperationRelation item);

    @Delete("DELETE FROM project_operation_relations WHERE project_id = #{projectId}")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Delete("DELETE FROM project_operation_relations WHERE operation_id = #{operationId}")
    void deleteByOperationId(@Param("operationId") Long operationId);
}
