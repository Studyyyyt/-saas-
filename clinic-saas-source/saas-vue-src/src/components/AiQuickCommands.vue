<template>
  <div class="ai-quick-commands">
    <div
      v-for="(cmd, index) in commands"
      :key="index"
      class="quick-capsule"
      :style="{ animationDelay: `${index * 0.06}s` }"
      @click="handleClick(cmd)"
    >
      <i :class="cmd.icon || 'el-icon-chat-dot-round'" />
      <span>{{ cmd.label }}</span>
    </div>
  </div>
</template>

<script>
/**
 * AI 快捷指令胶囊组件
 * 用于输入框上方浮动展示快捷指令，点击直接发送
 */
export default {
  name: 'AiQuickCommands',
  props: {
    commands: {
      type: Array,
      default: () => [
        { label: '今日预约', icon: 'el-icon-date', value: '今日预约情况如何？' },
        { label: '待写病历', icon: 'el-icon-document', value: '我有哪些待写病历？' },
        { label: '本月收入', icon: 'el-icon-coin', value: '本月收入情况怎么样？' }
      ]
    }
  },
  methods: {
    handleClick(cmd) {
      this.$emit('send', cmd.value || cmd.label)
    }
  }
}
</script>

<style scoped>
.ai-quick-commands {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px 0;
  animation: fade-in-down 0.3s ease forwards;
}

.quick-capsule {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 999px;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  animation: capsule-pop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  user-select: none;
}

.quick-capsule:hover {
  background: #ffffff;
  color: #0071e3;
  border-color: rgba(0, 113, 227, 0.2);
  box-shadow: 0 4px 14px rgba(0, 113, 227, 0.12);
  transform: translateY(-2px) scale(1.03);
}

.quick-capsule:active {
  transform: translateY(0) scale(0.98);
}

.quick-capsule i {
  font-size: 13px;
}

@keyframes fade-in-down {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes capsule-pop {
  from { opacity: 0; transform: scale(0.8) translateY(4px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>
