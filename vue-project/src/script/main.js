import { createApp } from 'vue';
import '../styles/style.css';
import {
  getInterviewHistory,
  startInterview as apiStartInterview,
  sendMessage as apiSendMessage,
  endInterview as apiEndInterview,
} from '@/api/interview.js';

let currentSessionId = null;
let conversationHistory = [];
let inputMode = 'text';
let isRecording = false;
let recognition = null;
let recognitionTextVal = '';
let SpeechRec;
let interviewDomInited = false;
let answerStartTime = 0; 
let currentSpeechUtterance = null;

let state = {
  timerInterval: null,
  duration: 0,
};

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
          <div class="message-avatar">${m.role === 'assistant' ? 'AI' : '我'}</div>
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
  if (roundsEl) roundsEl.textContent = String(conversationHistory.length);
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
  if (durationEl) durationEl.textContent = formatTime(0);

  state.timerInterval = setInterval(() => {
    state.duration += 1;
    if (durationEl) durationEl.textContent = formatTime(state.duration);
  }, 1000);
}

function stopTimer() {
  if (state.timerInterval) {
    clearInterval(state.timerInterval);
    state.timerInterval = null;
  }
}

async function startInterview() {
  const positionEl = document.getElementById('position');
  if (!positionEl) return;

  try {
    const result = await apiStartInterview({
      jobPosition: positionEl.value,
      session_id: null,
    });

    if (!result || !result.session_id) {
      alert(result?.message || '开始面试失败');
      return;
    }

    currentSessionId = result.session_id;

    if (window.__rootApp__) {
      window.__rootApp__.view = 'interview';
      await window.__rootApp__.$nextTick();
    }

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
    renderMessages();
    updateKeywords([]);
    startTimer();


    if (typeof answerStartTime !== 'undefined') {
      answerStartTime = Date.now();
    }
    // ========================================

    addMessage('assistant', result.first_question || '面试开始，请先做一个简短自我介绍。');
  } catch (error) {
    console.error('开始面试失败:', error);
    alert('开始面试失败: ' + error.message);
  }
}

async function retrieveKnowledge(query, jobPosition) {
  try {
    console.log('RAG检索请求:', { query, jobPosition });
    
    const token = localStorage.getItem('token');
    const response = await fetch('/api/knowledge/retrieve', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        query: query,
        jobPosition: jobPosition,
        topK: 3,
        minScore: 0.3
      })
    });
    
    const result = await response.json();
    console.log('RAG检索结果:', result);
    
    // 注意：后端返回格式是 { code, message, data }
    if (result.code === 200 && result.data && result.data.results && result.data.results.length > 0) {
      // 提取知识内容作为上下文
      const context = result.data.results.map(r => r.content).join('\n\n');
      console.log(`✅ 检索到 ${result.data.results.length} 条相关知识`);
      return context;
    }
    
    console.log('⚠️ 未检索到相关知识');
    return null;
  } catch (error) {
    console.error('❌ RAG检索失败:', error);
    return null;
  }
}

