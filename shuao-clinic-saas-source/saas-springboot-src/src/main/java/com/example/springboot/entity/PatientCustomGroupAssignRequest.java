package com.example.springboot.entity;

import java.util.List;

public class PatientCustomGroupAssignRequest {
    private Long group_id;
    private List<Long> patient_ids;

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public List<Long> getPatient_ids() {
        return patient_ids;
    }

    public void setPatient_ids(List<Long> patient_ids) {
        this.patient_ids = patient_ids;
    }
}
