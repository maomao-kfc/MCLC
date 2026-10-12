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
import { ElButton, ElInput, ElMessage } from 'element-plus'
import { useRegisterStore } from '@/stores/register'
import { useUserStore } from '@/stores/user'
import { getDiscountConfigurationList } from '@/api/discountConfiguration'
import { checkDeductionCode } from '@/api/contribution'
import { updateRegisterUser } from '@/api/user'

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
})

const handleBack = () => {
  router.push('/register/signup')
}

const handleConfirm = async () => {
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
    ElMessage.error(e?.response?.data?.msg || t('register.updateFailed'))
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
