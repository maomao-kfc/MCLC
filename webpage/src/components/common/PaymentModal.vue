<template>
  <el-dialog
    v-model="dialogVisible"
    title="支付"
    width="560px"
    :before-close="handleClose"
    @open="onDialogOpen"
  >
    <div class="payment-content">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="创建支付" name="create">
          <el-form :model="createForm" ref="createFormRef" label-width="100px" label-position="right">
            <el-form-item label="注册用户ID" prop="registrationId" :rules="[{ required: true, message: '请输入注册用户ID' }]">
              <el-input v-model.number="createForm.registrationId" placeholder="请输入注册用户ID" />
            </el-form-item>
            <el-form-item label="支付方式" prop="paymentMethod" :rules="[{ required: true, message: '请选择支付方式' }]">
              <el-select v-model="createForm.paymentMethod" placeholder="请选择支付方式" style="width:100%">
                <el-option label="信用卡" :value="1" />
                <el-option label="银行转账" :value="2" />
                <el-option label="微信" :value="3" />
                <el-option label="支付宝" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item label="金额">
              <span class="amount-display">{{ createForm.amount || '--' }}</span>
            </el-form-item>
            <el-form-item label="货币">
              <span class="currency-display">CNY (人民币)</span>
            </el-form-item>
            <el-form-item v-if="createForm.paymentMethod === 3 || createForm.paymentMethod === 4" label="支付场景" prop="scene">
              <el-select v-model="createForm.scene" style="width:100%">
                <el-option label="PC扫码付" value="scan" />
<!--                <el-option label="手机H5付" value="h5" />-->
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="createLoading" @click="handleCreate">创建支付意图</el-button>
            </el-form-item>
          </el-form>
          <div v-if="createResult" class="result-box">
            <p><strong>订单号：</strong>{{ createResult.orderNo }}</p>
            <p><strong>状态：</strong>{{ createResult.paymentStatus }}</p>
            <p><strong>方式：</strong>{{ createResult.paymentMethod }}</p>
            <p><strong>金额：</strong>{{ createResult.amount }}</p>
            <div v-if="createResult.qrCodeUrl" class="qr-box">
              <p><strong>扫码支付二维码：</strong></p>
              <img :src="'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=' + encodeURIComponent(createResult.qrCodeUrl)" width="180" height="180" />
              <p class="qr-tip">请使用对应App扫码支付</p>
            </div>
            <div v-if="createResult.payUrl" class="pay-url-box">
              <p><strong>支付链接：</strong><a :href="createResult.payUrl" target="_blank">{{ createResult.payUrl }}</a></p>
            </div>
            <div v-if="createResult.clientSecret">
              <p><strong>clientSecret：</strong>{{ createResult.clientSecret }}</p>
            </div>
            <div v-if="createResult.bankTransferInfo" class="bank-info-box">
              <p><strong>--- 银行转账信息 ---</strong></p>
              <p v-for="(val, key) in createResult.bankTransferInfo" :key="key">
                <strong>{{ key }}：</strong>{{ val }}
              </p>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="查询状态" name="status">
          <el-form inline>
            <el-form-item label="注册用户ID">
              <el-input v-model.number="statusId" placeholder="请输入注册用户ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="statusLoading" @click="handleQueryStatus">查询</el-button>
            </el-form-item>
          </el-form>
          <div v-if="statusResult" class="result-box">
            <p><strong>支付状态：</strong>{{ statusResult.paymentStatus }}</p>
            <p><strong>支付方式：</strong>{{ statusResult.paymentMethod }}</p>
            <p><strong>金额：</strong>{{ statusResult.amount }}</p>
            <div v-if="statusResult.bankTransferInfo" class="bank-info-box">
              <p><strong>--- 银行转账信息 ---</strong></p>
              <p v-for="(val, key) in statusResult.bankTransferInfo" :key="key">
                <strong>{{ key }}：</strong>{{ val }}
              </p>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="确认银行转账" name="confirm">
          <el-form inline>
            <el-form-item label="注册用户ID">
              <el-input v-model.number="confirmId" placeholder="请输入注册用户ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="confirmLoading" @click="handleConfirm">确认</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="退款" name="refund">
          <el-form inline>
            <el-form-item label="注册用户ID">
              <el-input v-model.number="refundId" placeholder="请输入注册用户ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" :loading="refundLoading" @click="handleRefund">退款</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="银行信息" name="bankInfo">
          <el-button type="primary" :loading="bankInfoLoading" @click="handleGetBankInfo">获取银行信息</el-button>
          <div v-if="bankInfoResult" class="result-box">
            <p v-for="(val, key) in bankInfoResult" :key="key">
              <strong>{{ key }}：</strong>{{ val }}
            </p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="支付配置" name="config">
          <el-button type="primary" :loading="configLoading" @click="handleGetConfig">获取配置</el-button>
          <div v-if="configResult" class="result-box">
            <p><strong>publishableKey：</strong>{{ configResult.publishableKey }}</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRegisterStore } from '@/stores/register'
import { getRegisterUserByEmail } from '@/api/user'
import {
  createPaymentIntent,
  getPaymentStatus,
  getBankTransferInfo,
  confirmBankTransfer,
  refund,
  getPaymentConfig
} from '@/api/payment'

const userStore = useUserStore()
const registerStore = useRegisterStore()

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

const handleClose = () => {
  dialogVisible.value = false
}

const activeTab = ref('create')

