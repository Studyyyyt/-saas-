<template>
  <div id="app">
    <OfflineStatusBar />
    <router-view></router-view>
  </div>

</template>

<script>
import OfflineStatusBar from '@/components/OfflineStatusBar.vue'
import { loadAiConfig } from '@/utils/aiConfig'

export default {
  name: 'AppRoot',
  components: {
    OfflineStatusBar
  },
  created() {
    // 预加载 AI 全局配置，供各业务页面判断 AI 入口显示/隐藏
    loadAiConfig()
  }
}
</script>
