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
import tablePersist from './mixins/tablePersist'
import axios from 'axios'
import { getAdminSession } from './utils/adminSession'

Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.mixin(tablePersist);
initOfflineSync()

axios.interceptors.request.use(
  config => {
    const session = getAdminSession()
    if (session && session.id) {
      config.headers = config.headers || {}
      config.headers['X-Operator-Account-Id'] = session.id
    }
    return config
  },
  error => Promise.reject(error)
)

axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      const status = error.response.status
      const msg = error.response.data && error.response.data.msg ? error.response.data.msg : '请求失败，请稍后重试'
      if (status >= 500) {
        Vue.prototype.$message.error('服务器错误：' + msg)
      } else if (status === 403) {
        Vue.prototype.$message.warning('权限不足：' + msg)
      } else if (status === 404) {
        Vue.prototype.$message.warning('请求的资源不存在')
      } else if (status >= 400) {
        Vue.prototype.$message.warning(msg)
      }
    } else if (error.request) {
      Vue.prototype.$message.error('网络连接失败，请检查网络')
    } else {
      Vue.prototype.$message.error('请求发送失败：' + error.message)
    }
    return Promise.reject(error)
  }
)

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
