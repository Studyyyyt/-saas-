<template>
  <div class="admin-report-page" v-loading="loading">
    <div class="report-shell">
      <section class="hero-card">
        <div class="hero-top">
          <span class="hero-badge">管理员微信经营简报</span>
          <span class="hero-badge hero-badge--light">{{ focusLabel }}</span>
        </div>
        <div class="hero-title">{{ summary.displayName ? `${summary.displayName}，你好` : '经营简报' }}</div>
        <div class="hero-subtitle">公众号内直接查看最新日报、周报、月报和异常波动告警。</div>
      </section>

      <section v-if="error" class="section-card">
        <div class="error-text">{{ error }}</div>
      </section>

      <template v-else>
        <section class="section-card" ref="dailySection">
          <div class="section-head">
            <div>
              <div class="section-title">最新日报</div>
              <div class="section-subtitle">{{ latestDaily.analysis_date || '-' }}</div>
            </div>
            <el-tag :type="statusTagType(latestDaily.analysis_status)" size="mini">{{ latestDaily.analysis_status || 'UNKNOWN' }}</el-tag>
          </div>
          <div class="headline">{{ latestDaily.headline || '暂无日报' }}</div>
          <div class="summary">{{ latestDaily.summary || '暂无摘要' }}</div>
          <div class="stats-grid" v-if="latestDaily.metrics">
            <div class="stat-card">
              <div class="stat-value">{{ latestDaily.operating_score ?? '--' }}</div>
              <div class="stat-label">经营评分</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ trendLabel(latestDaily.trend) }}</div>
              <div class="stat-label">趋势</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ latestDaily.metrics.today_appointments ?? 0 }}</div>
              <div class="stat-label">当日预约</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">¥{{ moneyValue(latestDaily.metrics.today_income) }}</div>
              <div class="stat-label">当日收入</div>
            </div>
          </div>
        </section>

        <section class="double-grid">
          <div class="section-card" ref="weeklySection">
            <div class="section-head">
              <div>
                <div class="section-title">最新周报</div>
                <div class="section-subtitle">{{ latestWeekly.period_label || '-' }}</div>
              </div>
              <el-tag :type="statusTagType(latestWeekly.report_status)" size="mini">{{ latestWeekly.report_status || 'UNKNOWN' }}</el-tag>
            </div>
            <div class="headline small">{{ latestWeekly.headline || '暂无周报' }}</div>
            <div class="summary">{{ latestWeekly.summary || '暂无摘要' }}</div>
          </div>

          <div class="section-card" ref="monthlySection">
            <div class="section-head">
              <div>
                <div class="section-title">最新月报</div>
                <div class="section-subtitle">{{ latestMonthly.period_label || '-' }}</div>
              </div>
              <el-tag :type="statusTagType(latestMonthly.report_status)" size="mini">{{ latestMonthly.report_status || 'UNKNOWN' }}</el-tag>
            </div>
            <div class="headline small">{{ latestMonthly.headline || '暂无月报' }}</div>
            <div class="summary">{{ latestMonthly.summary || '暂无摘要' }}</div>
          </div>
        </section>

        <section class="section-card" ref="alertsSection">
          <div class="section-head">
            <div>
              <div class="section-title">最近异常波动告警</div>
              <div class="section-subtitle">仅展示最近 10 条</div>
            </div>
          </div>
          <div v-if="recentAlerts.length" class="alert-list">
            <div v-for="item in recentAlerts" :key="item.id" class="alert-item">
              <div class="alert-top">
                <strong>{{ item.alert_title }}</strong>
                <el-tag :type="alertLevelTagType(item.alert_level)" size="mini">{{ item.alert_level }}</el-tag>
              </div>
              <div class="alert-meta">{{ item.alert_date }} · {{ item.metric_name || '-' }}</div>
              <div class="alert-text">{{ item.alert_message }}</div>
              <div class="alert-action">{{ item.suggested_action }}</div>
            </div>
          </div>
          <div v-else class="empty-text">最近没有检测到异常波动。</div>
        </section>
      </template>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AdminReportH5',
  data() {
    return {
      loading: false,
      error: '',
      payload: {}
    }
  },
  computed: {
    summary() {
      return this.payload.summary || {}
    },
    latestDaily() {
      return this.payload.latestDaily || {}
    },
    latestWeekly() {
      return this.payload.latestWeekly || {}
    },
    latestMonthly() {
      return this.payload.latestMonthly || {}
    },
    recentAlerts() {
      return Array.isArray(this.payload.recentAlerts) ? this.payload.recentAlerts : []
    },
    focusLabel() {
      const focus = String(this.$route.query.focus || '').trim()
      if (focus === 'weekly') return '周报'
      if (focus === 'monthly') return '月报'
      if (focus === 'alerts') return '告警'
      return '日报'
    }
  },
  mounted() {
    this.loadReport()
  },
  methods: {
    loadReport() {
      const { accountId, reportToken } = this.$route.query
      if (!accountId || !reportToken) {
        this.error = '缺少报表访问凭证，请重新从公众号消息进入。'
        return
      }
      this.loading = true
      axios.get('/admin-report-portal/overview', {
        params: { accountId, reportToken }
      }).then(res => {
        if (res.data.code !== '200') {
          this.error = res.data.msg || '报表加载失败'
          return
        }
        this.error = ''
        this.payload = res.data.data || {}
        this.$nextTick(() => this.scrollToFocus())
      }).catch(() => {
        this.error = '报表加载失败，请重新从公众号消息进入。'
      }).finally(() => {
        this.loading = false
      })
    },
    scrollToFocus() {
      const focus = String(this.$route.query.focus || '').trim()
      const refMap = {
        weekly: 'weeklySection',
        monthly: 'monthlySection',
        alerts: 'alertsSection',
        daily: 'dailySection'
      }
      const refName = refMap[focus] || 'dailySection'
      const target = this.$refs[refName]
      if (target && typeof target.scrollIntoView === 'function') {
        target.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    },
    trendLabel(value) {
      if (value === 'up') return '上升'
      if (value === 'down') return '下降'
      if (value === 'flat') return '持平'
      return value || '-'
    },
    statusTagType(status) {
      if (status === 'SUCCESS') return 'success'
      if (status === 'FALLBACK') return 'warning'
      if (status === 'FAILED') return 'danger'
      return 'info'
    },
    alertLevelTagType(level) {
      if (level === 'HIGH') return 'danger'
      if (level === 'MEDIUM') return 'warning'
      return 'info'
    },
    moneyValue(value) {
      const amount = Number(value || 0)
      return amount.toFixed(2)
    }
  }
}
</script>

