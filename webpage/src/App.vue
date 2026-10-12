<template>
  <!-- 注册页面全屏独占 -->
  <div v-if="isRegisterPage" class="register-fullpage">
    <router-view />
  </div>

  <!-- 带整体布局的页面 -->
  <template v-else>
    <!-- 固定顶部 -->
    <div class="app-fixed-header">
      <HeaderLang />
    </div>

    <div class="page-content">
      <div class="block">
        <!-- 首页顶部大图 -->
        <div class="banner-wrapper">
          <el-image class="hI" :src="objVal?.titleImage" />
        </div>

        <!-- 导航栏 -->
        <div
          ref="navBarEl"
          class="nav-bar"
          :class="{ 'nav-fixed': isNavFixed }"
        >
          <NavBar />
        </div>

        <div class="bottom-row">
          <div class="zuo">
            <router-view />
          </div>
          <Sidebar />
        </div>
      </div>
    </div>

    <!-- 固定底部 -->
    <div class="app-fixed-footer">
      <Footer />
    </div>
  </template>
</template>

<script setup>
import {ref, onMounted, onUnmounted, computed, watch} from 'vue'
import HeaderLang from '@/components/common/HeaderLang.vue'
import NavBar from '@/components/common/NavBar.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Footer from '@/components/common/Footer.vue'
import { useGuestStore } from '@/stores/guest.js'
import { useUserStore } from '@/stores/user'
import { useLangStore } from '@/stores/lang.js'
import { useI18n } from 'vue-i18n'
import {storeToRefs} from "pinia";
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const guestStore = useGuestStore()
const userStore = useUserStore()
const langStore = useLangStore()
const { locale } = useI18n()
const { hwList } = storeToRefs(guestStore)
const objVal = computed(() => {
  return  hwList.value?.[0]
})

const isRegisterPage = computed(() => {
  return route.matched.some(r => ['Register', 'FirstDraft', 'Payment', 'ReviewFile'].includes(r.name))
})

const navBarEl = ref(null)
const isNavFixed = ref(false)
const navOffsetTop = ref(0)

const handleScroll = () => {
  if (!navBarEl.value) return
  const scrollY = window.scrollY || document.documentElement.scrollTop
  isNavFixed.value = scrollY > navOffsetTop.value
}

// 应用初始化
// - 初始化语言设置（默认中文）
// - 获取大会配置信息
// - 计算导航栏偏移量并绑定滚动监听
// 注意：已移除页面刷新时跳转首页的逻辑，刷新后保持在当前路由页面
onMounted(() => {
  langStore.initLang()
  locale.value = 'zh'
  guestStore.gethwList()
  setTimeout(() => {
    if (navBarEl.value) {
      navOffsetTop.value = navBarEl.value.offsetTop
    }
    handleScroll()
    window.addEventListener('scroll', handleScroll, { passive: true })
    window.addEventListener('resize', handleScroll, { passive: true })
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleScroll)
})

watch(() => route.query.login, (val) => {
  if (val) {
    userStore.showGlobalLoginModal = true
    router.replace({ query: {} })
  }
})
</script>

<style scoped>
.app-fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.app-fixed-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.banner-wrapper {
  width: 90%;
  max-width: 1400px;
  margin: 0 auto;
}

.hI {
  width: 100%;
  height: auto;
  object-fit: cover;
}

.nav-bar {
  position: relative;
  z-index: 998;
  padding-top: 20px;
  padding-bottom: 10px;
}

.nav-bar.nav-fixed {
  position: fixed;
  top: 40px;
  left: 0;
  right: 0;
  z-index: 999;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding-top: 10px;
  padding-bottom: 10px;
}

.page-content {
  width: 100%;
  background: #F7F7F7;
  padding-top: 40px;
  padding-bottom: 80px;
}

.block {
  margin-top: 20px;
  text-align: center;
}
.bottom-row {
  display: flex;
  width: 90%;
  max-width: 1400px;
  margin: 20px auto;
  gap: 20px;
  align-items: flex-start;
}
.zuo {
  width: calc(65% - 8px);
  min-width: 300px;
  flex-shrink: 0;
}
@media (max-width: 1400px) {
  .bottom-row {
    width: 95%;
    max-width: 1200px;
  }
  .zuo {
    width: calc(60% - 10px);
  }
}

@media (max-width: 1024px) {
  .zuo {
    width: calc(100% - 320px);
  }
}
@media (max-width: 768px) {
  .bottom-row {
    flex-direction: column;
    gap: 24px;
    padding: 0 16px;
  }
  .zuo {
    width: 100%;
    min-width: unset;
  }
}

@media (max-width: 480px) {
  .bottom-row {
    padding: 0 12px;
    gap: 20px;
  }
}
</style>

<style>
.register-fullpage {
  width: 100vw;
  min-height: 100vh;
  background: #F7F7F7;
  padding: 0;
  overflow: hidden;
  box-sizing: border-box;
}
</style>
