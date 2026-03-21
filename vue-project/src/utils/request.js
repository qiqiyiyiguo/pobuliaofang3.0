// request.js
import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',  // 后端 API 基础地址
  timeout: 1000,                      // 请求超时时间（ms），可根据实际调整
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token（假设存储名为 'token'）
    const token = localStorage.getItem('token')
    if (token) {
      // 将 token 添加到请求头
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果返回的状态码是 200，说明接口请求成功，可以正常拿到数据
    const res = response.data
    // 这里可以根据后端约定的状态码进行进一步处理
    // 假设后端返回格式为 { code, message, data }
    if (res.code !== 200) {
      // 业务状态码非 200 视为错误
      console.error('业务错误:', res.message)
      // 可以统一提示错误信息，例如使用 Element Plus 的 ElMessage
      // ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 返回 data 部分，方便调用时直接获取数据
    return res.data
  },
  error => {
    // HTTP 状态码错误处理
    console.error('响应拦截器错误:', error)
    // 根据状态码进行不同提示
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，跳转登录页
          console.error('未授权，请重新登录')
          // 可以清除本地 token 并跳转
          // localStorage.removeItem('token')
          // window.location.href = '/login'
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`请求错误: ${error.response.status}`)
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      console.error('网络错误，请检查您的网络连接')
    } else {
      console.error('请求配置错误:', error.message)
    }
    return Promise.reject(error)
  }
)

export default request