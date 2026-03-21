<template>
  <div class="question-list-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="category"
        placeholder="请选择题目类别"
        clearable
        @change="handleCategoryChange"
      >
        <el-option
          v-for="item in categoryOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </div>

    <!-- 题目列表 -->
    <el-table
      v-loading="loading"
      :data="list"
      stripe
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="题目" min-width="200" />
      <el-table-column prop="category" label="类别" width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <!-- 可根据后端实际字段调整列 -->
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      />
    </div>
  </div>
</template>

<script>
// 导入四个模块的 API 函数（根据实际路径调整）
import {
  getJavaQuestion,
  getWebQuestion,
  getPythonQuestion,
  getTestQuestion
} from '../../api/question.js'

export default {
  name: 'QuestionListTemplate',
  props: {
    // 当前模块名称，由父路由或页面传入
    module: {
      type: String,
      default: 'Java'
    }
  },
  data() {
    return {
      // 筛选类别选项
      categoryOptions: [
        { label: '技术知识', value: '技术知识' },
        { label: '项目经历', value: '项目经历' },
        { label: '场景题', value: '场景题' },
        { label: '行为题', value: '行为题' }
      ],
      category: '', // 当前选中的类别，空字符串表示全部
      list: [],      // 题目列表
      total: 0,      // 总条数
      page: 1,       // 当前页码
      pageSize: 10,  // 每页条数
      loading: false // 加载状态
    }
  },
  computed: {
    // 根据模块名映射对应的 API 函数
    apiMap() {
      return {
        Java: getJavaQuestion,
        Web: getWebQuestion,
        Python: getPythonQuestion,
        Test: getTestQuestion
      }
    },
    currentApi() {
      return this.apiMap[this.module]
    }
  },
  watch: {
    // 当模块变化时，重置页码并重新获取数据（若模块切换）
    module: {
      handler() {
        this.page = 1
        this.fetchData()
      },
      immediate: true
    }
  },
  methods: {
    // 处理筛选变化：重置为第一页，然后获取数据
    handleCategoryChange() {
      this.page = 1
      this.fetchData()
    },
    // 每页条数变化
    handleSizeChange(size) {
      this.pageSize = size
      this.page = 1 // 通常改变每页条数后也回到第一页
      this.fetchData()
    },
    // 页码变化
    handleCurrentChange(page) {
      this.page = page
      this.fetchData()
    },
    // 获取数据
    async fetchData() {
  if (!this.currentApi) return
  this.loading = true
  try {
    const params = {
      page: this.page,
      pageSize: this.pageSize
    }
    if (this.category) {
      params.category = this.category
    }
    // 响应拦截器已经返回了 data 部分，所以这里直接拿到 { list, total }
    const data = await this.currentApi(params)
    // 直接使用 data，因为响应拦截器已经返回了 res.data
    this.list = data.list || []
    this.total = data.total || 0
  } catch (error) {
    console.error('获取题目列表失败', error)
    this.$message.error('获取数据失败')
  } finally {
    this.loading = false
    }
  }
  }
}
</script>

<style scoped>
.question-list-container {
  padding: 20px;
}
.filter-bar {
  display: flex;
  justify-content: flex-start;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>