<script setup>
import { computed, reactive, ref } from 'vue'
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
const pageTitle = computed(() => isLoginMode.value ? '欢迎登录' : '创建账号')
const pageSubtitle = computed(() =>
  isLoginMode.value
    ? '登录后即可继续使用 AI 模拟面试、题库与历史分析功能。'
    : '注册一个新账号，后续可直接登录并保存你的练习记录。'
)

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
    switchMode('login')
    loginForm.username = registerForm.username.trim()
    loginForm.password = ''
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
  } catch (error) {
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

loadCurrentUser()
</script>

<template>
  <div class="auth-page">
    <div class="auth-shell">
      <section class="auth-panel auth-brand">
        <p class="eyebrow">AI Mock Interview</p>
        <h1>{{ pageTitle }}</h1>
        <p class="brand-copy">
          {{ pageSubtitle }}
        </p>
        <div class="feature-list">
          <div class="feature-card">
            <strong>岗位题库</strong>
            <span>按方向练习，聚焦更贴近真实招聘的问题。</span>
          </div>
          <div class="feature-card">
            <strong>模拟面试</strong>
            <span>支持多模态互动，完成一整轮问答演练。</span>
          </div>
          <div class="feature-card">
            <strong>练习沉淀</strong>
            <span>保留你的账号信息，后续扩展历史记录与能力分析。</span>
          </div>
        </div>
      </section>

      <section class="auth-panel auth-form-panel">
        <div class="tabs">
          <button :class="['tab-btn', { active: isLoginMode }]" @click="switchMode('login')">登录</button>
          <button :class="['tab-btn', { active: !isLoginMode }]" @click="switchMode('register')">注册</button>
        </div>

        <div v-if="isLoginMode" class="form-card">
          <h2>登录账号</h2>
          <label class="field-label" for="login-username">用户名</label>
          <input id="login-username" v-model="loginForm.username" class="field-input" type="text" placeholder="请输入用户名">

          <label class="field-label" for="login-password">密码</label>
          <input id="login-password" v-model="loginForm.password" class="field-input" type="password" placeholder="请输入密码" @keydown.enter="handleLogin">

          <button class="submit-btn" :disabled="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '立即登录' }}
          </button>
        </div>

        <div v-else class="form-card">
          <h2>注册账号</h2>
          <label class="field-label" for="register-username">用户名</label>
          <input id="register-username" v-model="registerForm.username" class="field-input" type="text" placeholder="请输入用户名">

          <label class="field-label" for="register-email">邮箱</label>
          <input id="register-email" v-model="registerForm.email" class="field-input" type="email" placeholder="请输入邮箱">

          <label class="field-label" for="register-nickname">昵称</label>
          <input id="register-nickname" v-model="registerForm.nickname" class="field-input" type="text" placeholder="可选，默认使用用户名">

          <label class="field-label" for="register-password">密码</label>
          <input id="register-password" v-model="registerForm.password" class="field-input" type="password" placeholder="至少 6 位">

          <label class="field-label" for="register-confirm-password">确认密码</label>
          <input id="register-confirm-password" v-model="registerForm.confirmPassword" class="field-input" type="password" placeholder="请再次输入密码" @keydown.enter="handleRegister">

          <button class="submit-btn" :disabled="loading" @click="handleRegister">
            {{ loading ? '注册中...' : '完成注册' }}
          </button>
        </div>

        <p v-if="message" class="status-text">{{ message }}</p>

        <div v-if="currentUser" class="profile-card">
          <h3>当前已登录用户</h3>
          <p>用户名：{{ currentUser.username }}</p>
          <p>昵称：{{ currentUser.nickname || '未设置' }}</p>
          <p>邮箱：{{ currentUser.email || '未设置' }}</p>

          <div class="password-box">
            <h4>修改密码</h4>
            <input v-model="passwordForm.oldPassword" class="field-input" type="password" placeholder="旧密码">
            <input v-model="passwordForm.newPassword" class="field-input" type="password" placeholder="新密码">
            <button class="secondary-btn" :disabled="loading" @click="handlePasswordChange">修改密码</button>
          </div>
        </div>

        <button class="back-link" @click="router.push('/home')">返回首页</button>
      </section>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(98, 106, 194, 0.26), transparent 32%),
    radial-gradient(circle at bottom right, rgba(60, 179, 113, 0.18), transparent 26%),
    linear-gradient(135deg, #f7f8ff 0%, #eef2ff 48%, #ffffff 100%);
  box-sizing: border-box;
}

.auth-shell {
  max-width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 24px;
}

.auth-panel {
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 64px rgba(76, 87, 186, 0.14);
  backdrop-filter: blur(12px);
}

.auth-brand {
  padding: 48px 42px;
  color: #1f2a56;
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #6070c9;
}

.auth-brand h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.15;
}

.brand-copy {
  margin: 18px 0 32px;
  font-size: 16px;
  line-height: 1.8;
  color: #4d5b8a;
}

.feature-list {
  display: grid;
  gap: 16px;
}

.feature-card {
  padding: 18px 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(98, 106, 194, 0.1), rgba(98, 106, 194, 0.02));
}

.feature-card strong,
.feature-card span {
  display: block;
}

.feature-card strong {
  margin-bottom: 8px;
  font-size: 17px;
}

.feature-card span {
  color: #55627d;
  line-height: 1.7;
}

.auth-form-panel {
  padding: 28px;
}

.tabs {
  display: inline-flex;
  padding: 6px;
  border-radius: 999px;
  background: #eef1ff;
}

.tab-btn {
  min-width: 108px;
  height: 42px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #4b5b89;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.tab-btn.active {
  background: linear-gradient(135deg, #626ac2, #4f58d7);
  color: #fff;
  box-shadow: 0 12px 30px rgba(79, 88, 215, 0.28);
}

.form-card {
  margin-top: 24px;
}

.form-card h2,
.profile-card h3,
.password-box h4 {
  margin-top: 0;
  color: #26345f;
}

.field-label {
  display: block;
  margin: 14px 0 8px;
  font-size: 14px;
  color: #5b678a;
}

.field-input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  border: 1px solid #d8def7;
  border-radius: 14px;
  box-sizing: border-box;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field-input:focus {
  border-color: #626ac2;
  box-shadow: 0 0 0 4px rgba(98, 106, 194, 0.14);
}

.submit-btn,
.secondary-btn,
.back-link {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  cursor: pointer;
}

.submit-btn {
  margin-top: 20px;
  background: linear-gradient(135deg, #626ac2, #4752db);
  color: #fff;
}

.submit-btn:disabled,
.secondary-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.secondary-btn {
  margin-top: 14px;
  background: #edf1ff;
  color: #40508d;
}

.back-link {
  margin-top: 18px;
  background: transparent;
  color: #55627d;
  border: 1px solid #d8def7;
}

.status-text {
  margin: 16px 0 0;
  line-height: 1.7;
  color: #40508d;
}

.profile-card {
  margin-top: 20px;
  padding: 18px;
  border-radius: 18px;
  background: #f8f9ff;
  color: #4a577b;
}

.password-box {
  margin-top: 18px;
}

@media (max-width: 900px) {
  .auth-page {
    padding: 18px;
  }

  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-brand,
  .auth-form-panel {
    padding: 24px;
  }

  .auth-brand h1 {
    font-size: 34px;
  }
}
</style>
