/* 会议数据（日程/酒店添加）全局状态 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { scheduleData as initScheduleData } from '@/data/schedule.js'

export const useMeetingStore = defineStore('meeting', () => {
  // 日程数据（响应式）
  const scheduleData = ref(JSON.parse(JSON.stringify(initScheduleData)))

  // 添加日程方法
  const addSchedule = (dateKey, sessionId, newItem) => {
    const targetDay = scheduleData.value[dateKey]
    const targetSession = targetDay?.sessions.find(s => s.id === sessionId)
    if (targetSession) {
      targetSession.items.push(newItem)
    }
  }

  return { scheduleData, addSchedule }
})
