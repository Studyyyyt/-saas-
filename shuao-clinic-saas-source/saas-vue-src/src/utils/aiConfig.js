import Vue from 'vue'
import axios from 'axios'

// 使用 Vue.observable 创建响应式全局状态（Vue 2.6+）
const aiState = Vue.observable({
  globalEnabled: true,
  debugMode: false,
  functions: {},
  loaded: false,
  loading: false
})

const CACHE_KEY = '_ai_config_cache'
const CACHE_TTL = 5 * 60 * 1000 // 缓存 5 分钟

/**
 * 加载 AI 全局配置（优先使用内存，其次 localStorage，最后请求接口）
 */
export async function loadAiConfig(force = false) {
  if (aiState.loading) return
  if (aiState.loaded && !force) return

  // 尝试从 localStorage 读取缓存（减少首次页面闪烁）
  if (!force) {
    try {
      const cached = localStorage.getItem(CACHE_KEY)
      if (cached) {
        const { data, time } = JSON.parse(cached)
        if (Date.now() - time < CACHE_TTL) {
          applyConfig(data)
        }
      }
    } catch (e) { /* 忽略缓存错误 */ }
  }

  aiState.loading = true
  try {
    const [overviewRes, functionsRes] = await Promise.all([
      axios.get('/api/ai-config/overview').catch(() => null),
      axios.get('/api/ai-config/functions').catch(() => null)
    ])

    const overviewData = overviewRes?.data?.code === '200' ? overviewRes.data.data : null
    const functionsData = functionsRes?.data?.code === '200' ? functionsRes.data.data : null

    const config = {
      globalEnabled: overviewData ? overviewData.globalEnabled !== false : true,
      debugMode: overviewData ? overviewData.debugMode === true : false,
      functions: {}
    }

    if (Array.isArray(functionsData)) {
      functionsData.forEach(item => {
        if (item.functionKey) {
          config.functions[item.functionKey] = item.isEnabled === true
        }
      })
    }

    applyConfig(config)

    // 写入本地缓存
    try {
      localStorage.setItem(CACHE_KEY, JSON.stringify({ data: config, time: Date.now() }))
    } catch (e) { /* 忽略写入错误 */ }
  } catch (e) {
    console.error('加载 AI 配置失败', e)
  } finally {
    aiState.loading = false
  }
}

function applyConfig(config) {
  aiState.globalEnabled = config.globalEnabled !== false
  aiState.debugMode = config.debugMode === true
  aiState.functions = { ...config.functions }
  aiState.loaded = true
}

/**
 * 判断全局 AI 是否开启
 */
export function isGlobalEnabled() {
  return aiState.globalEnabled
}

/**
 * 功能依赖关系：某些前端功能实际调用的是共享后端服务
 * 当依赖服务关闭时，前端功能也应被视为不可用
 * 注：后端已支持根据请求中的 functionKey 独立校验开关，因此共享服务的依赖关系已移除
 */
const FUNCTION_DEPENDENCIES = {
}

/**
 * 判断指定 AI 功能是否可用（全局开启 + 自身功能开启 + 依赖功能开启）
 * @param {string} functionKey - 功能标识，如 'medical-expand', 'home-assistant'
 */
export function isAiEnabled(functionKey) {
  if (!aiState.globalEnabled) return false
  if (!functionKey) return true
  // 未加载完成时，默认允许展示（避免页面闪烁后突然消失）
  if (!aiState.loaded) return true

  // 检查自身开关
  if (aiState.functions[functionKey] === false) return false

  // 检查依赖开关（如 home-assistant 依赖 business-analysis）
  const deps = FUNCTION_DEPENDENCIES[functionKey] || []
  for (const dep of deps) {
    if (aiState.functions[dep] === false) return false
  }

  return true
}

/**
 * 获取调试模式状态
 */
export function isDebugMode() {
  return aiState.debugMode
}

/**
 * 获取原始响应式状态（供 Vue 组件 computed 使用）
 */
export function getAiState() {
  return aiState
}

/**
 * 手动更新全局状态（供 AI 总览页开关切换后实时同步）
 * @param {Object} patch - 要更新的字段
 */
export function updateAiState(patch) {
  if (typeof patch.globalEnabled === 'boolean') {
    aiState.globalEnabled = patch.globalEnabled
  }
  if (typeof patch.debugMode === 'boolean') {
    aiState.debugMode = patch.debugMode
  }
  if (patch.functions && typeof patch.functions === 'object') {
    Object.keys(patch.functions).forEach(key => {
      aiState.functions[key] = patch.functions[key]
    })
  }
}

/**
 * 清除本地缓存并重新加载
 */
export async function reloadAiConfig() {
  localStorage.removeItem(CACHE_KEY)
  await loadAiConfig(true)
}
