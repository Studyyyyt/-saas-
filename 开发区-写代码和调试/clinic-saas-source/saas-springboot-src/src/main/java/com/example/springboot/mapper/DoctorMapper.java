package com.example.springboot.mapper;

import com.example.springboot.entity.Doctor;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DoctorMapper {

    // 查询所有医生排班信息
    @Select("SELECT * FROM doctors")
    List<Doctor> selectAll();

    @Select("<script>" +
            "SELECT * FROM doctors " +
            "<where>" +
            "  <if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "  <if test='scheduleDate != null and scheduleDate != \"\"'>AND schedule_date = #{scheduleDate}</if>" +
            "</where>" +
            "ORDER BY schedule_date DESC, doctor_name ASC" +
            "</script>")
    List<Doctor> searchDoctors(@Param("status") String status, @Param("scheduleDate") String scheduleDate);


    // 根据状态查询所有医生排班信息
    @Select("SELECT * FROM doctors WHERE status = #{status}")
    List<Doctor> selectByStatus(@Param("status") String status);

    // 根据ID查询医生排班信息
    @Select("SELECT * FROM doctors WHERE id = #{id}")
    List<Doctor> selectById(@Param("id") Long id);

    // 根据ID和状态查询医生排班信息
    @Select("SELECT * FROM doctors WHERE id = #{id} AND status = #{status}")
    Doctor selectByIdAndStatus(@Param("id") Long id, @Param("status") String status);

    // 根据医生姓名查询医生排班信息
    @Select("SELECT * FROM doctors WHERE doctor_name = #{name}")
    List<Doctor> selectByName(@Param("name") String name);

    // 根据医生姓名和状态查询医生排班信息
    @Select("SELECT * FROM doctors WHERE doctor_name = #{name} AND status = #{status}")
    List<Doctor> selectByNameAndStatus(@Param("name") String name, @Param("status") String status);

    // 按日期范围查询排班
    @Select("SELECT * FROM doctors WHERE schedule_date >= #{startDate} AND schedule_date <= #{endDate} ORDER BY schedule_date, doctor_name")
    List<Doctor> selectByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // 插入医生排班信息
    @Insert("INSERT INTO doctors (doctor_name, schedule_date, start_time, end_time, status, shift_type) " +
            "VALUES (#{doctor_name}, #{schedule_date}, #{start_time}, #{end_time}, #{status}, #{shift_type})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Doctor doctor);

    // 更新医生排班信息（示例中仅更新状态，可根据需要扩展）
    @Update("UPDATE doctors SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    // 批量删除医生排班信息
    @Delete({
            "<script>",
            "DELETE FROM doctors WHERE id IN ",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    void deleteByIds(@Param("ids") List<Long> ids);

    @Update("UPDATE doctors SET doctor_name = #{doctor_name}, schedule_date = #{schedule_date}, start_time = #{start_time}, end_time = #{end_time}, status = #{status}, shift_type = #{shift_type} WHERE id = #{id}")
    void update(Doctor doctor);

    @Delete("DELETE FROM doctors WHERE id = #{id}")
    void deleteById(Long id);




}
