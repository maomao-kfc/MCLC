<template>
  <div class="payment-page">
    <div class="payment-bg-shape shape-1"></div>
    <div class="payment-bg-shape shape-2"></div>
    <div class="payment-bg-shape shape-3"></div>

    <div class="payment-container">
      <div class="payment-header">
        <h1 class="payment-title">Payment</h1>
        <p class="payment-subtitle">Secure & Fast Transaction</p>
      </div>

      <div class="payment-card">
<!--        <el-tabs v-model="activeTab" class="payment-tabs" type="border-card">-->
<!--          <el-tab-pane  name="pay">-->
<!--            <div class="pay-content">-->
<!--              <el-form :model="payForm" ref="payFormRef" label-width="110px" label-position="right" size="large">-->
<!--&lt;!&ndash;                <el-form-item label="注册用户ID" prop="registrationId" :rules="[{ required: true, message: '请输入注册用户ID', trigger: 'blur' }]">&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-input v-model.number="payForm.registrationId" placeholder="请输入注册用户ID" />&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->

<!--                <el-form-item label="支付方式" prop="paymentMethod" :rules="[{ required: true, message: '请选择支付方式', trigger: 'change' }]">-->
<!--                  <div class="payment-methods">-->
<!--                    <div-->
<!--                      v-for="method in paymentMethods"-->
<!--                      :key="method.value"-->
<!--                      class="method-card"-->
<!--                      :class="{ active: payForm.paymentMethod === method.value }"-->
<!--                      @click="payForm.paymentMethod = method.value"-->
<!--                    >-->
<!--                      <div class="method-icon" :style="{ background: method.bg }">-->
<!--                        <span v-html="method.icon"></span>-->
<!--                      </div>-->
<!--                      <span class="method-label">{{ method.label }}</span>-->
<!--                    </div>-->
<!--                  </div>-->
<!--                </el-form-item>-->

<!--                <el-form-item label="金额">-->
<!--                  <div class="amount-row">-->
<!--                    <span class="currency-text">CNY ¥</span>-->
<!--                    <span class="amount-text">{{ payForm.amount || '&#45;&#45;' }}</span>-->
<!--                  </div>-->
<!--                </el-form-item>-->

<!--                <el-form-item v-if="payForm.paymentMethod === 3 || payForm.paymentMethod === 4" label="支付场景">-->
<!--                  <el-radio-group v-model="payForm.scene">-->
<!--                    <el-radio-button value="scan">PC扫码付</el-radio-button>-->
<!--                    <el-radio-button value="h5">手机H5付</el-radio-button>-->
<!--                  </el-radio-group>-->
<!--                </el-form-item>-->

<!--                <el-form-item>-->
<!--                  <el-button-->
<!--                    type="primary"-->
<!--                    size="large"-->
<!--                    :loading="payLoading"-->
<!--                    @click="handleCreatePayment"-->
<!--                    class="pay-btn"-->
<!--                  >-->
<!--                    确认支付-->
<!--                  </el-button>-->
<!--                </el-form-item>-->
<!--              </el-form>-->

<!--              <transition name="result-fade">-->
<!--                <div v-if="payResult" class="pay-result">-->
<!--                  <div class="result-card">-->
<!--                    <div class="result-header">-->
<!--                      <span class="result-icon" v-html="payResult.paymentStatus === 'succeeded' ? '✓' : '○'"></span>-->
<!--                      <span class="result-status">{{ payResult.paymentStatus === 'succeeded' ? '支付成功' : '待支付' }}</span>-->
<!--                    </div>-->
<!--                    <div class="result-detail">-->
<!--                      <div class="detail-row">-->
<!--                        <span>订单号</span>-->
<!--                        <strong>{{ payResult.orderNo }}</strong>-->
<!--                      </div>-->
<!--                      <div class="detail-row">-->
<!--                        <span>支付方式</span>-->
<!--                        <strong>{{ getMethodLabel(payResult.paymentMethod) }}</strong>-->
<!--                      </div>-->
<!--                      <div class="detail-row">-->
<!--                        <span>金额</span>-->
<!--                        <strong class="amount-highlight">{{ payResult.amount }} CNY</strong>-->
<!--                      </div>-->
<!--                    </div>-->
<!--                    <div v-if="payResult.qrCodeUrl" class="qr-section">-->
<!--                      <p class="qr-title">请扫码支付</p>-->
<!--                      <div class="qr-wrapper">-->
<!--                        <img :src="'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=' + encodeURIComponent(payResult.qrCodeUrl)" width="200" height="200" alt="QR Code" />-->
<!--                      </div>-->
<!--                      <p class="qr-tip">使用对应App扫描二维码完成支付</p>-->
<!--                    </div>-->
<!--                    <div v-if="payResult.payUrl" class="pay-link">-->
<!--                      <a :href="payResult.payUrl" target="_blank" class="link-btn">点击前往支付页面</a>-->
<!--                    </div>-->
<!--                    <div v-if="payResult.bankTransferInfo" class="bank-info">-->
<!--                      <p class="bank-title">银行转账信息</p>-->
<!--                      <div class="bank-detail" v-for="(val, key) in payResult.bankTransferInfo" :key="key">-->
<!--                        <span>{{ key }}</span><strong>{{ val }}</strong>-->
<!--                      </div>-->
<!--                    </div>-->
<!--                    <div v-if="payResult.paymentStatus === 'succeeded'" class="success-badge">-->
<!--                      <span>✓ Payment Confirmed</span>-->
<!--                    </div>-->
<!--                  </div>-->
<!--                </div>-->
<!--              </transition>-->
<!--            </div>-->
<!--          </el-tab-pane>-->

