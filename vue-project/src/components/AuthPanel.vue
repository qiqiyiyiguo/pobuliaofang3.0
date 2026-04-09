<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { changePassword, getUserInfo, login, register } from '@/api/user.js'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const message = ref('')
const currentUser = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  nickname: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const isLoginMode = computed(() => route.path !== '/register')
const shellClass = computed(() => ({
  'right-panel-active': !isLoginMode.value
}))

function switchMode(mode) {
  router.push(mode === 'register' ? '/register' : '/login')
  message.value = ''
}

function setAuth(data) {
  if (!data) return
  localStorage.setItem('token', data.token || '')
  localStorage.setItem('userInfo', JSON.stringify(data.user || null))
  currentUser.value = data.user || null
}

async function handleLogin() {
  if (!loginForm.username.trim() || !loginForm.password) {
    message.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  message.value = ''
  try {
    const data = await login(loginForm.username.trim(), loginForm.password)
    setAuth(data)
    message.value = '登录成功，正在返回首页'
    setTimeout(() => {
      router.push('/home')
    }, 500)
  } catch (error) {
    message.value = error?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username.trim() || !registerForm.password || !registerForm.email.trim()) {
    message.value = '请填写用户名、邮箱和密码'
    return
  }
  if (registerForm.password.length < 6) {
    message.value = '密码长度不能少于 6 位'
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    message.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  message.value = ''
  try {
    await register({
      username: registerForm.username.trim(),
      password: registerForm.password,
      email: registerForm.email.trim(),
      nickname: registerForm.nickname.trim()
    })
    message.value = '注册成功，请登录'
    loginForm.username = registerForm.username.trim()
    loginForm.password = ''
    switchMode('login')
  } catch (error) {
    message.value = error?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function loadCurrentUser() {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const user = await getUserInfo()
    currentUser.value = user
    localStorage.setItem('userInfo', JSON.stringify(user))
  } catch {
    currentUser.value = null
  }
}

async function handlePasswordChange() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.value = '请输入旧密码和新密码'
    return
  }

  loading.value = true
  message.value = ''
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    message.value = '密码修改成功'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (error) {
    message.value = error?.message || '修改密码失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => route.path,
  () => {
    message.value = ''
  }
)

loadCurrentUser()
</script>

<template>
  <div class="auth-page">
    <div :class="['auth-shell', shellClass]">
      <section class="auth-surface container">
        <div class="auth-card auth-card--signin container__form container--signin">
          <form id="form2" class="form form-pane form-pane--signin" @submit.prevent="handleLogin">
            <div class="title-wrap">
              <p class="micro-copy">Sign In</p>
              <h2 class="form-title">登录账号</h2>
              <span class="title-line"></span>
            </div>

            <p class="form-subtitle">继续你的模拟面试、题库练习和分析记录。</p>

            <div class="field-group">
              <input
                v-model="loginForm.username"
                class="field-input"
                type="text"
                placeholder="请输入用户名"
              >
              <input
                v-model="loginForm.password"
                class="field-input"
                type="password"
                placeholder="请输入密码"
              >
            </div>

            <button type="button" class="text-link">忘记密码？</button>

            <button class="primary-btn" :disabled="loading">
              {{ loading && isLoginMode ? '登录中...' : 'Sign In' }}
            </button>

            <button type="button" class="ghost-link mobile-switch" @click="switchMode('register')">
              没有账号？去注册
            </button>
          </form>
        </div>

        <div class="auth-card auth-card--signup container__form container--signup">
          <form id="form1" class="form form-pane form-pane--signup" @submit.prevent="handleRegister">
            <h2 class="form-title form-title--light">注册账号</h2>

            <div class="field-group">
              <input
                v-model="registerForm.username"
                class="field-input field-input--glass"
                type="text"
                placeholder="用户名"
              >
              <input
                v-model="registerForm.email"
                class="field-input field-input--glass"
                type="email"
                placeholder="邮箱地址"
              >
              <input
                v-model="registerForm.nickname"
                class="field-input field-input--glass"
                type="text"
                placeholder="昵称（可选）"
              >
              <input
                v-model="registerForm.password"
                class="field-input field-input--glass"
                type="password"
                placeholder="密码，至少 6 位"
              >
              <input
                v-model="registerForm.confirmPassword"
                class="field-input field-input--glass"
                type="password"
                placeholder="确认密码"
              >
            </div>

            <button class="secondary-btn secondary-btn--light" :disabled="loading">
              {{ loading && !isLoginMode ? '注册中...' : 'Sign Up' }}
            </button>

            <button type="button" class="ghost-link ghost-link--light mobile-switch" @click="switchMode('login')">
              已有账号？去登录
            </button>
          </form>
        </div>

        <div class="overlay-wrap container__overlay">
          <div class="overlay-track overlay">
            <div class="overlay-panel overlay-panel--left overlay__panel overlay--left">
              <span class="badge">Welcome Back</span>
              <h3>保持你的练习节奏</h3>
              <p>回到你的面试训练空间，继续查看题目、记录与分析结果。</p>
              <button id="signIn" class="overlay-btn" @click="switchMode('login')">Sign In</button>
            </div>

            <div class="overlay-panel overlay-panel--right overlay__panel overlay--right">
              <span class="badge">Join Us</span>
              <h3>创建新的学习身份</h3>
              <p>快速注册后即可保存面试记录、同步成长数据，并开始新的练习。</p>
              <button id="signUp" class="overlay-btn" @click="switchMode('register')">Sign Up</button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div class="auth-footer-card">
      <div v-if="currentUser" class="profile-card">
        <h3>当前已登录用户</h3>
        <p>用户名：{{ currentUser.username }}</p>
        <p>昵称：{{ currentUser.nickname || '未设置' }}</p>
        <p>邮箱：{{ currentUser.email || '未设置' }}</p>

        <div class="password-box">
          <h4>修改密码</h4>
          <input v-model="passwordForm.oldPassword" class="field-input" type="password" placeholder="请输入旧密码">
          <input v-model="passwordForm.newPassword" class="field-input" type="password" placeholder="请输入新密码">
          <button class="secondary-btn" :disabled="loading" @click="handlePasswordChange">修改密码</button>
        </div>
      </div>

      <p v-if="message" class="status-text">{{ message }}</p>
      <button class="back-btn" @click="router.push('/home')">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 32px 24px;
  box-sizing: border-box;
  background:
    radial-gradient(circle at 50% -2%, rgba(150, 74, 255, 0.5), transparent 22%),
    radial-gradient(circle at 10% 92%, rgba(35, 206, 255, 0.36), transparent 26%),
    radial-gradient(circle at 92% 62%, rgba(58, 90, 255, 0.24), transparent 22%),
    radial-gradient(circle at 86% 88%, rgba(72, 97, 255, 0.28), transparent 24%),
    linear-gradient(135deg, #0c1428 0%, #131b35 40%, #1a2243 66%, #101935 100%);
  position: relative;
  overflow: hidden;
}

.auth-page::before,
.auth-page::after {
  content: '';
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 28px;
  pointer-events: none;
}

.auth-page::before {
  inset: 28px auto auto 22px;
  width: 220px;
  height: 150px;
  background: rgba(255, 255, 255, 0.015);
}

.auth-page::after {
  right: 34px;
  bottom: 36px;
  width: 150px;
  height: 104px;
  background: rgba(255, 255, 255, 0.012);
}

.auth-shell {
  max-width: 1080px;
  margin: 0 auto;
  display: block;
  position: relative;
  z-index: 1;
}

.auth-surface,
.auth-footer-card {
  background: rgba(23, 31, 62, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  box-shadow: 0 28px 70px rgba(4, 10, 30, 0.45);
}

.auth-surface {
  position: relative;
  min-height: 610px;
  border-radius: 30px;
  overflow: hidden;
}

.container {
  position: relative;
  width: 100%;
}

.auth-footer-card {
  max-width: 1080px;
  margin: 18px auto 0;
  border-radius: 24px;
  padding: 22px;
}

.container__form,
.container__overlay,
.auth-card,
.overlay-wrap {
  position: absolute;
  top: 0;
  height: 100%;
  transition:
    transform 0.78s cubic-bezier(0.645, 0.045, 0.355, 1),
    opacity 0.48s ease,
    visibility 0s linear 0.42s;
  will-change: transform, opacity;
}

.auth-card {
  width: 50%;
}

.container--signin,
.auth-card--signin {
  left: 0;
  z-index: 2;
  opacity: 1;
  visibility: visible;
  transform: translateX(0) scale(1);
}

.container--signup,
.auth-card--signup {
  left: 0;
  opacity: 0;
  visibility: hidden;
  z-index: 1;
  transform: translateX(0);
}

.auth-shell.right-panel-active .container--signin,
.auth-shell.right-panel-active .auth-card--signin {
  transform: translateX(100%);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
}

.auth-shell.right-panel-active .container--signup,
.auth-shell.right-panel-active .auth-card--signup {
  transform: translateX(100%) scale(1);
  opacity: 1;
  visibility: visible;
  z-index: 5;
  pointer-events: auto;
  animation: show 0.78s;
  transition:
    transform 0.78s cubic-bezier(0.645, 0.045, 0.355, 1),
    opacity 0.54s ease 0.18s,
    visibility 0s linear 0s;
}

.form,
.form-pane {
  height: 100%;
  padding: 70px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition:
    transform 0.72s cubic-bezier(0.645, 0.045, 0.355, 1),
    opacity 0.48s ease,
    filter 0.48s ease;
  will-change: transform, opacity, filter;
}

.form-pane--signin {
  background:
    linear-gradient(180deg, rgba(24, 31, 56, 0.88), rgba(18, 29, 49, 0.9)),
    rgba(255, 255, 255, 0.02);
}

.form-pane--signup {
  background:
    linear-gradient(180deg, rgba(36, 40, 84, 0.84), rgba(31, 36, 75, 0.88)),
    rgba(255, 255, 255, 0.02);
}

.auth-card--signin .form-pane {
  transform: translateX(0);
  opacity: 1;
  filter: blur(0);
}

.auth-card--signup .form-pane {
  transform: translateX(36px);
  opacity: 0.02;
  filter: blur(10px);
}

.auth-shell.right-panel-active .auth-card--signin .form-pane {
  transform: translateX(24px);
  opacity: 0.02;
  filter: blur(10px);
}

.auth-shell.right-panel-active .auth-card--signup .form-pane {
  transform: translateX(0);
  opacity: 1;
  filter: blur(0);
}

.title-wrap {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
}

.title-line {
  width: 140px;
  height: 4px;
  margin-top: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.95), rgba(165, 180, 255, 0.4));
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin: 10px 0 18px;
}

.micro-copy,
.badge {
  margin: 0 0 12px;
  font-size: 12px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.micro-copy {
  color: rgba(212, 219, 248, 0.55);
}

.form-title {
  margin: 0;
  font-size: 48px;
  color: #ffffff;
  font-weight: 800;
  line-height: 1.08;
}

.form-title--light {
  max-width: 320px;
}

.form-subtitle {
  margin: 18px 0 14px;
  line-height: 1.8;
  color: rgba(214, 222, 245, 0.7);
  max-width: 360px;
}

.form-subtitle--light {
  color: rgba(232, 237, 255, 0.74);
}

.field-input {
  width: 100%;
  height: 54px;
  margin: 0;
  padding: 0 20px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(241, 244, 252, 0.96);
  box-sizing: border-box;
  color: #18223d;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field-input--glass {
  background: rgba(64, 73, 117, 0.56);
  color: rgba(243, 246, 255, 0.96);
  border-color: rgba(255, 255, 255, 0.08);
}

.field-input::placeholder {
  color: rgba(56, 67, 97, 0.48);
}

.field-input--glass::placeholder {
  color: rgba(220, 226, 248, 0.58);
}

.field-input:focus {
  border-color: rgba(126, 141, 255, 0.72);
  box-shadow: 0 0 0 4px rgba(101, 119, 255, 0.14);
  background: rgba(255, 255, 255, 0.96);
}

.field-input--glass:focus {
  background: rgba(78, 88, 136, 0.72);
}

.primary-btn,
.secondary-btn,
.overlay-btn,
.back-btn {
  height: 52px;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.primary-btn {
  width: 172px;
  margin-top: 8px;
  background: linear-gradient(135deg, #7d86ff 0%, #7a59ff 100%);
  color: #fff;
  box-shadow: 0 14px 30px rgba(112, 95, 255, 0.34);
}

.secondary-btn {
  width: 168px;
  margin-top: 12px;
  background: rgba(255, 255, 255, 0.14);
  color: #314278;
}

.secondary-btn--light {
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #7d86ff 0%, #7a59ff 100%);
  box-shadow: 0 14px 28px rgba(112, 95, 255, 0.28);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.primary-btn:hover:not(:disabled),
.secondary-btn:hover:not(:disabled),
.overlay-btn:hover,
.back-btn:hover {
  transform: translateY(-1px);
}

.ghost-link {
  margin-top: 16px;
  padding: 0;
  border: none;
  background: transparent;
  color: rgba(219, 228, 255, 0.7);
  cursor: pointer;
  font-size: 14px;
}

.ghost-link--light {
  color: rgba(255, 255, 255, 0.74);
}

.text-link {
  margin: 2px 0 0;
  padding: 0;
  width: fit-content;
  border: none;
  background: transparent;
  color: rgba(225, 232, 255, 0.7);
  cursor: pointer;
  font-size: 14px;
}

.mobile-switch {
  display: none;
}

.container__overlay,
.overlay-wrap {
  left: 50%;
  width: 50%;
  overflow: hidden;
  z-index: 10;
  transition: transform 0.78s cubic-bezier(0.645, 0.045, 0.355, 1);
  will-change: transform;
}

.auth-shell.right-panel-active .container__overlay,
.auth-shell.right-panel-active .overlay-wrap {
  transform: translateX(-100%);
}

.overlay,
.overlay-track {
  position: relative;
  left: -100%;
  width: 200%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 85%, rgba(89, 220, 255, 0.08), transparent 24%),
    radial-gradient(circle at 78% 14%, rgba(142, 88, 255, 0.16), transparent 20%),
    linear-gradient(135deg, rgba(25, 31, 56, 0.92), rgba(34, 40, 76, 0.88)),
    linear-gradient(160deg, rgba(255, 255, 255, 0.04), rgba(255, 255, 255, 0.02));
  transition: transform 0.78s cubic-bezier(0.645, 0.045, 0.355, 1);
  will-change: transform;
}

.overlay::after,
.overlay-track::after {
  content: '';
  position: absolute;
  inset: -20% 36% -20% auto;
  width: 220px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0.02));
  filter: blur(16px);
  opacity: 0.65;
  transform: skewX(-18deg) translateX(0);
  transition: transform 0.82s cubic-bezier(0.645, 0.045, 0.355, 1), opacity 0.56s ease;
}

.auth-shell.right-panel-active .overlay,
.auth-shell.right-panel-active .overlay-track {
  transform: translateX(50%);
}

.auth-shell.right-panel-active .overlay::after,
.auth-shell.right-panel-active .overlay-track::after {
  transform: skewX(-18deg) translateX(-54px);
  opacity: 0.9;
}

.overlay__panel,
.overlay-panel {
  position: absolute;
  top: 0;
  width: 50%;
  height: 100%;
  padding: 72px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  transition:
    transform 0.78s cubic-bezier(0.645, 0.045, 0.355, 1),
    opacity 0.48s ease;
  opacity: 1;
  will-change: transform, opacity;
}

.overlay--left,
.overlay-panel--left {
  transform: translateX(-20%);
}

.overlay--right,
.overlay-panel--right {
  right: 0;
  transform: translateX(0);
}

.auth-shell.right-panel-active .overlay--left,
.auth-shell.right-panel-active .overlay-panel--left {
  transform: translateX(0);
}

.auth-shell.right-panel-active .overlay--right,
.auth-shell.right-panel-active .overlay-panel--right {
  transform: translateX(20%);
}

@keyframes show {
  0%,
  49.99% {
    opacity: 0;
    z-index: 1;
  }
  50%,
  100% {
    opacity: 1;
    z-index: 5;
  }
}

.badge {
  color: rgba(220, 226, 248, 0.48);
}

.overlay-panel h3 {
  margin: 0;
  font-size: 46px;
  line-height: 1.12;
  max-width: 320px;
}

.overlay-panel p {
  margin: 18px 0 28px;
  line-height: 1.8;
  color: rgba(223, 228, 245, 0.72);
}

.overlay-btn {
  width: 170px;
  background: rgba(111, 119, 150, 0.32);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: none;
}

.profile-card {
  margin-top: 0;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.profile-card h3,
.password-box h4 {
  margin-top: 0;
}

.profile-card p,
.status-text {
  color: rgba(224, 231, 255, 0.8);
  line-height: 1.7;
}

.password-box {
  margin-top: 18px;
}

.secondary-btn {
  width: 100%;
  margin-top: 4px;
}

.status-text {
  margin: 18px 0 0;
}

.back-btn {
  width: 100%;
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.14);
}

@media (max-width: 1080px) {
  .auth-shell {
    max-width: 920px;
  }

  .form-pane,
  .overlay-panel {
    padding-left: 40px;
    padding-right: 40px;
  }

  .form-title,
  .overlay-panel h3 {
    font-size: 40px;
  }
}

@media (max-width: 900px) {
  .auth-page {
    padding: 16px;
  }

  .auth-surface {
    min-height: auto;
    padding: 18px;
  }

  .auth-card,
  .overlay-wrap {
    position: static;
    width: 100%;
    transform: none !important;
    opacity: 1 !important;
  }

  .overlay-wrap {
    display: none;
  }

  .auth-card--signin,
  .auth-card--signup {
    position: static;
    width: 100%;
    left: auto;
    z-index: auto;
  }

  .form-pane {
    padding: 30px 20px;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.06);
  }

  .form-title {
    font-size: 34px;
  }

  .field-input,
  .field-input--glass {
    background: rgba(255, 255, 255, 0.88);
    color: #18223d;
  }

  .field-input--glass::placeholder {
    color: rgba(56, 67, 97, 0.6);
  }

  .secondary-btn,
  .primary-btn {
    width: 100%;
  }

  .auth-card--signin {
    display: block;
    opacity: 1;
    visibility: visible;
  }

  .auth-card--signup {
    display: none;
    opacity: 1;
    visibility: visible;
  }

  .auth-shell.right-panel-active .auth-card--signin {
    display: none;
  }

  .auth-shell.right-panel-active .auth-card--signup {
    display: block;
  }

  .mobile-switch {
    display: inline-block;
  }

  .auth-footer-card {
    margin-top: 16px;
    padding: 18px;
  }
}
</style>
