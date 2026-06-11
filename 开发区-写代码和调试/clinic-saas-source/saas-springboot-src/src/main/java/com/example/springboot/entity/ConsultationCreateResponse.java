package com.example.springboot.entity;

public class ConsultationCreateResponse {
    private ConsultationRecord record;
    private ConsultationPromptFlags promptFlags;
    private int weekCount;

    public ConsultationRecord getRecord() {
        return record;
    }

    public void setRecord(ConsultationRecord record) {
        this.record = record;
    }

    public ConsultationPromptFlags getPromptFlags() {
        return promptFlags;
    }

    public void setPromptFlags(ConsultationPromptFlags promptFlags) {
        this.promptFlags = promptFlags;
    }

    public int getWeekCount() {
        return weekCount;
    }

    public void setWeekCount(int weekCount) {
        this.weekCount = weekCount;
    }
}
