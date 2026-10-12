/* 语言切换全局状态 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLangStore = defineStore('lang', () => {
  // 状态
  const currentLang = ref('zh')

  // 方法
  const setLang = (lang) => {
    localStorage.setItem('meeting-lang', lang)
    currentLang.value = lang
  }

  // 初始化：默认简体中文（刷新时也默认中文）
  const initLang = () => {
    localStorage.setItem('meeting-lang', 'zh')
    currentLang.value = 'zh'
  }

  return { currentLang, setLang, initLang }
})