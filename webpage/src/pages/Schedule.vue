<!-- 日程页面 -->
<template>
  <div class="schedule-content">
    <div class="schedule-top-fixed">
      <el-button type="primary" class="add-btn" @click="openAddModal">
        {{ currentLang === 'zh' ? '添加日程' : 'Add Schedule' }}
      </el-button>
    </div>
    <div class="schedule-day" v-for="(dayInfo, dayKey) in scheduleData" :key="dayKey">
      <div class="day-header">
        <div class="day-info">
          <span class="date">{{ dayInfo.date }}</span>
          <span class="week">{{ currentLang === 'zh' ? dayInfo.week : dayInfo.weekEn }}</span>
        </div>
        <div class="hall-info">
          <el-image src="/assets/image/common/diZhi.png" class="loc-icon" />
          <span>{{ currentLang === 'zh' ? dayInfo.hall : dayInfo.hallEn }}</span>
        </div>
      </div>
      <div class="session-block" v-for="session in dayInfo.sessions" :key="session.id">
        <div class="session-header">
          <div class="session-title">
            <span v-if="currentLang === 'zh'">{{ session.title }}</span>
            <span v-else>{{ session.titleEn }}</span>
          </div>
          <div class="session-chair" v-if="session.chair">
            {{ currentLang === 'zh' ? `Chair:${session.chair}` : session.chairEn }}
          </div>
        </div>
        <div class="schedule-table">
          <div class="table-header">
            <div class="th th-time">{{ currentLang === 'zh' ? '时间' : 'Time' }}</div>
            <div class="th th-type">{{ currentLang === 'zh' ? '类型' : 'Type' }}</div>
            <div class="th th-topic">{{ currentLang === 'zh' ? '题目' : 'Topic' }}</div>
            <div class="th th-speaker">{{ currentLang === 'zh' ? '讲者' : 'Speaker' }}</div>
            <div class="th th-affiliation">{{ currentLang === 'zh' ? '单位' : 'Affiliation' }}</div>
          </div>
          <div class="table-body">
            <div class="table-row" v-for="(item, idx) in session.items" :key="idx"
              :class="{ 'special-row': item.isSpecial }"
            >
              <template v-if="item.isSpecial">
                <div class="td special-td" :colspan="5">
                  <span class="special-text">{{ currentLang === 'zh' ? item.topic : item.topicEn }}</span>
                </div>
              </template>
              <template v-else>
                <div class="td td-time">{{ item.time }}</div>
                <div class="td td-type">{{ currentLang === 'zh' ? item.type : item.typeEn }}</div>
                <div class="td td-topic">{{ currentLang === 'zh' ? item.topic : item.topicEn }}</div>
                <div class="td td-speaker">{{ item.speaker }}</div>
                <div class="td td-affiliation">{{ currentLang === 'zh' ? item.affiliation : item.affiliationEn }}</div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div class="day-footer">
        <p v-if="currentLang === 'zh'">注:最终日程以现场为准</p>
        <p v-else>Note: Final official schedule will be published onsite</p>
      </div>
    </div>
  </div>

  <!-- 添加日程弹窗 -->
  <AddScheduleModal ref="addScheduleModalRef" />
</template>

<script setup>
import { computed, ref } from 'vue'
import { useLangStore } from '@/stores/lang.js'
import { useMeetingStore } from '@/stores/meeting.js'
import AddScheduleModal from '@/components/business/AddScheduleModal.vue'

const langStore = useLangStore()
const meetingStore = useMeetingStore()

const currentLang = computed(() => langStore.currentLang)
const scheduleData = computed(() => meetingStore.scheduleData)

// 弹窗引用
const addScheduleModalRef = ref()

// 打开添加日程弹窗
const openAddModal = () => {
  addScheduleModalRef.value.open()
}
</script>

<style scoped>
.schedule-content {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  position: relative;
}
.schedule-top-fixed {
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 10;
  padding: 10px 0;
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}
.add-btn {
  background: #7952b3;
  border-color: #7952b3;
}
.add-btn:hover {
  background: #6a45a0;
  border-color: #6a45a0;
}
.schedule-day {
  margin-bottom: 40px;
}
.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #7952b3;
}
.day-info .date {
  font-size: 28px;
  font-weight: 700;
  color: #7952b3;
  margin-right: 12px;
}
.day-info .week {
  font-size: 16px;
  color: #333;
}
.hall-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  color: #333;
}
.loc-icon {
  width: 18px;
  height: 18px;
}
.session-block {
  margin-bottom: 20px;
}
.session-header {
  background: linear-gradient(90deg, #7952b3, #9d75d6);
  color: #fff;
  padding: 10px 16px;
  border-radius: 4px 4px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.session-title {
  font-size: 16px;
  font-weight: 700;
}
.session-chair {
  font-size: 14px;
  opacity: 0.9;
}
.schedule-table {
  width: 100%;
  border: 1px solid #eee;
  border-top: none;
  border-radius: 0 0 4px 4px;
  overflow: hidden;
}
.table-header {
  display: flex;
  background: #f8f5fc;
  font-weight: 600;
  font-size: 14px;
}
.table-body {
  display: flex;
  flex-direction: column;
}
.table-row {
  display: flex;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}
.table-row:last-child {
  border-bottom: none;
}
.th,
.td {
  padding: 8px 6px;
  word-break: break-all;
  line-height: 1.4;
}
.th-time,
.td-time {
  width: 12%;
  flex-shrink: 0;
}
.th-type,
.td-type {
  width: 10%;
  flex-shrink: 0;
}
.th-topic,
.td-topic {
  width: 38%;
  flex-shrink: 0;
}
.th-speaker,
.td-speaker {
  width: 15%;
  flex-shrink: 0;
}
.th-affiliation,
.td-affiliation {
  width: 25%;
  flex-shrink: 0;
}
.th {
  text-align: center;
  color: #7952b3;
}
.td {
  color: #666;
}
.td-time,
.td-type,
.td-speaker {
  text-align: center;
}
.special-row {
  background: #f8f5fc;
}
.special-td {
  width: 100%;
  text-align: center;
  font-weight: 600;
  color: #7952b3;
  padding: 10px 0;
}
.special-text {
  font-size: 14px;
}
.day-footer {
  margin-top: 8px;
  font-size: 12px;
  color: #ff0000;
  text-align: left;
}
@media (max-width: 1300px) {
  .th-time,
  .td-time {
    width: 15%;
  }
  .th-type,
  .td-type {
    width: 12%;
  }
  .th-topic,
  .td-topic {
    width: 33%;
  }
}
@media (max-width: 992px) {
  .schedule-table {
    overflow-x: auto;
  }
  .table-header,
  .table-body {
    min-width: 800px;
  }
}
</style>