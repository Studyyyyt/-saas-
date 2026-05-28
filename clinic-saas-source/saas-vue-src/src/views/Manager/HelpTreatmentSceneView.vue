<template>
  <div class="help-detail-page">
    <div class="page-header">
      <h1 class="page-title">诊疗场景库</h1>
      <p class="page-subtitle">维护口腔门诊常见病种和标准化治疗流程，辅助 AI 生成规范病历</p>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-info" /> 什么是诊疗场景库</div>
      <div class="help-content">
        <p>诊疗场景库用于维护口腔门诊常见的病种和诊疗流程。每个场景对应一种疾病类型（如根管治疗、种植修复、拔牙等），可以配置该病种的复杂度级别和标准化治疗步骤。</p>
        <p>场景库是病历 AI 扩写的重要上下文来源，AI 在生成病历时会参考匹配场景的信息，使输出内容更贴合实际诊疗规范。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-promotion" /> 诊疗场景的作用</div>
      <div class="help-content">
        <ol>
          <li><strong>辅助 AI 扩写</strong>：AI 在生成病历内容时，会参考诊疗场景库中对应病种的信息，使输出更贴合实际诊疗流程。例如选择「根管治疗」场景后，AI 会按照根管治疗的标准步骤生成病历。</li>
          <li><strong>规范化诊疗</strong>：通过预设标准化的治疗步骤，帮助医生（尤其是年轻医生）遵循规范化的诊疗路径，减少遗漏关键步骤的风险。</li>
          <li><strong>关键词约束</strong>：可以为每个步骤设置「禁止关键词」和「必须包含关键词」，确保 AI 输出符合医疗安全规范和专业术语要求。</li>
        </ol>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-plus" /> 如何新增病种</div>
      <div class="help-content">
        <ol>
          <li>在「诊疗场景库」区域点击「新增病种」按钮。</li>
          <li>填写病种名称（如：根管治疗）和分类（如：牙体牙髓）。</li>
          <li>选择复杂度：
            <ul>
              <li><strong>简单</strong>：无需分步骤，一键扩写即可。适用于常规检查、简单充填等一次性完成的操作。</li>
              <li><strong>中等</strong>：可选分步骤，建议配置 2-3 个主要步骤。适用于补牙、简单拔牙等。</li>
              <li><strong>复杂</strong>：必须分步骤，如种植修复需要分阶段完成，根管治疗需要多次复诊。</li>
            </ul>
          </li>
          <li>配置治疗步骤（可选）：点击「添加步骤」，填写步骤名称、禁止关键词和必须包含关键词。</li>
          <li>点击「保存」完成添加。</li>
        </ol>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-check" /> 步骤配置详解</div>
      <div class="help-content">
        <table class="help-table">
          <tr><th>配置项</th><th>说明</th></tr>
          <tr>
            <td>步骤名称</td>
            <td>治疗阶段的标准名称，如「开髓引流」、「根管预备」、「根管充填」等。步骤名称会作为 AI 生成该阶段病历内容时的上下文参考。</td>
          </tr>
          <tr>
            <td>禁止关键词</td>
            <td>AI 生成该步骤相关内容时，绝对不允许出现的词汇。例如根管治疗步骤中禁止出现「拔牙」、「拔除」等词汇，防止 AI 输出不恰当的诊疗建议。<br><strong>多个关键词用逗号分隔</strong>。</td>
          </tr>
          <tr>
            <td>必须包含关键词</td>
            <td>AI 生成内容时必须提及的要点。例如根管充填步骤必须包含「牙胶尖」、「根充糊剂」、「侧压」等关键词，确保专业术语完整。<br><strong>多个关键词用逗号分隔</strong>。</td>
          </tr>
        </table>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-document-copy" /> 使用示例：根管治疗场景</div>
      <div class="help-content">
        <div class="example-card">
          <div class="example-header">根管治疗（牙体牙髓科）— 复杂度：复杂</div>
          <div class="example-body">
            <p><strong>步骤配置：</strong></p>
            <ol>
              <li><strong>开髓引流</strong> — 禁止：拔牙、拔除；必须包含：开髓、减压</li>
              <li><strong>根管预备</strong> — 禁止：拔牙；必须包含：根管锉、工作长度、冲洗</li>
              <li><strong>根管消毒</strong> — 必须包含：氢氧化钙、封药</li>
              <li><strong>根管充填</strong> — 禁止：开放；必须包含：牙胶尖、根充糊剂、侧压</li>
            </ol>
          </div>
        </div>
        <div class="tip-box">
          当医生选择「根管治疗」场景并点击 AI 扩写时，AI 会按照以上步骤生成规范的病历内容，并自动遵守各步骤的关键词约束。例如根管充填步骤不会出现「建议开放引流」等不当表述，且会确保提及「牙胶尖」等关键材料。
        </div>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-edit-outline" /> 编辑与维护</div>
      <div class="help-content">
        <ul>
          <li><strong>编辑场景</strong>：点击场景卡片右侧的「编辑」按钮，可修改病种名称、分类、复杂度、步骤配置等信息。</li>
          <li><strong>删除场景</strong>：点击「删除」按钮可移除不再使用的病种。删除前系统会弹出确认提示，防止误操作。</li>
          <li><strong>排序</strong>：通过设置「排序」值调整场景在列表中的显示顺序，常用场景可以排在前面。</li>
        </ul>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-warning-outline" /> 注意事项</div>
      <div class="help-content">
        <ul>
          <li>诊疗场景库目前主要用于辅助病历 AI 扩写的上下文参考，暂不会强制影响医生的实际治疗操作。</li>
          <li>建议定期更新场景库，根据门诊实际开展的诊疗项目进行维护。新增开展的项目（如隐形矫正、牙周手术）应及时补充对应场景。</li>
          <li>禁止关键词和必须包含关键词的设置应谨慎，过多限制可能导致 AI 输出质量下降或内容生硬。建议先配置核心关键词，后续根据实际输出效果逐步调整。</li>
          <li>复杂度选择应实事求是，简单操作设为「复杂」会导致 AI 生成过多不必要的分步骤描述，反而降低效率。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HelpTreatmentSceneView'
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
  width: 140px;
}

.help-table td {
  color: var(--apple-text-secondary);
  line-height: 1.7;
}

/* 示例卡片 */
.example-card {
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-divider);
  border-radius: 12px;
  overflow: hidden;
  margin: 12px 0;
}

.example-header {
  background: var(--apple-accent-light);
  color: var(--apple-accent);
  font-size: 13px;
  font-weight: 600;
  padding: 10px 16px;
}

.example-body {
  padding: 16px;
  font-size: 13px;
  color: var(--apple-text-secondary);
  line-height: 1.8;
}

.example-body p {
  margin: 4px 0;
}

.example-body ol {
  padding-left: 20px;
}

.example-body li {
  margin: 6px 0;
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
