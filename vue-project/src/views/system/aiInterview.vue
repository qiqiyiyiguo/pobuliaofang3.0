<template>
  <div id="ai-interview-app"></div>
</template>

<script setup>
import { onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { mountAiInterview } from '@/script/main.js';

const route = useRoute();
const router = useRouter();
let appInstance = null;

const applyRouteState = () => {
  if (!appInstance) return;
  const { view, tab } = route.query;

  if (view === 'question') {
    appInstance.view = 'question';
    appInstance.questionTab = tab || 'bank';
  } else if (view === 'resume') {
    // 新增：简历分析视图
    appInstance.view = 'resume';
    // 打开简历上传弹窗（如果方法存在）
    if (typeof appInstance.openResumeModal === 'function') {
      appInstance.openResumeModal();
    }
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
  // 把路由实例传给 mountAiInterview
  appInstance = mountAiInterview('#ai-interview-app', router);
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