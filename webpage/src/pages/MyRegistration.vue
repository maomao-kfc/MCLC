<!-- 我的注册：邮箱查询注册状态 -->

<template>
  <div class="my-registration-page">
    <div class="section-header">{{ t('myRegistration.title') }}</div>

    <!-- 邮箱查询区 -->
    <div v-if="!registrationData" class="search-section">
      <p class="search-tip">{{ t('myRegistration.searchTip') }}</p>
      <div class="search-box">
        <el-input
          v-model="searchEmail"
          :placeholder="t('myRegistration.emailPlaceholder')"
          size="large"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" size="large" :loading="searching" @click="handleSearch">
          {{ t('myRegistration.searchBtn') }}
        </el-button>
      </div>
      <p v-if="searchError" class="search-error">{{ searchError }}</p>
    </div>

    <!-- 查询结果 -->
    <div v-if="registrationData" class="result-section">
      <div class="back-search">
        <el-button text @click="resetSearch">{{ t('myRegistration.searchAgain') }}</el-button>
      </div>

      <!-- 注册成功 -->
      <div v-if="isRegistrationSuccess" class="status-card success-card">
        <div class="status-icon">✅</div>
        <div class="status-title">{{ t('myRegistration.successTitle') }}</div>
        <div class="status-desc">{{ t('myRegistration.successDesc') }}</div>
      </div>

      <!-- 审核中 -->
      <div v-else-if="isUnderReview" class="status-card reviewing-card">
        <div class="status-icon">⏳</div>
        <div class="status-title">{{ t('myRegistration.reviewingTitle') }}</div>
        <div class="status-desc">{{ t('myRegistration.reviewingDesc') }}</div>
      </div>

      <!-- 已驳回 -->
      <div v-else-if="isRejected" class="status-card rejected-card">
        <div class="status-icon">❌</div>
        <div class="status-title">{{ t('myRegistration.rejectedTitle') }}</div>
        <div class="status-desc">{{ t('myRegistration.rejectedDesc') }}</div>
        <div v-if="registrationData.proofRejectReason" class="rejected-reason">
          {{ t('myRegistration.rejectReason') }}：{{ registrationData.proofRejectReason }}
        </div>
      </div>

      <!-- 待提交凭证 -->
      <div v-else class="status-card pending-card">
        <div class="status-icon">📝</div>
        <div class="status-title">{{ t('myRegistration.pendingTitle') }}</div>
        <div class="status-desc">{{ t('myRegistration.pendingDesc') }}</div>
      </div>

      <!-- 注册详情 -->
      <div class="detail-card">
        <div class="detail-title">{{ t('myRegistration.detailTitle') }}</div>
        <div class="detail-rows">
          <div class="detail-row">
            <span class="detail-label">{{ t('myRegistration.name') }}</span>
            <span class="detail-value">{{ registrationData.surname }} {{ registrationData.name }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">{{ t('myRegistration.registerType') }}</span>
            <span class="detail-value">{{ registrationData.registerLabel || '-' }}</span>
          </div>
          <div class="detail-row" v-if="registrationData.paymentAmount">
            <span class="detail-label">{{ t('myRegistration.paymentAmount') }}</span>
            <span class="detail-value">{{ registrationData.paymentAmount }}</span>
          </div>
          <div class="detail-row" v-if="registrationData.actualPayment">
            <span class="detail-label">{{ t('myRegistration.actualPayment') }}</span>
            <span class="detail-value">{{ registrationData.actualPayment }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">{{ t('myRegistration.proofStatus') }}</span>
            <span class="detail-value">{{ proofStatusText }}</span>
          </div>
          <div class="detail-row" v-if="registrationData.proofSubmitTime">
            <span class="detail-label">{{ t('myRegistration.submitTime') }}</span>
            <span class="detail-value">{{ formatTime(registrationData.proofSubmitTime) }}</span>
          </div>
          <div class="detail-row" v-if="registrationData.registrationDate">
            <span class="detail-label">{{ t('myRegistration.registrationDate') }}</span>
            <span class="detail-value">{{ formatTime(registrationData.registrationDate) }}</span>
          </div>
        </div>
      </div>

      <!-- 其他会议（仅注册成功时显示） -->
      <div v-if="isRegistrationSuccess" class="other-conferences">
        <div class="other-conferences-header">{{ t('register.otherConferences') }}</div>
        <p class="other-conferences-tip">{{ t('register.otherConferencesTip') }}</p>
        <div class="conference-cards">
          <div v-for="conf in placeholderConferences" :key="conf.id" class="conference-card">
            <div class="conference-card-name">{{ conf.name }}</div>
            <div class="conference-card-status">{{ t('register.confComingSoon') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElButton, ElInput, ElMessage } from 'element-plus'
import { getRegistrationStatus } from '@/api/user'

const { t } = useI18n()

const searchEmail = ref('')
const searching = ref(false)
const searchError = ref('')
const registrationData = ref(null)

// 其他会议占位卡片
const placeholderConferences = [
  { id: 1, name: '小会一（名称待定）' },
  { id: 2, name: '小会二（名称待定）' },
  { id: 3, name: '小会三（名称待定）' },
  { id: 4, name: '小会四（名称待定）' }
]

// 注册状态判断
const isRegistrationSuccess = computed(() => {
  const s = registrationData.value?.proofStatus
  return s === 'paid' || s === 'offline_paid'
})
const isUnderReview = computed(() => {
  const s = registrationData.value?.proofStatus
  return s === 'proof_submitted' || s === 'review_passed'
})
const isRejected = computed(() => registrationData.value?.proofStatus === 'rejected')

const proofStatusText = computed(() => {
  const map = {
    proof_submitted: t('register.proofStatusPending'),
    review_passed: t('register.proofStatusReviewPassed'),
    paid: t('register.proofStatusPaid'),
    rejected: t('register.proofStatusRejected'),
    offline_paid: t('register.proofStatusOfflinePaid')
  }
  const s = registrationData.value?.proofStatus
  return s ? (map[s] || s) : t('myRegistration.proofNotSubmitted')
})

// 格式化时间（去掉秒和.0后缀）
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return String(timeStr).replace(/\.0$/, '').replace(/:\d{2}$/, '')
}

const handleSearch = async () => {
  const email = searchEmail.value.trim()
  if (!email) {
    searchError.value = t('myRegistration.emailRequired')
    return
  }
  searching.value = true
  searchError.value = ''
  try {
    const res = await getRegistrationStatus(email)
    if (res.code === '00000' && res.data) {
      registrationData.value = res.data
    } else {
      searchError.value = t('myRegistration.notFound')
    }
  } catch (e) {
    console.error('查询注册状态失败：', e)
    searchError.value = t('myRegistration.searchFail')
  } finally {
    searching.value = false
  }
}

const resetSearch = () => {
  registrationData.value = null
  searchEmail.value = ''
  searchError.value = ''
}
</script>

<style scoped>
.my-registration-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.section-header {
  background-color: #009688;
  color: white;
  font-size: 24px;
  font-weight: 600;
  padding: 12px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 30px;
}

.search-section {
  text-align: center;
  padding: 40px 20px;
}

.search-tip {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.search-box {
  display: flex;
  gap: 12px;
  max-width: 450px;
  margin: 0 auto;
}

.search-error {
  margin-top: 16px;
  font-size: 14px;
  color: #f56c6c;
}

.back-search {
  text-align: right;
  margin-bottom: 10px;
}

/* 状态卡片 */
.status-card {
  border-radius: 14px;
  padding: 32px 28px;
  text-align: center;
  margin-bottom: 24px;
}

.success-card {
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
}

.reviewing-card {
  background: #fdf6ec;
  border: 1px solid #faecd8;
}

.rejected-card {
  background: #fef0f0;
  border: 1px solid #fde2e2;
}

.pending-card {
  background: #f4f4f5;
  border: 1px solid #e9e9eb;
}

.status-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.status-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 10px;
}

.success-card .status-title { color: #67c23a; }
.reviewing-card .status-title { color: #e6a23c; }
.rejected-card .status-title { color: #f56c6c; }
.pending-card .status-title { color: #909399; }

.status-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.rejected-reason {
  margin-top: 12px;
  padding: 10px 14px;
  background: #fff;
  border-radius: 8px;
  font-size: 13px;
  color: #f56c6c;
  line-height: 1.5;
}

/* 注册详情 */
.detail-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.detail-label {
  font-size: 14px;
  color: #999;
  white-space: nowrap;
  flex-shrink: 0;
}

.detail-value {
  font-size: 14px;
  color: #333;
  text-align: right;
  word-break: break-all;
}

/* 其他会议 */
.other-conferences {
  margin-top: 10px;
}

.other-conferences-header {
  background-color: #009688;
  color: white;
  font-size: 18px;
  font-weight: 600;
  padding: 10px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 12px;
}

.other-conferences-tip {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin-bottom: 20px;
}

.conference-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.conference-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.25s;
}

.conference-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.conference-card-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.conference-card-status {
  font-size: 13px;
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 6px;
  padding: 4px 12px;
  display: inline-block;
}

@media (max-width: 768px) {
  .my-registration-page {
    padding: 10px;
  }
  .section-header {
    font-size: 18px;
    padding: 10px;
    margin-bottom: 20px;
  }
  .search-box {
    flex-direction: column;
  }
  .conference-cards {
    grid-template-columns: 1fr;
  }
}
</style>