<!-- 审稿决议页面 -->
<template>
  <div class="review-file-page">
    <div class="lang-bar">
      <button :class="['lang-btn', { active: currentLang === 'en' }]" @click="switchLang('en')">English</button>
      <span class="divider">|</span>
      <button :class="['lang-btn', { active: currentLang === 'zh' }]" @click="switchLang('zh')">简体中文</button>
    </div>

    <div class="review-file-wrapper">
      <h2 class="page-title">{{ t('reviewFile.pageTitle') }}</h2>

      <!-- 加载中 -->
      <div v-if="loading" class="common-card review-card">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px; color: #666;">{{ t('reviewFile.loading') || 'Loading...' }}</p>
      </div>

      <!-- 已审核：显示审核结果 -->
      <div v-else-if="reviewedResult !== null" class="common-card review-card">
        <div class="review-card-header">
          <h3 class="review-card-title">{{ t('reviewFile.alreadyReviewedTitle') || 'Review Completed' }}</h3>
        </div>
        <div class="reviewed-info">
          <el-tag v-if="reviewedResult === 1" type="success" size="large" style="font-size: 16px; padding: 8px 24px;">
            {{ t('reviewFile.approve') }}
          </el-tag>
          <el-tag v-else-if="reviewedResult === 2" type="danger" size="large" style="font-size: 16px; padding: 8px 24px;">
            {{ t('reviewFile.reject') }}
          </el-tag>
          <p v-if="reviewedRejectReason" style="margin-top: 16px; color: #666;">
            {{ t('reviewFile.rejectReasonLabel') || 'Reject Reason' }}：{{ reviewedRejectReason }}
          </p>
          <p style="margin-top: 12px; color: #999; font-size: 13px;">
            {{ t('reviewFile.alreadyReviewedTip') || 'You have already reviewed this submission. No further action is needed.' }}
          </p>
        </div>
      </div>

      <!-- 未审核或查询失败：显示操作按钮 -->
      <div v-else class="common-card review-card">
        <div class="review-card-header">
          <h3 class="review-card-title">{{ t('reviewFile.confirmTitle') }}</h3>
        </div>

        <div class="review-actions">
          <el-button
            type="success"
            size="large"
            class="review-btn"
            @click="handleApprove"
          >
            {{ t('reviewFile.approve') }}
          </el-button>
          <el-button
            type="danger"
            size="large"
            class="review-btn"
            @click="handleReject"
          >
            {{ t('reviewFile.reject') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useLangStore } from '@/stores/lang.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { expertReview } from '@/api/contribution.js'
import request from '@/utils/request'

const { locale, t } = useI18n()
const route = useRoute()
const langStore = useLangStore()

const currentLang = computed(() => langStore.currentLang)

const switchLang = (lang) => {
  langStore.setLang(lang)
  locale.value = lang
}

const id = ref('')
const loading = ref(true)
const reviewedResult = ref(null)  // null=未审核/查询中, 1=已通过, 2=未通过
const reviewedRejectReason = ref('')

onMounted(async () => {
  id.value = route.query.id || ''
  if (!id.value) {
    loading.value = false
    return
  }

  // 页面加载时查询审核状态，防止已审核的记录被重复操作
  try {
    const res = await request({
      url: '/api/v1/paperSubmission/emailRecord/' + id.value,
      method: 'get',
    })
    if (res.data && res.data.reviewResult != null && res.data.reviewResult !== 0) {
      reviewedResult.value = res.data.reviewResult
      reviewedRejectReason.value = res.data.rejectReason || ''
    }
  } catch (e) {
    // 查询失败不影响页面显示，允许用户继续操作
    console.warn('查询审核状态失败', e)
  } finally {
    loading.value = false
  }
})

const handleApprove = () => {
  ElMessageBox.confirm(
    t('reviewFile.approveConfirm'),
    t('reviewFile.pageTitle'),
    { confirmButtonText: t('reviewFile.approve'), cancelButtonText: t('reviewFile.cancel'), type: 'success' }
  ).then(async () => {
    try {
      await expertReview(id.value, 1)
      ElMessage.success(t('reviewFile.approveSuccess'))
      reviewedResult.value = 1  // 更新页面状态，防止重复操作
    } catch (err) {
      ElMessage.error(err?.response?.data?.msg || t('reviewFile.operateFail'))
    }
  }).catch(() => {})
}

const handleReject = () => {
  ElMessageBox.prompt(
    t('reviewFile.rejectReasonPlaceholder'),
    t('reviewFile.rejectReasonTitle'),
    {
      confirmButtonText: t('reviewFile.reject'),
      cancelButtonText: t('reviewFile.cancel'),
      type: 'warning',
      inputType: 'textarea',
      inputPlaceholder: t('reviewFile.rejectReasonPlaceholder'),
      inputValidator: (val) => {
        if (!val || !val.trim()) {
          return t('reviewFile.rejectReasonRequired')
        }
        return true
      }
    }
  ).then(async ({ value }) => {
    try {
      await expertReview(id.value, 2, value)
      ElMessage.success(t('reviewFile.rejectSuccess'))
      reviewedResult.value = 2  // 更新页面状态
      reviewedRejectReason.value = value
    } catch (err) {
      ElMessage.error(err?.response?.data?.msg || t('reviewFile.operateFail'))
    }
  }).catch(() => {})
}
</script>

<style scoped>
.review-file-page {
  width: 100%;
  min-height: 100vh;
}

.lang-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 24px;
  background: #fff;
}

.lang-btn {
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 18px;
  font-size: 14px;
  color: #333;
  transition: color 0.2s;
}

.lang-btn.active {
  color: #114C9A;
  font-weight: 600;
}

.divider {
  margin: 0 6px;
  color: #bdc3c7;
}

.review-file-wrapper {
  width: 100%;
  min-height: 80vh;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(90deg, #114C9A, #7952b3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 32px;
  text-align: center;
}

.common-card {
  border-radius: 12px;
  background: #fff;
  padding: 40px;
  width: 100%;
  max-width: 560px;
  text-align: center;
}

.review-card-header {
  margin-bottom: 40px;
}

.review-card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.review-subtitle {
  font-size: 15px;
  color: #666;
}

.review-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.review-btn {
  width: 160px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.reviewed-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
</style>
