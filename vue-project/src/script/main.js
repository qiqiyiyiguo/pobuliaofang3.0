// 智能面试2 的主脚本逻辑，改为 ES 模块形式，并导出挂载函数以便在路由页面中使用
import { createApp } from 'vue';
import '../styles/style.css';

const App = {
  name: 'AiInterviewCoach',
  data() {
    return {
      view: 'home', // home | question | interview | report | analysis
      selectedTrack: '', // frontend | backend | algo | test
      questionTab: 'bank', // bank | multimodal | history
      interviewInited: false,
      analysisReport: {
        overall: 85,
        tech: 88,
        communication: 80,
        logic: 82,
      },
      // 历史记录示例数据（后续可由后端 / 本地存储替换）
      historySessions: [
        {
          id: 1,
          date: '2026-02-20',
          position: '前端开发',
          difficulty: '初级',
          overall: 72,
          tech: 75,
          communication: 68,
          logic: 70,
        },
        {
          id: 2,
          date: '2026-02-27',
          position: '前端开发',
          difficulty: '中级',
          overall: 78,
          tech: 82,
          communication: 74,
          logic: 76,
        },
        {
          id: 3,
          date: '2026-03-03',
          position: '后端开发',
          difficulty: '中级',
          overall: 81,
          tech: 85,
          communication: 77,
          logic: 79,
        },
        {
          id: 4,
          date: '2026-03-06',
          position: '全栈开发',
          difficulty: '中级',
          overall: 84,
          tech: 88,
          communication: 80,
          logic: 82,
        },
      ],
    };
  },
  computed: {
    trackLabel() {
      switch (this.selectedTrack) {
        case 'frontend':
          return 'Web 前端工程师题库';
        case 'backend':
          return 'Java 后端工程师题库';
        case 'algo':
          return 'Python 算法工程师题库';
        case 'test':
          return '测试工程师题库';
        default:
          return '';
      }
    },
    // 把历史记录转换成 SVG 折线图坐标
    abilityChart() {
      const sessions = this.historySessions || [];
      if (!sessions.length) {
        return { overall: '', tech: '', communication: '', logic: '' };
      }
      const maxScore = 100;
      const step = sessions.length > 1 ? 100 / (sessions.length - 1) : 0;
      const toPoints = (key) =>
        sessions
          .map((s, idx) => {
            const x = idx * step;
            const score = typeof s[key] === 'number' ? s[key] : 0;
            const y = 100 - (score / maxScore) * 90 - 5; // 上下留白
            return `${x},${y}`;
          })
          .join(' ');
      return {
        overall: toPoints('overall'),
        tech: toPoints('tech'),
        communication: toPoints('communication'),
        logic: toPoints('logic'),
      };
    },
  },
  methods: {
    goHome() {
      this.view = 'home';
      this.questionTab = 'bank';
    },
    goAuth(mode) {
      window.location.href = mode === 'register' ? '/register' : '/login';
    },
    // 新增：点击岗位跳转到对应页面
    goToJob(track) {
      const routeMap = {
        frontend: '/frontques',
        backend: '/javaques',
        algo: '/pythonques',
        test: '/testques'
      };
      const route = routeMap[track];
      if (route && window.__router__) {
        window.__router__.push(route);
      } else {
        console.warn('路由实例未找到，尝试直接跳转');
        window.location.href = route;
      }
    },
    goQuestion(tab = 'bank') {
      // 如果有选中的岗位，直接跳转
      if (this.selectedTrack) {
        const routeMap = {
          frontend: '/frontques',
          backend: '/javaques',
          algo: '/pythonques',
          test: '/testques'
        };
        const route = routeMap[this.selectedTrack];
        if (route && window.__router__) {
          window.__router__.push(route);
          return;
        }
      }
      this.view = 'question';
      this.selectedTrack = '';
      this.questionTab = tab;
    },
    goInterview() {
      this.view = 'interview';
      this.questionTab = 'multimodal';
      // 确保第三个界面的原生事件和语音识别只初始化一次
      this.$nextTick(() => {
        if (!this.interviewInited && typeof initInterviewPage === 'function') {
          initInterviewPage();
          this.interviewInited = true;
        }
      });
    },
    // 统一侧边栏导航：除首页外其他界面共用同一套侧边栏
    goSidebar(section) {
      if (section === 'bank') {
        this.view = 'question';
        this.questionTab = section;
      } else if (section === 'multimodal') {
        this.goInterview();
      } else if (section === 'history') {
        this.view = 'analysis';
      }
    },
  },
  template: `
    <div class="page">
      <header class="header">
        <div class="brand" @click="goHome">
          <span class="brand-mark"></span>
          <span class="brand-text">AI 模拟面试官</span>
        </div>
        <div class="auth-buttons">
          <button class="btn btn-ghost" @click="goAuth('login')">登录</button>
          <button class="btn btn-primary" @click="goAuth('register')">注册</button>
        </div>
      </header>

      <main class="main">
        <template v-if="view === 'home'">
          <section class="hero">
            <div class="hero-icon">🎯</div>
            <h1 class="hero-title">AI模拟面试官</h1>
            <p class="hero-subtitle">你的专属面试教练</p>
          </section>

          <section class="cards">
            <div class="row row-top">
              <button class="card" @click="goQuestion('bank')">
                <div class="card-title">岗位题库</div>
                <div class="card-subtitle">按岗位维度智能刷题</div>
              </button>
              <button class="card" @click="goInterview">
                <div class="card-title">多模态模拟面试</div>
                <div class="card-subtitle">语音 / 文本互动面试</div>
              </button>
            </div>

            <div class="row row-bottom">
              <button class="card card-introduce">
                <div class="card-title">INTRODUCE</div>
                <div class="card-subtitle">介绍说明</div>
              </button>
              <button class="card" @click="goSidebar('history')">
                <div class="card-title">历史记录与能力曲线</div>
                <div class="card-subtitle">回看历次记录与能力变化</div>
              </button>
            </div>
          </section>
        </template>

        <template v-else-if="view === 'question'">
          <section class="question-layout">
            <section class="question-main">
              <header class="question-header">
                <h2>选择你要练习的岗位方向</h2>
                <p>
                  不同岗位的面试侧重点差异很大，这里根据项目要求划分为 Web 前端工程师、Java 后端工程师、Python 算法工程师和测试工程师，
                  后续会与多模态交互式模拟面试界面、知识库与能力评估模块打通。
                </p>
                <button class="btn btn-ghost btn-small question-back" @click="goHome">
                  ← 返回首页
                </button>
              </header>

              <div class="question-role-grid">
                <button
                  class="role-card"
                  :class="{ 'role-card-active': selectedTrack === 'frontend' }"
                  @click="goToJob('frontend')"
                >
                  <div class="role-tag">Web 前端工程师</div>
                  <div class="role-title">Web 前端 / H5 / 小程序</div>
                  <p class="role-desc">
                    HTML/CSS/JavaScript、前端框架、浏览器与网络、性能优化、工程化等典型面试题，以及项目细节深挖。
                  </p>
                </button>

                <button
                  class="role-card"
                  :class="{ 'role-card-active': selectedTrack === 'backend' }"
                  @click="goToJob('backend')"
                >
                  <div class="role-tag">Java 后端工程师</div>
                  <div class="role-title">Java 后端 / 微服务</div>
                  <p class="role-desc">
                    Java 语法与集合、多线程与并发、Spring 全家桶、MySQL / Redis、消息队列、分布式与微服务设计等题目，贴近实际工程招聘要求。
                  </p>
                </button>

                <button
                  class="role-card"
                  :class="{ 'role-card-active': selectedTrack === 'algo' }"
                  @click="goToJob('algo')"
                >
                  <div class="role-tag">Python 算法工程师</div>
                  <div class="role-title">数据结构 / 算法 / AI 基础</div>
                  <p class="role-desc">
                    Python 语法、数据结构与算法刷题、机器学习 / 深度学习基础、模型评估与调参、实际项目算法设计与复杂场景题。
                  </p>
                </button>

                <button
                  class="role-card"
                  :class="{ 'role-card-active': selectedTrack === 'test' }"
                  @click="goToJob('test')"
                >
                  <div class="role-tag">测试工程师</div>
                  <div class="role-title">功能 / 接口 / 自动化测试</div>
                  <p class="role-desc">
                    测试理论、测试用例设计、缺陷管理、接口测试、自动化测试框架、性能与安全测试等方向题库，突出质量意识与场景分析能力。
                  </p>
                </button>
              </div>

              <div class="question-hint" v-if="selectedTrack">
                当前选择：
                <strong>{{ trackLabel }}</strong>
                。后续可以在这里接入“开始模拟面试 / 生成练习计划 / 查看知识库示例回答”等按钮，实现练习-评估-提升闭环。
              </div>

              <div class="question-hint" v-if="questionTab === 'multimodal'">
                <button class="btn btn-primary btn-small" @click="goInterview">
                  进入多模态交互式模拟面试
                </button>
              </div>
            </section>
          </section>
        </template>

        <template v-else-if="view === 'interview'">
          <section class="question-layout">
            <section class="question-main">
              <div class="interview-shell-header">
                <button class="btn btn-ghost btn-small" @click="goHome">
                  ← 返回首页
                </button>
              </div>

              <div class="app-container">
                <header class="app-header">
                  <h1>🎤 AI面试官</h1>
                  <p class="subtitle">多模态交互 · 语音/文字输入 · 智能追问</p>
                  <div class="interview-info" id="interviewInfo" style="display: none">
                    <span class="info-item">
                      面试时长:
                      <span id="duration">00:00</span>
                    </span>
                    <span class="info-item">
                      对话轮数:
                      <span id="rounds">0</span>
                    </span>
                    <button class="btn btn-danger" onclick="endInterview()">结束面试</button>
                  </div>
                </header>

                <div class="chat-messages" id="chatMessages">
                  <div class="welcome-screen" id="welcomeScreen">
                    <div class="welcome-content">
                      <h2>欢迎使用AI面试官</h2>
                      <p>
                        支持<strong>语音</strong>和<strong>文字</strong>两种输入，AI将根据回答进行<strong>智能追问</strong>
                      </p>

                      <div class="form-group">
                        <label>面试岗位：</label>
                        <select id="position" class="form-control">
                          <option value="frontend">前端开发</option>
                          <option value="backend">后端开发</option>
                          <option value="fullstack">全栈开发</option>
                          <option value="data">数据分析</option>
                          <option value="product">产品经理</option>
                        </select>
                      </div>
                      <div class="form-group">
                        <label>难度：</label>
                        <select id="difficulty" class="form-control">
                          <option value="junior">初级</option>
                          <option value="middle">中级</option>
                          <option value="senior">高级</option>
                        </select>
                      </div>
                      <button class="btn btn-primary" onclick="startInterview()">开始面试</button>
                    </div>
                  </div>
                  <div id="messageList"></div>
                  <div id="typingIndicator" style="display: none" class="message assistant">
                    <div class="message-avatar">🤖</div>
                    <div class="message-content">
                      <div class="typing-indicator">
                        <span></span>
                        <span></span>
                        <span></span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="input-container" id="inputContainer" style="display: none">
                  <div class="input-tabs">
                    <button class="tab-btn active" data-mode="text">✏️ 文字输入</button>
                    <button class="tab-btn" data-mode="voice">🎙️ 语音输入</button>
                  </div>

                  <div id="textInputArea">
                    <div class="text-input-wrapper">
                      <textarea
                        id="textInput"
                        class="text-input"
                        rows="3"
                        placeholder="输入您的回答... (Enter发送)"
                        disabled
                      ></textarea>
                      <button class="btn btn-primary send-btn" id="sendBtn" disabled>发送</button>
                    </div>
                  </div>

                  <div id="voiceInputArea" style="display: none">
                    <div class="voice-input-wrapper">
                      <button class="voice-btn" id="voiceBtn" disabled>🎙️ 按住说话</button>
                      <div class="recognition-preview" id="recognitionPreview" style="display: none">
                        <p>
                          识别结果：<span id="recognitionText"></span>
                        </p>
                        <div class="recognition-actions">
                          <button
                            class="btn btn-primary"
                            style="padding: 8px 16px"
                            onclick="confirmVoice()"
                          >
                            确认发送
                          </button>
                          <button
                            class="btn btn-danger"
                            style="padding: 8px 16px"
                            onclick="clearVoice()"
                          >
                            取消
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div id="sidebarRow" class="sidebar-row" style="display: none">
                  <div class="sidebar-section">
                    <h3>📌 关键词</h3>
                    <div id="keywordsList"></div>
                  </div>
                </div>
              </div>
            </section>
          </section>
        </template>

        <template v-else-if="view === 'report'">
          <section class="question-layout">
            <section class="question-main">
              <section class="analysis-layout">
                <header class="analysis-header">
                  <h2>本场综合报告</h2>
                  <p>
                    这是本场多模态模拟面试的简要综合报告，可从综合得分、技术能力、表达与沟通、逻辑与应变四个维度进行回顾。后续可以接入真实评估模型与详细点评。
                  </p>
                  <div class="analysis-actions">
                    <button class="btn btn-primary">导出本场报告（预留）</button>
                    <button class="btn btn-ghost btn-small" @click="goInterview">
                      再练一场面试
                    </button>
                    <button class="btn btn-ghost btn-small" @click="() => { view = 'analysis' }">
                      查看历史记录与能力曲线
                    </button>
                    <button class="btn btn-ghost btn-small" @click="goHome">
                      返回首页
                    </button>
                  </div>
                </header>

                <div class="analysis-content">
                  <div class="analysis-summary-card">
                    <h3>本场表现概览</h3>
                    <ul class="analysis-metrics">
                      <li>
                        <span>综合得分</span>
                        <strong>{{ analysisReport.overall }} 分</strong>
                      </li>
                      <li>
                        <span>技术能力</span>
                        <strong>{{ analysisReport.tech }} 分</strong>
                      </li>
                      <li>
                        <span>表达与沟通</span>
                        <strong>{{ analysisReport.communication }} 分</strong>
                      </li>
                      <li>
                        <span>逻辑与应变</span>
                        <strong>{{ analysisReport.logic }} 分</strong>
                      </li>
                    </ul>
                    <p class="analysis-note">
                      这里可以根据面试对话内容、语音情绪、回答结构等生成更加细致的优劣势分析和改进建议，例如“建议加强项目细节讲解”“注意控制语速与停顿节奏”等。
                    </p>
                  </div>

                  <div class="analysis-chart-card">
                    <h3>本场能力雷达说明（占位）</h3>
                    <div class="analysis-chart-placeholder">
                      <p>此处可以接入雷达图组件，用于展示本场在综合、技术、沟通、逻辑等维度的相对水平，并与目标岗位要求进行对比。</p>
                      <p>当前为占位描述，后续可与评估服务打通，生成真实图表。</p>
                    </div>
                  </div>
                </div>
              </section>
            </section>
          </section>
        </template>

        <template v-else-if="view === 'analysis'">
          <section class="question-layout">
            <section class="question-main">
              <section class="analysis-layout">
                <header class="analysis-header">
                  <h2>历史记录与能力曲线</h2>
                  <p>
                    这里汇总了你最近几次 AI 模拟面试的记录，并用能力曲线展示综合得分与各维度能力的变化趋势。后续可以接入真实评估结果与导出功能。
                  </p>
                  <div class="analysis-actions">
                    <button class="btn btn-ghost btn-small" @click="goInterview">
                      再练一场面试
                    </button>
                    <button class="btn btn-ghost btn-small" @click="goHome">
                      返回首页
                    </button>
                  </div>
                </header>

                <div class="analysis-content">
                  <div class="analysis-summary-card">
                    <h3>最近模拟面试记录</h3>
                    <ul class="history-list">
                      <li v-for="session in historySessions" :key="session.id" class="history-item">
                        <div class="history-meta">
                          <span class="history-date">{{ session.date }}</span>
                          <span class="history-tag">{{ session.position }} · {{ session.difficulty }}</span>
                        </div>
                        <div class="history-scores">
                          <span>综合 {{ session.overall }} 分</span>
                          <span>技术 {{ session.tech }} 分</span>
                          <span>沟通 {{ session.communication }} 分</span>
                          <span>逻辑 {{ session.logic }} 分</span>
                        </div>
                        <div class="history-bar">
                          <div class="history-bar-fill" :style="{ width: session.overall + '%' }"></div>
                        </div>
                      </li>
                    </ul>
                    <ul class="analysis-metrics">
                      <li>
                        <span>综合得分</span>
                        <strong>{{ analysisReport.overall }} 分</strong>
                      </li>
                      <li>
                        <span>技术能力</span>
                        <strong>{{ analysisReport.tech }} 分</strong>
                      </li>
                      <li>
                        <span>表达与沟通</span>
                        <strong>{{ analysisReport.communication }} 分</strong>
                      </li>
                      <li>
                        <span>逻辑与应变</span>
                        <strong>{{ analysisReport.logic }} 分</strong>
                      </li>
                    </ul>
                    <p class="analysis-note">
                      这里可以根据对话内容、语速与情绪、答案深度等自动生成更细致的点评与改进建议，并支持导出结构化 PDF 报告。
                    </p>
                  </div>

                  <div class="analysis-chart-card">
                    <h3>历次模拟面试能力曲线</h3>
                    <svg viewBox="0 0 100 100" class="analysis-chart-svg">
                      <!-- 网格与坐标 -->
                      <g class="grid-lines">
                        <line v-for="y in [20, 40, 60, 80]" :key="'grid-' + y" :x1="0" :x2="100" :y1="y" :y2="y" />
                      </g>
                      <!-- 综合得分 -->
                      <polyline :points="abilityChart.overall" class="line-overall" />
                      <!-- 技术 -->
                      <polyline :points="abilityChart.tech" class="line-tech" />
                      <!-- 沟通 -->
                      <polyline :points="abilityChart.communication" class="line-communication" />
                      <!-- 逻辑 -->
                      <polyline :points="abilityChart.logic" class="line-logic" />
                    </svg>
                    <div class="analysis-chart-legend">
                      <span class="legend-item legend-overall">综合</span>
                      <span class="legend-item legend-tech">技术</span>
                      <span class="legend-item legend-communication">沟通</span>
                      <span class="legend-item legend-logic">逻辑</span>
                    </div>
                  </div>
                </div>
              </section>
            </section>
          </section>
        </template>
      </main>

      <footer class="footer">
        <span>v1.0.0</span>
      </footer>
    </div>
  `,
};

