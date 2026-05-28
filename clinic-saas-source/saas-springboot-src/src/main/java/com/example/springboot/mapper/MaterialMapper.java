package com.example.springboot.mapper;

import com.example.springboot.entity.Material;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface MaterialMapper {

    @Select("SELECT * FROM materials ORDER BY updated_at DESC, id DESC")
    List<Material> selectAll();

    @Select("SELECT * FROM materials WHERE id = #{id}")
    Material selectById(@Param("id") Long id);

    @Insert("INSERT INTO materials(name, spec, brand, category_id, category_name, unit, min_stock_alert, current_stock, status, remark) VALUES(#{name}, #{spec}, #{brand}, #{category_id}, #{category_name}, #{unit}, #{min_stock_alert}, #{current_stock}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Material item);

    @Update("UPDATE materials SET name = #{name}, spec = #{spec}, brand = #{brand}, category_id = #{category_id}, category_name = #{category_name}, unit = #{unit}, min_stock_alert = #{min_stock_alert}, current_stock = #{current_stock}, status = #{status}, remark = #{remark} WHERE id = #{id}")
    void update(Material item);

    @Delete("DELETE FROM materials WHERE id = #{id}")
    void delete(@Param("id") Long id);

    /**
     * 数据库层过滤查询耗材列表
     *
     * @param keyword       关键词（模糊匹配 name, spec, brand, category_name, unit, remark）
     * @param categoryIds   分类 ID 集合（已包含子分类，空集合时不限制）
     * @param status        状态筛选（空字符串时不限制）
     * @param lowStockOnly  是否仅查询低库存（true 时：min_stock_alert > 0 AND current_stock <= min_stock_alert）
     * @return 已排序的耗材列表
     */
    @Select("<script>" +
            "SELECT *, (CASE WHEN min_stock_alert &gt; 0 AND current_stock &lt;= min_stock_alert THEN min_stock_alert - current_stock ELSE 0 END) AS alert_gap " +
            "FROM materials " +
            "<where>" +
            "  <if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "  <if test='categoryIds != null and !categoryIds.isEmpty()'>" +
            "    AND category_id IN " +
            "    <foreach item='id' collection='categoryIds' open='(' separator=',' close=')'>#{id}</foreach>" +
            "  </if>" +
            "  <if test='lowStockOnly != null and lowStockOnly == true'>" +
            "    AND min_stock_alert &gt; 0 AND current_stock &lt;= min_stock_alert" +
            "  </if>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (" +
            "      name LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR spec LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR brand LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR category_name LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR unit LIKE CONCAT('%', #{keyword}, '%') " +
            "      OR remark LIKE CONCAT('%', #{keyword}, '%')" +
            "    )" +
            "  </if>" +
            "</where>" +
            "ORDER BY alert_gap DESC, updated_at DESC, id DESC" +
            "</script>")
    List<Material> search(@Param("keyword") String keyword,
                          @Param("categoryIds") Set<Long> categoryIds,
                          @Param("status") String status,
                          @Param("lowStockOnly") Boolean lowStockOnly);
}
