<template>
  <div class="privacy-container">
    <div class="section">
      <h3 class="section-title">{{ t('register.privacyPolicy') }}</h3>
      <p class="desc">
        {{ t('register.dataUseInfo') }}
        <a href="#" class="link">{{ t('register.dataProtectionPolicy') }}</a>
        {{ t('register.mciDataProcessor') }}
      </p>
      <div class="checkbox-item">
        <el-checkbox v-model="agreePolicy1" class="required-checkbox">
          {{ t('register.agreeDataPrivacy') }}
          <span class="required-mark">*</span>
        </el-checkbox>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">{{ t('register.icoDataPrivacy') }}</h3>
      <p class="desc">
        {{ t('register.icoPrivacyInfo') }}
        <a href="#" class="link">{{ t('register.icoPrivacyPolicy') }}</a>。
      </p>
      <div class="checkbox-item">
        <el-checkbox v-model="agreePolicy2" class="required-checkbox">
          {{ t('register.agreeIcoPrivacy') }}
          <span class="required-mark">*</span>
        </el-checkbox>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">{{ t('register.information') }}</h3>
      <p class="desc">
        {{ t('register.optInInfo') }}
      </p>
      <div class="checkbox-item">
        <el-checkbox v-model="agreeThirdParty">
          {{ t('register.optInThirdParty') }}
          <span class="required-mark">*</span>
        </el-checkbox>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">{{ t('register.badgeScanTitle') }}</h3>
      <p class="desc">
        {{ t('register.badgeScanDesc') }}
      </p>
      <p class="desc">
        {{ t('register.badgeScanConsent') }}
      </p>
      <div class="checkbox-item">
        <el-checkbox v-model="agreeBadgeScan" class="required-checkbox">
          {{ t('register.agreeBadgeScan') }}
          <span class="required-mark">*</span>
        </el-checkbox>
      </div>
    </div>

    <div class="btn-wrapper">
      <el-button type="primary" class="back-btn" @click="handleBack">{{ t('register.back') }}</el-button>
      <el-button v-if="allChecked" type="primary" class="continue-btn" @click="handleContinue">{{ t('register.next') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ElCheckbox, ElButton } from 'element-plus'
import { useRegisterStore } from '@/stores/register'

const router = useRouter()
const { t } = useI18n()
const registerStore = useRegisterStore()

const agreePolicy1 = ref(false)
const agreePolicy2 = ref(false)
const agreeThirdParty = ref(false)
const agreeBadgeScan = ref(false)

watch([agreePolicy1, agreePolicy2, agreeThirdParty, agreeBadgeScan], () => {
  registerStore.setConsentData({
    agreePolicy1: agreePolicy1.value,
    agreePolicy2: agreePolicy2.value,
    agreeThirdParty: agreeThirdParty.value,
    agreeBadgeScan: agreeBadgeScan.value
  })
})

onMounted(() => {
  const consent = registerStore.state.consent
  if (!agreePolicy1.value && !agreePolicy2.value && !agreeBadgeScan.value) {
    agreePolicy1.value = consent.agreePolicy1
    agreePolicy2.value = consent.agreePolicy2
    agreeThirdParty.value = consent.agreeThirdParty
    agreeBadgeScan.value = consent.agreeBadgeScan
  }
})

const allChecked = computed(() => {
  return agreePolicy1.value && agreePolicy2.value && agreeThirdParty.value && agreeBadgeScan.value
})

const handleBack = () => {
  router.push('/register/enroll')
}

const handleContinue = () => {
  if (!allChecked.value) {
    ElMessage.error(t('register.agreeAllRequired'))
    return
  }
  registerStore.state.consent = {
    agreePolicy1: agreePolicy1.value,
    agreePolicy2: agreePolicy2.value,
    agreeThirdParty: agreeThirdParty.value,
    agreeBadgeScan: agreeBadgeScan.value
  }
  router.push('/register/contact')
}
</script>

<style scoped>
.privacy-container {
  margin: 0 auto;
  padding: 30px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.7;
}

.section {
  margin-bottom: 35px;
}

.section-title {
  text-align: left;
  font-size: 18px;
  font-weight: 600;
  color: #009688; /* 青绿色标题 */
  margin-bottom: 12px;
}

.desc {
  font-size: 15px;
  color: #444;
  margin-bottom: 12px;
}

.link {
  color: #7d7ef4; /* 紫色链接 */
  text-decoration: underline;
}

.checkbox-item {
  font-size: 15px;
  color: #333;
}

.required-mark {
  color: #ff0000;
  margin-left: 4px;
}

.btn-wrapper {
  margin-top: 40px;
  display: flex;
  gap: 20px;
}

.back-btn, .continue-btn {
  background-color: #7d7ef4;
  border: none;
  padding: 12px 60px;
  font-size: 16px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #7d7ef4;
  border-color: #7d7ef4;
}
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #7d7ef4;
}
</style>
