<template>
  <div class="interview-page" style="min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px;">
    <!-- 右上角用户区域 -->
    <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;">
      <div v-if="!isLoggedIn" style="display: flex; gap: 12px;">
        <button @click="goToLogin" style="background: white; color: #667eea; border: none; padding: 8px 24px; border-radius: 24px; cursor: pointer; font-size: 14px; font-weight: 500; transition: all 0.2s;">
          登录
        </button>
        <button @click="goToRegister" style="background: transparent; color: white; border: 1px solid white; padding: 8px 24px; border-radius: 24px; cursor: pointer; font-size: 14px; font-weight: 500; transition: all 0.2s;">
          注册
        </button>
      </div>
      <div v-else style="display: flex; align-items: center; gap: 16px; background: rgba(255, 255, 255, 0.2); padding: 6px 20px; border-radius: 40px; backdrop-filter: blur(10px);">
        <span style="color: white; font-size: 14px;">欢迎, {{ username }}</span>
        <button @click="handleLogout" style="background: #f44336; color: white; border: none; padding: 6px 18px; border-radius: 24px; cursor: pointer; font-size: 13px; transition: all 0.2s;">
          退出
        </button>
      </div>
    </div>

    <div style="background: white; border-radius: 12px; padding: 30px; max-width: 800px; margin: 0 auto; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);">
      <h1 style="text-align: center; color: #333; margin-bottom: 30px;">🎤 AI面试官</h1>
      
      <!-- 面试设置 -->
      <div v-if="!interviewStarted">
        <div style="margin: 20px 0;">
          <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #555;">面试岗位：</label>
          <select v-model="selectedPosition" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px;">
            <option value="frontend">前端开发</option>
            <option value="backend">后端开发</option>
            <option value="fullstack">全栈开发</option>
            <option value="data">数据分析</option>
            <option value="product">产品经理</option>
          </select>
        </div>
        
        <div style="margin: 20px 0;">
          <label style="display: block; margin-bottom: 8px; font-weight: 500; color: #555;">难度：</label>
          <select v-model="selectedDifficulty" style="width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px;">
            <option value="junior">初级</option>
            <option value="middle">中级</option>
            <option value="senior">高级</option>
          </select>
        </div>
        
        <button @click="startInterview" style="width: 100%; padding: 14px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: 500; margin-top: 10px; transition: transform 0.2s;">
          开始面试
        </button>
      </div>
      
      <!-- 面试进行中 -->
      <div v-else>
        <div style="background: #f5f5f5; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
          <span style="color: #666;">⏱️ 面试时长: <strong>{{ formatTime(duration) }}</strong></span>
          <span style="color: #666;">💬 对话轮数: <strong>{{ conversationHistory.length }}</strong></span>
          <button @click="endInterview" style="background: #f44336; color: white; border: none; padding: 6px 16px; border-radius: 6px; cursor: pointer;">结束面试</button>
        </div>
        
        <div style="height: 400px; overflow-y: auto; border: 1px solid #e0e0e0; padding: 16px; border-radius: 8px; margin-bottom: 20px; background: #fafafa;">
          <div v-for="(msg, idx) in conversationHistory" :key="idx" :style="{textAlign: msg.role === 'user' ? 'right' : 'left', margin: '12px 0'}">
            <strong style="font-size: 13px; color: #888;">{{ msg.role === 'assistant' ? '🤖 AI面试官' : '👤 我的回答' }}:</strong>
            <div :style="{background: msg.role === 'user' ? '#667eea' : '#e8e8e8', color: msg.role === 'user' ? 'white' : '#333', padding: '10px 14px', borderRadius: '12px', display: 'inline-block', maxWidth: '80%', marginTop: '4px', wordWrap: 'break-word'}">
              {{ msg.content }}
            </div>
          </div>
          <div v-if="isTyping" style="text-align: left;">
            <strong style="font-size: 13px; color: #888;">🤖 AI面试官:</strong>
            <div style="background: #e8e8e8; padding: 10px 14px; border-radius: 12px; display: inline-block;">
              <span class="typing-dots">...</span>
            </div>
          </div>
        </div>
        
        <div style="display: flex; gap: 12px;">
          <textarea 
            v-model="currentMessage" 
            @keyup.enter="sendMessage"
            placeholder="输入你的回答... (按 Enter 发送)"
            style="flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; font-family: inherit; font-size: 14px; resize: vertical;"
            rows="3"
          ></textarea>
          <button @click="sendMessage" style="padding: 0 24px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 500;">
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWebQuestion, getJavaQuestion, getPythonQuestion, getTestQuestion } from '@/api/question.js'
import { startInterview as apiStartInterview, sendMessage as apiSendMessage, endInterview as apiEndInterview } from '@/api/interview.js'

