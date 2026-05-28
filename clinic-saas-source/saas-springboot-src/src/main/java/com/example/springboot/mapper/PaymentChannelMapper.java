package com.example.springboot.mapper;

import com.example.springboot.entity.PaymentChannel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PaymentChannelMapper {

    String BASE_COLUMNS = "id, channel_name, status, sort_order, created_at, updated_at";

    @Select("select " + BASE_COLUMNS + " from payment_channel order by status desc, sort_order asc, id desc")
    List<PaymentChannel> selectAll();

    @Select("select " + BASE_COLUMNS + " from payment_channel where status = 1 order by sort_order asc, id desc")
    List<PaymentChannel> selectEnabled();

    @Select("select " + BASE_COLUMNS + " from payment_channel where id = #{id} limit 1")
    PaymentChannel selectById(@Param("id") Long id);

    @Insert("INSERT INTO payment_channel (channel_name, status, sort_order) VALUES (#{channel_name}, #{status}, #{sort_order})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(PaymentChannel item);

    @Update("UPDATE payment_channel SET channel_name = #{channel_name}, status = #{status}, sort_order = #{sort_order} WHERE id = #{id}")
    void edit(PaymentChannel item);

    @Delete("DELETE FROM payment_channel WHERE id = #{id}")
    void delete(@Param("id") Long id);
}
