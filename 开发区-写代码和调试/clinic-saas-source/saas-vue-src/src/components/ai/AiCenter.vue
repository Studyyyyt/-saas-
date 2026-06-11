<template>
  <div class="ai-center">
    <div class="ai-panel">
      <!-- AI 面板头部 -->
      <div class="ai-panel-header">
        <div class="ai-header-title">
          <i class="el-icon-cpu" style="color: #5A8F7B; margin-right: 6px;"></i>
          <span>AI 智能中心</span>
          <el-tag v-if="currentAgent" size="mini" type="primary" style="margin-left: 8px;">
            {{ currentAgent.name }}
          </el-tag>
        </div>
        <div class="ai-header-actions">
          <el-select
            v-model="currentAgentKey"
            size="mini"
            placeholder="选择 AI Agent"
            style="width: 150px; margin-right: 8px;"
          >
            <el-option
              v-for="agent in enabledAgents"
              :key="agent.agentKey"
              :label="agent.name"
              :value="agent.agentKey"
            />
          </el-select>
          <div class="ai-header-action" title="新建会话" @click="createNewSession">
            <i class="el-icon-plus"></i>
          </div>
          <div class="ai-header-action" title="清空当前会话" @click="clearCurrentSession">
            <i class="el-icon-delete"></i>
          </div>
        </div>
      </div>

      <!-- 会话侧边栏 + 消息区（桌面端并排，移动端堆叠） -->
      <div class="ai-main-layout">
        <!-- 会话列表 -->
        <div v-if="sessions.length > 0" class="ai-session-sidebar">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: session.id === activeSessionId }"
            @click="switchSession(session.id)"
          >
            <div class="session-item-title">{{ session.title || '新会话' }}</div>
            <div class="session-item-meta">
              <span>{{ session.messages.length }} 条消息</span>
              <i class="el-icon-close" title="删除会话" @click.stop="deleteSession(session.id)" />
            </div>
          </div>
        </div>

        <!-- 消息展示区 -->
        <div class="ai-messages" ref="aiChatBody">
          <!-- 欢迎消息（无消息时显示） -->
          <div v-if="currentMessages.length === 0" class="ai-welcome">
            <div class="ai-welcome-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="ai-welcome-title">{{ greeting }}，我是 AI 智能助手</div>
            <div class="ai-welcome-desc">我可以帮您分析经营数据、统计回访情况、辅助病历撰写，或分析患者信息</div>
          </div>

          <!-- 消息列表 -->
          <template v-else>
            <ChatMessage
              v-for="(msg, index) in currentMessages"
              :key="index"
              :role="msg.role"
              :content="msg.content"
              :time="msg.time"
              :is-streaming="msg.streaming"
              :payload="msg.payload"
              :display-type="msg.displayType"
              :show-actions="msg.role === 'assistant' && !msg.streaming"
              @regenerate="handleRegenerate(index)"
            />
            <!-- 加载状态 -->
            <AiLoadingState
              v-if="aiLoading && !currentMessages.some(m => m.streaming)"
              state="analyzing"
              label="AI 正在思考..."
            />
          </template>
        </div>
      </div>

      <!-- 快捷指令面板 -->
      <AiQuickPanel :agents="enabledAgents" @select="handleQuickSelect" />

      <!-- 输入区 -->
      <div class="ai-input-area">
        <ChatInput
          v-model="aiInput"
          :sending="aiLoading"
          :suggestions="currentSuggestions"
          placeholder="请输入您的问题，Enter 发送，Shift + Enter 换行..."
          @send="sendMessage"
          @suggestion-click="handleSuggestionClick"
        />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import ChatMessage from '@/components/design-system/ChatMessage.vue'
import ChatInput from '@/components/design-system/ChatInput.vue'
import AiLoadingState from '@/components/AiLoadingState.vue'
import AiQuickPanel from './AiQuickPanel.vue'

const SESSION_STORAGE_KEY = 'ai_sessions_v2'
const ACTIVE_SESSION_KEY = 'ai_active_session'

function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

function formatTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  return pad(now.getHours()) + ':' + pad(now.getMinutes())
}

function getAccountScopeKey() {
  const session = getAdminSession()
  const accountId = session && session.id ? String(session.id) : 'anonymous'
  return `${SESSION_STORAGE_KEY}_${accountId}`
}

function getActiveSessionKey() {
  const session = getAdminSession()
  const accountId = session && session.id ? String(session.id) : 'anonymous'
  return `${ACTIVE_SESSION_KEY}_${accountId}`
}

