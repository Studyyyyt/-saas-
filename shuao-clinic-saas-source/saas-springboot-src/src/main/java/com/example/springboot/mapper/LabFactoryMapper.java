package com.example.springboot.mapper;

import com.example.springboot.entity.LabFactory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LabFactoryMapper {

    @Select("SELECT * FROM lab_factories ORDER BY updated_at DESC, id DESC")
    List<LabFactory> selectAll();

    @Select("SELECT * FROM lab_factories WHERE status = '合作中' ORDER BY updated_at DESC, id DESC")
    List<LabFactory> selectEnabled();

    @Select("SELECT * FROM lab_factories WHERE id = #{id}")
    LabFactory selectById(@Param("id") Long id);

    @Insert("INSERT INTO lab_factories(name, contact_name, contact_phone, address, cooperation_start_date, status) VALUES(#{name}, #{contact_name}, #{contact_phone}, #{address}, #{cooperation_start_date}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabFactory item);

    @Update("UPDATE lab_factories SET name = #{name}, contact_name = #{contact_name}, contact_phone = #{contact_phone}, address = #{address}, cooperation_start_date = #{cooperation_start_date}, status = #{status} WHERE id = #{id}")
    void update(LabFactory item);

    @Delete("DELETE FROM lab_factories WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
