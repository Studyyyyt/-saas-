package com.example.springboot.service;

import com.example.springboot.entity.PaymentChannel;
import com.example.springboot.mapper.PaymentChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentChannelService {

    @Autowired
    private PaymentChannelMapper paymentChannelMapper;

    public List<PaymentChannel> selectAll() {
        return paymentChannelMapper.selectAll();
    }

    public List<PaymentChannel> selectEnabled() {
        return paymentChannelMapper.selectEnabled();
    }

    public PaymentChannel selectById(Long id) {
        return paymentChannelMapper.selectById(id);
    }

    public void add(PaymentChannel item) {
        paymentChannelMapper.add(item);
    }

    public void edit(PaymentChannel item) {
        paymentChannelMapper.edit(item);
    }

    public void delete(Long id) {
        paymentChannelMapper.delete(id);
    }
}
