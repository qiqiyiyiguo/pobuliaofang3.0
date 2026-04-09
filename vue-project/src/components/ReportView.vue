<template>
  <section class="analysis-layout">
    <header class="analysis-header">
      <h2>本场综合报告</h2>
      <div class="analysis-actions">
        <button class="btn btn-primary" @click="exportReport">导出本场报告</button>
        <button class="btn btn-ghost btn-small" @click="$emit('go-interview')">再练一场面试</button>
        <button class="btn btn-ghost btn-small" @click="$emit('view-history')">查看历史记录</button>
        <button class="btn btn-ghost btn-small" @click="$emit('go-home')">返回首页</button>
      </div>
    </header>

    <div class="analysis-content">
      <!-- 综合评分卡片 -->
      <div class="analysis-summary-card">
        <h3>📊 本场表现概览</h3>
        <div class="score-grid">
          <div class="score-item" v-for="item in scoreItems" :key="item.key">
            <span class="score-label">{{ item.label }}</span>
            <strong class="score-value" :class="getScoreClass(item.value)">{{ item.value }} 分</strong>
            <div class="score-bar">
              <div class="score-bar-fill" :style="{ width: item.value + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细问答分析 -->
      <div class="analysis-details-card" v-if="qaList.length > 0">
        <h3>💬 问答详情与点评</h3>
        <div class="qa-list">
          <div v-for="(item, idx) in qaList" :key="idx" class="qa-item">
            <div class="qa-header">
              <span class="qa-number">问题 {{ idx + 1 }}</span>
              <span class="qa-score" :class="getScoreClass(item.score)">{{ item.score }} 分</span>
            </div>
            <div class="qa-question">
              <strong>问：</strong> {{ item.question }}
            </div>
            <div class="qa-answer">
              <strong>答：</strong> {{ item.answer || '未回答' }}
            </div>
            <div class="qa-evaluation">
              <strong>📝 AI 点评：</strong>
              <p>{{ item.evaluation }}</p>
            </div>
            <div class="qa-suggestion" v-if="item.score < 70">
              <strong>💡 改进建议：</strong>
              <p>{{ getSuggestionByScore(item.score) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 综合改进建议 -->
      <div class="analysis-suggestions-card">
        <h3>📌 综合改进建议</h3>
        <ul class="suggestions-list">
          <li v-if="analysisReport.tech < 60">🔧 技术能力有待提升，建议系统学习核心知识点，多做练习。</li>
          <li v-if="analysisReport.communication < 60">🗣️ 表达能力需要加强，建议多练习用STAR法则回答问题。</li>
          <li v-if="analysisReport.logic < 60">🧠 逻辑思维需要锻炼，建议先理清思路再回答，注意因果关系。</li>
          <li v-if="analysisReport.overall < 60">📚 整体表现有待提升，建议多进行模拟面试练习。</li>
          <li v-if="analysisReport.overall >= 60 && analysisReport.overall < 80">👍 表现良好，继续保持，重点提升薄弱环节。</li>
          <li v-if="analysisReport.overall >= 80">🎉 表现优秀！继续保持，可以挑战更高难度的问题。</li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  analysisReport: {
    type: Object,
    default: () => ({ overall: 0, tech: 0, communication: 0, logic: 0 })
  },
  conversationHistory: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['go-interview', 'view-history', 'go-home'])

const scoreItems = computed(() => [
  { key: 'overall', label: '综合得分', value: props.analysisReport.overall },
  { key: 'tech', label: '技术能力', value: props.analysisReport.tech },
  { key: 'communication', label: '表达与沟通', value: props.analysisReport.communication },
  { key: 'logic', label: '逻辑与应变', value: props.analysisReport.logic }
])

// 获取问答列表
const qaList = computed(() => {
  const list = []
  const assistantMessages = props.conversationHistory.filter(m => m.role === 'assistant')
  
  for (let i = 0; i < assistantMessages.length; i++) {
    const assistantMsg = assistantMessages[i]
    const userMsg = props.conversationHistory.find(m => m.role === 'user' && m.time < assistantMsg.time && 
      (!list.length || m.time > list[list.length - 1]?.time))
    
    list.push({
      question: assistantMsg.content,
      answer: userMsg?.content || '',
      evaluation: extractEvaluation(assistantMsg.content),
      score: extractScore(assistantMsg.content)
    })
  }
  return list
})

// 从AI回复中提取评价
function extractEvaluation(content) {
  if (content.includes('评价')) {
    const match = content.match(/评价[：:](.*?)(?=建议|$)/)
    if (match) return match[1].trim()
  }
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 从AI回复中提取分数（根据关键词）
function extractScore(content) {
  if (content.includes('详细') || content.includes('清晰') || content.includes('准确')) return 85
  if (content.includes('具体') || content.includes('完整')) return 75
  if (content.includes('简略') || content.includes('简短')) return 45
  if (content.includes('不知道') || content.includes('缺乏')) return 25
  return 60
}

function getScoreClass(score) {
  if (score >= 80) return 'score-excellent'
  if (score >= 60) return 'score-good'
  if (score >= 40) return 'score-average'
  return 'score-poor'
}

function getSuggestionByScore(score) {
  if (score < 30) return '建议重新学习基础知识，理解核心概念后再尝试。'
  if (score < 50) return '回答不够完整，建议补充更多细节和实际例子。'
  if (score < 70) return '回答基本正确，可以进一步深入讲解原理和细节。'
  return '回答很好，继续保持！'
}

function exportReport() {
  console.log('导出报告功能待实现')
}
</script>

<style scoped>
.analysis-layout {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.analysis-header {
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 16px;
  margin-bottom: 20px;
}

.analysis-header h2 {
  margin: 0 0 12px 0;
  color: #333;
}

.analysis-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.score-item {
  text-align: center;
}

.score-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.score-value {
  display: inline-block;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.score-bar {
  width: 100%;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.score-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.3s;
}

.score-excellent { color: #4caf50; }
.score-good { color: #8bc34a; }
.score-average { color: #ff9800; }
.score-poor { color: #f44336; }

.qa-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.qa-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.qa-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.qa-number {
  font-weight: bold;
  color: #667eea;
}

.qa-score {
  font-weight: bold;
  font-size: 18px;
}

.qa-question, .qa-answer, .qa-evaluation, .qa-suggestion {
  margin-bottom: 12px;
  line-height: 1.6;
}

.qa-evaluation p, .qa-suggestion p {
  margin: 4px 0 0 0;
  color: #555;
}

.suggestions-list {
  margin: 16px 0 0 0;
  padding-left: 20px;
}

.suggestions-list li {
  margin-bottom: 8px;
  line-height: 1.6;
  color: #555;
}

.analysis-summary-card, .analysis-details-card, .analysis-suggestions-card {
  margin-bottom: 24px;
}

.analysis-summary-card h3, .analysis-details-card h3, .analysis-suggestions-card h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 18px;
}
</style>