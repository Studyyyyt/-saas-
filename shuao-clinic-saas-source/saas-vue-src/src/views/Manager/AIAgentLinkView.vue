<template>
  <div class="ai-agent-link-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Agent 链接</h1>
        <p class="page-subtitle">配置外部平台智能体（n8n、Dify、Coze 等）的 API 接入</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="openEditor()">
        新增链接
      </el-button>
    </div>

    <!-- Agent 列表 -->
    <div class="section-card" v-loading="loading">
      <div v-if="agentLinks.length === 0" class="empty-state">
        <div class="empty-icon">🔗</div>
        <div class="empty-text">暂无外部 Agent 链接</div>
        <div class="empty-hint">点击右上角“新增链接”接入 n8n、Dify 等平台的智能体</div>
      </div>

      <div v-else class="agent-list">
        <div v-for="agent in agentLinks" :key="agent.id" class="agent-card">
          <div class="agent-main">
            <div class="agent-icon" :style="{ background: getPlatformColor(agent.platform) }">
              {{ getPlatformIcon(agent.platform) }}
            </div>
            <div class="agent-body">
              <div class="agent-name">
                {{ agent.name }}
                <el-tag v-if="agent.isActive" size="mini" type="success">启用</el-tag>
                <el-tag v-else size="mini" type="info">禁用</el-tag>
              </div>
              <div class="agent-meta">
                <span class="meta-item">平台: {{ agent.platform }}</span>
                <span class="meta-divider">|</span>
                <span class="meta-item">{{ maskUrl(agent.apiUrl) }}</span>
                <span class="meta-divider">|</span>
                <span class="meta-item">超时: {{ agent.timeout }}秒</span>
              </div>
            </div>
          </div>
          <div class="agent-actions">
            <el-button size="mini" icon="el-icon-connection" @click="testAgent(agent)">测试</el-button>
            <el-button size="mini" icon="el-icon-edit" @click="openEditor(agent)">编辑</el-button>
            <el-button size="mini" type="danger" icon="el-icon-delete" @click="removeAgent(agent)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      :title="editingId ? '编辑 Agent 链接' : '新增 Agent 链接'"
      :visible.sync="editorVisible"
      width="580px"
      :close-on-click-modal="false"
    >
      <div class="form-body">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">名称 <span class="required">*</span></label>
            <el-input v-model="form.name" placeholder="如：病历扩写 n8n 工作流" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">平台类型 <span class="required">*</span></label>
            <el-select v-model="form.platform" placeholder="请选择" style="width: 100%">
              <el-option label="n8n (Webhook)" value="n8n" />
              <el-option label="Dify" value="dify" />
              <el-option label="Coze (扣子)" value="coze" />
              <el-option label="FastGPT" value="fastgpt" />
              <el-option label="自定义 Webhook" value="custom" />
            </el-select>
          </div>
          <div class="form-group">
            <label class="form-label">超时时间（秒）</label>
            <el-input-number v-model="form.timeout" :min="5" :max="120" :step="5" style="width: 100%" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">API 地址 / Webhook URL <span class="required">*</span></label>
          <el-input v-model="form.apiUrl" placeholder="https://n8n.example.com/webhook/xxx" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">认证方式</label>
            <el-select v-model="form.authType" placeholder="请选择" style="width: 100%">
              <el-option label="API Key" value="apikey" />
              <el-option label="Bearer Token" value="bearer" />
              <el-option label="无认证" value="none" />
            </el-select>
          </div>
          <div class="form-group" v-if="form.authType !== 'none'">
            <label class="form-label">认证密钥</label>
            <el-input v-model="form.authSecret" :type="showSecret ? 'text' : 'password'" placeholder="留空表示不修改">
              <el-button slot="append" icon="el-icon-view" @click="showSecret = !showSecret" />
            </el-input>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">请求参数示例（JSON，可选）</label>
          <el-input
            v-model="form.samplePayload"
            type="textarea"
            :rows="4"
            placeholder='{"chiefComplaint": "牙痛", "diagnosis": "龋齿"}'
          />
          <div class="form-hint">用于测试连接时发送的示例数据</div>
        </div>

        <div class="form-group">
          <label class="form-label">启用状态</label>
          <el-switch v-model="form.isActive" active-text="启用" inactive-text="禁用" />
        </div>
      </div>

      <div slot="footer">
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveAgent">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
const platformMeta = {
  n8n: { icon: 'n8n', color: 'linear-gradient(135deg, #ff6d5a, #ff8f7a)' },
  dify: { icon: 'Dify', color: 'linear-gradient(135deg, #2563eb, #3b82f6)' },
  coze: { icon: 'Coze', color: 'linear-gradient(135deg, #7c3aed, #a78bfa)' },
  fastgpt: { icon: 'FG', color: 'linear-gradient(135deg, #059669, #10b981)' },
  custom: { icon: 'API', color: 'linear-gradient(135deg, #64748b, #94a3b8)' }
}

