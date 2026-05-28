<template>
  <div class="ai-quick-panel">
    <div class="quick-panel-header">
      <span class="quick-panel-title">快捷指令</span>
      <span class="quick-panel-hint">点击填入输入框，可编辑后发送</span>
    </div>
    <div class="quick-cards">
      <div
        v-for="(item, index) in displayItems"
        :key="index"
        class="quick-card"
        :style="item.gradient ? { '--card-gradient': item.gradient } : {}"
        @click="handleClick(item)"
      >
        <div class="quick-card-icon">
          <i :class="item.icon || 'el-icon-cpu'" />
        </div>
        <div class="quick-card-body">
          <div class="quick-card-label">{{ item.label }}</div>
          <div class="quick-card-desc">{{ item.desc }}</div>
        </div>
        <div class="quick-card-arrow">
          <i class="el-icon-arrow-right" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * AI 快捷指令面板
 * 将 Agent 列表渲染为可点击卡片，点击后填入输入框（不自动发送）
 */
export default {
  name: 'AiQuickPanel',
  props: {
    agents: { type: Array, default: () => [] }
  },
  computed: {
    displayItems() {
      return (this.agents || []).map(agent => ({
        label: agent.name || agent.agentKey,
        desc: agent.description || `使用「${agent.name || agent.agentKey}」Agent`,
        icon: agent.icon || 'el-icon-cpu',
        value: agent.presetMessage || (agent.chips && agent.chips[0]) || `请使用「${agent.name || agent.agentKey}」Agent`,
        agentKey: agent.agentKey,
        gradient: agent.gradient || ''
      }))
    }
  },
  methods: {
    handleClick(item) {
      this.$emit('select', item)
    }
  }
}
</script>

<style scoped>
.ai-quick-panel {
  margin-bottom: 16px;
}

.quick-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 0 2px;
}

.quick-panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #2C3E35);
}

.quick-panel-hint {
  font-size: 12px;
  color: var(--text-muted, #A0A0A0);
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #fff;
  border: 1px solid var(--border-color, rgba(90, 143, 123, 0.15));
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
  overflow: hidden;
}

.quick-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--card-gradient, linear-gradient(180deg, #5A8F7B, #6BA08C));
  opacity: 0;
  transition: opacity 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(90, 143, 123, 0.1);
  border-color: rgba(90, 143, 123, 0.3);
}

.quick-card:hover::before {
  opacity: 1;
}

.quick-card-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--primary-light, rgba(90, 143, 123, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--primary, #5A8F7B);
  font-size: 16px;
}

.quick-card-body {
  flex: 1;
  min-width: 0;
}

.quick-card-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #2C3E35);
  line-height: 1.4;
}

.quick-card-desc {
  font-size: 11px;
  color: var(--text-secondary, #6B6B6B);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-card-arrow {
  color: var(--text-muted, #A0A0A0);
  font-size: 12px;
  transition: transform 0.2s ease, color 0.2s ease;
}

.quick-card:hover .quick-card-arrow {
  transform: translateX(3px);
  color: var(--primary, #5A8F7B);
}

@media (max-width: 768px) {
  .quick-cards {
    grid-template-columns: 1fr;
  }
}
</style>
