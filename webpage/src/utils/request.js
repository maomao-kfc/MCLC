
import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/dev-api', // 后续改成你的真实API地址
  timeout: 60000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const lang = localStorage.getItem('meeting-lang') || 'zh'
    config.params = config.params || {}
    config.params.countryType = lang === 'en' ? 'EN' : 'ZH'
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 业务code不为00000时视为业务异常，reject让调用方catch处理
    if (res.code && res.code !== '00000') {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => Promise.reject(error)
)

export default request
