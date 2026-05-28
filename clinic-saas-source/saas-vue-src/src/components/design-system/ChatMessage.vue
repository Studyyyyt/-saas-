<template>
  <div class="chat-message" :class="`chat-message--${role}`">
    <div class="chat-message__avatar">
      <el-avatar :size="36" :icon="role === 'user' ? 'el-icon-user' : 'el-icon-cpu'" :style="avatarStyle" />
    </div>
    <div class="chat-message__bubble">
      <div class="chat-message__meta">
        <span class="chat-message__name">{{ role === 'user' ? '你' : 'AI 助手' }}</span>
        <span v-if="time" class="chat-message__time">{{ time }}</span>
        <span v-if="model" class="chat-message__model">{{ model }}</span>
      </div>

      <!-- 结构化内容渲染 -->
      <div v-if="hasStructuredPayload" class="chat-message__structured">
        <AiResponseCard
          :display-type="effectiveDisplayType"
          :content="content"
          :payload="payload"
        />
      </div>
      <!-- 普通文本渲染 -->
      <div v-else class="chat-message__body" v-html="renderedContent" />

      <div v-if="isStreaming" class="chat-message__typing">
        <span class="typing-cursor" />
      </div>

      <!-- 操作栏（仅 AI 消息且非流式状态） -->
      <div v-if="role === 'assistant' && !isStreaming && showActions" class="chat-message__actions">
        <span class="action-btn" title="复制内容" @click="handleCopy">
          <i class="el-icon-document-copy" />
          <span>复制</span>
        </span>
        <span class="action-btn" title="重新生成" @click="handleRegenerate">
          <i class="el-icon-refresh" />
          <span>重新生成</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import AiResponseCard from '@/components/ai/AiResponseCard.vue'

export default {
  name: 'ChatMessage',
  components: { AiResponseCard },
  props: {
    role: { type: String, required: true, validator: v => ['user', 'assistant'].includes(v) },
    content: { type: String, default: '' },
    time: { type: String, default: '' },
    model: { type: String, default: '' },
    isStreaming: { type: Boolean, default: false },
    payload: { type: [Object, Array], default: null },
    displayType: { type: String, default: 'text' },
    showActions: { type: Boolean, default: true }
  },
  computed: {
    avatarStyle() {
      if (this.role === 'user') {
        return { background: 'linear-gradient(135deg, #5A8F7B, #6BA08C)' }
      }
      return { background: 'linear-gradient(135deg, #10b981, #34d399)' }
    },
    effectiveDisplayType() {
      if (this.displayType && this.displayType !== 'text') return this.displayType
      if (this.payload != null && typeof this.payload === 'object') {
        if (Array.isArray(this.payload) && this.payload.length > 0) {
          const first = this.payload[0]
          if (first && typeof first === 'object' && !Array.isArray(first)) {
            const keys = Object.keys(first)
            if (keys.length >= 2 && keys.some(k => typeof first[k] === 'number')) {
              return 'table'
            }
          }
        }
        if (this.payload.chartType || this.payload.categories || this.payload.series) {
          return 'chart'
        }
        if (this.payload.groups || (this.payload.metrics && Array.isArray(this.payload.metrics))) {
          return 'comparison'
        }
      }
      return 'text'
    },
    hasStructuredPayload() {
      return this.effectiveDisplayType !== 'text'
    },
    renderedContent() {
      // 简单的 Markdown 渲染：粗体、列表、代码块
      let html = this.escapeHtml(this.content)
      html = html
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/`(.+?)`/g, '<code>$1</code>')
        .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/(<li>.+<\/li>\n?)+/g, '<ul>$&</ul>')
        .replace(/\n/g, '<br>')
      return html
    }
  },
  methods: {
    escapeHtml(text) {
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },
    handleCopy() {
      if (!this.content) return
      const textArea = document.createElement('textarea')
      textArea.value = this.content
      document.body.appendChild(textArea)
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
      this.$message?.success?.('已复制到剪贴板') || alert('已复制到剪贴板')
    },
    handleRegenerate() {
      this.$emit('regenerate')
    }
  }
}
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.chat-message--user {
  flex-direction: row-reverse;
}

.chat-message__avatar {
  flex-shrink: 0;
}

.chat-message__bubble {
  max-width: 70%;
  padding: 14px 18px;
  border-radius: 16px;
  position: relative;
}

.chat-message--user .chat-message__bubble {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chat-message--assistant .chat-message__bubble {
  background: #f8fafc;
  border-radius: 16px 16px 16px 4px;
}

.chat-message__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.chat-message__name {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.chat-message__time,
.chat-message__model {
  font-size: 11px;
  color: #94a3b8;
}

.chat-message__body {
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
  word-break: break-word;
}

.chat-message__body ::v-deep strong {
  color: #0f172a;
  font-weight: 600;
}

.chat-message__body ::v-deep code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #5A8F7B;
  font-family: 'SF Mono', Monaco, monospace;
}

.chat-message__body ::v-deep pre {
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.chat-message__body ::v-deep pre code {
  background: none;
  padding: 0;
}

.chat-message__body ::v-deep ul {
  margin: 8px 0;
  padding-left: 20px;
}

.chat-message__body ::v-deep li {
  margin: 4px 0;
}

.chat-message__typing {
  margin-top: 4px;
}

.typing-cursor::after {
  content: '|';
  display: inline-block;
  color: #5A8F7B;
  animation: blink 1s step-end infinite;
  font-weight: 300;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 结构化内容区域 */
.chat-message__structured {
  margin-top: 4px;
}

/* 操作栏 */
.chat-message__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(90, 143, 123, 0.08);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s ease;
  user-select: none;
}

.action-btn:hover {
  color: #5A8F7B;
}

.action-btn i {
  font-size: 13px;
}

@media (max-width: 768px) {
  .chat-message__bubble {
    max-width: 85%;
  }
}
</style>
