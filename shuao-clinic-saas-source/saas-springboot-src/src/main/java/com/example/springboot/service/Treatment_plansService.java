package com.example.springboot.service;

import com.example.springboot.entity.Treatment_plans;
import com.example.springboot.mapper.TreatmentMapper;
import com.example.springboot.mapper.Treatment_plansMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Treatment_plansService {

    @Autowired
    private Treatment_plansMapper treatment_plansMapper;

    public List<Treatment_plans> selectAll() {
        return treatment_plansMapper.selectAll();
    }
}
