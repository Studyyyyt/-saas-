<template>
  <div class="agent-config-page">
    <!-- 页面标题 -->
    <div class="agent-config-header">
      <div class="agent-config-header-left">
        <h1 class="agent-config-title">AI 助手配置</h1>
        <p class="agent-config-subtitle">自定义首页 AI 助手的角色、快捷指令与外观</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" class="add-agent-btn" :loading="loading" @click="openEditor()">
        新增助手
      </el-button>
    </div>

    <!-- Agent 列表 -->
    <div v-loading="loading" class="agent-list">
      <div v-for="agent in agents" :key="agent.id" class="agent-card">
        <div class="agent-card-main">
          <div class="agent-card-avatar" :style="{ background: agent.gradient }">
            {{ agent.icon }}
          </div>
          <div class="agent-card-body">
            <div class="agent-card-name">
              {{ agent.name }}
              <el-tag v-if="agent.isSystemDefault" size="mini" type="info">系统</el-tag>
            </div>
            <div class="agent-card-desc">{{ agent.desc }}</div>
            <div class="agent-card-chips">
              <span v-for="chip in agent.chips.slice(0, 6)" :key="chip" class="agent-chip">{{ chip }}</span>
              <span v-if="agent.chips.length > 6" class="agent-chip-more">+{{ agent.chips.length - 6 }}</span>
            </div>
          </div>
        </div>
        <div class="agent-card-actions">
          <span class="agent-action" title="编辑" @click="openEditor(agent)">
            <i class="el-icon-edit"></i>
          </span>
          <span v-if="!agent.isSystemDefault" class="agent-action danger" title="删除" @click="removeAgent(agent)">
            <i class="el-icon-delete"></i>
          </span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="agents.length === 0 && !loading" class="agent-empty">
      <div class="agent-empty-icon">🤖</div>
      <div class="agent-empty-text">暂无自定义 AI 助手</div>
      <div class="agent-empty-hint">点击右上角“新增助手”创建您的第一个 AI 角色</div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <el-dialog
      :title="editingId ? '编辑 AI 助手' : '新增 AI 助手'"
      :visible.sync="editorVisible"
      width="600px"
      custom-class="agent-editor-dialog"
      :close-on-click-modal="false"
    >
      <div class="agent-form">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">助手名称 <span class="required">*</span></label>
            <el-input v-model="form.name" placeholder="如：经营分析" maxlength="10" show-word-limit />
          </div>
          <div class="form-group" style="width: 100px; flex-shrink: 0;">
            <label class="form-label">图标 <span class="required">*</span></label>
            <el-input v-model="form.icon" placeholder="如：📊" maxlength="2" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">描述</label>
          <el-input v-model="form.desc" placeholder="一句话说明该助手的用途" maxlength="40" show-word-limit />
        </div>

        <div class="form-group">
          <label class="form-label">主题色 <span class="required">*</span></label>
          <div class="gradient-presets">
            <div
              v-for="g in gradientPresets"
              :key="g.value"
              class="gradient-preset"
              :class="{ active: form.gradient === g.value }"
              :style="{ background: g.value }"
              :title="g.label"
              @click="form.gradient = g.value"
            />
            <div class="gradient-custom" :class="{ active: isCustomGradient }">
              <el-input v-model="customGradient" placeholder="自定义 CSS 渐变" size="mini" />
            </div>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">快捷指令 <span class="required">*</span></label>
          <div class="chips-input">
            <div class="chips-list">
              <el-tag
                v-for="(chip, idx) in form.chips"
                :key="chip + idx"
                closable
                size="small"
                @close="removeChip(idx)"
              >{{ chip }}</el-tag>
            </div>
            <el-input
              v-model="chipInput"
              placeholder="输入后按回车添加"
              size="small"
              @keyup.enter.native="addChip"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">系统提示词（System Prompt）</label>
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="定义该 AI 助手的角色与行为准则。例如：你是一位口腔门诊经营分析专家，擅长通过数据发现问题并给出 actionable 建议..."
          />
          <div class="form-hint">系统提示词会作为 AI 的隐藏上下文，影响其回答风格与能力边界</div>
        </div>

        <div class="form-group">
          <label class="form-label">启用的数据工具</label>
          <el-checkbox-group v-model="form.enabledTools" size="small">
            <el-checkbox-button v-for="tool in toolOptions" :key="tool.key" :label="tool.key">
              {{ tool.label }}
            </el-checkbox-button>
          </el-checkbox-group>
          <div class="form-hint">勾选后，该助手在对话中可以调用对应的数据查询工具来获取实时信息</div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!canSave" :loading="saveLoading" @click="saveAgent">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  fetchAgentConfigs,
  saveAgentConfig,
  updateAgentConfig,
  deleteAgentConfig
} from '@/utils/aiStreamClient'
import { getAdminSession } from '@/utils/adminSession'