export default {
  name: 'AIAgentLinkView',
  data() {
    return {
      loading: false,
      saveLoading: false,
      editorVisible: false,
      editingId: null,
      showSecret: false,
      agentLinks: [
        {
          id: 1,
          name: '病历扩写工作流',
          platform: 'n8n',
          apiUrl: 'https://n8n.shuao.com/webhook/medical-expand',
          authType: 'apikey',
          timeout: 30,
          isActive: true
        }
      ],
      form: {
        name: '',
        platform: 'n8n',
        apiUrl: '',
        authType: 'apikey',
        authSecret: '',
        timeout: 30,
        samplePayload: '',
        isActive: true
      }
    }
  },
  methods: {
    getPlatformColor(platform) {
      return platformMeta[platform]?.color || platformMeta.custom.color
    },
    getPlatformIcon(platform) {
      return platformMeta[platform]?.icon || platformMeta.custom.icon
    },
    maskUrl(url) {
      if (!url) return '-'
      try {
        const u = new URL(url)
        return `${u.protocol}//${u.hostname}/...${u.pathname.slice(-12)}`
      } catch {
        return url.substring(0, 30) + '...'
      }
    },
    openEditor(agent) {
      if (agent) {
        this.editingId = agent.id
        this.form = { ...agent, authSecret: '' }
      } else {
        this.editingId = null
        this.form = {
          name: '',
          platform: 'n8n',
          apiUrl: '',
          authType: 'apikey',
          authSecret: '',
          timeout: 30,
          samplePayload: '',
          isActive: true
        }
      }
      this.showSecret = false
      this.editorVisible = true
    },
    saveAgent() {
      if (!this.form.name.trim() || !this.form.apiUrl.trim()) {
        this.$message.warning('请填写必填项')
        return
      }
      this.saveLoading = true
      setTimeout(() => {
        if (this.editingId) {
          const idx = this.agentLinks.findIndex(a => a.id === this.editingId)
          if (idx >= 0) {
            this.$set(this.agentLinks, idx, { ...this.agentLinks[idx], ...this.form, id: this.editingId })
          }
        } else {
          this.agentLinks.push({ ...this.form, id: Date.now() })
        }
        this.editorVisible = false
        this.$message.success('保存成功')
        this.saveLoading = false
      }, 300)
    },
    removeAgent(agent) {
      this.$confirm(`确定删除「${agent.name}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.agentLinks = this.agentLinks.filter(a => a.id !== agent.id)
        this.$message.success('已删除')
      }).catch(() => {})
    },
    testAgent(agent) {
      this.$message.info(`正在测试「${agent.name}」的连接...`)
      // TODO: 调用后端测试接口
      setTimeout(() => {
        this.$message.success('连接成功')
      }, 800)
    }
  }
}
</script>

<style scoped>
.ai-agent-link-page {
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
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 24px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 4px;
}

.empty-hint {
  font-size: 13px;
  color: var(--apple-text-tertiary);
}

/* Agent 列表 */
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
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-divider);
  border-radius: 14px;
  transition: all 0.3s ease;
}

.agent-card:hover {
  border-color: var(--apple-accent);
  box-shadow: var(--apple-shadow-sm);
}

.agent-main {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.agent-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.agent-body {
  flex: 1;
  min-width: 0;
}

.agent-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-meta {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-divider {
  opacity: 0.5;
}

.agent-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 表单 */
.form-body {
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
}

.required {
  color: var(--apple-danger);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .agent-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .agent-actions {
    align-self: flex-end;
  }

  .form-row {
    flex-direction: column;
  }
}
</style>
