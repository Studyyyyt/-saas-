<template>
  <div class="help-detail-page">
    <div class="page-header">
      <h1 class="page-title">患者洞察配置</h1>
      <p class="page-subtitle">基于 AI 分析患者数据，自动生成患者画像与就诊建议</p>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-info" /> 功能概述</div>
      <div class="help-content">
        <p>患者洞察功能基于 AI 分析患者的就诊历史、治疗记录、沟通内容和行为习惯，自动生成患者画像和就诊建议，帮助医生在接诊前快速了解患者背景，提升沟通效率和诊疗质量。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-tools" /> 配置说明</div>
      <div class="help-content">
        <ol>
          <li>进入「系统设置 - AI 智能中心 - 患者洞察」页面。</li>
          <li>开启「功能开关」启用患者洞察功能。</li>
          <li>配置洞察维度：
            <ul>
              <li><strong>就诊偏好</strong>：分析患者对特定医生、治疗方式、预约时段的偏好</li>
              <li><strong>治疗依从性</strong>：评估患者按时复诊、遵医嘱的情况</li>
              <li><strong>沟通风格</strong>：判断患者偏好详细解释还是简明扼要</li>
              <li><strong>风险提示</strong>：识别有流失风险、投诉风险或依从性低的患者</li>
            </ul>
          </li>
          <li>设置 AI 生成提示词和约束条件。</li>
          <li>点击「保存配置」使设置生效。</li>
        </ol>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-user" /> 使用位置</div>
      <div class="help-content">
        <p>配置完成后，在以下页面可以查看 AI 生成的患者洞察：</p>
        <ul>
          <li><strong>患者 360° 视图</strong>：在患者详情页右侧显示洞察面板，展示该患者的综合画像和本次就诊建议。</li>
          <li><strong>病历编辑页面</strong>：可查看与该次就诊相关的历史洞察摘要。</li>
        </ul>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-warning-outline" /> 隐私与合规</div>
      <div class="help-content">
        <ul>
          <li>患者洞察仅基于门诊内部数据生成，不会将患者信息发送到外部 AI 服务（除非已配置并启用外部模型）。</li>
          <li>AI 生成的洞察仅供参考，不能替代医生的专业判断。</li>
          <li>建议定期审查洞察内容的准确性，避免因数据偏差导致误判。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HelpPatientInsightView'
}
</script>

<style scoped>
.help-detail-page {
  padding: 0 0 32px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--apple-text-primary);
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

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title i {
  font-size: 18px;
  color: var(--apple-accent);
}

.help-content p {
  font-size: 13px;
  color: var(--apple-text-secondary);
  line-height: 1.8;
  margin: 8px 0;
}

.help-content ol,
.help-content ul {
  font-size: 13px;
  color: var(--apple-text-secondary);
  line-height: 1.8;
  padding-left: 20px;
  margin: 8px 0;
}

.help-content li {
  margin: 4px 0;
}
</style>
