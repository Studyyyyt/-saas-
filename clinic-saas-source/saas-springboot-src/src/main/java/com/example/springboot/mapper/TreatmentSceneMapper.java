package com.example.springboot.mapper;

import com.example.springboot.entity.TreatmentScene;
import com.example.springboot.entity.TreatmentSceneStep;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 治疗场景 Mapper
 */
@Mapper
public interface TreatmentSceneMapper {

    @Select("SELECT * FROM treatment_scene WHERE enabled = 1 ORDER BY sort_order, id")
    List<TreatmentScene> selectAllEnabled();

    @Select("SELECT * FROM treatment_scene ORDER BY sort_order, id")
    List<TreatmentScene> selectAll();

    @Select("SELECT * FROM treatment_scene WHERE id = #{id}")
    TreatmentScene selectById(Long id);

    @Insert("INSERT INTO treatment_scene (name, category, level, enabled, sort_order) " +
            "VALUES (#{name}, #{category}, #{level}, #{enabled}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TreatmentScene scene);

    @Update("UPDATE treatment_scene SET name = #{name}, category = #{category}, level = #{level}, " +
            "enabled = #{enabled}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(TreatmentScene scene);

    @Delete("DELETE FROM treatment_scene WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM treatment_scene_step WHERE scene_id = #{sceneId} AND enabled = 1 ORDER BY sort_order, id")
    List<TreatmentSceneStep> selectStepsBySceneId(Long sceneId);

    @Select("SELECT * FROM treatment_scene_step WHERE scene_id = #{sceneId} ORDER BY sort_order, id")
    List<TreatmentSceneStep> selectAllStepsBySceneId(Long sceneId);

    @Insert("INSERT INTO treatment_scene_step (scene_id, name, sort_order, forbidden_keywords, required_keywords, enabled) " +
            "VALUES (#{sceneId}, #{name}, #{sortOrder}, #{forbiddenKeywords}, #{requiredKeywords}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertStep(TreatmentSceneStep step);

    @Update("UPDATE treatment_scene_step SET name = #{name}, sort_order = #{sortOrder}, " +
            "forbidden_keywords = #{forbiddenKeywords}, required_keywords = #{requiredKeywords}, " +
            "enabled = #{enabled} WHERE id = #{id}")
    int updateStep(TreatmentSceneStep step);

    @Delete("DELETE FROM treatment_scene_step WHERE id = #{id}")
    int deleteStepById(Long id);

    @Delete("DELETE FROM treatment_scene_step WHERE scene_id = #{sceneId}")
    int deleteStepsBySceneId(Long sceneId);
}
