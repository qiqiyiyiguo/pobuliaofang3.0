<template>
        <div class="container">
          <div class="header" :class="{ hidden: currentPage !== 1 }">
            <div class="a">
        <div class="b"></div>
        <div class="c"></div>
        <div class="d"></div>
        <div class="e"></div>
        <div class="f"><img class="icon" src="../../assets/image/图标w.png" alt="">
          <h1>面试模拟网站</h1>
          <p>Project Overview</p>
        </div>
        
    </div>
          </div>

  

            <div class="text" :class="{ visible: currentPage === 2 }">
            <div id="main">
                <div id="click-section">
                <div id="drawerboxes">
                    <div class="drawerbox" :class="{ active: chosenSlideNumber === 1 }">
                      <button class="drawer-btn" 
         :class="{ active: chosenSlideNumber === 1 }"
         @click="slideTo(1)">Project Overview<span class="drawer-head">1</span></button>
                    </div>
                    <div class="drawerbox" :class="{ active: chosenSlideNumber === 2 }">
                      <button class="drawer-btn" 
         :class="{ active: chosenSlideNumber === 2 }"
         @click="slideTo(2)">Technical Details<span class="drawer-head">2</span></button>
                    </div>
                    <div class="drawerbox" :class="{ active: chosenSlideNumber === 3 }">
                      <button class="drawer-btn" 
         :class="{ active: chosenSlideNumber === 3 }"
         @click="slideTo(3)">Key Features<span class="drawer-head">3</span></button>
                    </div>
                    <div class="drawerbox" :class="{ active: chosenSlideNumber === 4 }">
                      <button class="drawer-btn" 
         :class="{ active: chosenSlideNumber === 4 }"
         @click="slideTo(4)">Applications<span class="drawer-head">4</span></button>
                    </div>
                    <div class="drawerbox" :class="{ active: chosenSlideNumber === 5 }">
                      <button class="drawer-btn" 
         :class="{ active: chosenSlideNumber === 5 }"
         @click="slideTo(5)">Demo<span class="drawer-head">5</span></button>
                    </div>
                    
                </div>
                </div>
                <div id="slide-section">
                <div id="slide-bar">
                    <div id="bar"></div>
                </div>
                <div id="card-section">
                    <div id="card1" class="card" :style="{ transform: `translateY(${offset}%)` }">
                    <div class="card-small-title">面试模拟网站</div>
                    <div class="card-title">Project Overview</div>
                    <div class="card-content">

                      <div class="shell">
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/警示1.png">
                                </div>
                                <div class="content">
                                    <h2>Problem Statement</h2>
                                    <h3>缺乏真实面试场景：学生难以获得贴近企业真实流程的模拟机会，练习方式单一，无法体验面试压力。</h3>
          <h3>反馈滞后且主观：依赖人工点评，反馈不及时、难量化，且受指导者经验影响大，无法精准定位问题。</h3>
          <h3>岗位针对性弱：不同技术岗位面试侧重点各异，学生难以针对性准备，综合能力（沟通、逻辑等）评估不足。</h3>
                                </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/建设中.png">
                                </div>
                                <div class="content">
                                    <h2>Our Solution</h2>
                                    <h3>多岗位模拟面试：针对Java后端、Web前端等岗位构建专属题库，AI面试官动态追问，还原真实压力场景。</h3>
          <h3>多模态交互评估：支持语音/文本输入，集成语音情感分析，多维度评估技术正确性、表达逻辑、沟通能力。</h3>
          <h3>个性化提升路径：生成详细评估报告，智能推荐学习资源，记录成长曲线，形成“练习-评估-提升”闭环。</h3>
                                  </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/数据提示.png">
                                </div>
                                <div class="content">
                                    <h2>Value Proposition</h2>
                                    <h3>降低面试焦虑：随时可用的AI教练，支持反复练习，帮助学生熟悉流程，增强自信。</h3>
          <h3>快速提升技巧：即时、客观的反馈，精准定位短板，针对性强化，显著提升面试通过率。</h3>
          <h3>助力高质量就业：清晰岗位能力图谱，补齐知识短板，为学生从校园到职场的关键跨越提供实践性支撑。</h3>
                                </div>
                            </div>
                            
                        </div>
                    </div>
                   
                    </div>
                    <div id="card2" class="card" :style="{ transform: `translateY(${offset}%)` }">
                    <div class="card-small-title">面试模拟网站</div>
                    <div class="card-title">Technical Details</div>
                    <div class="card-content">
                      <div class="shell">
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/博客.png">
                                </div>
                                <div class="content">
                                    <h2>Model Introduction</h2>
                                    <h3>核心AI引擎：基于大语言模型构建，集成语音识别（ASR）、情感分析、意图理解，实现多轮对话管理与动态追问。</h3>
          <h3>岗位知识库：采用RAG技术，融合岗位核心技术栈、面试题库、优秀范例，确保回答评估的专业性与准确性。</h3>
                                </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/客户评价.png">
                                </div>
                                <div class="content">
                                    <h2>Data</h2>
                                    <h3>岗位化题库：针对Java后端、Web前端等岗位，涵盖技术知识题、项目深挖题、场景题、行为题，总计超过500道。</h3>
          <h3>知识库构建：收集各岗位常见考点、面试经验、优秀回答范例，建立向量知识库，支持检索增强生成。</h3>
                                  </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/联系.png">
                                </div>
                                <div class="content">
                                    <h2>Performance</h2>
                                    <h3>实时响应：多轮对话平均延迟<2秒，支持并发模拟面试，满足高校批量使用需求。</h3>
          <h3>准确评估：评估维度涵盖技术正确性、逻辑严谨性、沟通表达、自信度等，与专家评分一致性达90%以上。</h3>
          <h3>能力可视化：记录历次面试成绩，生成能力成长曲线，直观展示进步轨迹。</h3>
                                    
                                </div>
                                </div>
                            </div>
                    </div>
                    
                    </div>
                    <div id="card3" class="card" :style="{ transform: `translateY(${offset}%)` }">
                    <div class="card-small-title">面试模拟网站</div>
                    <div class="card-title">Key Features</div>
                    <div class="card-content">
                      
                      <div class="shell">
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/电脑-01.png">
                                </div>
                                <div class="content">
                                    <h2>多岗位模拟</h2>
          <h3>岗位差异化面试：支持Java后端、Web前端、测试工程师等岗位，面试题目、追问逻辑、评估标准均按岗位定制。</h3>
          <h3>智能追问：AI根据学生回答中的关键词，动态生成追问，模拟真实面试官的压力式提问。</h3>
                                </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/数据分析.png">
                                </div>
                                <div class="content">
                                    <h2>多维度评估</h2>
          <h3>技术内容分析：深度分析回答的技术正确性、知识深度、与岗位匹配度，指出知识盲点。</h3>
          <h3>表达与软实力评估：通过语音识别与情感分析，评估语速、清晰度、自信度、逻辑条理，量化软实力。</h3>
          <h3>结构化报告：生成详细评估报告，包含各维度得分、亮点与不足、具体改进建议。</h3>
                                 
                                  </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/数据云存储-01.png">
                                </div>
                                <div class="content">
                                    <h2>个性化提升</h2>
          <h3>智能学习推荐：根据评估短板，自动推荐相关技术知识点、典型面试题、沟通技巧文章，精准提升。</h3>
          <h3>成长记录：记录每次面试表现，可视化能力成长曲线，激励学生持续进步。</h3>
                                </div>
                                </div>
                            </div>
                          
                          </div>
                    
                    </div>
                    <div id="card4" class="card" :style="{ transform: `translateY(${offset}%)` }">
                    <div class="card-small-title">面试模拟网站</div>
                    <div class="card-title">Applications</div>
                    <div class="card-content">
                      <div class="shell">
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/客户服务.png">
                                </div>
                                <div class="content">
                                    <h2>高校就业指导</h2>
          <h3>融入就业课程：为高校就业指导中心提供批量账号，组织学生进行模拟面试练习，辅助教师开展针对性辅导。</h3>
                                </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/常见问题.png">
                                </div>
                                <div class="content">
                                    <h2>学生自主练习</h2>
          <h3>随时随地练习：学生可通过网页或小程序随时发起模拟面试，利用碎片时间反复演练，缓解面试焦虑。</h3>
                                    
                                 
                                  </div>
                            </div>
                            <div class="box">
                                <div class="images">
                                    <img src="../../assets/image/个人资料.png">
                                </div>
                                <div class="content">
                                     <h2>职业发展评估</h2>
          <h3>能力图谱与规划：通过模拟面试数据生成个人能力雷达图，为学生提供岗位匹配度分析和职业发展建议。</h3>
                                </div>
                                </div>
                            </div>
                    </div>
                    
                    </div>

                    <div id="card5" class="card" :style="{ transform: `translateY(${offset}%)` }">
                    <div class="card-small-title">面试模拟网站</div>
                    <div class="card-title">Video</div>
                    <div class="card-content"></div>
                    <div class="card-img">
                        <img class="fengmian" src="../../assets/image/直播.png" alt="">
                    </div>
                    </div>

                    
                </div>
                </div>
            </div>
            </div>

          </div>
  </template>
  
  <script setup>
 import { ref, onMounted, onBeforeUnmount } from 'vue';

