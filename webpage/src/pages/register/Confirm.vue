<template>
  <div class="summary-page">
    <!-- 标题 -->
    <div class="section-header">{{ t('register.summary') }}</div>

    <div class="summary-content">
      <p class="tip">{{ t('register.reviewInfo') }}</p>

      <!-- 用户信息 -->
      <div class="user-name">{{ orderInfo.name }}</div>

      <!-- 订单明细 -->
       <div class="order-table" >
        <div class="row header">
          <span>{{ t('register.steps.signup') }}</span>
        </div>
        <div v-if="!isTeamRegister" class="row deduction-row">
          <el-input v-model="deductionCode" :placeholder="t('register.deductionCodePlaceholder')" size="small" class="deduction-input" :disabled="deductionVerified" />
          <el-button size="small" type="primary" @click="verifyDeductionCode" :loading="deductionLoading" :disabled="deductionVerified || !deductionCode">
            {{ t('register.verifyDeduction') }}
          </el-button>
        </div>
        <div v-if="!isTeamRegister && deductionMsg" class="deduction-msg" :class="deductionVerified ? 'success' : 'error'">
          {{ deductionMsg }}
        </div>
        <div class="row item" style="display: flex;
    flex-direction: column;">
          <span>{{ orderInfo.registerType }}</span>
          <span>{{ orderInfo.price }}</span>
        </div>
        <div class="row total">
          <span>{{ t('register.totalPayable') }}</span>
          <span>{{ orderInfo.originalTotal }}</span>
        </div>
        <div v-if="orderInfo.showActual" class="row actual">
          <span>{{ t('register.actualPayment') }}</span>
          <span>{{ orderInfo.actualTotal }}</span>
        </div>
      </div>

      <!-- 付款方式：二维码+链接 -->
      <div v-if="!totalIsZero" class="payment-section">
        <p class="payment-tip">{{ t('register.selectPaymentMethod') }}</p>

        <div class="payment-qr-card">
          <div class="qr-image-wrapper">
            <img :src="paymentQrImage" alt="支付二维码" class="qr-image" />
          </div>
          <div class="payment-link-area">
            <a :href="paymentLink" target="_blank" class="payment-link-btn">{{ t('register.clickToPay') }}</a>
          </div>

          <!-- 付款后上传凭证提醒（醒目，防用户漏看下方凭证区） -->
          <div class="proof-notice" @click="scrollToProof">
            <span class="proof-notice-icon">⚠️</span>
            <span class="proof-notice-text">{{ t('register.proofNotice') }}</span>
            <span class="proof-notice-arrow">↓</span>
          </div>
        </div>

        <!-- 支付凭证上传区 -->
        <div class="proof-section" id="proof-section">
          <div class="proof-title">{{ t('register.proofUploadTitle') }}</div>
          <p class="proof-tip">{{ t('register.proofUploadTip') }}</p>

          <!-- 已提交凭证：状态展示 -->
          <div v-if="proofStatus && proofStatus !== 'rejected'" class="proof-status-card">
            <el-tag :type="proofStatusTagType" size="large" effect="dark">{{ proofStatusText }}</el-tag>
            <p v-if="proofSubmitTime" class="proof-submit-time">{{ proofSubmitTime }}</p>
          </div>

          <!-- 凭证上传表单（未提交 或 已驳回可重传） -->
          <div v-if="!proofStatus || proofStatus === 'rejected'" class="proof-form">
            <div v-if="proofStatus === 'rejected'" class="proof-rejected-tip">
              {{ t('register.proofRejectReason') }}：{{ proofRejectReason || '-' }}，{{ t('register.proofResubmit') }}
            </div>

            <el-upload
              class="proof-upload"
              drag
              accept=".jpg,.jpeg,.png"
              :auto-upload="false"
              :limit="1"
              :on-change="handleProofFileChange"
              :on-remove="() => (proofFile = null)"
              :file-list="proofFileList"
            >
              <div class="proof-upload-inner">
                <span class="proof-upload-icon">📷</span>
                <span class="proof-upload-text">{{ t('register.proofSelectFile') }}</span>
                <span class="proof-upload-hint">jpg / png / jpeg，≤10MB</span>
              </div>
            </el-upload>

            <div class="proof-field">
              <label class="proof-label">{{ t('register.proofPayTime') }} *</label>
              <el-date-picker
                v-model="proofPayTime"
                type="datetime"
                :placeholder="t('register.proofPayTimePlaceholder')"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="proof-input"
              />
            </div>

            <div class="proof-field">
              <label class="proof-label">{{ t('register.proofAmount') }} *</label>
              <el-input v-model="proofAmount" :placeholder="t('register.proofAmountPlaceholder')" class="proof-input" />
            </div>

            <div class="proof-field">
              <label class="proof-label">{{ t('register.proofTradeNo') }}</label>
              <el-input v-model="proofTradeNo" :placeholder="t('register.proofTradeNoPlaceholder')" class="proof-input" />
            </div>

            <div class="proof-field">
              <label class="proof-label">{{ t('register.proofRemark') }}</label>
              <el-input v-model="proofRemark" type="textarea" :rows="2" class="proof-input" />
            </div>

            <el-button
              type="primary"
              class="proof-submit-btn"
              :loading="proofSubmitting"
              :disabled="!proofFile || !proofPayTime || !proofAmount"
              @click="handleSubmitProof"
            >
              {{ proofSubmitting ? t('register.proofSubmitting') : t('register.proofSubmitBtn') }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 按钮组 -->
      <div class="btn-group">
        <el-button class="back-btn" @click="handleBack">{{ t('register.back') }}</el-button>
        <el-button class="confirm-btn" :loading="confirmLoading" @click="handleConfirm">{{ t('register.nextStep') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElButton, ElInput, ElMessage, ElMessageBox } from 'element-plus'
import { useRegisterStore } from '@/stores/register'
import { useUserStore } from '@/stores/user'
import { getDiscountConfigurationList } from '@/api/discountConfiguration'
import { checkDeductionCode } from '@/api/contribution'
import { updateRegisterUser } from '@/api/user'
import { submitPaymentProof, getProofStatus } from '@/api/paymentProof'

const router = useRouter()
const { t } = useI18n()
const registerStore = useRegisterStore()
const userStore = useUserStore()
const discountList = ref([])

const deductionCode = ref(registerStore.state.deductionCode || '')
const deductionLoading = ref(false)
const deductionVerified = ref(registerStore.state.deductionVerified)
const deductionMsg = ref('')

const confirmLoading = ref(false)

// ========== 支付凭证相关状态 ==========
const proofStatus = ref(null) // null=未提交 / proof_submitted / review_passed / paid / rejected / offline_paid
const proofRejectReason = ref('')
const proofSubmitTime = ref('')
const proofFile = ref(null)
const proofFileList = ref([])
const proofPayTime = ref('')
const proofAmount = ref('')
const proofTradeNo = ref('')
const proofRemark = ref('')
const proofSubmitting = ref(false)

const proofStatusText = computed(() => {
  const map = {
    proof_submitted: t('register.proofStatusPending'),
    review_passed: t('register.proofStatusReviewPassed'),
    paid: t('register.proofStatusPaid'),
    rejected: t('register.proofStatusRejected'),
    offline_paid: t('register.proofStatusOfflinePaid')
  }
  return map[proofStatus.value] || ''
})

const proofStatusTagType = computed(() => {
  const map = {
    proof_submitted: 'warning',
    review_passed: 'primary',
    paid: 'success',
    rejected: 'danger',
    offline_paid: 'success'
  }
  return map[proofStatus.value] || 'info'
})

const handleProofFileChange = (uploadFile) => {
  const raw = uploadFile.raw
  if (!raw) return
  const okTypes = ['image/jpeg', 'image/jpg', 'image/png']
  if (!okTypes.includes(raw.type)) {
    ElMessage.error(t('register.proofFileTypeError'))
    proofFileList.value = []
    proofFile.value = null
    return
  }
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.error(t('register.proofFileSizeError'))
    proofFileList.value = []
    proofFile.value = null
    return
  }
  proofFile.value = raw
  proofFileList.value = [uploadFile]
}

const fetchProofStatus = async () => {
  const id = userStore.registrationId
  if (!id) return
  try {
    const res = await getProofStatus(id)
    if (res.code === '00000' && res.data) {
      proofStatus.value = res.data.proofStatus || null
      proofRejectReason.value = res.data.proofRejectReason || ''
      proofSubmitTime.value = res.data.proofSubmitTime || ''
      // 驳回后重传时，预填上次申报的金额
      if (proofStatus.value === 'rejected' && res.data.lastDeclaredAmount) {
        proofAmount.value = res.data.lastDeclaredAmount
      }
    }
  } catch (e) {
    console.error('查询凭证状态失败：', e)
  }
}

const handleSubmitProof = async () => {
  if (!proofFile.value || !proofPayTime.value || !proofAmount.value) return
  const id = userStore.registrationId
  if (!id) {
    ElMessage.error(t('register.registrationFailed'))
    return
  }
  proofSubmitting.value = true
  try {
    const res = await submitPaymentProof(proofFile.value, {
      registrationId: Number(id),
      declaredPayTime: proofPayTime.value,
      declaredAmount: proofAmount.value,
      declaredTradeNo: proofTradeNo.value || undefined,
      remark: proofRemark.value || undefined
    })
    if (res.code === '00000') {
      ElMessage.success(t('register.proofSubmitSuccess'))
      proofStatus.value = 'proof_submitted'
      // 清空表单
      proofFile.value = null
      proofFileList.value = []
      proofPayTime.value = ''
      proofAmount.value = ''
      proofTradeNo.value = ''
      proofRemark.value = ''
    } else {
      ElMessage.error(res.msg || t('register.uploadPicBankFail'))
    }
  } catch (e) {
    console.error('提交凭证失败：', e)
    ElMessage.error(e?.message || e?.response?.data?.msg || t('register.uploadPicBankFail'))
  } finally {
    proofSubmitting.value = false
  }
}

const totalIsZero = computed(() => {
  const price = calculatedPrice.value.finalPrice
  return price <= 0
})

// 根据注册类别决定支付二维码和链接
const paymentQrImage = computed(() => {
  const signup = registerStore.state.signup
  const typeName = signup.registerTypeName || ''
  // Category B (住院医师/学生) → studentRegistration.png
  if (typeName.includes('住院医师') || typeName.includes('学生') || typeName.includes('Resident') || typeName.includes('Student')) {
    return '/assets/image/patternOfPayment/studentRegistration.png'
  }
  // Category A (普通代表) 及其他付费类别 → regularRegistration.png
  return '/assets/image/patternOfPayment/regularRegistration.png'
})

const paymentLink = computed(() => {
  const signup = registerStore.state.signup
  const typeName = signup.registerTypeName || ''
  if (typeName.includes('住院医师') || typeName.includes('学生') || typeName.includes('Resident') || typeName.includes('Student')) {
    return 'https://cahpepx.kechuangfu.com/coursecenter/preview/180'
  }
  return 'https://cahpepx.kechuangfu.com/coursecenter/preview/179'
})



const isTeamRegister = computed(() => registerStore.state.enroll.registerType === '1')

const memberCount = computed(() => {
  if (!isTeamRegister.value) return 1
  const members = registerStore.state.contact.teamMembers
  if (!members || !Array.isArray(members)) return 1
  const validMembers = members.filter(m => m.firstName || m.lastName || m.email)
  return validMembers.length + 1
})

const matchedDiscount = computed(() => {
  if (!isTeamRegister.value || discountList.value.length === 0) return null
  const count = memberCount.value
  return discountList.value.find(
    item => count >= item.minPeople && count <= item.maxPeople
  ) || null
})

const calculatedPrice = computed(() => {
  const unitPrice = registerStore.state.signup.registerTypePrice || 0
  const count = memberCount.value
  const discount = matchedDiscount.value
  const discountRate = discount ? parseFloat(discount.discount) / 10 : 1
  let finalPrice = unitPrice * count * discountRate

  if (deductionVerified.value && !isTeamRegister.value) {
    finalPrice = 0
  }

  return { unitPrice, count, discount, discountRate, finalPrice }
})

const updateStorePrice = () => {
  const { count, discount, discountRate, finalPrice } = calculatedPrice.value
  registerStore.setSignupData({
    ...registerStore.state.signup,
    memberCount: count,
    discountRate: discountRate,
    finalPrice: parseFloat(finalPrice.toFixed(2)),
    finalPriceDisplay: finalPrice.toFixed(2) + ' ' + t('register.euro')
  })
}

const orderInfo = computed(() => {
  const contact = registerStore.state.contact
  const signup = registerStore.state.signup
  const name = `${contact.lastName || ''}${contact.firstName || ''}` || t('register.notFilled')
  const registerType = signup.registerTypeName || t('register.notSelected')
  const unitDisplay = signup.registerTypePriceDisplay || `0.00 ${t('register.euro')}`

  const { count, discount, discountRate, finalPrice, unitPrice } = calculatedPrice.value
  const euro = t('register.euro')

  let priceText
  if (isTeamRegister.value) {
    if (discount) {
      priceText = `${count} x ${unitDisplay} x ${discount.discount}折`
    } else {
      priceText = `${count} x ${unitDisplay}`
    }
  } else {
    priceText = `1 x ${unitDisplay}`
  }

  const originalTotal = (unitPrice * count).toFixed(2) + ' ' + euro
  const actualTotal = finalPrice.toFixed(2) + ' ' + euro
  const showActual = true

  return {
    name,
    registerType,
    price: priceText,
    originalTotal,
    actualTotal,
    showActual
  }
})

const fetchDiscounts = async () => {
  try {
    const res = await getDiscountConfigurationList()
    if (res.code === '00000' && res.data) {
      discountList.value = res.data
    }
  } catch (e) {
    console.error('获取折扣配置失败：', e)
  } finally {
    updateStorePrice()
  }
}

const verifyDeductionCode = async () => {
  const code = deductionCode.value.trim()
  if (!code) return
  deductionLoading.value = true
  deductionMsg.value = ''
  try {
    const res = await checkDeductionCode(code)
    if (res.code === '00000' && res.data && res.data.list && res.data.list.length > 0) {
      const item = res.data.list[0]
      if (item.deductionStatus === 0) {
        deductionVerified.value = true
        deductionMsg.value = t('register.deductionSuccess')
        registerStore.setDeductionVerified(true)
        registerStore.setDeductionCode(code)
      } else {
        deductionVerified.value = false
        deductionMsg.value = t('register.deductionInvalid')
      }
    } else {
      deductionVerified.value = false
      deductionMsg.value = t('register.deductionNotFound')
    }
  } catch (e) {
    console.error('校验抵扣码失败：', e)
    deductionVerified.value = false
    deductionMsg.value = t('register.deductionFail')
  } finally {
    deductionLoading.value = false
  }
}

onMounted(() => {
  fetchDiscounts()
  fetchProofStatus()
})

const handleBack = () => {
  router.push('/register/signup')
}

// 滚动到凭证上传区
const scrollToProof = () => {
  document.getElementById('proof-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleConfirm = async () => {
  // 需付款但未提交凭证时，弹窗提醒用户先上传凭证
  if (!totalIsZero.value && !proofStatus.value) {
    try {
      await ElMessageBox.confirm(
        t('register.proofMissingConfirm'),
        t('register.proofMissingTitle'),
        {
          confirmButtonText: t('register.proofGoUpload'),
          cancelButtonText: t('register.proofSkipContinue'),
          type: 'warning'
        }
      )
      // 用户选择去上传凭证，滚动到凭证区并中止
      scrollToProof()
      return
    } catch {
      // 用户选择继续，放行
    }
  }

  registerStore.setSignupData({
    ...registerStore.state.signup,
    registerLabel: registerStore.state.signup.registerTypeName
  })
  updateStorePrice()

  confirmLoading.value = true
  try {
    const id = userStore.registrationId
    if (id) {
      const state = registerStore.state
      const signup = state.signup
      const isTeam = state.enroll.registerType === '1'
      const memberCount = isTeam ? (signup.memberCount || 1) : 1
      const res = await updateRegisterUser(id, {
        id,
        paymentMethod: String(state.paymentMethod || 'qr_code'),
        picBank: state.picBank || '',
        paymentAmount: String(signup.finalPrice || (signup.registerTypePrice || 0) * memberCount),
        actualPayment: String((state.deductionVerified && state.deductionCode) ? 0 : ((signup.registerTypePrice || 0) * memberCount)),
        ...(state.deductionCode ? { deductionCode: state.deductionCode } : {})
      })
      if (res.code !== '00000') {
        ElMessage.error(res.msg || t('register.updateFailed'))
        return
      }
    }
  } catch (e) {
    console.error('更新支付信息失败', e)
    ElMessage.error(e?.response?.data?.msg || e?.message || t('register.updateFailed'))
    return
  } finally {
    confirmLoading.value = false
  }

  router.push('/register/final')
}
</script>

<style scoped>
.summary-page {
  max-width: 900px;
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

.summary-content {
  text-align: center;
}

.tip {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 25px;
}

.order-table {
  width: 300px;
  margin: 0 auto 30px;
  text-align: left;
}

.order-table .row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.order-table .header {
  font-weight: 600;
}

.order-table .total {
  font-weight: 600;
  border-top: 1px solid #333;
  padding-top: 10px;
}

.deduction-row {
  align-items: center;
  gap: 8px;
  padding: 6px 0;
}

.deduction-input {
  flex: 1;
}

.deduction-msg {
  font-size: 13px;
  text-align: right;
  padding-bottom: 4px;
}

.deduction-msg.success {
  color: #67c23a;
}

.deduction-msg.error {
  color: #f56c6c;
}

.payment-section {
  margin-bottom: 40px;
}

.payment-tip {
  font-size: 15px;
  margin-bottom: 15px;
}

.payment-qr-card {
  max-width: 400px;
  margin: 20px auto;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.qr-image-wrapper {
  display: inline-block;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}

.qr-image {
  width: 200px;
  height: 200px;
  display: block;
  border-radius: 8px;
  object-fit: contain;
}

.payment-link-area {
  margin-top: 12px;
}

.payment-link-btn {
  display: inline-block;
  padding: 12px 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  text-decoration: none;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s;
}

.payment-link-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 付款后上传凭证提醒条 */
.proof-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 16px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s;
}

.proof-notice:hover {
  background: #fff1cc;
  box-shadow: 0 2px 10px rgba(250, 173, 20, 0.25);
}

.proof-notice-icon {
  font-size: 16px;
}

.proof-notice-text {
  font-size: 14px;
  font-weight: 600;
  color: #d46b08;
  line-height: 1.4;
}

.proof-notice-arrow {
  font-size: 18px;
  font-weight: 700;
  color: #d46b08;
  animation: bounce-down 1.2s infinite;
}

@keyframes bounce-down {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(4px); }
}

/* ========== 支付凭证上传区 ========== */
.proof-section {
  max-width: 480px;
  margin: 30px auto 0;
  background: #fafbff;
  border: 1px solid #e4e7f5;
  border-radius: 14px;
  padding: 24px 28px;
  text-align: left;
}

.proof-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.proof-tip {
  font-size: 13px;
  color: #999;
  text-align: center;
  margin-bottom: 18px;
  line-height: 1.5;
}

.proof-status-card {
  text-align: center;
  padding: 20px 0;
}

.proof-submit-time {
  font-size: 12px;
  color: #aaa;
  margin-top: 10px;
}

.proof-rejected-tip {
  background: #fef0f0;
  color: #f56c6c;
  font-size: 13px;
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 16px;
  line-height: 1.5;
}

.proof-upload {
  width: 100%;
  margin-bottom: 16px;
}

.proof-upload :deep(.el-upload-dragger) {
  padding: 20px;
  border-radius: 10px;
}

.proof-upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.proof-upload-icon {
  font-size: 28px;
}

.proof-upload-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.proof-upload-hint {
  font-size: 12px;
  color: #bbb;
}

.proof-field {
  margin-bottom: 14px;
}

.proof-label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.proof-input {
  width: 100%;
}

.proof-submit-btn {
  width: 100%;
  margin-top: 6px;
  background-color: #009688;
  border-color: #009688;
  font-size: 15px;
  padding: 12px 0;
  border-radius: 8px;
}

.proof-submit-btn:hover {
  background-color: #00b3a4;
  border-color: #00b3a4;
}

.btn-group {
  display: flex;
  justify-content: space-between;
  padding: 0 100px;
}

.back-btn,
.confirm-btn {
  background-color: #7d7ef4;
  border: none;
  color: white;
  padding: 12px 60px;
  font-size: 16px;
  border-radius: 8px;
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}



.order-table .actual {
  font-weight: 600;
  border-top: 1px solid #333;
  padding-top: 10px;
  color: #e6a23c;
}

@media (max-width: 768px) {
  .summary-page {
    padding: 10px;
  }
  .section-header {
    font-size: 18px;
    padding: 10px;
    margin-bottom: 20px;
  }
  .order-table {
    width: 100%;
  }
  .payment-qr-card {
    max-width: 100%;
  }
  .proof-section {
    max-width: 100%;
    padding: 16px;
  }
  .btn-group {
    padding: 0;
    flex-direction: column;
    gap: 12px;
  }
  .back-btn,
  .confirm-btn {
    width: 100%;
    padding: 12px 0;
  }
}
</style>
