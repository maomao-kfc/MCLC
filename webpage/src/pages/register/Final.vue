<template>
  <div class="confirm-page">
    <div class="section-header">{{ t('register.registrationConfirmed') }}</div>

    <div class="success-desc">
      {{ t('register.pleaseReview') }}
    </div>

    <div class="info-table">
      <div class="info-row">
        <span class="label">{{ t('register.lastName') }}:</span>
        <span class="value">{{ userInfo.lastName }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.firstName') }}:</span>
        <span class="value">{{ userInfo.firstName }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.phone') }}:</span>
        <span class="value">{{ userInfo.phone }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.email') }}:</span>
        <span class="value">{{ userInfo.email }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.title') }}:</span>
        <span class="value">{{ userInfo.title }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.organization') }}:</span>
        <span class="value">{{ userInfo.organization }}</span>
      </div>
    </div>

    <template v-if="!deductionApplied">
    <div class="section-header">{{ t('register.paymentSummary') }}</div>
    <div class="info-table">
      <div class="info-row">
        <span class="label">{{ t('register.paymentMethod') }}:</span>
        <span class="value">{{ paymentMethodText }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.total') }}:</span>
        <span class="value">{{ paymentInfo.total }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.totalPayable') }}:</span>
        <span class="value">{{ paymentInfo.dueTotal }}</span>
      </div>
      <div class="info-row">
        <span class="label">{{ t('register.amountReceived') }}:</span>
        <span class="value">{{ paymentInfo.received }}</span>
      </div>
    </div>
    </template>

    <div class="btn-group">
      <el-button class="confirm-btn" @click="handleConfirm">{{ t('register.confirm') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElButton } from 'element-plus'
import { useRegisterStore } from '@/stores/register'

const router = useRouter()
const { t } = useI18n()
const registerStore = useRegisterStore()

const paymentMethodLabels = { 1: t('register.creditCard'), 2: t('register.bankTransfer'), 3: t('register.wechat'), 4: t('register.alipay') }

const deductionApplied = computed(() => registerStore.state.deductionVerified && registerStore.state.enroll.registerType !== '1')

const userInfo = computed(() => {
  const contact = registerStore.state.contact
  return {
    lastName: contact.lastName || '',
    firstName: contact.firstName || '',
    phone: contact.phone || '',
    email: contact.email || '',
    title: contact.title || '',
    organization: contact.organization || ''
  }
})

const paymentInfo = computed(() => {
  const signup = registerStore.state.signup
  const finalPrice = signup.finalPrice || signup.registerTypePrice || 0
  const priceDisplay = signup.finalPriceDisplay || signup.registerTypePriceDisplay || ('0.00 ' + t('register.euro'))
  return {
    total: priceDisplay,
    dueTotal: priceDisplay,
    received: finalPrice.toFixed(2) + ' ' + t('register.euro')
  }
})

const paymentMethodText = computed(() => {
  return paymentMethodLabels[registerStore.state.paymentMethod] || t('register.notSelected')
})

const handleConfirm = () => {
  router.push({ path: '/', query: { login: 1 } })
  registerStore.clearRegisterData()
}
</script>

<style scoped>
.confirm-page {
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
  margin: 20px 0;
}

.success-desc {
  text-align: center;
  color: #444;
  margin-bottom: 30px;
}

.info-table {
  width: 400px;
  margin: 0 auto 30px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
}
.info-row .label {
  color: #444;
}
.info-row .value {
  text-align: right;
}

.btn-group {
  text-align: center;
  margin-top: 40px;
}
.confirm-btn {
  background-color: #7d7ef4;
  border: none;
  color: white;
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .confirm-page {
    padding: 10px;
  }
  .section-header {
    font-size: 18px;
    padding: 10px;
    margin: 12px 0;
  }
  .info-table {
    width: 100%;
  }
  .info-row {
    flex-direction: column;
    gap: 4px;
  }
  .info-row .value {
    text-align: left;
  }
  .confirm-btn {
    width: 100%;
  }
}
</style>