const currentPage = ref(1); // 当前页面 1-header 2-text
let isScrolling = false;    // 防止滚动冲突

const handleWheel = (e) => {
  if (isScrolling) return;
  isScrolling = true;

  // 判断滚动方向
  const delta = Math.sign(e.deltaY);
  
  if (delta > 0 && currentPage.value === 1) {
    // 向下滚动切换到text
    currentPage.value = 2;
  } else if (delta < 0 && currentPage.value === 2) {
    // 向上滚动切换回header
    currentPage.value = 1;
  }

  setTimeout(() => {
    isScrolling = false;
  }, 800); // 滚动节流时间
};

onMounted(() => {
  window.addEventListener('wheel', handleWheel, { passive: false });
});

onBeforeUnmount(() => {
  window.removeEventListener('wheel', handleWheel);
});
  
  let chosenSlideNumber = ref(1); // 当前选择的幻灯片编号
  let offset = 0; // 幻灯片偏移量
  let barOffset = 0; // 导航条偏移量
  
  const slideTo = (slideNumber) => {
    if (chosenSlideNumber.value === slideNumber) return; // 防止重复点击
  
    const previousSlideNumber = chosenSlideNumber.value;
    chosenSlideNumber.value = slideNumber;
  
    // 更新幻灯片的偏移量
    offset += (slideNumber - previousSlideNumber) * -100;
    barOffset += (slideNumber - previousSlideNumber) * 100;
  
    // 切换幻灯片位置
    const slides = document.querySelectorAll('.card');
    slides.forEach(slide => {
      slide.style.transform = `translateY(${offset}%)`;
    });
  
    // 更新导航条位置
    const bar = document.querySelector('#bar');
    if (bar) {
      bar.style.transform = `translateY(${barOffset}%)`;
    }
  };

  onMounted(() => {
  const background = document.querySelector(".background");

  document.addEventListener('scroll', () => {
    const scrollY = window.scrollY;

    if (scrollY !== 0) {
      background.style.backgroundPosition = `calc(50% + ${scrollY}px) calc(50% + ${scrollY}px)`;
    } else {
      background.style.backgroundPosition = '';
    }
  });
});



  // const background = document.querySelector(".background")

  //       document.addEventListener('scroll', () => {
  //           const scrollY = window.scrollY

  //           if (scrollY !== 0) {
  //               background.style.backgroundPosition = `calc(50% + ${scrollY}px) calc(50% + ${scrollY}px)`
  //           }else{
  //               background.style.backgroundPosition = ''
  //           }
  //       })
  </script>
  

  

