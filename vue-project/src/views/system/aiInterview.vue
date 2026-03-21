<template>
  <!-- 容器交给智能面试2 的 App 去接管渲染 -->
  <div id="ai-interview-app"></div>
</template>

<script setup>
import { onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { mountAiInterview } from '@/script/main.js';

const route = useRoute();
let appInstance = null;

const applyRouteState = () => {
  if (!appInstance) return;
  const { view, tab } = route.query;

  if (view === 'question') {
    appInstance.view = 'question';
    appInstance.questionTab = tab || 'bank';
  } else if (view === 'interview') {
    if (typeof appInstance.goInterview === 'function') {
      appInstance.goInterview();
    } else {
      appInstance.view = 'interview';
      appInstance.questionTab = 'multimodal';
    }
  } else if (view === 'analysis') {
    appInstance.view = 'analysis';
  } else {
    appInstance.view = 'home';
    appInstance.questionTab = 'bank';
  }
};

onMounted(() => {
  // 在当前路由页面挂载智能面试2 的完整应用
  appInstance = mountAiInterview('#ai-interview-app');
  applyRouteState();
});

watch(
  () => route.fullPath,
  () => {
    applyRouteState();
  }
);
</script>

<style scoped>
#ai-interview-app {
  width: 100%;
  height: 100%;
}
</style>

