<template>
  <div class="introduce-page">
    <section class="intro-header">
      <div class="header-copy">
        <p class="header-tag">智能计算 / 应用类</p>
        <h1>AI模拟面试与能力提升软件</h1>
        <p class="header-desc">
          面向计算机相关专业学生，围绕岗位化题库、多模态模拟面试、多维度能力分析和个性化提升路径，
          构建“练习 - 评估 - 提升”的完整闭环。
        </p>
      </div>

      <div class="header-side">
        <div class="summary-card">
          <span>项目目标</span>
          <strong>让学生获得更真实、更及时、更个性化的面试训练体验</strong>
        </div>
        <div class="summary-stats">
          <article v-for="item in stats" :key="item.label" class="stat-card">
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </article>
        </div>
      </div>
    </section>

    <div class="intro-layout">
      <aside class="intro-nav">
        <p class="nav-title">项目介绍</p>
        <button
          v-for="(section, index) in sections"
          :key="section.id"
          class="nav-item"
          :class="{ active: activeIndex === index }"
          @click="scrollToSection(index)"
        >
          <span class="nav-index">0{{ index + 1 }}</span>
          <span class="nav-text">{{ section.nav }}</span>
        </button>
        <div class="nav-progress">
          <div class="nav-progress-bar" :style="progressStyle"></div>
        </div>
      </aside>

      <main ref="contentRef" class="intro-content" @scroll="handleScroll">
        <section
          v-for="(section, index) in sections"
          :key="section.id"
          :ref="(el) => setSectionRef(el, index)"
          class="intro-section"
        >
          <div class="section-top">
            <div>
              <p class="section-no">0{{ index + 1 }}</p>
              <h2>{{ section.title }}</h2>
            </div>
            <p class="section-subtitle">{{ section.subtitle }}</p>
          </div>

          <div class="section-grid">
            <article class="section-main-card">
              <p class="card-label">{{ section.label }}</p>
              <h3>{{ section.headline }}</h3>
              <p>{{ section.description }}</p>
            </article>

            <div class="section-side-grid">
              <article
                v-for="item in section.points"
                :key="item.title"
                class="info-card"
              >
                <span class="info-tag">{{ item.tag }}</span>
                <h4>{{ item.title }}</h4>
                <p>{{ item.text }}</p>
              </article>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

const stats = [
  { value: '2+', label: '岗位方向起步' },
  { value: '多模态', label: '语音文字交互' },
  { value: '闭环', label: '练习评估提升' }
]

