package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.PaymentChannel;
import com.example.springboot.service.PaymentChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment-channels")
public class PaymentChannelController {

    @Autowired
    private PaymentChannelService paymentChannelService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(paymentChannelService.selectAll());
    }

    @GetMapping("/selectEnabled")
    public Result selectEnabled() {
        return Result.success(paymentChannelService.selectEnabled());
    }

    @PostMapping("/add")
    public Result add(@RequestBody PaymentChannel item) {
        if (item == null || item.getChannel_name() == null || item.getChannel_name().trim().isEmpty()) {
            return Result.error("收款渠道名称不能为空");
        }
        paymentChannelService.add(item);
        return Result.success("新增成功");
    }

    @PutMapping("/edit")
    public Result edit(@RequestBody PaymentChannel item) {
        if (item == null || item.getId() == null) {
            return Result.error("收款渠道ID不能为空");
        }
        if (item.getChannel_name() == null || item.getChannel_name().trim().isEmpty()) {
            return Result.error("收款渠道名称不能为空");
        }
        paymentChannelService.edit(item);
        return Result.success("编辑成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        paymentChannelService.delete(id);
        return Result.success("删除成功");
    }
}