export default {
  name: 'AiCenter',
  components: {
    ChatMessage,
    ChatInput,
    AiLoadingState,
    AiQuickPanel
  },
  props: {
    user: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      sessions: [],
      activeSessionId: '',
      aiInput: '',
      aiLoading: false,
      currentAgentKey: '',
      agentList: [],
      aiAgentsLoaded: false,
      typingTimer: null,
      typingIndex: -1
    }
  },
  computed: {
    normalizedRole() {
      const role = String((this.user && this.user.role) || '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 9) return '早上好'
      if (hour < 12) return '上午好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    enabledAgents() {
      return this.agentList || []
    },
    currentAgent() {
      return (this.agentList || []).find(a => a.agentKey === this.currentAgentKey) || null
    },
    currentSession() {
      return this.sessions.find(s => s.id === this.activeSessionId) || null
    },
    currentMessages() {
      const session = this.currentSession
      return session ? session.messages : []
    },
    currentSuggestions() {
      const agent = this.currentAgent
      if (agent && Array.isArray(agent.chips) && agent.chips.length > 0) {
        return agent.chips.slice(0, 4)
      }
      return []
    }
  },
  mounted() {
    this.loadSessions()
    this.loadAiAgents()
    window.addEventListener('focus', this.handleWindowFocus)
  },
  beforeDestroy() {
    this.stopTypingEffect()
    this.saveSessions()
    window.removeEventListener('focus', this.handleWindowFocus)
  },
  methods: {
    // ========== 会话管理 ==========
    loadSessions() {
      try {
        const raw = localStorage.getItem(getAccountScopeKey())
        const parsed = raw ? JSON.parse(raw) : []
        if (Array.isArray(parsed) && parsed.length > 0) {
          this.sessions = parsed
        } else {
          this.createNewSession(false)
        }
        const activeRaw = localStorage.getItem(getActiveSessionKey())
        if (activeRaw && this.sessions.some(s => s.id === activeRaw)) {
          this.activeSessionId = activeRaw
        } else if (this.sessions.length > 0) {
          this.activeSessionId = this.sessions[0].id
        }
      } catch (e) {
        this.sessions = []
        this.createNewSession(false)
      }
    },
    saveSessions() {
      try {
        localStorage.setItem(getAccountScopeKey(), JSON.stringify(this.sessions))
        localStorage.setItem(getActiveSessionKey(), this.activeSessionId)
      } catch (e) {
        console.warn('[AiCenter] 保存会话失败:', e)
      }
    },
    createNewSession(save = true) {
      const newSession = {
        id: generateUUID(),
        title: '新会话',
        createdAt: Date.now(),
        messages: []
      }
      this.sessions.unshift(newSession)
      this.activeSessionId = newSession.id
      // 限制会话数量，保留最近 20 个
      if (this.sessions.length > 20) {
        this.sessions = this.sessions.slice(0, 20)
      }
      if (save) this.saveSessions()
    },
    deleteSession(id) {
      const idx = this.sessions.findIndex(s => s.id === id)
      if (idx === -1) return
      this.sessions.splice(idx, 1)
      if (this.activeSessionId === id) {
        if (this.sessions.length > 0) {
          this.activeSessionId = this.sessions[0].id
        } else {
          this.createNewSession(false)
        }
      }
      this.saveSessions()
    },
    switchSession(id) {
      this.activeSessionId = id
      this.saveSessions()
      this.$nextTick(() => this.scrollToBottom())
    },
    clearCurrentSession() {
      const session = this.currentSession
      if (!session) return
      session.messages = []
      session.title = '新会话'
      this.saveSessions()
    },
    updateSessionTitle(text) {
      const session = this.currentSession
      if (!session || session.title !== '新会话') return
      // 使用用户第一条消息的前 12 个字作为标题
      const title = text.slice(0, 12) + (text.length > 12 ? '...' : '')
      session.title = title
    },

    // ========== Agent 加载 ==========
    async loadAiAgents() {
      try {
        const session = getAdminSession()
        const accountId = session && session.id ? session.id : ''
        const res = await axios.get('/api/ai-agent-configs', { params: { accountId } })
        if (res.data && res.data.code === '200' && Array.isArray(res.data.data)) {
          this.agentList = res.data.data
          const firstAgent = this.agentList[0]
          if (firstAgent && firstAgent.agentKey) {
            this.currentAgentKey = firstAgent.agentKey
          }
          this.aiAgentsLoaded = true
          await this.applyFunctionMappingToAgentList(accountId)
        } else {
          this.loadAgentsFromLocalStorage()
        }
      } catch (error) {
        console.warn('[AiCenter] 加载 Agent 列表失败，回退到本地缓存:', error)
        this.loadAgentsFromLocalStorage()
      }
    },
    async applyFunctionMappingToAgentList(accountId) {
      try {
        const res = await axios.get('/api/ai/function-mappings', { params: { accountId } })
        if (res.data && res.data.code === '200' && Array.isArray(res.data.data)) {
          const mappings = res.data.data
          const homeVisibilityMap = new Map()
          for (const m of mappings) {
            if (m.agentKey) {
              homeVisibilityMap.set(m.agentKey, m.isVisibleOnHome === true || m.isVisibleOnHome === 1)
            }
          }
          // 记录哪些 agentKey 来自真实 ai_agent_config 配置
          const realAgentKeys = new Set((this.agentList || []).map(a => a.agentKey))

          // 1. 真实 Agent 的可见性 = 自身 isVisibleOnHome AND（如有功能映射）映射 isVisibleOnHome
          this.agentList = (this.agentList || []).filter(agent => {
            if (!agent.agentKey) return false
            const agentVisible = agent.isVisibleOnHome !== false && agent.isVisibleOnHome !== 0
            if (homeVisibilityMap.has(agent.agentKey)) {
              return agentVisible && homeVisibilityMap.get(agent.agentKey)
            }
            return agentVisible
          })

          // 2. 为没有对应真实 Agent、但系统功能映射中标记为显示的系统功能创建虚拟 Agent
          const existingKeys = new Set((this.agentList || []).map(a => a.agentKey))
          for (const mapping of mappings) {
            const visible = mapping.isVisibleOnHome === true || mapping.isVisibleOnHome === 1
            if (!visible || !mapping.agentKey || existingKeys.has(mapping.agentKey)) continue
            // 若该 agentKey 原本有真实 Agent 但被过滤掉了（如 Agent 配置关了），不再创建虚拟 Agent
            if (realAgentKeys.has(mapping.agentKey)) continue
            this.agentList.push({
              name: mapping.functionName || mapping.agentKey,
              agentKey: mapping.agentKey,
              description: `系统功能：${mapping.functionName}`,
              chips: [],
              gradient: 'linear-gradient(135deg, #64748b 0%, #94a3b8 100%)'
            })
            existingKeys.add(mapping.agentKey)
          }
          // 过滤完成后，若当前选中的 Agent 已不可见，自动切换到第一个可见 Agent
          const visibleKeys = new Set((this.agentList || []).map(a => a.agentKey))
          if (!visibleKeys.has(this.currentAgentKey)) {
            const firstVisible = this.agentList[0]
            if (firstVisible && firstVisible.agentKey) {
              this.currentAgentKey = firstVisible.agentKey
            }
          }
        }
      } catch (error) {
        console.warn('[AiCenter] 应用功能映射过滤失败:', error)
      }
    },
    loadAgentsFromLocalStorage() {
      try {
        const cached = localStorage.getItem('ai_agents_config')
        if (cached) {
          const list = JSON.parse(cached)
          if (Array.isArray(list)) {
            this.agentList = list
            const firstEnabled = list.find(a => a.isEnabled !== false)
            if (firstEnabled && firstEnabled.agentKey) {
              this.currentAgentKey = firstEnabled.agentKey
            }
          }
        }
      } catch (e) {
        console.warn('[AiCenter] 读取本地 Agent 缓存失败:', e)
      }
    },
    /**
     * 页面重新获得焦点时刷新 Agent 列表
     * 以便同步设置页面的修改
     */
    handleWindowFocus() {
      this.loadAiAgents()
    },

    // ========== 消息发送与接收 ==========
    sendMessage(text) {
      const content = text || this.aiInput.trim()
      if (!content || this.aiLoading) return
      // 添加用户消息
      const session = this.currentSession
      if (!session) {
        this.createNewSession()
      }
      this.currentSession.messages.push({
        role: 'user',
        content: content,
        time: formatTime()
      })
      this.updateSessionTitle(content)
      this.aiInput = ''
      this.$nextTick(() => this.scrollToBottom())
      // 调用 AI
      const key = this.currentAgentKey || 'default'
      this.callAi(content, key)
    },
    async callAi(text, agentKey) {
      const self = this
      self.aiLoading = true
      const typingIndex = self.currentSession.messages.length
      // 先添加一个空的 AI 消息占位
      self.currentSession.messages.push({
        role: 'assistant',
        content: '',
        streaming: true,
        time: formatTime(),
        payload: null,
        displayType: 'text'
      })
      self.$nextTick(() => self.scrollToBottom())

      const session = getAdminSession()
      const accountId = session && session.id ? String(session.id) : ''
      const accountName = session && session.name ? String(session.name) : ''
      const key = agentKey || 'default'

      try {
        const res = await axios.post('/api/ai/proxy/' + encodeURIComponent(key), {
          message: text,
          session_id: self.currentSession.id || '',
          account_id: accountId,
          account_name: accountName,
          clinic_id: '1'
        })

        const bizCode = res && res.data && res.data.code
        if (bizCode !== '200' && bizCode !== 200) {
          const errMsg = (res.data && res.data.msg) || 'AI 请求失败，请稍后重试'
          const msg = self.currentSession.messages[typingIndex]
          if (msg) {
            self.$set(msg, 'content', errMsg)
            self.$set(msg, 'streaming', false)
          }
          self.aiLoading = false
          self.saveSessions()
          return
        }

        const resultData = res && res.data && res.data.data
        const replyContent = self.extractReplyContent(resultData)
        const displayType = resultData && resultData.displayType ? resultData.displayType : 'text'
        const payload = resultData && resultData.payload != null ? resultData.payload : null

        const msg = self.currentSession.messages[typingIndex]
        if (msg) {
          self.$set(msg, 'streaming', false)
          self.$set(msg, 'displayType', displayType)
          self.$set(msg, 'payload', payload)
          // 启动打字机效果
          self.startTypingEffect(typingIndex, replyContent)
        }
      } catch (err) {
        const errMsg = err && err.response && err.response.data && err.response.data.msg
          ? err.response.data.msg
          : 'AI 请求失败，请稍后重试'
        const msg = self.currentSession.messages[typingIndex]
        if (msg) {
          self.$set(msg, 'content', errMsg)
          self.$set(msg, 'streaming', false)
        }
        self.aiLoading = false
        self.saveSessions()
      }
    },

    // ========== 打字机效果 ==========
    startTypingEffect(msgIndex, fullText) {
      this.stopTypingEffect()
      const msg = this.currentSession.messages[msgIndex]
      if (!msg) return
      let index = 0
      const total = fullText.length
      // 根据内容长度动态计算打字速度
      const baseDelay = total > 500 ? 8 : total > 200 ? 12 : 18
      const typeNext = () => {
        if (index >= total) {
          this.stopTypingEffect()
          this.aiLoading = false
          this.saveSessions()
          return
        }
        index += 1
        this.$set(msg, 'content', fullText.slice(0, index))
        this.scrollToBottom()
        // 在标点符号处稍微停顿
        const char = fullText[index - 1]
        const pauseChars = ['。', '，', '；', '！', '？', '.', ',', ';', '!', '?', '\n']
        const delay = pauseChars.includes(char) ? baseDelay * 2.5 : baseDelay
        this.typingTimer = setTimeout(typeNext, delay + Math.random() * 5)
      }
      typeNext()
    },
    stopTypingEffect() {
      if (this.typingTimer) {
        clearTimeout(this.typingTimer)
        this.typingTimer = null
      }
    },

    // ========== 内容提取 ==========
    extractReplyContent(data) {
      if (data == null) return '（AI 未返回内容）'
      // 统一响应格式：{ content, displayType, payload, suggested_next }
      if (typeof data === 'object' && data.content != null) {
        return this.formatObjectToText(data.content)
      }
      if (typeof data === 'string') return data
      if (typeof data === 'object') {
        const candidateKeys = ['reply', 'message', 'text', 'answer', 'result', 'output']
        for (const k of candidateKeys) {
          if (data[k] != null) {
            return this.formatObjectToText(data[k])
          }
        }
        return this.formatObjectToText(data)
      }
      return String(data)
    },
    formatObjectToText(obj, indent = 0) {
      if (obj == null) return ''
      if (typeof obj === 'string') return obj
      if (typeof obj === 'number' || typeof obj === 'boolean') return String(obj)
      if (Array.isArray(obj)) {
        if (obj.length === 0) return ''
        return obj
          .map(item => this.formatObjectToText(item, indent))
          .filter(item => item && item.trim())
          .join('\n')
      }
      if (typeof obj === 'object') {
        const prefix = '  '.repeat(indent)
        const lines = []
        for (const [key, value] of Object.entries(obj)) {
          const formattedValue = this.formatObjectToText(value, indent + 1)
          if (!formattedValue || !formattedValue.trim()) continue
          if (formattedValue.includes('\n') && indent === 0) {
            lines.push(`${prefix}${key}：`)
            lines.push(formattedValue)
          } else {
            lines.push(`${prefix}${key}：${formattedValue}`)
          }
        }
        return lines.join('\n')
      }
      return String(obj)
    },

    // ========== 交互处理 ==========
    handleQuickSelect(item) {
      if (!item || !item.agentKey) return
      this.currentAgentKey = item.agentKey
      if (item.value) {
        this.aiInput = item.value
      }
    },
    handleSuggestionClick(text) {
      this.aiInput = text
    },
    handleRegenerate(msgIndex) {
      // 找到该 AI 消息前面对应的用户消息
      const messages = this.currentSession.messages
      let userIndex = -1
      for (let i = msgIndex - 1; i >= 0; i--) {
        if (messages[i].role === 'user') {
          userIndex = i
          break
        }
      }
      if (userIndex === -1) {
        this.$message.warning('未找到对应的问题消息')
        return
      }
      const userMsg = messages[userIndex]
      // 移除该 AI 消息及其之后的所有消息
      messages.splice(msgIndex)
      // 重新发送
      this.callAi(userMsg.content, this.currentAgentKey)
    },
    scrollToBottom() {
      const body = this.$refs.aiChatBody
      if (body) body.scrollTop = body.scrollHeight
    }
  }
}
</script>

