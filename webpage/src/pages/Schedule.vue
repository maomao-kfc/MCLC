<!-- 日程页面 -->
<template>
  <!-- 会前会内容 -->
  <div class="pre-meeting-wrapper">
    <div class="huiHead">
      <el-image :src="objVal?.hqapImage" fit="cover" />
    </div>
    <div class="pre-meeting-header" v-if="headerArrange">
      <div class="pre-meeting-header-inner">
        <h1 class="pre-meeting-title">{{ headerArrange.room }}</h1>
        <p class="pre-meeting-desc" v-html="headerArrange.topic"></p>
      </div>
    </div>
    <div class="pre-meeting-list">
      <div class="meeting-item" v-for="(item, index) in blockArrangeList" :key="index">
        <div class="room-tag" :ref="el => setRoomTagRef(el, index)" :class="{ 'align-top': item.isMultiLine }">
          {{ item.room }}
        </div>
        <div class="meeting-topic" :ref="el => setTopicTagRef(el, index)" v-html="item.topic"></div>
      </div>
    </div>
  </div>

  <div v-if="objVal?.scheduleEnabled === 1" class="schedule-content">
    <template v-for="(dayInfo, dayIdx) in scheduleData" :key="dayIdx">
    <div class="schedule-day">
      <div class="day-header">
        <div class="day-info">
          <span class="date">{{ dayInfo?.date }}</span>
          <div class="week-bilingual">
            <span class="cell-zh">{{ dayInfo?.week }}</span>
            <span class="cell-en" v-if="dayInfo?.weekEn">{{ dayInfo?.weekEn }}</span>
          </div>
        </div>
        <div class="hall-info">
          <el-image src="/assets/image/common/diZhi.png" class="loc-icon" />
          <div class="hall-bilingual">
            <span class="cell-zh">{{ dayInfo?.hall }}</span>
            <span class="cell-en" v-if="dayInfo?.hallEn">{{ dayInfo?.hallEn }}</span>
          </div>
        </div>
      </div>
      <!-- 一天一个表格 -->
      <div class="schedule-table">
        <div class="table-body">
          <template v-for="session in dayInfo?.sessions" :key="session?.id">
            <!-- 板块标题行 - 板块名称放在时间、类型栏上面 -->
            <div class="session-header-row" :class="{ 'session-header-ceremony': session?.itemType === 'OPENING' || session?.itemType === 'CLOSING' }">
              <div class="session-title-wrap">
                <span class="session-title">{{ session?.title }}</span>
                <span class="session-title-en" v-if="session?.titleEn">{{ session?.titleEn }}</span>
              </div>
              <span class="session-chair" v-if="session?.chair">
                <span class="chair-zh">主持人：{{ session?.chair }}</span>
                <span class="chair-en" v-if="session?.chairEn">Chair：{{ session?.chairEn }}</span>
              </span>
            </div>
            <!-- 表头行 - 每个板块下显示（闭幕式/研讨不显示表头） -->
            <div class="table-header" v-if="session?.itemType !== 'CLOSING' && session?.itemType !== 'DISCUSSION' && session?.itemType !== 'OPENING'">
              <div class="th th-time">时间/Time</div>
              <div class="th th-type">类型/Type</div>
              <div class="th th-topic">题目/Topic</div>
              <div class="th th-speaker">讲者/Speaker</div>
              <div class="th th-affiliation">主讲人单位/Affiliation</div>
            </div>
            <!-- 条目行 - 按sortOrder顺序渲染 -->
            <template v-for="(block, bIdx) in getItemRenderBlocks(session?.items)" :key="bIdx">
              <!-- 开幕式/闭幕式/茶歇/中餐 条目: 独占一整行（单单元格） -->
              <div v-if="block.type === 'special'" class="special-full-row" :class="[getSpecialTypeClass(block.itemType), block.rowIndex % 2 === 0 ? 'row-odd' : 'row-even']">
                <span v-if="block.itemType === 'BREAK'" class="break-icon">☕</span>
                <span v-if="block.itemType === 'BREAK' || block.itemType === 'LUNCH' || block.itemType === 'OPENING'" class="special-full-text">
                  <span class="bilingual-zh">{{ getSpecialTypeText(block.itemType, 'zh') }}</span>
                  <span class="bilingual-en">{{ getSpecialTypeText(block.itemType, 'en') }}</span>
                </span>
                <span v-else class="special-full-text">{{ getSpecialTypeText(block.itemType) }}</span>
              </div>
              <!-- 正常条目按时间分组（大会发言也参与时间分组） -->
              <div v-else-if="block.type === 'timeGroup'" class="time-group">
                <div class="time-cell-merged">
                  <template v-if="getTimeText(block.time)">
                    <div class="cell-zh">{{ getTimeText(block.time).zh }}</div>
                    <div class="cell-en">{{ getTimeText(block.time).en1 }}</div>
                    <div class="cell-en">{{ getTimeText(block.time).en2 }}</div>
                  </template>
                  <template v-else>{{ block.time }}</template>
                </div>
                <div class="time-group-items">
                  <template v-for="(item, idx) in block.items" :key="idx">
                    <!-- 大会发言：类型+题目+讲者+单位合并为一列，中英双行显示 -->
                    <template v-if="isItemGeneralSpeech(item)">
                      <div class="table-row general-speech-row" :class="item.rowIndex % 2 === 0 ? 'row-odd' : 'row-even'">
                        <div class="td td-merged-general-speech">
                          <template v-if="isSameText(item.type, item?.typeEn)">{{ item.type }}</template>
                          <template v-else>
                            <div class="cell-zh">{{ item.type }}</div>
                            <div class="cell-en">{{ item?.typeEn }}</div>
                          </template>
                        </div>
                      </div>
                    </template>
                    <!-- 正常条目：每条一行，各列单元格内上行中文+下行英文，讲者中英相同时合并显示 -->
                    <template v-else>
                      <div class="table-row" :class="{ 'special-row': isItemSpecial(item), 'row-odd': item.rowIndex % 2 === 0, 'row-even': item.rowIndex % 2 !== 0 }">
                        <div class="td td-type">
                          <div class="cell-zh">{{ item.type }}</div>
                          <div class="cell-en">{{ item?.typeEn }}</div>
                        </div>
                        <div class="td td-topic" :class="{ 'topic-pending': isPendingTopic(item) }">
                          <div class="cell-zh">{{ item.topic }}</div>
                          <div class="cell-en">{{ item?.topicEn }}</div>
                        </div>
                        <div class="td td-speaker" :class="{ 'speaker-merged': isSameText(item?.speaker, item?.speakerEn) }">
                          <template v-if="isSameText(item?.speaker, item?.speakerEn)">{{ item?.speaker }}</template>
                          <template v-else>
                            <div class="cell-zh">{{ item?.speaker }}</div>
                            <div class="cell-en">{{ item?.speakerEn }}</div>
                          </template>
                        </div>
                        <div class="td td-affiliation">
                          <div class="cell-zh">{{ item?.affiliation }}</div>
                          <div class="cell-en">{{ item?.affiliationEn }}</div>
                        </div>
                      </div>
                    </template>
                  </template>
                </div>
              </div>
            </template>
            <!-- Panel Discussion 三列布局：时间 | 参与人 | 讨论会名称 -->
            <div class="discussion-table" v-if="session?.itemType === 'DISCUSSION' && session?.participants?.length">
              <div class="discussion-time">
                  <template v-if="session?.timePeriod">
                    <div class="cell-zh">{{ session?.timePeriod }}</div>
                    <template v-if="session?.timePeriodEn">
                      <div class="cell-en">{{ splitEnPeriod(session.timePeriodEn).en1 }}</div>
                      <div class="cell-en" v-if="splitEnPeriod(session.timePeriodEn).en2">{{ splitEnPeriod(session.timePeriodEn).en2 }}</div>
                    </template>
                  </template>
                  <template v-else-if="getTimeText(session?.items?.[0]?.time)">
                    <div class="cell-zh">{{ getTimeText(session?.items?.[0]?.time).zh }}</div>
                    <div class="cell-en">{{ getTimeText(session?.items?.[0]?.time).en1 }}</div>
                    <div class="cell-en">{{ getTimeText(session?.items?.[0]?.time).en2 }}</div>
                  </template>
                  <template v-else>{{ session?.items?.[0]?.time }}</template>
                </div>
              <div class="discussion-participants">
                <div class="discussion-participant" v-for="(p, pIdx) in session.participants" :key="pIdx">
                  <span class="participant-name" :class="{ 'cell-merged': isSameText(p?.name, p?.nameEn) }">
                    <template v-if="isSameText(p?.name, p?.nameEn)">{{ p?.name }}</template>
                    <template v-else>
                      <div class="cell-zh">{{ p?.name }}</div>
                      <div class="cell-en">{{ p?.nameEn }}</div>
                    </template>
                  </span>
                  <span class="participant-title" v-if="p?.speakerTitle || p?.speakerTitleEn" :class="{ 'cell-merged': isSameText(p?.speakerTitle, p?.speakerTitleEn) }">
                    <template v-if="isSameText(p?.speakerTitle, p?.speakerTitleEn)">{{ getBilingualText(p?.speakerTitle, p?.speakerTitleEn) }}</template>
                    <template v-else>
                      <div class="cell-zh">{{ p?.speakerTitle }}</div>
                      <div class="cell-en">{{ p?.speakerTitleEn }}</div>
                    </template>
                  </span>
                  <span class="participant-affiliation" v-if="p?.affiliation || p?.affiliationEn" :class="{ 'cell-merged': isSameText(p?.affiliation, p?.affiliationEn) }">
                    <template v-if="isSameText(p?.affiliation, p?.affiliationEn)">{{ getBilingualText(p?.affiliation, p?.affiliationEn) }}</template>
                    <template v-else>
                      <div class="cell-zh">{{ p?.affiliation }}</div>
                      <div class="cell-en">{{ p?.affiliationEn }}</div>
                    </template>
                  </span>
                </div>
              </div>
              <div class="discussion-topic">
                <template v-if="session?.topicContent || session?.topicContentEn">
                  <div class="topic-content-zh" v-if="session?.topicContent">
                    <p v-for="(line, li) in session.topicContent.split('\n')" :key="'zh-'+li" :class="{ 'topic-sub-item': line.trim().startsWith('-') }">{{ line.trim() }}</p>
                  </div>
                  <div class="topic-content-en" v-if="session?.topicContentEn">
                    <p v-for="(line, li) in session.topicContentEn.split('\n')" :key="'en-'+li" :class="{ 'topic-sub-item': line.trim().startsWith('-') }">{{ line.trim() }}</p>
                  </div>
                </template>
                <template v-else>{{ getBilingualText(session?.title, session?.titleEn) }}</template>
              </div>
            </div>
          </template>
        </div>
      </div>
      <div class="day-footer">
        <p>注：最终日程以现场为准</p>
        <p class="footer-en">Note: Final official schedule will be published onsite</p>
      </div>
    </div>
    <!-- 日程之间的分割线 -->
    <div class="day-divider" v-if="dayIdx < scheduleData.length - 1"></div>
    </template>
  </div>

  <!-- 添加日程弹窗 -->
  <!-- <AddScheduleModal ref="addScheduleModalRef" /> -->
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import AddScheduleModal from '@/components/business/AddScheduleModal.vue'
import { onMounted } from 'vue'
import { useMeetingStore } from '@/stores/meeting'
import { getArrangeList } from '@/api/portal/arrange'
import { useGuestStore } from '@/stores/guest'
import { storeToRefs } from 'pinia'

