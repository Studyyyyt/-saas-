<template>
  <div class="login-page apple-design-scope">
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
          <h1 class="brand-title">{{ displayClinicName }}</h1>
          <div class="divider-line"></div>
          <p class="brand-subtitle">诊所管理系统</p>
        </div>

        <!-- 系统初始化表单 -->
        <el-form v-if="needsInit" :model="initForm" class="login-form" :rules="initRules" ref="initRef">
          <div class="init-tip">
            <i class="el-icon-info" style="color: #5A8F7B; margin-right: 6px;"></i>
            系统首次启动，请创建超级管理员和默认诊所
          </div>
          <el-form-item prop="username" class="input-animate">
            <el-input
              prefix-icon="el-icon-user"
              size="medium"
              placeholder="管理员账号"
              v-model="initForm.username"
            />
          </el-form-item>

          <el-form-item prop="password" class="input-animate">
            <el-input
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              placeholder="登录密码"
              v-model="initForm.password"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword" class="input-animate">
            <el-input
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              placeholder="确认密码"
              v-model="initForm.confirmPassword"
              @keyup.enter.native="initSystem"
            />
          </el-form-item>

          <el-form-item prop="clinicName" class="input-animate">
            <el-input
              prefix-icon="el-icon-office-building"
              size="medium"
              placeholder="默认诊所名称"
              v-model="initForm.clinicName"
            />
          </el-form-item>

          <el-form-item prop="activationCode" class="input-animate">
            <el-input
              prefix-icon="el-icon-key"
              size="medium"
              placeholder="激活码（从 Easytoac 管理后台获取）"
              v-model="initForm.activationCode"
              @keyup.enter.native="initSystem"
            />
          </el-form-item>

          <el-form-item class="btn-animate">
            <el-button
              type="primary"
              class="login-btn"
              :loading="isLoading"
              @click="initSystem"
            >
              初始化系统
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 登录表单 -->
        <el-form v-else :model="user" class="login-form" :rules="rules" ref="loginRef">
          <el-form-item prop="username" class="input-animate">
            <el-input
              prefix-icon="el-icon-user"
              size="medium"
              placeholder="用户名"
              v-model="user.username"
            />
          </el-form-item>

          <el-form-item prop="password" class="input-animate">
            <el-input
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              placeholder="密码"
              v-model="user.password"
              @keyup.enter.native="login"
            />
          </el-form-item>

          <el-form-item class="btn-animate">
            <el-button
              type="primary"
              class="login-btn"
              :loading="isLoading"
              @click="login"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 授权续期对话框 -->
        <el-dialog
          :visible.sync="renewalDialogVisible"
          :show-close="true"
          :close-on-click-modal="false"
          :append-to-body="true"
          :modal-append-to-body="true"
          width="440px"
          custom-class="renewal-dialog"
        >
          <div class="renewal-header">
            <div class="renewal-status-badge">
              <i class="el-icon-warning"></i>
              <span>授权已过期</span>
            </div>
            <h3 class="renewal-title">续期系统授权</h3>
            <p class="renewal-desc">{{ renewalMessage }}</p>
          </div>
          <div class="renewal-body">
            <div class="renewal-form-label">
              <i class="el-icon-key" style="margin-right:6px"></i>新激活码
            </div>
            <el-form :model="renewalForm" :rules="renewalRules" ref="renewalRef" @submit.native.prevent>
              <el-form-item prop="activationCode">
                <el-input
                  v-model="renewalForm.activationCode"
                  placeholder="请输入从 Easytoac 获取的激活码"
                  prefix-icon="el-icon-key"
                  size="medium"
                  class="renewal-input"
                  @keyup.enter.native="renewLicense"
                />
              </el-form-item>
            </el-form>
            <div class="renewal-help">
              <i class="el-icon-info" style="margin-right:4px;font-size:12px"></i>
              联系系统管理员获取新的激活码，续期后即可正常使用系统
            </div>
          </div>
          <div slot="footer" class="renewal-dialog-footer">
            <el-button
              plain
              size="medium"
              @click="renewalDialogVisible = false"
              style="width: 120px"
            >
              取消
            </el-button>
            <el-button
              type="primary"
              size="medium"
              :loading="isLoading"
              @click="renewLicense"
              style="width: 180px"
            >
              续期并登录
            </el-button>
          </div>
        </el-dialog>

        <!-- 诊所选择对话框 -->
        <el-dialog
          title="选择诊所"
          :visible.sync="clinicDialogVisible"
          :close-on-click-modal="false"
          :close-on-press-escape="false"
          :show-close="false"
          :append-to-body="true"
          :modal-append-to-body="true"
          width="440px"
          custom-class="clinic-select-dialog"
        >
          <div class="clinic-select-content">
            <p class="clinic-select-tip">您的账号关联了多个诊所，请选择要进入的诊所</p>
            <div
              v-for="clinic in clinics"
              :key="clinic.clinicId"
              class="clinic-card"
              :class="{ active: selectedClinicId === clinic.clinicId }"
              @click="selectClinic(clinic.clinicId)"
            >
              <div class="clinic-card-main">
                <span class="clinic-name">{{ clinic.clinicName }}</span>
                <div class="clinic-card-tags">
                  <span class="clinic-role-tag" :class="clinic.role">
                    {{ clinicRoleLabel(clinic.role) }}
                  </span>
                  <span v-if="clinic.isDefault" class="clinic-default-tag">
                    <i class="el-icon-check" style="margin-right: 2px; font-size: 10px;"></i>默认诊所
                  </span>
                </div>
              </div>
              <div v-if="selectedClinicId === clinic.clinicId" class="clinic-selected-icon">
                <i class="el-icon-check"></i>
              </div>
            </div>
          </div>
          <div slot="footer" class="clinic-dialog-footer">
            <el-button
              type="primary"
              :disabled="!selectedClinicId"
              @click="confirmClinic"
              class="clinic-confirm-btn"
            >
              进入诊所
            </el-button>
          </div>
        </el-dialog>

        <div class="login-footer">
          <div class="footer-icp">湘ICP备2026011054号 · {{ displayClinicFooterName }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { saveAdminSession, getAdminSession } from "@/utils/adminSession";

export default {
  name: "Login",
  data() {
    return {
      isLoading: false,
      needsInit: false,
      user: {
        username: '',
        password: ''
      },
      initForm: {
        username: '',
        password: '',
        confirmPassword: '',
        clinicName: '',
        activationCode: ''
      },
      clinicDialogVisible: false,
      clinics: [],
      selectedClinicId: '',
      renewalDialogVisible: false,
      renewalMessage: '',
      renewalForm: {
        username: '',
        password: '',
        activationCode: ''
      },
      renewalRules: {
        activationCode: [
          { required: true, message: '请输入激活码', trigger: 'blur' },
          { min: 10, message: '激活码格式不正确', trigger: 'blur' }
        ]
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ]
      },
      initRules: {
        username: [
          { required: true, message: '请输入管理员账号', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.initForm.password) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            }, trigger: 'blur'
          }
        ],
        clinicName: [
          { required: true, message: '请输入诊所名称', trigger: 'blur' }
        ],
        activationCode: [
          { required: true, message: '请输入激活码', trigger: 'blur' },
          { min: 10, message: '激活码格式不正确', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    // 登录页品牌名称：优先使用本地持久化会话中的诊所名，未获取时显示默认名称
    displayClinicName() {
      const session = getAdminSession()
      return (session && session.currentClinicName) ? session.currentClinicName : '诊所管理系统'
    },
    displayClinicFooterName() {
      const session = getAdminSession()
      return (session && session.currentClinicName) ? session.currentClinicName : '诊所管理系统'
    }
  },
  created() {
    this.checkNeedsInit()
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
    checkNeedsInit() {
      axios.get('/auth/needs-init')
        .then(res => {
          if (res.data.code === '200' && res.data.data && res.data.data.needsInit) {
            this.needsInit = true
          }
        })
        .catch(() => {
          // 接口失败时不影响登录流程
        })
    },
    initSystem() {
      this.$refs['initRef'].validate((valid) => {
        if (!valid) return
        this.isLoading = true
        axios.post('/auth/init', {
          username: String(this.initForm.username || '').trim(),
          password: String(this.initForm.password || ''),
          clinicName: String(this.initForm.clinicName || '').trim(),
          activationCode: String(this.initForm.activationCode || '').trim().toUpperCase()
        })
          .then(response => {
            this.isLoading = false
            if (response.data.code === '200') {
              this.$message.success('系统初始化成功，请登录')
              this.needsInit = false
              this.user.username = this.initForm.username
            } else {
              this.$message.error(response.data.msg || '初始化失败')
            }
          })
          .catch(error => {
            this.isLoading = false
            console.error('初始化错误:', error)
            const msg = error && error.response && error.response.data && error.response.data.msg
              ? error.response.data.msg
              : '初始化失败'
            this.$message.error(msg)
          })
      })
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (!valid) return;
        this.isLoading = true;
        const payload = {
          username: String(this.user.username || '').trim(),
          password: String(this.user.password || '').trim()
        };
        axios.post('/auth/login', payload)
          .then(response => {
            this.isLoading = false;
            const code = String(response.data.code);
            if (code === '200') {
              this.handleLoginSuccess(response.data.data);
            } else if (code === '403') {
              const data = response.data.data || {};
              // 需要续期（管理员）
              if (data.needsRenewal) {
                this.renewalMessage = data.licenseMessage || '系统授权已过期';
                this.renewalForm.username = this.user.username;
                this.renewalForm.password = this.user.password;
                this.renewalForm.activationCode = '';
                this.renewalDialogVisible = true;
              } else {
                this.$message.error(response.data.msg || "登录失败");
              }
            } else {
              this.$message.error(response.data.msg || "登录失败");
            }
          })
          .catch(error => {
            this.isLoading = false;
            console.error('登录错误:', error);
            const msg = error && error.response && error.response.data && error.response.data.msg
              ? error.response.data.msg
              : "登录失败";
            this.$message.error(msg);
          });
      });
    },
    selectClinic(clinicId) {
      this.selectedClinicId = clinicId;
    },
    confirmClinic() {
      const selected = this.clinics.find(c => c.clinicId === this.selectedClinicId);
      if (!selected) return;

      this.pendingLoginData.currentClinicId = selected.clinicId;
      this.pendingLoginData.currentClinicName = selected.clinicName;
      saveAdminSession(this.pendingLoginData);
      this.clinicDialogVisible = false;
      this.$message.success("登录成功");
      if (this.$route.path !== '/home') {
        this.$router.push("/home").catch(() => {});
      }
    },
    clinicRoleTagType(role) {
      const map = { admin: 'danger', doctor: 'success', nurse: 'warning' };
      return map[role] || 'info';
    },
    clinicRoleLabel(role) {
      const map = { admin: '管理员', doctor: '医生', nurse: '护士' };
      return map[role] || role;
    },
    handleLoginSuccess(data) {
      const clinics = data.clinics || [];

      if (clinics.length === 0) {
        this.$message.warning("该账号未分配诊所，请联系管理员");
        return;
      }

      if (clinics.length === 1) {
        data.currentClinicId = clinics[0].clinicId;
        data.currentClinicName = clinics[0].clinicName;
        saveAdminSession(data);
        this.$message.success("登录成功");
        if (this.$route.path !== '/home') {
          this.$router.push("/home").catch(() => {});
        }
      } else {
        this.clinics = clinics;
        const defaultClinic = clinics.find(c => c.isDefault === 1);
        this.selectedClinicId = defaultClinic ? defaultClinic.clinicId : clinics[0].clinicId;
        this.pendingLoginData = data;
        this.clinicDialogVisible = true;
      }
    },
    renewLicense() {
      this.$refs['renewalRef'].validate((valid) => {
        if (!valid) return;
        this.isLoading = true;
        axios.post('/auth/renew-license', {
          username: String(this.renewalForm.username || '').trim(),
          password: String(this.renewalForm.password || '').trim(),
          activationCode: String(this.renewalForm.activationCode || '').trim().toUpperCase()
        })
          .then(response => {
            this.isLoading = false;
            if (response.data.code === '200') {
              this.$message.success('续期成功，正在登录');
              this.renewalDialogVisible = false;
              this.handleLoginSuccess(response.data.data);
            } else {
              this.$message.error(response.data.msg || '续期失败');
            }
          })
          .catch(error => {
            this.isLoading = false;
            console.error('续期错误:', error);
            const msg = error && error.response && error.response.data && error.response.data.msg
              ? error.response.data.msg
              : '续期失败';
            this.$message.error(msg);
          });
      });
    }
  }
}
</script>