<style scoped>
.admin-report-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef5ff 0%, #f8fbff 100%);
  padding: 14px;
  box-sizing: border-box;
}
.report-shell {
  max-width: 680px;
  margin: 0 auto;
}
.hero-card,
.section-card {
  background: #fff;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 10px 28px rgba(31, 71, 136, 0.08);
  margin-bottom: 14px;
}
.hero-top,
.section-head,
.alert-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: #e0ecff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}
.hero-badge--light {
  background: #eff6ff;
  color: #475569;
}
.hero-title {
  margin-top: 14px;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}
.hero-subtitle {
  margin-top: 8px;
  color: #64748b;
  line-height: 1.7;
}
.section-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}
.section-subtitle {
  margin-top: 4px;
  color: #94a3b8;
  font-size: 12px;
}
.headline {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.6;
}
.headline.small {
  font-size: 16px;
}
.summary,
.alert-text,
.alert-action {
  margin-top: 10px;
  color: #475569;
  line-height: 1.8;
}
.stats-grid,
.double-grid {
  display: grid;
  gap: 12px;
}
.stats-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 14px;
}
.double-grid {
  grid-template-columns: 1fr 1fr;
}
.stat-card {
  padding: 14px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #dbeafe;
}
.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #2563eb;
}
.stat-label {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}
.period-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.alert-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}
.alert-item {
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 14px;
}
.alert-meta {
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}
.alert-action {
  color: #0f172a;
  font-weight: 600;
}
.error-text,
.empty-text {
  color: #64748b;
  line-height: 1.8;
}
@media (max-width: 768px) {
  .double-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 359px) {
  .double-grid,
  .alert-list {
    grid-template-columns: 1fr;
  }
}
</style>
