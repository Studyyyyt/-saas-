<template>
  <div class="help-index-page">
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎使用帮助中心</h1>
      <p class="welcome-desc">
        这里汇集了口腔 SaaS 管理系统各模块的功能说明与操作指南。
        你可以在左侧目录中选择感兴趣的主题进行阅读。
      </p>
    </div>

    <div class="quick-links">
      <div class="quick-link-card" @click="goTo('/SystemSettings/help/ai/medical')">
        <div class="quick-link-icon"><i class="el-icon-first-aid-kit" /></div>
        <div class="quick-link-title">病历 AI 扩写</div>
        <div class="quick-link-desc">了解如何配置 AI 自动扩写病历，提高书写效率</div>
      </div>
      <div class="quick-link-card" @click="goTo('/SystemSettings/help/ai/scene')">
        <div class="quick-link-icon"><i class="el-icon-collection" /></div>
        <div class="quick-link-title">诊疗场景库</div>
        <div class="quick-link-desc">维护病种和标准化治疗步骤，辅助 AI 生成规范病历</div>
      </div>
      <div class="quick-link-card" @click="goTo('/SystemSettings/help/ai/patient')">
        <div class="quick-link-icon"><i class="el-icon-user" /></div>
        <div class="quick-link-title">患者洞察</div>
        <div class="quick-link-desc">配置 AI 患者画像分析，快速了解患者背景</div>
      </div>
      <div class="quick-link-card" @click="goTo('/SystemSettings/help/ai/model')">
        <div class="quick-link-icon"><i class="el-icon-cpu" /></div>
        <div class="quick-link-title">模型供应商</div>
        <div class="quick-link-desc">配置 DeepSeek、OpenAI 等大模型 API 连接</div>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-info" /> 关于本文档</div>
      <div class="help-content">
        <p>本文档按模块组织，每个模块独立成页，方便你快速定位需要了解的功能。部分模块标记为「待补充」，表示帮助文档正在编写中，后续会持续更新。</p>
        <p>如果你在使用过程中遇到问题，可以通过以下方式获取支持：</p>
        <ul>
          <li>优先查阅对应模块的帮助文档</li>
          <li>联系系统管理员或技术支持团队</li>
          <li>查看系统内的操作提示和引导</li>
        </ul>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title"><i class="el-icon-s-grid" /> 模块索引</div>
      <div class="module-index">
        <div v-for="group in moduleIndex" :key="group.key" class="index-group">
          <div class="index-group-title"><i :class="group.icon" /> {{ group.title }}</div>
          <div class="index-items">
            <div
              v-for="item in group.children"
              :key="item.key"
              class="index-item"
              :class="{ pending: item.status === 'pending' }"
              @click="goTo(item.path)"
            >
              <span>{{ item.title }}</span>
              <el-tag v-if="item.status === 'pending'" size="mini" type="info">待补充</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 帮助文档首页
 * 展示欢迎语、快捷入口和模块索引
 */

