package com.example.springboot.entity;

public class PatientWorkbenchQuery {
    private Integer page;
    private Integer size;
    private String searchType;
    private String keyword;
    private String quickScope;
    private String groupKey;
    private String doctorFilter;
    private String sourceFilter;
    private String relationFilter;
    private String arrearsFilter;
    private String sortMode;
    private Long doctorAccountId;
    private String doctorName;

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

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getQuickScope() {
        return quickScope;
    }

    public void setQuickScope(String quickScope) {
        this.quickScope = quickScope;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getDoctorFilter() {
        return doctorFilter;
    }

    public void setDoctorFilter(String doctorFilter) {
        this.doctorFilter = doctorFilter;
    }

    public String getSourceFilter() {
        return sourceFilter;
    }

    public void setSourceFilter(String sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    public String getRelationFilter() {
        return relationFilter;
    }

    public void setRelationFilter(String relationFilter) {
        this.relationFilter = relationFilter;
    }

    public String getArrearsFilter() {
        return arrearsFilter;
    }

    public void setArrearsFilter(String arrearsFilter) {
        this.arrearsFilter = arrearsFilter;
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
    }

    public Long getDoctorAccountId() {
        return doctorAccountId;
    }

    public void setDoctorAccountId(Long doctorAccountId) {
        this.doctorAccountId = doctorAccountId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
}
