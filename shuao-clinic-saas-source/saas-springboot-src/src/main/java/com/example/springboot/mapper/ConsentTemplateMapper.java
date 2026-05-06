package com.example.springboot.mapper;

import com.example.springboot.entity.ConsentTemplate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ConsentTemplateMapper {

    String BASE_COLUMNS = "id, title, content, remark, status, sort_order, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from consent_template order by status desc, sort_order asc, id desc")
    List<ConsentTemplate> selectAll();

    @Select("select " + BASE_COLUMNS + " from consent_template where status = 1 order by sort_order asc, id desc")
    List<ConsentTemplate> selectEnabled();

    @Select("select " + BASE_COLUMNS + " from consent_template where id = #{id} limit 1")
    ConsentTemplate selectById(@Param("id") Long id);

    @Insert("INSERT INTO consent_template (title, content, remark, status, sort_order) VALUES (#{title}, #{content}, #{remark}, #{status}, #{sort_order})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(ConsentTemplate item);

    @Update("UPDATE consent_template SET title=#{title}, content=#{content}, remark=#{remark}, status=#{status}, sort_order=#{sort_order} WHERE id=#{id}")
    void edit(ConsentTemplate item);

    @Delete("DELETE FROM consent_template WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
