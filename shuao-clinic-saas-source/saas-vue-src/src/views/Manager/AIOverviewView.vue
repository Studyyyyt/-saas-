<template>
  <div class="ai-overview-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">AI 总览</h1>
        <p class="page-subtitle">管理系统所有 AI 功能的开关与运行状态</p>
      </div>
      <div class="page-header-right">
        <el-button size="small" icon="el-icon-refresh" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </div>

    <!-- 全局开关区 -->
    <div class="section-card global-control-card">
      <div class="section-title">全局控制</div>
      <div class="global-switches">
        <div class="switch-item">
          <div class="switch-info">
            <div class="switch-label">
              <i class="el-icon-set-up switch-icon"></i>
              AI 功能总开关
            </div>
            <div class="switch-desc">关闭后，系统所有 AI 入口将隐藏且不可用</div>
          </div>
          <el-switch
            v-model="globalEnabled"
            active-text="开启"
            inactive-text="关闭"
            :disabled="switchLoading.global"
            @change="onGlobalSwitchChange"
          />
        </div>
        <div class="switch-item">
          <div class="switch-info">
            <div class="switch-label">
              <i class="el-icon-bug switch-icon"></i>
              调试模式
            </div>
            <div class="switch-desc">开启后，AI 请求会在浏览器控制台打印详细日志</div>
          </div>
          <el-switch
            v-model="debugMode"
            active-text="开启"
            inactive-text="关闭"
            :disabled="switchLoading.debug"
            @change="onDebugModeChange"
          />
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="(stat, index) in statCards" :key="index">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 功能列表区 -->
    <div class="section-card function-list-card">
      <div class="section-header">
        <div class="section-title">AI 功能列表</div>
        <el-tag v-if="!globalEnabled" type="warning" size="small" effect="plain">
          <i class="el-icon-warning-outline"></i> AI 已全局关闭
        </el-tag>
      </div>
      <el-table
        :data="aiFunctions"
        style="width: 100%"
        v-loading="loading"
        :class="{ 'disabled-table': !globalEnabled }"
      >
        <el-table-column prop="name" label="功能名称" min-width="160">
          <template slot-scope="scope">
            <div class="func-name">
              <span class="func-icon">{{ scope.row.icon }}</span>
              <div class="func-info">
                <div class="func-title">{{ scope.row.functionName }}</div>
                <div class="func-model" v-if="scope.row.modelName">
                  <i class="el-icon-cpu"></i> {{ scope.row.modelName }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="pagePath" label="所属页面" min-width="120">
          <template slot-scope="scope">
            <el-tag size="mini" type="info" effect="plain">{{ scope.row.pagePath || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.isEnabled ? 'success' : 'info'"
              size="small"
              effect="dark"
              class="status-tag"
            >
              {{ scope.row.isEnabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="今日调用" width="100" align="center">
          <template slot-scope="scope">
            <span class="call-count" :class="{ 'zero': getTodayCalls(scope.row.functionKey) === 0 }">
              {{ getTodayCalls(scope.row.functionKey) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-setting"
              @click="goToConfig(scope.row)"
              :disabled="!globalEnabled"
            >配置</el-button>
            <el-switch
              v-model="scope.row.isEnabled"
              active-color="#13ce66"
              inactive-color="#dcdfe6"
              :disabled="!globalEnabled || switchLoading[scope.row.functionKey]"
              @change="(val) => onFunctionSwitchChange(scope.row, val)"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { updateAiState } from '@/utils/aiConfig'

export default {
  name: 'AIOverviewView',
  data() {
    return {
      globalEnabled: true,
      debugMode: false,
      loading: false,
      switchLoading: {
        global: false,
        debug: false
      },
      stats: {
        todayCalls: 0,
        todayTokens: 0,
        activeFunctions: 0,
        totalFunctions: 0,
        errorRate: 0
      },
      aiFunctions: []
    }
  },
  computed: {
    statCards() {
      return [
        {
          value: this.stats.todayCalls,
          label: '今日 AI 调用',
          icon: 'el-icon-chat-dot-round',
          gradient: 'linear-gradient(135deg, #2563eb, #3b82f6)'
        },
        {
          value: this.formatTokens(this.stats.todayTokens),
          label: '今日 Token 消耗',
          icon: 'el-icon-document',
          gradient: 'linear-gradient(135deg, #059669, #10b981)'
        },
        {
          value: `${this.stats.activeFunctions} / ${this.stats.totalFunctions}`,
          label: '活跃功能数',
          icon: 'el-icon-magic-stick',
          gradient: 'linear-gradient(135deg, #d97706, #f59e0b)'
        },
        {
          value: `${this.stats.errorRate}%`,
          label: '错误率',
          icon: 'el-icon-warning-outline',
          gradient: 'linear-gradient(135deg, #dc2626, #ef4444)'
        }
      ]
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [overviewRes, functionsRes] = await Promise.all([
          axios.get('/api/ai-config/overview'),
          axios.get('/api/ai-config/functions')
        ])

        if (overviewRes.data && overviewRes.data.code === '200') {
          const data = overviewRes.data.data || {}
          this.stats.todayCalls = data.todayCalls || 0
          this.stats.todayTokens = data.todayTokens || 0
          this.stats.activeFunctions = data.activeFunctions || 0
          this.stats.totalFunctions = data.totalFunctions || 0
          this.stats.errorRate = data.errorRate || 0
          this.globalEnabled = data.globalEnabled !== false
          this.debugMode = data.debugMode === true
        }

        if (functionsRes.data && functionsRes.data.code === '200') {
          this.aiFunctions = Array.isArray(functionsRes.data.data) ? functionsRes.data.data : []
        }
      } catch (error) {
        console.error('加载 AI 总览数据失败:', error)
        this.$message.error('加载数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    async onGlobalSwitchChange(val) {
      this.switchLoading.global = true
      try {
        const res = await axios.put('/api/ai-config/global', {
          globalEnabled: val,
          debugMode: this.debugMode
        })
        if (res.data && res.data.code === '200') {
          this.$message.success(val ? 'AI 功能已全局开启' : 'AI 功能已全局关闭')
          updateAiState({ globalEnabled: val })
          // 全局关闭时刷新列表，让禁用状态更明显
          if (!val) {
            await this.loadData()
          }
        } else {
          this.$message.error(res.data?.msg || '操作失败')
          // 回滚状态
          this.globalEnabled = !val
        }
      } catch (error) {
        console.error('更新全局开关失败:', error)
        this.$message.error('保存失败')
        this.globalEnabled = !val
      } finally {
        this.switchLoading.global = false
      }
    },

    async onDebugModeChange(val) {
      this.switchLoading.debug = true
      try {
        const res = await axios.put('/api/ai-config/global', {
          globalEnabled: this.globalEnabled,
          debugMode: val
        })
        if (res.data && res.data.code === '200') {
          this.$message.success(val ? '调试模式已开启' : '调试模式已关闭')
          updateAiState({ debugMode: val })
        } else {
          this.$message.error(res.data?.msg || '操作失败')
          this.debugMode = !val
        }
      } catch (error) {
        console.error('更新调试模式失败:', error)
        this.$message.error('保存失败')
        this.debugMode = !val
      } finally {
        this.switchLoading.debug = false
      }
    },

    async onFunctionSwitchChange(row, val) {
      this.$set(this.switchLoading, row.functionKey, true)
      try {
        const res = await axios.put(`/api/ai-config/functions/${row.functionKey}`, {
          enabled: val === true
        })
        if (res.data && res.data.code === '200') {
          this.$message.success(`${row.functionName} 已${val ? '启用' : '禁用'}`)
          updateAiState({ functions: { [row.functionKey]: val === true } })
          // 刷新统计数据
          const overviewRes = await axios.get('/api/ai-config/overview')
          if (overviewRes.data && overviewRes.data.code === '200') {
            const data = overviewRes.data.data || {}
            this.stats.activeFunctions = data.activeFunctions || 0
            this.stats.totalFunctions = data.totalFunctions || 0
          }
        } else {
          this.$message.error(res.data?.msg || '操作失败')
          // 回滚状态
          this.$set(row, 'isEnabled', !val)
        }
      } catch (error) {
        console.error('更新功能状态失败:', error)
        this.$message.error('保存失败')
        this.$set(row, 'isEnabled', !val)
      } finally {
        this.$set(this.switchLoading, row.functionKey, false)
      }
    },

    goToConfig(row) {
      // 根据功能键映射到对应的路由
      // 注：followup-generate（智能随访）与 business-analysis（经营分析）暂无独立配置页
      const routeMap = {
        'home-assistant': '/SystemSettings/ai/agent',
        'medical-expand': '/SystemSettings/ai/pages/medical',
        'patient-insight': '/SystemSettings/ai/pages/patient',
        'followup-generate': '/SystemSettings/ai/agent',
        'business-analysis': '/SystemSettings/ai/agent',
        'lab-order-analysis': '/SystemSettings/ai/agent',
        'lab-factory-analysis': '/SystemSettings/ai/agent',
        'ad-spending-analysis': '/SystemSettings/ai/agent',
        'doctor-schedule': '/SystemSettings/ai/agent'
      }
      const path = routeMap[row.functionKey]
      if (path) {
        this.$router.push(path)
      } else {
        this.$message.info('该功能配置页面尚未开放')
      }
    },

    formatTokens(tokens) {
      if (tokens >= 1000) {
        return (tokens / 1000).toFixed(1) + 'K'
      }
      return String(tokens)
    },

    getTodayCalls(functionKey) {
      // 说明：后端已提供 ai_operation_log 表记录各功能调用日志，
      // 但当前 MedicalRecordAIService 等 AI 服务尚未在调用成功后写入日志。
      // 待后端各 AI 服务接入 logAiOperation() 后，
      // 可扩展 OverviewVO 返回各功能今日调用明细，此处再改为真实数据。
      return 0
    }
  }
}
</script>

<style scoped>
.ai-overview-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-header-left {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--apple-text-primary);
  line-height: 1.2;
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--apple-text-secondary);
}

/* 卡片区块 */
.section-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  transition: box-shadow 0.3s ease;
}

.section-card:hover {
  box-shadow: var(--apple-shadow-lg), var(--apple-surface-shadow-inset);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

/* 全局开关 */
.global-switches {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--apple-divider);
  transition: background 0.2s ease;
}

.switch-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.switch-item:first-child {
  padding-top: 0;
}

.switch-info {
  flex: 1;
}

.switch-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.switch-icon {
  font-size: 16px;
  color: var(--apple-accent-blue);
}

.switch-desc {
  font-size: 13px;
  color: var(--apple-text-tertiary);
  line-height: 1.4;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--apple-shadow-lg);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-body {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--apple-text-primary);
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 13px;
  color: var(--apple-text-tertiary);
  margin-top: 4px;
}

/* 功能列表 */
.function-list-card {
  overflow: hidden;
}

.func-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.func-icon {
  font-size: 22px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--apple-bg-secondary);
  border-radius: 10px;
  flex-shrink: 0;
}

.func-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.func-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.func-model {
  font-size: 12px;
  color: var(--apple-text-tertiary);
}

.func-model i {
  font-size: 11px;
  margin-right: 2px;
}

.status-tag {
  border-radius: 12px;
  padding: 0 10px;
  height: 24px;
  line-height: 22px;
}

.call-count {
  font-weight: 600;
  color: var(--apple-text-primary);
}

.call-count.zero {
  color: var(--apple-text-tertiary);
  font-weight: 400;
}

.disabled-table {
  opacity: 0.65;
  pointer-events: none;
  filter: grayscale(0.4);
  transition: all 0.3s ease;
}

.disabled-table >>> .el-table__body-wrapper {
  pointer-events: none;
}

/* 覆盖禁用表格的操作按钮，使其仍可交互 */
.disabled-table >>> .el-table__body-wrapper .el-button--text,
.disabled-table >>> .el-table__body-wrapper .el-switch {
  pointer-events: none;
}

/* 响应式 */
@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
