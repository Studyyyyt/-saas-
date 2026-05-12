<template>
  <div class="provider-config-page">
    <!-- 页面标题 -->
    <div class="provider-config-header">
      <div class="provider-config-header-left">
        <h1 class="provider-config-title">模型供应商配置</h1>
        <p class="provider-config-subtitle">配置 AI 对话所使用的模型供应商接口、密钥与模型参数</p>
      </div>
      <el-button type="primary" icon="el-icon-check" :loading="saveLoading" @click="saveConfig">
        保存配置
      </el-button>
    </div>

    <!-- 配置表单 -->
    <div v-loading="loading" class="provider-form-card">
      <div class="provider-form">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">供应商名称 <span class="required">*</span></label>
            <el-input v-model="form.providerName" placeholder="如：OpenAI、Azure、硅基流动" />
          </div>
          <div class="form-group">
            <label class="form-label">模型名称 <span class="required">*</span></label>
            <el-input v-model="form.modelName" placeholder="如：gpt-4o、deepseek-chat" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">API 基础地址 <span class="required">*</span></label>
          <el-input v-model="form.baseUrl" placeholder="https://api.openai.com/v1" />
          <div class="form-hint">请填写完整的 API 基础地址，不需要带 /responses 后缀</div>
        </div>

        <div class="form-group">
          <label class="form-label">API 密钥 <span class="required">*</span></label>
          <el-input
            v-model="form.apiKey"
            :type="showApiKey ? 'text' : 'password'"
            placeholder="sk-..."
          >
            <el-button slot="append" icon="el-icon-view" @click="showApiKey = !showApiKey" />
          </el-input>
          <div class="form-hint">密钥仅在保存时传输，返回后会脱敏显示。若不想修改密钥，留空即可保留原值</div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">API 类型</label>
            <el-select v-model="form.apiType" placeholder="请选择" style="width: 100%">
              <el-option label="Chat Completions（通用兼容，推荐）" value="chat_completions" />
              <el-option label="Responses（OpenAI 原生）" value="responses" />
            </el-select>
            <div class="form-hint">国内供应商（DeepSeek、硅基流动、OpenRouter 等）请选择 Chat Completions</div>
          </div>
          <div class="form-group">
            <label class="form-label">推理力度</label>
            <el-select v-model="form.reasoningEffort" placeholder="请选择" style="width: 100%">
              <el-option label="低（响应更快）" value="low" />
              <el-option label="中（平衡）" value="medium" />
              <el-option label="高（更深入）" value="high" />
            </el-select>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">最大输出 Token</label>
          <el-input-number v-model="form.maxOutputTokens" :min="500" :max="16000" :step="500" style="width: 100%" />
        </div>

        <div class="form-group">
          <label class="form-label">启用状态</label>
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
          <div class="form-hint">禁用后将自动回退到 application.yml 中的默认配置</div>
        </div>
      </div>
    </div>

    <!-- 测试连接 -->
    <div class="provider-form-card" style="margin-top: 16px;">
      <div class="provider-form">
        <div class="test-connection-header">
          <div class="form-label">连接测试</div>
          <el-button size="small" icon="el-icon-connection" :loading="testLoading" @click="testConnection">
            测试连接
          </el-button>
        </div>
        <div v-if="testResult" class="test-result" :class="testResult.status">
          <div class="test-result-title">
            <i :class="testResult.status === 'success' ? 'el-icon-success' : 'el-icon-error'" />
            {{ testResult.status === 'success' ? '连接成功' : '连接失败' }}
          </div>
          <div class="test-result-text">{{ testResult.message }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchModelProviderConfig, saveModelProviderConfig } from '@/utils/aiStreamClient'

