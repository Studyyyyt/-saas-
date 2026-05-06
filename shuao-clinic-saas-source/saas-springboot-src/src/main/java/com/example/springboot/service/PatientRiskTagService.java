package com.example.springboot.service;

import com.example.springboot.entity.PatientRiskTag;
import com.example.springboot.mapper.PatientRiskTagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientRiskTagService {

    @Autowired
    private PatientRiskTagMapper mapper;

    public List<PatientRiskTag> selectAll() { return mapper.selectAll(); }
    public List<PatientRiskTag> selectActiveByPatientId(Long patientId) { return mapper.selectActiveByPatientId(patientId); }
    public List<PatientRiskTag> selectActiveByPatientIds(List<Long> patientIds) {
        return patientIds == null || patientIds.isEmpty() ? List.of() : mapper.selectActiveByPatientIds(patientIds);
    }
    public PatientRiskTag selectById(Long id) { return mapper.selectById(id); }
    public void add(PatientRiskTag t) { mapper.insert(t); }
    public void update(PatientRiskTag t) { mapper.update(t); }
    public void delete(Long id) { mapper.deleteById(id); }
    public void deleteByPatientId(Long patientId) { mapper.deleteByPatientId(patientId); }
}
