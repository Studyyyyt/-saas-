<template>
  <div class="timeline-item">
    <div class="timeline-item__dot" :style="dotStyle">
      <i :class="icon" />
    </div>
    <div class="timeline-item__content">
      <div class="timeline-item__header">
        <span class="timeline-item__title">{{ title }}</span>
        <span class="timeline-item__time">{{ time }}</span>
      </div>
      <div v-if="description" class="timeline-item__desc">{{ description }}</div>
      <div v-if="$slots.extra" class="timeline-item__extra">
        <slot name="extra" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TimelineItem',
  props: {
    title: { type: String, required: true },
    time: { type: String, default: '' },
    description: { type: String, default: '' },
    icon: { type: String, default: 'el-icon-time' },
    color: { type: String, default: '#5A8F7B' }
  },
  computed: {
    dotStyle() {
      return {
        background: this.color,
        boxShadow: `0 4px 12px ${this.color}40`
      }
    }
  }
}
</script>

<style scoped>
.timeline-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  position: relative;
}

.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 19px;
  top: 48px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #e2e8f0 0%, transparent 100%);
}

.timeline-item__dot {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
  z-index: 1;
}

.timeline-item__content {
  flex: 1;
  min-width: 0;
}

.timeline-item__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 6px;
}

.timeline-item__title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.timeline-item__time {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.timeline-item__desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.timeline-item__extra {
  margin-top: 10px;
}
</style>
