<template>
  <div class="interview-coach-container">
    <!-- 面试设置页面 -->
    <div v-if="!interviewStarted" class="setup-container">
      <h1>🎤 AI面试官</h1>
      <p class="subtitle">多模态交互 · 语音/文字输入 · 智能追问</p>
      
      <div class="form-group">
        <label>面试岗位：</label>
        <select v-model="selectedPosition" class="form-control">
          <option value="frontend">前端开发</option>
          <option value="backend">后端开发</option>
          <option value="fullstack">全栈开发</option>
          <option value="data">数据分析</option>
          <option value="product">产品经理</option>
        </select>
      </div>
      
      <div class="form-group">
        <label>难度：</label>
        <select v-model="selectedDifficulty" class="form-control">
          <option value="junior">初级</option>
          <option value="middle">中级</option>
          <option value="senior">高级</option>
        </select>
      </div>
      
      <button class="start-btn" @click="startInterview">开始面试</button>
    </div>

    <!-- 面试进行中页面 -->
    <div v-else class="interview-container">
      <div class="interview-header">
        <div class="interview-info">
          <span>面试时长: {{ formatTime(duration) }}</span>
          <span>对话轮数: {{ conversationHistory.length }}</span>
          <button class="end-btn" @click="endInterview">结束面试</button>
        </div>
      </div>
      
      <div class="chat-area" ref="chatArea">
        <div v-for="(msg, idx) in conversationHistory" :key="idx" :class="['message', msg.role]">
          <div class="message-avatar">{{ msg.role === 'assistant' ? '🤖' : '👤' }}</div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
        <div v-if="isTyping" class="message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-indicator">正在输入...</div>
          </div>
        </div>
      </div>
      
      <div class="input-area">
        <textarea 
          v-model="currentMessage" 
          @keyup.enter="sendMessage"
          placeholder="输入你的回答... (Enter发送)"
          rows="3"
        ></textarea>
        <button class="send-btn" @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import { getJavaQuestion, getWebQuestion, getPythonQuestion, getTestQuestion } from '../api/question.js'
import { startInterview, sendMessage, endInterview } from '../api/interview.js'

