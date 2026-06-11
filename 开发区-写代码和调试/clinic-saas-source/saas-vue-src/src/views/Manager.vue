<template>
  <div id="app" class="manager-shell apple-design-scope" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <AppTopNav @toggle-sidebar="toggleSidebar" />
    <AppSideNav :collapsed="sidebarCollapsed" @toggle="toggleSidebar" />
    <main class="manager-main">
      <router-view class="view-container apple-page-enter" />
    </main>
  </div>
</template>

<script>
import AppTopNav from '@/components/apple-design/AppTopNav.vue'
import AppSideNav from '@/components/apple-design/AppSideNav.vue'

const SIDEBAR_COLLAPSED_KEY = 'sidebar_collapsed'

export default {
  name: 'ManagerShell',
  components: {
    AppTopNav,
    AppSideNav
  },
  data() {
    return {
      sidebarCollapsed: false
    }
  },
  created() {
    // 从 localStorage 恢复侧边栏折叠状态
    try {
      const saved = localStorage.getItem(SIDEBAR_COLLAPSED_KEY)
      if (saved !== null) {
        this.sidebarCollapsed = saved === 'true'
      }
    } catch {
      // ignore
    }
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      try {
        localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(this.sidebarCollapsed))
      } catch {
        // ignore
      }
    }
  }
}
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
}

body {
  background: var(--apple-bg-primary);
  font-family: var(--apple-font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.manager-shell {
  min-height: 100vh;
  background: var(--apple-bg-primary);
  /* 顶部中央青瓷环境光晕 */
  background-image:
    radial-gradient(ellipse 80% 50% at 50% -10%, rgba(90, 143, 123, 0.07), transparent),
    url("data:image/svg+xml,%3Csvg width='6' height='6' viewBox='0 0 6 6' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23000000' fill-opacity='0.015' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='1'/%3E%3C/g%3E%3C/svg%3E");
}

.manager-main {
  padding-top: var(--apple-nav-height);
  min-height: 100vh;
  box-sizing: border-box;
  margin-left: var(--apple-sidebar-width);
  transition: margin-left var(--apple-transition-normal);
}

/* 侧边栏折叠时主内容区域左移减少 */
.sidebar-collapsed .manager-main {
  margin-left: var(--apple-sidebar-collapsed-width);
}

/* 移动端隐藏侧边栏时主内容区域全宽 */
@media (max-width: 1100px) {
  .manager-main {
    margin-left: 0 !important;
  }
}

.view-container {
  max-width: var(--apple-content-max-width);
  margin: 0 auto;
  padding: var(--apple-space-xl) var(--apple-content-padding);
  min-height: calc(100vh - var(--apple-nav-height));
}

/* 页面进入动画 */
.apple-page-enter {
  animation: apple-fade-in-up 0.5s cubic-bezier(0.25, 0.1, 0.25, 1.0) forwards;
  opacity: 0;
}

@keyframes apple-fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
