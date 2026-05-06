<template>
  <div class="offline-status-shell">
    <div class="offline-status-bar" :class="barClass">
      <div class="offline-status-main">
        <span class="offline-status-dot"></span>
        <span class="offline-status-text">{{ statusText }}</span>
        <span v-if="detailText" class="offline-status-detail">{{ detailText }}</span>
      </div>
      <el-button
        v-if="showRetryButton"
        type="text"
        size="mini"
        class="offline-status-action"
        @click="retrySync"
      >
        重试同步
      </el-button>
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
    barClass() {
      return `is-${this.currentMode}`
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
      if (this.store.pendingCount > 0) {
        return `待同步 ${this.store.pendingCount} 条`
      }
      return this.store.lastSyncedAt ? '离线基础层已启用' : '网络正常'
    },
    showRetryButton() {
      return this.store.isOnline && !this.store.syncing && this.store.failedCount > 0
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
    retrySync() {
      retryOfflineSync().catch(() => {})
    }
  }
}
</script>

<style scoped>
.offline-status-shell {
  position: sticky;
  top: 0;
  z-index: 1200;
}

.offline-status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 16px;
  border-bottom: 1px solid transparent;
  font-size: 12px;
}

.offline-status-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.offline-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  flex-shrink: 0;
  background: currentColor;
}

.offline-status-text {
  font-weight: 700;
}

.offline-status-detail {
  color: inherit;
  opacity: 0.86;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.offline-status-action {
  padding: 0;
}

.offline-status-bar.is-online {
  background: #ecfdf5;
  color: #166534;
  border-bottom-color: #bbf7d0;
}

.offline-status-bar.is-offline {
  background: #fff7ed;
  color: #c2410c;
  border-bottom-color: #fed7aa;
}

.offline-status-bar.is-cached {
  background: #fffbeb;
  color: #b45309;
  border-bottom-color: #fde68a;
}

.offline-status-bar.is-syncing {
  background: #eff6ff;
  color: #1d4ed8;
  border-bottom-color: #bfdbfe;
}

.offline-status-bar.is-failed {
  background: #fef2f2;
  color: #b91c1c;
  border-bottom-color: #fecaca;
}
</style>
