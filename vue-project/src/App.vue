<script setup>
import { onMounted, onUnmounted } from "vue";

const preventZoom = (event) => {
  if (event.ctrlKey || event.metaKey) {
    event.preventDefault();
  }
};

onMounted(() => {
  document.addEventListener("wheel", preventZoom, { passive: false });
  document.addEventListener("keydown", (event) => {
    if (
      (event.ctrlKey || event.metaKey) &&
      ["+", "-", "=", "0"].includes(event.key)
    ) {
      event.preventDefault();
    }
  });
});

onUnmounted(() => {
  document.removeEventListener("wheel", preventZoom);
});
</script>

<template>
  <!-- 只保留路由出口，根实例挂载在 index.html 的 #app 上 -->
  <router-view></router-view>
</template>

<style scoped>
/* src/assets/main.css */
.el-overlay {
  background-color: rgba(0, 0, 0, 0.5) !important;
  backdrop-filter: blur(2px);
}

.el-overlay .el-dialog {
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}
</style>