<!--&lt;!&ndash;          <el-tab-pane label="查询支付状态" name="status">&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="status-content">&ndash;&gt;-->
<!--&lt;!&ndash;              <el-form inline size="large">&ndash;&gt;-->
<!--&lt;!&ndash;                <el-form-item label="注册用户ID">&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-input v-model.number="statusId" placeholder="请输入注册用户ID" />&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;                <el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-button type="primary" :loading="statusLoading" @click="handleQueryStatus">查询</el-button>&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;              </el-form>&ndash;&gt;-->
<!--&lt;!&ndash;              <div v-if="statusResult" class="status-result-card">&ndash;&gt;-->
<!--&lt;!&ndash;                <div class="result-row">&ndash;&gt;-->
<!--&lt;!&ndash;                  <span>支付状态</span>&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-tag :type="statusResult.paymentStatus === 'succeeded' ? 'success' : 'warning'">&ndash;&gt;-->
<!--&lt;!&ndash;                    {{ statusResult.paymentStatus === 'succeeded' ? '已支付' : statusResult.paymentStatus }}&ndash;&gt;-->
<!--&lt;!&ndash;                  </el-tag>&ndash;&gt;-->
<!--&lt;!&ndash;                </div>&ndash;&gt;-->
<!--&lt;!&ndash;                <div class="result-row">&ndash;&gt;-->
<!--&lt;!&ndash;                  <span>支付方式</span>&ndash;&gt;-->
<!--&lt;!&ndash;                  <strong>{{ getMethodLabel(statusResult.paymentMethod) }}</strong>&ndash;&gt;-->
<!--&lt;!&ndash;                </div>&ndash;&gt;-->
<!--&lt;!&ndash;                <div class="result-row">&ndash;&gt;-->
<!--&lt;!&ndash;                  <span>金额</span>&ndash;&gt;-->
<!--&lt;!&ndash;                  <strong>{{ statusResult.amount }}</strong>&ndash;&gt;-->
<!--&lt;!&ndash;                </div>&ndash;&gt;-->
<!--&lt;!&ndash;              </div>&ndash;&gt;-->
<!--&lt;!&ndash;            </div>&ndash;&gt;-->
<!--&lt;!&ndash;          </el-tab-pane>&ndash;&gt;-->

<!--&lt;!&ndash;          <el-tab-pane label="确认银行转账" name="confirm">&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="confirm-content">&ndash;&gt;-->
<!--&lt;!&ndash;              <el-form inline size="large">&ndash;&gt;-->
<!--&lt;!&ndash;                <el-form-item label="注册用户ID">&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-input v-model.number="confirmId" placeholder="请输入注册用户ID" />&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;                <el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-button type="primary" :loading="confirmLoading" @click="handleConfirm">确认转账</el-button>&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;              </el-form>&ndash;&gt;-->
<!--&lt;!&ndash;            </div>&ndash;&gt;-->
<!--&lt;!&ndash;          </el-tab-pane>&ndash;&gt;-->

