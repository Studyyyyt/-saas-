<template>
  <div class="staff-h5-page lab-h5-page" v-loading="loading">
    <div class="h5-hero-card">
      <div>
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>外加工</h2>
        <p>汇总当前待登记加工的病历操作，便于手机端快速追踪。</p>
      </div>
      <el-input v-model="keyword" placeholder="搜索患者/医生/操作" clearable @input="applyFilter"></el-input>
    </div>

    <div class="h5-summary-row">
      <div class="h5-summary-card">
        <div class="summary-num">{{ filteredItems.length }}</div>
        <div class="summary-label">待处理</div>
      </div>
      <div class="h5-summary-card accent">
        <div class="summary-num">{{ items.length }}</div>
        <div class="summary-label">全部待登记</div>
      </div>
    </div>

    <div class="h5-section-card">
      <div class="section-title">待登记加工列表</div>
      <div v-if="filteredItems.length" class="list-card-stack">
        <div v-for="item in filteredItems" :key="item.id" class="list-card">
          <div class="list-card__title">{{ item.patient_name || '未命名患者' }} · {{ item.operation_name || '未命名操作' }}</div>
          <div class="list-card__meta">{{ formatDate(item.visit_date) || '-' }} · {{ item.doctor_name || '未指定医生' }}</div>
          <div class="list-card__desc">牙位：{{ item.tooth_positions || '未填写' }}</div>
          <div class="list-card__desc">加工厂：{{ item.factory_name || '待登记' }}</div>
          <div class="list-card__desc">备注：{{ item.remark || '无' }}</div>
          <div class="list-card__actions">
            <el-button size="mini" type="primary" plain @click="openPatient(item)">进入患者360</el-button>
            <el-button size="mini" plain @click="markSkip(item)">标记已完成</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="当前没有待登记加工项目"></el-empty>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { fetchCachedResource } from '@/utils/offline/apiClient'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'

export default {
  name: 'StaffLabProcessingH5',
  data() {
    return {
      loading: false,
      keyword: '',
      items: [],
      filteredItems: []
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.loadItems()
  },
  methods: {
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    buildQuery() {
      const portalQuery = getStaffPortalQuery(this.$route.query)
      const doctorAccountId = portalQuery.doctorName ? Number(portalQuery.accountId || 0) : 0
      return {
        page: 1,
        size: 200,
        doctorAccountId: doctorAccountId > 0 ? doctorAccountId : undefined
      }
    },
    loadItems() {
      this.loading = true
      const params = this.buildQuery()
      fetchCachedResource({
        cacheKey: `page:staff:lab-processing:${params.doctorAccountId || 'all'}`,
        scope: '',
        url: '/medical-record-operations/pendingLabList',
        params,
        loader: () => axios.get('/medical-record-operations/pendingLabList', { params }),
        notifier: message => this.$message.warning(message)
      }).then(result => {
        const data = result && result.data ? result.data : {}
        this.items = Array.isArray(data.list) ? data.list : []
        this.applyFilter()
      }).catch(() => {
        this.items = []
        this.applyFilter()
      }).finally(() => {
        this.loading = false
      })
    },
    applyFilter() {
      const text = String(this.keyword || '').trim().toLowerCase()
      if (!text) {
        this.filteredItems = this.items.slice()
        return
      }
      this.filteredItems = this.items.filter(item => {
        const values = [
          item.patient_name,
          item.doctor_name,
          item.operation_name,
          item.factory_name
        ].map(value => String(value || '').toLowerCase())
        return values.some(value => value.includes(text))
      })
    },
    openPatient(item) {
      this.$router.push({
        path: '/staff-h5/patient360',
        query: Object.assign({}, getStaffPortalQuery(this.$route.query), {
          id: item.patient_id,
          name: item.patient_name
        })
      })
    },
    markSkip(item) {
      this.$prompt('可选：补充说明', '标记已完成', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：已线下处理/无需继续登记'
      }).then(({ value }) => {
        return axios.put('/medical-record-operations/markSkip', {
          id: item.id,
          skip_reason: value || '',
          updated_by: Number(getStaffPortalQuery(this.$route.query).accountId || 0) || null,
          updated_by_name: getStaffPortalQuery(this.$route.query).doctorName || ''
        })
      }).then(res => {
        if (res && res.data && String(res.data.code) === '200') {
          this.$message.success('已标记完成')
          this.loadItems()
        } else if (res) {
          this.$message.error((res.data && res.data.msg) || '更新失败')
        }
      }).catch(() => {})
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
  grid-template-columns: repeat(2, minmax(0, 1fr));
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
.list-card-stack {
  display: grid;
  gap: 12px;
}
.list-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
}
.list-card__title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}
.list-card__meta,
.list-card__desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.list-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}
</style>
