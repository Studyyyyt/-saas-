<template>
  <div class="staff-h5-page patient-h5-page">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>我的患者</h2>
        <p>在微信内快速查找患者，并进入患者 360 视图。</p>
      </div>
      <el-button type="primary" size="small" plain @click="openAddDialog">新增患者</el-button>
      <el-input v-model="keyword" placeholder="搜索患者姓名/手机号" clearable @input="handleKeywordInput" @clear="handleKeywordInput"></el-input>
    </div>

    <div class="h5-summary-row">
      <div class="h5-summary-card">
        <div class="summary-num">{{ pagination.total }}</div>
        <div class="summary-label">匹配患者</div>
      </div>
      <div class="h5-summary-card accent">
        <div class="summary-num">{{ patients.length }}</div>
        <div class="summary-label">当前页</div>
      </div>
    </div>

    <div class="h5-section-card">
      <div class="section-title">患者列表</div>
      <div v-if="loading" class="loading-box"><i class="el-icon-loading"></i></div>
      <div v-else-if="patients.length" class="patient-list">
        <div v-for="patient in patients" :key="patient.id" class="patient-row" @click="openPatient360(patient)">
          <div class="patient-row__main">
            <div class="patient-row__name">{{ patient.name || '未命名患者' }}</div>
            <div class="patient-row__meta">{{ patient.gender || '未知性别' }} · {{ formatAge(patient) }} · {{ patient.phone || '未留手机号' }}</div>
            <div class="patient-row__meta">
              <span>{{ patient.customer_source || '未标记来源' }}</span>
              <span v-if="patient.latest_visit_doctor_name">· {{ patient.latest_visit_doctor_name }}</span>
            </div>
            <div v-if="patient.last_activity_at" class="patient-row__meta patient-row__meta--time">最近活跃：{{ formatDateTime(patient.last_activity_at) }}</div>
            <div v-if="patient._offline" class="patient-row__meta">
              <el-tag size="mini" :type="patient._offline.failed ? 'danger' : 'warning'" effect="plain">{{ patient._offline.label }}</el-tag>
            </div>
          </div>
          <div class="patient-row__action">查看360</div>
        </div>
      </div>
      <el-empty v-else :description="keyword ? '没有匹配到患者' : '暂无患者'"></el-empty>
      <div v-if="pagination.pages > 1" class="patient-pagination">
        <el-pagination
          small
          background
          layout="prev, pager, next"
          :pager-count="5"
          :current-page="pagination.page"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </div>

    <el-dialog title="新增患者" :visible.sync="dialogVisible" width="92%" append-to-body>
      <el-form :model="editItem" label-width="88px">
        <el-form-item label="患者姓名">
          <el-input v-model="editItem.name"></el-input>
        </el-form-item>
        <el-form-item label="患者性别">
          <el-select v-model="editItem.gender" style="width:100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="editItem.age" :min="0" :max="150" controls-position="right" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editItem.phone" maxlength="11"></el-input>
        </el-form-item>
        <el-form-item label="患者来源">
          <el-select v-model="editItem.customer_source" style="width:100%">
            <el-option v-for="item in customerSourceOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="介绍人（可选）">
          <ReferralSelector :value="referralForm" @input="handleReferralChange" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPatient">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import ReferralSelector from '@/components/ReferralSelector.vue'
import { CUSTOMER_SOURCE_OPTIONS } from '@/utils/consultationOptions'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'
import { getPatientAge, rememberRecentPatient } from '@/utils/patientList'
import { fetchCachedResource, savePatient } from '@/utils/offline/apiClient'

const PATIENT_PHONE_REGEX = /^\d{11}$/
const PAGE_SIZE = 30

function createEmptyPatient() {
  return {
    name: '',
    gender: '',
    age: null,
    phone: '',
    customer_source: ''
  }
}

