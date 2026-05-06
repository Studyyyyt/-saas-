<template>
  <div class="insurance-page">
    <el-card class="page-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">医保管理</div>
          <h2>医保配置</h2>
          <p>维护医保平台、机构编码、签名参数与加密方式，作为后续真实医保接口接入的配置中心。</p>
        </div>
        <el-button type="primary" icon="el-icon-refresh" @click="loadConfig">重新加载</el-button>
      </div>
    </el-card>

    <el-card class="form-card" shadow="never">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="平台编码"><el-input v-model="form.platform_code" placeholder="如：HN_CHANGSHA" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="平台名称"><el-input v-model="form.platform_name" placeholder="如：长沙医保平台" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="机构编码"><el-input v-model="form.org_code" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="机构名称"><el-input v-model="form.org_name" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="统筹区编码"><el-input v-model="form.region_code" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="加密方式"><el-input v-model="form.encryption_type" placeholder="如：SM2/SM4、RSA、NONE" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="接口基础地址"><el-input v-model="form.api_base_url" placeholder="https://..." /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="App ID"><el-input v-model="form.app_id" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="App Secret"><el-input v-model="form.app_secret" show-password /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="签名Key"><el-input v-model="form.sign_key" show-password /></el-form-item></el-col>
        </el-row>
        <el-form-item label="扩展配置JSON"><el-input v-model="form.ext_json" type="textarea" :rows="5" placeholder="可放地区特有参数、接口版本、证书别名等" /></el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="enabledSwitch" active-text="启用" inactive-text="停用"></el-switch>
        </el-form-item>
      </el-form>
      <div class="footer-actions">
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const emptyForm = () => ({
  platform_code: '',
  platform_name: '',
  api_base_url: '',
  org_code: '',
  org_name: '',
  app_id: '',
  app_secret: '',
  sign_key: '',
  encryption_type: '',
  region_code: '',
  enabled: 1,
  ext_json: ''
})

export default {
  name: 'InsuranceConfigView',
  data() {
    return {
      form: emptyForm()
    }
  },
  computed: {
    enabledSwitch: {
      get() {
        return this.form.enabled === 1
      },
      set(value) {
        this.form.enabled = value ? 1 : 0
      }
    }
  },
  mounted() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      axios.get('/insurance/config').then(res => {
        if (res.data.code === '200') {
          this.form = Object.assign(emptyForm(), res.data.data || {})
        } else {
          this.$message.error(res.data.msg || '加载医保配置失败')
        }
      }).catch(() => {
        showApiError(this, '加载医保配置', error)
      })
    },
    resetForm() {
      this.loadConfig()
    },
    saveConfig() {
      if (!this.form.platform_code || !this.form.platform_name) {
        this.$message.warning('平台编码和平台名称不能为空')
        return
      }
      axios.post('/insurance/config', this.form).then(res => {
        if (res.data.code === '200') {
          this.$message.success('医保配置保存成功')
          this.form = Object.assign(emptyForm(), res.data.data || {})
        } else {
          this.$message.error(res.data.msg || '医保配置保存失败')
        }
      }).catch(() => {
        this.$message.error('医保配置保存失败')
      })
    }
  }
}
</script>

<style scoped>
.insurance-page { display:flex; flex-direction:column; gap:18px; }
.page-card, .form-card { border-radius:18px; }
.page-head { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; }
.page-kicker { color:#2563eb; font-size:13px; font-weight:600; margin-bottom:8px; }
.page-head h2 { margin:0; font-size:28px; color:#0f172a; }
.page-head p { margin:10px 0 0; color:#64748b; line-height:1.7; max-width:760px; }
.footer-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:16px; }
@media (max-width: 1200px) {
  .page-head { flex-direction:column; }
}
</style>
