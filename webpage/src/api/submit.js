/* 投稿接口封装 */

import request from '@/utils/request'

// 1. 提交论文投稿接口
export const submitPaper = (data) => {
  return request({
    url: '/api/paper/submit', // 替换为你的后端真实接口地址
    method: 'POST',
    data,
    headers: {
      'Content-Type': 'multipart/form-data' // 文件上传必须的请求头
    }
  })
}

// 2. 发送投稿查询验证码
export const sendQueryCode = (email) => {
  return request({
    url: '/api/v1/paperSubmission/query/sendCode',
    method: 'POST',
    params: { email }
  })
}

// 3. 按邮箱查询投稿列表（需验证码）
export const queryPapersByEmail = (email, code) => {
  return request({
    url: '/api/v1/paperSubmission/query/byEmail',
    method: 'GET',
    params: { email, code }
  })
}

// 4. 查询投稿详情
export const getPaperDetail = (id, email) => {
  return request({
    url: '/api/v1/paperSubmission/query/detail',
    method: 'GET',
    params: { id, email }
  })
}

// 5. 投稿者修改投稿信息（仅待审核状态可修改）
export const updatePaperBySubmitter = (data) => {
  return request({
    url: '/api/v1/paperSubmission/query/update',
    method: 'PUT',
    data
  })
}