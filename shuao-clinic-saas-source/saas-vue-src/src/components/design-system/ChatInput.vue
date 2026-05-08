<template>
  <div class="chat-input">
    <div v-if="suggestions.length" class="chat-input__suggestions">
      <span
        v-for="(item, index) in suggestions"
        :key="index"
        class="chat-input__suggestion-chip"
        @click="handleSuggestionClick(item)"
      >
        {{ item }}
      </span>
    </div>
    <div class="chat-input__box">
      <el-input
        v-model="inputValue"
        type="textarea"
        :rows="3"
        resize="none"
        :placeholder="placeholder"
        @keydown.enter.native="handleKeydown"
      />
      <div class="chat-input__actions">
        <span class="chat-input__tip">Enter 发送，Shift + Enter 换行</span>
        <el-button
          type="primary"
          :disabled="!inputValue.trim() || sending"
          :loading="sending"
          @click="handleSend"
        >
          <i class="el-icon-s-promotion" />
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    value: { type: String, default: '' },
    placeholder: { type: String, default: '请输入您的问题...' },
    sending: { type: Boolean, default: false },
    suggestions: { type: Array, default: () => [] }
  },
  computed: {
    inputValue: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  methods: {
    handleKeydown(e) {
      if (!e.shiftKey && e.key === 'Enter') {
        e.preventDefault()
        this.handleSend()
      }
    },
    handleSend() {
      const text = this.inputValue.trim()
      if (!text || this.sending) return
      this.$emit('send', text)
    },
    handleSuggestionClick(text) {
      this.$emit('send', text)
    }
  }
}
</script>

<style scoped>
.chat-input {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-input__suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 4px;
}

.chat-input__suggestion-chip {
  padding: 6px 14px;
  background: #f1f5f9;
  border-radius: 999px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.chat-input__suggestion-chip:hover {
  background: #eff6ff;
  color: #2563eb;
  border-color: rgba(37, 99, 235, 0.18);
}

.chat-input__box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 12px 16px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.chat-input__box:focus-within {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1), 0 4px 12px rgba(15, 23, 42, 0.06);
}

.chat-input__box ::v-deep .el-textarea__inner {
  border: none;
  padding: 0;
  background: transparent;
  font-size: 15px;
  resize: none;
}

.chat-input__box ::v-deep .el-textarea__inner:focus {
  box-shadow: none;
}

.chat-input__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.chat-input__tip {
  font-size: 12px;
  color: #94a3b8;
}

.chat-input__actions .el-button {
  border-radius: 12px;
  padding: 10px 20px;
}
</style>
