package com.example.springboot.mapper;

import com.example.springboot.entity.PortalAccessToken;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PortalAccessTokenMapper {

    @Insert("INSERT INTO portal_access_token (token, token_type, subject_id, payload, expires_at, consumed_at) " +
            "VALUES (#{token}, #{token_type}, #{subject_id}, #{payload}, #{expires_at}, #{consumed_at})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PortalAccessToken token);

    @Select("SELECT * FROM portal_access_token " +
            "WHERE token = #{token} AND token_type = #{tokenType} " +
            "AND expires_at > NOW() AND consumed_at IS NULL LIMIT 1")
    PortalAccessToken selectActive(@Param("token") String token, @Param("tokenType") String tokenType);

    @Update("UPDATE portal_access_token SET consumed_at = NOW() WHERE id = #{id} AND consumed_at IS NULL")
    void consume(@Param("id") Long id);
}
