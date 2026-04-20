<!-- 右边侧边栏（倒计时/注册/联系我们） -->
<template>
  <div class="you">
    <!-- 倒计时组件 -->
    <div class="countDown">
      <span>{{ t('countDown.title') }}</span>
      <div class="countdown-timer">{{ countdownText }}</div>
    </div>
    <!-- 注册入口 -->
    <div class="register">
      <div class="register-item" @click="goToRegister">
        <el-image class="register-icon orange" src="/assets/image/common/ge.png" />
        <p>{{ t('register.personal') }}</p>
      </div>
      <div class="register-item" @click="goToRegister">
        <el-image class="register-icon blue" src="/assets/image/common/tuan.png" />
        <p>{{ t('register.team') }}</p>
      </div>
      <div class="register-item" @click="submit">
        <el-image class="register-icon green" src="/assets/image/common/touGao.png" />
        <p>{{ t('register.submit') }}</p>
      </div>
    </div>
    <!-- 重要日期 -->
    <div class="chapter">
      <h3 class="module-title">{{ t('importantDates.title') }}</h3>
      <div class="date-row">
        <span class="date-label">{{ t('importantDates.conferenceDate') }}</span>
        <span class="date-value">{{ t('importantDatesValue.conferenceDate') }}</span>
      </div>
      <div class="date-row">
        <span class="date-label">{{ t('importantDates.registrationDeadline') }}</span>
        <span class="date-value">{{ t('importantDatesValue.registrationDeadline') }}</span>
      </div>
      <div class="date-row">
        <span class="date-label">{{ t('importantDates.onSiteCheckIn') }}</span>
        <span class="date-value">{{ t('importantDatesValue.onSiteCheckIn') }}</span>
      </div>
    </div>
    <!-- 联系我们 -->
    <div class="contactUs">
      <h3 class="module-title">{{ t('contact.us') }}</h3>
      <h4 class="meeting-name">{{ t('hero.subTitle') }}</h4>
      
      <!-- 1. 授权主办方 -->
      <div class="contact-block">
        <h5 class="block-title">{{ t('contactInfo.authorizedHost') }} {{ t('contactInfo.contactInfo') }}</h5>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>
          <span>{{ currentLang === 'zh' ? '袁老师 / 梅老师' : 'Ms. Yuan / Ms. Mei' }}</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>
          <span>13062687248</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.email') }}：</span>
          <span>organizerMCLC@163.com</span>
        </div>
      </div>
    
      <!-- 2. 注册缴费 -->
      <div class="contact-block">
        <h5 class="block-title">{{ t('contactInfo.registrationPayment') }}</h5>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>
          <span>{{ currentLang === 'zh' ? '袁老师 / 邓老师' : 'Ms. Yuan / Ms. Deng' }}</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>
          <span>13701963591</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.email') }}：</span>
          <span>registrationMCLC@163.com</span>
        </div>
      </div>
    
      <!-- 3. 学术投稿 -->
      <div class="contact-block">
        <h5 class="block-title">{{ t('contactInfo.academicSubmission') }}</h5>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>
          <span>{{ currentLang === 'zh' ? '周老师 / 邓老师' : 'Mr. Zhou / Ms. Deng' }}</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>
          <span>13701963591</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.email') }}：</span>
          <span>academicMCLC@163.com</span>
        </div>
      </div>
    
      <!-- 4. 商务合作 -->
      <div class="contact-block">
        <h5 class="block-title">{{ t('contactInfo.businessCooperation') }}</h5>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>
          <span>{{ currentLang === 'zh' ? '梅老师' : 'Ms. Mei' }}</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>
          <span>13062687248</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.email') }}：</span>
          <span>organizerMCLC@163.com</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)
const router = useRouter()

//注册跳转
const goToRegister = () => {
	router.push('/register')
	// 需要区分“个人/团队”：
	// router.push({ path: '/register', query: { type: 'personal' } }) // 个人
	// router.push({ path: '/register', query: { type: 'team' } }) // 团队
}

//投稿跳转
const submit = () => {
	router.push('/submit')
}

// 倒计时逻辑（月份从0开始，所以5月填写4）
const targetDate = new Date(2026, 4, 15, 0, 0, 0)
const countdownText = ref('')
let timer = null

const updateCountdown = () => {
  const now = new Date()
  const diff = targetDate - now
  if (diff <= 0) {
    countdownText.value = t('countDown.opened')
    return
  }
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % 86400000) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % 3600000) / (1000 * 60))
  const seconds = Math.floor((diff % 60000) / 1000)
  countdownText.value = `${days}天 ${hours}小时 ${minutes}分钟 ${seconds}秒`
}

onMounted(() => {
  updateCountdown()
  timer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.you {
  flex: 1;
  min-width: 320px;
  /* position: sticky;
  top: 20px; */
}
.countDown {
  background: #114C9A;
  color: #fff;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 12px;
}
.countdown-timer {
  font-size: 24px;
  font-weight: bold;
  margin-top: 8px;
}
.register {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.register-item {
  flex: 1;
  text-align: center;
}
.register-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  padding: 10px;
  margin-bottom: 8px;
}
.register-icon.orange {
  background: #f7931e;
}
.register-icon.blue {
  background: #00a0e9;
}
.register-icon.green {
  background: #00b97a;
}
.chapter {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 12px;
}
.date-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.contactUs {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: left;
}
.module-title {
  color: #7952b3;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: left;
}
.meeting-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
  text-align: left;
}
.contact-block {
  margin-bottom: 20px;
}
.block-title {
  font-weight: bold;
  margin-bottom: 8px;
  text-align: left;
}
.contact-row {
  font-size: 14px;
  line-height: 2;
  text-align: left;
}
.contact-label {
  font-weight: 500;
}
@media (max-width: 992px) {
  .you {
    width: 100%;
    position: static;
  }
}
.register-item {
  cursor: pointer; /* 鼠标悬浮变成小手 */
  transition: 0.3s;
}
.register-item:hover {
  transform: scale(1.05); /* 悬浮轻微放大，不影响布局 */
}
</style>