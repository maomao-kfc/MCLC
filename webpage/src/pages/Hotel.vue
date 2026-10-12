<template>
  <div class="hotel-wrapper">
<!--    <h2 class="page-title">{{ t('nav.hotel') }}</h2>-->



    <div class="hotel-list">
      <div class="hotel-card common-card" v-for="item in hotelList " :key="item.id">
<!--        <h3 class="hotel-name">上海绿地铂瑞酒店</h3>-->
<!--        <p class="hotel-address">地址：上海市青浦区诸光路1588弄100号</p>-->
<!--        <el-image src="/assets/image/hotel/lotel1.png" class="hotel-img" />-->
                <h3 class="hotel-name">{{item?.hotelName}} <el-button v-if="item?.hotelUrl" size="small" class="btnReserve" @click="reserveEve(item?.hotelUrl)">{{ t('hotel.reserveBtn') }}</el-button></h3>
                <p class="hotel-address">{{ t('hotel.address') }}：{{item?.hotelAddress}}</p>
                <el-image :src="item?.hotelImage" class="hotel-img" />
      </div>
    </div>

    <!-- 用 v-model 双向绑定，彻底告别 Ref 报错 -->
    <AddHotelModal
      v-model="addModalVisible"
      @submit="handleAddHotel"
    />
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import AddHotelModal from '@/components/business/AddHotelModal.vue'
import { getPageHotel } from  '@/api/portal/hotel'
const { t } = useI18n()
const langStore = useLangStore()
const hotelList = ref([])

const fetchData = async () => {
  const res = await getPageHotel()
  if(res?.msg === '成功'){
    hotelList.value = JSON.parse(JSON.stringify( res?.data))
  }
}

onMounted(() => {
  fetchData()
})

watch(() => langStore.currentLang, () => {
  fetchData()
})
// 直接用响应式变量控制弹窗显隐，无需 Ref
const addModalVisible = ref(false)
const reserveEve = (v) => {
  window.open(v, '_blank')
}
const handleAddHotel = (formData) => {
  console.log('新增酒店数据：', formData)
  // 后续对接API，添加到酒店列表
}
</script>

<style scoped>
.hotel-wrapper {
  width: 100%;
}
.add-btn-wrapper {
  margin-bottom: 24px;
  text-align: right;
}
.hotel-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.hotel-card {
  padding: 24px;
}
.hotel-name {
  font-size: 20px;
  font-weight: 700;
  color: #114C9A;
  margin-bottom: 8px;
  position: relative;
}
.btnReserve{
  position: absolute;
  right: 0;
}
.hotel-address {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}
.hotel-img {
  width: 100%;
  border-radius: 8px;
}
</style>
