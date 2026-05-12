package com.example.springboot.mapper;

import com.example.springboot.entity.MedicalRecordPhrase;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MedicalRecordPhraseMapper {

    @Select("select id, field_type, content, category, sort_order, status, created_at, updated_at from medical_record_phrases where status = 1 and field_type = #{fieldType} order by sort_order, id")
    List<MedicalRecordPhrase> selectByFieldType(@Param("fieldType") String fieldType);

    @Select("select id, field_type, content, category, sort_order, status, created_at, updated_at from medical_record_phrases order by field_type, sort_order, id")
    List<MedicalRecordPhrase> selectAll();

    @Select("select id, field_type, content, category, sort_order, status, created_at, updated_at from medical_record_phrases where id = #{id} limit 1")
    MedicalRecordPhrase selectById(@Param("id") Long id);

    @Insert("insert into medical_record_phrases (field_type, content, category, sort_order, status) values (#{field_type}, #{content}, #{category}, #{sort_order}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(MedicalRecordPhrase item);

    @Update("update medical_record_phrases set field_type=#{field_type}, content=#{content}, category=#{category}, sort_order=#{sort_order}, status=#{status} where id=#{id}")
    void edit(MedicalRecordPhrase item);

    @Delete("delete from medical_record_phrases where id = #{id}")
    void delete(@Param("id") Long id);
}
