<template>
  <div class="staff-h5-page record-h5-page">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>病历记录</h2>
        <p>手机查看近期病历，支持按患者快速搜索。</p>
      </div>
      <div class="hero-actions hero-actions--single">
        <el-input v-model="keyword" placeholder="搜索患者姓名/医生" clearable @input="loadRecords"></el-input>
      </div>
    </div>

    <div class="h5-summary-row h5-summary-row--triple">
      <div class="h5-summary-card">
        <div class="summary-num">{{ records.length }}</div>
        <div class="summary-label">当前记录</div>
      </div>
      <div class="h5-summary-card accent">
        <div class="summary-num">{{ uniquePatients }}</div>
        <div class="summary-label">涉及患者</div>
      </div>
      <div class="h5-summary-card success">
        <div class="summary-num">{{ doctorCount }}</div>
        <div class="summary-label">接诊医生</div>
      </div>
    </div>

    <div class="h5-section-card">
      <div class="section-title">病历列表</div>
      <div v-if="records.length" class="record-card-list">
        <div v-for="item in records" :key="item.id" class="record-card" @click="openDetail(item)">
          <div class="record-card__top">
            <div>
              <div class="record-card__name">{{ item.patient_name || '未填写患者' }}</div>
              <div class="record-card__meta">{{ formatDate(item.visit_date) || '未记录日期' }} · {{ item.doctor_name || '未指定医生' }}</div>
            </div>
            <el-tag size="mini" :type="item._offline ? (item._offline.failed ? 'danger' : 'warning') : 'primary'">
              {{ item._offline ? item._offline.label : `#${item.id}` }}
            </el-tag>
          </div>
          <div class="record-card__desc">主诉：{{ item.chief_complaint || '无' }}</div>
          <div class="record-card__desc">诊断：{{ item.diagnosis || '无' }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无病历"></el-empty>
    </div>

    <el-drawer :visible.sync="detailVisible" size="92%" direction="btt" custom-class="appointment-detail-drawer">
      <div v-if="detailItem.id" class="detail-sheet">
        <div class="detail-title">病历详情</div>
        <div class="detail-row"><span>患者</span><strong>{{ detailItem.patient_name || '-' }}</strong></div>
        <div class="detail-row"><span>医生</span><strong>{{ detailItem.doctor_name || '-' }}</strong></div>
        <div class="detail-row"><span>日期</span><strong>{{ formatDate(detailItem.visit_date) || '-' }}</strong></div>
        <div class="detail-row detail-row--block"><span>主诉</span><strong>{{ detailItem.chief_complaint || '无' }}</strong></div>
        <div class="detail-row detail-row--block"><span>诊断</span><strong>{{ detailItem.diagnosis || '无' }}</strong></div>
        <div class="detail-row detail-row--block"><span>治疗</span><strong>{{ detailItem.treatment || '无' }}</strong></div>
        <div class="detail-row detail-row--block"><span>处方</span><strong>{{ detailItem.prescription || '无' }}</strong></div>
        <div class="detail-row detail-row--block"><span>备注</span><strong>{{ detailItem.notes || '无' }}</strong></div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import axios from 'axios'
import { fetchCachedResource } from '@/utils/offline/apiClient'

export default {
  name: 'StaffMedicalRecordH5',
  data() {
    return {
      keyword: '',
      records: [],
      detailVisible: false,
      detailItem: {}
    }
  },
  computed: {
    uniquePatients() {
      return new Set(this.records.map(item => item.patient_name).filter(Boolean)).size
    },
    doctorCount() {
      return new Set(this.records.map(item => item.doctor_name).filter(Boolean)).size
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    formatDate(value) {
      if (!value) return ''
      return String(value).slice(0, 10)
    },
    loadRecords() {
      fetchCachedResource({
        cacheKey: 'page:staff:records:list',
        scope: 'medicalRecordsH5',
        url: '/medical-records/selectAll',
        params: { page: 1, size: 200 },
        loader: () => axios.get('/medical-records/selectAll', { params: { page: 1, size: 200 } }),
        notifier: message => this.$message.warning(message)
      }).then(res => {
        const data = res && res.data ? res.data : {}
        const list = data.list || []
        const text = String(this.keyword || '').trim().toLowerCase()
        this.records = (list || [])
          .filter(item => {
            if (!text) return true
            const patientName = String(item.patient_name || '').toLowerCase()
            const doctorName = String(item.doctor_name || '').toLowerCase()
            return patientName.includes(text) || doctorName.includes(text)
          })
          .sort((a, b) => String(b.visit_date || '').localeCompare(String(a.visit_date || '')))
      }).catch(error => {
        console.error('Error fetching medical records:', error)
      })
    },
    openDetail(item) {
      this.detailItem = Object.assign({}, item)
      this.detailVisible = true
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
  margin: 0;
  color: #606266;
  font-size: 13px;
}
.hero-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 14px;
}
.hero-actions--single {
  grid-template-columns: 1fr;
}
.h5-summary-row {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}
.h5-summary-row--triple {
  grid-template-columns: repeat(3, 1fr);
}
.h5-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 14px 12px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
}
.h5-summary-card.accent { background: #eef6ff; }
.h5-summary-card.success { background: #eefbf3; }
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
.record-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.record-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  height: 100%;
}
.record-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.record-card__name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}
.record-card__meta,
.record-card__desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
}
.detail-sheet {
  padding: 18px;
}
.detail-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
  color: #606266;
}
.detail-row strong {
  color: #303133;
  text-align: right;
}
.detail-row--block {
  display: block;
}
.detail-row--block strong {
  display: block;
  margin-top: 8px;
  text-align: left;
  line-height: 1.7;
}

@media (max-width: 420px) {
  .h5-summary-row--triple {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-card__top,
  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 359px) {
  .h5-summary-row--triple,
  .record-card-list {
    grid-template-columns: 1fr;
  }
}
</style>
