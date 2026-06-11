package com.example.springboot.service;

import com.example.springboot.entity.PatientTimeline;
import com.example.springboot.mapper.PatientTimelineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientTimelineService {

    @Autowired
    private PatientTimelineMapper mapper;

    public List<PatientTimeline> selectByPatientId(Long patientId) { return mapper.selectByPatientId(patientId); }
    public void add(PatientTimeline t) { mapper.insert(t); }
    public void delete(Long id) { mapper.deleteById(id); }
    public void deleteByPatientId(Long patientId) { mapper.deleteByPatientId(patientId); }
}
