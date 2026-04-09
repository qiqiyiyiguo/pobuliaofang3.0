import request from '../utils/request.js'

/**
 * 开始面试
 * POST /api/chat/start
 */
export function startInterview(data) {
  return request({
    url: '/api/chat/start',
    method: 'post',
    data
  })
}

/**
 * 发送消息
 * POST /api/chat/message
 */
export function sendMessage(data) {
  return request({
    url: '/api/chat/message',
    method: 'post',
    data
  })
}

/**
 * 结束面试
 * POST /api/chat/end
 */
export function endInterview(data) {
  return request({
    url: '/api/chat/end',
    method: 'post',
    data
  })
}

/**
 * 获取面试历史记录（从 ReportController 获取）
 * GET /api/report/history
 */
export function getInterviewHistory(params) {
  return request({
    url: '/api/report/history',  // 改为 /api/report/history
    method: 'get',
    params
  })
}

/**
 * 获取面试报告详情
 * GET /api/report/detail/{sessionId}
 */
export function getInterviewReport(sessionId) {
  return request({
    url: `/api/report/detail/${sessionId}`,
    method: 'get'
  })
}

/**
 * 获取能力曲线数据
 * GET /api/report/ability-curve
 */
export function getAbilityCurve(params) {
  return request({
    url: '/api/report/ability-curve',
    method: 'get',
    params
  })
}