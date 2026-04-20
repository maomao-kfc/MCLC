import { createApp } from 'vue'
import App from './App.vue'
// 引入放大镜插件和样式
import Viewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 必须引入，否则Element组件样式失效
import '@/assets/styles/index.scss' // 导入全局样式（必须放在Element Plus样式之后）
import { createI18n } from 'vue-i18n'
import zh from './locales/zh.js'
import en from './locales/en.js'

// 国际化配置
const i18n = createI18n({
  legacy: false, // Vue3组合式API必须关闭legacy模式
  locale: 'zh',
  fallbackLocale: 'zh',
  messages: { zh, en }
})

const app = createApp(App)

// 全局挂载插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(i18n)
// 注册插件，配置默认参数
app.use(Viewer, {
  defaultOptions: {
    zIndex: 9999, 
    toolbar: true, 
    title: true, 
    button: true, 
    navbar: true, 
    transition: true,
    rotatable: true,
  }
})

app.mount('#app')