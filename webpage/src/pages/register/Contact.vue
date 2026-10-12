<template>
  <div class="form-container">
    <!-- 联系信息模块 -->
    <div class="form-section">
      <div class="section-header">{{ t('register.steps.contact') }}</div>
      <el-form ref="contactFormRef" :model="contactForm" :rules="contactRules" label-position="top">
        <!-- 标题下拉框 -->
        <el-form-item :label="t('register.title')" prop="title" required>
          <el-select v-model="contactForm.title" :placeholder="t('register.titlePlaceholder')">
            <el-option :label="t('register.titleGuest')" value="嘉宾" />
            <el-option :label="t('register.titleSpeaker')" value="讲者" />
<!--            <el-option label="先生" value="先生" />-->
<!--            <el-option label="太太" value="太太" />-->
<!--            <el-option label="多发性硬化症" value="多发性硬化症" />-->
<!--            <el-option label="Mx" value="Mx" />-->
<!--            <el-option label="教授" value="教授" />-->
<!--            <el-option label="教授博士" value="教授博士" />-->
          </el-select>
        </el-form-item>

        <!-- 姓名字段 -->
        <el-form-item :label="t('register.firstName')" prop="firstName" required>
          <el-input v-model="contactForm.firstName" placeholder="" />
        </el-form-item>
        <el-form-item :label="t('register.lastName')" prop="lastName" required>
          <el-input v-model="contactForm.lastName" placeholder="" />
        </el-form-item>

        <!-- 邮箱和组织 -->
        <el-form-item :label="t('register.email')" prop="email" required>

          <el-input v-model="contactForm.email" :placeholder="t('register.emailPlaceholder')" />

        </el-form-item>
        <el-form-item :label="t('register.organization')" prop="organization" required>

          <el-input v-model="contactForm.organization" />
        </el-form-item>

        <!-- 团队注册成员 -->
        <div v-if="registerStore.state.enroll.registerType === '1'" class="team-section">
          <div class="team-header">
            <span>{{ t('register.teamMembers') }}</span>
            <el-button type="primary" size="small" @click="addTeamMember">{{ t('register.addMember') }}</el-button>
          </div>
          <div v-for="(member, index) in teamMembers" :key="index" class="team-member-row">
            <div class="member-field">
              <label class="member-label">{{ t('register.firstName') }} <span class="required-star">*</span></label>
              <el-input v-model="member.firstName" :placeholder="t('register.firstName')" class="member-input" @input="clearTeamError(index, 'firstName')" />
              <span v-if="teamErrors[index]?.firstName" class="field-error">{{ teamErrors[index].firstName }}</span>
            </div>
            <div class="member-field">
              <label class="member-label">{{ t('register.lastName') }} <span class="required-star">*</span></label>
              <el-input v-model="member.lastName" :placeholder="t('register.lastName')" class="member-input" @input="clearTeamError(index, 'lastName')" />
              <span v-if="teamErrors[index]?.lastName" class="field-error">{{ teamErrors[index].lastName }}</span>
            </div>
            <div class="member-field">
              <label class="member-label">{{ t('register.email') }} <span class="required-star">*</span></label>
              <el-input v-model="member.email" :placeholder="t('register.emailPlaceholder')" class="member-input" @input="clearTeamError(index, 'email')" />
              <span v-if="teamErrors[index]?.email" class="field-error">{{ teamErrors[index].email }}</span>
            </div>
            <div class="member-field">
              <label class="member-label">{{ t('register.phone') }} <span class="required-star">*</span></label>
              <el-input v-model="member.phone" :placeholder="t('register.phonePlaceholder')" class="member-input" @input="clearTeamError(index, 'phone')" />
              <span v-if="teamErrors[index]?.phone" class="field-error">{{ teamErrors[index].phone }}</span>
            </div>
            <el-button type="danger" size="small" @click="removeTeamMember(index)">{{ t('register.delete') }}</el-button>
          </div>
        </div>

        <!-- 手机号 -->
        <el-form-item :label="t('register.phoneInternational')" prop="phone" required>
          <el-input v-model="contactForm.phone" placeholder="">
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 主要地址 -->
        <div class="address-section">
          <h4 class="address-title">{{ t('register.mainAddress') }}</h4>
          <el-form-item :label="t('register.street')" prop="street" required>
            <el-input v-model="contactForm.street" placeholder="" />
          </el-form-item>

          <div class="two-col-row">
            <el-form-item :label="t('register.zipCode')" prop="zipCode" required>
              <el-input v-model="contactForm.zipCode" placeholder="" />
            </el-form-item>
            <el-form-item :label="t('register.city')" prop="city" required>
              <el-input v-model="contactForm.city" placeholder="" />
            </el-form-item>
          </div>
          <div class="two-col-row">
            <el-form-item :label="t('register.state')">
              <el-input v-model="contactForm.state" placeholder="" />
            </el-form-item>
            <el-form-item :label="t('register.country')" prop="country" required>
              <el-select v-model="contactForm.country" :placeholder="t('register.countryPlaceholder')" filterable>
                <el-option v-for="c in countryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </div>
        </div>
      </el-form>
    </div>

    <!-- 账单地址模块（动态显示） -->
    <div class="billing-section">
      <el-checkbox v-model="showBillingAddress">
        {{ t('register.useDifferentBilling') }}
      </el-checkbox>

      <div v-if="showBillingAddress" class="billing-form">
        <h4 class="address-title">{{ t('register.billingAddress') }}</h4>
        <el-form :model="billingForm" :rules="billingRules" ref="billingFormRef" label-position="top">
          <el-form-item :label="t('register.organization')">
            <el-input v-model="billingForm.organization" placeholder="" />
          </el-form-item>
          <el-form-item :label="t('register.vatNumber')">
            <el-input v-model="billingForm.vatNumber" placeholder="" />
          </el-form-item>
          <el-form-item :label="t('register.poNumber')">
            <el-input v-model="billingForm.poNumber" placeholder="" />
          </el-form-item>

          <el-form-item :label="t('register.street')" prop="street" required>
            <el-input v-model="billingForm.street" placeholder="" />
          </el-form-item>

          <div class="two-col-row">
            <el-form-item :label="t('register.zipCode')" prop="zipCode" required>
              <el-input v-model="billingForm.zipCode" placeholder="" />
            </el-form-item>
            <el-form-item :label="t('register.city')" prop="city" required>
              <el-input v-model="billingForm.city" placeholder="" />
            </el-form-item>
          </div>

          <div class="two-col-row">
            <el-form-item :label="t('register.state')">
              <el-input v-model="billingForm.state" placeholder="" />
            </el-form-item>
            <el-form-item :label="t('register.country')" prop="country" required>
              <el-select v-model="billingForm.country" :placeholder="t('register.countryPlaceholder')" filterable>
                <el-option v-for="c in countryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 底部提示与按钮 -->
    <div class="form-footer">
      <p class="note">{{ t('register.requiredFieldsNote') }}</p>

      <div class="section-header">{{ t('register.createAccount') }}</div>
      <div class="info-tip">
        <p>{{ t('register.accountAutoCreated') }}</p>
        <p>{{ t('register.accountLinkedToEmail') }}</p>
        <p>{{ t('register.loginViaEmail') }}</p>
      </div>

      <div class="btn-group">
        <el-button type="primary" class="back-btn" @click="handleBack">{{ t('register.back') }}</el-button>
        <el-button type="primary" class="next-btn" @click="handleNext">{{ t('register.next') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElCheckbox, ElButton, ElIcon } from 'element-plus'
import { Phone } from '@element-plus/icons-vue'
import { useRegisterStore } from '@/stores/register'
import { countryList, getCountryLabel } from '@/data/country'

const router = useRouter()
const { t, locale } = useI18n()
const registerStore = useRegisterStore()

const countryOptions = computed(() =>
  countryList.map(c => ({ label: getCountryLabel(c, locale.value), value: c.value }))
)
const contactFormRef = ref()
const billingFormRef = ref()

const teamErrors = reactive({})

const showBillingAddress = ref(false)

const isTeamRegister = computed(() => {
  return registerStore.state.enroll.registerType === '1'
})

const teamMembers = ref([
  { firstName: '', lastName: '', email: '', phone: '' }
])

const addTeamMember = () => {
  teamMembers.value.push({ firstName: '', lastName: '', email: '', phone: '' })
}

const removeTeamMember = (index) => {
  teamMembers.value.splice(index, 1)
}

const contactForm = ref({
  title: '',
  firstName: '',
  lastName: '',
  email: '',
  organization: '',
  phone: '',
  street: '',
  zipCode: '',
  city: '',
  state: '',
  country: ''
})

const billingForm = ref({
  organization: '',
  vatNumber: '',
  poNumber: '',
  street: '',
  zipCode: '',
  city: '',
  state: '',
  country: ''
})

watch(() => contactForm.value, (newVal) => {
  registerStore.setContactData(newVal)
  registerStore.setEnrollData({
    ...registerStore.state.enroll,
    firstName: newVal.firstName,
    lastName: newVal.lastName,
    email: newVal.email
  })
}, { deep: true })

watch(() => showBillingAddress.value, (newVal) => {
  registerStore.setContactData({
    ...contactForm.value,
    hasBillingAddress: newVal,
    billingAddress: billingForm.value
  })
})

watch(() => billingForm.value, (newVal) => {
  if (showBillingAddress.value) {
    registerStore.setContactData({
      ...registerStore.state.contact,
      hasBillingAddress: true,
      billingAddress: {
        organization: newVal.organization,
        vatNumber: newVal.vatNumber,
        poNumber: newVal.poNumber,
        street: newVal.street,
        zipCode: newVal.zipCode,
        city: newVal.city,
        state: newVal.state,
        country: newVal.country
      }
    })
  }
}, { deep: true })

watch(() => teamMembers.value, (newVal) => {
  registerStore.setContactData({
    ...registerStore.state.contact,
    teamMembers: newVal
  })
}, { deep: true })

onMounted(() => {
  if (!contactForm.value.firstName && !contactForm.value.lastName && !contactForm.value.email) {
    const contact = registerStore.state.contact
    if (contact.firstName || contact.lastName || contact.email) {
      contactForm.value.title = contact.title
      contactForm.value.firstName = contact.firstName
      contactForm.value.lastName = contact.lastName
      contactForm.value.email = contact.email
      contactForm.value.organization = contact.organization
      contactForm.value.phone = contact.phone
      contactForm.value.street = contact.street
      contactForm.value.zipCode = contact.zipCode
      contactForm.value.city = contact.city
      contactForm.value.state = contact.state
      contactForm.value.country = contact.country
    }
    if (contact.billingAddress) {
      billingForm.value.organization = contact.billingAddress.organization || ''
      billingForm.value.vatNumber = contact.billingAddress.vatNumber || ''
      billingForm.value.poNumber = contact.billingAddress.poNumber || ''
      billingForm.value.street = contact.billingAddress.street || ''
      billingForm.value.zipCode = contact.billingAddress.zipCode || ''
      billingForm.value.city = contact.billingAddress.city || ''
      billingForm.value.state = contact.billingAddress.state || ''
      billingForm.value.country = contact.billingAddress.country || ''
    }
    showBillingAddress.value = contact.hasBillingAddress || false
    if (contact.teamMembers && contact.teamMembers.length > 0) {
      teamMembers.value = contact.teamMembers.map(m => ({ firstName: m.firstName || '', lastName: m.lastName || '', email: m.email || '', phone: m.phone || '' }))
    }
  }
})

const contactRules = computed(() => ({
  title: [{ required: true, message: t('register.validation.selectTitle'), trigger: 'change' }],
  firstName: [{ required: true, message: t('register.validation.enterFirstName'), trigger: 'blur' }],
  lastName: [{ required: true, message: t('register.validation.enterLastName'), trigger: 'blur' }],
  email: [
    { required: true, message: t('register.validation.enterEmail'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('register.validation.enterValidEmail'), trigger: 'blur' }
  ],
  organization: [{ required: true, message: t('register.validation.enterOrganization'), trigger: 'blur' }],
  phone: [
    { required: true, message: t('register.validation.enterPhone'), trigger: 'blur' },
    { pattern: /^[\d\s\-+()]{6,20}$/, message: t('register.phoneInvalid'), trigger: 'blur' }
  ],
  street: [{ required: true, message: t('register.validation.enterStreet'), trigger: 'blur' }],
  zipCode: [
    { required: true, message: t('register.validation.enterZipCode'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9\s\-]{3,10}$/, message: t('register.validation.zipCodeInvalid'), trigger: 'blur' }
  ],
  city: [
    { required: true, message: t('register.validation.enterCity'), trigger: 'blur' },
    { pattern: /^[^\d]+$/, message: t('register.validation.cityInvalid'), trigger: 'blur' }
  ],
  country: [{ required: true, message: t('register.validation.selectCountry'), trigger: 'change' }]
}))

const billingRules = computed(() => ({
  street: [{ required: true, message: t('register.validation.enterStreet'), trigger: 'blur' }],
  zipCode: [
    { required: true, message: t('register.validation.enterZipCode'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9\s\-]{3,10}$/, message: t('register.validation.zipCodeInvalid'), trigger: 'blur' }
  ],
  city: [
    { required: true, message: t('register.validation.enterCity'), trigger: 'blur' },
    { pattern: /^[^\d]+$/, message: t('register.validation.cityInvalid'), trigger: 'blur' }
  ],
  country: [{ required: true, message: t('register.validation.selectCountry'), trigger: 'change' }]
}))

const validateTeamMembers = () => {
  let hasError = false
  teamMembers.value.forEach((member, index) => {
    const errors = {}
    if (!member.firstName || !member.firstName.trim()) {
      errors.firstName = t('register.validation.enterFirstName')
    }
    if (!member.lastName || !member.lastName.trim()) {
      errors.lastName = t('register.validation.enterLastName')
    }
    if (!member.email || !member.email.trim()) {
      errors.email = t('register.validation.enterEmail')
    } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(member.email)) {
      errors.email = t('register.validation.enterValidEmail')
    }
    if (!member.phone || !member.phone.trim()) {
      errors.phone = t('register.validation.enterPhone')
    }
    if (Object.keys(errors).length > 0) {
      teamErrors[index] = errors
      hasError = true
    }
  })
  return hasError
}

const clearTeamError = (index, field) => {
  if (teamErrors[index] && teamErrors[index][field]) {
    delete teamErrors[index][field]
    if (Object.keys(teamErrors[index]).length === 0) {
      delete teamErrors[index]
    }
  }
}

const handleBack = () => {
  router.push('/register/consent')
}

const handleNext = async () => {
  const valid = await contactFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.error('请完善联系信息中的必填项')
    return
  }

  if (isTeamRegister.value && validateTeamMembers()) {
    ElMessage.error('请完善团队成员的必填信息')
    return
  }

  if (showBillingAddress.value) {
    const billingValid = await billingFormRef.value.validate().catch(() => false)
    if (!billingValid) {
      ElMessage.error('请完善账单地址中的必填项')
      return
    }
  }

  const form = contactForm.value
  registerStore.state.contact = {
    title: form.title,
    firstName: form.firstName,
    lastName: form.lastName,
    email: form.email,
    organization: form.organization,
    phone: form.phone,
    street: form.street,
    zipCode: form.zipCode,
    city: form.city,
    state: form.state,
    country: form.country,
    hasBillingAddress: showBillingAddress.value,
    billingAddress: showBillingAddress.value ? {
      organization: billingForm.value.organization,
      vatNumber: billingForm.value.vatNumber,
      poNumber: billingForm.value.poNumber,
      street: billingForm.value.street,
      zipCode: billingForm.value.zipCode,
      city: billingForm.value.city,
      state: billingForm.value.state,
      country: billingForm.value.country
    } : null,
    teamMembers: teamMembers.value
  }
  ElMessage.success('联系信息已保存')
  router.push('/register/signup')
}
</script>

