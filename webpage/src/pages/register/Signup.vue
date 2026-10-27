

<template>
  <div class="register-container">
    <!-- 大会登记模块 -->
    <div class="section">
      <div class="section-header">{{ t('register.conferenceRegistration') }}</div>

      <div class="content">
        <p class="desc-title">{{ t('register.registrationFeeIncludes') }}</p>
        <ul class="benefit-list">
          <li v-for="(item, idx) in feeItems" :key="idx">{{ item }}</li>
        </ul>

        <div class="fee-radio-group">
          <div
              class="fee-item"
              v-for="item in feeList"
              :key="item.value"
              :class="{ active: formData.registerType === item.value }"
          >
            <el-radio v-model="formData.registerType" :label="item.value">
              {{ item.label }}
            </el-radio>
            <span class="price">{{ item.price }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 签证邀请函模块 -->
    <div class="section">
      <div class="section-header">{{ t('register.visaInvitation') }}</div>

      <div class="content">
        <p class="desc">
          {{ t('register.visaDesc1') }}
          <a href="#" class="link">{{ t('register.visaDescLink') }}</a>。
        </p>
        <p class="desc">
          {{ t('register.visaDesc2') }}
        </p>

        <el-checkbox v-model="formData.visaIsNeed">
          {{ t('register.needVisaInvitation') }}
        </el-checkbox>

        <!-- 签证信息表单（勾选后才显示） -->
        <div v-if="formData.visaIsNeed" class="visa-form">
          <p class="visa-title">{{ t('register.visaInfo') }}</p>
          <p class="visa-tip">{{ t('register.visaTip') }}</p>

           <el-form :model="formData.visaInfo" label-position="top">
             <el-form-item :label="t('register.passportFirstName')" required>
               <el-input v-model="formData.visaInfo.visaName" />
             </el-form-item>

             <el-form-item :label="t('register.passportLastName')" required>
               <el-input v-model="formData.visaInfo.visaSurname" />
             </el-form-item>

             <el-form-item :label="t('register.passportNumber')" required>
               <el-input v-model="formData.visaInfo.visaPassport" />
             </el-form-item>

             <el-form-item :label="t('register.passportIssueDate')" required>
               <el-date-picker
                   v-model="formData.visaInfo.visaIssueTime"
                   type="date"
                   format="YYYY/MM/DD"
                   value-format="YYYY-MM-DD"
                   placeholder="年/月/日"
                   style="width: 100%"
               />
             </el-form-item>

             <el-form-item :label="t('register.passportExpiryDate')" required>
               <el-date-picker
                   v-model="formData.visaInfo.visaEndTime"
                   type="date"
                   format="YYYY/MM/DD"
                   value-format="YYYY-MM-DD"
                   placeholder="年/月/日"
                   style="width: 100%"
               />
             </el-form-item>

             <el-form-item :label="t('register.birthDate')" required>
               <el-date-picker
                   v-model="formData.visaInfo.visaTime"
                   type="date"
                   format="YYYY/MM/DD"
                   value-format="YYYY-MM-DD"
                   placeholder="年/月/日"
                   style="width: 100%"
               />
             </el-form-item>

             <el-form-item :label="t('register.visaOrigin')" required>
               <el-select v-model="formData.visaInfo.visaArea" placeholder="请选择国家" style="width: 100%" filterable>
                <el-option v-for="c in countryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="btn-group">
      <el-button class="back-btn" @click="handleBack">{{ t('register.back') }}</el-button>
      <el-button type="primary" class="next-btn" :loading="loading" @click="handleNext">{{ t('register.next') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useRegisterStore } from '@/stores/register'
import { useUserStore } from '@/stores/user'
import { getRegistrationTypeList } from '@/api/registrationType'
import { countryList, getCountryLabel } from '@/data/country'
import {
  ElRadio,
  ElRadioGroup,
  ElCheckbox,
  ElForm,
  ElFormItem,
  ElInput,
  ElSelect,
  ElOption,
  ElDatePicker,
  ElButton
} from 'element-plus'

const router = useRouter()
const { locale, t, tm } = useI18n()
const registerStore = useRegisterStore()
const userStore = useUserStore()

const feeList = ref([])
const loading = ref(false)
const countryOptions = computed(() =>
  countryList.map(c => ({ label: getCountryLabel(c, locale.value), value: c.value }))
)
const feeItems = computed(() => {
  const items = tm('register.registrationFeeItems')
  return Array.isArray(items) ? items : []
})

const fetchFeeList = async () => {
  try {
    const countryType = locale.value === 'zh' ? 'ZH' : 'EN'
    const res = await getRegistrationTypeList(countryType)
    if (res.code === '00000' && res.data && res.data.list && res.data.list.length > 0) {
      feeList.value = res.data.list.map(item => {
        const priceStr = (item.deadlineAmount || '0').replace(/,/g, '')
        const priceNum = parseFloat(priceStr) || 0
        return {
          value: item.id,
          fieldName: item.fieldName,
          label: item.fieldName,
          price: priceNum.toFixed(2) + ' ' + t('register.priceUnit'),
          priceNum: priceNum
        }
      })
    }
  } catch (e) {
    console.error('获取注册类型失败：', e)
  } finally {
    syncPriceToStore()
  }
}

const syncPriceToStore = () => {
  const selected = feeList.value.find(item => item.value === formData.value.registerType)
  if (selected) {
    const data = {
      ...registerStore.state.signup,
      registerTypeName: selected.label,
      registerTypePrice: selected.priceNum,
      registerTypePriceDisplay: selected.price
    }
    registerStore.setSignupData(data)
  }
}

const defaultFormData = () => ({
  registerType: '',
  visaIsNeed: false,
  visaInfo: {
    visaName: '',
    visaSurname: '',
    visaPassport: '',
    visaIssueTime: '',
    visaEndTime: '',
    visaTime: '',
    visaArea: ''
  }
})

const savedData = registerStore.state.signup || {}
const formData = ref({
  ...defaultFormData(),
  registerType: savedData.registerType || '',
  visaIsNeed: savedData.visaIsNeed ?? false,
  visaInfo: {
    ...defaultFormData().visaInfo,
    ...(savedData.visaInfo || {})
  }
})

watch(() => formData.value, (newVal) => {
  const selected = feeList.value.find(item => item.value === newVal.registerType)
  const data = {
    registerType: newVal.registerType,
    visaIsNeed: newVal.visaIsNeed,
    visaInfo: newVal.visaInfo,
    registerTypeName: selected ? selected.label : (registerStore.state.signup.registerTypeName || ''),
    registerTypePrice: selected ? selected.priceNum : (registerStore.state.signup.registerTypePrice || 0),
    registerTypePriceDisplay: selected ? selected.price : (registerStore.state.signup.registerTypePriceDisplay || '')
  }
  registerStore.setSignupData(data)
}, { deep: true, immediate: true })

watch(() => formData.value.registerType, (newVal) => {
  const selected = feeList.value.find(item => item.value === newVal)
  if (selected) {
    registerStore.state.signup.registerTypeName = selected.label
    registerStore.state.signup.registerTypePrice = selected.priceNum
    registerStore.state.signup.registerTypePriceDisplay = selected.price
  }
})

const handleBack = () => {
  router.push('/register/contact')
}

const handleNext = async () => {
  if (feeList.value.length === 0) {
    ElMessage.error(t('register.validation.registrationTypeLoadFail'))
    return
  }
  if (!formData.value.registerType) {
    ElMessage.error(t('register.validation.registerTypeRequired'))
    return
  }
  if (formData.value.visaIsNeed) {
    const visa = formData.value.visaInfo
    if (!visa.visaName || !visa.visaSurname || !visa.visaPassport || !visa.visaIssueTime || !visa.visaEndTime || !visa.visaTime || !visa.visaArea) {
      ElMessage.error(t('register.validation.visaInfoRequired'))
      return
    }
  }
  syncPriceToStore()
  loading.value = true
  try {
    const result = await userStore.doRegister(registerStore.state)
    if (result.success) {
      router.push('/register/confirm')
    }
  } catch (e) {
    console.error('注册失败', e)
    ElMessage.error(e?.response?.data?.msg || e?.message || t('register.registrationFailed'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFeeList()
})

watch(locale, () => {
  fetchFeeList()
})
</script>

<style scoped>
.register-container {
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.section {
  margin-bottom: 30px;
}

.section-header {
  background-color: #009688;
  color: white;
  font-size: 22px;
  font-weight: 600;
  padding: 12px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 20px;
}

.content {
  line-height: 1.6;
}

.desc-title {
  color: #009688;
  font-weight: 600;
  margin-bottom: 10px;
}

.benefit-list {
  list-style-type: disc;
  padding-left: 20px;
  margin-bottom: 25px;
}

.fee-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.fee-item :deep(.el-radio__label) {
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
}

.price {
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  flex-shrink: 0;
  margin-left: 12px;
  margin-top: 2px;
}

.desc {
  margin: 8px 0;
}

.link {
  color: #7d7ef4;
  text-decoration: underline;
}

.visa-form {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.visa-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.visa-tip {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.btn-group {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}

.back-btn,
.next-btn {
  background-color: #7d7ef4;
  border: none;
  color: white;
  padding: 12px 60px;
  font-size: 16px;
  border-radius: 8px;
}
</style>
