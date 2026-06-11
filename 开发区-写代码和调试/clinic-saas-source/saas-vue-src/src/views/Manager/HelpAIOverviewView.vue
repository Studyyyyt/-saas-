<template>
  <div class="help-detail-page">
    <div class="page-header">
      <h1 class="page-title">AI 总览</h1>
      <p class="page-subtitle">AI 功能的全局控制面板，管理系统所有 AI 功能的开关与运行状态</p>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-info" /> 功能概述</div>
      <div class="help-content">
        <p>AI 总览是「系统设置 - AI 智能中心」的默认入口页面，提供对系统所有 AI 功能的全局控制能力。通过本页面，管理员可以一键查看今日 AI 调用统计、管理各功能的启用状态，并快速跳转到对应的配置页面。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-set-up" /> 全局控制</div>
      <div class="help-content">
        <h4>AI 功能总开关</h4>
        <p>系统级的一键开关。关闭后，所有页面的 AI 功能入口将自动隐藏，前端不再展示任何 AI 相关按钮或面板，后端也会拒绝所有 AI 调用请求。适用于以下场景：</p>
        <ul>
          <li>系统维护或升级期间，临时停用所有 AI 功能。</li>
          <li>诊所暂未采购 AI 服务，需要完全关闭入口。</li>
          <li>出现 AI 服务异常时，快速全局熔断。</li>
        </ul>

        <h4>调试模式</h4>
        <p>开启后，系统会在浏览器控制台和后台同时打印 AI 请求的详细日志，方便排查问题：</p>
        <ul>
          <li><strong>前端日志</strong>（浏览器 F12 Console）：打印每次流式请求的参数、响应状态码、逐字返回的 Token 内容以及流结束标记。</li>
          <li><strong>后端日志</strong>（Docker 后台输出）：打印接收到的请求字段、System Prompt 长度、模型原始响应文本及异常堆栈。</li>
        </ul>
        <p>调试模式仅影响日志输出，不会改变 AI 的实际行为。建议由技术人员在排查问题时开启，日常使用中保持关闭以避免控制台噪音。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-data-line" /> 统计卡片说明</div>
      <div class="help-content">
        <table class="help-table">
          <tr><th>指标</th><th>说明</th><th>数据来源</th></tr>
          <tr>
            <td><strong>今日 AI 调用</strong></td>
            <td>当日 00:00 至当前时间的 AI 请求总次数</td>
            <td><code>ai_operation_log</code> 表，按 <code>create_time</code> 统计</td>
          </tr>
          <tr>
            <td><strong>今日 Token 消耗</strong></td>
            <td>当日所有 AI 请求消耗的 Token 总数，自动格式化显示（如 45.2K）</td>
            <td><code>ai_operation_log.token_used</code> 字段累加</td>
          </tr>
          <tr>
            <td><strong>活跃功能数</strong></td>
            <td>已启用的 AI 功能数量 / 系统总功能数量</td>
            <td><code>ai_function_config.is_enabled = 1</code> 的计数</td>
          </tr>
          <tr>
            <td><strong>错误率</strong></td>
            <td>当日 AI 调用中返回错误的比例（含网络超时、模型拒绝、解析失败等）</td>
            <td><code>ai_operation_log.error_msg IS NOT NULL</code> 的占比</td>
          </tr>
        </table>
        <p><strong>注意</strong>：若今日尚无 AI 调用记录，所有统计值均显示为 0。系统不会显示历史累计数据，仅聚焦当日实时状态。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-grid" /> AI 功能列表</div>
      <div class="help-content">
        <p>列表展示系统当前注册的所有 AI 功能模块，每行包含以下信息：</p>
        <ul>
          <li><strong>功能名称</strong>：显示名称与图标（emoji），下方标注当前使用的模型供应商。</li>
          <li><strong>所属页面</strong>：该功能在前端哪个业务页面中生效。</li>
          <li><strong>状态</strong>：「已启用」或「已禁用」。仅影响当前功能的可用性，不影响其他功能。</li>
          <li><strong>今日调用</strong>：该功能今日的独立调用次数（后续版本接入）。</li>
          <li><strong>操作</strong>：配置按钮（跳转至该功能的详细配置页）+ 启用/禁用开关。</li>
        </ul>

        <h4>全局关闭时的行为</h4>
        <p>当「AI 功能总开关」处于关闭状态时，整个功能列表区域会进入置灰状态：</p>
        <ul>
          <li>表格透明度降低，视觉上提示用户当前不可用。</li>
          <li>所有「配置」按钮和单个功能开关被禁用，无法点击。</li>
          <li>右上角出现「AI 已全局关闭」的橙色警告标签。</li>
        </ul>
        <p>此时仅「调试模式」开关仍可独立交互，因为它属于系统调试工具，不依赖 AI 总开关。</p>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-tools" /> 操作指南</div>
      <div class="help-content">
        <ol>
          <li>进入「系统设置 - AI 智能中心 - AI 总览」页面。</li>
          <li>确认「AI 功能总开关」为开启状态。若需完全停用 AI，将其关闭即可。</li>
          <li>在「AI 功能列表」中，通过右侧开关按需启用或禁用各功能。
            <ul>
              <li>仅启用实际需要使用的功能，可降低 Token 消耗和误操作风险。</li>
              <li>禁用某功能后，对应业务页面的 AI 入口会自动隐藏。</li>
            </ul>
          </li>
          <li>点击功能名称右侧的「配置」按钮，跳转至该功能的详细参数设置页。</li>
          <li>如需排查 AI 输出异常，开启「调试模式」，在浏览器 F12 控制台查看完整请求链路。</li>
        </ol>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-warning-outline" /> 常见问题</div>
      <div class="help-content">
        <h4>关闭全局开关后，已启用的功能会丢失配置吗？</h4>
        <p>不会。全局开关仅控制入口的显示与后端请求的拦截，各功能的独立启用状态、提示词模板、字段规则等配置均完整保留。重新开启全局开关后，各功能会按之前的配置立即恢复可用。</p>

        <h4>为什么「今日调用」都显示为 0？</h4>
        <p>当前版本仅在执行 AI 调用时向 <code>ai_operation_log</code> 表写入日志。如果今日尚未触发任何 AI 功能（如未使用病历扩写、未生成患者洞察），则统计为 0 是正常表现。后续版本将在各业务页面接入 AI 按钮后，调用数据会自动累计。</p>

        <h4>调试模式日志在哪里查看？</h4>
        <p>开启调试模式后，使用 Chrome 浏览器按 <code>F12</code> 打开开发者工具，切换到「Console」面板。每次 AI 调用都会以分组形式输出，包含请求参数、响应内容和解析结果。建议仅由技术人员查看，普通医生用户无需关注。</p>

        <h4>各 AI 功能开关之间有关联吗？</h4>
        <p>各功能开关完全独立，互不影响。例如：关闭「经营 AI 分析」不会导致「首页 AI 助手」或「病历 AI 扩写」不可用。每个功能在请求后端时都会携带独立的功能标识（functionKey），后端会单独校验该功能的启用状态。因此你可以按需精细化控制，只开启诊所实际需要的功能，以降低 Token 消耗。</p>

        <h4>可以添加自定义 AI 功能吗？</h4>
        <p>当前系统预置了 9 个标准功能模块。如需扩展自定义功能，需由开发人员在 <code>ai_function_config</code> 表中插入新记录，并开发对应的前后端业务逻辑。不建议非技术人员直接操作数据库。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HelpAIOverviewView'
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
