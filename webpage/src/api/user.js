/* 注册接口封装 */

import request from '@/utils/request'

// 用户注册接口
export const register = (data) => {
  return request({
    url: '/api/user/register', // 后续改成你的后端真实接口地址
    method: 'POST',
    data
  })
}

// 后续可扩展：登录、获取用户信息、报名记录等接口
// export const login = (data) => request.post('/api/user/login', data)
// export const getUserInfo = () => request.get('/api/user/info')
