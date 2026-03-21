// router.js
import { createRouter, createWebHistory } from 'vue-router'


import AppLayout from '../components/layout/header.vue'

import Home from '../views/system/index.vue'
import introduce from '../views/system/introduce.vue'
import aiInterview from '../views/system/aiInterview.vue'
import javaques from '../views/system/javaques.vue'
import frontques from '../views/system/frontques.vue'
import Login from '../views/system/login.vue'

const routes = [
  {
  path: '/',
  component: AppLayout, // 父路由使用布局组件
  redirect: '/home', // 重定向到子路由的默认路径
  children: [
      { path: '/home', component: Home },
      { path: '/introduce', component: introduce },
      { path: '/ai-interview', component: aiInterview },
      { path: '/javaques', component: javaques },
      { path: '/frontques', component: frontques },
      { path: '/login', component: Login },
    ]
  }
]

const router = createRouter({
  history:createWebHistory(),
  routes
})

export default router
// router.js