// =======  sendMessage 函数 =======
async function sendMessage(content) {
  const textInput = document.getElementById('textInput');
  const sendBtn = document.getElementById('sendBtn');
  const msg = content || (textInput && textInput.value.trim());
  if (!msg) return;

  // ========== 新增：计算本题回答耗时（秒） ==========
  let answerDuration = 0;
  if (answerStartTime > 0) {
    answerDuration = Math.floor((Date.now() - answerStartTime) / 1000);
    console.log(`本题回答耗时: ${answerDuration} 秒`);
  }
  // =================================================

  addMessage('user', msg);
  if (textInput) textInput.value = '';
  if (textInput) textInput.disabled = true;
  if (sendBtn) sendBtn.disabled = true;
  showTyping(true);

  // RAG检索
  const positionSelect = document.getElementById('position');
  const jobPosition = positionSelect ? positionSelect.value : 'backend';
  const knowledgeContext = await retrieveKnowledge(msg, jobPosition);

  try {
    const response = await apiSendMessage({
      session_id: currentSessionId,
      message: msg,
      context: knowledgeContext || '',
      duration: state.duration,        // 面试总时长
      answerDuration: answerDuration   // 新增：本题回答耗时
    });
    // =================================================
    
    console.log('发送消息结果:', response);
    
    if (response && response.reply) {
      addMessage('assistant', response.reply);
      updateKeywords([]);
      
      if (response.next_question && !response.is_end) {
        setTimeout(() => {
          addMessage('assistant', response.next_question);
        }, 500);
      }
      
      if (response.is_end) {
        endInterview(true);
      } else if ('speechSynthesis' in window) {
        speakText(response.reply);
      }
    } else {
      addMessage('assistant', response?.message || '抱歉，处理失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    addMessage('assistant', '网络错误: ' + error.message);
  } finally {
    showTyping(false);
    if (textInput) {
      textInput.disabled = false;
      textInput.focus();
    }
    if (sendBtn) sendBtn.disabled = false;
    
    // ========== 新增：重置计时，为下一题准备 ==========
    answerStartTime = Date.now();
    // ===============================================
  }
}

async function endInterview(skipConfirm) {
  if (!skipConfirm && !window.confirm('确定结束面试？')) return;

  stopCurrentSpeech();
  stopTimer();

  try {
    if (currentSessionId) {
      const response = await apiEndInterview({ session_id: currentSessionId });
      if (response && response.total_score !== undefined) {
        addMessage('assistant', `面试结束，你的得分：${response.total_score} 分`);
      }
    }
  } catch (error) {
    console.error('结束面试失败:', error);
  }

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
  if (!recognitionTextVal.trim()) return;
  sendMessage(recognitionTextVal);
  recognitionTextVal = '';
  const preview = document.getElementById('recognitionPreview');
  if (preview) preview.style.display = 'none';
}

function clearVoice() {
  recognitionTextVal = '';
  const preview = document.getElementById('recognitionPreview');
  if (preview) preview.style.display = 'none';
}

// 停止当前播放的语音
function stopCurrentSpeech() {
    if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
    }
    currentSpeechUtterance = null;
}

// 播放语音
function speakText(text) {
    // 先停止当前正在播放的语音
    stopCurrentSpeech();
    
    if ('speechSynthesis' in window) {
        const u = new SpeechSynthesisUtterance(text);
        u.lang = 'zh-CN';
        u.rate = 0.9;
        u.pitch = 1.0;
        
        // 尝试选择更好的中文语音
        const voices = window.speechSynthesis.getVoices();
        const chineseVoice = voices.find(v => v.lang === 'zh-CN' && v.name.includes('Tingting'));
        if (chineseVoice) u.voice = chineseVoice;
        
        currentSpeechUtterance = u;
        window.speechSynthesis.speak(u);
    }
}

function initInterviewPage() {
  if (interviewDomInited) return;
  interviewDomInited = true;

  SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;

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
  if (sendBtn) sendBtn.addEventListener('click', () => sendMessage());

  const voiceBtn = document.getElementById('voiceBtn');
  if (SpeechRec && voiceBtn) {
    recognition = new SpeechRec();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = 'zh-CN';

    recognition.onresult = (e) => {
      let text = '';
      for (let i = e.resultIndex; i < e.results.length; i += 1) {
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
      if (btn) btn.innerHTML = '按住说话';
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

const HOME_STAGE_CARDS = [
  {
    key: 'bank',
    title: '岗位题库',
    subtitle: '按岗位维度智能刷题',
    cardClass: 'stage-card-1',
    action(vm) {
      vm.goQuestion('bank');
    },
  },
  {
    key: 'interview',
    title: '多模态模拟面试',
    subtitle: '语音 / 文本互动面试',
    cardClass: 'stage-card-2',
    action(vm) {
      vm.goInterview();
    },
  },
  {
    key: 'introduce',
    title: 'INTRODUCE',
    subtitle: '介绍说明',
    cardClass: 'stage-card-3',
    action() {
      if (window.__router__) {
        window.__router__.push('/introduce');
      }
    },
  },
  {
    key: 'history',
    title: '历史记录与能力曲线',
    subtitle: '回看历次记录与能力变化',
    cardClass: 'stage-card-4',
    action(vm) {
      vm.goSidebar('history');
    },
  },
];

const App = {
  name: 'AiInterviewCoach',
  data() {
    return {
      view: 'home',
      homeCardOrder: [0, 1, 2, 3],
      homeTrackOffset: 0,
      homeTrackTransition: true,
      homeTrackAnimating: false,
      selectedTrack: '',
      questionTab: 'bank',
      interviewInited: false,
      analysisReport: {
        overall: 85,
        tech: 88,
        communication: 80,
        logic: 82,
      },
      historySessions: [],
      loadingHistory: false,
      resumeText: '',           
      selectedJobForResume: 'backend',  
      resumeParsing: false,     
      resumeParseResult: null,  
      skillGapResult: null,     
      showResumeModal: false,  
    };
  },
  computed: {
    homeCards() {
      return HOME_STAGE_CARDS;
    },
    homeLoopCards() {
      const cards = this.homeCardOrder.map((index) => this.homeCards[index]);
      if (!cards.length) return [];
      return [cards[cards.length - 1], ...cards, cards[0]];
    },
    homeTrackStyle() {
      return {
        transition: this.homeTrackTransition ? 'transform 0.5s ease' : 'none',
        transform: `translate3d(calc(var(--stage-track-base) + (${this.homeTrackOffset} * var(--stage-card-span))), 0, 0)`,
      };
    },
    trackLabel() {
      switch (this.selectedTrack) {
        case 'frontend': return 'Web 前端工程师题库';
        case 'backend': return 'Java 后端工程师题库';
        case 'algo': return 'Python 算法工程师题库';
        case 'test': return '测试工程师题库';
        default: return '';
      }
    },
    abilityChart() {
      const sessions = this.historySessions || [];
      if (!sessions.length) return { overall: '', tech: '', communication: '', logic: '' };
      const maxScore = 100;
      const step = sessions.length > 1 ? 100 / (sessions.length - 1) : 0;
      const toPoints = (key) => sessions.map((s, idx) => {
        const x = idx * step;
        const score = typeof s[key] === 'number' ? s[key] : 0;
        const y = 100 - (score / maxScore) * 90 - 5;
        return `${x},${y}`;
      }).join(' ');
      return {
        overall: toPoints('overall'),
        tech: toPoints('tech'),
        communication: toPoints('communication'),
        logic: toPoints('logic'),
      };
    },
    abilityDots() {
      const sessions = this.historySessions || [];
      if (!sessions.length) return { overall: [], tech: [], communication: [], logic: [] };
      const maxScore = 100;
      const step = sessions.length > 1 ? 100 / (sessions.length - 1) : 0;
      const toDots = (key) => sessions.map((session, idx) => {
        const x = idx * step;
        const score = typeof session[key] === 'number' ? session[key] : 0;
        const y = 100 - (score / maxScore) * 90 - 5;
        return { x, y, value: score };
      });
      return {
        overall: toDots('overall'),
        tech: toDots('tech'),
        communication: toDots('communication'),
        logic: toDots('logic'),
      };
    },
    resumeAnalysis() {
  const route = this.$route || {};
  return route.path === '/ai-interview' && route.query?.view === 'resume'
    ? 'butRouterDeeper'
    : 'butRouter';
},
  },
  mounted() {
    this.loadHistory();
  },
  methods: {
    goHome() {
      this.view = 'home';
      this.questionTab = 'bank';
    },
    goAuth(mode) {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      window.location.href = mode === 'login' ? '/login' : '/register';
    },
    goToJob(track) {
      const routeMap = {
        frontend: '/frontques',
        backend: '/javaques',
        algo: '/pythonques',
        test: '/testques',
      };
      const route = routeMap[track];
      if (route && window.__router__) {
        window.__router__.push(route);
      } else if (route) {
        window.location.href = route;
      }
    },
    goQuestion(tab = 'bank') {
      this.view = 'question';
      this.selectedTrack = '';
      this.questionTab = tab;
    },
    goInterview() {
      this.view = 'interview';
      this.questionTab = 'multimodal';
      this.$nextTick(() => {
        if (!this.interviewInited) {
          initInterviewPage();
          this.interviewInited = true;
        }
      });
    },
    goSidebar(section) {
      if (section === 'bank') {
        this.goQuestion('bank');
      }else if (section === 'resume') {
        this.view = 'resume';
        this.openResumeModal();
      } else if (section === 'multimodal') {
        this.goInterview();
      } else if (section === 'history') {
        this.view = 'analysis';
        this.loadHistory();
      }
    },
    prevHomeCard() {
      this.rotateHomeTrack('prev');
    },
    nextHomeCard() {
      this.rotateHomeTrack('next');
    },
    rotateHomeTrack(direction) {
      if (this.homeTrackAnimating) return;
      this.homeTrackAnimating = true;
      this.homeTrackTransition = true;
      this.homeTrackOffset = direction === 'next' ? -1 : 1;

      window.setTimeout(() => {
        if (direction === 'next') {
          this.homeCardOrder.push(this.homeCardOrder.shift());
        } else {
          this.homeCardOrder.unshift(this.homeCardOrder.pop());
        }
        this.homeTrackTransition = false;
        this.homeTrackOffset = 0;
        this.$nextTick(() => {
          window.requestAnimationFrame(() => {
            window.requestAnimationFrame(() => {
              this.homeTrackTransition = true;
              this.homeTrackAnimating = false;
            });
          });
        });
      }, 500);
    },
    activateHomeCard(card) {
      if (this.homeTrackAnimating || !card || typeof card.action !== 'function') return;
      card.action(this);
    },
    updateHomeButtonPointer(event) {
      const button = event.currentTarget.closest('.stage-card');
      if (!button) return;
      const rect = button.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      button.style.setProperty('--pointer-x', `${x}px`);
      button.style.setProperty('--pointer-y', `${y}px`);
    },
    resetHomeButtonPointer(event) {
      const button = event.currentTarget.closest('.stage-card');
      if (!button) return;
      button.style.removeProperty('--pointer-x');
      button.style.removeProperty('--pointer-y');
    },
    getHomeCardDisplayIndex(renderIndex) {
      const index = renderIndex - 1;
      const total = this.homeCardOrder.length;
      return (index + total) % total;
    },
    getHomeCardAriaLabel(card, renderIndex) {
      return `${card.title}，第 ${this.getHomeCardDisplayIndex(renderIndex) + 1} 项`;
    },
    isHomeCardActive(renderIndex) {
      return renderIndex === 2;
    },
    getHomeCardExtraClass(renderIndex) {
      if (renderIndex === 0 || renderIndex === this.homeLoopCards.length - 1) {
        return 'is-buffer';
      }
      return this.isHomeCardActive(renderIndex) ? 'is-active' : '';
    },
    async loadHistory() {
      this.loadingHistory = true;
      try {
        const response = await getInterviewHistory({ page: 1, pageSize: 10 });
        if (response && response.list) {
          const records = response.list || [];
          this.historySessions = records.map((record) => ({
            id: record.id,
            date: record.created_at ? record.created_at.split('T')[0] : new Date().toISOString().split('T')[0],
            position: this.getPositionName(record.job_position),
            difficulty: record.difficulty || '中级',
            overall: record.overall_score || 0,
            tech: record.tech_score || 0,
            communication: record.communication_score || 0,
            logic: record.logic_score || 0,
            sessionId: record.session_id,
          }));
          const latest = this.historySessions[0];
          if (latest) {
            this.analysisReport = {
              overall: latest.overall,
              tech: latest.tech,
              communication: latest.communication,
              logic: latest.logic,
            };
          }
        }
      } catch (error) {
        console.error('加载历史记录失败:', error);
      } finally {
        this.loadingHistory = false;
      }
    },
    getPositionName(positionCode) {
      const positionMap = {
        frontend: '前端开发',
        backend: '后端开发',
        fullstack: '全栈开发',
        data: '数据分析',
        product: '产品经理',
        algo: '算法工程师',
        test: '测试工程师',
      };
      return positionMap[positionCode] || positionCode || '未知岗位';
    },
    refreshHistory() {
      this.loadHistory();
    },
    getScoreClass(score) {
      if (score >= 80) return 'score-excellent';
      if (score >= 60) return 'score-good';
      if (score >= 40) return 'score-average';
      return 'score-poor';
    },
    exportReport() {
      console.log('导出报告功能待实现');
    },
  // 打开简历上传弹窗
openResumeModal() {
  this.showResumeModal = true;
  this.resumeText = '';
  this.resumeParseResult = null;
  this.skillGapResult = null;
},

// 关闭简历上传弹窗
closeResumeModal() {
  this.showResumeModal = false;
},

// 解析简历
async parseResume() {
  if (!this.resumeText.trim()) {
    alert('请填写简历内容');
    return;
  }
  
  this.resumeParsing = true;
  
  try {
    // 1. 调用简历解析接口
    const parseResponse = await fetch('/api/resume/parse', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        resumeContent: this.resumeText,
        jobPosition: this.selectedJobForResume
      })
    });
    
    const parseResult = await parseResponse.json();
    
    if (parseResult.code === 200) {
      this.resumeParseResult = parseResult.data;
      
      // 2. 调用能力缺口分析接口
      const gapResponse = await fetch('/api/resume/gap', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          skills: parseResult.data.skills || [],
          jobPosition: this.selectedJobForResume
        })
      });
      
      const gapResult = await gapResponse.json();
      
      if (gapResult.code === 200) {
        this.skillGapResult = gapResult.data;
      } else {
        alert(gapResult.message || '缺口分析失败');
      }
    } else {
      alert(parseResult.message || '简历解析失败');
    }
  } catch (error) {
    console.error('简历解析失败:', error);
    alert('简历解析失败: ' + error.message);
  } finally {
    this.resumeParsing = false;
  }
},

// 获取岗位显示名称
getJobPositionName(position) {
  const names = {
    'backend': 'Java后端开发',
    'frontend': 'Web前端开发',
    'algo': 'Python算法工程师',
    'test': '测试工程师'
  };
  return names[position] || position;
},

// 获取等级样式
getLevelClass(level) {
  if (level === '优秀') return 'level-excellent';
  if (level === '良好') return 'level-good';
  if (level === '中等') return 'level-average';
  return 'level-poor';
}
  },
  template: `
    <div :class="['page', { 'page-home': view === 'home' }]">
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
            <h1 class="hero-title">AI 模拟面试官</h1>
          </section>

          <section class="cards">
            <div class="row row-top">
              <button class="card" @click="goQuestion('bank')">
                <div
                  class="home-card-hit"
                  @pointermove="updateHomeButtonPointer"
                  @pointerleave="resetHomeButtonPointer"
                ></div>
                <div class="card-title">岗位题库</div>
                <div class="card-subtitle">按岗位维度智能刷题</div>
              </button>
              <button class="card" @click="goInterview">
                <div
                  class="home-card-hit"
                  @pointermove="updateHomeButtonPointer"
                  @pointerleave="resetHomeButtonPointer"
                ></div>
                <div class="card-title">多模态模拟面试</div>
                <div class="card-subtitle">语音 / 文本互动面试</div>
              </button>
            </div>

            <div class="row row-bottom">
              <button class="card card-introduce" @click="() => { window.__router__ && window.__router__.push('/introduce') }">
                <div
                  class="home-card-hit"
                  @pointermove="updateHomeButtonPointer"
                  @pointerleave="resetHomeButtonPointer"
                ></div>
                <div class="card-title">INTRODUCE</div>
                <div class="card-subtitle">介绍说明</div>
              </button>
              <button class="card" @click="goSidebar('history')">
                <div
                  class="home-card-hit"
                  @pointermove="updateHomeButtonPointer"
                  @pointerleave="resetHomeButtonPointer"
                ></div>
                <div class="card-title">历史记录与能力曲线</div>
                <div class="card-subtitle">回看历次记录与能力变化</div>
              </button>
            </div>
          </section>
        </template>

        <template v-else-if="view === 'resume'">
  <section class="question-layout">
    <section class="question-main">
      <div class="resume-analysis-container">
        <header class="analysis-header">
          <h2>简历分析与能力缺口</h2>
          <div class="analysis-actions">
            <button class="btn btn-ghost btn-small" @click="goHome">返回首页</button>
          </div>
        </header>

        <div class="resume-content">
          <!-- 左侧：简历输入区 -->
          <div class="resume-input-area">
            <h3>简历内容</h3>
            <textarea 
              v-model="resumeText" 
              class="resume-textarea" 
              rows="12" 
              placeholder="请在此处粘贴或输入简历内容，例如：&#10;&#10;张三，计算机科学与技术专业本科毕业，3年Java开发经验。熟练掌握Spring Boot、MySQL、Redis。参与过电商订单系统开发，负责核心模块设计与实现。"
            ></textarea>
            
            <div class="resume-options">
              <label>目标岗位：</label>
              <select v-model="selectedJobForResume" class="job-select">
                <option value="backend">Java后端开发</option>
                <option value="frontend">Web前端开发</option>
                <option value="algo">Python算法工程师</option>
                <option value="test">测试工程师</option>
              </select>
              <button class="btn btn-primary" @click="parseResume" :disabled="resumeParsing">
                {{ resumeParsing ? '分析中...' : '开始分析' }}
              </button>
            </div>
          </div>

          <!-- 右侧：分析结果区 -->
          <div class="resume-result-area" v-if="resumeParseResult">
            <h3>分析结果</h3>
            
            <!-- 技能标签 -->
            <div class="result-section">
              <h4>技能标签</h4>
              <div class="skill-tags">
                <span v-for="skill in resumeParseResult.skills" :key="skill" class="skill-tag">
                  {{ skill }}
                </span>
              </div>
            </div>

            <!-- 项目经验 -->
            <div class="result-section" v-if="resumeParseResult.projects && resumeParseResult.projects.length">
              <h4>项目经验</h4>
              <ul class="project-list">
                <li v-for="project in resumeParseResult.projects" :key="project">{{ project }}</li>
              </ul>
            </div>

            <!-- 学历信息 -->
            <div class="result-section" v-if="resumeParseResult.education">
              <h4>学历信息</h4>
              <p>{{ resumeParseResult.education }}</p>
            </div>

            <!-- 工作年限 -->
            <div class="result-section" v-if="resumeParseResult.experienceYears">
              <h4>工作年限</h4>
              <p>{{ resumeParseResult.experienceYears }} 年</p>
            </div>

            <!-- 能力缺口分析 -->
            <div class="result-section" v-if="skillGapResult">
              <h4>能力缺口分析</h4>
              <div class="gap-score">
                <span class="gap-label">岗位匹配度：</span>
                <span class="gap-value" :class="getLevelClass(skillGapResult.level)">
                  {{ skillGapResult.matchScore }}%
                </span>
                <span class="gap-level">{{ skillGapResult.level }}</span>
              </div>

              <!-- 已掌握的技能 -->
              <div class="matched-skills" v-if="skillGapResult.matchedRequired && skillGapResult.matchedRequired.length">
                <h5>已掌握</h5>
                <div class="skill-tags">
                  <span v-for="skill in skillGapResult.matchedRequired" :key="skill" class="skill-tag matched">
                    {{ skill }}
                  </span>
                </div>
              </div>

              <!-- 缺失的技能 -->
              <div class="missing-skills" v-if="skillGapResult.missingRequired && skillGapResult.missingRequired.length">
                <h5>待提升</h5>
                <div class="skill-tags">
                  <span v-for="skill in skillGapResult.missingRequired" :key="skill" class="skill-tag missing">
                    {{ skill }}
                  </span>
                </div>
              </div>

              <!-- 学习建议 -->
              <div class="suggestions-list" v-if="skillGapResult.suggestions && skillGapResult.suggestions.length">
                <h5>学习建议</h5>
                <ul>
                  <li v-for="(sug, idx) in skillGapResult.suggestions" :key="idx">
                    <strong>{{ sug.skill }}</strong>：{{ sug.suggestion }}
                  </li>
                </ul>
              </div>
            </div>
          </div>
          
          <!-- 未分析时的提示 -->
          <div class="resume-result-area empty" v-else>
            <div class="empty-placeholder">
              <p>输入简历内容后点击"开始分析"</p>
              <p>系统将自动提取技能、项目经验，并进行能力缺口分析</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </section>
</template>

        <template v-else-if="view === 'question'">
          <section class="question-layout">
            <section class="question-main">
              <header class="question-header">
                <h2>岗位题库</h2>
                <p>选择目标方向，进入对应题库开始练习。</p>
                <button class="btn btn-ghost btn-small question-back" @click="goHome">返回首页</button>
              </header>
              <div class="question-role-grid">
                <button class="role-card" @click="goToJob('frontend')">
                  <div class="role-tag">Web 前端</div>
                  <div class="role-title">Web 前端 / H5 / 小程序</div>
                  <p class="role-desc">覆盖 HTML、CSS、JavaScript、框架与工程化相关问题。</p>
                </button>
                <button class="role-card" @click="goToJob('backend')">
                  <div class="role-tag">Java 后端</div>
                  <div class="role-title">Java 后端 / 服务端开发</div>
                  <p class="role-desc">包含 Java、Spring、数据库、缓存与接口设计相关问题。</p>
                </button>
                <button class="role-card" @click="goToJob('algo')">
                  <div class="role-tag">Python 算法</div>
                  <div class="role-title">数据分析 / 算法 / AI 基础</div>
                  <p class="role-desc">覆盖 Python、算法思维、数据处理与 AI 入门知识。</p>
                </button>
                <button class="role-card" @click="goToJob('test')">
                  <div class="role-tag">测试工程师</div>
                  <div class="role-title">测试 / 自动化 / 质量保障</div>
                  <p class="role-desc">聚焦测试理论、用例设计、自动化与质量保障能力。</p>
                </button>
              </div>
            </section>
          </section>
        </template>

        <template v-else-if="view === 'interview'">
          <section class="question-layout">
            <section class="question-main">
              <div class="interview-shell-header">
                <button class="btn btn-ghost btn-small" @click="goHome">返回首页</button>
              </div>
              <div class="app-container">
                <header class="app-header">
                  <h1>AI 模拟面试</h1>
                  <p class="subtitle">选择岗位后开始一场文本或语音互动的模拟面试。</p>
                  <div class="interview-info" id="interviewInfo" style="display: none">
                    <span class="info-item">面试时长: <span id="duration">00:00</span></span>
                    <span class="info-item">对话轮次: <span id="rounds">0</span></span>
                    <button class="btn btn-danger" onclick="endInterview()">结束面试</button>
                  </div>
                </header>
                <div class="chat-messages" id="chatMessages">
                  <div class="welcome-screen" id="welcomeScreen">
                    <div class="welcome-content">
                      <h2>开始你的 AI 模拟面试</h2>
                      <p>选择岗位与难度后，系统会自动生成首个面试问题。</p>
                      <div class="form-group">
                        <label>岗位方向</label>
                        <select id="position" class="form-control">
                          <option value="frontend">前端开发</option>
                          <option value="backend">后端开发</option>
                          <option value="fullstack">全栈开发</option>
                          <option value="data">数据分析</option>
                          <option value="product">产品经理</option>
                        </select>
                      </div>
                      <div class="form-group">
                        <label>面试难度</label>
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
                    <div class="message-avatar">AI</div>
                    <div class="message-content">
                      <div class="typing-indicator"><span></span><span></span><span></span></div>
                    </div>
                  </div>
                </div>
                <div class="input-container" id="inputContainer" style="display: none">
                  <div class="input-tabs">
                    <button class="tab-btn active" data-mode="text">文本输入</button>
                    <button class="tab-btn" data-mode="voice">语音输入</button>
                  </div>
                  <div id="textInputArea">
                    <div class="text-input-wrapper">
                      <textarea id="textInput" class="text-input" rows="3" placeholder="请输入你的回答..." disabled></textarea>
                      <button class="btn btn-primary send-btn" id="sendBtn" disabled>发送</button>
                    </div>
                  </div>
                  <div id="voiceInputArea" style="display: none">
                    <div class="voice-input-wrapper">
                      <button class="voice-btn" id="voiceBtn" disabled>按住说话</button>
                      <div class="recognition-preview" id="recognitionPreview" style="display: none">
                        <p>识别结果: <span id="recognitionText"></span></p>
                        <div class="recognition-actions">
                          <button class="btn btn-primary" style="padding: 8px 16px" onclick="confirmVoice()">确认发送</button>
                          <button class="btn btn-danger" style="padding: 8px 16px" onclick="clearVoice()">清空</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div id="sidebarRow" class="sidebar-row" style="display: none">
                  <div class="sidebar-section">
                    <h3>关键词</h3>
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
                  <h2>面试报告</h2>
                  <div class="analysis-actions">
                    <button class="btn btn-primary" @click="exportReport">导出报告</button>
                    <button class="btn btn-ghost btn-small" @click="goInterview">再次面试</button>
                    <button class="btn btn-ghost btn-small" @click="() => { view = 'analysis' }">查看历史</button>
                    <button class="btn btn-ghost btn-small" @click="goHome">返回首页</button>
                  </div>
                </header>
                <div class="analysis-content">
                  <div class="analysis-summary-card">
                    <h3>综合评分</h3>
                    <div class="score-grid">
                      <div class="score-item">
                        <span class="score-label">综合得分</span>
                        <strong class="score-value" :class="getScoreClass(analysisReport.overall)">{{ analysisReport.overall }} 分</strong>
                        <div class="score-bar"><div class="score-bar-fill" :style="{ width: analysisReport.overall + '%' }"></div></div>
                      </div>
                      <div class="score-item">
                        <span class="score-label">技术能力</span>
                        <strong class="score-value" :class="getScoreClass(analysisReport.tech)">{{ analysisReport.tech }} 分</strong>
                        <div class="score-bar"><div class="score-bar-fill" :style="{ width: analysisReport.tech + '%' }"></div></div>
                      </div>
                      <div class="score-item">
                        <span class="score-label">沟通表达</span>
                        <strong class="score-value" :class="getScoreClass(analysisReport.communication)">{{ analysisReport.communication }} 分</strong>
                        <div class="score-bar"><div class="score-bar-fill" :style="{ width: analysisReport.communication + '%' }"></div></div>
                      </div>
                      <div class="score-item">
                        <span class="score-label">逻辑思维</span>
                        <strong class="score-value" :class="getScoreClass(analysisReport.logic)">{{ analysisReport.logic }} 分</strong>
                        <div class="score-bar"><div class="score-bar-fill" :style="{ width: analysisReport.logic + '%' }"></div></div>
                      </div>
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
                  <div class="analysis-actions">
                    <button class="btn btn-ghost btn-small" @click="goInterview">开始面试</button>
                    <button class="btn btn-ghost btn-small" @click="goHome">返回首页</button>
                  </div>
                </header>
                <div class="analysis-content">
                  <div class="analysis-summary-card">
                    <h3>最近面试记录</h3>
                    <ul class="history-list">
                      <li v-for="session in historySessions" :key="session.id" class="history-item">
                        <div class="history-meta">
                          <span class="history-date">{{ session.date }}</span>
                          <span class="history-tag">{{ session.position }} / {{ session.difficulty }}</span>
                        </div>
                        <div class="history-scores">
                          <span>综合 {{ session.overall }} 分</span>
                          <span>技术 {{ session.tech }} 分</span>
                          <span>沟通 {{ session.communication }} 分</span>
                          <span>逻辑 {{ session.logic }} 分</span>
                        </div>
                      </li>
                    </ul>
                  </div>
                  <div class="analysis-chart-card">
                    <h3>能力曲线</h3>
                    <svg viewBox="0 0 100 100" class="analysis-chart-svg">
                      <defs>
                        <filter id="chartGlow" x="-30%" y="-30%" width="160%" height="160%">
                          <feGaussianBlur stdDeviation="1.2" result="blur" />
                          <feMerge>
                            <feMergeNode in="blur" />
                            <feMergeNode in="SourceGraphic" />
                          </feMerge>
                        </filter>
                      </defs>
                      <g class="grid-lines">
                        <line v-for="y in [20, 40, 60, 80]" :key="'grid-' + y" :x1="8" :x2="96" :y1="y" :y2="y" />
                      </g>
                      <g class="axis-labels">
                        <text v-for="y in [20, 40, 60, 80]" :key="'label-' + y" x="2" :y="y + 1.5">{{ 100 - y }}</text>
                      </g>
                      <polyline :points="abilityChart.overall" class="line-overall" filter="url(#chartGlow)" />
                      <polyline :points="abilityChart.tech" class="line-tech" filter="url(#chartGlow)" />
                      <polyline :points="abilityChart.communication" class="line-communication" filter="url(#chartGlow)" />
                      <polyline :points="abilityChart.logic" class="line-logic" filter="url(#chartGlow)" />
                      <g class="chart-dots">
                        <circle v-for="(dot, index) in abilityDots.overall" :key="'overall-dot-' + index" :cx="dot.x" :cy="dot.y" r="1.6" class="dot-overall" />
                        <circle v-for="(dot, index) in abilityDots.tech" :key="'tech-dot-' + index" :cx="dot.x" :cy="dot.y" r="1.6" class="dot-tech" />
                        <circle v-for="(dot, index) in abilityDots.communication" :key="'communication-dot-' + index" :cx="dot.x" :cy="dot.y" r="1.6" class="dot-communication" />
                        <circle v-for="(dot, index) in abilityDots.logic" :key="'logic-dot-' + index" :cx="dot.x" :cy="dot.y" r="1.6" class="dot-logic" />
                      </g>
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
      <footer class="footer"><span>v1.0.0</span></footer>
    </div>
  `,
};

export function mountAiInterview(selector = '#ai-interview-app', routerInstance = null) {
  const app = createApp(App);
  const rootVm = app.mount(selector);
  window.__rootApp__ = rootVm;
  window.__router__ = routerInstance || rootVm.$router || window.__router__;
  window.startInterview = startInterview;
  window.endInterview = endInterview;
  window.confirmVoice = confirmVoice;
  window.clearVoice = clearVoice;
  window.initInterviewPage = initInterviewPage;
  return rootVm;
}

export { App as AiInterviewApp, initInterviewPage };
