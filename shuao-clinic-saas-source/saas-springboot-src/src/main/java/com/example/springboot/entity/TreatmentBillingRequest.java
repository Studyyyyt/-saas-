package com.example.springboot.entity;

import java.util.List;

public class TreatmentBillingRequest {
    private String date;
    private Double amount;
    private String remark;
    private List<TreatmentBillingChannelSplit> channel_splits;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<TreatmentBillingChannelSplit> getChannel_splits() {
        return channel_splits;
    }

    public void setChannel_splits(List<TreatmentBillingChannelSplit> channel_splits) {
        this.channel_splits = channel_splits;
    }
}
