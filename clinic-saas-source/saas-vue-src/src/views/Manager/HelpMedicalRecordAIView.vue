<template>
  <div class="help-detail-page">
    <div class="page-header">
      <h1 class="page-title">病历 AI 扩写配置</h1>
      <p class="page-subtitle">配置病历编辑页面中 AI 一键扩写的行为参数、提示词与安全策略</p>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-info" /> 功能概述</div>
      <div class="help-content">
        <p>病历 AI 扩写功能帮助医生将简要填写的病历信息自动扩写为规范、专业的完整病历。医生只需在病历编辑页面填写关键字段的简要内容，点击「AI 扩写」按钮，系统即可基于配置好的规则生成其余字段。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-tools" /> 基础设置</div>
      <div class="help-content">
        <table class="help-table">
          <tr><th>配置项</th><th>说明</th></tr>
          <tr>
            <td>功能开关</td>
            <td>控制病历编辑页面是否显示「AI 扩写」按钮。关闭后医生只能手动填写病历。</td>
          </tr>
          <tr>
            <td>默认温度</td>
            <td>控制 AI 输出的创造性。低温度（0.1-0.3）输出稳定、规范；高温度（0.7-1.0）输出更多样、灵活。建议病历扩写使用 0.2-0.3。</td>
          </tr>
          <tr>
            <td>最大输出 Token</td>
            <td>限制 AI 单次返回的最大字数。Token 数约等于中文字数的 1.5 倍。建议 2000-3000。</td>
          </tr>
          <tr>
            <td>空字段处理策略</td>
            <td>
              <ul>
                <li><strong>留白</strong>：医生未填写的字段返回空字符串</li>
                <li><strong>AI 生成</strong>：医生未填写的字段由 AI 智能推断生成内容</li>
                <li><strong>提示医生</strong>：医生未填写的字段返回提示语，提醒医生手动填写</li>
              </ul>
            </td>
          </tr>
        </table>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-tickets" /> 字段配置</div>
      <div class="help-content">
        <table class="help-table">
          <tr><th>配置项</th><th>说明</th></tr>
          <tr><td>字段名</td><td>病历字段的中文名称，如主诉、现病史、既往史等。</td></tr>
          <tr>
            <td>启用扩写</td>
            <td>只有启用的字段才会参与 AI 扩写。关闭后 AI 不会为该字段生成内容，这是字段级别的「总开关」。<br><strong>注意</strong>：即使空字段策略设为「AI 生成」，已关闭的字段也不会被填充。</td>
          </tr>
          <tr><td>最大字数</td><td>限制该字段 AI 生成的最大字数。超过时系统会截断或要求重试。</td></tr>
          <tr>
            <td>默认值</td>
            <td>当医生未填写该字段时，优先返回默认值而非 AI 生成内容。适用于固定表述的字段（如既往史、一般情况）。例如：既往史可设为「否认全身系统性疾病史，否认药物过敏史」。</td>
          </tr>
          <tr><td>必须包含</td><td>校验规则，要求 AI 输出中必须包含指定的关键词（白名单），多个关键词用逗号分隔。</td></tr>
          <tr><td>校验提示</td><td>当「必须包含」校验失败时，系统向医生展示的提示语。</td></tr>
        </table>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-document" /> 提示词模板</div>
      <div class="help-content">
        <p>提示词模板定义了 AI 扩写时的角色和行为准则。通常使用默认模板即可满足需求，高级用户可以根据门诊特色进行修改。</p>
        <h4>可插入变量</h4>
        <ul>
          <li><code>{kb_content}</code>：知识库内容（诊疗场景库中匹配的病种信息）</li>
          <li><code>{input_fields}</code>：医生已填写的字段内容</li>
          <li><code>{disease_type}</code>：当前诊疗场景类型</li>
        </ul>
        <h4>实时生效 JSON 字段预览</h4>
        <p>提示词模板区域下方会展示当前启用的字段列表，字段开关的变更会实时反映到 Prompt 中发送给 AI 的 JSON 格式要求里。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-claim" /> Few-shot 示例</div>
      <div class="help-content">
        <p>Few-shot 示例让 AI 参考你提供的输入-输出对，学习门诊的书写风格和格式规范。建议至少添加 1-2 条高质量示例。</p>
        <ul>
          <li><strong>简要输入</strong>：医生实际填写的简要内容</li>
          <li><strong>扩写输出</strong>：期望 AI 生成的完整专业内容</li>
        </ul>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-warning-outline" /> 安全策略</div>
      <div class="help-content">
        <table class="help-table">
          <tr><th>配置项</th><th>说明</th></tr>
          <tr><td>禁用确诊性断言</td><td>拦截「确诊」、「绝对」、「保证」等确定性词汇，强制 AI 使用建议性语气。</td></tr>
          <tr><td>敏感词拦截</td><td>AI 输出中包含指定词汇时将拒绝返回，可自定义敏感词列表。</td></tr>
          <tr><td>诊断语气校验</td><td>要求诊断字段必须包含「考虑/疑似/待排」等建议性表述。</td></tr>
          <tr><td>主诉长度校验</td><td>检查主诉是否超过设定的最大字数限制。</td></tr>
          <tr><td>现病史时间校验</td><td>检查现病史字段是否包含时间描述。</td></tr>
        </table>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-magic-stick" /> 效果测试</div>
      <div class="help-content">
        <p>配置完成后，在「效果测试」区域模拟医生填写的内容，点击「测试扩写」即可验证 AI 输出效果。</p>
        <ol>
          <li>在左侧表单中输入模拟病历内容（仅显示已启用的字段）。</li>
          <li>选择调用模式：Mock 模式（本地模拟，无需配置模型）或真实 AI（调用配置的大模型）。</li>
          <li>点击「测试扩写」查看右侧输出结果。</li>
          <li>展开「链路详情」可查看每个字段的来源（输入扩写 / AI 生成 / 默认值填充 / 留白）。</li>
          <li>确认效果满意后，点击「保存配置」使配置生效。</li>
        </ol>
        <div class="tip-box">
          <strong>提示：</strong>测试基于数据库中已保存的配置运行，修改字段开关或默认值后请先保存再测试。页面底部的浮动「保存配置」按钮方便你随时保存。
        </div>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-refresh" /> 完整工作流程</div>
      <div class="help-content">
        <ol>
          <li>在「字段配置」中勾选需要 AI 扩写的字段，设置最大字数和默认值。</li>
          <li>在「提示词模板」中确认或调整 System Prompt。</li>
          <li>根据需要添加 Few-shot 示例，训练 AI 符合门诊书写风格。</li>
          <li>在「效果测试」区域验证扩写效果。</li>
          <li>点击「保存配置」使配置生效。</li>
          <li>医生在病历编辑页面填写简要内容后，点击「AI 扩写」按钮生成完整病历。</li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HelpMedicalRecordAIView'
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
  width: 160px;
}

.help-table td {
  color: var(--apple-text-secondary);
  line-height: 1.7;
}

.help-table td ul,
.help-table td ol {
  margin: 4px 0;
  padding-left: 18px;
}

/* 提示框 */
.tip-box {
  margin-top: 12px;
  padding: 12px 16px;
  background: rgba(255, 193, 7, 0.08);
  border-left: 3px solid var(--apple-warning);
  border-radius: 0 8px 8px 0;
  font-size: 13px;
  color: var(--apple-text-secondary);
  line-height: 1.7;
}
</style>
