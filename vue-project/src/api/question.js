// api/question.js
import request from '../utils/request.js' // 根据实际路径调整

/**
 * 获取题目类型列表
 * @returns {Promise<Array>} 类型列表
 */


/**
 * 获取题库列表（支持分页和类型筛选）
 * @param {Object} params - 查询参数
 * @param {number} [params.type_id] - 题目类型ID（不传或传0表示全部）
 * @param {number} [params.page=1] - 当前页码
 * @param {number} [params.page_size=10] - 每页条数
 * @returns {Promise<{ total: number, list: Array }>} 题库列表及总数
 */
export function getJavaQuestion(params) {
  return request({
    url: '/question/Java',
    method: 'get',
    params
  })

}

  export function getWebQuestion(params) {
  return request({
    url: '/question/Web',
    method: 'get',
    params
  })
}

  export function getPythonQuestion(params) {
  return request({
    url: '/question/Python',
    method: 'get',
    params
  })
}

  export function getTestQuestion(params) {
  return request({
    url: '/question/Test',
    method: 'get',
    params
  })
}