// ================= 第三界面：多模态模拟面试逻辑 =================

const QUESTION_BANKS = {
  frontend: {
    junior: [
      { question: '请简单介绍一下你自己和你的技术背景。', keywords: ['html', 'css', 'javascript', 'vue', 'react', '项目'], topic: 'intro' },
      { question: '你熟悉哪些前端技术栈？请举例说明。', keywords: ['html', 'css', 'js', '框架', '组件'], topic: 'tech' },
      { question: '能谈谈你对Vue或React的理解吗？', keywords: ['vue', 'react', '组件化', '响应式'], topic: 'framework' },
      { question: '你如何进行前端性能优化？', keywords: ['性能', '加载', '缓存', '懒加载'], topic: 'performance' },
      { question: '感谢参与，面试结束。', topic: 'end' },
    ],
    middle: [
      { question: '请介绍你的技术成长路径和最有挑战的项目。', keywords: ['项目', '架构', '团队', '解决'], topic: 'intro' },
      { question: '你如何设计组件架构和状态管理？', keywords: ['架构', '状态', 'pinia', 'vuex'], topic: 'architecture' },
      { question: '遇到过哪些棘手的前端问题？如何解决？', keywords: ['问题', '调试', '性能', '兼容'], topic: 'problem' },
      { question: '你了解前端工程化吗？有何实践？', keywords: ['工程化', '打包', 'eslint', '测试'], topic: 'engineering' },
      { question: '面试结束，期待进一步沟通。', topic: 'end' },
    ],
    senior: [
      { question: '请分享你对前端架构设计的理解和实践。', keywords: ['架构', '微前端', '模块化'], topic: 'architecture' },
      { question: '如何带领团队做技术选型和基建？', keywords: ['团队', '选型', '规范'], topic: 'team' },
      { question: '如何保证代码质量和可维护性？', keywords: ['质量', '测试', '重构'], topic: 'quality' },
      { question: '对前端技术未来趋势有何看法？', keywords: ['趋势', '新技术'], topic: 'future' },
      { question: '感谢分享，我们会尽快反馈。', topic: 'end' },
    ],
  },
  backend: {
    junior: [
      { question: '请简单介绍你的编程背景和熟悉的后端语言。', keywords: ['java', 'python', 'go', '数据库'], topic: 'intro' },
      { question: '你了解RESTful API吗？如何设计接口？', keywords: ['rest', '接口', 'http'], topic: 'api' },
      { question: '你使用过哪些数据库？', keywords: ['mysql', 'redis', 'sql'], topic: 'database' },
      { question: '什么是事务？什么场景会用到？', keywords: ['事务', 'acid'], topic: 'transaction' },
      { question: '面试结束，谢谢。', topic: 'end' },
    ],
    middle: [
      { question: '请介绍你负责过的最复杂的后端系统。', keywords: ['架构', '微服务', '分布式'], topic: 'intro' },
      { question: '高并发场景下如何优化性能？', keywords: ['高并发', '缓存', 'redis', '异步'], topic: 'performance' },
      { question: '如何保证分布式系统一致性？', keywords: ['一致性', '消息', '事务'], topic: 'consistency' },
      { question: '如何考虑可扩展性和可维护性？', keywords: ['扩展', '解耦'], topic: 'design' },
      { question: '感谢，面试结束。', topic: 'end' },
    ],
    senior: [
      { question: '请分享你设计过的系统架构及关键决策。', keywords: ['架构', '决策'], topic: 'intro' },
      { question: '如何设计高可用、高并发的分布式系统？', keywords: ['高可用', '容灾', '限流'], topic: 'system' },
      { question: '如何推动技术升级和人才培养？', keywords: ['团队', '技术'], topic: 'team' },
      { question: '对后端技术未来有何预判？', keywords: ['趋势', '云原生'], topic: 'future' },
      { question: '期待深入交流，面试结束。', topic: 'end' },
    ],
  },
  fullstack: {
    junior: [
      { question: '请介绍你的全栈开发经验。', keywords: ['前端', '后端', '项目'], topic: 'intro' },
      { question: '前后端如何协作？如何定义接口规范？', keywords: ['接口', '协作'], topic: 'api' },
      { question: '了解前后端分离吗？优缺点？', keywords: ['分离', 'spa'], topic: 'mode' },
      { question: '面试结束，谢谢。', topic: 'end' },
    ],
    middle: [
      { question: '请分享你主导的完整项目。', keywords: ['项目', '架构'], topic: 'intro' },
      { question: '前后端技术选型如何平衡？', keywords: ['选型', '团队'], topic: 'choice' },
      { question: '如何保证代码质量和开发效率？', keywords: ['质量', '规范'], topic: 'quality' },
      { question: '感谢，面试结束。', topic: 'end' },
    ],
    senior: [
      { question: '请分享全栈架构经验。', keywords: ['架构'], topic: 'intro' },
      { question: '如何协调前后端团队高效协作？', keywords: ['协作', '团队'], topic: 'collab' },
      { question: '对全栈工程师成长有何建议？', keywords: ['成长', '学习'], topic: 'growth' },
      { question: '面试结束，谢谢。', topic: 'end' },
    ],
  },
  data: {
    junior: [
      { question: '请介绍你的数据分析背景和常用工具。', keywords: ['python', 'excel', 'sql'], topic: 'intro' },
      { question: '了解哪些数据分析方法？能举例吗？', keywords: ['统计', '可视化'], topic: 'method' },
      { question: '使用过SQL做数据查询吗？', keywords: ['sql', '聚合'], topic: 'sql' },
      { question: '面试结束。', topic: 'end' },
    ],
    middle: [
      { question: '请分享有影响力的数据分析项目。', keywords: ['项目', '指标'], topic: 'intro' },
      { question: '如何进行数据清洗和特征工程？', keywords: ['清洗', '特征'], topic: 'process' },
      { question: '数据可视化方面有何经验？', keywords: ['可视化', '报表'], topic: 'viz' },
      { question: '感谢，面试结束。', topic: 'end' },
    ],
    senior: [
      { question: '如何搭建数据体系和指标体系？', keywords: ['体系', '指标'], topic: 'intro' },
      { question: '如何驱动业务做数据驱动决策？', keywords: ['驱动', '协作'], topic: 'decision' },
      { question: '数据安全和隐私方面有何实践？', keywords: ['安全', '隐私'], topic: 'security' },
      { question: '期待交流，面试结束。', topic: 'end' },
    ],
  },
  product: {
    junior: [
      { question: '请介绍你理解的产品经理职责。', keywords: ['需求', '用户'], topic: 'intro' },
      { question: '做过什么产品相关的工作？', keywords: ['项目', '需求'], topic: 'project' },
      { question: '如何收集和梳理用户需求？', keywords: ['需求', '调研'], topic: 'requirement' },
      { question: '面试结束。', topic: 'end' },
    ],
    middle: [
      { question: '请分享你主导的产品及功能设计。', keywords: ['产品', '功能'], topic: 'intro' },
      { question: '如何做需求优先级排序？', keywords: ['优先级', '价值'], topic: 'priority' },
      { question: '如何与开发、设计协作推进项目？', keywords: ['协作', '沟通'], topic: 'collab' },
      { question: '感谢，面试结束。', topic: 'end' },
    ],
    senior: [
      { question: '如何制定产品战略和roadmap？', keywords: ['战略', 'roadmap'], topic: 'intro' },
      { question: '产品从0到1有哪些关键经验？', keywords: ['0到1', '验证'], topic: 'zero2one' },
      { question: '如何衡量产品成功？关注哪些指标？', keywords: ['指标', '留存'], topic: 'metrics' },
      { question: '期待交流，面试结束。', topic: 'end' },
    ],
  },
};