<!--&lt;!&ndash;          <el-tab-pane label="退款" name="refund">&ndash;&gt;-->
<!--&lt;!&ndash;            <div class="refund-content">&ndash;&gt;-->
<!--&lt;!&ndash;              <div class="refund-notice">&ndash;&gt;-->
<!--&lt;!&ndash;                <span class="refund-icon">⚠</span>&ndash;&gt;-->
<!--&lt;!&ndash;                <p>退款功能仅在支付成功后方可使用。请确认您的支付状态后再进行退款操作。</p>&ndash;&gt;-->
<!--&lt;!&ndash;              </div>&ndash;&gt;-->
<!--&lt;!&ndash;              <el-form inline size="large">&ndash;&gt;-->
<!--&lt;!&ndash;                <el-form-item label="注册用户ID">&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-input v-model.number="refundId" placeholder="请输入注册用户ID" />&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;                <el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;                  <el-button type="danger" :loading="refundLoading" @click="handleRefund">申请退款</el-button>&ndash;&gt;-->
<!--&lt;!&ndash;                </el-form-item>&ndash;&gt;-->
<!--&lt;!&ndash;              </el-form>&ndash;&gt;-->
<!--&lt;!&ndash;            </div>&ndash;&gt;-->
<!--&lt;!&ndash;          </el-tab-pane>&ndash;&gt;-->
<!--        </el-tabs>-->
        <div class="pay-content">
          <el-form :model="payForm" ref="payFormRef" label-width="110px" label-position="right" size="large">
            <!--                <el-form-item label="注册用户ID" prop="registrationId" :rules="[{ required: true, message: '请输入注册用户ID', trigger: 'blur' }]">-->
            <!--                  <el-input v-model.number="payForm.registrationId" placeholder="请输入注册用户ID" />-->
            <!--                </el-form-item>-->

            <el-form-item label="支付方式" prop="paymentMethod" :rules="[{ required: true, message: '请选择支付方式', trigger: 'change' }]">
              <div class="payment-methods">
                <div
                    v-for="method in paymentMethods"
                    :key="method.value"
                    class="method-card"
                    :class="{ active: payForm.paymentMethod === method.value }"
                    @click="payForm.paymentMethod = method.value"
                >
                  <div class="method-icon" :style="{ background: method.bg }">
                    <span v-html="method.icon"></span>
                  </div>
                  <span class="method-label">{{ method.label }}</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="金额">
              <div class="amount-row">
                <span class="currency-text">CNY ¥</span>
                <span class="amount-text">{{ payForm.amount || '--' }}</span>
              </div>
            </el-form-item>

            <el-form-item v-if="payForm.paymentMethod === 3 || payForm.paymentMethod === 4" label="支付场景">
              <el-radio-group v-model="payForm.scene">
                <el-radio-button value="scan">扫码支付</el-radio-button>
<!--                <el-radio-button value="h5">手机H5付</el-radio-button>-->
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  size="large"
                  :loading="payLoading"
                  @click="handleCreatePayment"
                  class="pay-btn"
              >
                确认支付
              </el-button>
            </el-form-item>
          </el-form>

          <transition name="result-fade">
            <div v-if="payResult" class="pay-result">
              <div class="result-card">
                <div class="result-header">
                  <span class="result-icon" v-html="payResult.paymentStatus === 'succeeded' ? '✓' : '○'"></span>
                  <span class="result-status">{{ payResult.paymentStatus === 'succeeded' ? '支付成功' : '待支付' }}</span>
                </div>
                <div class="result-detail">
                  <div class="detail-row">
                    <span>订单号</span>
                    <strong>{{ payResult.orderNo }}</strong>
                  </div>
                  <div class="detail-row">
                    <span>支付方式</span>
                    <strong>{{ getMethodLabel(payResult.paymentMethod) }}</strong>
                  </div>
                  <div class="detail-row">
                    <span>金额</span>
                    <strong class="amount-highlight">{{ payResult.amount }} CNY</strong>
                  </div>
                </div>
                <div v-if="payResult.qrCodeUrl" class="qr-section">
                  <p class="qr-title">请扫码支付</p>
                  <div class="qr-wrapper">
                    <img :src="'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=' + encodeURIComponent(payResult.qrCodeUrl)" width="200" height="200" alt="QR Code" />
                  </div>
                  <p class="qr-tip">使用对应App扫描二维码完成支付</p>
                </div>
                <div v-if="payResult.payUrl" class="pay-link">
                  <a :href="payResult.payUrl" target="_blank" class="link-btn">点击前往支付页面</a>
                </div>
                <div v-if="payResult.bankTransferInfo" class="bank-info">
                  <p class="bank-title">银行转账信息</p>
                  <div class="bank-detail" v-for="(val, key) in payResult.bankTransferInfo" :key="key">
                    <span>{{ key }}</span><strong>{{ val }}</strong>
                  </div>
                </div>
                <div v-if="payResult.paymentStatus === 'succeeded'" class="success-badge">
                  <span>✓ Payment Confirmed</span>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRegisterStore } from '@/stores/register'
