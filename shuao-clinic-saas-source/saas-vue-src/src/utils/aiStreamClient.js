/**
 * AI 流式对话客户端
 * 使用原生 fetch + ReadableStream 读取 SSE 流
 */

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
  const { message, agentKey = 'default', sessionId = '', onToken, onDone, onError } = options
  const session = getAdminSession()
  const accountId = session.id || null
  const accountName = session.name || ''

  const abortController = new AbortController()

  fetch('/business-analysis/chat/stream?agentKey=' + encodeURIComponent(agentKey), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      account_id: accountId,
      account_name: accountName,
      session_id: sessionId,
      message
    }),
    signal: abortController.signal
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('HTTP ' + response.status)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      function readChunk() {
        return reader.read().then(({ done, value }) => {
          if (done) {
            if (typeof onDone === 'function') onDone()
            return
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            const trimmed = line.trim()
            if (trimmed.startsWith('event:')) {
              // 缓存事件名
              buffer = buffer || ''
              // 将当前行和后续data行一起处理
            } else if (trimmed.startsWith('data:')) {
              const data = trimmed.substring(5).trim()
              if (data) {
                if (typeof onToken === 'function') onToken(data)
              }
            } else if (trimmed === '') {
              // SSE 空行分隔符
            }
          }

          return readChunk()
        })
      }

      return readChunk()
    })
    .catch(err => {
      if (err.name === 'AbortError') return
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

/**
 * 获取模型供应商配置
 */
export function fetchModelProviderConfig() {
  return fetch('/api/model-providers').then(r => r.json())
}

/**
 * 保存模型供应商配置
 */
export function saveModelProviderConfig(config) {
  return fetch('/api/model-providers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  }).then(r => r.json())
}