const FOLLOW_UPS = {
  default: ['能详细说说吗？', '能举个实际例子吗？', '遇到过什么挑战？如何解决？'],
  tech: ['学习路径是怎样的？', '能举个项目例子吗？'],
  project: ['项目规模多大？团队多少人？', '承担什么角色？'],
  problem: ['怎么定位问题的？', '有什么经验可总结？'],
};

const TECH_WORDS = ['vue', 'react', 'javascript', 'html', 'css', 'java', 'python', 'go', 'mysql', 'redis', '项目', '架构', '性能', '组件', 'api', '接口', '团队', '需求', '状态', '测试', '部署'];

function extractKeywords(text) {
  if (!text || typeof text !== 'string') return [];
  const lower = text.toLowerCase();
  return [...new Set(TECH_WORDS.filter((w) => lower.includes(w)))].slice(0, 6);
}

let state = {
  questions: [],
  currentIndex: 0,
  followUpCount: 0,
  timerInterval: null,
  duration: 0,
};

function getFollowUp(answer, topic) {
  const kw = extractKeywords(answer);
  if (kw.length === 0 || state.followUpCount >= 2) return null;
  const list = FOLLOW_UPS[topic] || FOLLOW_UPS.default;
  return { question: list[state.followUpCount % list.length], keywords: kw };
}