<style scoped>
.ai-center {
  max-width: 1200px;
  margin: 0 auto;
}

.ai-panel {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(90, 143, 123, 0.08);
}

/* AI 面板头部 */
.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.ai-header-title {
  display: flex;
  align-items: center;
  font-family: var(--apple-font-sans);
  font-size: 16px;
  font-weight: 600;
  color: #2C3E35;
}

.ai-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-header-action {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #A0A0A0;
  transition: all 0.2s ease;
  background: #FAF9F7;
}

.ai-header-action:hover {
  background: rgba(90, 143, 123, 0.1);
  color: #5A8F7B;
}

/* 主布局：会话侧边栏 + 消息区 */
.ai-main-layout {
  display: flex;
  gap: 16px;
  min-height: 300px;
  max-height: 500px;
  margin-bottom: 16px;
}

/* 会话侧边栏 */
.ai-session-sidebar {
  width: 180px;
  flex-shrink: 0;
  background: #FAF9F7;
  border-radius: 10px;
  padding: 10px;
  overflow-y: auto;
  border: 1px solid rgba(90, 143, 123, 0.08);
}

.session-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 6px;
}

.session-item:hover {
  background: rgba(90, 143, 123, 0.06);
}

.session-item.active {
  background: rgba(90, 143, 123, 0.1);
  border: 1px solid rgba(90, 143, 123, 0.15);
}

