<template>
  <div class="flip-card">
    <div class="flip-card-inner">
      <div class="flip-card-front">
        <el-image class="guest-avatar" :src="guest?.avatar" fit="cover" />
        <h3 class="guest-name">
          {{ currentLang === 'zh' ? guest?.nameZh : guest?.nameEn }}
        </h3>
      </div>
      <div class="flip-card-back">
        <h3 class="guest-name">
          {{ currentLang === 'zh' ? guest?.nameZh : guest?.nameEn }}
        </h3>
        <div class="title-list guest-card-vhtml" v-html="getTitles()"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useLangStore } from '@/stores/lang.js'

const props = defineProps({
  guest: {
    type: Object,
    required: true
  }
})

const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)

const getTitles = () => {
  const titles = currentLang.value === 'zh' ? props.guest?.titleZh : props.guest?.titleEn
  if (!titles) return ''
  if (Array.isArray(titles)) {
    return titles.map(t => `<p class="title-item">${t}</p>`).join('')
  }
  if (/<[^>]+>/.test(titles)) {
    return titles
  }
  return titles.split(',').map(t => `<p class="title-item">${t.trim()}</p>`).join('')
}
</script>

<style scoped>
.flip-card {
  width: 100%;
  height: 320px;
  perspective: 1000px;
  cursor: pointer;
}

.flip-card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.9s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
}

.flip-card:hover .flip-card-inner {
  transform: rotateY(-180deg);
}

.flip-card-front,
.flip-card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.flip-card-front {
  background: #fff;
}

.flip-card-back {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  transform: rotateY(180deg);
}

.guest-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid #f0f0ff;
  margin-bottom: 16px;
}

.guest-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
  text-align: center;
}

.title-list {
  width: 100%;
  max-height: 180px;
  overflow-y: auto;
  padding: 0 8px;
}

.title-item {
  font-size: 13px;
  line-height: 1.6;
  margin: 4px 0;
  text-align: center;
  opacity: 0.9;
}

.title-list::-webkit-scrollbar {
  width: 4px;
}

.title-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}
</style>

<style>
.guest-card-vhtml .title-item {
  font-size: 13px;
  line-height: 1.6;
  margin: 4px 0;
  text-align: center;
  opacity: 0.9;
}
</style>
