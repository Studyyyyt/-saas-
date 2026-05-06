<template>
  <div class="register-page">
    <div class="register-shell">
      <section class="hero-card">
        <div class="hero-kicker">舒澳口腔</div>
        <h1>在线挂号</h1>
        <p>填写基本信息后，系统会自动建档到患者列表，门诊可直接继续预约、接诊和写病历。</p>
        <div class="hero-tags">
          <span>手机提交</span>
          <span>快速建档</span>
          <span>前台同步可见</span>
        </div>
      </section>

      <section class="form-card">
        <div class="section-head">
          <div class="section-title">基本信息</div>
          <div class="section-subtitle">带 * 为必填项</div>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
          <el-form-item label="患者姓名 *" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
          </el-form-item>

          <el-form-item label="性别 *" prop="gender">
            <el-radio-group v-model="form.gender" class="gender-group">
              <el-radio-button label="男"></el-radio-button>
              <el-radio-button label="女"></el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="年龄 *" prop="age">
            <el-input-number v-model="form.age" :min="0" :max="150" controls-position="right" class="full-input"></el-input-number>
          </el-form-item>

          <el-form-item label="手机号码 *" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号"></el-input>
          </el-form-item>

          <el-form-item label="患者来源 *" prop="customer_source">
            <el-select v-model="form.customer_source" placeholder="请选择患者来源" class="full-input">
              <el-option
                v-for="item in customerSourceOptions"
                :key="item"
                :label="item"
                :value="item"
              ></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="介绍人（可选）">
            <ReferralSelector :value="referralForm" @input="handleReferralChange" />
          </el-form-item>
        </el-form>

        <div class="submit-bar">
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="submit">提交挂号</el-button>
          <div class="submit-tip">提交成功后会自动保存到患者列表</div>
        </div>
      </section>

      <section v-if="savedPatient" class="success-card">
        <div class="success-badge">提交成功</div>
        <div class="success-title">患者已建档</div>
        <div class="success-meta">患者ID：{{ savedPatient.id }}　姓名：{{ savedPatient.name }}</div>
        <el-button plain class="success-btn" @click="resetForm">继续新增</el-button>
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import ReferralSelector from '@/components/ReferralSelector.vue'
import { CUSTOMER_SOURCE_OPTIONS } from '@/utils/consultationOptions'

const PATIENT_PHONE_REGEX = /^\d{11}$/

function buildEmptyForm() {
  return {
    name: '',
    gender: '',
    age: null,
    phone: '',
    customer_source: ''
  }
}

function buildEmptyReferralState() {
  return {
    referrer_type: '',
    referrer_patient_id: null,
    referrer_patient_name: '',
    external_referrer_type: '',
    external_referrer_name: '',
    external_referrer_contact: '',
    referral_remark: ''
  }
}

