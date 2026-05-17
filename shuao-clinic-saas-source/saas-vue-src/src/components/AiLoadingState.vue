<template>
  <div class="ai-loading-state" :class="`state--${state}`">
    <!-- analyzing: 脑电波动画 -->
    <template v-if="state === 'analyzing'">
      <div class="brain-wave-wrap">
        <svg viewBox="0 0 120 40" class="brain-wave-svg">
          <path class="brain-wave-path" d="M0,20 Q10,5 20,20 T40,20 T60,20 T80,20 T100,20 T120,20" />
          <path class="brain-wave-path brain-wave-path--delay" d="M0,20 Q10,35 20,20 T40,20 T60,20 T80,20 T100,20 T120,20" />
        </svg>
      </div>
      <div class="state-label">{{ label || 'AI 正在分析...' }}</div>
    </template>

    <!-- writing: 逐行文字生长动画 -->
    <template v-else-if="state === 'writing'">
      <div class="writing-lines">
        <div v-for="n in 3" :key="n" class="writing-line" :style="{ animationDelay: `${(n - 1) * 0.25}s` }">
          <div class="writing-line__bar" />
        </div>
      </div>
      <div class="state-label">{{ label || 'AI 正在生成...' }}</div>
    </template>

    <!-- confirming: 对勾闪烁动画 -->
    <template v-else-if="state === 'confirming'">
      <div class="confirm-ring">
        <div class="confirm-check">
          <i class="el-icon-check" />
        </div>
      </div>
      <div class="state-label">{{ label || '生成完成' }}</div>
    </template>

    <!-- 默认 fallback -->
    <template v-else>
      <div class="typing-dots-modern">
        <span /><span /><span />
      </div>
      <div class="state-label">{{ label || '处理中...' }}</div>
    </template>
  </div>
</template>

<script>
/**
 * AI 加载状态统一组件
 * @state analyzing - 脑电波动画
 * @state writing   - 逐行文字生长动画
 * @state confirming- 对勾闪烁动画
 */
export default {
  name: 'AiLoadingState',
  props: {
    state: { type: String, default: 'analyzing', validator: v => ['analyzing', 'writing', 'confirming', 'default'].includes(v) },
    label: { type: String, default: '' }
  }
}
</script>

<style scoped>
.ai-loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
}

.state-label {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

/* ========== analyzing: 脑电波动画 ========== */
.brain-wave-wrap {
  width: 120px;
  height: 40px;
}

.brain-wave-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.brain-wave-path {
  fill: none;
  stroke: #0071e3;
  stroke-width: 2.5;
  stroke-linecap: round;
  stroke-dasharray: 200;
  stroke-dashoffset: 200;
  animation: brain-wave-draw 1.8s ease-in-out infinite;
}

.brain-wave-path--delay {
  stroke: #42a5f5;
  opacity: 0.5;
  animation-delay: 0.3s;
}

@keyframes brain-wave-draw {
  0% { stroke-dashoffset: 200; }
  50% { stroke-dashoffset: 0; }
  100% { stroke-dashoffset: -200; }
}

/* ========== writing: 逐行文字生长动画 ========== */
.writing-lines {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 140px;
}

.writing-line {
  height: 8px;
  background: #f2f2f7;
  border-radius: 4px;
  overflow: hidden;
}

.writing-line__bar {
  height: 100%;
  width: 0%;
  background: linear-gradient(90deg, #0071e3, #42a5f5);
  border-radius: 4px;
  animation: writing-grow 1.2s ease-in-out infinite;
}

@keyframes writing-grow {
  0% { width: 0%; opacity: 0.4; }
  50% { width: 100%; opacity: 1; }
  100% { width: 100%; opacity: 0.4; }
}

/* ========== confirming: 对勾闪烁动画 ========== */
.confirm-ring {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(52, 199, 89, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: confirm-pulse 1.2s ease-in-out infinite;
}

.confirm-check {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #34c759;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
}

@keyframes confirm-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

/* ========== default: 现代圆点波浪 ========== */
.typing-dots-modern {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  height: 24px;
}

.typing-dots-modern span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0071e3, #42a5f5);
  animation: typing-bounce-modern 1.4s infinite ease-in-out both;
}

.typing-dots-modern span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots-modern span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing-bounce-modern {
  0%, 80%, 100% { transform: scale(0.6) translateY(0); opacity: 0.4; }
  40% { transform: scale(1) translateY(-6px); opacity: 1; }
}
</style>
