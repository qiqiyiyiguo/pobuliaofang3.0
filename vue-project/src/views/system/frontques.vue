<template>
  <div class="question-bank-page">
    <!-- 筛选区域 -->
    <div class="filter-bar">
      <label for="category-select">题目类别：</label>
      <select
        id="category-select"
        v-model="selectedCategory"
        @change="handleCategoryChange"
      >
        <option :value="null">全部</option>
        <option
          v-for="cat in categories"
          :key="cat.id"
          :value="cat.id"
        >
          {{ cat.name }}
        </option>
      </select>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 列表区域 -->
    <div v-else class="question-list">
      <div v-if="questionList.length === 0" class="empty">暂无题目</div>
      <div
        v-for="item in questionList"
        :key="item.id"
        class="question-item"
      >
        <h3 class="question-title">{{ item.title || item.name }}</h3>
        <p class="question-desc">{{ item.content || item.description }}</p>
        <span class="question-type">{{ item.category || item.type_name }}</span>
      </div>
    </div>

    <!-- 分页组件 -->
    <div class="pagination" v-if="total > 0">
      <button
        :disabled="currentPage === 1"
        @click="goToPage(currentPage - 1)"
      >
        上一页
      </button>
      <span>{{ currentPage }} / {{ totalPages }}</span>
      <button
        :disabled="currentPage === totalPages"
        @click="goToPage(currentPage + 1)"
      >
        下一页
      </button>
      <span class="page-info">共 {{ total }} 条</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { getPythonQuestion } from '../../api/question.js'

// ---------- 数据 ----------
const categories = ref([])                // 类别列表
const selectedCategory = ref(null)        // 当前选中的类别ID (null 表示全部)
const questionList = ref([])              // 题目列表
const total = ref(0)                       // 总条数
const currentPage = ref(1)                // 当前页码
const pageSize = ref(10)                  // 每页条数
const loading = ref(false)                // 加载状态

// 计算总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// ---------- 获取题目类别 ----------
const fetchCategories = async () => {
  try {
    // 调用获取类型的接口
    const res = await axios.get('/api/question-types')
    if (res.data.code === 200) {
      categories.value = res.data.data
    } else {
      console.error('获取类别失败', res.data.message)
    }
  } catch (err) {
    console.error('请求类别出错', err)
  }
}

// ---------- 获取题目列表（使用 Python 接口）----------
const fetchQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    // 如果选择了类别，根据类别名称筛选
    if (selectedCategory.value) {
      const selectedCat = categories.value.find(c => c.id === selectedCategory.value)
      if (selectedCat) {
        params.category = selectedCat.name
      }
    }
    
    // 使用 Python 题目的 API
    const res = await getPythonQuestion(params)
    // 注意：你的 getPythonQuestion 返回的是 res.data（因为响应拦截器已经返回了 data）
    // 所以 res 就是 { list, total }
    if (res && res.list) {
      questionList.value = res.list
      total.value = res.total || 0
    } else {
      console.error('获取题目列表失败', res)
    }
  } catch (err) {
    console.error('请求题目列表出错', err)
  } finally {
    loading.value = false
  }
}

// 处理类别切换：重置页码并重新加载
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchQuestions()
}

// 跳转页面
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchQuestions()
}

// 初始化
onMounted(async () => {
  await fetchCategories()
  await fetchQuestions()
})
</script>

<style scoped>
.question-bank-page {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.filter-bar select {
  margin-left: 8px;
  padding: 5px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
  border: 1px dashed #ddd;
  border-radius: 4px;
}

.question-item {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
  background-color: #fff;
  transition: box-shadow 0.2s;
}

.question-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.question-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.question-desc {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.question-type {
  font-size: 12px;
  color: #1890ff;
  background-color: #e6f7ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.pagination button {
  padding: 5px 12px;
  margin: 0 5px;
  border: 1px solid #d9d9d9;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.pagination span {
  margin: 0 10px;
  font-size: 14px;
}

.page-info {
  margin-left: 15px;
  color: #666;
}
</style>