function mockStart(position, difficulty) {
  const bank = QUESTION_BANKS[position]?.[difficulty] || QUESTION_BANKS.frontend.middle;
  state.questions = bank;
  state.currentIndex = 0;
  state.followUpCount = 0;
  const posNames = { frontend: '前端', backend: '后端', fullstack: '全栈', data: '数据分析', product: '产品' };
  const diffNames = { junior: '初级', middle: '中级', senior: '高级' };
  return {
    welcome: `您好！欢迎参加${posNames[position] || '技术'}${diffNames[difficulty] || '中级'}面试。我会根据您的回答进行追问，请详细作答。开始吧！`,
    firstQuestion: state.questions[0].question,
    totalQuestions: state.questions.length,
  };
}

function mockMessage(userMsg, questionIndex) {
  const keywords = extractKeywords(userMsg);
  const cur = state.questions[questionIndex];
  const topic = cur?.topic || 'default';
  const followUp = getFollowUp(userMsg, topic);

  if (followUp && topic !== 'end') {
    state.followUpCount++;
    return {
      reply: followUp.question,
      keywords,
      nextIndex: questionIndex,
      completed: false,
    };
  }

  state.followUpCount = 0;
  const nextIndex = questionIndex + 1;
  const nextQ = state.questions[nextIndex];
  if (!nextQ) {
    return { reply: '感谢参与，面试结束。祝您好运！', keywords, nextIndex, completed: true };
  }
  return {
    reply: nextQ.question,
    keywords,
    nextIndex,
    completed: nextQ.topic === 'end',
  };
}