const sections = [
  {
    id: 'background',
    nav: '项目背景',
    title: '项目背景',
    subtitle: '为什么要做这套 AI 模拟面试与能力提升平台',
    label: 'BACKGROUND',
    headline: '高校学生需要的不只是题库，而是真实、有反馈、可重复的面试训练环境。',
    description:
      '随着毕业生数量逐年增加，就业竞争持续加剧。很多学生虽然具备专业技能，但在面试环节普遍存在缺乏经验、紧张怯场、无法精准表达、不了解岗位差异等问题。传统讲座或少量人工模拟面试，难以提供个性化、沉浸式和可持续训练的指导服务。',
    points: [
      {
        tag: '现状',
        title: '传统方式覆盖有限',
        text: '一对多讲座和有限次数的模拟面试，无法兼顾不同学生的能力差异与岗位目标。'
      },
      {
        tag: '机会',
        title: 'AI 技术提供新可能',
        text: '多模态交互、语音识别和智能评估技术的发展，让高度拟真的训练系统具备落地条件。'
      },
      {
        tag: '价值',
        title: '提升求职竞争力',
        text: '通过反复练习和即时反馈，帮助学生从“会知识”进阶到“能展示能力”。'
      }
    ]
  },
  {
    id: 'company',
    nav: '企业场景',
    title: '企业场景与命题来源',
    subtitle: '结合锐捷网络的行业背景与教育数字化实践',
    label: 'SCENARIO',
    headline: '项目不是脱离场景的技术展示，而是扎根在教育就业准备环节中的真实需求。',
    description:
      '锐捷网络作为行业领先的 ICT 基础设施及行业解决方案提供商，长期深耕教育行业数字化场景，关注学生从学习到就业的全链条培养。在云课堂业务实践中，团队持续观察到高校学生在就业准备阶段的面试瓶颈，因此提出构建“AI 模拟面试与能力提升平台”的方向。',
    points: [
      {
        tag: '企业',
        title: '锐捷网络行业积累',
        text: '业务覆盖多个行业和国家地区，在教育、金融、医疗等领域拥有丰富数字化服务经验。'
      },
      {
        tag: '观察',
        title: '就业准备存在断层',
        text: '学生在面试准备中缺乏真实互动与客观反馈，这成为影响高质量就业的重要因素。'
      },
      {
        tag: '方向',
        title: 'AI 与场景融合',
        text: '项目强调把企业级面试场景与技术评估模型结合，形成真正可用的训练平台。'
      }
    ]
  },
  {
    id: 'pain',
    nav: '痛点分析',
    title: '用户痛点',
    subtitle: '当前高校学生在技术类岗位面试准备中的核心问题',
    label: 'PAIN POINTS',
    headline: '学生需要的不是更多资料，而是针对岗位、有节奏、有反馈的训练过程。',
    description:
      '技术类岗位面试不只是考知识点，还考察表达逻辑、项目理解、应变能力和岗位匹配度。如果准备过程缺乏场景感和反馈机制，学生很难知道自己到底弱在哪里，也很难针对性改进。',
    points: [
      {
        tag: '痛点 01',
        title: '缺乏真实感练习',
        text: '难以获得贴近企业真实流程的模拟面试机会，无法感受实际面试的节奏与压力。'
      },
      {
        tag: '痛点 02',
        title: '反馈滞后且主观',
        text: '依赖人工指导时，反馈不即时、难量化，且容易受指导者个人经验影响。'
      },
      {
        tag: '痛点 03',
        title: '岗位针对性不足',
        text: '后端、前端、算法、测试等岗位关注点不同，学生往往难以进行差异化准备。'
      },
      {
        tag: '痛点 04',
        title: '综合能力评估不足',
        text: '除了技术内容，沟通表达、逻辑思维和应变能力也很关键，但学生很难全面自我判断。'
      }
    ]
  },
  {
    id: 'solution',
    nav: '方案设计',
    title: '平台方案',
    subtitle: '系统核心能力与业务闭环设计',
    label: 'SOLUTION',
    headline: '构建一个能够模拟不同技术岗位场景、并提供多维度智能反馈的 AI 教练。',
    description:
      '平台核心不是单次问答，而是让用户在岗位化情景模拟、多模态互动、评估报告和个性化推荐之间形成持续提升链路。系统既要能问，也要能评，更要能指导后续练习。',
    points: [
      {
        tag: '能力 01',
        title: '岗位化情景模拟',
        text: '针对至少两类岗位构建专属题库与评估模型，例如 Java 后端、Web 前端、Python 算法等。'
      },
      {
        tag: '能力 02',
        title: '多模态互动面试',
        text: '支持语音和文本输入，AI 面试官可根据回答内容智能追问，并控制面试节奏。'
      },
      {
        tag: '能力 03',
        title: '多维能力评估',
        text: '从技术正确性、知识深度、逻辑严谨性、沟通表达和岗位匹配度等维度进行分析。'
      },
      {
        tag: '能力 04',
        title: '个性化提升路径',
        text: '结合历史记录推荐练习计划、知识点资源和面试技巧内容，形成成长闭环。'
      }
    ]
  },
  {
    id: 'requirement',
    nav: '功能要求',
    title: '功能要求',
    subtitle: '围绕比赛题目整理出的核心实现模块',
    label: 'REQUIREMENTS',
    headline: '系统既要体现大模型应用能力，也要体现对真实求职训练场景的理解。',
    description:
      '题目要求系统在岗位化题库、知识库、RAG、多模态交互、面试表现分析、报告生成和成长反馈等方面形成完整实现。页面展示上也需要把这些核心模块清楚拆出来，让人一眼看懂系统结构。',
    points: [
      {
        tag: '模块 01',
        title: '岗位题库与知识库',
        text: '包含技术知识、项目深挖、场景题、行为题及优秀回答范例，作为 RAG 的基础。'
      },
      {
        tag: '模块 02',
        title: '语音文本双通道',
        text: '实现自然、流畅的多轮对话体验，兼顾不同用户的使用习惯。'
      },
      {
        tag: '模块 03',
        title: '内容与表达双分析',
        text: '不仅分析回答内容，也要结合语音识别与情感分析评估表达表现。'
      },
      {
        tag: '模块 04',
        title: '成长报告与建议',
        text: '输出结构化报告、练习建议和能力成长曲线，保证结果对学生有实际指导意义。'
      }
    ]
  }
]

