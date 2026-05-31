<template>
  <div class="settings-page">
    <!-- 页面标题 -->
    <div class="settings-header">
      <h1 class="settings-title">系统设置</h1>
      <p class="settings-subtitle">统一管理门诊基础参数、账号权限与业务配置</p>
    </div>

    <!-- 设置分类网格 -->
    <div class="settings-grid">
      <div
        v-for="group in settingGroups"
        :key="group.key"
        class="settings-group-card"
        @click="goTo(group)"
      >
        <div class="settings-group-icon" :style="{ background: group.gradient }">
          {{ group.icon }}
        </div>
        <div class="settings-group-body">
          <div class="settings-group-name">{{ group.name }}</div>
          <div class="settings-group-desc">{{ group.desc }}</div>
          <div class="settings-group-tags">
            <span v-for="tag in group.tags" :key="tag" class="settings-tag">{{ tag }}</span>
          </div>
        </div>
        <div class="settings-group-arrow">
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
    </div>

    <!-- 系统信息栏 -->
    <div class="system-info-bar">
      <div class="system-info-item">
        <span class="system-info-label">系统版本</span>
        <span class="system-info-value">v2.0.0</span>
      </div>
      <div class="system-info-divider"></div>
      <div class="system-info-item">
        <span class="system-info-label">诊所名称</span>
        <span class="system-info-value">某某口腔</span>
      </div>
      <div class="system-info-divider"></div>
      <div class="system-info-item">
        <span class="system-info-label">数据存储</span>
        <span class="system-info-value">本地 + 云端同步</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SystemSettingsView',
  data() {
    return {
      settingGroups: [
        {
          key: 'ai-agent',
          name: 'AI 助手配置',
          icon: '🤖',
          gradient: 'linear-gradient(135deg, #5A8F7B, #6BA08C)',
          desc: '自定义首页 AI 助手角色、快捷指令与外观主题',
          path: '/SystemAIAgentConfig',
          tags: ['助手管理', '快捷指令']
        },
        {
          key: 'model-provider',
          name: '模型供应商配置',
          icon: '⚙️',
          gradient: 'linear-gradient(135deg, #0891b2, #06b6d4)',
          desc: '配置 AI 对话所使用的模型供应商接口、密钥与模型参数',
          path: '/SystemModelProviderConfig',
          tags: ['模型配置', 'API 密钥']
        },
        {
          key: 'treatment',
          name: '项目与治疗',
          icon: '🦷',
          gradient: 'linear-gradient(135deg, #5A8F7B, #6BA08C)',
          desc: '治疗项目库、操作字典、标准流程维护',
          path: '/SystemTreatmentCatalog',
          tags: ['项目库', '操作字典']
        },
        {
          key: 'financial',
          name: '财务与收费',
          icon: '💳',
          gradient: 'linear-gradient(135deg, #059669, #10b981)',
          desc: '收款渠道配置、收费标准、财务分类',
          path: '/SystemPaymentChannel',
          tags: ['收款渠道']
        },
        {
          key: 'consent',
          name: '知情同意书',
          icon: '📄',
          gradient: 'linear-gradient(135deg, #d97706, #f59e0b)',
          desc: '知情同意书模板库、签署配置',
          path: '/SystemConsentTemplate',
          tags: ['模板库']
        },
        {
          key: 'open-api',
          name: '开放接口',
          icon: '🔑',
          gradient: 'linear-gradient(135deg, #d97706, #f59e0b)',
          desc: '管理 API Key，供 n8n、MCP 等外部系统调用',
          path: '/SystemSettings/open/api-key',
          tags: ['API Key', '外部集成']
        },
        {
          key: 'account',
          name: '账号与权限',
          icon: '🔐',
          gradient: 'linear-gradient(135deg, #7c3aed, #a78bfa)',
          desc: '员工账号管理、角色权限分配、菜单权限',
          path: '/SystemAccountManage',
          tags: ['账号管理', '权限配置']
        },
        {
          key: 'lab',
          name: '义齿加工',
          icon: '⚙️',
          gradient: 'linear-gradient(135deg, #0891b2, #06b6d4)',
          desc: '加工厂档案、产品库、加工单流程',
          path: '/lab-factories',
          tags: ['加工厂', '产品库']
        },
        {
          key: 'material',
          name: '耗材管理',
          icon: '📦',
          gradient: 'linear-gradient(135deg, #dc2626, #ef4444)',
          desc: '耗材分类、库存预警、采购配置',
          path: '/material-categories',
          tags: ['耗材分类', '库存预警']
        },
        {
          key: 'api-docs',
          name: 'API 接口文档',
          icon: '📘',
          gradient: 'linear-gradient(135deg, #2563eb, #3b82f6)',
          desc: '在线查看 Swagger 接口文档，供外部系统对接调试',
          path: 'http://localhost:8080/swagger-ui/index.html',
          external: true,
          tags: ['Swagger', 'OpenAPI']
        }
      ]
    }
  },
  methods: {
    goTo(group) {
      if (group.external) {
        window.open(group.path, '_blank')
      } else {
        this.$router.push(group.path)
      }
    }
  }
}
</script>

<style scoped>
.settings-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: calc(100vh - var(--apple-nav-height));
  box-sizing: border-box;
}

/* 页面标题 */
.settings-header {
  text-align: center;
  margin-bottom: 36px;
}

.settings-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0f172a;
  line-height: 1.2;
}

.settings-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #64748b;
  font-weight: 400;
}

/* 设置分类网格 */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.settings-group-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  overflow: hidden;
}

.settings-group-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: var(--apple-shadow-lg), var(--apple-shadow-glow);
}

.settings-group-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.settings-group-body {
  flex: 1;
  min-width: 0;
}

.settings-group-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 4px;
}

.settings-group-desc {
  font-size: 12px;
  color: var(--apple-text-secondary);
  line-height: 1.5;
  margin-bottom: 8px;
}

.settings-group-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.settings-tag {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: var(--apple-accent);
  background: var(--apple-accent-light);
}

.settings-group-arrow {
  width: 32px;
  height: 32px;
  border-radius: var(--apple-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-text-tertiary);
  transition: all var(--apple-transition-fast);
  flex-shrink: 0;
}

.settings-group-card:hover .settings-group-arrow {
  background: var(--apple-accent-light);
  color: var(--apple-accent);
  transform: translateX(2px);
}

/* 系统信息栏 */
.system-info-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 16px 24px;
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-sm);
  border-radius: 14px;
  flex-wrap: wrap;
}

.system-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.system-info-label {
  font-size: 12px;
  color: var(--apple-text-tertiary);
}

.system-info-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.system-info-divider {
  width: 1px;
  height: 16px;
  background: var(--apple-divider);
}

/* 响应式 */
@media (max-width: 768px) {
  .settings-page {
    padding: 16px;
  }

  .settings-title {
    font-size: 24px;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .system-info-bar {
    flex-direction: column;
    gap: 8px;
  }

  .system-info-divider {
    display: none;
  }
}
</style>
