<template>
  <div id="app" class="manager-shell apple-design-scope">
    <AppTopNav />
    <main class="manager-main">
      <router-view class="view-container apple-page-enter" />
    </main>
  </div>
</template>

<script>
import AppTopNav from '@/components/apple-design/AppTopNav.vue'

export default {
  name: 'ManagerShell',
  components: {
    AppTopNav
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
  /* 顶部中央蓝色环境光晕 */
  background-image:
    radial-gradient(ellipse 80% 50% at 50% -10%, rgba(37, 99, 235, 0.07), transparent),
    url("data:image/svg+xml,%3Csvg width='6' height='6' viewBox='0 0 6 6' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%23000000' fill-opacity='0.015' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='1'/%3E%3C/g%3E%3C/svg%3E");
}

.manager-main {
  padding-top: var(--apple-nav-height);
  min-height: 100vh;
  box-sizing: border-box;
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
