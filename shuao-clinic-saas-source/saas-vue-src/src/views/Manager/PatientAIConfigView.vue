<template>
  <div class="patient-ai-config-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">患者 AI 洞察配置</h1>
        <p class="page-subtitle">配置患者列表与患者 360 页面的 AI 洞察面板行为</p>
      </div>
      <el-button type="primary" icon="el-icon-check" :loading="saveLoading" @click="saveConfig">
        保存配置
      </el-button>
    </div>

    <!-- 基础设置 -->
    <div class="section-card">
      <div class="section-title">基础设置</div>
      <div class="basic-settings">
        <div class="setting-row">
          <div class="setting-item">
            <div class="setting-label">功能开关</div>
            <el-switch v-model="config.enabled" active-text="启用" inactive-text="禁用" />
          </div>
          <div class="setting-item">
            <div class="setting-label">默认展开面板</div>
            <el-switch v-model="config.autoExpand" active-text="展开" inactive-text="收起" />
            <div class="setting-hint">进入患者详情时 AI 洞察面板是否自动展开</div>
          </div>
        </div>
        <div class="setting-row">
          <div class="setting-item">
            <div class="setting-label">洞察维度</div>
            <el-checkbox-group v-model="config.dimensions">
              <el-checkbox label="treatmentRisk">治疗风险分析</el-checkbox>
              <el-checkbox label="consumptionPotential">消费潜力评估</el-checkbox>
              <el-checkbox label="returnProbability">复诊概率预测</el-checkbox>
              <el-checkbox label="churnRisk">流失风险预警</el-checkbox>
              <el-checkbox label="referralPotential">转介绍可能性</el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示词模板 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">提示词模板</div>
        <el-button size="small" icon="el-icon-document-copy" @click="resetPrompt">恢复默认</el-button>
      </div>
      <div class="form-group">
        <label class="form-label">系统提示词</label>
        <el-input v-model="config.systemPrompt" type="textarea" :rows="8" placeholder="定义 AI 分析患者数据时的角色与行为..." />
      </div>
      <div class="form-group" style="margin-top: 16px;">
        <label class="form-label">输出 JSON 格式模板</label>
        <el-input v-model="config.outputSchema" type="textarea" :rows="10" placeholder="定义 AI 返回的 JSON 结构..." />
        <div class="form-hint">前端将按照此 JSON 结构解析并渲染 AI 洞察卡片</div>
      </div>
    </div>

    <!-- 数据工具授权 -->
    <div class="section-card">
      <div class="section-title">数据工具授权</div>
      <div class="setting-hint" style="margin-bottom: 12px;">配置该 AI 功能可以查询的数据范围（权限控制）</div>
      <el-checkbox-group v-model="config.allowedTools">
        <el-checkbox-button v-for="tool in toolOptions" :key="tool.key" :label="tool.key">
          {{ tool.label }}
        </el-checkbox-button>
      </el-checkbox-group>
    </div>
  </div>
</template>

<script>
const defaultPrompt = `你是一位口腔门诊患者管理专家，擅长通过患者数据分析其治疗需求、消费行为和复诊意向。

【任务】
根据提供的患者数据，生成结构化的洞察分析报告。

【数据范围】
你可以访问以下患者数据：
- 基本信息（年龄、性别、联系方式）
- 就诊历史（历次就诊时间、治疗项目）
- 治疗记录（治疗方案、费用）
- 收费记录（支付金额、未付金额）
- 预约记录（预约履约情况）

【分析维度】
1. 治疗风险：评估治疗复杂度、并发症风险
2. 消费潜力：基于历史消费评估后续消费能力
3. 复诊概率：基于预约习惯和口腔健康状态预测
4. 流失风险：识别长期未就诊、治疗中断的患者
5. 转介绍可能性：评估患者成为推荐人的潜力

【输出要求】
严格按下方 JSON Schema 格式返回，不要包含其他内容。`;

const defaultSchema = `{
  "riskLevel": "low",
  "riskReasons": ["患者按时复诊", "治疗配合度高"],
  "suggestedActions": ["推荐年度口腔检查套餐", "跟进种植术后恢复"],
  "potentialValue": "中高消费潜力",
  "summary": "这是一位高依从性患者，建议推进正畸咨询。"
}`;

export default {
  name: 'PatientAIConfigView',
  data() {
    return {
      saveLoading: false,
      config: {
        enabled: true,
        autoExpand: true,
        dimensions: ['treatmentRisk', 'consumptionPotential', 'returnProbability', 'churnRisk'],
        systemPrompt: defaultPrompt,
        outputSchema: defaultSchema,
        allowedTools: ['query_patients', 'query_medical_records', 'query_appointments', 'query_finances']
      },
      toolOptions: [
        { key: 'query_patients', label: '患者基本信息' },
        { key: 'query_medical_records', label: '就诊历史' },
        { key: 'query_treatments', label: '治疗记录' },
        { key: 'query_finances', label: '收费记录' },
        { key: 'query_appointments', label: '预约记录' }
      ]
    };
  },
  methods: {
    resetPrompt() {
      this.$confirm('确定恢复默认提示词和格式模板吗？', '提示', {
        confirmButtonText: '恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.config.systemPrompt = defaultPrompt;
        this.config.outputSchema = defaultSchema;
        this.$message.success('已恢复默认配置');
      }).catch(() => {});
    },
    saveConfig() {
      this.saveLoading = true;
      setTimeout(() => {
        this.saveLoading = false;
        this.$message.success('配置已保存');
      }, 500);
    }
  }
};
</script>

<style scoped>
.patient-ai-config-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--apple-text-primary);
  line-height: 1.2;
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--apple-text-secondary);
}

.section-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

.basic-settings {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-row {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.setting-item {
  flex: 1;
  min-width: 280px;
}

.setting-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 8px;
}

.setting-hint {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  margin-top: 4px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.form-hint {
  font-size: 12px;
  color: var(--apple-text-tertiary);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .setting-row {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
