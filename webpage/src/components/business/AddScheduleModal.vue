<!-- 添加日程弹窗 -->
<template>
  <el-dialog 
    v-model="addModalVisible" 
    :title="t('schedule.addTitle')" 
    width="600px"
  >
    <el-form :model="addForm" label-width="100px" ref="addFormRef">
      <el-form-item :label="t('schedule.selectDate')">
        <el-select v-model="addForm.dateKey" @change="onDateChange" style="width: 100%">
          <el-option label="1/16 Friday" value="2026-01-16" />
          <el-option label="1/17 Saturday" value="2026-01-17" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('schedule.selectSession')">
        <el-select v-model="addForm.sessionId" style="width: 100%">
          <el-option 
            v-for="session in scheduleData[addForm.dateKey]?.sessions" 
            :key="session.id"
            :label="currentLang === 'zh' ? session.title : session.titleEn" 
            :value="session.id" 
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('schedule.specialRow')">
        <el-switch v-model="addForm.isSpecial" />
        <span style="margin-left: 8px; font-size: 12px; color: #999;">
          {{ t('schedule.specialRowHint') }}
        </span>
      </el-form-item>
      <template v-if="!addForm.isSpecial">
        <el-form-item :label="t('schedule.time')">
          <el-input v-model="addForm.time" placeholder="e.g. 09:00-10:00" />
        </el-form-item>
        <el-form-item :label="t('schedule.typeCN')">
          <el-input v-model="addForm.type" placeholder="e.g. Keynote Speech" />
        </el-form-item>
        <el-form-item :label="t('schedule.typeEN')">
          <el-input v-model="addForm.typeEn" placeholder="e.g. Keynote Speech" />
        </el-form-item>
        <el-form-item :label="t('schedule.topicCN')">
          <el-input v-model="addForm.topic" type="textarea" :rows="2" :placeholder="t('schedule.topicCN')" />
        </el-form-item>
        <el-form-item :label="t('schedule.topicEN')">
          <el-input v-model="addForm.topicEn" type="textarea" :rows="2" :placeholder="t('schedule.topicEN')" />
        </el-form-item>
        <el-form-item :label="t('schedule.speaker')">
          <el-input v-model="addForm.speaker" :placeholder="t('schedule.speaker')" />
        </el-form-item>
        <el-form-item :label="t('schedule.affiliationCN')">
          <el-input v-model="addForm.affiliation" :placeholder="t('schedule.affiliationCN')" />
        </el-form-item>
        <el-form-item :label="t('schedule.affiliationEN')">
          <el-input v-model="addForm.affiliationEn" :placeholder="t('schedule.affiliationEN')" />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item :label="t('schedule.titleCN')">
          <el-input v-model="addForm.topic" :placeholder="t('schedule.titleCN')" />
        </el-form-item>
        <el-form-item :label="t('schedule.titleEN')">
          <el-input v-model="addForm.topicEn" :placeholder="t('schedule.titleEN')" />
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="addModalVisible = false">{{ t('schedule.cancel') }}</el-button>
      <el-button type="primary" @click="confirmAddSchedule">
        {{ t('schedule.confirmAdd') }}
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
    ElMessage.warning(t('schedule.selectDateAndSession'))
    return
  }
  if (!isSpecial) {
    if (!rest.time || !rest.topic || !rest.speaker) {
      ElMessage.warning(t('schedule.fillRequired'))
      return
    }
  } else {
    if (!rest.topic) {
      ElMessage.warning(t('schedule.fillSpecialTitle'))
      return
    }
  }
  
  // 调用Pinia添加日程
  meetingStore.addSchedule(dateKey, sessionId, { ...addForm.value })
  addModalVisible.value = false
  ElMessage.success(t('schedule.addSuccess'))
}
</script>