<style scoped>
.form-container {
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.section-header {
  background-color: #009688;
  color: white;
  font-size: 20px;
  font-weight: 600;
  padding: 12px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 25px;
}

.address-title {
  color: #009688;
  font-size: 16px;
  font-weight: 600;
  margin: 25px 0 15px;
}

.two-col-row {
  display: flex;
  gap: 20px;
}
.two-col-row .el-form-item {
  flex: 1;
}

.billing-section {
  margin-top: 30px;
}
.billing-form {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.team-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: 600;
  color: #009688;
}

.team-member-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.member-input {
  flex: 1;
  min-width: 120px;
}

.member-field {
  flex: 1;
  min-width: 120px;
  display: flex;
  flex-direction: column;
}

.member-label {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
  font-weight: 500;
}

.required-star {
  color: #f56c6c;
}

.field-error {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 2px;
}

@media (max-width: 768px) {
  .team-member-row {
    flex-direction: column;
  }
  .member-input {
    width: 100%;
  }
}

.note {
  font-size: 13px;
  color: #666;
  margin: 10px 0 25px;
}

.info-tip {
  background-color: #f0f0f0;
  padding: 18px;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
  margin-bottom: 30px;
}
.info-tip p {
  margin: 5px 0;
}

.btn-group {
  display: flex;
  justify-content: space-between;
}
.back-btn, .next-btn {
  background-color: #7d7ef4;
  border: none;
  padding: 12px 60px;
  font-size: 16px;
}
</style>
