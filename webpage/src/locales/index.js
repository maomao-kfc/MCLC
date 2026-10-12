import { createI18n } from 'vue-i18n'
import en from './en.js'
import zh from './zh.js'

const messages = {
  en,
  zh
}

const i18n = createI18n({
  legacy: false,
  locale: 'zh',
  fallbackLocale: 'zh',
  messages
})

export default i18n