const { t, locale, messages } = useI18n()
const langStore = useLangStore()
const meetingStore = useMeetingStore()
const guestStore = useGuestStore()
const { hwList } = storeToRefs(guestStore)
const objVal = computed(() => {
  return hwList.value?.[0]
})

const roomTagRefs = ref([])
const topicTagRefs = ref([])
const processedMeetingList = ref([])

const initMeetingList = () => {
  const originList = messages.value[locale.value]?.preMeeting?.list || []
  processedMeetingList.value = originList.map(item => ({
    ...item,
    lineCount: 1,
    isMultiLine: false
  }))
  roomTagRefs.value = []
  topicTagRefs.value = []
  calculateLineCounts()
}

const calculateLineCounts = async () => {
  await nextTick()
  await nextTick()
  const newList = processedMeetingList.value.map((item, index) => {
    const topicEl = topicTagRefs.value[index]
    let lineCount = 1
    let isMultiLine = false
    if (topicEl) {
      const style = window.getComputedStyle(topicEl)
      const lineHeight = parseFloat(style.lineHeight) || 30
      const height = topicEl.clientHeight
      const paddingTop = parseFloat(style.paddingTop) || 0
      const paddingBottom = parseFloat(style.paddingBottom) || 0
      const contentHeight = height - paddingTop - paddingBottom
      lineCount = Math.round(contentHeight / lineHeight)
      isMultiLine = lineCount > 1
    }
    return {
      ...item,
      lineCount,
      isMultiLine
    }
  })
  processedMeetingList.value = newList
}

