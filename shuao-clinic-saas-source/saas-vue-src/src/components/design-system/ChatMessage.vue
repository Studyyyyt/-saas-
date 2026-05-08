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
      <div class="chat-message__body" v-html="renderedContent" />
      <div v-if="isStreaming" class="chat-message__typing">
        <span class="typing-cursor" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatMessage',
  props: {
    role: { type: String, required: true, validator: v => ['user', 'assistant'].includes(v) },
    content: { type: String, default: '' },
    time: { type: String, default: '' },
    model: { type: String, default: '' },
    isStreaming: { type: Boolean, default: false }
  },
  computed: {
    avatarStyle() {
      if (this.role === 'user') {
        return { background: 'linear-gradient(135deg, #2563eb, #60a5fa)' }
      }
      return { background: 'linear-gradient(135deg, #10b981, #34d399)' }
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
  color: #2563eb;
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
  color: #2563eb;
  animation: blink 1s step-end infinite;
  font-weight: 300;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@media (max-width: 768px) {
  .chat-message__bubble {
    max-width: 85%;
  }
}
</style>