import { getRegisterUserByEmail } from '@/api/user'
import {
  createPaymentIntent,
  getPaymentStatus,
  confirmBankTransfer,
  refund
} from '@/api/payment'

const route = useRoute()
const userStore = useUserStore()
const registerStore = useRegisterStore()

const activeTab = ref('pay')

const paymentMethods = [
  { value: 1, label: '信用卡', icon: '💳', bg: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { value: 2, label: '银行转账', icon: '🏦', bg: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { value: 3, label: '微信', icon:  `<img src="/public/assets/image/weixinzhifu.png" alt="微信支付" style="width:24px;height:24px;vertical-align:middle;">`, bg: 'rgb(31,190,42)' },
  { value: 4, label: '支付宝', icon: `<img src="/public/assets/image/zhifubaozhifu.png" alt="微信支付" style="width:24px;height:24px;vertical-align:middle;">`, bg:  'rgb(0,159,232)' }
]

const getMethodLabel = (val) => {
  const map = { credit_card: '信用卡', bank_transfer: '银行转账', wechat: '微信', alipay: '支付宝' }
  return map[val] || val || ''
}

const paymentMethodReverseMap = {
  'credit_card': 1, 'bank_transfer': 2, 'wechat': 3, 'alipay': 4
}

// 支付表单
const payFormRef = ref()
const payLoading = ref(false)
const payResult = ref(null)
const payForm = reactive({
  registrationId: null,
  paymentMethod: '',
  amount: '',
  currency: 'cny',
  scene: 'scan'
})

const initPayForm = () => {
  payForm.registrationId = null
  payForm.paymentMethod = ''
  payForm.amount = ''
  payForm.currency = 'cny'
  payForm.scene = 'scan'
  payResult.value = null

  const signupRt = registerStore.state.signup?.registerTypePrice
  if (signupRt) {
    payForm.amount = String(signupRt)
  }
  const pMethod = registerStore.state.paymentMethod
  if (pMethod && [1, 2, 3, 4].includes(pMethod)) {
    payForm.paymentMethod = pMethod
  }

  if (userStore.isLoggedIn) {
    const email = userStore.userInfo?.email
    if (email) {
      getRegisterUserByEmail(email).then(res => {
        if (res.code === '00000' && res.data?.list?.length > 0) {
          const userData = res.data.list[0]
          if (userData.id) {
            payForm.registrationId = userData.id
          }
          if (userData.paymentMethod != null) {
            const raw = userData.paymentMethod
            const num = typeof raw === 'number' ? raw : (paymentMethodReverseMap[raw] || '')
            if ([1, 2, 3, 4].includes(num)) {
              payForm.paymentMethod = num
            }
          }
          if (userData.paymentAmount != null) {
            payForm.amount = String(userData.paymentAmount)
          }
        }
      }).catch(e => {
        console.error('查询注册用户失败：', e)
      })
    }
  }
}

const handleCreatePayment = () => {
  payFormRef.value.validate(async (valid) => {
    if (!valid) return
    payLoading.value = true
    try {
      const res = await createPaymentIntent({
        registrationId: payForm.registrationId,
        paymentMethod: payForm.paymentMethod,
        amount: payForm.amount,
        currency: payForm.currency,
        scene: payForm.scene,
        returnUrl: window.location.origin + '/payment'
      })
      if (res.code === '00000') {
        payResult.value = res.data
        if (res.data?.paymentStatus === 'succeeded') {
          sessionStorage.setItem('paymentSuccess', 'true')
        }
        ElMessage.success('支付意图创建成功！')
      } else {
        ElMessage.error(res.msg || '创建失败')
      }
    } catch (e) {
      ElMessage.error('请求失败，请稍后重试')
    }
    payLoading.value = false
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
      if (res.data?.paymentStatus === 'succeeded') {
        sessionStorage.setItem('paymentSuccess', 'true')
      }
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
  if (!sessionStorage.getItem('paymentSuccess')) {
    ElMessage.warning('请先完成支付后再申请退款')
    return
  }
  refundLoading.value = true
  try {
    const res = await refund(refundId.value)
    if (res.code === '00000') {
      sessionStorage.removeItem('paymentSuccess')
      ElMessage.success('退款成功！')
    } else {
      ElMessage.error(res.msg || '退款失败')
    }
  } catch (e) {
    ElMessage.error('请求失败')
  }
  refundLoading.value = false
}

onMounted(() => {
  initPayForm()
  if (route.query.tab === 'refund') {
    activeTab.value = 'refund'
  }
})
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  position: relative;
  overflow: hidden;
  padding: 40px 20px 80px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.payment-bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  pointer-events: none;
}
.shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #667eea, transparent);
  top: -150px;
  right: -100px;
}
.shape-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #f093fb, transparent);
  bottom: -100px;
  left: -80px;
}
.shape-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #4facfe, transparent);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.payment-container {
  width: 100%;
  max-width: 720px;
  position: relative;
  z-index: 1;
}

