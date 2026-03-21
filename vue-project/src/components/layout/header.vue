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

// 项目经历深挖
const projectDeep = computed(() => {
  const route = currentRoute.value;
  return route.path === '/ai-interview' &&
    route.query.view === 'question' &&
    route.query.tab === 'project'
    ? 'butRouterDeeper'
    : 'butRouter';
});

// 行为与场景题
const behaviorScene = computed(() => {
  const route = currentRoute.value;
  return route.path === '/ai-interview' &&
    route.query.view === 'question' &&
    route.query.tab === 'behavior'
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
  '--asidewidth': `${asidewidth.value}px`  // 添加 -- 前缀并添加单位
}))
const contentCss = computed(() => ({
  '--contentwidth': `${contentwidth.value}px`  // 添加 -- 前缀并添加单位
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
          <!-- 使用已存在的图标资源，避免 404 -->
          <img class="icon" src="../../assets/image/左箭头.png" />
        </el-button>
      </div>
      <!-- <div id="topContainer">
      <div><span id="top">Auris Glow</span></div><button class="butTop">回</button>
      </div> -->
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
          <el-button
            :class="questionBank"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'question', tab: 'bank' } })"
          >
            <span>岗位题库</span>
          </el-button>
          <el-button
            :class="projectDeep"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'question', tab: 'project' } })"
          >
            <span>项目经历深挖</span>
          </el-button>
          <el-button
            :class="behaviorScene"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'question', tab: 'behavior' } })"
          >
            <span>行为与场景题</span>
          </el-button>
          <el-button
            :class="multimodalInterview"
            class="ai-menu-btn"
            @click="routerPush({ path: '/ai-interview', query: { view: 'interview' } })"
          >
            <span>多模态交互式模拟面试</span>
          </el-button>
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
            <!-- <img id="personal" src="../../assets/image/图标.png" class="img">
            <div id="name">

              <span id="name" class="name">
                zhangsan
              </span>
            </div>
            <div id="email" class="email">
              1234567890@qq.com
            </div> -->
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
          <!-- <div class="personal">
            <img id="personal" src="../../assets/image/图标.png" class="img">
          </div> -->
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
.app-layout {
  display: grid;
  grid-template-areas:
    "sidebar content";
  grid-template-columns: var(--contentwidth) 1fr;
  /* 正确引用 */
  height: 100vh;
  width: 100vw;
  margin: -8px;
  padding: 0;
  /* transition: grid-template-columns 0.3s; */
  transition: grid-template-columns 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar {
  grid-area: sidebar;
  background: #fefefe;
  position: absolute;
  width: var(--asidewidth);
  /* 正确引用 */
  padding: 1.5rem;
  height: 100vh;
  overflow-y: auto;
  /* transition: width 0.3s; */
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  /* 标准缓入缓出曲线 */
  box-shadow: 9px 9px 18px #e4e4e4,
    -9px -9px 18px #e6e6e6;
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
  /* 水平居中 */
  text-align: left;
  /* 防止内部文本也居中 */
  max-width: 100%;
  /* 防止溢出 */
}

#topContainer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  /* 为绝对定位按钮提供参照 */
  width: 100%;
  margin-top: 10px;
  margin-bottom: 50px;
  height: auto;
  
  /* 为按钮预留空间 */
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
  background-color: #fefefe;
  border: none;
  flex-shrink: 0;
  /* 禁止按钮缩小 */
  color: #646ac2;
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
  /*设置主轴方向是水平方向*/
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
  color: black;
  margin-top: 35px;
  margin-left: -40px;
  position: absolute;
}

.email {
  font-size: 14px;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  color: gray;
  margin-top: 135px;
  margin-left: -70px
}

/* 
#topContainer{
  display: inline-block;
  width: 100%;
  margin-top: 10px;
  margin-bottom: 30px;
  height: auto;
}


#top {
  display: inline-block;
  font-size: 30px;
  font-family: 'Futura', 'Century Gothic', 'Avenir', sans-serif;
}

#butTop {
  width: 5px;
  margin-left: 15px;
  padding-left: 15px;
  padding-bottom: 20px;
  background-color: rgb(249, 251, 255);
  border: none;
} */

/* #topContainer > div {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: auto;

}

#top {
  display: inline-block;
  font-size: 32px;
  font-family: 'Courier New', Courier, monospace;
}

#topbut {
flex: ;

} */


.butRouter {
  margin-bottom: 30px;
  width: 100%;
  background-color: none;
  height: 38px;
  background-color: #fefefe;
  border: none;
  font-size: 18px;
  border-radius: 16px;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  transition: all 0.3s;
  justify-content: flex-start;
  padding: 0 18px;
}

#bigone .el-button:hover {
  background-color: #626AC2 !important;
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

.butRouter:active span::after{
  opacity: 0;
  top: 0;
  right: -25px;
  transition: 0.5s;
}
.butRouter:active span{
  padding-right: 0px;
  transition: 0.5s;
}


.butRouterDeeper {
  margin-bottom: 30px;
  width: 100%;
  background-color: none;
  height: 38px;
  background-color: #626AC2;
  border: none;
  font-size: 18px;
  color: white;
  border-radius: 16px !important;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  transition: all 0.3s;
  justify-content: flex-start;
  padding: 0 18px;
}

/* AI 模拟面试官功能说明区 */
.ai-menu {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #e5e7eb;
}

.ai-menu-title {
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 12px;
}

/* 统一 AI 区域里所有按钮的对齐与尺寸 */
.ai-menu .el-button {
  width: 100%;
  justify-content: flex-start;
  padding: 0 18px;
  margin: 0 0 14px 0;
  height: 38px;
  border-radius: 999px !important;
  border: none;
  box-shadow: none;
}

.ai-menu .el-button span {
  width: 100%;
  text-align: left;
  letter-spacing: 0.04em;
}

/* 普通状态：浅色胶囊 */
.ai-menu .butRouter {
  background-color: #e5e7ff;
  color: #4b5563;
}

/* 激活状态：深色胶囊 */
.ai-menu .butRouterDeeper {
  background-color: #626AC2;
  color: #ffffff;
}

/* 取消 AI 菜单里的 “>” 箭头动画，避免视觉拥挤 */
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
  background-color: #fefefe;

}

#smallone>button {
  margin-bottom: 30px;
  margin-left: 5px;
  transition: all 0.3s;

}

#smallone .el-button:hover {
  background-color: #626AC2 !important;
  color: white;
  width: 40px;
  height: 40px;
  padding-left: 7px;
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
  background-color: #626AC2 !important;
  margin-left: 15px;
  padding-left: 7px;
}

#smallone .el-icon {
  color: inherit;
}



</style>