const STORAGE_KEY = 'saas_ai_agents_v1'

const defaultAgents = [
  {
    id: 'default',
    name: '智能助手',
    icon: '🤖',
    desc: '通用门诊查询与数据汇总',
    gradient: 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)',
    chips: ['今日预约', '我的待办', '本月收入', '患者查询', '今日患者', '待收费'],
    systemPrompt: '',
    enabledTools: ['query_patients', 'query_appointments', 'query_medical_records', 'query_finances']
  },
  {
    id: 'finance',
    name: '经营分析',
    icon: '📊',
    desc: '财务、收入与经营数据分析',
    gradient: 'linear-gradient(135deg, #d97706 0%, #f59e0b 100%)',
    chips: ['本月收入', '近7天趋势', '待收费', '加工费', '耗材支出', '高价值客户'],
    systemPrompt: '',
    enabledTools: ['query_finances', 'query_appointments', 'query_treatments', 'query_materials']
  },
  {
    id: 'patient',
    name: '患者管理',
    icon: '🏥',
    desc: '患者档案、随访与病历查询',
    gradient: 'linear-gradient(135deg, #059669 0%, #10b981 100%)',
    chips: ['患者查询', '待回访', '流失风险', '转介绍', '待写病历', '今日患者'],
    systemPrompt: '',
    enabledTools: ['query_patients', 'query_medical_records', 'query_appointments', 'query_treatments']
  },
  {
    id: 'schedule',
    name: '预约调度',
    icon: '📅',
    desc: '预约排班、医生日程与调度',
    gradient: 'linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)',
    chips: ['今日预约', '明日预约', '医生排班', '待接诊', '已取消', '预约趋势'],
    systemPrompt: '',
    enabledTools: ['query_appointments', 'query_treatments']
  }
]

const gradientPresets = [
  { label: '蓝色', value: 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)' },
  { label: '绿色', value: 'linear-gradient(135deg, #059669 0%, #10b981 100%)' },
  { label: '琥珀', value: 'linear-gradient(135deg, #d97706 0%, #f59e0b 100%)' },
  { label: '紫色', value: 'linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)' },
  { label: '红色', value: 'linear-gradient(135deg, #dc2626 0%, #ef4444 100%)' },
  { label: '青色', value: 'linear-gradient(135deg, #0891b2 0%, #06b6d4 100%)' },
  { label: '粉色', value: 'linear-gradient(135deg, #db2777 0%, #ec4899 100%)' },
  { label: '靛蓝', value: 'linear-gradient(135deg, #4338ca 0%, #6366f1 100%)' }
]

function loadAgentsFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed) && parsed.length > 0) return parsed
    }
  } catch (e) {
    console.warn('加载 AI agents 失败', e)
  }
  return defaultAgents.map(a => ({ ...a }))
}

function saveAgentsToStorage(agents) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(agents))
  } catch (e) {
    console.warn('保存 AI agents 失败', e)
  }
}

export { loadAgentsFromStorage, saveAgentsToStorage }