const setRoomTagRef = (el, index) => {
  if (el) roomTagRefs.value[index] = el
}
const setTopicTagRef = (el, index) => {
  if (el) topicTagRefs.value[index] = el
}

const preMeetingList = computed(() => processedMeetingList.value)

const listArrange = ref([])

const fetchPreMeetingData = async () => {
  const res = await getArrangeList()
  if (res?.msg === '成功') {
    listArrange.value = JSON.parse(JSON.stringify(res?.data.map(item => ({
      room: item?.arrangeRoom,
      topic: item?.arrangeContent,
      arrangeType: item?.arrangeType,
      lineCount: 1,
      isMultiLine: true
    }))))
  }
}

// HEADER类型的会前安排数据（标题+描述），用于控制pre-meeting-header区域显示
const headerArrange = computed(() => {
  return listArrange.value.find(item => item.arrangeType === 'HEADER')
})

// BLOCK类型的会前安排数据（板块列表），用于渲染pre-meeting-list
const blockArrangeList = computed(() => {
  return listArrange.value.filter(item => item.arrangeType !== 'HEADER')
})

const currentLang = computed(() => langStore?.currentLang)

// 使用新API数据源（meetingStore.scheduleData 来自 getAllSchedules）
const scheduleData = computed(() => meetingStore?.scheduleData || [])

