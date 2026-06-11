<template>
  <div class="insurance-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="hero-kicker">医保管理</div>
          <h2>医保总览</h2>
          <p>查看医保配置、患者建档、结算草稿与异常状态，作为后续真实医保平台对接的总控台。</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" icon="el-icon-refresh" @click="loadOverview">刷新数据</el-button>
          <el-button plain @click="$router.push('/InsuranceConfig')">前往医保配置</el-button>
        </div>
      </div>
    </el-card>

    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-label">患者医保档案数</div>
        <div class="stat-value">{{ overview.patientProfileCount || 0 }}</div>
        <div class="stat-desc">已建立医保基础档案的患者数量</div>
      </div>
      <div class="stat-card success">
        <div class="stat-label">医保结算记录数</div>
        <div class="stat-value">{{ overview.settlementCount || 0 }}</div>
        <div class="stat-desc">包含草稿与后续正式结算记录</div>
      </div>
      <div class="stat-card warn">
        <div class="stat-label">待处理结算</div>
        <div class="stat-value">{{ overview.pendingSettlementCount || 0 }}</div>
        <div class="stat-desc">当前状态为 PENDING 的医保结算记录</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-label">失败结算</div>
        <div class="stat-value">{{ overview.failedSettlementCount || 0 }}</div>
        <div class="stat-desc">当前状态为 FAILED 的医保结算记录</div>
      </div>
    </div>

    <div class="content-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">当前医保平台配置</div>
        <template v-if="overview.config">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="平台编码">{{ overview.config.platform_code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="平台名称">{{ overview.config.platform_name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="机构编码">{{ overview.config.org_code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="机构名称">{{ overview.config.org_name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="统筹区编码">{{ overview.config.region_code || '-' }}</el-descriptions-item>
            <el-descriptions-item label="加密方式">{{ overview.config.encryption_type || '-' }}</el-descriptions-item>
            <el-descriptions-item label="接口地址" :span="2">{{ overview.config.api_base_url || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>
        <el-empty v-else description="暂未配置医保平台"></el-empty>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">接入建议</div>
        <div class="tips-list">
          <div class="tip-item"><span class="dot blue"></span><span>先补齐机构编码、平台编码、签名参数，再接真实医保接口。</span></div>
          <div class="tip-item"><span class="dot green"></span><span>患者医保档案应尽量绑定身份证号、参保地编码、医保人员编号。</span></div>
          <div class="tip-item"><span class="dot orange"></span><span>处置收费项目库已扩展医保编码字段，后续可做项目对码。</span></div>
          <div class="tip-item"><span class="dot red"></span><span>当前仅为骨架版本，尚未接真实医保认证、结算、撤销与退费流程。</span></div>
        </div>
      </el-card>
    </div>

    <el-card class="roadmap-card" shadow="never">
      <div slot="header" class="panel-title">医保模块当前范围</div>
      <div class="roadmap-grid">
        <div class="roadmap-item done">
          <div class="roadmap-title">后端骨架</div>
          <div class="roadmap-desc">配置、患者医保档案、结算草稿、日志、mock 报文</div>
        </div>
        <div class="roadmap-item in-progress">
          <div class="roadmap-title">管理后台</div>
          <div class="roadmap-desc">总览、配置、档案、结算、日志页面</div>
        </div>
        <div class="roadmap-item">
          <div class="roadmap-title">真实接口对接</div>
          <div class="roadmap-desc">待医保平台文档、签名规则、业务编码明确后接入</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'InsuranceOverviewView',
  data() {
    return {
      overview: {}
    }
  },
  mounted() {
    this.loadOverview()
  },
  methods: {
    loadOverview() {
      axios.get('/insurances/overview').then(res => {
        if (res.data.code === '200') {
          this.overview = res.data.data || {}
        } else {
          this.$message.error(res.data.msg || '加载医保总览失败')
        }
      }).catch(() => {
        showApiError(this, '加载医保总览', error)
      })
    }
  }
}
</script>

<style scoped>
.insurance-page { display:flex; flex-direction:column; gap:18px; }
.hero-card, .panel-card, .roadmap-card { border-radius: 18px; }
.hero-head { display:flex; justify-content:space-between; gap:20px; align-items:flex-start; }
.hero-kicker { color:#2563eb; font-size:13px; font-weight:600; margin-bottom:8px; }
.hero-head h2 { margin:0; font-size:28px; color:#0f172a; }
.hero-head p { margin:10px 0 0; color:#64748b; max-width:760px; line-height:1.7; }
.hero-actions { display:flex; gap:10px; }
.stats-grid { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:14px; }
.stat-card { background:#fff; border:1px solid #e5e7eb; border-radius:18px; padding:18px; box-shadow:0 10px 24px rgba(15,23,42,.05); }
.stat-card.primary { border-top:4px solid #2563eb; }
.stat-card.success { border-top:4px solid #16a34a; }
.stat-card.warn { border-top:4px solid #f59e0b; }
.stat-card.danger { border-top:4px solid #ef4444; }
.stat-label { color:#64748b; font-size:13px; }
.stat-value { margin-top:8px; font-size:30px; font-weight:800; color:#0f172a; }
.stat-desc { margin-top:6px; font-size:12px; color:#94a3b8; line-height:1.6; }
.content-grid { display:grid; grid-template-columns:1.2fr 1fr; gap:14px; }
.panel-title { font-weight:700; color:#0f172a; }
.tips-list { display:flex; flex-direction:column; gap:14px; }
.tip-item { display:flex; gap:10px; align-items:flex-start; color:#334155; line-height:1.7; }
.dot { width:10px; height:10px; border-radius:999px; margin-top:7px; flex-shrink:0; }
.dot.blue { background:#3b82f6; }
.dot.green { background:#22c55e; }
.dot.orange { background:#f59e0b; }
.dot.red { background:#ef4444; }
.roadmap-grid { display:grid; grid-template-columns:repeat(3, minmax(0,1fr)); gap:14px; }
.roadmap-item { border:1px solid #e2e8f0; border-radius:16px; padding:16px; background:#f8fafc; }
.roadmap-item.done { border-color:#bbf7d0; background:#f0fdf4; }
.roadmap-item.in-progress { border-color:#bfdbfe; background:#eff6ff; }
.roadmap-title { font-size:16px; font-weight:700; color:#0f172a; }
.roadmap-desc { margin-top:8px; font-size:13px; color:#64748b; line-height:1.7; }
@media (max-width: 1200px) {
  .stats-grid, .roadmap-grid, .content-grid { grid-template-columns:1fr; }
  .hero-head { flex-direction:column; }
}
</style>
