<!-- 嘉宾页面 -->
<template>
  <div class="guests-content">
    <div class="guest-section">
      <h2 class="section-title">一 主旨报告 一</h2>
      <div class="main-guest-single">
        <GuestCard 
          :guest="guestData.main[0]" 
          @click="showGuestDetail" 
        />
      </div>
      <div class="guests-grid">
        <GuestCard 
          v-for="guest in guestData.main.slice(1)" 
          :key="guest.id"
          :guest="guest" 
          @click="showGuestDetail" 
        />
      </div>
    </div>
    <div class="guest-section">
      <h2 class="section-title">一 特邀嘉宾 一</h2>
      <div class="guests-grid">
        <GuestCard 
          v-for="guest in guestData.invited" 
          :key="guest.id"
          :guest="guest" 
          @click="showGuestDetail" 
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import { guestData } from '@/data/guest.js'
import GuestCard from '@/components/business/GuestCard.vue'
import { ElMessageBox } from 'element-plus'

const { t } = useI18n()
const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)

// 嘉宾详情弹窗
const showGuestDetail = (guest) => {
  if (!guest) return
  const title = currentLang.value === 'zh' ? guest.nameZh : guest.nameEn
  const content = (currentLang.value === 'zh' ? guest.titleZh : guest.titleEn).join('\n')
  ElMessageBox.alert(content, title, {
    confirmButtonText: currentLang.value === 'zh' ? '确定' : 'Confirm',
    type: 'info',
    center: true,
    customClass: 'guest-detail-modal'
  })
}
</script>

<style scoped>
.guests-content {
  background: #fff;
  border-radius: 8px;
  padding: 30px 20px;
}
.guest-section {
  margin-bottom: 40px;
}
.section-title {
  text-align: center;
  font-size: 22px;
  color: #7952b3;
  margin-bottom: 30px;
}
.main-guest-single {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.guests-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
@media (max-width: 992px) {
  .guests-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<style>
/* 嘉宾详情弹窗深度样式 */
:deep(.guest-detail-modal) {
  .el-message-box__content {
    white-space: pre-line;
    font-size: 14px;
    line-height: 1.6;
  }
  .el-message-box__title {
    font-size: 18px;
    font-weight: 700;
  }
}
</style>