// ======= 界面逻辑（保持原生 DOM，按需初始化） =======

let conversationHistory = [];
let currentQuestionIndex = 0;
let totalQuestions = 5;
let inputMode = 'text';
let isRecording = false;
let recognition = null;
let recognitionTextVal = '';
let SpeechRec;
let interviewDomInited = false;

function addMessage(role, content) {
  conversationHistory.push({ role, content, time: new Date() });
  renderMessages();
}

function renderMessages() {
  const el = document.getElementById('messageList');
  if (!el) return;
  el.innerHTML = conversationHistory
    .map(
      (m) => `
                <div class="message ${m.role}">
                    <div class="message-avatar">${m.role === 'assistant' ? '🤖' : '👤'}</div>
                    <div class="message-content">
                        <div class="message-text">${m.content.replace(/\n/g, '<br>')}</div>
                        <div class="message-time">${m.time.toLocaleTimeString('zh-CN', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}</div>
                    </div>
                </div>
            `,
    )
    .join('');
  const roundsEl = document.getElementById('rounds');
  if (roundsEl) {
    roundsEl.textContent = String(conversationHistory.length);
  }
  el.scrollTop = el.scrollHeight;
}

function showTyping(show) {
  const el = document.getElementById('typingIndicator');
  if (!el) return;
  el.style.display = show ? 'flex' : 'none';
}

