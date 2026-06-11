package com.example.springboot.entity;

public class ConsultationQuery {
    private Integer page;
    private Integer size;
    private String keyword;
    private String startTime;
    private String endTime;
    private String rangePreset;
    private String channel;
    private String chiefProject;
    private String intentLevel;
    private String handlingResult;
    private Boolean hasDeal;
    private Long createdBy;
    private String name;
    private String phone;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRangePreset() {
        return rangePreset;
    }

    public void setRangePreset(String rangePreset) {
        this.rangePreset = rangePreset;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChiefProject() {
        return chiefProject;
    }

    public void setChiefProject(String chiefProject) {
        this.chiefProject = chiefProject;
    }

    public String getIntentLevel() {
        return intentLevel;
    }

    public void setIntentLevel(String intentLevel) {
        this.intentLevel = intentLevel;
    }

    public String getHandlingResult() {
        return handlingResult;
    }

    public void setHandlingResult(String handlingResult) {
        this.handlingResult = handlingResult;
    }

    public Boolean getHasDeal() {
        return hasDeal;
    }

    public void setHasDeal(Boolean hasDeal) {
        this.hasDeal = hasDeal;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
