package com.example.springboot.mapper;

import com.example.springboot.entity.Account;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccountMapper {

    @Select("select id, username, password, name, role, wechat_openid from users ")
    List<Account> searchAccounts();

    @Select("select id, username, password, name, role, wechat_openid from users where id = #{id}")
    List<Account> selectById(Long id);

    @Select("select count(*) from users where username = #{username}")
    int countByUsername(@Param("username") String username);

    @Insert("INSERT INTO users (username, password,name, role, wechat_openid) VALUES (#{username}, #{password},#{name}, #{role}, #{wechat_openid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addAccount(Account account);

    @Update({
            "<script>",
            "UPDATE users",
            "<set>",
            "username = #{username},",
            "name = #{name},",
            "role = #{role},",
            "wechat_openid = #{wechat_openid}",
            "<if test='password != null and password != \"\"'>, password = #{password}</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    void updateAccount(Account account);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteAccount(@Param("id") int id);

    @Delete({
            "<script>",
            "DELETE FROM users WHERE id IN ",
            "<foreach item='id' collection='list' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    void deleteAccountBatch(List<Long> ids);

    @Update("UPDATE users SET wechat_openid = #{wechat_openid} WHERE id = #{id}")
    void bindWechatOpenid(Account account);

    @Select("select id, username, password, name, role, wechat_openid from users where name = #{name}")
    List<Account> selectByName(String name);

    @Select("select id, username, password, name, role, wechat_openid from users where wechat_openid = #{openid} limit 1")
    Account selectByWechatOpenid(@Param("openid") String openid);
}