export default {
  name: 'AIAgentConfigView',
  data() {
    return {
      agents: [],
      editorVisible: false,
      editingId: null,
      form: {
        name: '',
        icon: '',
        desc: '',
        gradient: gradientPresets[0].value,
        chips: [],
        systemPrompt: '',
        enabledTools: []
      },
      customGradient: '',
      chipInput: '',
      gradientPresets,
      loading: false,
      saveLoading: false,
      toolOptions: [
        { key: 'query_patients', label: '查询患者' },
        { key: 'query_appointments', label: '查询预约' },
        { key: 'query_medical_records', label: '查询病历' },
        { key: 'query_finances', label: '查询财务' },
        { key: 'query_treatments', label: '查询治疗' },
        { key: 'query_lab_orders', label: '查询加工单' },
        { key: 'query_materials', label: '查询耗材' }
      ]
    }
  },
  computed: {
    isCustomGradient() {
      return this.customGradient && !this.gradientPresets.some(g => g.value === this.form.gradient)
    },
    canSave() {
      return this.form.name.trim() && this.form.icon.trim() && this.form.chips.length > 0
    }
  },
  watch: {
    customGradient(val) {
      if (val && val.includes('gradient')) {
        this.form.gradient = val
      }
    }
  },
  created() {
    this.loadAgents()
  },
  methods: {
    async loadAgents() {
      this.loading = true
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || null
        const res = await fetchAgentConfigs(accountId)
        if (res.code === '200' && Array.isArray(res.data)) {
          this.agents = res.data.map(item => this.backendToFrontend(item))
          if (this.agents.length === 0) {
            this.agents = defaultAgents.map(a => ({ ...a }))
          }
        } else {
          this.agents = loadAgentsFromStorage()
        }
      } catch (e) {
        console.warn('从后端加载 Agent 配置失败', e)
        this.agents = loadAgentsFromStorage()
      } finally {
        this.loading = false
      }
    },
    backendToFrontend(item) {
      return {
        id: item.agentKey || String(item.id),
        name: item.name,
        icon: item.icon,
        desc: item.description || '',
        gradient: item.gradient,
        chips: Array.isArray(item.chips) ? item.chips : [],
        systemPrompt: item.systemPrompt || '',
        enabledTools: Array.isArray(item.enabledTools) ? item.enabledTools : [],
        sortOrder: item.sortOrder,
        isSystemDefault: item.isSystemDefault,
        rawId: item.id
      }
    },
    frontendToBackend(agent) {
      const session = getAdminSession() || {}
      return {
        accountId: session.id || null,
        agentKey: agent.id,
        name: agent.name,
        icon: agent.icon,
        description: agent.desc,
        gradient: agent.gradient,
        chips: agent.chips,
        systemPrompt: agent.systemPrompt,
        enabledTools: agent.enabledTools,
        sortOrder: agent.sortOrder || 0
      }
    },
    openEditor(agent) {
      if (agent) {
        this.editingId = agent.id
        this.form = {
          name: agent.name,
          icon: agent.icon,
          desc: agent.desc || '',
          gradient: agent.gradient,
          chips: agent.chips.slice(),
          systemPrompt: agent.systemPrompt || '',
          enabledTools: Array.isArray(agent.enabledTools) ? agent.enabledTools.slice() : []
        }
        this.customGradient = this.gradientPresets.some(g => g.value === agent.gradient) ? '' : agent.gradient
      } else {
        this.editingId = null
        this.form = {
          name: '',
          icon: '',
          desc: '',
          gradient: gradientPresets[0].value,
          chips: [],
          systemPrompt: '',
          enabledTools: []
        }
        this.customGradient = ''
      }
      this.chipInput = ''
      this.editorVisible = true
    },
    async saveAgent() {
      this.saveLoading = true
      const payload = {
        id: this.editingId || 'agent_' + Date.now(),
        name: this.form.name.trim(),
        icon: this.form.icon.trim(),
        desc: this.form.desc.trim(),
        gradient: this.form.gradient,
        chips: this.form.chips.slice(),
        systemPrompt: this.form.systemPrompt.trim(),
        enabledTools: this.form.enabledTools.slice()
      }
      try {
        if (this.editingId) {
          const existing = this.agents.find(a => a.id === this.editingId)
          const backendPayload = this.frontendToBackend(payload)
          if (existing && existing.rawId) {
            await updateAgentConfig(existing.rawId, backendPayload)
          } else {
            await saveAgentConfig(backendPayload)
          }
          const idx = this.agents.findIndex(a => a.id === this.editingId)
          if (idx >= 0) {
            this.$set(this.agents, idx, payload)
          }
        } else {
          const backendPayload = this.frontendToBackend(payload)
          const res = await saveAgentConfig(backendPayload)
          if (res.code === '200' && res.data && res.data.id) {
            payload.rawId = res.data.id
          }
          this.agents.push(payload)
        }
        saveAgentsToStorage(this.agents)
        this.editorVisible = false
        this.$message.success('保存成功')
      } catch (e) {
        this.$message.error('保存失败：' + (e.message || '未知错误'))
      } finally {
        this.saveLoading = false
      }
    },
    removeAgent(agent) {
      this.$confirm(`确定删除「${agent.name}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          if (agent.rawId && !agent.isSystemDefault) {
            await deleteAgentConfig(agent.rawId)
          }
          this.agents = this.agents.filter(a => a.id !== agent.id)
          saveAgentsToStorage(this.agents)
          this.$message.success('已删除')
        } catch (e) {
          this.$message.error('删除失败：' + (e.message || '未知错误'))
        }
      }).catch(() => {})
    },
    addChip() {
      const text = this.chipInput.trim()
      if (!text) return
      if (this.form.chips.includes(text)) {
        this.$message.warning('该指令已存在')
        return
      }
      this.form.chips.push(text)
      this.chipInput = ''
    },
    removeChip(index) {
      this.form.chips.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.agent-config-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.agent-config-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
}

.agent-config-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0f172a;
  line-height: 1.2;
}

.agent-config-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: #64748b;
}

.add-agent-btn {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
}

/* Agent 卡片列表 */
.agent-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.agent-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 18px;
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.agent-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--apple-shadow-lg), var(--apple-shadow-glow);
}

.agent-card-main {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.agent-card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.agent-card-body {
  flex: 1;
  min-width: 0;
}

.agent-card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.agent-card-desc {
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin-bottom: 8px;
}

.agent-card-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.agent-chip {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: var(--apple-accent);
  background: var(--apple-accent-light);
}

.agent-chip-more {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: var(--apple-text-tertiary);
  background: rgba(0, 0, 0, 0.04);
}

.agent-card-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.agent-action {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--apple-text-secondary);
  transition: all var(--apple-transition-fast);
}

.agent-action:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-accent);
}

.agent-action.danger:hover {
  background: rgba(239, 68, 68, 0.08);
  color: var(--apple-danger);
}

/* 空状态 */
.agent-empty {
  text-align: center;
  padding: 64px 24px;
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md);
  border-radius: 20px;
}

.agent-empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.agent-empty-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 4px;
}

.agent-empty-hint {
  font-size: 13px;
  color: var(--apple-text-tertiary);
}

/* 弹窗表单 */
.agent-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.form-hint {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  line-height: 1.5;
}

.required {
  color: var(--apple-danger);
}

.gradient-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.gradient-preset {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.gradient-preset:hover {
  transform: scale(1.1);
}

.gradient-preset.active {
  border-color: var(--apple-text-primary);
}

.gradient-custom {
  flex: 1;
  min-width: 140px;
}

.gradient-custom.active >>> .el-input__inner {
  border-color: var(--apple-accent);
}

.chips-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chips-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chips-list >>> .el-tag {
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .agent-config-page {
    padding: 16px;
  }

  .agent-config-title {
    font-size: 22px;
  }

  .agent-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .agent-card-actions {
    align-self: flex-end;
  }

  .form-row {
    flex-direction: column;
  }
}
</style>
