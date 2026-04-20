<!-- 导航栏 -->

<template>
  <div class="navigationBar">
    <div class="nav-pagination">
      <img 
        src="/assets/image/common/up.png" 
        alt="上一页" 
        class="page-icon left-icon" 
        :class="{ disabled: startIndex === 0 }"
        @click="prevPage" 
      />
      <div class="nav-buttons">
        <div class="nav-slider" :style="{ transform: `translateX(${sliderOffset}px)` }">
          <button 
            v-for="item in navItems" 
            :key="item.id" 
            class="nav-btn"
            :class="{ active: $route.path === item.path }" 
            @click="handleNavClick(item.path)"
          >
            {{ t(item.i18nKey) }}
          </button>
        </div>
      </div>
      <div class="right-controls">
        <img 
          src="/assets/image/common/next.png" 
          alt="下一页" 
          class="page-icon"
          :class="{ disabled: startIndex >= maxStartIndex }" 
          @click="nextPage" 
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { navItems } from '@/data/nav.js'

// 初始化实例，无任何重复声明
const { t } = useI18n()
const router = useRouter()
const route = useRoute()

// 导航分页逻辑
const WINDOW_SIZE = 6
const startIndex = ref(0)
// computed仅定义一次，无重复
const maxStartIndex = computed(() => Math.max(0, navItems.length - WINDOW_SIZE))
const BUTTON_WIDTH = 150
const BUTTON_GAP = 30
const UNIT_WIDTH = BUTTON_WIDTH + BUTTON_GAP
const sliderOffset = computed(() => -(startIndex.value * UNIT_WIDTH))

// 导航方法
const prevPage = () => startIndex.value > 0 && startIndex.value--
const nextPage = () => startIndex.value < maxStartIndex.value && startIndex.value++
const handleNavClick = (path) => router.push(path)
</script>

<style scoped>
.nav-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 1200px;
  max-width: 100%;
  margin: 0 auto;
  min-height: 70px;
  padding: 0 10px;
}
.left-icon {
  width: 32px;
  height: 32px;
  cursor: pointer;
  position: static;
  transform: none;
}
.right-controls {
  position: static;
  transform: none;
}
.page-icon {
  width: 32px;
  height: 32px;
  cursor: pointer;
}
.page-icon.disabled {
  opacity: 0.3;
  pointer-events: none;
}
.nav-buttons {
  flex: 1;
  max-width: 1050px;
  margin: 0 auto;
  overflow: hidden;
}
.nav-slider {
  display: flex;
  gap: 30px;
  transition: transform 0.3s;
}
.nav-btn {
  width: 150px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 8px 0;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}
.nav-btn.active {
  color: #114C9A;
}
/* 响应式适配 */
@media (max-width: 1300px) {
  .nav-pagination {
    width: 100%;
    padding: 0 20px;
  }
}
@media (max-width: 992px) {
  .nav-buttons {
    max-width: 700px;
  }
}
@media (max-width: 768px) {
  .nav-buttons {
    max-width: 400px;
  }
}
</style>