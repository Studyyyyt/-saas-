package com.example.springboot.mapper;

import com.example.springboot.entity.Inventory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
//操作数据库的接口
public interface InventoryMapper {

    @Select("select * from `inventory` ")
    List<Inventory> selectAll();

    @Select("<script>" +
            "SELECT * FROM inventory " +
            "<where>" +
            "  <if test='category != null and category != \"\"'>AND category = #{category}</if>" +
            "  <if test='brand != null and brand != \"\"'>AND brand = #{brand}</if>" +
            "  <if test='supplier != null and supplier != \"\"'>AND supplier = #{supplier}</if>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (product_name LIKE CONCAT('%', #{keyword}, '%') OR category LIKE CONCAT('%', #{keyword}, '%') OR brand LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "</where>" +
            "ORDER BY id DESC" +
            "</script>")
    List<Inventory> searchInventory(@Param("category") String category,
                                    @Param("brand") String brand,
                                    @Param("supplier") String supplier,
                                    @Param("keyword") String keyword);

    @Select("select * from inventory where id = #{id}")
    List<Inventory> selectById(@Param("id") Long id);

    @Select("select * from inventory where product_name = #{name}")
    List<Inventory> selectByProductName(String name);

    @Select("select * from inventory where category = #{category}")
    List<Inventory> selectByCategory(@Param("category") String category);

    @Select("select * from inventory where brand = #{brand}")
    List<Inventory> selectByBrand(@Param("brand") String brand);

    @Select("select * from inventory where supplier = #{supplier}")
    List<Inventory> selectBySupplier(@Param("supplier") String supplier);

    @Insert("INSERT INTO inventory (product_name, category, brand, supplier, specification, unit, quantity, price, product_batch) " +
            "VALUES (#{product_name}, #{category}, #{brand}, #{supplier}, #{specification}, #{unit}, #{quantity}, #{price}, #{product_batch})")
    void addInventory(Inventory inventory);

    @Update("UPDATE inventory SET product_name = #{product_name}, category = #{category}, brand = #{brand}, supplier = #{supplier}, " +
            "specification = #{specification}, unit = #{unit}, quantity = #{quantity}, price = #{price}, product_batch = #{product_batch} " +
            "WHERE id = #{id}")
    void editInventory(Inventory inventory);

    @Delete("DELETE FROM inventory WHERE id = #{id}")
    void deleteInventory(@Param("id") Long id);

    @Delete({
            "<script>",
            "DELETE FROM inventory WHERE id IN ",
            "<foreach item='id' collection='list' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    void deleteInventoryBatch(@Param("list") List<Long> ids);

    @Select("select * from inventory where id = #{id}")
    Inventory getInventoryItemById(Long id);

    @Update("UPDATE inventory SET quantity = #{quantity} WHERE id = #{id}")
    void updateInventoryItem(Inventory inventory);

    @Insert({
            "<script>",
            "INSERT INTO inventory (product_name, category, brand, supplier, specification, unit, quantity, price, product_batch) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.product_name}, #{item.category}, #{item.brand}, #{item.supplier}, #{item.specification}, #{item.unit}, #{item.quantity}, #{item.price}, #{item.product_batch})",
            "</foreach>",
            "</script>"
    })
    void addInventoryBatch(@Param("list")List<Inventory> inventoryList);


    // 添加新的方法来查询库存数量少于10的物品
    @Select("SELECT * FROM inventory WHERE quantity < 10")
    List<Inventory> selectLowStock();

    @Select("select * from inventory where id = #{id} and quantity < 10")
    List<Inventory> select1ById(Long id);

    @Select("select * from inventory where product_name = #{name} and quantity < 10")
    List<Inventory> select1ByProductName(String name);

    @Select("select * from inventory where category = #{category} and quantity < 10")
    List<Inventory> select1ByCategory(@Param("category") String category);

    @Select("select * from inventory where brand = #{brand} and quantity < 10")
    List<Inventory> select1ByBrand(@Param("brand") String brand);

    @Select("select * from inventory where supplier = #{supplier} and quantity < 10")
    List<Inventory> select1BySupplier(@Param("supplier") String supplier);
}
