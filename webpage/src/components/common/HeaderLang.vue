<template>
  <!-- 顶部语言栏容器：宽度100%，作为定位基准 -->
  <div class="lang-header">
    <!-- 内部容器：和页面主内容(banner)宽度完全一致，居中对齐 -->
    <div class="lang-container">
      <!-- 语言切换按钮：永远在容器的右上角，和banner右边缘完美对齐 -->
      <div class="language-bar">
        <button
          :class="['lang-btn', { active: currentLang === 'en' }]"
          @click="switchLang('en')"
        >
          English
        </button>
        <span class="divider">|</span>
        <button
          :class="['lang-btn', { active: currentLang === 'zh' }]"
          @click="switchLang('zh')"
        >
          简体中文
        </button>
      </div>
      <!-- 用户下拉（登录/支付/退出） -->
<!--      <UserDropdown />-->
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import UserDropdown from '@/components/common/UserDropdown.vue'

const { locale } = useI18n()
const langStore = useLangStore()

// 当前语言状态
const currentLang = computed(() => langStore.currentLang)

// 语言切换方法
const switchLang = (lang) => {
  langStore.setLang(lang)
  locale.value = lang
}

// 初始化语言
onMounted(() => {
  langStore.initLang()
  locale.value = langStore.currentLang
})
</script>

<style scoped>
/* 顶部语言栏：和页面同宽，作为定位容器 */
.lang-header {
  width: 100%;
  height: 40px;
  background: #fff;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 999;
}

/* 核心容器：和页面主内容(banner)宽度、边距完全一致，保证对齐 */
.lang-container {
  width: 90%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
}

/* 语言按钮原有样式，完全保留 */
.language-bar {
  display: flex;
  align-items: center;
}
.lang-btn {
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 18px;
  font-size: 14px;
  color: #333;
  transition: color 0.2s;
}
.lang-btn.active {
  color: #114C9A;
  font-weight: 600;
}
.divider {
  margin: 0 6px;
  color: #bdc3c7;
}
</style>
