<!-- 导航栏 -->

<template>
  <div class="navigationBar">
    <div class="nav-pagination">
      <div 
        class="nav-arrow nav-arrow-left" 
        :class="{ visible: canScrollLeft }"
        @click="scrollLeft"
      >
        ‹
      </div>
      
      <div class="nav-wrapper" ref="navWrapper">
        <div 
          class="nav-buttons"
          ref="navButtons"
          @scroll="handleScroll"
        >
          <div class="nav-slider">
            <button 
              v-for="item in filteredNavItems" 
              :key="item.id" 
              class="nav-btn"
              :class="{ active: $route.path === item.path }" 
              @click="handleNavClick(item.path)"
            >
              {{ t(item.i18nKey) }}
            </button>
          </div>
        </div>
      </div>
      
      <div 
        class="nav-arrow nav-arrow-right" 
        :class="{ visible: canScrollRight }"
        @click="scrollRight"
      >
        ›
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { navItems } from '@/data/nav.js'
import { useGuestStore } from '@/stores/guest.js'
import { storeToRefs } from 'pinia'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const guestStore = useGuestStore()
const { hwList } = storeToRefs(guestStore)

const showSchedule = computed(() => {
  return hwList.value?.[0]?.showSchedule !== 0
})

const filteredNavItems = computed(() => {
  return navItems.filter(item => {
    if (item.id === 'schedule1') {
      return showSchedule.value
    }
    return true
  })
})

const navWrapper = ref(null)
const navButtons = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)

const handleNavClick = (path) => router.push(path)

const updateScrollState = () => {
  if (!navButtons.value) return
  const { scrollLeft: sl, scrollWidth, clientWidth } = navButtons.value
  canScrollLeft.value = sl > 0
  canScrollRight.value = sl + clientWidth < scrollWidth - 1
}

const scrollLeft = () => {
  if (!navButtons.value) return
  navButtons.value.scrollBy({ left: -200, behavior: 'smooth' })
}

const scrollRight = () => {
  if (!navButtons.value) return
  navButtons.value.scrollBy({ left: 200, behavior: 'smooth' })
}

const handleScroll = () => {
  updateScrollState()
}

onMounted(() => {
  updateScrollState()
  window.addEventListener('resize', updateScrollState)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScrollState)
})
</script>

<style scoped>
.navigationBar {
  z-index: 998;
}

.nav-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 90%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.nav-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.nav-buttons {
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.nav-buttons::-webkit-scrollbar {
  display: none;
}

.nav-slider {
  display: flex;
  gap: 20px;
}

.nav-btn {
  min-width: 140px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 10px 8px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.nav-btn:hover {
  border-color: #114C9A;
  color: #114C9A;
}

.nav-btn.active {
  color: #114C9A;
  border-color: #114C9A;
  background: #f0f7ff;
}

.nav-arrow {
  width: 32px;
  height: 32px;
  display: none;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #666;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.nav-arrow.visible {
  display: flex;
}

.nav-arrow:hover {
  color: #114C9A;
}

@media (max-width: 1200px) {
  .nav-pagination {
    width: 95%;
    max-width: 1200px;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .nav-pagination {
    width: 100%;
    padding: 0 12px;
  }
  .nav-slider {
    gap: 12px;
    padding: 0 12px;
  }
  .nav-btn {
    min-width: 120px;
    padding: 12px 8px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .nav-btn {
    min-width: 100px;
    font-size: 13px;
    padding: 10px 6px;
  }
  .nav-slider {
    gap: 10px;
  }
}
</style>