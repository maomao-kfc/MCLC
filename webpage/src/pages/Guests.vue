<!-- 嘉宾页面 -->
<template>
  <div class="guests-content">
    <div class="guest-section">
      <h2 class="section-title">{{ t('keynoteReport') }}</h2>
      <div class="guests-grid">
        <GuestCard
          v-for="guest in zzbgList"
          :key="guest.id"
          :guest="guest"
        />
      </div>
    </div>
    <div class="guest-section">
      <h2 class="section-title">{{ t('invitedGuests') }}</h2>
      <div class="guests-grid">
        <GuestCard
          v-for="guest in tyjbList"
          :key="guest.id"
          :guest="guest"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import GuestCard from '@/components/business/GuestCard.vue'
import { getPageGuest } from  '@/api/portal/guest'
const { t } = useI18n()
const langStore = useLangStore()
let zzbgList = ref([])
let tyjbList = ref([])
let fetchId = 0

const fetchData = async () => {
  const id = ++fetchId
  zzbgList.value = []
  tyjbList.value = []
  try {
  const res = await getPageGuest()
  if (id !== fetchId) return
  if(res?.msg === '成功'){
    let dataList = JSON.parse(JSON.stringify( res?.data))
    for (const item of dataList) {
      item.avatar = item?.image
      item.nameZh = item?.name
      item.nameEn = item?.name;
      item.titleZh = item?.position
      item.titleEn = item?.position;
    }
    zzbgList.value = dataList?.filter(item => item?.type === '1')
    tyjbList.value = dataList?.filter(item => item?.type === '2')
  }
  } catch {
    if (id === fetchId) {
      fetchId = 0
    }
  }
}

onMounted(() => {
  fetchData()
})

watch(() => langStore.currentLang, () => {
  fetchData()
})

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
.guests-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
@media (max-width: 992px) {
  .guests-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .guests-grid {
    grid-template-columns: 1fr;
  }
}
</style>