// 判断条目是否为特殊类型（开幕式/闭幕式/茶歇/中餐，独占整行显示）
// 注意：GENERAL_SPEECH 不在此列，它参与时间分组，在同时间段内合并显示
const isItemSpecialType = (item) => {
  return item?.itemType === 'OPENING' || item?.itemType === 'CLOSING' || item?.itemType === 'BREAK' || item?.itemType === 'LUNCH'
}

// 判断条目是否为大会发言类型（参与时间分组，但渲染时只显示类型名称）
const isItemGeneralSpeech = (item) => {
  return item?.itemType === 'GENERAL_SPEECH'
}

// 判断topic是否为待发布状态
const isPendingTopic = (item) => {
  return item?.topic === '待发布' || item?.topicEn === 'TBD'
}

// 获取特殊类型的显示文本
const getSpecialTypeText = (itemType, lang) => {
  const map = {
    'OPENING': { zh: '开幕式致辞', en: 'Opening Remarks' },
    'CLOSING': { zh: '闭幕式', en: 'Closing Ceremony' },
    'BREAK': { zh: '茶歇/壁报', en: 'Tea/Poster Break' },
    'LUNCH': { zh: '中餐-专题会', en: 'Lunchtime Symposium' },
    'GENERAL_SPEECH': { zh: '大会发言', en: 'Free Paper' }
  }
  const text = map[itemType]
  if (!text) return ''
  if (lang) return text[lang]
  return currentLang.value === 'zh' ? text.zh : text.en
}

