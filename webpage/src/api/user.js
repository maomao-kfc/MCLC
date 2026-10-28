/* 注册接口封装 */

import request from '@/utils/request'

// 用户注册接口
export const register = (data) => {
  return request({
    url: '/api/v1/registerUser',
    method: 'POST',
    data
  })
}

// 根据邮箱查询注册用户
export const getRegisterUserByEmail = (email) => {
  return request({
    url: '/api/v1/registerUser',
    method: 'GET',
    params: { email, pageNum: 1, pageSize: 1 }
  })
}

// 更新注册用户
export const updateRegisterUser = (id, data) => {
  return request({
    url: `/api/v1/registerUser/${id}`,
    method: 'PUT',
    data
  })
}

// 根据邮箱查询注册状态（公开接口，"我的注册"页面用）
export const getRegistrationStatus = (email) => {
  return request({
    url: '/api/v1/registerUser/status',
    method: 'GET',
    params: { email }
  })
}
