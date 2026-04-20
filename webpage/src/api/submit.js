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

// 2. 获取当前用户的稿件列表
export const getPaperList = () => {
  return request({
    url: '/api/paper/list', // 替换为你的后端真实接口地址
    method: 'GET'
  })
}

// 3. 获取稿件详情（可选扩展）
export const getPaperDetail = (paperId) => {
  return request({
    url: `/api/paper/detail/${paperId}`,
    method: 'GET'
  })
}