<template>
  <div class="ai-overview-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">AI 总览</h1>
        <p class="page-subtitle">管理系统所有 AI 功能的开关与运行状态</p>
      </div>
    </div>

    <!-- 全局开关区 -->
    <div class="section-card">
      <div class="section-title">全局控制</div>
      <div class="global-switches">
        <div class="switch-item">
          <div class="switch-info">
            <div class="switch-label">AI 功能总开关</div>
            <div class="switch-desc">关闭后，系统所有 AI 入口将隐藏且不可用</div>
          </div>
          <el-switch
            v-model="globalEnabled"
            active-text="开启"
            inactive-text="关闭"
            @change="onGlobalSwitchChange"
          />
        </div>
        <div class="switch-item">
          <div class="switch-info">
            <div class="switch-label">调试模式</div>
            <div class="switch-desc">开启后，AI 请求会在浏览器控制台打印详细日志</div>
          </div>
          <el-switch
            v-model="debugMode"
            active-text="开启"
            inactive-text="关闭"
          />
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #2563eb, #3b82f6);">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.todayCalls }}</div>
          <div class="stat-label">今日 AI 调用</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #059669, #10b981);">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.todayTokens }}</div>
          <div class="stat-label">今日 Token 消耗</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #d97706, #f59e0b);">
          <i class="el-icon-magic-stick"></i>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.activeFunctions }} / {{ stats.totalFunctions }}</div>
          <div class="stat-label">活跃功能数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #dc2626, #ef4444);">
          <i class="el-icon-warning-outline"></i>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.errorRate }}%</div>
          <div class="stat-label">错误率</div>
        </div>
      </div>
    </div>

    <!-- 功能列表区 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">AI 功能列表</div>
        <el-button size="small" icon="el-icon-refresh" @click="loadFunctions">刷新</el-button>
      </div>
      <el-table :data="aiFunctions" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="功能名称" min-width="140">
          <template slot-scope="scope">
            <div class="func-name">
              <span class="func-icon">{{ scope.row.icon }}</span>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="page" label="所属页面" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">
              {{ scope.row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="todayCalls" label="今日调用" width="100" align="center" />
        <el-table-column prop="modelName" label="使用模型" min-width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="goToConfig(scope.row)">配置</el-button>
            <el-switch
              v-model="scope.row.enabled"
              active-color="#13ce66"
              inactive-color="#dcdfe6"
              @change="onFunctionSwitchChange(scope.row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AIOverviewView',
  data() {
    return {
      globalEnabled: true,
      debugMode: false,
      loading: false,
      stats: {
        todayCalls: 128,
        todayTokens: '45.2K',
        activeFunctions: 3,
        totalFunctions: 5,
        errorRate: 2.1
      },
      aiFunctions: [
        {
          key: 'home-assistant',
          name: '首页 AI 助手',
          icon: '🤖',
          page: '首页',
          enabled: true,
          todayCalls: 128,
          modelName: 'DeepSeek-V3',
          configPath: '/SystemSettings/ai/agent'
        },
        {
          key: 'medical-expand',
          name: '病历 AI 扩写',
          icon: '📝',
          page: '病历编辑',
          enabled: true,
          todayCalls: 56,
          modelName: 'DeepSeek-V3',
          configPath: '/SystemSettings/ai/pages/medical'
        },
        {
          key: 'patient-insight',
          name: '患者 AI 洞察',
          icon: '🔍',
          page: '患者列表',
          enabled: false,
          todayCalls: 0,
          modelName: '-',
          configPath: '/SystemSettings/ai/pages/patient'
        },
        {
          key: 'followup-generate',
          name: '智能随访生成',
          icon: '📞',
          page: '随访管理',
          enabled: true,
          todayCalls: 23,
          modelName: 'DeepSeek-V3',
          configPath: '/SystemSettings/ai/link'
        },
        {
          key: 'business-analysis',
          name: '经营 AI 分析',
          icon: '📊',
          page: '经营分析',
          enabled: false,
          todayCalls: 0,
          modelName: '-',
          configPath: '/SystemSettings/ai/link'
        }
      ]
    }
  },
  created() {
    this.loadFunctions()
  },
  methods: {
    loadFunctions() {
      this.loading = true
      // TODO: 调用后端接口 GET /api/ai-config/functions
      setTimeout(() => {
        this.loading = false
      }, 300)
    },
    onGlobalSwitchChange(val) {
      this.$message.success(val ? 'AI 功能已全局开启' : 'AI 功能已全局关闭')
      // TODO: 调用后端接口保存全局开关状态
    },
    onFunctionSwitchChange(row) {
      this.$message.success(`${row.name} 已${row.enabled ? '启用' : '禁用'}`)
      // TODO: 调用后端接口 PUT /api/ai-config/functions/{key}
    },
    goToConfig(row) {
      if (row.configPath) {
        this.$router.push(row.configPath)
      }
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
  margin-bottom: 28px;
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
  gap: 16px;
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--apple-divider);
}

.switch-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.switch-info {
  flex: 1;
}

.switch-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 4px;
}

.switch-desc {
  font-size: 12px;
  color: var(--apple-text-tertiary);
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
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-body {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--apple-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  margin-top: 2px;
}

/* 功能列表 */
.func-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.func-icon {
  font-size: 18px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
