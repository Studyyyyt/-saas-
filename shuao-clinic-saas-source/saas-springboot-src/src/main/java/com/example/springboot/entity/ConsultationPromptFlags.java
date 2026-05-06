package com.example.springboot.entity;

public class ConsultationPromptFlags {
    private boolean phoneMatchedPatient;
    private boolean phoneHasOpenConsultation;
    private Long matchedPatientId;
    private String matchedPatientName;
    private int openConsultationCount;

    public boolean isPhoneMatchedPatient() {
        return phoneMatchedPatient;
    }

    public void setPhoneMatchedPatient(boolean phoneMatchedPatient) {
        this.phoneMatchedPatient = phoneMatchedPatient;
    }

    public boolean isPhoneHasOpenConsultation() {
        return phoneHasOpenConsultation;
    }

    public void setPhoneHasOpenConsultation(boolean phoneHasOpenConsultation) {
        this.phoneHasOpenConsultation = phoneHasOpenConsultation;
    }

    public Long getMatchedPatientId() {
        return matchedPatientId;
    }

    public void setMatchedPatientId(Long matchedPatientId) {
        this.matchedPatientId = matchedPatientId;
    }

    public String getMatchedPatientName() {
        return matchedPatientName;
    }

    public void setMatchedPatientName(String matchedPatientName) {
        this.matchedPatientName = matchedPatientName;
    }

    public int getOpenConsultationCount() {
        return openConsultationCount;
    }

    public void setOpenConsultationCount(int openConsultationCount) {
        this.openConsultationCount = openConsultationCount;
    }
}
