<template>
  <!-- 仅在线时隐藏，其余状态（离线/同步中/失败/缓存回退）显示右下角小圆点 -->
  <div
    v-if="currentMode !== 'online'"
    class="offline-status-fab"
    :class="[`is-${currentMode}`, { 'is-pulse': currentMode === 'syncing' }]"
    :title="fullTitle"
    @click="handleClick"
  >
    <span class="offline-fab-dot"></span>

    <!-- hover 浮层：详细状态 -->
    <div class="offline-fab-tooltip">
      <div class="tooltip-row">
        <span class="tooltip-dot" :class="`dot-${currentMode}`"></span>
        <span class="tooltip-status">{{ statusText }}</span>
      </div>
      <div v-if="detailText" class="tooltip-detail">{{ detailText }}</div>
      <div v-if="showRetryButton" class="tooltip-action">点击圆点重试同步</div>
    </div>
  </div>
</template>

<script>
import { offlineStatusStore } from '@/utils/offline/network'
import { retryOfflineSync } from '@/utils/offline/sync'

export default {
  name: 'OfflineStatusBar',
  computed: {
    store() {
      return offlineStatusStore
    },
    currentRoutePath() {
      return this.$route && this.$route.path ? String(this.$route.path) : ''
    },
    activeCacheFallbacks() {
      return (this.store.cacheFallbackEntries || []).filter(item => {
        return String((item && item.routePath) || '') === this.currentRoutePath
      })
    },
    latestCacheFallbackAt() {
      const timestamps = this.activeCacheFallbacks
        .map(item => Date.parse(String((item && item.cachedAt) || '')))
        .filter(value => Number.isFinite(value))
      if (!timestamps.length) return ''
      return new Date(Math.max(...timestamps)).toISOString()
    },
    hasActiveCacheFallback() {
      return this.activeCacheFallbacks.length > 0
    },
    currentMode() {
      if (this.store.syncing) return 'syncing'
      if (this.store.failedCount > 0) return 'failed'
      if (this.hasActiveCacheFallback) return 'cached'
      if (!this.store.isOnline) return 'offline'
      return 'online'
    },
    statusText() {
      if (this.currentMode === 'cached') return '已回退缓存'
      if (this.currentMode === 'offline') return '当前离线'
      if (this.currentMode === 'syncing') return '正在同步'
      if (this.currentMode === 'failed') return '同步失败'
      return '当前在线'
    },
    detailText() {
      if (this.currentMode === 'cached') {
        const countText = this.activeCacheFallbacks.length > 1
          ? `当前页面 ${this.activeCacheFallbacks.length} 处数据显示最近缓存`
          : '当前页面显示最近缓存'
        const cachedAtText = this.formatDateTime(this.latestCacheFallbackAt)
        const reasonText = this.store.isOnline ? '请求失败' : '网络离线'
        return cachedAtText
          ? `${reasonText}，${countText}，缓存时间 ${cachedAtText}`
          : `${reasonText}，${countText}`
      }
      if (this.currentMode === 'offline') {
        return this.store.pendingCount > 0 ? `待同步 ${this.store.pendingCount} 条` : '核心列表将显示最近缓存'
      }
      if (this.currentMode === 'syncing') {
        const total = this.store.pendingCount + this.store.failedCount
        return total > 0 ? `剩余 ${total} 条待处理` : '正在回放离线操作'
      }
      if (this.currentMode === 'failed') {
        return this.store.failedCount > 0 ? `${this.store.failedCount} 条同步失败` : (this.store.lastError || '')
      }
      return ''
    },
    showRetryButton() {
      return this.store.isOnline && !this.store.syncing && this.store.failedCount > 0
    },
    fullTitle() {
      return `${this.statusText}${this.detailText ? ' · ' + this.detailText : ''}`
    }
  },
  methods: {
    formatDateTime(value) {
      const timestamp = Date.parse(String(value || ''))
      if (!Number.isFinite(timestamp)) return ''
      const date = new Date(timestamp)
      const pad = part => String(part).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    handleClick() {
      if (this.showRetryButton) {
        retryOfflineSync().catch(() => {})
      }
    }
  }
}
</script>

<style scoped>
/* === 右下角浮动小圆点 === */
.offline-status-fab {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 999;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease;
}

.offline-status-fab:hover {
  transform: scale(1.2);
}

/* 小圆点颜色 */
.offline-status-fab.is-online {
  background: #52c41a;
}

.offline-status-fab.is-offline {
  background: #faad14;
}

.offline-status-fab.is-cached {
  background: #faad14;
}

.offline-status-fab.is-syncing {
  background: #00a6c9;
}

.offline-status-fab.is-failed {
  background: #f86359;
}

/* 同步中脉冲动画 */
.is-pulse {
  animation: fab-pulse 1.4s ease-in-out infinite;
}

@keyframes fab-pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(0, 166, 201, 0.4); }
  50% { opacity: 0.85; box-shadow: 0 0 0 6px rgba(0, 166, 201, 0); }
}

/* hover 浮层 */
.offline-fab-tooltip {
  position: absolute;
  right: 0;
  bottom: 24px;
  min-width: 220px;
  max-width: 320px;
  padding: 10px 14px;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  font-size: 12px;
  line-height: 1.6;
  color: #3e3e3c;
  pointer-events: none;
  opacity: 0;
  transform: translateY(4px);
  transition: all 0.2s ease;
  white-space: normal;
}

.offline-status-fab:hover .offline-fab-tooltip {
  opacity: 1;
  transform: translateY(0);
}

.tooltip-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.tooltip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tooltip-dot.dot-online { background: #52c41a; }
.tooltip-dot.dot-offline { background: #faad14; }
.tooltip-dot.dot-cached { background: #faad14; }
.tooltip-dot.dot-syncing { background: #00a6c9; }
.tooltip-dot.dot-failed { background: #f86359; }

.tooltip-status {
  font-weight: 600;
  color: #1d222a;
}

.tooltip-detail {
  color: #636a74;
  word-break: break-word;
}

.tooltip-action {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed #e8e8e8;
  color: #00a6c9;
  font-weight: 500;
}
</style>