function createEmptyReferralState() {
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
  name: 'StaffPatientH5',
  components: {
    ReferralSelector
  },
  data() {
    return {
      keyword: '',
      loading: false,
      patients: [],
      keywordDebounceTimer: null,
      pagination: {
        page: 1,
        pageSize: PAGE_SIZE,
        total: 0,
        pages: 0
      },
      dialogVisible: false,
      editItem: createEmptyPatient(),
      referralForm: createEmptyReferralState(),
      customerSourceOptions: CUSTOMER_SOURCE_OPTIONS
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.loadPatients()
  },
  beforeDestroy() {
    if (this.keywordDebounceTimer) {
      clearTimeout(this.keywordDebounceTimer)
      this.keywordDebounceTimer = null
    }
  },
  methods: {
    loadPatients(page = this.pagination.page) {
      const query = getStaffPortalQuery(this.$route.query)
      const doctorName = query.doctorName || ''
      const accountId = Number(query.accountId || 0)
      const keyword = String(this.keyword || '').trim()
      const params = {
        page: Math.max(Number(page) || 1, 1),
        size: PAGE_SIZE,
        searchType: 'name',
        keyword,
        quickScope: 'all',
        groupKey: 'all',
        sortMode: 'recent'
      }
      if (doctorName) {
        params.doctorFilter = accountId > 0 ? `id:${accountId}` : `name:${doctorName}`
      }
      this.loading = true
      fetchCachedResource({
        cacheKey: 'page:staff:patients:workbench',
        scope: 'patientsH5',
        url: '/patients/workbench',
        params,
        loader: () => axios.get('/patients/workbench', { params }),
        notifier: message => this.$message.warning(message)
      }).then(response => {
        const payload = response && response.data ? response.data : {}
        this.patients = Array.isArray(payload.list) ? payload.list : []
        this.pagination.page = Number(payload.pageNum || params.page) || 1
        this.pagination.pageSize = Number(payload.pageSize || PAGE_SIZE) || PAGE_SIZE
        this.pagination.total = Number(payload.total || 0) || 0
        this.pagination.pages = Number(payload.pages || 0) || 0
      }).catch(error => {
        console.error('Error fetching patients:', error)
      }).finally(() => {
        this.loading = false
      })
    },
    handleKeywordInput() {
      this.pagination.page = 1
      if (this.keywordDebounceTimer) {
        clearTimeout(this.keywordDebounceTimer)
      }
      this.keywordDebounceTimer = setTimeout(() => {
        this.loadPatients(1)
      }, 250)
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.loadPatients(page)
    },
    formatAge(patient) {
      const age = getPatientAge(patient)
      return age === '' ? '年龄未知' : `${age}岁`
    },
    formatDateTime(value) {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''
      const pad = part => String(part).padStart(2, '0')
      return `${date.getMonth() + 1}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    openPatient360(patient) {
      rememberRecentPatient(patient)
      this.$router.push({
        path: '/staff-h5/patient360',
        query: Object.assign({}, getStaffPortalQuery(this.$route.query), {
          id: patient.id,
          name: patient.name
        })
      })
    },
    openAddDialog() {
      this.editItem = createEmptyPatient()
      this.referralForm = createEmptyReferralState()
      this.dialogVisible = true
    },
    handleReferralChange(value) {
      this.referralForm = Object.assign(createEmptyReferralState(), value || {})
      if (this.hasReferralPayload()) {
        this.editItem.customer_source = '转介绍'
      }
    },
    validatePatient() {
      if (!this.editItem.name || !String(this.editItem.name).trim()) return '患者姓名必填'
      if (!this.editItem.gender) return '患者性别必填'
      if (this.editItem.age === null || this.editItem.age === undefined || this.editItem.age === '') return '患者年龄必填'
      if (!PATIENT_PHONE_REGEX.test(String(this.editItem.phone || '').trim())) return '手机号需为11位数字'
      if (!this.editItem.customer_source) return '患者来源必填'
      if (this.referralForm.referrer_type === 'patient' && !this.referralForm.referrer_patient_id) return '请选择有效的介绍患者'
      if (this.referralForm.referrer_type === 'external' && !String(this.referralForm.external_referrer_name || '').trim()) return '请输入外部介绍人姓名'
      return ''
    },
    submitPatient() {
      const validation = this.validatePatient()
      if (validation) {
        this.$message.warning(validation)
        return
      }
      savePatient(Object.assign({}, this.editItem, {
        customer_source: this.hasReferralPayload() ? '转介绍' : this.editItem.customer_source,
        referrer_type: this.referralForm.referrer_type || '',
        referrer_patient_id: this.referralForm.referrer_patient_id || null,
        referrer_patient_name: this.referralForm.referrer_patient_name || '',
        external_referrer_type: this.referralForm.external_referrer_type || '',
        external_referrer_name: this.referralForm.external_referrer_name || '',
        external_referrer_contact: this.referralForm.external_referrer_contact || '',
        referral_remark: this.referralForm.referral_remark || ''
      }), {
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success('新增成功')
        }
        this.dialogVisible = false
        this.referralForm = createEmptyReferralState()
        this.loadPatients()
      }).catch(error => {
        this.$message.error((error && error.message) || '新增失败')
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
.staff-h5-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 14px;
  box-sizing: border-box;
}
.h5-hero-card,
.h5-section-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
  margin-bottom: 14px;
}
.h5-page-kicker {
  color: #409eff;
  font-size: 12px;
  margin-bottom: 8px;
}
.h5-hero-card h2 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #1f2d3d;
}
.h5-hero-card p {
  margin: 0 0 14px;
  color: #606266;
  font-size: 13px;
}
.h5-summary-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 14px;
}
.h5-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 14px 12px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
}
.h5-summary-card.accent { background: #eef6ff; }
.summary-num {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.summary-label {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.loading-box {
  padding: 24px 0;
  text-align: center;
  color: #409eff;
  font-size: 24px;
}
.patient-list {
  display: flex;
  flex-direction: column;
}
.patient-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
  cursor: pointer;
}
.patient-row:last-child {
  border-bottom: none;
}
.patient-row__main {
  min-width: 0;
  flex: 1;
}
.patient-row__name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}
.patient-row__meta {
  color: #606266;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}
.patient-row__meta--time {
  color: #8b95a7;
}
.patient-row__action {
  flex-shrink: 0;
  color: #409eff;
  font-size: 12px;
  font-weight: 600;
}
.patient-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

@media (max-width: 420px) {
  .h5-summary-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 359px) {
  .h5-summary-row {
    grid-template-columns: 1fr;
  }
}
</style>
