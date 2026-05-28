package com.example.springboot.service;

import com.example.springboot.entity.PatientImage;
import com.example.springboot.mapper.PatientImageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class PatientImageService {
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/.local/uploads/patient-images/";

    @Autowired
    private PatientImageMapper mapper;

    public List<PatientImage> selectByPatientId(Long patientId) { return mapper.selectByPatientId(patientId); }
    public List<PatientImage> selectSentByPatientId(Long patientId) { return mapper.selectSentByPatientId(patientId); }
    public PatientImage selectById(Long id) { return mapper.selectById(id); }
    public void add(PatientImage image) { mapper.insert(image); }
    public PatientImage markSentToPatient(Long id) {
        PatientImage image = mapper.selectById(id);
        if (image == null) {
            return null;
        }
        image.setSent_to_patient(Boolean.TRUE);
        image.setSent_at(new java.util.Date());
        mapper.updateSendStatus(image);
        return mapper.selectById(id);
    }
    public void delete(Long id) {
        PatientImage image = mapper.selectById(id);
        if (image != null && image.getFile_path() != null && !image.getFile_path().trim().isEmpty()) {
            new File(UPLOAD_DIR + image.getFile_path()).delete();
        }
        mapper.deleteById(id);
    }
    public void deleteByPatientId(Long patientId) {
        List<PatientImage> images = mapper.selectByPatientId(patientId);
        if (images != null) {
            for (PatientImage image : images) {
                if (image != null && image.getId() != null) {
                    delete(image.getId());
                }
            }
        }
        mapper.deleteByPatientId(patientId);
    }
}