const router = useRouter()

// 用户状态
const token = ref(localStorage.getItem('token'))
const isLoggedIn = computed(() => !!token.value)
const username = ref('')

// 面试相关
const selectedPosition = ref('frontend')
const selectedDifficulty = ref('middle')
const interviewStarted = ref(false)
const currentSessionId = ref(null)
const currentMessage = ref('')
const conversationHistory = ref([])
const isTyping = ref(false)
const duration = ref(0)
let timerInterval = null

// 加载用户信息
onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.nickname || user.username || '用户'
    } catch (e) {
      console.error('解析用户信息失败', e)
    }
  }
})

// 格式化时间
const formatTime = (seconds) => {
  if (typeof seconds === 'object') return ''
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 计时器
const startTimer = () => {
  duration.value = 0
  timerInterval = setInterval(() => {
    duration.value++
  }, 1000)
}

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// 跳转登录
const goToLogin = () => {
  router.push('/login')
}

// 跳转注册
const goToRegister = () => {
  router.push('/register')
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  token.value = null
  router.push('/login')
}

// 开始面试
const startInterview = async () => {
  const apiMap = {
    frontend: getWebQuestion,
    backend: getJavaQuestion,
    fullstack: getJavaQuestion,
    data: getPythonQuestion,
    product: getPythonQuestion
  }
  
  const apiFunc = apiMap[selectedPosition.value]
  if (!apiFunc) {
    alert('未知岗位')
    return
  }

  try {
    // 获取题目
    const result = await apiFunc({ page: 1, pageSize: 5 })
    const questions = result.list || []
    
    if (questions.length === 0) {
      alert('该岗位暂无题目')
      return
    }
    
    // 开始面试
    const startResult = await apiStartInterview({
      jobPosition: selectedPosition.value,
      session_id: null
    })
    
    if (startResult.code === 200) {
      currentSessionId.value = startResult.data.session_id
      interviewStarted.value = true
      conversationHistory.value = []
      conversationHistory.value.push({
        role: 'assistant',
        content: startResult.data.first_question || `欢迎参加${selectedPosition.value}岗位面试。请先介绍一下你自己`,
        time: new Date()
      })
      startTimer()
    } else {
      alert(startResult.message || '开始面试失败')
    }
  } catch (error) {
    console.error('开始面试失败:', error)
    alert('开始面试失败: ' + error.message)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!currentMessage.value.trim()) return
  
  const msg = currentMessage.value.trim()
  currentMessage.value = ''
  conversationHistory.value.push({
    role: 'user',
    content: msg,
    time: new Date()
  })
  
  isTyping.value = true
  
  try {
    const response = await apiSendMessage({
      session_id: currentSessionId.value,
      message: msg,
      duration: duration.value
    })
    
    if (response.code === 200) {
      const data = response.data
      conversationHistory.value.push({
        role: 'assistant',
        content: data.reply,
        time: new Date()
      })
      
      if (data.next_question && !data.is_end) {
        setTimeout(() => {
          conversationHistory.value.push({
            role: 'assistant',
            content: data.next_question,
            time: new Date()
          })
        }, 500)
      }
      
      if (data.is_end) {
        endInterview(true)
      }
    } else {
      conversationHistory.value.push({
        role: 'assistant',
        content: response.message || '抱歉，处理失败',
        time: new Date()
      })
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    conversationHistory.value.push({
      role: 'assistant',
      content: '网络错误: ' + error.message,
      time: new Date()
    })
  } finally {
    isTyping.value = false
  }
}

// 结束面试
const endInterview = async (skipConfirm) => {
  if (!skipConfirm && !confirm('确定结束面试吗？')) return
  stopTimer()
  
  try {
    if (currentSessionId.value) {
      const response = await apiEndInterview({
        session_id: currentSessionId.value
      })
      console.log('结束面试结果:', response)
      if (response.code === 200) {
        alert(`面试结束！得分: ${response.data.total_score}分`)
      }
    }
  } catch (error) {
    console.error('结束面试失败:', error)
  }
  
  interviewStarted.value = false
}
</script>

<style scoped>
.typing-dots {
  animation: typing 1.4s infinite;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
  }
  30% {
    opacity: 1;
  }
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

button:active {
  transform: translateY(0);
}
</style>