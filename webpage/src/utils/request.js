
import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 后续改成你的真实API地址
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 后续可以在这里加token
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

export default request
