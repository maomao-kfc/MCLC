<template>
  <div class="register-container">
    <!-- 顶部说明信息 -->
    <div class="info-section">
      <h1 class="title">{{ t('register.enrollTitle') }}</h1>
      <div class="info-text">
        <p>{{ t('register.enrollNotice1') }}</p>
        <p class="important">{{ t('register.enrollNotice2') }}</p>
        <p><strong>{{ t('register.paymentMethodsTitle') }}</strong> {{ t('register.paymentMethodsDesc') }}</p>
        <p><strong>{{ t('register.discountCategoryTitle') }}</strong> {{ t('register.discountCategoryDesc') }}<a href="#" class="link">{{ t('register.clickHere') }}</a>{{ t('register.discountCategoryMoreInfo') }}</p>
        <p><strong>{{ t('register.visaInvitationTitle') }}</strong> {{ t('register.visaInvitationDesc') }}</p>
      </div>
    </div>

    <!-- 主体内容区：登录 + 创建账户 -->
    <div class="main-content">
      <!-- 左侧：现有用户登录 -->
<!--      <div class="login-section">-->
<!--        <h2 class="section-title">以现有用户身份在此登录</h2>-->
<!--        <p class="desc">请在下方输入您的用户名或电子邮件地址。</p>-->
<!--        <div class="input-group">-->
<!--          <label class="label">用户名或电子邮件地址：</label>-->
<!--          <div class="input-wrapper">-->
<!--            <el-input v-model="loginForm.username" placeholder="" />-->
<!--            <el-button class="submit-btn" type="primary" circle>-->
<!--              <el-icon><ArrowRight /></el-icon>-->
<!--            </el-button>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->

      <!-- 右侧：创建账户 -->
      <div class="create-account-section">
        <h2 class="section-title">{{ t('register.createAccountTitle') }}</h2>
        <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-position="top">
          <el-form-item :label="t('register.firstName') + '：'" prop="firstName">
            <el-input v-model="createForm.firstName" placeholder="" />
          </el-form-item>
          <el-form-item :label="t('register.lastName') + '：'" prop="lastName">
            <el-input v-model="createForm.lastName" placeholder="" />
          </el-form-item>
          <el-form-item :label="t('register.emailAddress')" prop="email">
            <el-input v-model="createForm.email" placeholder="" />
          </el-form-item>
          <div class="btn-wrapper">
            <el-button type="primary" class="continue-btn" @click="handleContinue">{{ t('register.continueBtn') }}</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ElInput, ElButton, ElForm, ElFormItem, ElIcon } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useRegisterStore } from '@/stores/register'
import { useGuestStore } from '@/stores/guest.js'

const router = useRouter()
const { t } = useI18n()
const registerStore = useRegisterStore()
const guestStore = useGuestStore()

const loginForm = ref({
  username: ''
})

const createForm = ref({
  firstName: '',
  lastName: '',
  registerType: '0',
  email: ''
})

watch(() => createForm.value, (newVal) => {
  registerStore.setEnrollData(newVal)
  registerStore.setContactData({
    ...registerStore.state.contact,
    firstName: newVal.firstName,
    lastName: newVal.lastName,
    email: newVal.email
  })
}, { deep: true })

onMounted(async () => {
  await guestStore.gethwList()
  if (!createForm.value.firstName && !createForm.value.lastName && !createForm.value.email) {
    const enroll = registerStore.state.enroll
    if (enroll.firstName || enroll.lastName || enroll.email) {
      createForm.value.firstName = enroll.firstName
      createForm.value.lastName = enroll.lastName
      createForm.value.registerType = enroll.registerType
      createForm.value.email = enroll.email
    }
  }
})

const createRules = computed(() => ({
  firstName: [{ required: true, message: t('register.firstNamePlaceholder'), trigger: 'blur' }],
  lastName: [{ required: true, message: t('register.lastNamePlaceholder'), trigger: 'blur' }],
  email: [
    { required: true, message: t('register.emailRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('register.emailInvalidFormat'), trigger: 'blur' }
  ]
}))

const createFormRef = ref()

const handleContinue = async () => {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.error(t('register.formIncomplete'))
    return
  }
  registerStore.setEnrollData(createForm.value)
  router.push('/register/consent')
}
</script>

<style scoped>
.register-container {
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.info-section {
  margin-bottom: 40px;
  line-height: 1.6;
  color: #444;
}

.info-text .important {
  color: #7d7ef4; /* 紫色重点文字 */
  font-weight: 500;
}

.info-text .link {
  color: #0066cc;
  text-decoration: underline;
}

.main-content {
  display: flex;
  justify-content: center;
  gap: 40px;
}

/* 左侧登录区域 */
.login-section {
  flex: 1;
  max-width: 400px;
  padding-top: 40px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.label {
  color: #ff7f24; /* 橙色标签文字 */
  font-size: 14px;
  font-weight: 500;
  display: block;
  margin-bottom: 8px;
}

.input-wrapper {
  display: flex;
  align-items: center;
}

.submit-btn {
  background-color: #7d7ef4;
  margin-left: -1px;
  border-radius: 0 4px 4px 0;
}

/* 右侧创建账户区域 */
.create-account-section {
  flex: 1;
  max-width: 500px;
}

.btn-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.continue-btn {
  background-color: #7d7ef4;
  border: none;
  padding: 12px 60px;
  font-size: 16px;
}
</style>
