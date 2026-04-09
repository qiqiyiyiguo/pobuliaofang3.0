import { createApp } from 'vue/dist/vue.esm-bundler.js'
// import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// import api from './api/index.js'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// import './styles/message.scss'
// 引入智能面试2 的全局样式（UTF-8），不改变现有路由结构，只增加样式能力
import './styles/style.css'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// import Loading from './views/error/loading.vue'

// import './assets/main.css'

const app = createApp(App)

// Elment的所有图标注册
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)

app.mount('#app')

// 保留原项目对 $api 与 Pinia 的使用方式（如在其他文件中已配置），这里不改结构
// app.config.globalProperties.$api = api

app.use(ElementPlus)

// app.use(createPinia())

// app.component('Loading',Loading)