const contentRef = ref(null)
const sectionRefs = ref([])
const activeIndex = ref(0)

const setSectionRef = (el, index) => {
  if (el) {
    sectionRefs.value[index] = el
  }
}

const updateActiveIndex = () => {
  const container = contentRef.value
  if (!container) return

  const containerTop = container.getBoundingClientRect().top
  let closestIndex = 0
  let closestDistance = Number.POSITIVE_INFINITY

  sectionRefs.value.forEach((section, index) => {
    if (!section) return
    const distance = Math.abs(section.getBoundingClientRect().top - containerTop)
    if (distance < closestDistance) {
      closestDistance = distance
      closestIndex = index
    }
  })

  activeIndex.value = closestIndex
}

const handleScroll = () => {
  updateActiveIndex()
}

const scrollToSection = (index) => {
  const target = sectionRefs.value[index]
  if (!target) return

  target.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

const progressStyle = computed(() => ({
  height: `${((activeIndex.value + 1) / sections.length) * 100}%`
}))

onMounted(async () => {
  await nextTick()
  updateActiveIndex()
  window.addEventListener('resize', updateActiveIndex)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateActiveIndex)
})
</script>

<style scoped>
.introduce-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(116, 159, 255, 0.16), transparent 24%),
    linear-gradient(180deg, #f5f8ff 0%, #eef3fb 100%);
  color: #1f2d3d;
}

.intro-header,
.intro-nav,
.intro-section {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(201, 214, 255, 0.8);
  box-shadow: 0 16px 40px rgba(88, 116, 180, 0.12);
}

.intro-header {
  border-radius: 24px;
  padding: 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 24px;
  margin-bottom: 24px;
}

.header-tag,
.nav-title,
.section-no,
.card-label,
.info-tag {
  margin: 0;
  color: #5b7ce6;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.header-copy h1 {
  margin: 10px 0 16px;
  font-size: clamp(32px, 4vw, 46px);
  line-height: 1.15;
  color: #22314f;
}

.header-desc {
  margin: 0;
  max-width: 760px;
  color: #5f6f8f;
  font-size: 16px;
  line-height: 1.9;
}

.header-side {
  display: grid;
  gap: 16px;
}

.summary-card {
  border-radius: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #5f7cf7 0%, #7aa2ff 100%);
  color: #fff;
}

.summary-card span {
  display: block;
  font-size: 13px;
  opacity: 0.8;
}

