<!-- 添加日程弹窗 -->
<template>
  <el-dialog 
    v-model="addModalVisible" 
    :title="currentLang === 'zh' ? '添加日程' : 'Add Schedule'" 
    width="600px"
  >
    <el-form :model="addForm" label-width="100px" ref="addFormRef">
      <el-form-item :label="currentLang === 'zh' ? '选择日期' : 'Select Date'">
        <el-select v-model="addForm.dateKey" @change="onDateChange" style="width: 100%">
          <el-option label="1/16 星期五" value="2026-01-16" />
          <el-option label="1/17 星期六" value="2026-01-17" />
        </el-select>
      </el-form-item>
      <el-form-item :label="currentLang === 'zh' ? '选择板块' : 'Select Session'">
        <el-select v-model="addForm.sessionId" style="width: 100%">
          <el-option 
            v-for="session in scheduleData[addForm.dateKey]?.sessions" 
            :key="session.id"
            :label="currentLang === 'zh' ? session.title : session.titleEn" 
            :value="session.id" 
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="currentLang === 'zh' ? '特殊行' : 'Special Row'">
        <el-switch v-model="addForm.isSpecial" />
        <span style="margin-left: 8px; font-size: 12px; color: #999;">
          {{ currentLang === 'zh' ? '开启后为茶歇/午餐/开幕式等跨列行' : 'For Tea Break/Lunch/Opening etc.' }}
        </span>
      </el-form-item>
      <template v-if="!addForm.isSpecial">
        <el-form-item :label="currentLang === 'zh' ? '时间' : 'Time'">
          <el-input v-model="addForm.time" placeholder="例：09:00-10:00" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '类型(中文)' : 'Type(CN)'">
          <el-input v-model="addForm.type" placeholder="例：主旨报告" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '类型(英文)' : 'Type(EN)'">
          <el-input v-model="addForm.typeEn" placeholder="例：Keynote speech" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '题目(中文)' : 'Topic(CN)'">
          <el-input v-model="addForm.topic" type="textarea" :rows="2" placeholder="请输入题目" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '题目(英文)' : 'Topic(EN)'">
          <el-input v-model="addForm.topicEn" type="textarea" :rows="2" placeholder="Please enter topic" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '讲者' : 'Speaker'">
          <el-input v-model="addForm.speaker" placeholder="请输入讲者姓名" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '单位(中文)' : 'Affiliation(CN)'">
          <el-input v-model="addForm.affiliation" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '单位(英文)' : 'Affiliation(EN)'">
          <el-input v-model="addForm.affiliationEn" placeholder="Please enter affiliation" />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item :label="currentLang === 'zh' ? '标题(中文)' : 'Title(CN)'">
          <el-input v-model="addForm.topic" placeholder="例：茶歇" />
        </el-form-item>
        <el-form-item :label="currentLang === 'zh' ? '标题(英文)' : 'Title(EN)'">
          <el-input v-model="addForm.topicEn" placeholder="例：Tea Break" />
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="addModalVisible = false">{{ currentLang === 'zh' ? '取消' : 'Cancel' }}</el-button>
      <el-button type="primary" @click="confirmAddSchedule">
        {{ currentLang === 'zh' ? '确认添加' : 'Confirm' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import { useMeetingStore } from '@/stores/meeting.js'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const langStore = useLangStore()
const meetingStore = useMeetingStore()

const currentLang = computed(() => langStore.currentLang)
const scheduleData = computed(() => meetingStore.scheduleData)

// 弹窗状态
const addModalVisible = ref(false)
const addFormRef = ref()
const addForm = ref({
  dateKey: '2026-01-16',
  sessionId: 'opening',
  time: '',
  type: '',
  typeEn: '',
  topic: '',
  topicEn: '',
  speaker: '',
  affiliation: '',
  affiliationEn: '',
  isSpecial: false
})

// 暴露打开弹窗的方法
defineExpose({
  open: () => {
    addForm.value = {
      dateKey: '2026-01-16',
      sessionId: scheduleData.value['2026-01-16'].sessions[0].id,
      time: '',
      type: '',
      typeEn: '',
      topic: '',
      topicEn: '',
      speaker: '',
      affiliation: '',
      affiliationEn: '',
      isSpecial: false
    }
    addModalVisible.value = true
  }
})

// 日期切换
const onDateChange = (newDateKey) => {
  if (scheduleData.value[newDateKey]?.sessions?.length) {
    addForm.value.sessionId = scheduleData.value[newDateKey].sessions[0].id
  }
}

// 确认添加
const confirmAddSchedule = () => {
  const { dateKey, sessionId, isSpecial, ...rest } = addForm.value
  if (!dateKey || !sessionId) {
    ElMessage.warning(currentLang.value === 'zh' ? '请选择日期和板块' : 'Please select date and session')
    return
  }
  if (!isSpecial) {
    if (!rest.time || !rest.topic || !rest.speaker) {
      ElMessage.warning(currentLang.value === 'zh' ? '请填写时间、题目、讲者等必填信息' : 'Please fill in required fields')
      return
    }
  } else {
    if (!rest.topic) {
      ElMessage.warning(currentLang.value === 'zh' ? '请填写特殊行标题' : 'Please fill in the title')
      return
    }
  }
  
  // 调用Pinia添加日程
  meetingStore.addSchedule(dateKey, sessionId, { ...addForm.value })
  addModalVisible.value = false
  ElMessage.success(currentLang.value === 'zh' ? '日程添加成功' : 'Schedule added successfully')
}
</script>