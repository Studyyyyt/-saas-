<template>
  <div class="api-key-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>开放接口密钥</h2>
        <p>管理外部系统和 AI 工具访问本系统所需的 API Key</p>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap">
      <i class="el-icon-loading"></i>
      <span>加载中…</span>
    </div>

    <div v-else class="api-key-card">
      <div class="card-header">
        <div class="card-icon">🔑</div>
        <div class="card-info">
          <div class="card-title">当前 API Key</div>
          <div class="card-subtitle">每个诊所仅拥有一个 Key，可用于 n8n、MCP 等外部集成</div>
        </div>
      </div>

      <div class="key-display">
        <div class="key-value">{{ displayedKey }}</div>
        <div class="key-actions">
          <button
            class="btn-copy"
            :class="{ copied: copied }"
            :disabled="!apiKeyData.key || isMasked"
            @click="copyKey"
          >
            {{ copied ? '已复制' : '复制' }}
          </button>
          <button class="btn-toggle" @click="toggleMask">
            {{ isMasked ? '显示' : '隐藏' }}
          </button>
        </div>
      </div>

      <div class="key-meta">
        <div class="meta-item">
          <span class="meta-label">诊所ID</span>
          <span class="meta-value clinic-id-wrap">
            <template v-if="!editingClinicId">
              <code class="clinic-id">{{ apiKeyData.clinicId || '1' }}</code>
              <button class="btn-text" @click="startEditClinicId">编辑</button>
            </template>
            <template v-else>
              <input
                v-model="clinicIdInput"
                class="clinic-id-input"
                placeholder="如: kouqiangmenzhen"
                maxlength="64"
                @keyup.enter="saveClinicId"
              />
              <button class="btn-text btn-confirm" @click="saveClinicId">保存</button>
              <button class="btn-text btn-cancel" @click="cancelEditClinicId">取消</button>
            </template>
          </span>
        </div>
        <div class="meta-item">
          <span class="meta-label">名称</span>
          <span class="meta-value">{{ apiKeyData.name || '-' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">状态</span>
          <span class="meta-value">
            <span class="status-badge" :class="apiKeyData.isEnabled ? 'enabled' : 'disabled'">
              {{ apiKeyData.isEnabled ? '启用中' : '已禁用' }}
            </span>
          </span>
        </div>
        <div class="meta-item">
          <span class="meta-label">最后使用</span>
          <span class="meta-value">{{ apiKeyData.lastUsedAt || '从未使用' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">调用次数</span>
          <span class="meta-value">{{ apiKeyData.usageCount || 0 }} 次</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">创建时间</span>
          <span class="meta-value">{{ apiKeyData.createdAt || '-' }}</span>
        </div>
      </div>

      <div class="danger-zone">
        <div class="danger-title">危险操作</div>
        <div class="danger-row">
          <div class="danger-info">
            <div class="danger-name">重新生成 Key</div>
            <div class="danger-desc">旧 Key 将立即失效，所有使用中的外部集成将中断</div>
          </div>
          <button class="btn-danger" :disabled="regenerating" @click="confirmRegenerate">
            {{ regenerating ? '生成中…' : '重新生成' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'SystemApiKeyView',
  data() {
    return {
      loading: false,
      apiKeyData: {},
      isMasked: true,
      copied: false,
      regenerating: false,
      editingClinicId: false,
      clinicIdInput: ''
    }
  },
  computed: {
    displayedKey() {
      if (!this.apiKeyData.key) return '暂无 Key'
      return this.isMasked ? this.apiKeyData.maskedKey : this.apiKeyData.key
    }
  },
  mounted() {
    // 先从 localStorage 读取已保存的 Key，减少后端请求
    const saved = localStorage.getItem('clinic_api_key')
    if (saved) {
      this.apiKeyData = { key: saved, maskedKey: this.maskKey(saved), name: '默认Key', isEnabled: true }
    }
    this.fetchApiKey()
  },
  methods: {
    async fetchApiKey() {
      this.loading = true
      try {
        // 单 Key 模式下不传 clinicId，后端自动查询唯一记录
        const res = await axios.get('/api/api-key')
        if (res.data.code === '200' && res.data.data) {
          this.apiKeyData = res.data.data
          this.isMasked = true
          // 将明文 Key 存入 localStorage，供前端全局请求拦截器使用
          if (res.data.data.key) {
            localStorage.setItem('clinic_api_key', res.data.data.key)
          }
        }
      } catch (e) {
        this.$message.error('获取 API Key 失败')
      } finally {
        this.loading = false
      }
    },
    startEditClinicId() {
      this.clinicIdInput = this.apiKeyData.clinicId || '1'
      this.editingClinicId = true
    },
    cancelEditClinicId() {
      this.editingClinicId = false
      this.clinicIdInput = ''
    },
    async saveClinicId() {
      const newClinicId = this.clinicIdInput.trim()
      if (!newClinicId) {
        this.$message.warning('诊所ID不能为空')
        return
      }
      if (!/^[a-zA-Z0-9_-]+$/.test(newClinicId)) {
        this.$message.warning('诊所ID只能包含字母、数字、下划线和横线')
        return
      }
      const oldClinicId = this.apiKeyData.clinicId || '1'
      if (oldClinicId === newClinicId) {
        this.editingClinicId = false
        return
      }
      try {
        const res = await axios.post('/api/api-key/update-clinic-id', null, {
          params: { clinicId: oldClinicId, newClinicId: newClinicId }
        })
        if (res.data.code === '200' && res.data.data) {
          this.apiKeyData = res.data.data
          this.editingClinicId = false
          this.$message.success('诊所ID已更新为: ' + newClinicId)
        } else {
          this.$message.error(res.data.msg || '更新失败')
        }
      } catch (e) {
        this.$message.error('请求失败')
      }
    },
    toggleMask() {
      this.isMasked = !this.isMasked
    },
    copyKey() {
      if (!this.apiKeyData.key || this.isMasked) return
      navigator.clipboard.writeText(this.apiKeyData.key).then(() => {
        this.copied = true
        setTimeout(() => { this.copied = false }, 2000)
      }).catch(() => {
        this.$message.warning('复制失败，请手动复制')
      })
    },
    confirmRegenerate() {
      this.$confirm('重新生成后，旧 Key 将立即失效，所有外部集成将中断。是否继续？', '确认重新生成', {
        confirmButtonText: '确认生成',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doRegenerate()
      }).catch(() => {})
    },
    async doRegenerate() {
      this.regenerating = true
      try {
        const currentClinicId = this.apiKeyData.clinicId || '1'
        const res = await axios.post('/api/api-key/regenerate', null, { params: { clinicId: currentClinicId } })
        if (res.data.code === '200' && res.data.data) {
          this.apiKeyData = res.data.data
          this.isMasked = false
          // 更新 localStorage 中的 Key
          if (res.data.data.key) {
            localStorage.setItem('clinic_api_key', res.data.data.key)
          }
          this.$message.success('API Key 已重新生成')
        } else {
          this.$message.error(res.data.msg || '生成失败')
        }
      } catch (e) {
        this.$message.error('请求失败')
      } finally {
        this.regenerating = false
      }
    },
    maskKey(key) {
      if (!key || key.length <= 9) return '******'
      return key.substring(0, 6) + '******' + key.substring(key.length - 3)
    }
  }
}
</script>

<style scoped>
.api-key-page {
  max-width: 720px;
  margin: 0 auto;
}

.loading-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 0;
  color: #64748b;
  font-size: 14px;
}

.api-key-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 20px;
  padding: 28px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.card-subtitle {
  font-size: 12px;
  color: var(--apple-text-secondary);
  margin-top: 2px;
}

.key-display {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-divider);
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 20px;
}

.key-value {
  flex: 1;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-text-primary);
  word-break: break-all;
  user-select: all;
}

.key-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.btn-copy,
.btn-toggle {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid var(--apple-divider);
  background: var(--apple-surface);
  color: var(--apple-text-primary);
  transition: all 0.2s ease;
}

.btn-copy:hover:not(:disabled),
.btn-toggle:hover {
  background: var(--apple-bg-hover);
}

.btn-copy:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-copy.copied {
  background: #10b981;
  color: #fff;
  border-color: #10b981;
}

.key-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
  padding: 16px;
  background: var(--apple-bg-primary);
  border-radius: 12px;
  margin-bottom: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.meta-label {
  color: var(--apple-text-tertiary);
  min-width: 64px;
}

.meta-value {
  color: var(--apple-text-primary);
  font-weight: 500;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.enabled {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.disabled {
  background: #fee2e2;
  color: #991b1b;
}

.clinic-id-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.clinic-id {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
  color: #0f172a;
}

.clinic-id-input {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  padding: 4px 10px;
  border: 1px solid var(--apple-divider);
  border-radius: 8px;
  background: var(--apple-bg-primary);
  color: var(--apple-text-primary);
  width: 180px;
  outline: none;
}

.clinic-id-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

.btn-text {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: transparent;
  color: #3b82f6;
  transition: all 0.2s ease;
}

.btn-text:hover {
  background: #eff6ff;
}

.btn-confirm {
  color: #10b981;
}

.btn-confirm:hover {
  background: #d1fae5;
}

.btn-cancel {
  color: #64748b;
}

.btn-cancel:hover {
  background: #f1f5f9;
}

.danger-zone {
  border-top: 1px solid var(--apple-divider);
  padding-top: 20px;
}

.danger-title {
  font-size: 14px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 14px;
}

.danger-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.danger-info {
  flex: 1;
}

.danger-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-text-primary);
}

.danger-desc {
  font-size: 12px;
  color: var(--apple-text-secondary);
  margin-top: 2px;
}

.btn-danger {
  padding: 8px 18px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid #fecaca;
  background: #fee2e2;
  color: #991b1b;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.btn-danger:hover:not(:disabled) {
  background: #fecaca;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .api-key-card {
    padding: 18px;
  }

  .key-display {
    flex-direction: column;
    align-items: stretch;
  }

  .key-actions {
    justify-content: flex-end;
  }

  .key-meta {
    grid-template-columns: 1fr;
  }

  .danger-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
