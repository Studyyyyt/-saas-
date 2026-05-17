<template>
  <div class="ai-overview-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">AI 中心</h1>
        <p class="page-subtitle">管理 Agent 配置与 API Key</p>
      </div>
      <div class="page-header-right">
        <el-button size="small" icon="el-icon-refresh" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </div>

    <!-- Agent 配置表格 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">Agent 配置</div>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openEditor()">新增 Agent</el-button>
      </div>
      <el-table :data="agentList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="功能名称" min-width="140" />
        <el-table-column prop="agentKey" label="AgentKey" min-width="140" />
        <el-table-column prop="usageLocation" label="用途位置" min-width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.usageLocation || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endpointUrl" label="Webhook 地址" min-width="200">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.endpointUrl" placement="top">
              <span class="ellipsis-text">{{ scope.row.endpointUrl || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="responseType" label="响应类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.responseType === 'sse' ? 'warning' : 'success'">{{ scope.row.responseType || 'json' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeoutSeconds" label="超时" width="80" align="center">
          <template slot-scope="scope">{{ scope.row.timeoutSeconds || 60 }}s</template>
        </el-table-column>
        <el-table-column prop="authType" label="认证" width="90" align="center">
          <template slot-scope="scope">{{ scope.row.authType || 'none' }}</template>
        </el-table-column>
        <el-table-column label="请求模板" min-width="120">
          <template slot-scope="scope">
            <el-tooltip v-if="scope.row.requestTemplate" :content="scope.row.requestTemplate" placement="top">
              <span class="ellipsis-text" style="color:#409eff">已配置</span>
            </el-tooltip>
            <span v-else class="ellipsis-text" style="color:#909399">默认协议</span>
          </template>
        </el-table-column>
        <el-table-column label="快捷指令" min-width="140">
          <template slot-scope="scope">
            <span class="ellipsis-text">{{ (scope.row.chips && scope.row.chips[0]) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-edit" @click="openEditor(scope.row)">编辑</el-button>
            <el-button type="text" size="small" icon="el-icon-delete" class="danger-text" @click="removeAgent(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 全局响应字段配置 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">全局响应字段配置</div>
      </div>
      <div class="api-key-desc">
        设置 n8n 工作流返回的内容字段名。所有 AI Agent 统一使用此字段提取回复内容，留空则自动识别。
      </div>
      <div class="api-key-row" style="margin-top: 12px;">
        <el-input v-model="responseField" size="small" placeholder="如：content、reply、message" style="width: 240px;" maxlength="50" />
        <el-button size="small" type="primary" :loading="saveFieldLoading" @click="saveResponseField">保存</el-button>
      </div>
    </div>

    <!-- API Key 管理卡片 -->
    <div class="section-card">
      <div class="section-title" style="margin-bottom: 12px;">API Key 管理</div>
      <div class="api-key-desc">
        API Key 用于第三方平台（如 n8n）调用系统接口获取数据。请妥善保管，泄露可能导致数据风险。
      </div>
      <div class="api-key-row">
        <div class="api-key-value">{{ maskedApiKey }}</div>
        <el-button size="small" icon="el-icon-document-copy" @click="copyApiKey">复制</el-button>
        <el-button size="small" icon="el-icon-refresh" :loading="regenerating" @click="regenerateApiKey">重新生成</el-button>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <el-dialog
      :title="editingId ? '编辑 Agent' : '新增 Agent'"
      :visible.sync="editorVisible"
      width="720px"
      :close-on-click-modal="false"
      :modal="false"
    >
      <el-form :model="form" label-width="110px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="功能名称">
              <el-input v-model="form.name" placeholder="请输入功能名称" maxlength="20" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AgentKey">
              <el-input v-model="form.agentKey" placeholder="唯一标识" maxlength="40" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="用途位置">
          <el-input v-model="form.usageLocation" placeholder="如：新增病历页、咨询分析" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="功能描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="描述该 Agent 的用途" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="Webhook 地址">
          <el-input v-model="form.endpointUrl" placeholder="https://n8n.xxx.com/webhook/xxx" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="认证类型">
              <el-select v-model="form.authType" placeholder="请选择" style="width:100%">
                <el-option label="无认证" value="none" />
                <el-option label="Bearer Token" value="bearer" />
                <el-option label="Basic Auth" value="basic" />
                <el-option label="API Key" value="api_key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证令牌">
              <el-input v-model="form.authToken" type="password" placeholder="Token / Key" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="响应类型">
              <el-select v-model="form.responseType" placeholder="请选择" style="width:100%">
                <el-option label="JSON（一次性返回）" value="json" />
                <el-option label="SSE（流式返回）" value="sse" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时秒数">
              <el-input-number v-model="form.timeoutSeconds" :min="5" :max="300" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="快捷指令">
              <el-input v-model="form.chipText" placeholder="如：分析客户意向" maxlength="40" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请求模板">
          <el-input v-model="form.requestTemplate" type="textarea" :rows="4" placeholder='请填写JSON格式的请求模板，支持变量替换' />
          <div v-pre style="font-size:12px;color:#909399;margin-top:4px">
            支持 {{变量}} 替换，留空则直接发送完整协议。可用变量：message、account_id、account_name、session_id、clinic_id 等。
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="editorVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saveLoading" @click="saveAgent">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'

export default {
  name: 'AIOverviewView',
  data() {
    return {
      loading: false,
      agentList: [],
      apiKey: '',
      regenerating: false,
      editorVisible: false,
      editingId: null,
      saveLoading: false,
      responseField: 'content',
      saveFieldLoading: false,
      form: {
        name: '',
        usageLocation: '',
        agentKey: '',
        description: '',
        endpointUrl: '',
        authType: 'none',
        authToken: '',
        requestTemplate: '',
        responseType: 'json',
        timeoutSeconds: 60,
        sortOrder: 0,
        chipText: ''
      }
    }
  },
  computed: {
    maskedApiKey() {
      const key = this.apiKey || ''
      if (key.length <= 9) return key || '暂无 API Key'
      return key.slice(0, 6) + '******' + key.slice(-3)
    },
    clinicId() {
      const session = getAdminSession() || {}
      return session.clinicId || session.id || ''
    }
  },
  created() {
    this.loadData()
    this.loadApiKey()
    this.loadGlobalConfig()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.get('/api/ai-agent-configs', { params: { accountId } })
        if (res.data && res.data.code === '200') {
          this.agentList = Array.isArray(res.data.data) ? res.data.data : []
        } else {
          this.agentList = []
          console.warn('[AIOverviewView] /api/ai-agent-configs 接口未返回成功数据')
        }
      } catch (error) {
        console.error('加载 Agent 列表失败:', error)
        this.agentList = []
      } finally {
        this.loading = false
      }
    },

    async loadApiKey() {
      try {
        const res = await axios.get('/api/api-key', { params: { clinicId: this.clinicId } })
        if (res.data && res.data.code === '200') {
          this.apiKey = (res.data.data && res.data.data.key) || ''
        }
      } catch (error) {
        console.error('加载 API Key 失败:', error)
        this.apiKey = ''
      }
    },

    async loadGlobalConfig() {
      try {
        const res = await axios.get('/api/ai/global-config/response_field')
        if (res.data && res.data.code === '200' && res.data.data) {
          this.responseField = res.data.data.configValue || 'content'
        }
      } catch (error) {
        console.error('加载全局配置失败:', error)
      }
    },

    async saveResponseField() {
      if (!this.responseField.trim()) {
        this.$message.warning('请输入字段名')
        return
      }
      this.saveFieldLoading = true
      try {
        const res = await axios.put('/api/ai/global-config/response_field', {
          configValue: this.responseField.trim()
        })
        if (res.data && res.data.code === '200') {
          this.$message.success('保存成功')
        } else {
          this.$message.error(res.data?.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存全局配置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveFieldLoading = false
      }
    },

    copyApiKey() {
      if (!this.apiKey) {
        this.$message.warning('暂无 API Key 可复制')
        return
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(this.apiKey).then(() => {
          this.$message.success('API Key 已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopy(this.apiKey)
        })
      } else {
        this.fallbackCopy(this.apiKey)
      }
    },

    fallbackCopy(text) {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.$message.success('API Key 已复制到剪贴板')
      } catch (e) {
        this.$message.error('复制失败，请手动复制')
      }
      document.body.removeChild(textarea)
    },

    regenerateApiKey() {
      this.$confirm('重新生成 API Key 后，旧 Key 将立即失效，是否继续？', '确认重新生成', {
        confirmButtonText: '重新生成',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.regenerating = true
        try {
          const res = await axios.post('/api/api-key/regenerate', null, { params: { clinicId: this.clinicId } })
          if (res.data && res.data.code === '200') {
            this.apiKey = (res.data.data && res.data.data.key) || ''
            this.$message.success('API Key 已重新生成')
          } else {
            this.$message.error(res.data?.msg || '重新生成失败')
          }
        } catch (error) {
          console.error('重新生成 API Key 失败:', error)
          this.$message.error('重新生成失败')
        } finally {
          this.regenerating = false
        }
      }).catch(() => {})
    },

    openEditor(row) {
      if (row) {
        this.editingId = row.id
        this.form = {
          name: row.name || '',
          usageLocation: row.usageLocation || '',
          agentKey: row.agentKey || '',
          description: row.description || '',
          endpointUrl: row.endpointUrl || '',
          authType: row.authType || 'none',
          authToken: row.authToken || '',
          requestTemplate: row.requestTemplate || '',
          responseType: row.responseType || 'json',
          timeoutSeconds: row.timeoutSeconds || 60,
          sortOrder: row.sortOrder || 0,
          chipText: (row.chips && row.chips[0]) || ''
        }
      } else {
        this.editingId = null
        this.form = {
          name: '',
          usageLocation: '',
          agentKey: '',
          description: '',
          endpointUrl: '',
          authType: 'none',
          authToken: '',
          requestTemplate: '',
          responseType: 'json',
          timeoutSeconds: 60,
          sortOrder: 0,
          chipText: ''
        }
      }
      this.editorVisible = true
    },

    async saveAgent() {
      if (!this.form.name.trim()) {
        this.$message.error('请输入功能名称')
        return
      }
      if (!this.form.agentKey.trim()) {
        this.$message.error('请输入 AgentKey')
        return
      }
      this.saveLoading = true
      try {
        const session = getAdminSession() || {}
        const payload = {
          ...this.form,
          accountId: session.id || null,
          chips: this.form.chipText ? [this.form.chipText] : []
        }
        delete payload.chipText
        let res
        if (this.editingId) {
          res = await axios.put(`/api/ai-agent-configs/${this.editingId}`, payload)
        } else {
          res = await axios.post('/api/ai-agent-configs', payload)
        }
        if (res.data && res.data.code === '200') {
          this.$message.success(this.editingId ? '保存成功' : '新增成功')
          this.editorVisible = false
          this.loadData()
        } else {
          this.$message.error(res.data?.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存 Agent 失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },

    removeAgent(row) {
      this.$confirm(`确定删除「${row.name || row.agentKey}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await axios.delete(`/api/ai-agent-configs/${row.id}`)
          if (res.data && res.data.code === '200') {
            this.$message.success('已删除')
            this.loadData()
          } else {
            this.$message.error(res.data?.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除 Agent 失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.ai-overview-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-header-left {
  flex: 1;
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

/* 卡片区块 */
.section-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  transition: box-shadow 0.3s ease;
}

.section-card:hover {
  box-shadow: var(--apple-shadow-lg), var(--apple-surface-shadow-inset);
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

.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.danger-text {
  color: #f56c6c;
}

/* API Key 区域 */
.api-key-desc {
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin-bottom: 12px;
  line-height: 1.5;
}

.api-key-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.api-key-value {
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  background: var(--apple-bg-secondary);
  padding: 8px 14px;
  border-radius: 8px;
  letter-spacing: 0.04em;
  word-break: break-all;
}

/* 响应式 */
@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .api-key-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
