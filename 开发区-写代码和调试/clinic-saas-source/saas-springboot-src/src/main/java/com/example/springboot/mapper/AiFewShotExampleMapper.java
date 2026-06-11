package com.example.springboot.mapper;

import com.example.springboot.entity.AiFewShotExample;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiFewShotExampleMapper {

    @Results(id = "fewShotMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "templateId", column = "template_id"),
            @Result(property = "inputContent", column = "input_content"),
            @Result(property = "outputContent", column = "output_content"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createTime", column = "create_time")
    })
    @Select("SELECT id, template_id AS templateId, input_content AS inputContent, output_content AS outputContent, sort_order AS sortOrder, create_time AS createTime FROM ai_few_shot_example WHERE template_id = #{templateId} ORDER BY sort_order, id")
    List<AiFewShotExample> selectByTemplateId(Long templateId);

    @Insert("INSERT INTO ai_few_shot_example (template_id, input_content, output_content, sort_order) VALUES (#{templateId}, #{inputContent}, #{outputContent}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiFewShotExample example);

    @Delete("DELETE FROM ai_few_shot_example WHERE template_id = #{templateId}")
    void deleteByTemplateId(Long templateId);
}