// 获取特殊类型的CSS类名
const getSpecialTypeClass = (itemType) => {
  const map = {
    'OPENING': 'opening-full',
    'CLOSING': 'closing-full',
    'BREAK': 'break-full',
    'LUNCH': 'lunch-full'
  }
  return map[itemType] || ''
}

// 判断条目是否为特殊行（茶歇等）
const isItemSpecial = (item) => {
  return item?.isSpecial
}

// 将条目按sortOrder顺序处理为渲染块：
// - 开幕式/闭幕式/茶歇/中餐 → special块（独占整行）
// - 大会发言 → 参与同时间段的时间分组，在timeGroup内特殊渲染
// - 其他正常条目 → 按时间分组
const getItemRenderBlocks = (items) => {
  if (!items || !items.length) return []
  const blocks = []
  let i = 0
  let rowIndex = 0  // 全局行计数器，用于隔行变色
  while (i < items.length) {
    const item = items[i]
    if (isItemSpecialType(item)) {
      // 开幕式/闭幕式/茶歇/中餐: 独立special块
      blocks.push({ type: 'special', itemType: item.itemType, time: item.time || '', rowIndex: rowIndex++ })
      i++
    } else {
      // 正常条目+大会发言: 收集连续同时间的条目为一个timeGroup块
      // 大会发言也参与时间分组，与同时间的KEYNOTE等条目合并显示
      const groupItems = [item]
      const time = item.time || ''
      while (i + 1 < items.length && !isItemSpecialType(items[i + 1]) && (items[i + 1].time || '') === time) {
        i++
        groupItems.push(items[i])
      }
      // 为每个条目附加全局行索引
      const indexedItems = groupItems.map(gi => ({ ...gi, rowIndex: rowIndex++ }))
      blocks.push({ type: 'timeGroup', time, items: indexedItems })
      i++
    }
  }
  return blocks
}

// 获取双语文本
const getBilingualText = (zhText, enText) => {
  return currentLang.value === 'zh' ? zhText : (enText || zhText)
}

// 判断中英文文本是否相同（用于自动合并单元格）
const isSameText = (zhText, enText) => {
  if (!zhText && !enText) return true
  if (!zhText || !enText) return false
  return zhText.trim() === enText.trim()
}

// 获取时间段的双语文本（上午/下午等中文时间段翻译为英文）
// 英文拆分为en1/en2两行，确保强制三行显示（中文+英文第一行+英文第二行）
const getTimeText = (time) => {
  if (!time) return ''
  const timeMap = {
    '上午': { zh: '上午', en1: 'Morning', en2: 'Session' },
    '下午': { zh: '下午', en1: 'Afternoon', en2: 'Session' },
  }
  const mapped = timeMap[time?.trim()]
  if (mapped) {
    return mapped
  }
  return null // 非上午/下午的时间段（如 08:00-09:00）
}

// 拆分英文时间段文本为两行（如"Afternoon Session"→["Afternoon","Session"]）
const splitEnPeriod = (enText) => {
  if (!enText) return null
  const parts = enText.split(' ')
  if (parts.length >= 2) {
    return { en1: parts[0], en2: parts.slice(1).join(' ') }
  }
  return { en1: enText, en2: '' }
}

const fetchData = async () => {
  await meetingStore.fetchAllSchedules()
}

