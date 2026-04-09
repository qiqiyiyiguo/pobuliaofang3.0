import { createRouter, createWebHistory } from 'vue-router'

import AiInterviewCoach from '../components/AiInterviewCoach.vue'
import AuthPanel from '../components/AuthPanel.vue'
import AppLayout from '../components/layout/header.vue'

import Home from '../views/system/index.vue'
import introduce from '../views/system/introduce.vue'
import aiInterview from '../views/system/aiInterview.vue'
import javaques from '../views/system/javaques.vue'
import frontques from '../views/system/frontques.vue'
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: AuthPanel,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: AuthPanel,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/home',
    meta: { requiresAuth: false },
    children: [
      { path: '/home', component: Home, meta: { requiresAuth: false } },
      { path: '/introduce', component: introduce, meta: { requiresAuth: false } },
      { path: '/ai-interview', component: aiInterview, meta: { requiresAuth: true } },
      { path: '/javaques', component: javaques, meta: { requiresAuth: true } },
      { path: '/frontques', component: frontques, meta: { requiresAuth: true } }
    ]
  },
  {
    path: '/interview-coach',
    name: 'InterviewCoach',
    component: AiInterviewCoach,
    meta: { 
      title: 'AI面试教练',
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  // 如果需要登录但未登录，跳转到登录页
  if (requiresAuth && !token) {
    next('/login')
  } 
  // 如果已登录且访问登录页或注册页，跳转到首页
  else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/home')
  } 
  else {
    next()
  }
})

export default router
