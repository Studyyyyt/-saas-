package com.example.springboot.entity;

import java.util.Date;

public class BusinessDailyAnalysis {
    private Long id;
    private Date analysis_date;
    private String analysis_status;
    private String source_type;
    private String trigger_type;
    private String model_name;
    private Integer operating_score;
    private String trend;
    private String headline;
    private String summary;
    private String metrics_json;
    private String analysis_json;
    private String raw_response;
    private String error_message;
    private Date created_at;
    private Date updated_at;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getAnalysis_date() {
        return analysis_date;
    }

    public void setAnalysis_date(Date analysis_date) {
        this.analysis_date = analysis_date;
    }

    public String getAnalysis_status() {
        return analysis_status;
    }

    public void setAnalysis_status(String analysis_status) {
        this.analysis_status = analysis_status;
    }

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public String getTrigger_type() {
        return trigger_type;
    }

    public void setTrigger_type(String trigger_type) {
        this.trigger_type = trigger_type;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public Integer getOperating_score() {
        return operating_score;
    }

    public void setOperating_score(Integer operating_score) {
        this.operating_score = operating_score;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMetrics_json() {
        return metrics_json;
    }

    public void setMetrics_json(String metrics_json) {
        this.metrics_json = metrics_json;
    }

    public String getAnalysis_json() {
        return analysis_json;
    }

    public void setAnalysis_json(String analysis_json) {
        this.analysis_json = analysis_json;
    }

    public String getRaw_response() {
        return raw_response;
    }

    public void setRaw_response(String raw_response) {
        this.raw_response = raw_response;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
