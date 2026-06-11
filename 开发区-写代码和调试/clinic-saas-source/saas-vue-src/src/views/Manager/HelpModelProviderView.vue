<template>
  <div class="help-detail-page">
    <div class="page-header">
      <h1 class="page-title">模型供应商配置</h1>
      <p class="page-subtitle">配置系统调用的 AI 大模型供应商信息</p>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-info" /> 功能概述</div>
      <div class="help-content">
        <p>模型供应商配置用于设置系统调用的 AI 大模型 API 信息，包括 API 地址、模型名称和访问密钥。配置正确后，系统内的 AI 功能（病历扩写、患者洞察等）才能正常调用大模型生成内容。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-check" /> 支持的供应商</div>
      <div class="help-content">
        <table class="help-table">
          <tr><th>供应商</th><th>适用场景</th><th>备注</th></tr>
          <tr>
            <td><strong>DeepSeek</strong></td>
            <td>病历扩写、结构化输出</td>
            <td>国内模型，性价比高，对中文病历理解和生成效果优秀，推荐作为首选。</td>
          </tr>
          <tr>
            <td><strong>OpenAI</strong></td>
            <td>通用场景、复杂推理</td>
            <td>GPT-4 / GPT-3.5，通用能力强，支持 JSON Mode。需要海外网络环境或代理。</td>
          </tr>
          <tr>
            <td><strong>Claude</strong></td>
            <td>长文本分析、复杂病历</td>
            <td>长文本理解能力强，适合复杂病历分析和多轮对话场景。</td>
          </tr>
        </table>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-tools" /> 配置步骤</div>
      <div class="help-content">
        <ol>
          <li>进入「系统设置 - AI 智能中心 - 模型供应商」页面。</li>
          <li>选择模型供应商（DeepSeek / OpenAI / Claude / 自定义）。</li>
          <li>填写 API Base URL：
            <ul>
              <li>DeepSeek: <code>https://api.deepseek.com</code></li>
              <li>OpenAI: <code>https://api.openai.com</code></li>
              <li>Claude: <code>https://api.anthropic.com</code></li>
            </ul>
          </li>
          <li>填写 API Key（从对应平台获取的访问密钥）。密钥将加密存储，前端不会明文显示。</li>
          <li>选择默认模型：
            <ul>
              <li>DeepSeek: <code>deepseek-chat</code> 或 <code>deepseek-reasoner</code></li>
              <li>OpenAI: <code>gpt-4</code> 或 <code>gpt-3.5-turbo</code></li>
              <li>Claude: <code>claude-3-sonnet</code> 或 <code>claude-3-opus</code></li>
            </ul>
          </li>
          <li>点击「测试连接」验证配置是否正确。系统会发送一条测试请求，验证 API 地址和密钥是否可用。</li>
          <li>测试通过后点击「保存配置」。</li>
        </ol>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-warning-outline" /> 常见问题</div>
      <div class="help-content">
        <h4>测试连接失败怎么办？</h4>
        <ul>
          <li>检查 API Base URL 是否填写正确，末尾不要带斜杠。</li>
          <li>确认 API Key 未过期且余额充足。</li>
          <li>如果使用 OpenAI，确认网络可以访问海外 API（可能需要配置代理）。</li>
          <li>查看后端日志获取详细的错误信息。</li>
        </ul>

        <h4>可以配置多个供应商吗？</h4>
        <p>目前系统支持配置一个主供应商。如需切换，修改配置后保存即可。后续版本可能支持多供应商负载均衡和故障自动切换。</p>

        <h4>API Key 安全吗？</h4>
        <p>API Key 在后端使用 AES 加密存储，前端页面不会显示明文密钥。建议定期更换密钥，并避免在多个系统共用同一个 Key。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HelpModelProviderView'
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

.help-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin: 16px 0 8px;
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

.help-content code {
  background: var(--apple-bg-primary);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Menlo', 'Monaco', monospace;
  font-size: 12px;
  color: var(--apple-accent);
}

/* 表格 */
.help-table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 13px;
}

.help-table th,
.help-table td {
  border: 1px solid var(--apple-divider);
  padding: 10px 12px;
  text-align: left;
  vertical-align: top;
}

.help-table th {
  background: var(--apple-bg-primary);
  font-weight: 600;
  color: var(--apple-text-primary);
}

.help-table td {
  color: var(--apple-text-secondary);
  line-height: 1.7;
}
</style>
