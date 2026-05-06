package com.example.springboot.entity;

import java.util.Date;

public class BusinessAlertLog {
    private Long id;
    private Date alert_date;
    private String alert_code;
    private String alert_level;
    private String alert_title;
    private String alert_message;
    private String metric_name;
    private Double current_value;
    private Double baseline_value;
    private Double change_rate;
    private String suggested_action;
    private String source_type;
    private String trigger_type;
    private Date created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getAlert_date() { return alert_date; }
    public void setAlert_date(Date alert_date) { this.alert_date = alert_date; }
    public String getAlert_code() { return alert_code; }
    public void setAlert_code(String alert_code) { this.alert_code = alert_code; }
    public String getAlert_level() { return alert_level; }
    public void setAlert_level(String alert_level) { this.alert_level = alert_level; }
    public String getAlert_title() { return alert_title; }
    public void setAlert_title(String alert_title) { this.alert_title = alert_title; }
    public String getAlert_message() { return alert_message; }
    public void setAlert_message(String alert_message) { this.alert_message = alert_message; }
    public String getMetric_name() { return metric_name; }
    public void setMetric_name(String metric_name) { this.metric_name = metric_name; }
    public Double getCurrent_value() { return current_value; }
    public void setCurrent_value(Double current_value) { this.current_value = current_value; }
    public Double getBaseline_value() { return baseline_value; }
    public void setBaseline_value(Double baseline_value) { this.baseline_value = baseline_value; }
    public Double getChange_rate() { return change_rate; }
    public void setChange_rate(Double change_rate) { this.change_rate = change_rate; }
    public String getSuggested_action() { return suggested_action; }
    public void setSuggested_action(String suggested_action) { this.suggested_action = suggested_action; }
    public String getSource_type() { return source_type; }
    public void setSource_type(String source_type) { this.source_type = source_type; }
    public String getTrigger_type() { return trigger_type; }
    public void setTrigger_type(String trigger_type) { this.trigger_type = trigger_type; }
    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
}
