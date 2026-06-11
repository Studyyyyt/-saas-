package com.example.springboot.mapper;

import com.example.springboot.entity.PatientCustomGroupMember;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PatientCustomGroupMemberMapper {

    @Select({
            "<script>",
            "SELECT m.id, m.group_id, m.patient_id, g.group_key, m.created_at",
            "FROM patient_custom_group_member m",
            "JOIN patient_custom_group g ON g.id = m.group_id",
            "WHERE g.status = 1",
            "<if test='patientIds != null and patientIds.size() &gt; 0'>",
            "AND m.patient_id IN ",
            "<foreach item='id' collection='patientIds' open='(' separator=',' close=')'>#{id}</foreach>",
            "</if>",
            "</script>"
    })
    List<PatientCustomGroupMember> selectByPatientIds(@Param("patientIds") List<Long> patientIds);

    @Insert({
            "<script>",
            "INSERT IGNORE INTO patient_custom_group_member(group_id, patient_id) VALUES",
            "<foreach item='patientId' collection='patientIds' separator=','>",
            "(#{groupId}, #{patientId})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(@Param("groupId") Long groupId, @Param("patientIds") List<Long> patientIds);
}
