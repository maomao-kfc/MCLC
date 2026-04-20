<!-- 嘉宾卡片组件 -->
<template>
  <div class="guest-card" @click="handleClick">
    <el-image class="guest-avatar" :src="guest.avatar" fit="cover" />
    <h3 class="guest-name">
      {{ currentLang === 'zh' ? guest.nameZh : guest.nameEn }}
    </h3>
    <p 
      v-for="(title, idx) in (currentLang === 'zh' ? guest.titleZh : guest.titleEn)"
      :key="idx" 
      class="guest-position"
    >
      {{ title }}
    </p>
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

const emit = defineEmits(['click'])

const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)

const handleClick = () => {
  emit('click', props.guest)
}
</script>

<style scoped>
.guest-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  text-align: center;
  cursor: pointer;
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
}
.guest-position {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}
</style>