<template>
  <div class="kpi-card" :class="[`kpi-card--${type}`, { 'kpi-card--clickable': clickable }]">
    <div class="kpi-card__header">
      <div class="kpi-card__icon" :style="iconStyle">
        <i :class="icon"></i>
      </div>
      <div v-if="trend !== null" class="kpi-card__trend" :class="`kpi-card__trend--${trendDirection}`">
        <i :class="trendIcon"></i>
        <span>{{ Math.abs(trend) }}%</span>
      </div>
    </div>
    <div class="kpi-card__body">
      <div class="kpi-card__value">{{ formattedValue }}</div>
      <div class="kpi-card__label">{{ label }}</div>
      <div v-if="description" class="kpi-card__desc">{{ description }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KpiCard',
  props: {
    label: { type: String, default: '' },
    value: { type: [Number, String], default: 0 },
    description: { type: String, default: '' },
    icon: { type: String, default: 'el-icon-data-analysis' },
    type: { type: String, default: 'primary' },
    trend: { type: Number, default: null },
    prefix: { type: String, default: '' },
    suffix: { type: String, default: '' },
    clickable: { type: Boolean, default: false }
  },
  computed: {
    trendDirection() {
      if (this.trend === null) return ''
      return this.trend >= 0 ? 'up' : 'down'
    },
    trendIcon() {
      return this.trend >= 0 ? 'el-icon-top' : 'el-icon-bottom'
    },
    formattedValue() {
      const val = this.value
      if (typeof val === 'number') {
        if (this.prefix === '¥') {
          return '¥' + val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
        }
        return this.prefix + val.toLocaleString('zh-CN') + this.suffix
      }
      return this.prefix + val + this.suffix
    },
    iconStyle() {
      const colorMap = {
        primary: 'linear-gradient(135deg, #2563eb, #60a5fa)',
        success: 'linear-gradient(135deg, #10b981, #34d399)',
        warning: 'linear-gradient(135deg, #f59e0b, #fbbf24)',
        danger: 'linear-gradient(135deg, #ef4444, #fb7185)',
        info: 'linear-gradient(135deg, #0ea5e9, #38bdf8)'
      }
      return {
        background: colorMap[this.type] || colorMap.primary
      }
    }
  }
}
</script>

<style scoped>
.kpi-card {
  background: #ffffff;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid transparent;
}

.kpi-card--clickable {
  cursor: pointer;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(31, 71, 136, 0.12);
}

.kpi-card--primary {
  border-top: 3px solid #2563eb;
}

.kpi-card--success {
  border-top: 3px solid #10b981;
}

.kpi-card--warning {
  border-top: 3px solid #f59e0b;
}

.kpi-card--danger {
  border-top: 3px solid #ef4444;
}

.kpi-card--info {
  border-top: 3px solid #0ea5e9;
}

.kpi-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.kpi-card__icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.kpi-card__trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 8px;
}

.kpi-card__trend--up {
  color: #059669;
  background: #d1fae5;
}

.kpi-card__trend--down {
  color: #dc2626;
  background: #fee2e2;
}

.kpi-card__value {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.2;
  margin-bottom: 6px;
}

.kpi-card__label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.kpi-card__desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