//调用查询日程的方法
onMounted(async () => {
  await fetchPreMeetingData()
  initMeetingList()
  fetchData()
})

watch(locale, () => {
  initMeetingList()
})

watch(() => langStore.currentLang, async () => {
  await fetchPreMeetingData()
  initMeetingList()
  fetchData()
})

// 弹窗引用
const addScheduleModalRef = ref()

// 打开添加日程弹窗
const openAddModal = () => {
  addScheduleModalRef.value.open()
}
</script>

<style scoped>
.pre-meeting-wrapper {
  width: 100%;
  background: #ffffff;
  border-radius: 12px;
  padding: 0 0 40px 0;
  box-sizing: border-box;
  overflow: hidden;
}

.huiHead {
  width: 100%;
  margin-bottom: 20px;
}

.huiHead .el-image {
  width: 100%;
  height: auto;
}

.pre-meeting-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 0 24px;
  box-sizing: border-box;
}

.pre-meeting-header-inner {
  display: block;
  margin: 0 auto;
  background: #E9E5FC;
  border-radius: 12px;
  padding: 20px 24px;
  box-sizing: border-box;
  width: 100%;
}

.pre-meeting-title {
  font-size: 24px;
  font-weight: 800;
  color: #000000;
  margin: 0;
  line-height: 1.2;
}

.pre-meeting-desc {
  font-size: 15px;
  font-weight: 400;
  color: #000000;
  margin: 0;
  line-height: 1.5;
}

.pre-meeting-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 0 24px;
  box-sizing: border-box;
}

.meeting-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #E9E5FC;
  border-radius: 12px;
}

@media (max-width: 768px) {
  .pre-meeting-list {
    grid-template-columns: 1fr;
  }
  .meeting-item {
  }
  .room-tag {
    min-width: unset;
    width: 100%;
  }
}

.room-tag {
  flex-shrink: 0;
  min-width: 180px;
  padding: 10px 16px;
  background: none;
  border-radius: 8px;
  color: #000000;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  display: flex;
  align-items: center;
  justify-content: center;
  height: auto;
}

.room-tag.align-top {
  align-items: flex-start !important;
  justify-content: center !important;
}

