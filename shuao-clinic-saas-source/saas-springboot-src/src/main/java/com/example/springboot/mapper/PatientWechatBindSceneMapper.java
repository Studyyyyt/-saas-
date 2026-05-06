package com.example.springboot.mapper;

import com.example.springboot.entity.PatientWechatBindScene;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PatientWechatBindSceneMapper {

    @Select("select * from patient_wechat_bind_scene where patient_id = #{patientId} order by id desc limit 1")
    PatientWechatBindScene selectLatestByPatientId(@Param("patientId") Long patientId);

    @Select("select * from patient_wechat_bind_scene where scene_key = #{sceneKey} order by id desc limit 1")
    PatientWechatBindScene selectBySceneKey(@Param("sceneKey") String sceneKey);

    @Insert("INSERT INTO patient_wechat_bind_scene (patient_id, scene_key, qr_ticket, qr_url, expire_seconds, status, bound_at, bound_openid) VALUES (#{patient_id}, #{scene_key}, #{qr_ticket}, #{qr_url}, #{expire_seconds}, #{status}, #{bound_at}, #{bound_openid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PatientWechatBindScene scene);

    @Update("UPDATE patient_wechat_bind_scene SET qr_ticket=#{qr_ticket}, qr_url=#{qr_url}, expire_seconds=#{expire_seconds}, status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void updateQrInfo(PatientWechatBindScene scene);

    @Update("UPDATE patient_wechat_bind_scene SET status=#{status}, bound_at=#{bound_at}, bound_openid=#{bound_openid}, updated_at=NOW() WHERE id=#{id}")
    void markBound(PatientWechatBindScene scene);
}
