<template>
  <div class="insurance-page">
    <el-card class="page-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">医保管理</div>
          <h2>医保日志</h2>
          <p>查看医保接口请求与响应日志，便于后续接真实平台时排查签名、参数和响应异常。</p>
        </div>
        <el-button type="primary" icon="el-icon-refresh" @click="loadLogs">刷新日志</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="logs" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="operation_type" label="操作类型" width="160"></el-table-column>
        <el-table-column prop="ref_type" label="业务类型" width="140"></el-table-column>
        <el-table-column prop="ref_id" label="业务ID" width="140"></el-table-column>
        <el-table-column prop="request_method" label="方法" width="100"></el-table-column>
        <el-table-column prop="request_url" label="请求地址" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="response_code" label="响应码" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="120"></el-table-column>
        <el-table-column prop="created_at" label="时间" min-width="180">
          <template slot-scope="scope">{{ formatDateTime(scope.row.created_at) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!logs.length" description="暂无医保日志"></el-empty>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'InsuranceLogView',
  data() {
    return {
      logs: []
    }
  },
  mounted() {
    this.loadLogs()
  },
  methods: {
    loadLogs() {
      axios.get('/insurance/logs', { params: { limit: 50 } }).then(res => {
        if (res.data.code === '200') {
          this.logs = res.data.data || []
        } else {
          this.$message.error(res.data.msg || '加载医保日志失败')
        }
      }).catch(() => {
        showApiError(this, '加载医保日志', error)
      })
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
.page-card, .table-card { border-radius:18px; }
.page-head { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; }
.page-kicker { color:#2563eb; font-size:13px; font-weight:600; margin-bottom:8px; }
.page-head h2 { margin:0; font-size:28px; color:#0f172a; }
.page-head p { margin:10px 0 0; color:#64748b; line-height:1.7; max-width:760px; }
@media (max-width: 1200px) {
  .page-head { flex-direction:column; }
}
</style>