function updateKeywords(kw) {
  const el = document.getElementById('keywordsList');
  if (!el) return;
  if (kw && kw.length) {
    el.innerHTML = kw.map((k) => `<span class="keyword-tag">${k}</span>`).join('');
  } else {
    el.innerHTML = '<span class="no-keywords">暂无</span>';
  }
}

function formatTime(sec) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function startTimer() {
  state.duration = 0;
  const durationEl = document.getElementById('duration');
  if (durationEl) {
    durationEl.textContent = formatTime(0);
  }
  state.timerInterval = setInterval(() => {
    state.duration++;
    if (durationEl) {
      durationEl.textContent = formatTime(state.duration);
    }
  }, 1000);
}

function stopTimer() {
  if (state.timerInterval) {
    clearInterval(state.timerInterval);
    state.timerInterval = null;
  }
}

function startInterview() {
  const positionEl = document.getElementById('position');
  const difficultyEl = document.getElementById('difficulty');
  if (!positionEl || !difficultyEl) return;
  const position = positionEl.value;
  const difficulty = difficultyEl.value;
  const result = mockStart(position, difficulty);

  const welcomeScreen = document.getElementById('welcomeScreen');
  const inputContainer = document.getElementById('inputContainer');
  const interviewInfo = document.getElementById('interviewInfo');
  const sidebarRow = document.getElementById('sidebarRow');
  const textInput = document.getElementById('textInput');
  const sendBtn = document.getElementById('sendBtn');
  const voiceBtn = document.getElementById('voiceBtn');

  if (welcomeScreen) welcomeScreen.style.display = 'none';
  if (inputContainer) inputContainer.style.display = 'block';
  if (interviewInfo) interviewInfo.style.display = 'flex';
  if (sidebarRow) sidebarRow.style.display = 'flex';
  if (textInput) textInput.disabled = false;
  if (sendBtn) sendBtn.disabled = false;
  if (SpeechRec && voiceBtn) voiceBtn.disabled = false;

  conversationHistory = [];
  currentQuestionIndex = 0;
  totalQuestions = result.totalQuestions;

  addMessage('assistant', result.welcome);
  addMessage('assistant', result.firstQuestion);
  updateKeywords([]);
  startTimer();

  if ('speechSynthesis' in window) {
    const u = new SpeechSynthesisUtterance(result.firstQuestion);
    u.lang = 'zh-CN';
    window.speechSynthesis.speak(u);
  }
}

