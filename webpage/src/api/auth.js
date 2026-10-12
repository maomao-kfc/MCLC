import request from '@/utils/request'

export const sendEmailCode = (email) => {
  return request({
    url: '/api/v1/portal/auth/email/code',
    method: 'POST',
    params: { email }
  })
}

export const loginByEmail = (email, code) => {
  return request({
    url: '/api/v1/portal/auth/email/login',
    method: 'POST',
    data: { email, code }
  })
}

export const logout = () => {
  return request({
    url: '/api/v1/auth/logout',
    method: 'DELETE'
  })
}
