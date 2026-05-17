<template>
  <div class="insurance-page">
    <el-card class="page-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">医保管理</div>
          <h2>患者医保档案</h2>
          <p>按患者 ID 读取或维护医保档案，用于后续做身份校验、就诊登记与门诊结算。</p>
        </div>
      </div>
    </el-card>

    <el-card class="query-card" shadow="never">
      <div class="query-row">
        <el-input-number v-model="patientId" :min="1" controls-position="right" placeholder="患者ID"></el-input-number>
        <el-button type="primary" icon="el-icon-search" @click="loadProfile">加载档案</el-button>
        <el-button @click="resetForm">清空</el-button>
      </div>
    </el-card>

    <el-card class="form-card" shadow="never">
      <el-form :model="form" label-width="130px">
        <el-form-item label="患者ID">
          <el-input v-model="form.patient_id" disabled></el-input>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="医保人员编号"><el-input v-model="form.insurance_person_no" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="医保登记姓名"><el-input v-model="form.person_name" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="身份证号"><el-input v-model="form.id_card_no" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="参保地编码"><el-input v-model="form.insured_region_code" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="参保类型"><el-input v-model="form.insured_type" placeholder="如：职工/居民" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="医保卡号"><el-input v-model="form.card_no" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="卡类型"><el-input v-model="form.card_type" placeholder="电子凭证/实体卡" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="性别"><el-input v-model="form.gender" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出生日期"><el-input v-model="form.birthday" placeholder="yyyy-MM-dd" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="最近认证流水号"><el-input v-model="form.last_auth_no" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最近校验时间"><el-input :value="formatDateTime(form.last_verified_at)" disabled /></el-form-item></el-col>
        </el-row>
        <el-form-item label="扩展JSON"><el-input v-model="form.ext_json" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <div class="footer-actions">
        <el-button @click="resetForm">清空</el-button>
        <el-button type="primary" @click="saveProfile">保存档案</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const emptyForm = () => ({
  patient_id: '',
  insurance_person_no: '',
  id_card_no: '',
  insured_region_code: '',
  insured_type: '',
  card_no: '',
  card_type: '',
  person_name: '',
  gender: '',
  birthday: '',
  phone: '',
  status: 1,
  last_auth_no: '',
  last_verified_at: '',
  ext_json: ''
})

export default {
  name: 'InsurancePatientProfileView',
  data() {
    return {
      patientId: null,
      form: emptyForm()
    }
  },
  methods: {
    loadProfile() {
      if (!this.patientId) {
        this.$message.warning('请先输入患者ID')
        return
      }
      axios.get(`/insurances/patient-profile/${this.patientId}`).then(res => {
        if (res.data.code === '200') {
          this.form = Object.assign(emptyForm(), res.data.data || {}, { patient_id: this.patientId })
          if (!res.data.data) {
            this.$message.info('该患者暂无医保档案，已为你初始化空白表单')
          }
        } else {
          this.$message.error(res.data.msg || '加载医保档案失败')
        }
      }).catch(() => {
        showApiError(this, '加载医保档案', error)
      })
    },
    saveProfile() {
      if (!this.patientId) {
        this.$message.warning('请先输入患者ID')
        return
      }
      const payload = Object.assign({}, this.form, { patient_id: Number(this.patientId) })
      axios.post('/insurances/patient-profile', payload).then(res => {
        if (res.data.code === '200') {
          this.$message.success('医保档案保存成功')
          this.form = Object.assign(emptyForm(), res.data.data || {}, { patient_id: Number(this.patientId) })
        } else {
          this.$message.error(res.data.msg || '医保档案保存失败')
        }
      }).catch(() => {
        this.$message.error('医保档案保存失败')
      })
    },
    resetForm() {
      this.form = emptyForm()
    },
    formatDateTime(value) {
      if (!value) return '-'
      return String(value).replace('T', ' ').slice(0, 19)
    }
  }
}
</script>

<style scoped>
.insurance-page { display:flex; flex-direction:column; gap:18px; }
.page-card, .query-card, .form-card { border-radius:18px; }
.page-kicker { color:#2563eb; font-size:13px; font-weight:600; margin-bottom:8px; }
.page-head h2 { margin:0; font-size:28px; color:#0f172a; }
.page-head p { margin:10px 0 0; color:#64748b; line-height:1.7; }
.query-row { display:flex; gap:10px; align-items:center; }
.footer-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:16px; }
</style>