export default {
  name: 'PatientRegisterH5',
  components: {
    ReferralSelector
  },
  data() {
    return {
      submitting: false,
      savedPatient: null,
      customerSourceOptions: CUSTOMER_SOURCE_OPTIONS,
      referralForm: buildEmptyReferralState(),
      form: buildEmptyForm(),
      rules: {
        name: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
        gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
        age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: PATIENT_PHONE_REGEX, message: '手机号码需为11位数字', trigger: 'blur' }
        ],
        customer_source: [{ required: true, message: '请选择患者来源', trigger: 'change' }]
      }
    }
  },
  methods: {
    handleReferralChange(value) {
      this.referralForm = Object.assign(buildEmptyReferralState(), value || {})
      if (this.hasReferralPayload()) {
        this.form.customer_source = '转介绍'
      }
    },
    buildPayload() {
      return {
        name: String(this.form.name || '').trim(),
        gender: String(this.form.gender || '').trim(),
        age: this.form.age === null || this.form.age === undefined || this.form.age === '' ? null : Number(this.form.age),
        phone: String(this.form.phone || '').trim(),
        customer_source: this.hasReferralPayload() ? '转介绍' : String(this.form.customer_source || '').trim(),
        referrer_type: this.referralForm.referrer_type || '',
        referrer_patient_id: this.referralForm.referrer_patient_id || null,
        referrer_patient_name: this.referralForm.referrer_patient_name || '',
        external_referrer_type: this.referralForm.external_referrer_type || '',
        external_referrer_name: this.referralForm.external_referrer_name || '',
        external_referrer_contact: this.referralForm.external_referrer_contact || '',
        referral_remark: this.referralForm.referral_remark || ''
      }
    },
    submit() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        if (this.referralForm.referrer_type === 'patient' && !this.referralForm.referrer_patient_id) {
          this.$message.warning('请选择有效的介绍患者')
          return
        }
        if (this.referralForm.referrer_type === 'external' && !String(this.referralForm.external_referrer_name || '').trim()) {
          this.$message.warning('请输入外部介绍人姓名')
          return
        }
        this.submitting = true
        axios.post('/patients/add', this.buildPayload()).then(res => {
          if (res.data.code !== '200') {
            this.$message.error(res.data.msg || '提交失败')
            return
          }
          this.savedPatient = res.data.data || null
          this.$message.success('挂号提交成功')
          this.form = buildEmptyForm()
          this.referralForm = buildEmptyReferralState()
          this.$nextTick(() => {
            if (this.$refs.formRef) this.$refs.formRef.resetFields()
          })
        }).catch(error => {
          this.$message.error((error.response && error.response.data && error.response.data.msg) || '提交失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    resetForm() {
      this.savedPatient = null
      this.form = buildEmptyForm()
      this.referralForm = buildEmptyReferralState()
      this.$nextTick(() => {
        if (this.$refs.formRef) this.$refs.formRef.clearValidate()
      })
    },
    hasReferralPayload() {
      return !!(
        Number(this.referralForm.referrer_patient_id || 0) > 0
        || String(this.referralForm.referrer_patient_name || '').trim()
        || String(this.referralForm.external_referrer_type || '').trim()
        || String(this.referralForm.external_referrer_name || '').trim()
        || String(this.referralForm.external_referrer_contact || '').trim()
        || String(this.referralForm.referrer_type || '').trim()
        || String(this.referralForm.referral_remark || '').trim()
      )
    }
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(238, 190, 118, 0.24), transparent 34%),
    linear-gradient(180deg, #f7f5ef 0%, #eef3f6 100%);
  padding: 16px;
  box-sizing: border-box;
}

.register-shell {
  max-width: 560px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-card,
.form-card,
.success-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-radius: 22px;
  box-shadow: 0 16px 36px rgba(49, 61, 75, 0.08);
}

.hero-card {
  padding: 22px 20px;
  color: #1f2933;
}

.hero-kicker {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: #183153;
  color: #fef5d8;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.hero-card h1 {
  margin: 14px 0 10px;
  font-size: 32px;
  line-height: 1.1;
  color: #0f172a;
}

.hero-card p {
  margin: 0;
  line-height: 1.7;
  color: #52606d;
  font-size: 14px;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.hero-tags span {
  padding: 6px 10px;
  border-radius: 999px;
  background: #f4efe2;
  color: #7c5d1c;
  font-size: 12px;
}

.form-card {
  padding: 20px;
}

.section-head {
  margin-bottom: 12px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.section-subtitle {
  margin-top: 4px;
  color: #7b8794;
  font-size: 13px;
}

.register-form :deep(.el-form-item__label) {
  color: #334e68;
  font-weight: 600;
  padding-bottom: 6px;
}

.gender-group {
  display: flex;
  width: 100%;
}

.gender-group :deep(.el-radio-button) {
  flex: 1;
}

.gender-group :deep(.el-radio-button__inner) {
  width: 100%;
}

.full-input {
  width: 100%;
}

.submit-bar {
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #183153, #2f6f8f);
  border: none;
}

.submit-tip {
  margin-top: 10px;
  text-align: center;
  color: #7b8794;
  font-size: 12px;
}

.success-card {
  padding: 18px 20px 20px;
  border: 1px solid rgba(63, 161, 93, 0.18);
}

.success-badge {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: #ebf8ef;
  color: #237445;
  font-size: 12px;
  font-weight: 700;
}

.success-title {
  margin-top: 12px;
  font-size: 22px;
  font-weight: 700;
  color: #102a43;
}

.success-meta {
  margin-top: 8px;
  line-height: 1.7;
  color: #486581;
  word-break: break-all;
}

.success-btn {
  margin-top: 14px;
  width: 100%;
  border-radius: 12px;
}

@media (max-width: 420px) {
  .hero-card h1 {
    font-size: 28px;
  }

  .form-card,
  .hero-card,
  .success-card {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