<style scoped>
.login-page {
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
.login-form {
  animation: fadeIn 0.4s ease 0.3s forwards;
  opacity: 0;
}

.init-tip {
  font-size: 13px;
  color: #5A8F7B;
  background: rgba(90, 143, 123, 0.08);
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
  line-height: 1.5;
}

@keyframes fadeIn {
  to { opacity: 1; }
}

/* 细线输入框（东方风格） */
.login-form >>> .el-input__inner {
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

.login-form >>> .el-input__inner:focus {
  background: transparent;
  border-bottom-color: #5A8F7B;
  box-shadow: none;
}

.login-form >>> .el-input__prefix {
  left: 2px;
  color: #A0A0A0;
  transition: color 0.3s ease;
}

.login-form >>> .el-input__inner:focus + .el-input__prefix,
.login-form >>> .el-input__inner:focus ~ .el-input__prefix {
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

/* === 登录按钮 === */
.login-btn {
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

.login-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
}

.login-btn:hover {
  background: #4A7F6B;
  box-shadow: 0 4px 16px rgba(90, 143, 123, 0.25);
}

.login-btn:hover::after {
  transform: translateX(100%);
}

.login-btn:active {
  transform: scale(0.98);
}

/* === 按钮入场动画 === */
.btn-animate {
  animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1) 0.6s forwards;
  opacity: 0;
  transform: translateY(12px);
}

/* === Footer === */
.login-footer {
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

.register-link {
  color: #5A8F7B;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s ease;
}
.register-link:hover {
  color: #4A7F6B;
  text-decoration: underline;
}

.footer-icp {
  font-size: 11px;
  color: #A0A0A0;
  letter-spacing: 0.05em;
}

/* === 诊所选择对话框 === */
.clinic-select-content {
  padding: 4px 4px;
}
.clinic-select-tip {
  font-size: 14px;
  color: #8A9A8E;
  margin-bottom: 24px;
  text-align: center;
  letter-spacing: 0.02em;
}
.clinic-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1.5px solid rgba(90, 143, 123, 0.12);
  border-radius: 12px;
  padding: 18px 20px;
  margin-bottom: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fff;
}
.clinic-card:last-child {
  margin-bottom: 0;
}
.clinic-card:hover {
  border-color: rgba(90, 143, 123, 0.35);
  box-shadow: 0 4px 16px rgba(90, 143, 123, 0.08);
  transform: translateY(-1px);
}
.clinic-card.active {
  border-color: #5A8F7B;
  background: rgba(90, 143, 123, 0.04);
  box-shadow: 0 4px 16px rgba(90, 143, 123, 0.12);
}
.clinic-card-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.clinic-name {
  font-size: 17px;
  font-weight: 600;
  color: #2C3E35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.clinic-card-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.clinic-role-tag {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 10px;
}
.clinic-role-tag.admin {
  color: #6B8FA8;
  background: rgba(107, 143, 168, 0.10);
}
.clinic-role-tag.doctor {
  color: #5A8F7B;
  background: rgba(90, 143, 123, 0.10);
}
.clinic-role-tag.nurse {
  color: #C9A227;
  background: rgba(201, 162, 39, 0.10);
}
.clinic-default-tag {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  color: #5A8F7B;
  background: rgba(90, 143, 123, 0.08);
  padding: 4px 10px;
  border-radius: 10px;
  font-weight: 500;
}
.clinic-selected-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #5A8F7B;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  margin-left: 14px;
  flex-shrink: 0;
}
.clinic-dialog-footer {
  text-align: center;
  padding-top: 8px;
}
.clinic-confirm-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.08em;
  border-radius: 10px !important;
  background: #5A8F7B !important;
  border-color: #5A8F7B !important;
}
.clinic-confirm-btn:hover {
  background: #4A7F6B !important;
  border-color: #4A7F6B !important;
}
.clinic-confirm-btn.is-disabled {
  background: rgba(90, 143, 123, 0.35) !important;
  border-color: rgba(90, 143, 123, 0.2) !important;
  color: #fff !important;
}

