package com.example.springboot.mapper;

import com.example.springboot.entity.PatientCustomGroup;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PatientCustomGroupMapper {

    @Select("SELECT * FROM patient_custom_group WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<PatientCustomGroup> selectActive();

    @Select("SELECT * FROM patient_custom_group WHERE id = #{id} LIMIT 1")
    PatientCustomGroup selectById(@Param("id") Long id);

    @Select("SELECT * FROM patient_custom_group WHERE group_key = #{groupKey} LIMIT 1")
    PatientCustomGroup selectByGroupKey(@Param("groupKey") String groupKey);

    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM patient_custom_group")
    Integer selectMaxSortOrder();

    @Insert("INSERT INTO patient_custom_group(group_key, group_name, status, sort_order, remark) VALUES(#{group_key}, #{group_name}, #{status}, #{sort_order}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientCustomGroup group);

    @Update("UPDATE patient_custom_group SET group_name = #{group_name}, status = #{status}, sort_order = #{sort_order}, remark = #{remark} WHERE id = #{id}")
    void update(PatientCustomGroup group);
}
