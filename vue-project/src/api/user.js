// api/user.js 此文件仅作示例 请勿直接使用!
import request from '../utils/request.js'  // 根据实际路径调整

/**
 * 用户登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise} 返回用户信息和 token
 */
export function login(username, password) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data: {
      username,
      password
    }
  })
}

/**
 * 用户注册
 * @param {Object} userInfo - 注册信息（包含用户名、密码、邮箱等）
 * @returns {Promise} 注册结果
 */
export function register(userInfo) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data: userInfo
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 用户详情
 */
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Object} userInfo - 需要更新的字段（如昵称、头像等）
 * @returns {Promise} 更新结果
 */
export function updateUserInfo(userInfo) {
  return request({
    url: '/api/user/update',
    method: 'put',
    data: userInfo
  })
}

/**
 * 修改密码
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 * @returns {Promise} 修改结果
 */
export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/api/user/change-password',
    method: 'post',
    data: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 退出登录
 * @returns {Promise} 退出结果（通常只需清除本地 token，但也可调用后端接口）
 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'post'
  })
}