.summary-card strong {
  display: block;
  margin-top: 10px;
  font-size: 20px;
  line-height: 1.6;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-card {
  border-radius: 18px;
  padding: 18px 14px;
  background: #f6f9ff;
  border: 1px solid #dce6ff;
  text-align: center;
}

.stat-card strong {
  display: block;
  color: #3657c8;
  font-size: 24px;
  line-height: 1;
}

.stat-card span {
  display: block;
  margin-top: 8px;
  color: #687a9b;
  font-size: 13px;
}

.intro-layout {
  display: grid;
  grid-template-columns: 250px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.intro-nav {
  position: sticky;
  top: 24px;
  border-radius: 22px;
  padding: 20px 18px;
}

.nav-item {
  width: 100%;
  margin-top: 10px;
  padding: 14px 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid transparent;
  border-radius: 16px;
  background: transparent;
  color: #53627e;
  cursor: pointer;
  text-align: left;
  transition: all 0.25s ease;
}

.nav-item:hover,
.nav-item.active {
  border-color: #cbd8ff;
  background: #f4f7ff;
  color: #2846b6;
}

.nav-index {
  min-width: 32px;
  font-size: 14px;
  font-weight: 700;
  color: #5b7ce6;
}

.nav-text {
  font-size: 14px;
  line-height: 1.4;
}

.nav-progress {
  position: relative;
  width: 4px;
  height: 180px;
  margin: 24px 0 0 14px;
  border-radius: 999px;
  background: #e3ebff;
  overflow: hidden;
}

.nav-progress-bar {
  width: 100%;
  border-radius: inherit;
  background: linear-gradient(180deg, #7aa2ff 0%, #4e6fe5 100%);
  transition: height 0.3s ease;
}

.intro-content {
  height: calc(100vh - 48px);
  overflow-y: auto;
  scroll-snap-type: y mandatory;
  scroll-behavior: smooth;
  padding-right: 4px;
}

.intro-content::-webkit-scrollbar {
  width: 8px;
}

.intro-content::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(120, 143, 210, 0.45);
}

.intro-section {
  min-height: calc(100vh - 48px);
  scroll-snap-align: start;
  border-radius: 24px;
  padding: 28px;
  margin-bottom: 18px;
}

.section-top {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
  margin-bottom: 24px;
}

.section-top h2 {
  margin: 8px 0 0;
  color: #22314f;
  font-size: clamp(28px, 3vw, 40px);
}

.section-subtitle {
  margin: 0;
  max-width: 320px;
  color: #7384a3;
  line-height: 1.8;
  text-align: right;
}

.section-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 1fr);
  gap: 20px;
}

.section-main-card,
.info-card {
  border-radius: 20px;
  border: 1px solid #dce5ff;
  background: #f9fbff;
}

.section-main-card {
  padding: 24px;
}

.section-main-card h3 {
  margin: 10px 0 16px;
  color: #2a3b60;
  font-size: 28px;
  line-height: 1.45;
}

.section-main-card p:last-child {
  margin: 0;
  color: #5e6f8f;
  line-height: 1.95;
  font-size: 15px;
}

.section-side-grid {
  display: grid;
  gap: 16px;
}

.info-card {
  padding: 18px 18px 16px;
}

.info-card h4 {
  margin: 10px 0 8px;
  color: #30466f;
  font-size: 18px;
}

.info-card p {
  margin: 0;
  color: #627391;
  line-height: 1.85;
  font-size: 14px;
}

@media (max-width: 1100px) {
  .intro-header,
  .intro-layout,
  .section-grid {
    grid-template-columns: 1fr;
  }

  .intro-nav {
    position: relative;
    top: 0;
  }

  .intro-content {
    height: auto;
    overflow: visible;
  }

  .intro-section {
    min-height: auto;
  }

  .section-top {
    align-items: flex-start;
    flex-direction: column;
  }

  .section-subtitle {
    max-width: none;
    text-align: left;
  }
}

@media (max-width: 720px) {
  .introduce-page {
    padding: 16px;
  }

  .intro-header,
  .intro-section {
    padding: 20px;
  }

  .summary-stats {
    grid-template-columns: 1fr;
  }
}
</style>