export default {
  name: 'AiInterviewCoach',
  data() {
    return {
      selectedPosition: 'frontend',
      selectedDifficulty: 'middle',
      interviewStarted: false,
      currentSessionId: null,
      currentMessage: '',
      conversationHistory: [],
      isTyping: false,
      duration: 0,
      timerInterval: null
    }
  },
  beforeUnmount() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval)
    }
  },
  methods: {
    formatTime(seconds) {
      if (typeof seconds === 'object') {
        return seconds.toLocaleTimeString()
      }
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    startTimer() {
      this.duration = 0
      this.timerInterval = setInterval(() => {
        this.duration++
      }, 1000)
    },
    stopTimer() {
      if (this.timerInterval) {
        clearInterval(this.timerInterval)
        this.timerInterval = null
      }
    },
    async startInterview() {
      const apiMap = {
        frontend: getWebQuestion,
        backend: getJavaQuestion,
        fullstack: getJavaQuestion,
        data: getPythonQuestion,
        product: getPythonQuestion
      }
      
      const apiFunc = apiMap[this.selectedPosition]
      if (!apiFunc) {
        alert('未知岗位')
        return
      }

      try {
        // 1. 获取题目
        const result = await apiFunc({ page: 1, pageSize: 5 })
        const questions = result.list || []
        
        if (questions.length === 0) {
          alert('该岗位暂无题目')
          return
        }
        
        // 2. 调用后端开始面试接口
        const startResult = await startInterview({
          jobPosition: this.selectedPosition,
          session_id: null
        })
        
        console.log('开始面试结果:', startResult)
        
        if (startResult.code === 200) {
          this.currentSessionId = startResult.data.session_id
          this.interviewStarted = true
          this.conversationHistory = []
          this.conversationHistory.push({
            role: 'assistant',
            content: startResult.data.first_question || `欢迎参加${this.selectedPosition}岗位面试。请先介绍一下你自己`,
            time: new Date()
          })
          this.startTimer()
          
          // 滚动到底部
          this.$nextTick(() => {
            if (this.$refs.chatArea) {
              this.$refs.chatArea.scrollTop = this.$refs.chatArea.scrollHeight
            }
          })
        } else {
          alert(startResult.message || '开始面试失败')
        }
      } catch (error) {
        console.error('开始面试失败:', error)
        alert('开始面试失败: ' + error.message)
      }
    },
    async sendMessage() {
      if (!this.currentMessage.trim()) return
      
      const msg = this.currentMessage.trim()
      this.currentMessage = ''
      this.conversationHistory.push({
        role: 'user',
        content: msg,
        time: new Date()
      })
      
      this.isTyping = true
      
      try {
        const response = await sendMessage({
          session_id: this.currentSessionId,
          message: msg,
          duration: this.duration
        })
        
        console.log('发送消息结果:', response)
        
        if (response.code === 200) {
          const data = response.data
          
          // 添加AI回复
          this.conversationHistory.push({
            role: 'assistant',
            content: data.reply,
            time: new Date()
          })
          
          // 如果有下一题且未结束，添加下一题
          if (data.next_question && !data.is_end) {
            setTimeout(() => {
              this.conversationHistory.push({
                role: 'assistant',
                content: data.next_question,
                time: new Date()
              })
              this.$nextTick(() => {
                if (this.$refs.chatArea) {
                  this.$refs.chatArea.scrollTop = this.$refs.chatArea.scrollHeight
                }
              })
            }, 500)
          }
          
          // 检查是否结束
          if (data.is_end) {
            await this.endInterviewAndGetReport()
          }
        } else {
          this.conversationHistory.push({
            role: 'assistant',
            content: response.message || '抱歉，处理失败',
            time: new Date()
          })
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.conversationHistory.push({
          role: 'assistant',
          content: '网络错误: ' + error.message,
          time: new Date()
        })
      } finally {
        this.isTyping = false
        this.$nextTick(() => {
          if (this.$refs.chatArea) {
            this.$refs.chatArea.scrollTop = this.$refs.chatArea.scrollHeight
          }
        })
      }
    },
    async endInterviewAndGetReport() {
      this.stopTimer()
      
      try {
        const response = await endInterview({
          session_id: this.currentSessionId
        })
        
        console.log('结束面试结果:', response)
        
        if (response.code === 200) {
          const data = response.data
          alert(`面试结束！您的得分: ${data.total_score}分`)
        }
      } catch (error) {
        console.error('结束面试失败:', error)
      }
      
      // 返回首页
      this.$router.push('/home')
    },
    async endInterview() {
      if (confirm('确定结束面试吗？')) {
        await this.endInterviewAndGetReport()
      }
    }
  }
}
</script>

<style scoped>
.interview-coach-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.setup-container {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.setup-container h1 {
  margin-bottom: 10px;
  color: #333;
}

.subtitle {
  color: #666;
  margin-bottom: 30px;
}

.form-group {
  margin: 20px 0;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.start-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 20px;
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.interview-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  height: 70vh;
}

.interview-header {
  background: #f5f5f5;
  border-bottom: 1px solid #ddd;
}

.interview-info {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  font-size: 14px;
  color: #666;
}

.end-btn {
  background: #f44336;
  color: white;
  border: none;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 15px;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  font-size: 18px;
}

.message.user .message-avatar {
  order: 2;
  margin-left: 10px;
  margin-right: 0;
}

.message-content {
  max-width: 70%;
  background: #f0f0f0;
  padding: 10px 15px;
  border-radius: 18px;
}

.message.user .message-content {
  background: #667eea;
  color: white;
}

.message-text {
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 5px;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.typing-indicator {
  color: #999;
  font-style: italic;
}

.input-area {
  padding: 20px;
  border-top: 1px solid #ddd;
  display: flex;
  gap: 10px;
}

.input-area textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
}

.send-btn {
  padding: 0 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.send-btn:hover {
  background: #5a67d8;
}
</style>