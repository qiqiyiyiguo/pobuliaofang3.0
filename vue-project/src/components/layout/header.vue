<script setup>
import { useRouter } from 'vue-router';
import { AddLocation, CaretLeft, DataAnalysis, Film, FolderAdd, HomeFilled, InfoFilled, UserFilled } from '@element-plus/icons-vue'
import { computed, ref } from 'vue';
const router = useRouter();

const routerPush = (path) => {
  router.push(path);
}

// 主页按钮高亮
const home = computed(() => {
  return router.currentRoute.value.path === '/home' ? 'butRouterDeeper' : 'butRouter'
})

// 介绍按钮高亮
const introduce = computed(() => {
  return router.currentRoute.value.path === '/introduce' ? 'butRouterDeeper' : 'butRouter'
})

// 当前路由对象
const currentRoute = computed(() => router.currentRoute.value);

// 岗位题库
const questionBank = computed(() => {
  const route = currentRoute.value;
  return route.path === '/ai-interview' &&
    (route.query.view === 'question' && (!route.query.tab || route.query.tab === 'bank'))
    ? 'butRouterDeeper'
    : 'butRouter';
});

// 简历分析（新增）
const resumeAnalysis = computed(() => {
  const route = currentRoute.value;
  return route.path === '/ai-interview' &&
    route.query.view === 'resume'
    ? 'butRouterDeeper'
    : 'butRouter';
});

// 多模态交互式模拟面试
const multimodalInterview = computed(() => {
  const route = currentRoute.value;
  return route.path === '/ai-interview' &&
    route.query.view === 'interview'
    ? 'butRouterDeeper'
    : 'butRouter';
});

// 历史记录与能力曲线
const historyAnalysis = computed(() => {
  const route = currentRoute.value;
  return route.path === '/ai-interview' &&
    route.query.view === 'analysis'
    ? 'butRouterDeeper'
    : 'butRouter';
});

const asidewidth = ref(260)
const contentwidth = ref(330)

const asideCss = computed(() => ({
  '--asidewidth': `${asidewidth.value}px`
}))
const contentCss = computed(() => ({
  '--contentwidth': `${contentwidth.value}px`
}))
const ifBig = ref(1)
const ifSmall = ref(0)

const beSmall = () => {
  asidewidth.value = 130
  contentwidth.value = 170
  ifBig.value = 0
  ifSmall.value = 1
}
const beBig = () => {
  asidewidth.value = 260
  contentwidth.value = 330
  ifBig.value = 1
  ifSmall.value = 0
}
</script>

<template>
  <div class="app-layout" :style="contentCss">
    <!-- 固定侧边栏 -->
    <aside class="sidebar" :style="asideCss">
      <div id="topContainer" v-if="ifBig">
        <b id="top"> 模拟面试</b>
        <el-button id="butTop" @click="beSmall">
          <img class="icon" src="../../assets/image/左箭头.png" />
        </el-button>
      </div>
      <div v-if="ifBig" id="bigone">
        <!-- 顶部两个主导航：主页 / 介绍 -->
        <el-button :class="home" @click="routerPush('/home')">
          <span>主页</span>
        </el-button><br>

        <el-button :class="introduce" @click="routerPush('/introduce')">
          <span>介绍</span>
        </el-button><br>

        <!-- 左侧与“主页 / 介绍”同侧的功能导航按钮 -->
        <div class="ai-menu">
          <div class="ai-menu-title">AI 模拟面试官</div>
          
          <!-- 1. 岗位题库 -->
          <el-button
            :class="questionBank"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'question', tab: 'bank' } })"
          >
            <span>岗位题库</span>
          </el-button>
          
          <!-- 2. 简历分析（新增，放在岗位题库后面） -->
          <el-button
            :class="resumeAnalysis"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'resume' } })"
          >
            <span>简历分析</span>
          </el-button>
          
          <!-- 3. 多模态交互式模拟面试 -->
          <el-button
            :class="multimodalInterview"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'interview' } })"
          >
            <span>多模态交互式模拟面试</span>
          </el-button>
          
          <!-- 4. 历史记录与能力曲线 -->
          <el-button
            :class="historyAnalysis"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'analysis' } })"
          >
            <span>历史记录与能力曲线</span>
          </el-button>
        </div>
        
        <div class="per">
          <div class="personal">
            <!-- 个人区域 -->
          </div>
        </div>
      </div>

      <div>
        <el-button id="bigger" @click="beBig" v-if="ifSmall"><img id="logo"
            src="../../assets/image/右箭头.png"></el-button>
      </div>
      <div id="smallone" v-if="ifSmall">
        <!-- 收缩状态下仅保留主页 / 介绍两个图标按钮 -->
        <el-button :class="home" @click="routerPush('/home')">
          <el-icon :size="29">
            <HomeFilled />
          </el-icon>
        </el-button><br>
        <el-button :class="introduce" @click="routerPush('/introduce')">
          <el-icon :size="29">
            <InfoFilled />
          </el-icon>
        </el-button><br>

        <div class="per">
          <!-- 个人区域 -->
        </div>
      </div>
    </aside>

    <!-- 动态内容区域 -->
    <main class="content">
      <router-view :key="router.fullPath" />
    </main>
  </div>
</template>

