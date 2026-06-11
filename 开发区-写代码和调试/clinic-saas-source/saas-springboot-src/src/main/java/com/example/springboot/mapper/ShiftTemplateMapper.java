package com.example.springboot.mapper;

import com.example.springboot.entity.ShiftTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 排班模板 Mapper
 */
@Mapper
public interface ShiftTemplateMapper {

    @Select("SELECT id, name, doctor_name, pattern_json, created_at FROM shift_template ORDER BY id")
    List<ShiftTemplate> selectAll();
}