export default {
  name: 'ModelProviderConfigView',
  data() {
    return {
      loading: false,
      saveLoading: false,
      testLoading: false,
      showApiKey: true,
      testResult: null,
      form: {
        id: null,
        providerName: '',
        baseUrl: '',
        apiKey: '',
        modelName: '',
        apiType: 'chat_completions',
        reasoningEffort: 'medium',
        maxOutputTokens: 3000,
        enabled: true
      }
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    async loadConfig() {
      this.loading = true
      try {
        const res = await fetchModelProviderConfig()
        if (res.code === '200' && res.data) {
          const data = res.data
          this.form.id = data.id || null
          this.form.providerName = data.providerName || ''
          this.form.baseUrl = data.baseUrl || ''
          this.form.modelName = data.modelName || ''
          this.form.apiType = data.apiType || 'chat_completions'
          this.form.reasoningEffort = data.reasoningEffort || 'medium'
          this.form.maxOutputTokens = data.maxOutputTokens || 3000
          this.form.enabled = data.enabled !== false
          // apiKey 返回的是脱敏的，显示脱敏值方便用户确认已配置
          this.form.apiKey = data.apiKey || ''
        }
      } catch (e) {
        this.$message.error('加载配置失败')
      } finally {
        this.loading = false
      }
    },
    async saveConfig() {
      if (!this.form.providerName.trim() || !this.form.baseUrl.trim() || !this.form.modelName.trim()) {
        this.$message.warning('请填写必填项')
        return
      }
      this.saveLoading = true
      try {
        const payload = {
          id: this.form.id,
          providerName: this.form.providerName.trim(),
          baseUrl: this.form.baseUrl.trim(),
          modelName: this.form.modelName.trim(),
          apiType: this.form.apiType || 'chat_completions',
          reasoningEffort: this.form.reasoningEffort,
          maxOutputTokens: this.form.maxOutputTokens,
          enabled: this.form.enabled
        }
        if (this.form.apiKey.trim()) {
          payload.apiKey = this.form.apiKey.trim()
        }
        const res = await saveModelProviderConfig(payload)
        if (res.code === '200') {
          this.$message.success('保存成功')
          if (res.data && res.data.id) {
            this.form.id = res.data.id
          }
          this.form.apiKey = ''
        } else {
          this.$message.error(res.msg || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },
    async testConnection() {
      this.testLoading = true
      this.testResult = null
      try {
        if (!this.form.baseUrl.trim() || !this.form.modelName.trim()) {
          this.testResult = {
            status: 'error',
            message: '请填写 API 基础地址和模型名称后再测试'
          }
          return
        }
        const payload = {
          id: this.form.id,
          baseUrl: this.form.baseUrl.trim(),
          modelName: this.form.modelName.trim(),
          apiType: this.form.apiType || 'chat_completions'
        }
        if (this.form.apiKey.trim()) {
          payload.apiKey = this.form.apiKey.trim()
        }
        const res = await fetch('/api/model-providers/test', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })
        const data = await res.json()
        if (data.code === '200') {
          this.testResult = {
            status: 'success',
            message: data.data || '连接成功'
          }
        } else {
          this.testResult = {
            status: 'error',
            message: data.msg || '连接失败'
          }
        }
      } catch (e) {
        this.testResult = {
          status: 'error',
          message: '请求失败：' + (e.message || '未知错误')
        }
      } finally {
        this.testLoading = false
      }
    }
  }
}
</script>

<style scoped>
.provider-config-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.provider-config-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
}

.provider-config-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0f172a;
  line-height: 1.2;
}

.provider-config-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: #64748b;
}

.provider-form-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 20px;
  padding: 28px;
}

.provider-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.test-connection-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.test-result {
  margin-top: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 13px;
}

.test-result.success {
  background: rgba(5, 150, 105, 0.06);
  border: 1px solid rgba(5, 150, 105, 0.15);
  color: #047857;
}

.test-result.error {
  background: rgba(239, 68, 68, 0.06);
  border: 1px solid rgba(239, 68, 68, 0.15);
  color: #b91c1c;
}

.test-result-title {
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.test-result-text {
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .provider-config-page {
    padding: 16px;
  }

  .provider-config-title {
    font-size: 22px;
  }

  .provider-config-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .form-row {
    flex-direction: column;
  }

  .provider-form-card {
    padding: 20px;
  }
}
</style>
