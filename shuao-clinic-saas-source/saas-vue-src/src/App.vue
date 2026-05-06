<template>
  <div id="app">
    <OfflineStatusBar />
    <router-view></router-view>
  </div>

</template>

<script>
import OfflineStatusBar from '@/components/OfflineStatusBar.vue'

export default {
  name: 'AppRoot',
  components: {
    OfflineStatusBar
  }
}
</script>