.meeting-topic {
  flex: 1;
  text-align: left;
  font-size: 15px;
  font-weight: 500;
  color: #000000;
  line-height: 1.5;
  word-break: break-word;
  background: none;
}

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
  margin-bottom: 60px;
}
/* 日程之间的分割线 */
.day-divider {
  border: none;
  border-top: 2px solid #7952b3;
  margin: 0 0 60px 0;
  opacity: 0.3;
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
.day-info .week-bilingual {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.day-info .week-bilingual .cell-zh {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}
.day-info .week-bilingual .cell-en {
  font-size: 12px;
  color: #888;
}
.hall-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  color: #333;
}
.hall-info .hall-bilingual {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.hall-info .hall-bilingual .cell-zh {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}
.hall-info .hall-bilingual .cell-en {
  font-size: 12px;
  color: #888;
}
.loc-icon {
  width: 18px;
  height: 18px;
}
.schedule-table {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}
.table-header {
  display: flex;
  background: #f8f5fc;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #ddd;
}
/* 表头样式 */
/* 通用双语显示样式：中文在上英文在下 */
.bilingual-zh,
.bilingual-en {
  display: block;
  line-height: 1.4;
}
.bilingual-zh {
  font-size: inherit;
  font-weight: 600;
}
.bilingual-en {
  font-size: 0.85em;
  opacity: 0.8;
}
.table-body {
  display: flex;
  flex-direction: column;
}
/* 板块标题行 - 板块名称放在时间、类型栏上面 */
.session-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #7952b3, #9d75d6);
  color: #fff;
  border-bottom: 1px solid #ddd;
  padding: 10px 12px;
}
.session-header-row .session-title-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.session-header-row .session-title {
  font-size: 15px;
  font-weight: 700;
}
.session-header-row .session-title-en {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 2px;
}
.session-header-row .session-chair {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.session-header-row .session-chair .chair-zh {
  font-size: 15px;
  font-weight: 700;
}
.session-header-row .session-chair .chair-en {
  font-size: 15px;
  font-weight: 700;
  opacity: 0.85;
  margin-top: 2px;
}
/* 开幕式/闭幕式板块标题背景色 */
.session-header-ceremony {
  background: #48DA09 !important;
  justify-content: center !important;
  flex-direction: column !important;
  align-items: center !important;
  gap: 2px;
}
.session-header-ceremony .session-title-wrap {
  align-items: center;
}
.session-header-ceremony .session-chair {
  margin-top: 2px;
}
/* 特殊条目行 - 独占整行（单单元格） */
.special-full-row {
  border-bottom: 1px solid #ddd;
  font-weight: 600;
  font-size: 14px;
  text-align: center;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lunch-full {
  background: #FFFFE2 !important;
  color: #333;
}
.break-full {
  background: #F5F5F6 !important;
  color: #333;
}
.opening-full {
  background: #FFFFFF !important;
  color: #333;
}
.closing-full {
  background: #6B8E23 !important;
  color: #fff;
}
.special-full-text {
  font-size: 14px;
}
.special-full-text .bilingual-zh {
  margin-right: 12px;
}
.special-full-text .bilingual-en {
  font-weight: 400;
  opacity: 0.85;
}
/* 茶歇/壁报图标 */
.break-icon {
  margin-right: 6px;
  font-size: 16px;
}
/* 待发布topic样式 - 浅灰色斜体 */
.topic-pending {
  color: #bbb !important;
  font-style: italic;
}
/* 时间列三行显示 */
.time-text-line {
  display: block;
  line-height: 0.4;
}
/* 表格行和列对齐 */
.table-row {
  display: flex;
  border-bottom: 1px solid #ddd;
  font-size: 13px;
}
/* 隔行变色：基于全局行索引 */
.row-odd {
  background: #fff;
}
.row-even {
  background: #f0f0f0;
}
.table-row:last-child {
  border-bottom: none;
}
.th,
.td {
  padding: 8px 6px;
  word-break: break-word;
  overflow-wrap: break-word;
  line-height: 1.4;
  border-right: 1px solid #ddd;
}
.th:last-child,
.td:last-child {
  border-right: none;
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
  vertical-align: middle;
}
/* 双语行样式 - 中文行 */
.cell-zh {
  font-size: 13px;
  line-height: 1.4;
  font-weight: 600;
}
/* 双语行样式 - 英文行 */
.cell-en {
  font-size: 12px;
  color: #888;
  line-height: 1.4;
}
/* 讲者列英文名加粗 */
.td-speaker .cell-en {
  font-weight: 600;
}
/* 讲者列允许换行 */
.td-speaker {
  word-break: break-word;
  overflow-wrap: break-word;
}
/* 讲者中英相同时合并 - 垂直居中占满同行高度 */
.speaker-merged {
  flex-direction: row !important;
  align-items: center !important;
  justify-content: flex-start !important;
  min-height: 40px;
  font-weight: 600;
}
.td-time {
  text-align: center;
  vertical-align: middle;
}
.td-type,
.td-speaker,
.td-affiliation {
  text-align: left;
  vertical-align: middle;
  display: flex;
  align-items: flex-start;
  flex-direction: column;
  justify-content: center;
}
.td-topic {
  text-align: left;
  vertical-align: middle;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
}
.time-group-items .table-row.special-row {
  background: #f8f5fc !important;
}
/* 大会发言行 - 在时间分组内合并4列为一列，白色背景 */
.time-group-items .table-row.general-speech-row {
  background: #fff !important;
}
.td-merged-general-speech {
  width: 100%;
  text-align: center;
  font-weight: 600;
  color: #333;
  padding: 8px 6px;
  border-right: none;
}
/* 时间分组 - 合并同时间段单元格 */
.time-group {
  display: flex;
  border-bottom: 1px solid #ddd;
}
.time-group:last-child {
  border-bottom: none;
}
.time-cell-merged {
  width: 12%;
  flex-shrink: 0;
  text-align: center;
  color: #666;
  padding: 8px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #ddd;
  font-size: 13px;
  line-height: 1.4;
  vertical-align: middle;
}
.time-group-items {
  flex: 1;
  min-width: 0;
}
.time-group-items .table-row {
  border-bottom: 1px solid #ddd;
}
.time-group-items .table-row:last-child {
  border-bottom: none;
}
.time-group-items .td {
  border-right: 1px solid #ddd;
}
.time-group-items .td:last-child {
  border-right: none;
}
/* time-group-items占88%宽度，内部td需按88%比例重算以对齐表头 */
.time-group-items .td-type {
  width: 11.36%;
  flex-shrink: 0;
}
.time-group-items .td-topic {
  width: 43.18%;
  flex-shrink: 0;
}
.time-group-items .td-speaker {
  width: 17.05%;
  flex-shrink: 0;
}
.time-group-items .td-affiliation {
  width: 28.41%;
  flex-shrink: 0;
}
.day-footer {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  text-align: left;
}
.day-footer .footer-en {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}
/* Panel Discussion 参与人行 - 表格内占满一行 */
/* Panel Discussion 三列布局 */
.discussion-table {
  display: flex;
  border-bottom: 1px solid #ddd;
  font-size: 13px;
}
.discussion-time {
  width: 15%;
  flex-shrink: 0;
  padding: 10px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #ddd;
  font-weight: 600;
  text-align: center;
}
.discussion-participants {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ddd;
}
.discussion-participant {
  padding: 8px 10px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.4;
  flex: 1;
  width: 100%;
}
.discussion-participant:nth-child(odd) {
  background: #fff;
}
.discussion-participant:nth-child(even) {
  background: #f0f0f0;
}
.discussion-participant:last-child {
  border-bottom: none;
}
.discussion-participant .participant-name {
  font-weight: 600;
  color: #333;
  text-align: left;
  display: block;
  width: 100%;
}
/* Discussion参与人英文名加粗 */
.participant-name .cell-en {
  font-weight: 600;
}
.discussion-participant .cell-zh,
.discussion-participant .cell-en {
  display: block;
}
.discussion-participant .participant-title {
  color: #999;
  font-size: 12px;
}
.discussion-participant .participant-affiliation {
  color: #999;
  font-size: 12px;
}
.discussion-topic {
  width: 35%;
  flex-shrink: 0;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  font-weight: 600;
  color: #333;
}
.discussion-topic .topic-content-zh,
.discussion-topic .topic-content-en {
  margin: 0;
  width: 100%;
  text-align: left;
}
.discussion-topic .topic-content-en {
  margin-top: 6px;
  color: #666;
  font-weight: 400;
  font-size: 13px;
}
.discussion-topic p {
  margin: 2px 0;
  line-height: 1.5;
  text-align: left;
  width: 100%;
}
.discussion-topic .topic-sub-item {
  padding-left: 8px;
  font-weight: 400;
  font-size: 13px;
}
@media (max-width: 1300px) {
  .th-time,
  .td-time,
  .time-cell-merged {
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
  /* time-group-items占85%宽度，内部td需按85%比例重算 */
  .time-group-items .td-type {
    width: 14.12%;
  }
  .time-group-items .td-topic {
    width: 38.82%;
  }
  .time-group-items .td-speaker {
    width: 17.65%;
  }
  .time-group-items .td-affiliation {
    width: 29.41%;
  }
}
@media (max-width: 992px) {
  .schedule-table {
    overflow-x: auto;
  }
  .table-header,
  .table-body,
  .session-header-row {
    min-width: 800px;
  }
}
</style>
