import { createI18n } from 'vue-i18n'
import en from './en.js'
import zh from './zh.js'

const messages = {
  en,
  zh
}

const i18n = createI18n({
  legacy: false,         // 使用 Composition API 模式
  locale: 'zh',          // 默认语言
  fallbackLocale: 'en',  // 回退语言
  messages
})

export default i18n