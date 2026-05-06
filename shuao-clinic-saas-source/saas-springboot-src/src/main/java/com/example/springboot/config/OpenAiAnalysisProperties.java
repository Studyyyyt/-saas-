package com.example.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiAnalysisProperties {
    private boolean enabled;
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private boolean disableResponseStorage = true;
    private BusinessAnalysis businessAnalysis = new BusinessAnalysis();

    @Data
    public static class BusinessAnalysis {
        private String model = "gpt-5.4-mini";
        private String reasoningEffort = "medium";
        private Integer maxOutputTokens = 3000;
        private String dailyCron = "0 10 0 * * *";
        private String weeklyCron = "0 20 0 * * MON";
        private String monthlyCron = "0 30 0 1 * *";
        private String alertCron = "0 40 0 * * *";
    }
}
