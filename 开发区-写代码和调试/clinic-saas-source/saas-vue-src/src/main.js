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
import { getAdminSession, clearAdminSession } from './utils/adminSession'

Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.mixin(tablePersist);
initOfflineSync()

let isRefreshingKey = false
let refreshKeyQueue = []
let isLicenseExpiredHandling = false

// 获取 API Key 并更新 localStorage
async function refreshApiKey() {
  if (isRefreshingKey) {
    return new Promise((resolve) => {
      refreshKeyQueue.push(resolve)
    })
  }
  isRefreshingKey = true
  try {
    const session = getAdminSession()
    const clinicId = session && session.currentClinicId ? session.currentClinicId : 'default'
    const res = await axios.get('/api/api-key', { params: { clinicId } })
    if (res.data && res.data.code === '200' && res.data.data && res.data.data.key) {
      localStorage.setItem('clinic_api_key', res.data.data.key)
      // 通知所有等待的队列项
      while (refreshKeyQueue.length) {
        const resolve = refreshKeyQueue.shift()
        resolve(true)
      }
      return true
    }
  } catch (e) {
    console.warn('获取 API Key 失败', e)
  } finally {
    isRefreshingKey = false
  }
  return false
}

axios.interceptors.request.use(
  config => {
    const session = getAdminSession()
    if (session && session.id) {
      config.headers = config.headers || {}
      config.headers['X-Operator-Account-Id'] = session.id
    }
    // 附加当前诊所ID
    if (session && session.currentClinicId) {
      config.headers = config.headers || {}
      config.headers['X-Clinic-Id'] = session.currentClinicId
    }
    // 自动附加 API Key（单 Key 模式，所有请求强制认证）
    const apiKey = localStorage.getItem('clinic_api_key')
    if (apiKey) {
      config.headers = config.headers || {}
      config.headers['X-API-Key'] = apiKey
    }
    return config
  },
  error => Promise.reject(error)
)

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config
    if (error.response) {
      const status = error.response.status
      const data = error.response.data || {}
      const msg = data.msg || '请求失败，请稍后重试'

      // 401 且是 Key 相关错误：自动获取 Key 并重试一次
      if (status === 401 && msg.includes('API Key') && !originalRequest._retry) {
        originalRequest._retry = true
        const refreshed = await refreshApiKey()
        if (refreshed) {
          const apiKey = localStorage.getItem('clinic_api_key')
          if (apiKey) {
            originalRequest.headers = originalRequest.headers || {}
            originalRequest.headers['X-API-Key'] = apiKey
          }
          return axios(originalRequest)
        }
        Vue.prototype.$message.error('API Key 获取失败，请前往系统设置 > 开放接口 > API Key 管理 重新获取')
      } else if (status === 401) {
        Vue.prototype.$message.error('登录已过期，请重新登录')
      } else if (status >= 500) {
        Vue.prototype.$message.error('服务器错误：' + msg)
      } else if (status === 403) {
        // 检测授权失效头（激活码被删除或过期）
        const licenseInvalid = error.response.headers && error.response.headers['x-license-invalid']
        if (licenseInvalid || (msg && msg.includes('授权无效'))) {
          // 避免多个并发请求同时触发多次弹窗和跳转
          if (!isLicenseExpiredHandling) {
            isLicenseExpiredHandling = true
            Vue.prototype.$message.error('系统授权已失效，请重新登录')
            clearAdminSession()
            setTimeout(() => {
              window.location.href = '/login1'
            }, 1500)
          }
          return Promise.reject(error)
        }
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

// 启动前确保 localStorage 中有 API Key
async function bootstrap() {
  const existingKey = localStorage.getItem('clinic_api_key')
  if (!existingKey) {
    await refreshApiKey()
  }

  new Vue({
    router,
    render: h => h(App)
  }).$mount('#app')
}

bootstrap()