const moduleIndex = [
  {
    key: 'ai',
    title: 'AI 智能中心',
    icon: 'el-icon-cpu',
    children: [
      { key: 'ai-overview', title: 'AI 总览', path: '/SystemSettings/help/ai/overview', status: 'pending' },
      { key: 'ai-medical', title: '病历 AI 扩写配置', path: '/SystemSettings/help/ai/medical', status: 'completed' },
      { key: 'ai-scene', title: '诊疗场景库', path: '/SystemSettings/help/ai/scene', status: 'completed' },
      { key: 'ai-patient', title: '患者洞察配置', path: '/SystemSettings/help/ai/patient', status: 'completed' },
      { key: 'ai-model', title: '模型供应商配置', path: '/SystemSettings/help/ai/model', status: 'completed' },
      { key: 'ai-agent', title: '首页助手配置', path: '/SystemSettings/help/ai/agent', status: 'pending' },
      { key: 'ai-link', title: 'Agent 链接配置', path: '/SystemSettings/help/ai/link', status: 'pending' }
    ]
  },
  {
    key: 'basic',
    title: '基础设置',
    icon: 'el-icon-s-tools',
    children: [
      { key: 'basic-treatment', title: '项目与治疗', path: '/SystemSettings/help/basic/treatment', status: 'pending' },
      { key: 'basic-payment', title: '财务与收费', path: '/SystemSettings/help/basic/payment', status: 'pending' },
      { key: 'basic-consent', title: '知情同意书', path: '/SystemSettings/help/basic/consent', status: 'pending' },
      { key: 'basic-lab', title: '义齿加工', path: '/SystemSettings/help/basic/lab', status: 'pending' },
      { key: 'basic-material', title: '耗材管理', path: '/SystemSettings/help/basic/material', status: 'pending' },
      { key: 'basic-account', title: '账号与权限', path: '/SystemSettings/help/basic/account', status: 'pending' }
    ]
  },
  {
    key: 'patient',
    title: '患者与病历',
    icon: 'el-icon-user',
    children: [
      { key: 'patient-list', title: '患者列表', path: '/SystemSettings/help/patient/list', status: 'pending' },
      { key: 'patient-360', title: '患者 360° 视图', path: '/SystemSettings/help/patient/360', status: 'pending' },
      { key: 'medical-record', title: '病历管理', path: '/SystemSettings/help/patient/record', status: 'pending' },
      { key: 'followup', title: '随访管理', path: '/SystemSettings/help/patient/followup', status: 'pending' },
      { key: 'consultation', title: '咨询管理', path: '/SystemSettings/help/patient/consultation', status: 'pending' }
    ]
  },
  {
    key: 'inventory',
    title: '库存与耗材',
    icon: 'el-icon-box',
    children: [
      { key: 'inventory-material', title: '材料管理', path: '/SystemSettings/help/inventory/material', status: 'pending' },
      { key: 'inventory-purchase', title: '采购管理', path: '/SystemSettings/help/inventory/purchase', status: 'pending' },
      { key: 'inventory-statistics', title: '库存统计', path: '/SystemSettings/help/inventory/statistics', status: 'pending' }
    ]
  },
  {
    key: 'lab',
    title: '技工与加工',
    icon: 'el-icon-office-building',
    children: [
      { key: 'lab-factory', title: '加工厂管理', path: '/SystemSettings/help/lab/factory', status: 'pending' },
      { key: 'lab-order', title: '加工订单', path: '/SystemSettings/help/lab/order', status: 'pending' },
      { key: 'lab-bill', title: '加工对账单', path: '/SystemSettings/help/lab/bill', status: 'pending' }
    ]
  }
]

export default {
  name: 'HelpDocumentIndexView',
  data() {
    return {
      moduleIndex
    }
  },
  methods: {
    goTo(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    }
  }
}
</script>

<style scoped>
.help-index-page {
  padding: 0 0 32px;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: var(--apple-text-secondary);
  line-height: 1.7;
}

/* 快捷入口 */
.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.quick-link-card {
  background: var(--apple-surface);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-sm);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-link-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--apple-shadow-md);
  border-color: var(--apple-accent);
}

.quick-link-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--apple-accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.quick-link-icon i {
  font-size: 20px;
  color: var(--apple-accent);
}

.quick-link-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 6px;
}

.quick-link-desc {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  line-height: 1.5;
}

/* 通用卡片 */
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

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title i {
  font-size: 18px;
  color: var(--apple-accent);
}

.help-content p {
  font-size: 13px;
  color: var(--apple-text-secondary);
  line-height: 1.8;
  margin: 8px 0;
}

.help-content ul {
  font-size: 13px;
  color: var(--apple-text-secondary);
  line-height: 1.8;
  padding-left: 20px;
  margin: 8px 0;
}

.help-content li {
  margin: 4px 0;
}

/* 模块索引 */
.module-index {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.index-group-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.index-group-title i {
  font-size: 16px;
  color: var(--apple-accent);
}

.index-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.index-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: var(--apple-bg-primary);
  border-radius: 8px;
  font-size: 13px;
  color: var(--apple-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.index-item:hover {
  background: var(--apple-accent-light);
  color: var(--apple-accent);
}

.index-item.pending {
  color: var(--apple-text-tertiary);
  cursor: default;
}

.index-item.pending:hover {
  background: var(--apple-bg-primary);
  color: var(--apple-text-tertiary);
}

@media (max-width: 768px) {
  .quick-links {
    grid-template-columns: 1fr;
  }

  .module-index {
    grid-template-columns: 1fr;
  }
}
</style>
