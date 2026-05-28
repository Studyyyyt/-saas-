package com.example.springboot.service;

import com.example.springboot.entity.Doctor;
import com.example.springboot.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    public List<Doctor> selectAll() {
        return doctorMapper.selectAll();
    }

    /**
     * 多条件组合查询医生排班
     *
     * @param status 排班状态
     * @param scheduleDate 排班日期，格式 yyyy-MM-dd
     * @return 医生排班列表
     */
    public List<Doctor> searchDoctors(String status, String scheduleDate) {
        return doctorMapper.searchDoctors(status, scheduleDate);
    }

//    public List<Doctor> getAllDoctors() {
//        return doctorMapper.selectAllDoctors();
//    }
    public  List<Doctor> selectById(Long id){
        return doctorMapper.selectById(id);
    }

    public List<Doctor> getDoctorsByStatus(String status) {
        return doctorMapper.selectByStatus(status);
    }


    public Doctor getDoctorByIdAndStatus(Long id, String status) {
        return doctorMapper.selectByIdAndStatus(id, status);
    }

    public List<Doctor> getDoctorsByName(String name) {
        return doctorMapper.selectByName(name);
    }

    public List<Doctor> getDoctorsByNameAndStatus(String name, String status) {
        return doctorMapper.selectByNameAndStatus(name, status);
    }

    public void addDoctor(Doctor doctor) {
        doctorMapper.insert(doctor);
    }

    public void updateDoctorStatus(Long id, String status) {
        doctorMapper.updateStatus(id, status);
    }

    public void updateDoctor(Doctor doctor) {
        doctorMapper.update(doctor);
    }

    public void deleteDoctorById(Long id) {
        doctorMapper.deleteById(id);
    }

    public void deleteDoctorsByIds(List<Long> ids) {
        doctorMapper.deleteByIds(ids);
    }

    public List<Doctor> getSchedulesByDateRange(String startDate, String endDate) {
        return doctorMapper.selectByDateRange(startDate, endDate);
    }

    @Transactional
    public void batchSaveSchedules(List<Doctor> schedules) {
        if (schedules == null || schedules.isEmpty()) return;
        for (Doctor doctor : schedules) {
            if (doctor.getId() != null) {
                doctorMapper.update(doctor);
            } else {
                doctorMapper.insert(doctor);
            }
        }
    }
}
