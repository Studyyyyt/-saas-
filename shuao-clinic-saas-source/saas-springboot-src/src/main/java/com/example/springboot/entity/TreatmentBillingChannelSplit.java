package com.example.springboot.entity;

public class TreatmentBillingChannelSplit {
    private Long payment_channel_id;
    private String payment_channel_name;
    private Double amount;

    public Long getPayment_channel_id() {
        return payment_channel_id;
    }

    public void setPayment_channel_id(Long payment_channel_id) {
        this.payment_channel_id = payment_channel_id;
    }

    public String getPayment_channel_name() {
        return payment_channel_name;
    }

    public void setPayment_channel_name(String payment_channel_name) {
        this.payment_channel_name = payment_channel_name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
