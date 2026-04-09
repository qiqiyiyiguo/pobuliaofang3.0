<template>
  <!-- 首页右侧内容直接挂载 AI 模拟面试官应用 -->
  <div id="ai-interview-app"></div>
</template>

<script setup>
import { onMounted } from 'vue'
import { mountAiInterview } from '@/script/main.js'

onMounted(() => {
  mountAiInterview('#ai-interview-app')
})
</script>

<style scoped>
#ai-interview-app {
  width: 100%;
  height: 100%;
}
</style>

