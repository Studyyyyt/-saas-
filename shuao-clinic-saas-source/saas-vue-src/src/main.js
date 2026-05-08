import Vue from 'vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './styles/button-theme.css'
import './styles/typography-scale.css'
import './styles/design-tokens.css'
import './styles/element-overrides.css'
import './styles/animations.css'
import './styles/apple-design/design-tokens.css'
import './styles/apple-design/element-overrides.css'
import './styles/apple-design/animations.css'
import App from './App.vue'
import router from './router'
import { initOfflineSync } from './utils/offline/sync'


Vue.config.productionTip = false
Vue.use(ElementUI);
initOfflineSync()

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
