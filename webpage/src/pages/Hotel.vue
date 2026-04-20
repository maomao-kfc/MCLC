<template>
  <div class="hotel-wrapper">
    <h2 class="page-title">{{ t('nav.hotel') }}</h2>
    
    <div class="add-btn-wrapper">
      <!-- 点击直接修改 v-model 变量，无需 Ref 调用 -->
      <el-button type="primary" @click="addModalVisible = true">
        {{ t('hotel.addBtn') }}
      </el-button>
    </div>

    <div class="hotel-list">
      <div class="hotel-card common-card">
        <h3 class="hotel-name">上海绿地铂瑞酒店</h3>
        <p class="hotel-address">地址：上海市青浦区诸光路1588弄100号</p>
        <el-image src="/assets/image/hotel/lotel1.png" class="hotel-img" />
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
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AddHotelModal from '@/components/business/AddHotelModal.vue'

const { t } = useI18n()
// 直接用响应式变量控制弹窗显隐，无需 Ref
const addModalVisible = ref(false)

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