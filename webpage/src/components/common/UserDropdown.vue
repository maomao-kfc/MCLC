<template>
  <div class="user-dropdown" ref="dropdownRef">
    <!-- 未登录：直接显示登录按钮，点击弹窗 -->
    <button v-if="!isLoggedIn" class="user-btn" @click="showLoginModal = true">
      {{ t('user.login') }}
    </button>

    <!-- 已登录：显示用户信息，点击展开下拉 -->
    <div v-else class="user-info" @click="showDropdown = !showDropdown">
      <span class="user-label">{{ userStore.userInfo?.email || t('user.loggedIn') }}</span>
    </div>

    <!-- 下拉菜单（仅已登录时显示） -->
    <transition name="dropdown-fade">
      <div v-if="showDropdown && isLoggedIn" class="dropdown-menu">
        <div class="dropdown-item" @mousedown="goToPayment">{{ t('user.payment') }}</div>
        <div class="dropdown-item" @mousedown="handleLogout">{{ t('user.logout') }}</div>
      </div>
    </transition>

    <!-- 登录弹窗 -->
    <LoginModal v-model="showLoginModal" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import LoginModal from '@/components/common/LoginModal.vue'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const showDropdown = ref(false)
const showLoginModal = ref(false)

watch(() => userStore.showGlobalLoginModal, (val) => {
  if (val) {
    showLoginModal.value = true
    userStore.showGlobalLoginModal = false
  }
})

const dropdownRef = ref(null)

const handleClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  if (userStore.showGlobalLoginModal) {
    showLoginModal.value = true
    userStore.showGlobalLoginModal = false
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

watch(showLoginModal, (val) => {
  if (!val && userStore.pendingRoute && userStore.isLoggedIn) {
    const route = userStore.pendingRoute
    userStore.pendingRoute = ''
    if (route === '/first-draft') {
      const resolved = router.resolve({ path: route })
      window.open(window.location.origin + resolved.href, '_blank')
    } else {
      router.push(route)
    }
  }
})

const goToPayment = () => {
  showDropdown.value = false
  const resolved = router.resolve({ path: '/payment' })
  window.open(window.location.origin + resolved.href, '_blank')
}

const handleLogout = () => {
  showDropdown.value = false
  userStore.doLogout()
}
</script>

<style scoped>
.user-dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 100%;
  margin-left: 20px;
  padding-left: 20px;
  border-left: 1px solid #e8e8e8;
}

.user-btn {
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 18px;
  font-size: 14px;
  color: #333;
  transition: color 0.2s;
}

.user-btn:hover {
  color: #114C9A;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 18px;
}

.user-label {
  font-size: 14px;
  color: #333;
}

.user-label:hover {
  color: #114C9A;
  font-weight: 600;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-width: 120px;
  z-index: 1000;
}

.dropdown-item {
  padding: 10px 20px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  white-space: nowrap;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
  color: #114C9A;
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.2s ease;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
}
</style>
