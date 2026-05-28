package com.example.springboot.service;

import org.springframework.stereotype.Component;

@Component
public class BusinessDailyAnalysisScheduler {

    // 内置AI日报调度已停用，所有AI分析请通过 n8n 外部工作流触发
    public void runDailyBusinessAnalysis() {
        System.out.println("[BUSINESS_ANALYSIS_SCHEDULE] 内置AI日报调度已停用，请通过 n8n 工作流触发分析");
    }
}
