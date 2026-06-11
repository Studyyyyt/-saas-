package com.example.springboot.util;

import java.util.List;

public final class MarketingChannelCatalog {

    public static final List<String> ADVERTISING_PLATFORM_OPTIONS = List.of(
            "抖音", "小红书", "大众点评", "美团", "微信", "百度", "快手", "其他"
    );

    public static final List<String> CONSULTATION_CHANNEL_OPTIONS = List.of(
            "微信", "大众点评", "美团", "电话",
            "抖音", "小红书", "百度", "快手",
            "抖音/小红书",
            "转介绍", "自然到店", "其他"
    );

    public static final List<String> CUSTOMER_SOURCE_OPTIONS = List.of(
            "微信", "大众点评", "美团", "电话",
            "抖音", "小红书", "百度", "快手",
            "抖音/小红书",
            "转介绍", "自然到店", "其他", "暂未确认"
    );

    private MarketingChannelCatalog() {
    }
}