async function sendMessage(content) {
  const textInput = document.getElementById('textInput');
  const sendBtn = document.getElementById('sendBtn');
  const msg = content || (textInput && textInput.value.trim());
  if (!msg) return;

  addMessage('user', msg);
  if (textInput) textInput.value = '';

  if (textInput) textInput.disabled = true;
  if (sendBtn) sendBtn.disabled = true;
  showTyping(true);

  await new Promise((r) => setTimeout(r, 600 + Math.random() * 400));

  const res = mockMessage(msg, currentQuestionIndex);
  addMessage('assistant', res.reply);
  updateKeywords(res.keywords);
  currentQuestionIndex = res.nextIndex;

  if (res.completed) {
    endInterview(true);
  } else if ('speechSynthesis' in window) {
    const u = new SpeechSynthesisUtterance(res.reply);
    u.lang = 'zh-CN';
    window.speechSynthesis.speak(u);
  }

  showTyping(false);
  if (textInput) {
    textInput.disabled = false;
    textInput.focus();
  }
  if (sendBtn) sendBtn.disabled = false;
}

function endInterview(skipConfirm) {
  if (!skipConfirm && !window.confirm('确定结束面试？')) return;
  stopTimer();
  addMessage('assistant', '感谢参加AI模拟面试！祝您求职顺利。');
  const inputContainer = document.getElementById('inputContainer');
  const interviewInfo = document.getElementById('interviewInfo');
  if (inputContainer) inputContainer.style.display = 'none';
  if (interviewInfo) interviewInfo.style.display = 'none';
  if (window.__rootApp__) {
    window.__rootApp__.view = 'report';
  }
}