.payment-header {
  text-align: center;
  margin-bottom: 36px;
}
.payment-title {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 8px;
  letter-spacing: 3px;
  text-shadow: 0 2px 20px rgba(102, 126, 234, 0.4);
}
.payment-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.65);
  margin: 0;
  letter-spacing: 2px;
}

.payment-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  padding: 30px 30px 20px 0;
  backdrop-filter: blur(10px);
}

:deep(.payment-tabs) {
  border: none !important;
  box-shadow: none !important;
}
:deep(.payment-tabs .el-tabs__header) {
  background: #f8f9fc;
  border-bottom: 1px solid #eee;
  margin: 0;
}
:deep(.payment-tabs .el-tabs__content) {
  padding: 32px 36px;
}

/* 支付方式卡片 */
.payment-methods {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  width: 100%;
}
.method-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fff;
}
.method-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}
.method-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06), rgba(118, 75, 162, 0.06));
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}
.method-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}
.method-label {
  font-size: 13px;
  color: #555;
  font-weight: 500;
}
.method-card.active .method-label {
  color: #667eea;
  font-weight: 600;
}

/* 金额输入 */
.amount-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.currency-text {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}
.amount-text {
  font-size: 15px;
  font-weight: 600;
  color: #667eea;
}

/* 支付按钮 */
.pay-btn {
  width: 100%;
  height: 48px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s;
}
.pay-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 支付结果 */
.pay-result {
  margin-top: 24px;
}
.result-card {
  background: linear-gradient(135deg, #f8f9fc, #fff);
  border: 1px solid #e8e8e8;
  border-radius: 16px;
  padding: 24px;
}
.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.result-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}
.result-status {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}
.result-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}
.detail-row strong {
  color: #333;
}
.amount-highlight {
  font-size: 20px !important;
  color: #667eea !important;
}

.qr-section {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.qr-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}
.qr-wrapper {
  display: inline-block;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.qr-wrapper img {
  display: block;
  border-radius: 8px;
}
.qr-tip {
  font-size: 13px;
  color: #999;
  margin-top: 12px;
}

.pay-link {
  text-align: center;
  margin-top: 16px;
}
.link-btn {
  display: inline-block;
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  text-decoration: none;
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s;
}
.link-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.bank-info {
  margin-top: 16px;
  padding: 16px;
  background: #f0fdf4;
  border-radius: 12px;
  border: 1px solid #bbf7d0;
}
.bank-title {
  font-weight: 700;
  color: #166534;
  margin-bottom: 10px;
}
.bank-detail {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #555;
  padding: 4px 0;
}

.success-badge {
  text-align: center;
  margin-top: 16px;
  padding: 10px;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: #fff;
  border-radius: 10px;
  font-weight: 700;
  font-size: 15px;
  letter-spacing: 1px;
}

/* 状态查询 */
.status-content {
  min-height: 120px;
}
.status-result-card {
  margin-top: 16px;
  padding: 20px;
  background: #f8f9fc;
  border-radius: 12px;
}
.result-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #eee;
}
.result-row:last-child { border-bottom: none; }
.result-row strong { color: #333; }

/* 确认内容 */
.confirm-content {
  min-height: 80px;
}

/* 退款 */
.refund-content {
  min-height: 120px;
}
.refund-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 12px;
  margin-bottom: 20px;
}
.refund-notice p {
  margin: 0;
  font-size: 14px;
  color: #856404;
  line-height: 1.6;
}
.refund-icon {
  font-size: 20px;
  flex-shrink: 0;
}

/* 过渡动画 */
.result-fade-enter-active {
  transition: all 0.4s ease-out;
}
.result-fade-leave-active {
  transition: all 0.2s ease-in;
}
.result-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.result-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .payment-page {
    padding: 20px 12px 60px;
  }
  .payment-title {
    font-size: 30px;
  }
  :deep(.payment-tabs .el-tabs__content) {
    padding: 20px 16px;
  }
  .payment-methods {
    grid-template-columns: repeat(2, 1fr);
  }
  .amount-row {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