/* === 授权续期对话框 === */
.renewal-dialog >>> .el-dialog__header {
  display: none;
}
.renewal-dialog >>> .el-dialog__body {
  padding: 0;
}
.renewal-dialog >>> .el-dialog__footer {
  padding: 0 32px 28px;
  border-top: none;
}
.renewal-header {
  text-align: center;
  padding: 32px 32px 20px;
  background: linear-gradient(180deg, #fdf6ec 0%, #ffffff 100%);
  border-radius: 12px 12px 0 0;
}
.renewal-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  padding: 6px 14px;
  border-radius: 20px;
  margin-bottom: 16px;
}
.renewal-status-badge i {
  font-size: 14px;
}
.renewal-title {
  font-size: 20px;
  font-weight: 600;
  color: #2C3E35;
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}
.renewal-desc {
  font-size: 14px;
  color: #8A9A8E;
  margin: 0;
  line-height: 1.6;
}
.renewal-body {
  padding: 20px 32px 0;
}
.renewal-form-label {
  font-size: 14px;
  font-weight: 500;
  color: #2C3E35;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}
.renewal-input >>> .el-input__inner {
  height: 44px;
  border-radius: 8px;
  border: 1.5px solid #e0e0e0;
  font-size: 15px;
  transition: all 0.25s ease;
  padding-left: 38px;
}
.renewal-input >>> .el-input__inner:focus {
  border-color: #5A8F7B;
  box-shadow: 0 0 0 3px rgba(90, 143, 123, 0.08);
}
.renewal-input >>> .el-input__prefix {
  left: 12px;
  color: #a0a0a0;
}
.renewal-help {
  margin-top: 12px;
  font-size: 12px;
  color: #8A9A8E;
  background: #f8f9fa;
  padding: 10px 14px;
  border-radius: 8px;
  display: flex;
  align-items: flex-start;
  line-height: 1.5;
}
.renewal-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
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
