<template>
  <div class="signature-pad">
    <canvas
      ref="canvas"
      class="signature-canvas"
      @mousedown="handlePointerDown"
      @mousemove="handlePointerMove"
      @mouseup="handlePointerUp"
      @mouseleave="handlePointerUp"
      @touchstart.prevent="handleTouchStart"
      @touchmove.prevent="handleTouchMove"
      @touchend.prevent="handlePointerUp"
    ></canvas>
  </div>
</template>

<script>
export default {
  name: 'SignaturePad',
  props: {
    height: {
      type: Number,
      default: 180
    },
    lineWidth: {
      type: Number,
      default: 2.4
    },
    strokeStyle: {
      type: String,
      default: '#111827'
    }
  },
  data() {
    return {
      drawing: false,
      hasContent: false
    }
  },
  mounted() {
    this.resetCanvas()
    window.addEventListener('resize', this.resetCanvas)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resetCanvas)
  },
  methods: {
    getCanvas() {
      return this.$refs.canvas
    },
    getContext() {
      const canvas = this.getCanvas()
      return canvas ? canvas.getContext('2d') : null
    },
    resetCanvas() {
      this.$nextTick(() => {
        const canvas = this.getCanvas()
        if (!canvas) return
        const parentWidth = canvas.parentElement ? canvas.parentElement.clientWidth : 0
        const width = Math.max(280, parentWidth || 280)
        const dpr = window.devicePixelRatio || 1
        canvas.width = Math.floor(width * dpr)
        canvas.height = Math.floor(this.height * dpr)
        canvas.style.width = `${width}px`
        canvas.style.height = `${this.height}px`

        const ctx = canvas.getContext('2d')
        ctx.setTransform(1, 0, 0, 1, 0, 0)
        ctx.scale(dpr, dpr)
        ctx.clearRect(0, 0, width, this.height)
        ctx.lineCap = 'round'
        ctx.lineJoin = 'round'
        ctx.lineWidth = this.lineWidth
        ctx.strokeStyle = this.strokeStyle
        this.drawing = false
        this.hasContent = false
      })
    },
    clear() {
      const canvas = this.getCanvas()
      const ctx = this.getContext()
      if (!canvas || !ctx) return
      ctx.clearRect(0, 0, canvas.clientWidth || 0, this.height)
      this.hasContent = false
      this.drawing = false
    },
    hasStroke() {
      return this.hasContent
    },
    exportImage() {
      const canvas = this.getCanvas()
      if (!canvas || !this.hasContent) return ''
      return canvas.toDataURL('image/png')
    },
    pointFromMouse(event) {
      const canvas = this.getCanvas()
      if (!canvas) return null
      const rect = canvas.getBoundingClientRect()
      return {
        x: event.clientX - rect.left,
        y: event.clientY - rect.top
      }
    },
    pointFromTouch(event) {
      const touch = event.touches && event.touches[0]
      if (!touch) return null
      return this.pointFromMouse(touch)
    },
    beginStroke(point) {
      const ctx = this.getContext()
      if (!ctx || !point) return
      ctx.beginPath()
      ctx.moveTo(point.x, point.y)
      this.drawing = true
      this.hasContent = true
    },
    drawStroke(point) {
      const ctx = this.getContext()
      if (!ctx || !point || !this.drawing) return
      ctx.lineTo(point.x, point.y)
      ctx.stroke()
    },
    handlePointerDown(event) {
      this.beginStroke(this.pointFromMouse(event))
    },
    handlePointerMove(event) {
      this.drawStroke(this.pointFromMouse(event))
    },
    handlePointerUp() {
      this.drawing = false
    },
    handleTouchStart(event) {
      this.beginStroke(this.pointFromTouch(event))
    },
    handleTouchMove(event) {
      this.drawStroke(this.pointFromTouch(event))
    }
  }
}
</script>

<style scoped>
.signature-pad {
  width: 100%;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  background: #fff;
  overflow: hidden;
}

.signature-canvas {
  display: block;
  width: 100%;
  touch-action: none;
  background:
    linear-gradient(transparent 31px, rgba(148, 163, 184, 0.18) 32px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 100% 32px, 24px 100%;
}
</style>