<style scoped>
* {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      margin-left: -30px;
    }

    body {
      display: flex;
      justify-content: center;
      align-items: center;
      overflow-x: hidden;
      
      height: 100vh;
      background: linear-gradient(to right bottom,
          rgba(255, 255, 255,.6),
          rgba(255, 255, 255,.3),
          rgba(255, 255, 255,.2)),;
      background-size: cover;
    }
    
    /* .header{
      
            margin: 0;
            padding: 0;
            height: 200vh;
            overflow-x: hidden;
         
    } */

  


    #main {
      
      height: 100vh;
      width: 100vw;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    #click-section {
      width: 20%;
      height: 100%;
      padding: 20px 0;
      position: relative;
    }

    #drawerboxes {
      
      margin-left: 10%;
      width: 100%;
      height: 100%;
      position: relative;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      justify-content: space-evenly;
    }

    #drawerboxes::before {
      
      content: "";
      width: 8px;
      height: 94%;
      position: absolute;
      left: -10px;
      top: 3%;
      z-index: 120;
    }

    .drawerbox {
      padding-top: -50px;
      height: calc(100% / 5.5);
      width: 70%;
      position: relative;
      z-index: 100;
      transform: translateX(-70%);
      transition: transform .5s ease-in-out;
    }

    .drawerbox.active {
      transform: translateX(0);
    }

    .drawer-btn {
      user-select: none;
      width: 100%;
      height: 100%;
      font: 800 30px '';
      background-color: rgb(175, 190, 255);
      border: none;
      transition: background-color .5s ease-in-out;
      color: #ffffff00;
      
    }

    /* .drawer-btn.active {
      
      background-size: cover;
      color: rgb(70, 100, 180);
      z-index: 1010;
    } */

    :deep(.drawer-btn.active) {
      user-select: none;
  background-color: #626AC2 !important;
  color: #ffffff00 !important;
  }

    .drawer-btn:hover {
      cursor: pointer;
    }

    .drawer-head {
      position: absolute;
      color: rgb(255, 255, 255);
      font-size: 200px;
      font-weight: 700;
      right: -38px;
      top: calc(50% - 135px);
      text-shadow: 2px -1px 8px rgba(250, 80, 193, 0.323);
    }

    #slide-section {
      
      user-select: none;
      position: relative;
      height: 100%;
      width: 100%;
      display: flex;
      justify-content: center;
      padding: 0 40px;
      background: linear-gradient(to right bottom,
          rgba(255, 255, 255, .6),
          rgba(255, 255, 255, .3),
          rgba(255, 255, 255, .2));
      backdrop-filter: blur(11px);
    }

    #slide-bar {
      user-select: none;
      position: absolute;
      top: 10%;
      left: 40px;
      height: 80%;
      width: 1px;
      background-color: rgb(223, 223, 223);
    }

    #bar {
      user-select: none;
      position: absolute;
      height: calc(100% / 4);
      width: 5px;
      top: 0;
      left: -1.2px;
      background-color: rgb(175, 190, 255);
      transition: transform .5s ease-in-out;
    }

    #card-section {
      height: 100%;
      width: 95%;
      overflow: hidden;
    }

    .card {
      user-select: none;
      display: flex;
      flex-direction: column;
      height: 100%;
      width: 100%;
      padding: 10% 0;
      color: white;
      font-size: 30px;
      transition: transform .5s ease-in-out;
    }

    .card-small-title {
      user-select: none;
      font-size: 30px;
      font-weight: 600;
      padding-bottom: min(5%, 10px);
      color: rgb(70, 100, 180);
      padding-left:60px;
      padding-top:60px;
    }

    .card-title {
      user-select: none;
      font-size: 80px;
      font-weight: 700;
      padding-bottom: min(20%, 40px);
      padding-left:60px;
      color: rgb(70, 100, 180);
    }

    .card-content {
      user-select: none;
      font-size: 24px;
      font-weight: 400;
      color: rgb(94, 123, 255);
      margin-bottom: 60px;
      display: flex;
            justify-content: center;
            align-items: center;
    }

    .card-img {
      width: 100%;
      height: 1000px;
      
      margin-left:-30px;
    }

    

    .container {
  height: 300vh;
  overflow: hidden;
}

