<template>
  <div class="register-page apple-design-scope">
    <!-- 流动渐变背景 -->
    <div class="bg-gradient-flow"></div>
    <!-- 浮动粒子层 -->
    <div class="particles">
      <div
        class="particle"
        v-for="n in 30"
        :key="n"
        :style="particleStyle(n)"
      ></div>
    </div>
    <!-- 几何装饰层 -->
    <div class="geo-decor geo-1"></div>
    <div class="geo-decor geo-2"></div>
    <div class="geo-decor geo-3"></div>
    <div class="geo-decor geo-4"></div>
    <div class="geo-line line-1"></div>
    <div class="geo-line line-2"></div>
    <!-- 水墨晕染装饰 -->
    <div class="ink-wash ink-1"></div>
    <div class="ink-wash ink-2"></div>

    <div class="page">
      <div class="card">
        <div class="brand">
          <div class="logo-ring">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2C7.5 2 4 6.5 4 10c0 3.5 3 7 8 12 5-5 8-8.5 8-12 0-3.5-3.5-8-8-8z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <h1 class="brand-title">一隐口腔</h1>
          <div class="divider-line"></div>
          <p class="brand-subtitle">诊所管理系统</p>
        </div>

        <el-form :model="user" class="register-form" :rules="rules" ref="registerRef">
          <el-form-item prop="username" class="input-animate">
            <el-input
              prefix-icon="el-icon-user"
              size="medium"
              placeholder="请输入账号"
              v-model="user.username"
            />
          </el-form-item>

          <el-form-item prop="password" class="input-animate">
            <el-input
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              placeholder="请输入密码"
              v-model="user.password"
            />
          </el-form-item>

          <el-form-item prop="confirmPass" class="input-animate">
            <el-input
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              placeholder="请确认密码"
              v-model="user.confirmPass"
              @keyup.enter.native="register"
            />
          </el-form-item>

          <el-form-item class="btn-animate">
            <el-button
              type="primary"
              class="register-btn"
              @click="register"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <div class="footer-link">
            已经有账号了？请 <span class="login-link" @click="$router.push('/login1')">登录</span>
          </div>
          <div class="footer-icp">湘ICP备2026011054号 · 一隐口腔诊所</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Register",
  data() {
    // 密码一致性校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.user.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      user: {
        username: '',
        password: '',
        confirmPass: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ],
      }
    }
  },
  methods: {
    particleStyle(n) {
      const seeded = (seed) => {
        const x = Math.sin(seed * 9999) * 10000;
        return x - Math.floor(x);
      };
      const top = seeded(n * 7) * 100;
      const left = seeded(n * 13) * 100;
      const delay = seeded(n * 3) * 12;
      const duration = 10 + seeded(n * 5) * 12;
      const size = 2 + seeded(n * 11) * 5;
      const opacity = 0.30 + seeded(n * 17) * 0.50;
      return {
        top: `${top}%`,
        left: `${left}%`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`,
        width: `${size}px`,
        height: `${size}px`,
        opacity: opacity.toFixed(2)
      };
    },
    register() {
      this.$refs['registerRef'].validate((valid) => {
        if (valid) {
          // 注册复用账号新增接口（后端暂无独立注册接口）
          const payload = {
            username: this.user.username,
            password: this.user.password,
            name: this.user.username,
            role: 'user'
          }
          this.$request.post('/accounts/add', payload).then(res => {
            if (res.code === '200' || res.code === 200) {
              this.$router.push('/login1')
              this.$message.success('注册成功')
            } else {
              this.$message.error(res.msg || '注册失败')
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--apple-bg-primary);
  padding: 24px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

/* === 流动渐变背景 === */
.bg-gradient-flow {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 80% 60% at 20% 40%, rgba(90, 143, 123, 0.18), transparent 55%),
    radial-gradient(ellipse 60% 80% at 80% 20%, rgba(90, 143, 123, 0.14), transparent 55%),
    radial-gradient(ellipse 70% 70% at 50% 80%, rgba(120, 100, 80, 0.10), transparent 55%),
    radial-gradient(ellipse 50% 50% at 30% 10%, rgba(90, 143, 123, 0.12), transparent 45%);
  background-size: 200% 200%;
  animation: gradientFlow 20s ease-in-out infinite alternate;
}
@keyframes gradientFlow {
  0%   { background-position: 0% 0%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 100%; }
}

/* === 浮动粒子层 === */
.particles {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.particle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(90, 143, 123, 0.85), rgba(90, 143, 123, 0.15));
  filter: blur(1px);
  animation: particleFloat linear infinite;
}
@keyframes particleFloat {
  0%   { transform: translateY(0) translateX(0) scale(1); }
  25%  { transform: translateY(-30px) translateX(15px) scale(1.2); }
  50%  { transform: translateY(-15px) translateX(-10px) scale(0.9); }
  75%  { transform: translateY(-40px) translateX(5px) scale(1.1); }
  100% { transform: translateY(0) translateX(0) scale(1); }
}

/* === 几何装饰层 === */
.geo-decor {
  position: fixed;
  pointer-events: none;
  z-index: 0;
  border-radius: 50%;
  border: 1.5px solid rgba(90, 143, 123, 0.22);
}
.geo-1 {
  width: 500px;
  height: 500px;
  top: -15%;
  right: -10%;
  animation: geoRotate 30s linear infinite;
}
.geo-2 {
  width: 350px;
  height: 350px;
  bottom: -10%;
  left: -8%;
  animation: geoRotate 25s linear infinite reverse;
}
.geo-3 {
  width: 180px;
  height: 180px;
  top: 40%;
  right: 15%;
  border-color: rgba(90, 143, 123, 0.15);
  animation: geoRotate 20s linear infinite;
}
.geo-4 {
  width: 120px;
  height: 120px;
  bottom: 25%;
  right: 25%;
  border-color: rgba(90, 143, 123, 0.12);
  animation: geoRotate 18s linear infinite reverse;
}
@keyframes geoRotate {
  0%   { transform: rotate(0deg) scale(1); }
  50%  { transform: rotate(180deg) scale(1.05); }
  100% { transform: rotate(360deg) scale(1); }
}

.geo-line {
  position: fixed;
  pointer-events: none;
  z-index: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(90, 143, 123, 0.22), transparent);
}
.line-1 {
  width: 60%;
  top: 30%;
  left: -10%;
  animation: lineSlide 15s ease-in-out infinite;
}
.line-2 {
  width: 50%;
  bottom: 35%;
  right: -10%;
  animation: lineSlide 18s ease-in-out infinite reverse;
}
@keyframes lineSlide {
  0%, 100% { transform: translateX(0) scaleX(1); opacity: 0.5; }
  50%      { transform: translateX(80px) scaleX(1.3); opacity: 1; }
}

/* === 水墨晕染装饰 === */
.ink-wash {
  position: fixed;
  pointer-events: none;
  z-index: 0;
  border-radius: 50%;
  filter: blur(60px);
}
.ink-1 {
  width: 400px;
  height: 400px;
  background: rgba(90, 143, 123, 0.18);
  top: -8%;
  right: -10%;
  animation: inkFloat 18s ease-in-out infinite;
}
.ink-2 {
  width: 320px;
  height: 320px;
  background: rgba(120, 100, 80, 0.10);
  bottom: 2%;
  left: -8%;
  animation: inkFloat 22s ease-in-out infinite reverse;
}

@keyframes inkFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-15px, 10px) scale(1.08); }
}

/* === 页面容器 === */
.page {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
}

/* === 卡片 === */
.card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(90, 143, 123, 0.15);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.04), 0 2px 8px rgba(0, 0, 0, 0.02);
  padding: 52px 44px 36px;
  animation: cardEnter 0.9s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes cardEnter {
  to { opacity: 1; transform: translateY(0); }
}

/* === 品牌区 === */
.brand {
  text-align: center;
  margin-bottom: 36px;
  animation: fadeInUp 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.2s forwards;
  opacity: 0;
  transform: translateY(20px);
}

.logo-ring {
  width: 72px;
  height: 72px;
  margin: 0 auto 18px;
  border-radius: 50%;
  border: 2px solid rgba(90, 143, 123, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: #5A8F7B;
}
.logo-ring::before {
  content: '';
  position: absolute;
  inset: 6px;
  border-radius: 50%;
  border: 1px solid rgba(90, 143, 123, 0.15);
}

.brand-title {
  font-family: var(--apple-font-serif);
  font-size: 26px;
  font-weight: 600;
  color: #2C3E35;
  letter-spacing: 0.12em;
}

.divider-line {
  width: 32px;
  height: 2px;
  background: #5A8F7B;
  margin: 14px auto 10px;
  border-radius: 1px;
  animation: lineGrow 0.6s cubic-bezier(0.22, 1, 0.36, 1) 0.5s forwards;
  transform: scaleX(0);
}

@keyframes lineGrow {
  to { transform: scaleX(1); }
}

.brand-subtitle {
  font-size: 12px;
  color: #8A9A8E;
  letter-spacing: 0.15em;
}

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

/* === 表单 === */
.register-form {
  animation: fadeIn 0.4s ease 0.3s forwards;
  opacity: 0;
}

@keyframes fadeIn {
  to { opacity: 1; }
}

/* 细线输入框（东方风格） */
.register-form >>> .el-input__inner {
  width: 100%;
  height: 44px;
  padding: 8px 4px 8px 36px;
  font-size: 15px;
  color: #2C3E35;
  background: transparent;
  border: none;
  border-bottom: 1.5px solid rgba(90, 143, 123, 0.2);
  border-radius: 0;
  outline: none;
  font-family: var(--apple-font-sans);
  transition: border-color 0.3s ease;
}

.register-form >>> .el-input__inner:focus {
  background: transparent;
  border-bottom-color: #5A8F7B;
  box-shadow: none;
}

.register-form >>> .el-input__prefix {
  left: 2px;
  color: #A0A0A0;
  transition: color 0.3s ease;
}

.register-form >>> .el-input__inner:focus + .el-input__prefix,
.register-form >>> .el-input__inner:focus ~ .el-input__prefix {
  color: #5A8F7B;
}

/* === 输入框入场动画 === */
.input-animate:nth-child(1) {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.4s forwards;
  opacity: 0;
  transform: translateY(12px);
}
.input-animate:nth-child(2) {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.5s forwards;
  opacity: 0;
  transform: translateY(12px);
}
.input-animate:nth-child(3) {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.6s forwards;
  opacity: 0;
  transform: translateY(12px);
}

/* === 注册按钮 === */
.register-btn {
  width: 100%;
  height: 46px;
  background: #5A8F7B;
  color: #FFFFFF;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  font-family: var(--apple-font-sans);
  cursor: pointer;
  letter-spacing: 0.1em;
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
  position: relative;
  overflow: hidden;
  margin-top: 8px;
}

.register-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.register-btn:hover {
  background: #4A7F6B;
  box-shadow: 0 4px 16px rgba(90, 143, 123, 0.25);
}

.register-btn:hover::after {
  transform: translateX(100%);
}

.register-btn:active {
  transform: scale(0.98);
}

/* === 按钮入场动画 === */
.btn-animate {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.7s forwards;
  opacity: 0;
  transform: translateY(12px);
}

/* === Footer === */
.register-footer {
  margin-top: 24px;
  text-align: center;
  animation: fadeIn 0.4s ease 0.9s forwards;
  opacity: 0;
}

.footer-link {
  font-size: 13px;
  color: #6B6B6B;
  margin-bottom: 12px;
}

.login-link {
  color: #5A8F7B;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}
.login-link:hover {
  color: #4A7F6B;
  text-decoration: underline;
}

.footer-icp {
  font-size: 11px;
  color: #A0A0A0;
  letter-spacing: 0.05em;
}

/* === 移动端适配 === */
@media (max-width: 480px) {
  .card {
    padding: 40px 28px 28px;
  }
  .brand-title {
    font-size: 22px;
  }
}
</style>