.session-item-title {
  font-size: 13px;
  font-weight: 600;
  color: #2C3E35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 11px;
  color: #A0A0A0;
}

.session-item-meta i {
  font-size: 12px;
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.session-item-meta i:hover {
  background: rgba(199, 91, 91, 0.1);
  color: #C75B5B;
}

/* 消息区 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  background: #FAF9F7;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(90, 143, 123, 0.08);
}

/* 欢迎态 */
.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  animation: fade-in-up 0.4s ease forwards;
}

.ai-welcome-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5A8F7B, #6BA08C);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(90, 143, 123, 0.25);
}

.ai-welcome-title {
  font-family: var(--apple-font-serif);
  font-size: 16px;
  font-weight: 600;
  color: #2C3E35;
  margin-bottom: 8px;
}

.ai-welcome-desc {
  font-size: 13px;
  color: #6B6B6B;
  text-align: center;
  max-width: 400px;
}

/* 输入区 */
.ai-input-area {
  flex-shrink: 0;
}

/* 动画 */
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .ai-panel {
    padding: 12px;
  }

  .ai-main-layout {
    flex-direction: column;
    min-height: 200px;
    max-height: none;
  }

  .ai-session-sidebar {
    width: 100%;
    max-height: 120px;
    display: flex;
    gap: 8px;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 8px;
  }

  .session-item {
    flex-shrink: 0;
    width: 140px;
    margin-bottom: 0;
  }

  .ai-messages {
    min-height: 200px;
    max-height: 350px;
    padding: 12px;
  }
}
</style>