// 支付方式映射（后端存字符串，前端用数字）
const paymentMethodReverseMap = {
  'credit_card': 1,
  'bank_transfer': 2,
  'wechat': 3,
  'alipay': 4
}

// 创建支付
const createFormRef = ref()
const createLoading = ref(false)
const createResult = ref(null)
const createForm = reactive({
  registrationId: null,
  paymentMethod: '',
  amount: '',
  currency: 'cny',
  scene: 'scan'
})

const initForm = () => {
  createForm.registrationId = null
  createForm.paymentMethod = ''
  createForm.amount = ''
  createForm.currency = 'cny'
  createForm.scene = 'scan'
  createResult.value = null
}

const onDialogOpen = () => {
  const signupRt = registerStore.state.signup?.registerTypePrice
  if (signupRt) {
    createForm.amount = String(signupRt)
  }

  const pMethod = registerStore.state.paymentMethod
  if (pMethod && [1, 2, 3, 4].includes(pMethod)) {
    createForm.paymentMethod = pMethod
  }

  if (!userStore.isLoggedIn) return
  const email = userStore.userInfo?.email
  if (!email) return

  getRegisterUserByEmail(email).then(res => {
    if (res.code === '00000' && res.data?.list?.length > 0) {
      const userData = res.data.list[0]
      if (userData.id) {
        createForm.registrationId = userData.id
      }
      if (userData.paymentMethod != null) {
        const raw = userData.paymentMethod
        const num = typeof raw === 'number' ? raw : (paymentMethodReverseMap[raw] || '')
        if ([1, 2, 3, 4].includes(num)) {
          createForm.paymentMethod = num
        }
      }
      if (userData.paymentAmount != null) {
        createForm.amount = String(userData.paymentAmount)
      }
    }
  }).catch(e => {
    console.error('查询注册用户失败：', e)
  })
}

const handleCreate = () => {
  createFormRef.value.validate(async (valid) => {
    if (valid) {
      createLoading.value = true
      try {
        const res = await createPaymentIntent({
          registrationId: createForm.registrationId,
          paymentMethod: createForm.paymentMethod,
          amount: createForm.amount,
          currency: createForm.currency,
          scene: createForm.scene,
          returnUrl: window.location.origin + '/payment/result'
        })
        if (res.code === '00000') {
          createResult.value = res.data
          ElMessage.success('支付意图创建成功！')
        } else {
          ElMessage.error(res.msg || '创建失败')
        }
      } catch (e) {
        ElMessage.error('请求失败')
      }
      createLoading.value = false
    }
  })
}

// 查询状态
const statusId = ref(null)
const statusLoading = ref(false)
const statusResult = ref(null)

const handleQueryStatus = async () => {
  if (!statusId.value) {
    ElMessage.warning('请输入注册用户ID')
    return
  }
  statusLoading.value = true
  try {
    const res = await getPaymentStatus(statusId.value)
    if (res.code === '00000') {
      statusResult.value = res.data
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
  statusLoading.value = false
}

// 确认银行转账
const confirmId = ref(null)
const confirmLoading = ref(false)

const handleConfirm = async () => {
  if (!confirmId.value) {
    ElMessage.warning('请输入注册用户ID')
    return
  }
  confirmLoading.value = true
  try {
    const res = await confirmBankTransfer(confirmId.value)
    if (res.code === '00000') {
      ElMessage.success('确认成功！')
    } else {
      ElMessage.error(res.msg || '确认失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
  confirmLoading.value = false
}

// 退款
const refundId = ref(null)
const refundLoading = ref(false)

const handleRefund = async () => {
  if (!refundId.value) {
    ElMessage.warning('请输入注册用户ID')
    return
  }
  refundLoading.value = true
  try {
    const res = await refund(refundId.value)
    if (res.code === '00000') {
      ElMessage.success('退款成功！')
    } else {
      ElMessage.error(res.msg || '退款失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
  refundLoading.value = false
}

// 获取银行信息
const bankInfoLoading = ref(false)
const bankInfoResult = ref(null)

const handleGetBankInfo = async () => {
  bankInfoLoading.value = true
  try {
    const res = await getBankTransferInfo()
    if (res.code === '00000') {
      bankInfoResult.value = res.data
    } else {
      ElMessage.error(res.msg || '获取失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
  bankInfoLoading.value = false
}

// 获取配置
const configLoading = ref(false)
const configResult = ref(null)

const handleGetConfig = async () => {
  configLoading.value = true
  try {
    const res = await getPaymentConfig()
    if (res.code === '00000') {
      configResult.value = res.data
    } else {
      ElMessage.error(res.msg || '获取失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
  configLoading.value = false
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
.payment-content {
  min-height: 280px;
}
.result-box {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.result-box p {
  margin: 4px 0;
}
.bank-info-box {
  margin-top: 12px;
  padding: 10px;
  background: #e8f5e9;
  border-radius: 4px;
}
.bank-info-box p {
  margin: 4px 0;
}
.qr-box {
  margin-top: 12px;
  padding: 10px;
  background: #fff3e0;
  border-radius: 4px;
  text-align: center;
}
.qr-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}
.pay-url-box {
  margin-top: 12px;
  padding: 10px;
  background: #e3f2fd;
  border-radius: 4px;
}
.pay-url-box a {
  word-break: break-all;
  color: #1976d2;
}
.amount-display {
  font-size: 15px;
  font-weight: 600;
  color: #667eea;
}
.currency-display {
  font-size: 14px;
  color: #333;
}
</style>