<style scoped>
/* 你的原有样式保持不变，这里省略重复内容 */
.app-layout {
  display: grid;
  grid-template-areas:
    "sidebar content";
  grid-template-columns: var(--contentwidth) 1fr;
  height: 100vh;
  width: 100vw;
  margin: -8px;
  padding: 0;
  transition: grid-template-columns 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar {
  grid-area: sidebar;
  background:
    linear-gradient(180deg, rgba(22, 30, 63, 0.9), rgba(12, 22, 44, 0.92)),
    rgba(255, 255, 255, 0.04);
  position: absolute;
  width: var(--asidewidth);
  padding: 1.5rem;
  height: 100vh;
  overflow-y: auto;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 24px 0 48px rgba(4, 10, 30, 0.32);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
  color: #f6f8ff;
  scrollbar-color: rgba(139, 151, 255, 0.32) transparent;
}

.content {
  grid-area: content;
  padding: 1rem;
  height: 100vh;
  overflow: auto;
  text-align: center;
}

.content>* {
  display: block;
  margin: 0 auto;
  text-align: left;
  max-width: 100%;
}

#topContainer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  width: 100%;
  margin-top: 10px;
  margin-bottom: 50px;
  height: auto;
}

#top {
  display: inline-block;
  margin-left: 20px;
  font-size: 24px;
  font-family: 'Futura', 'Century Gothic', 'Avenir', sans-serif;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(150%);
  color: #ffffff;
  letter-spacing: 0.08em;
}

#butTop {
  position: absolute;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  margin: 0;
  padding: 0;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  flex-shrink: 0;
  color: #d8dfff;
  border-radius: 999px;
  backdrop-filter: blur(12px);
}

.icon {
  width: 30px;
  height: 30px;
  margin-left: 60px;
}

.per {
  position: absolute;
  bottom: 30px;
  left: 50%;
}

.personal {
  position: relative;
  align-items: center;
  text-align: center;
  display: flex;
  flex-direction: row;
  align-items: center;
  bottom: 50px;
}

.img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  margin-left: -30px;
  position: absolute;
}

.name {
  font-size: 16px;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  color: #ffffff;
  margin-top: 35px;
  margin-left: -40px;
  position: absolute;
}

.email {
  font-size: 14px;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  color: rgba(220, 226, 255, 0.66);
  margin-top: 135px;
  margin-left: -70px;
}

.butRouter {
  margin-bottom: 30px;
  width: 100%;
  height: 44px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 18px;
  color: rgba(227, 233, 255, 0.84);
  border-radius: 16px;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  transition: all 0.3s;
  justify-content: flex-start;
  padding: 0 18px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

#bigone .el-button:hover {
  background: rgba(255, 255, 255, 0.12) !important;
  border-color: rgba(140, 152, 255, 0.4) !important;
  color: white;
}

.butRouter span {
  cursor: pointer;
  display: inline-block;
  position: relative;
  transition: 0.5s;
}

.butRouter span::after {
  content: '>';
  position: absolute;
  opacity: 0;
  top: 0;
  right: -25px;
  transition: 0.5s;
}

.butRouter:hover span {
  padding-right: 15px;
}

.butRouter:hover span::after {
  opacity: 1;
  right: -5px;
}

.butRouter:active span::after {
  opacity: 0;
  top: 0;
  right: -25px;
  transition: 0.5s;
}

.butRouter:active span {
  padding-right: 0px;
  transition: 0.5s;
}

.butRouterDeeper {
  margin-bottom: 30px;
  width: 100%;
  height: 44px;
  background: linear-gradient(135deg, #6673ff 0%, #7351ff 100%);
  border: 1px solid transparent;
  font-size: 18px;
  color: white;
  border-radius: 16px !important;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  transition: all 0.3s;
  justify-content: flex-start;
  padding: 0 18px;
}

.ai-menu {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.ai-menu-title {
  font-size: 14px;
  font-weight: 600;
  color: rgba(211, 219, 250, 0.74);
  margin-bottom: 12px;
  letter-spacing: 0.08em;
}

.ai-menu .el-button {
  width: 100%;
  justify-content: flex-start;
  padding: 0 18px;
  margin: 0 0 14px 0;
  min-height: 54px;
  height: auto;
  border-radius: 18px !important;
  box-shadow: none;
  align-items: center;
  white-space: normal;
  line-height: 1.5;
}

.ai-menu .el-button span {
  width: 100%;
  text-align: left;
  letter-spacing: 0.04em;
  white-space: normal;
}

.ai-menu .butRouter {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(227, 233, 255, 0.82);
}

.ai-menu .butRouterDeeper {
  background: linear-gradient(135deg, #6673ff 0%, #7351ff 100%);
  color: #ffffff;
  box-shadow: 0 16px 30px rgba(93, 89, 255, 0.28);
}

.ai-menu .butRouter span::after,
.ai-menu .butRouterDeeper span::after {
  content: '';
}

#logo {
  width: 50px;
  height: 40px;
}

#bigger {
  margin-top: 30px;
  margin-left: -5px;
  margin-bottom: 50px;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 999px;
}

#smallone>button {
  margin-bottom: 30px;
  margin-left: 5px;
  transition: all 0.3s;
}

#smallone .el-button:hover {
  background: rgba(255, 255, 255, 0.14) !important;
  color: white;
  width: 40px;
  height: 40px;
  padding-left: 7px;
  border-color: rgba(140, 152, 255, 0.4) !important;
}

#smallone .butRouter {
  width: 40px;
  height: 40px;
  border-radius: 10px !important;
}

#smallone .butRouterDeeper {
  border-radius: 10px !important;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6673ff 0%, #7351ff 100%) !important;
  margin-left: 15px;
  padding-left: 7px;
}

#smallone .el-icon {
  color: inherit;
}

.sidebar::-webkit-scrollbar {
  width: 8px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(140, 152, 255, 0.28);
  border-radius: 999px;
}
</style>