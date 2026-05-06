package com.example.springboot.mapper;

import com.example.springboot.entity.Inventory;
import com.example.springboot.entity.Purchase;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PurchaseMapper {


    @Select("select * from `purchases` ")
    List<Purchase> selectAll();

    @Select("select * from purchases where id = #{id}")
    List<Purchase> selectById(@Param("id") Long id);

    @Select("select * from purchases where product_name = #{name}")
    List<Purchase> selectByProductName(String name);

    @Select("select * from purchases where category = #{category}")
    List<Purchase> selectByCategory(@Param("category") String category);

    @Select("select * from purchases where brand = #{brand}")
    List<Purchase> selectByBrand(@Param("brand") String brand);

    @Select("select * from purchases where supplier = #{supplier}")
    List<Purchase> selectBySupplier(@Param("supplier") String supplier);

    @Insert("INSERT INTO purchases (product_name, category, brand, supplier, specification, unit, quantity, price, status,createdate,purchasedate,indate) " +
            "VALUES (#{product_name}, #{category}, #{brand}, #{supplier}, #{specification}, #{unit}, #{quantity}, #{price},  #{status},#{createdate}, #{purchasedate}, #{indate})")
    void addPurchase(Purchase purchase);

    @Update("UPDATE purchases SET product_name=#{product_name}, category=#{category}, brand=#{brand}, supplier=#{supplier}, specification=#{specification}, unit=#{unit}, quantity=#{quantity}, price=#{price}, status=#{status}, createdate=#{createdate}, purchasedate=#{purchasedate}, indate=#{indate} WHERE id=#{id}")
    void updatePurchase(Purchase purchase);

    @Delete("DELETE FROM purchases WHERE id = #{id}")
    void deletePurchase(@Param("id") Long id);

    @Delete({
            "<script>",
            "DELETE FROM purchases WHERE id IN ",
            "<foreach item='id' collection='list' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    void deletePurchaseBatch(@Param("list") List<Long> ids);

    @Select("<script>" +
            "SELECT * FROM purchases " +
            "WHERE ${type} LIKE CONCAT('%', #{keyword}, '%') " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Purchase> searchPurchase(@Param("type") String type, @Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

//    @Update("UPDATE purchases SET product_name=#{product_name}, category=#{category}, brand=#{brand}, supplier=#{supplier}, specification=#{specification}, unit=#{unit}, quantity=#{quantity}, price=#{price}, status=#{status}, createdate=#{createdate}, purchasedate=#{purchasedate}, indate=#{indate} WHERE id=#{id}")
//    void updatePurchase(Purchase purchase);

    @Select("SELECT * FROM purchases WHERE id=#{id}")
    Purchase getPurchaseById(@Param("id") int id);


}
