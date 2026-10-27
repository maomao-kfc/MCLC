/* 用户状态管理 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { register, getRegisterUserByEmail } from '@/api/user'
import { loginByEmail, logout as logoutApi } from '@/api/auth'
import { mapStateToRegisterUserForm } from '@/utils/registerFormMapper'
import i18n from '@/locales/index.js'

const { t } = i18n.global

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || '{}'))
  const token = ref(sessionStorage.getItem('token') || '')
  const submissionUserId = ref(sessionStorage.getItem('submissionUserId') || '')
  const registrationId = ref(sessionStorage.getItem('registrationId') || '')
  const pendingRoute = ref('')
  const showGlobalLoginModal = ref(false)

  const isRegistered = computed(() => !!token.value)
  const isLoggedIn = computed(() => !!token.value)

  const fetchAndStoreSubmissionUserId = async (email) => {
    try {
      const res = await getRegisterUserByEmail(email)
      if (res.code === '00000' && res.data?.list?.length > 0) {
        const id = res.data.list[0].id
        submissionUserId.value = String(id)
        sessionStorage.setItem('submissionUserId', String(id))
      }
    } catch (e) {
      console.error('Failed to get registered user ID:', e)
    }
  }

  const doRegister = async (registerState) => {
    try {
      const formData = mapStateToRegisterUserForm(registerState)
      console.log('Registration data:', formData)

      const res = await register(formData)

      if (res.code === '00000') {
        token.value = res.data?.token || ''
        userInfo.value = res.data?.userInfo || formData

        sessionStorage.setItem('token', token.value)
        sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))

        ElMessage.success(res.msg || t('user.registerSuccess'))
        registrationId.value = String(res.data?.id || '')
        sessionStorage.setItem('registrationId', registrationId.value)
        return { success: true, id: res.data?.id }
      } else {
        ElMessage.error(res.msg || t('user.registerFail'))
        return { success: false }
      }
    } catch (error) {
      console.error('Registration request error:', error)
      ElMessage.error(error?.response?.data?.msg || error?.message || t('user.networkError'))
      return { success: false }
    }
  }

  const doLogin = async (email, code) => {
    try {
      const res = await loginByEmail(email, code)

      if (res.code === '00000') {
        token.value = res.data?.accessToken || ''
        userInfo.value = { email }

        sessionStorage.setItem('token', token.value)
        sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))

        await fetchAndStoreSubmissionUserId(email)

        ElMessage.success(t('user.loginSuccess'))
        return true
      } else {
        ElMessage.error(res.msg || t('user.loginFail'))
        return false
      }
    } catch (error) {
      console.error('Login request error:', error)
      ElMessage.error(error?.response?.data?.msg || error?.message || t('user.networkError'))
      return false
    }
  }

  const doLogout = async () => {
    try {
      await logoutApi()
    } catch (e) {
      // ignore logout errors
    }
    clearUserInfo()
    ElMessage.success(t('user.logoutSuccess'))
  }

  const clearUserInfo = () => {
    token.value = ''
    userInfo.value = {}
    submissionUserId.value = ''
    registrationId.value = ''
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
    sessionStorage.removeItem('submissionUserId')
    sessionStorage.removeItem('registrationId')
  }

  return {
    userInfo,
    token,
    submissionUserId,
    registrationId,
    pendingRoute,
    showGlobalLoginModal,
    isRegistered,
    isLoggedIn,
    doRegister,
    doLogin,
    doLogout,
    clearUserInfo,
    fetchAndStoreSubmissionUserId
  }
})
