package com.example.springboot.mapper;

import com.example.springboot.entity.Treatment_plans;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface Treatment_plansMapper {

    @Select("select * from treatment_plans")
    List<Treatment_plans> selectAll();
}
