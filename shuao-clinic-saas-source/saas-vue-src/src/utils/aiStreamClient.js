/**
 * AI 流式对话客户端
 * 使用原生 fetch + ReadableStream 读取 SSE 流
 */

import { isDebugMode } from './aiConfig'

// 内存中的调试日志队列（供前端日志面板读取）
const DEBUG_LOGS = []
const MAX_LOGS = 100
const logListeners = []

function addDebugLog(type, data) {
  const log = { time: new Date().toLocaleTimeString(), type, data }
  DEBUG_LOGS.unshift(log)
  if (DEBUG_LOGS.length > MAX_LOGS) DEBUG_LOGS.pop()
  logListeners.forEach(cb => cb(log))
}

/**
 * 订阅调试日志更新
 * @param {Function} callback - 每次新增日志时调用，参数为 log 对象
 */
export function subscribeToDebugLogs(callback) {
  logListeners.push(callback)
}

/**
 * 取消订阅调试日志
 */
export function unsubscribeFromDebugLogs(callback) {
  const idx = logListeners.indexOf(callback)
  if (idx >= 0) logListeners.splice(idx, 1)
}

/**
 * 获取最近的调试日志
 */
export function getDebugLogs() {
  return DEBUG_LOGS.slice()
}

/**
 * 清空调试日志
 */
export function clearDebugLogs() {
  DEBUG_LOGS.length = 0
  logListeners.forEach(cb => cb(null))
}

function getAdminSession() {
  try {
    const raw = sessionStorage.getItem('adminSession')
    return raw ? JSON.parse(raw) : {}
  } catch (e) {
    return {}
  }
}

/**
 * 发送流式 AI 对话请求
 * @param {Object} options
 * @param {string} options.message - 用户消息
 * @param {string} [options.agentKey='default'] - Agent 标识
 * @param {string} [options.sessionId=''] - 会话ID
 * @param {Function} options.onToken - 每收到一个 token 回调
 * @param {Function} options.onDone - 流结束回调
 * @param {Function} options.onError - 错误回调
 */
export function streamChat(options) {
  const { message, agentKey = 'default', sessionId = '', functionKey = '', onToken, onDone, onError } = options
  const session = getAdminSession()
  const accountId = session.id || null
  const accountName = session.name || ''
  const debug = isDebugMode()

  const abortController = new AbortController()

  const requestBody = {
    account_id: accountId,
    account_name: accountName,
    session_id: sessionId,
    message,
    functionKey
  }

  if (debug) {
    console.log('[AI Debug] streamChat 请求:', JSON.parse(JSON.stringify(requestBody)))
  }
  addDebugLog('request', { agentKey, functionKey, message, accountId })

  fetch('/api/ai/proxy/' + encodeURIComponent(agentKey), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(requestBody),
    signal: abortController.signal
  })
    .then(response => {
      if (debug) {
        console.log('[AI Debug] streamChat 响应状态:', response.status)
      }
      addDebugLog('response', { status: response.status, ok: response.ok })
      if (!response.ok) {
        addDebugLog('error', { status: response.status, message: 'HTTP ' + response.status })
        throw new Error('HTTP ' + response.status)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let eventDataLines = []

      function flushEvent() {
        if (eventDataLines.length > 0) {
          const data = eventDataLines.join('\n')
          eventDataLines = []
          if (debug) {
            console.log('[AI Debug] streamChat token:', data)
          }
          addDebugLog('token', { data })
          if (typeof onToken === 'function') onToken(data)
        }
      }

      function readChunk() {
        return reader.read().then(({ done, value }) => {
          if (done) {
            flushEvent()
            if (debug) {
              console.log('[AI Debug] streamChat 流结束')
            }
            addDebugLog('done', { message: '流正常结束' })
            if (typeof onDone === 'function') onDone()
            return
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            // 处理 \r\n 换行
            const rawLine = line.endsWith('\r') ? line.slice(0, -1) : line

            if (rawLine.startsWith(':')) {
              // SSE 注释行，忽略
              continue
            }
            if (rawLine.startsWith('event:')) {
              if (debug) {
                console.log('[AI Debug] SSE event line:', rawLine.trim())
              }
            } else if (rawLine.startsWith('data:')) {
              // SSE 规范：data: 后的可选空格属于值的一部分
              const data = rawLine.length > 6 && rawLine.charAt(5) === ' ' ? rawLine.substring(6) : rawLine.substring(5)
              if (debug) {
                console.log('[AI Debug] SSE data line:', data)
              }
              eventDataLines.push(data)
            } else if (rawLine === '') {
              // 空行表示一个 SSE event 结束，刷新累积的数据行
              flushEvent()
            } else {
              if (debug) {
                console.log('[AI Debug] SSE unknown line:', rawLine.trim())
              }
            }
          }

          return readChunk()
        })
      }

      return readChunk()
    })
    .catch(err => {
      if (err.name === 'AbortError') {
        addDebugLog('abort', { message: '用户取消' })
        return
      }
      if (debug) {
        console.error('[AI Debug] streamChat 错误:', err)
      }
      addDebugLog('error', { message: err.message || '请求失败' })
      if (typeof onError === 'function') onError(err.message || '请求失败')
    })

  return {
    abort: () => abortController.abort()
  }
}

/**
 * 获取 AI Agent 配置列表
 * @param {number|null} accountId
 */
export function fetchAgentConfigs(accountId) {
  const url = accountId
    ? '/ai-agent-configs?accountId=' + accountId
    : '/ai-agent-configs'
  return fetch(url).then(r => r.json())
}

/**
 * 保存 AI Agent 配置
 */
export function saveAgentConfig(config) {
  return fetch('/ai-agent-configs', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  }).then(r => r.json())
}

/**
 * 更新 AI Agent 配置
 */
export function updateAgentConfig(id, config) {
  return fetch('/ai-agent-configs/' + id, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  }).then(r => r.json())
}

/**
 * 删除 AI Agent 配置
 */
export function deleteAgentConfig(id) {
  return fetch('/ai-agent-configs/' + id, {
    method: 'DELETE'
  }).then(r => r.json())
}