.header {
  
  position: fixed;
  display: flex;
            justify-content: center;
            background-image: url("../../assets/image/background.jpg");
            height: 100vh;

            width: 90vw;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.header.hidden {
  transform: translateY(-110%);
}

.text {
  user-select: none;
  position: fixed;
  top: 110vh;
  
  width: 100%;
  height: 100vh;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.text.visible {
  
  transform: translateY(-110%);
  
}

    /* .header{
            display: flex;
            justify-content: center;
            background-image: url("../../assets/image/介绍页背景.jpg");
            height: 100vh;
            
        } */
        .a{
          user-select: none;
            position: relative;
            top: 10px;
            width: 70vw;
            height: 600px;
            background-image: url("background.jpg");
            background-size: cover;
            display: flex;
            justify-content: center;
            align-items: center;
            overflow: hidden;
        }
        .b,
        .c,
        .d,
        .e{
          user-select: none;
            position: absolute;
            width: 1000px;
            height: 600px;
            /* 设置阴影 */
            filter: drop-shadow(4px 4px 12px rgba(0,0,0,.5));
            background-size: cover;
            opacity: .7;
            transition: .5s;
        }
        .b::after,
        .c::after,
        .d::after,
        .e::after{
          user-select: none;
            content: "";
            position: absolute;
            width: 1000px;
            height: 600px;
            background-image: url("../../assets/image/background.jpg");
        }
        .b{
            left: -400px;
            transform: rotateZ(100deg);
            overflow: hidden;
        }
        .b::after{
            transform: rotateZ(-100deg);
        }

        .c{
            left: -400px;
            transform: rotateZ(-100deg);
            overflow: hidden;
        }
        .c::after{
            transform: rotateZ(100deg);
        }

        .d{
            right: -400px;
            transform: rotateZ(105deg);
            overflow: hidden;
        }
        .d::after{
            transform: rotateZ(-105deg);
        }

        .e{
            right: -400px;
            transform: rotateZ(-100deg);
            overflow: hidden;
        }
        .e::after{
            transform: rotateZ(100deg);
        }
        .f{
            opacity: 0;
            font: 900 50px '';
            /* 设置字体间距 */
            letter-spacing: 10px;
            color: rgb(60,60,70);
            transition: .5s;
        }
        /* 设置动画 */
        .a:hover .b{
            left: -550px;
        }
        .a:hover .c{
            left: -600px;
        }
        .a:hover .d{
            right: -550px;
        }
        .a:hover .e{
            right: -610px;
        }
        .a:hover .f{
            opacity: 1;
        }

        .icon{
          user-select: none;
          height: 25vh;
          justify-items: center;
          
        }

        h1{
          user-select: none;
          margin-top:-50px;
          font-size: 50px;
          color: rgb(60,60,70);
          text-align: center;
          z-index: 1000;
        }

        p{
          user-select: none;
          font-size: 30px;
          text-align: center;
          letter-spacing: 3px;
        }
   
/* 卡片悬浮 */

        .shell {
            position: relative;
            min-width: 1000px;
            display: flex;
            justify-content: center;
            flex-wrap: wrap;
            /* 设置3d变化 */
            transform-style: preserve-3d;
            /* 添加透视效果 */
            perspective: 900px;
            margin-top: 50px;
            margin-left: -50px;
        }

        .shell .box {
            position: relative;
            width: 250px;
            height: 350px;
            transition: 0.3s;
            overflow: hidden;
            margin: 30px;
            transform: rotateY(0deg);
            transition-delay: .1s;
            border-radius: 5px;
            border: #fff 5px solid;
        }

        /* 当鼠标所有卡片的父元素点shell上时，
        所有卡片像Y轴翻转20度 */
        .shell:hover .box {
            transform: rotateY(20deg);
        }

        /* 当鼠标悬浮在当前卡片上时，当前卡片的翻转角度为0，
        并放大一点二五倍，加个阴影 */
        .shell .box:hover {
            transform: rotateY(0deg) scale(1.25);
            box-shadow: 0 25px 40px rgba(0, 0, 0, 0.7);
            z-index: 1;
        }

        /* 当鼠标悬浮在卡片上时，
        当前卡片后面的所有卡片全部都反向翻转负20度 */
        .shell .box:hover~.box {
            transform: rotateY(-20deg);
        }

        .shell .box .images img {
          margin-top:50px;
            width: 100%;
        }

        .shell .box .content {
            position: absolute;
            top: 0;
            width: 95%;
            height: 100%;
            z-index: 999;
            padding: 15px;
            overflow: auto;
            overflow-y: hidden;
        }

        .shell .box .content h2 {
            color: black;
            transition: 0.3s;
            font-size: 20px;
            transform: translateY(-100px);
        }

        .shell .box:hover .content h2 {
            transform: translateY(-5px);
        }

        .shell .box .content h3 {
            color: rgb(0, 0, 0);
            transition: 0.3s;
            font-size: 14px;
            transform: translateY(600px);
            background-color: rgba(255, 255, 255, 0.7);
            overflow: auto;
            font-family: kaiti;
        }

        .shell .box:hover .content h3 {
            transform: translateY(30px);
        }

        @font-face {
          font-family: kaiti;
          src: url(../../assets/font/汉仪全唐诗简.ttf);
        }

        .fengmian{
          
          height: 50vh;
        }

        .card-img img{
          margin-top:-60px;
          margin-left:80px;
          width: 80%;
          height: 55vh;
        }

</style>