function startVoice() {
  const voiceBtn = document.getElementById('voiceBtn');
  if (!SpeechRec || isRecording || !voiceBtn) return;
  recognitionTextVal = '';
  const preview = document.getElementById('recognitionPreview');
  if (preview) preview.style.display = 'none';
  recognition.start();
  isRecording = true;
  voiceBtn.innerHTML = '<span class="pulse-dot"></span>正在录音...';
}

function stopVoice() {
  if (recognition && isRecording) {
    recognition.stop();
  }
}

function confirmVoice() {
  if (recognitionTextVal.trim()) {
    sendMessage(recognitionTextVal);
    recognitionTextVal = '';
    const preview = document.getElementById('recognitionPreview');
    if (preview) preview.style.display = 'none';
  }
}

function clearVoice() {
  recognitionTextVal = '';
  const preview = document.getElementById('recognitionPreview');
  if (preview) preview.style.display = 'none';
}

// 按需初始化 DOM 事件与语音识别，仅在第一次进入第三界面时调用
function initInterviewPage() {
  if (interviewDomInited) return;
  interviewDomInited = true;

  SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;

  // 输入 Tab 切换
  const tabBtns = document.querySelectorAll('.tab-btn');
  tabBtns.forEach((btn) => {
    btn.addEventListener('click', () => {
      tabBtns.forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      inputMode = btn.dataset.mode;
      const textArea = document.getElementById('textInputArea');
      const voiceArea = document.getElementById('voiceInputArea');
      if (textArea) textArea.style.display = inputMode === 'text' ? 'block' : 'none';
      if (voiceArea) voiceArea.style.display = inputMode === 'voice' ? 'block' : 'none';
    });
  });

  // 文字输入 Enter 发送
  const textInput = document.getElementById('textInput');
  if (textInput) {
    textInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });
  }

  const sendBtn = document.getElementById('sendBtn');
  if (sendBtn) {
    sendBtn.addEventListener('click', () => {
      sendMessage();
    });
  }

  // 语音识别初始化与事件
  const voiceBtn = document.getElementById('voiceBtn');
  if (SpeechRec && voiceBtn) {
    recognition = new SpeechRec();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = 'zh-CN';
    recognition.onresult = (e) => {
      let text = '';
      for (let i = e.resultIndex; i < e.results.length; i++) {
        text += e.results[i][0].transcript;
      }
      recognitionTextVal = text;
      const textEl = document.getElementById('recognitionText');
      const preview = document.getElementById('recognitionPreview');
      if (textEl) textEl.textContent = text;
      if (preview) preview.style.display = text ? 'block' : 'none';
    };
    recognition.onend = () => {
      isRecording = false;
      const btn = document.getElementById('voiceBtn');
      if (btn) btn.innerHTML = '🎙️ 按住说话';
    };

    voiceBtn.addEventListener('mousedown', (e) => {
      e.preventDefault();
      startVoice();
    });
    voiceBtn.addEventListener('mouseup', stopVoice);
    voiceBtn.addEventListener('mouseleave', stopVoice);
    voiceBtn.addEventListener('touchstart', (e) => {
      e.preventDefault();
      startVoice();
    });
    voiceBtn.addEventListener('touchend', (e) => {
      e.preventDefault();
      stopVoice();
    });
  }
}

// 导出挂载函数，避免与现有 Vue 根实例冲突
export function mountAiInterview(selector = '#ai-interview-app') {
  const app = createApp(App);
  const rootVm = app.mount(selector);
  window.__rootApp__ = rootVm;
  // 保存路由实例到全局，供 goToJob 使用
  window.__router__ = rootVm.$router;
  // 将部分方法暴露到 window，给模板中的内联事件调用
  window.startInterview = startInterview;
  window.endInterview = endInterview;
  window.confirmVoice = confirmVoice;
  window.clearVoice = clearVoice;
  window.initInterviewPage = initInterviewPage;
  return rootVm;
}

export { App as AiInterviewApp, initInterviewPage };
