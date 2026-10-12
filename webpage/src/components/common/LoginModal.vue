<template>
  <el-dialog
    v-model="dialogVisible"
    :title="t('login.title')"
    width="440px"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form
      :model="loginForm"
      :rules="loginRules"
      ref="loginFormRef"
      label-width="80px"
      label-position="right"
    >
      <el-form-item :label="t('login.emailLabel')" prop="email">
        <el-input v-model="loginForm.email" :placeholder="t('login.emailPlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('login.codeLabel')" prop="code">
        <div class="code-row">
          <el-input v-model="loginForm.code" :placeholder="t('login.codePlaceholder')" />
          <el-button
            class="code-btn"
            :disabled="countdown > 0"
            @click="sendCode"
          >
            {{ countdown > 0 ? countdown + 's' : t('login.sendCode') }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">{{ t('login.cancel') }}</el-button>
        <el-button type="primary" :loading="loading" @click="handleLogin">{{ t('login.submit') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { sendEmailCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const userStore = useUserStore()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['update:modelValue'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loginFormRef = ref()
const loading = ref(false)
const countdown = ref(0)
let timer = null

const loginForm = reactive({
  email: '',
  code: ''
})

const loginRules = computed(() => ({
  email: [
    { required: true, message: t('login.emailRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('login.emailInvalid'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: t('login.codeRequired'), trigger: 'blur' }
  ]
}))

const sendCode = async () => {
  if (!loginForm.email) {
    ElMessage.warning(t('login.emailWarning'))
    return
  }
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(loginForm.email)) {
    ElMessage.warning(t('login.emailFormatWarning'))
    return
  }

  try {
    const res = await sendEmailCode(loginForm.email)
    if (res.code === '00000') {
      ElMessage.success(t('login.codeSent'))
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.msg || t('login.codeSendFail'))
    }
  } catch (e) {
    ElMessage.error(t('login.codeSendRetry'))
  }
}

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const success = await userStore.doLogin(loginForm.email, loginForm.code)
      loading.value = false
      if (success) {
        dialogVisible.value = false
        resetForm()
      }
    }
  })
}

const handleClose = () => {
  if (loading.value) return
  dialogVisible.value = false
  resetForm()
}

const resetForm = () => {
  loginFormRef.value?.resetFields()
  loginForm.email = ''
  loginForm.code = ''
  countdown.value = 0
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.code-btn {
  flex-shrink: 0;
  min-width: 110px;
}
</style>
