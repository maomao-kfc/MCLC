/* 用户状态管理 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 状态定义
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const token = ref(localStorage.getItem('token') || '')

  // 计算属性：是否已注册/登录
  const isRegistered = computed(() => !!token.value)

  // 核心注册方法
  /* const doRegister = async (formData) => {
    try {
      // 对接后端接口
      const res = await register(formData)
      
      // 后端返回格式约定：{ code: 200, data: { token, userInfo }, msg: '注册成功' }
      if (res.code === 200) {
        // 保存用户信息和token
        token.value = res.data.token
        userInfo.value = res.data.userInfo
        
        // 本地持久化，刷新页面不丢失
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
        
        ElMessage.success(res.msg || '注册成功！')
        return true
      } else {
        ElMessage.error(res.msg || '注册失败，请重试')
        return false
      }
    } catch (error) {
      console.error('注册请求错误：', error)
      ElMessage.error('网络异常，请稍后重试')
      return false
    }
  } */
  
  // 前端模拟注册，测试用
  const doRegister = async (formData) => {
    // 模拟接口请求延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    console.log('注册表单数据：', formData)
    // 模拟注册成功
    ElMessage.success('注册成功！')
    // 模拟保存用户信息
    userInfo.value = formData
    localStorage.setItem('userInfo', JSON.stringify(formData))
    return true
  }

  // 退出登录/清除信息
  const clearUserInfo = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    userInfo,
    token,
    isRegistered,
    doRegister,
    clearUserInfo
  }
})