/* 语言切换全局状态 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLangStore = defineStore('lang', () => {
  // 状态
  const currentLang = ref('zh')

  // 方法
  const setLang = (lang) => {
    currentLang.value = lang
    // 可选：持久化到localStorage
    localStorage.setItem('meeting-lang', lang)
  }

  // 初始化：从本地存储读取语言
  const initLang = () => {
    const localLang = localStorage.getItem('meeting-lang')
    if (localLang) {
      currentLang.value = localLang
    }
  }

  return { currentLang, setLang, initLang }
})