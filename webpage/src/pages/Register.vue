<template>
  <div class="register-page">
    <div class="lang-bar">
      <button :class="['lang-btn', { active: currentLang === 'en' }]" @click="switchLang('en')">English</button>
      <span class="divider">|</span>
      <button :class="['lang-btn', { active: currentLang === 'zh' }]" @click="switchLang('zh')">简体中文</button>
    </div>
    <div  class="register-banner">
      <el-image class="hI" :src="objVal?.titleImage" />
    </div>
    <div class="register-menu">
      <div class="menu-items">
        <div class="menu-item" :class="{ active: route.path === '/register/enroll' }" @click="goTo('/register/enroll')">{{ t('register.steps.enroll') }}</div>
        <div class="menu-item" :class="{ active: route.path === '/register/consent' }" @click="goTo('/register/consent')">{{ t('register.steps.consent') }}</div>
        <div class="menu-item" :class="{ active: route.path === '/register/contact' }" @click="goTo('/register/contact')">{{ t('register.steps.contact') }}</div>
        <div class="menu-item" :class="{ active: route.path === '/register/signup' }" @click="goTo('/register/signup')">{{ t('register.steps.signup') }}</div>
        <div class="menu-item" :class="{ active: route.path === '/register/confirm' }" @click="goTo('/register/confirm')">{{ t('register.steps.confirm') }}</div>
        <div class="menu-item" :class="{ active: route.path === '/register/final' }" @click="goTo('/register/final')">{{ t('register.steps.final') }}</div>
      </div>
    </div>
    <div class="register-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import { useGuestStore } from '@/stores/guest.js'
import { onMounted } from "vue";
import { storeToRefs } from 'pinia'
import { useRegisterStore } from '@/stores/register'
import { ElMessage } from 'element-plus'

const registerStore = useRegisterStore()
const guestStore = useGuestStore()
const { locale, t } = useI18n()
const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)

const switchLang = (lang) => {
  langStore.setLang(lang)
  locale.value = lang
}
const { hwList } = storeToRefs(guestStore)
const objVal = computed(() => {
  return  hwList.value?.[0]
})
onMounted(async () => {
  await guestStore.gethwList()
})

const isEnrollCompleted = () => {
  const enroll = registerStore.state.enroll
  return enroll.firstName && enroll.lastName && enroll.email && enroll.registerType
}

const isConsentCompleted = () => {
  const consent = registerStore.state.consent
  return consent.agreePolicy1 && consent.agreePolicy2 && consent.agreeBadgeScan
}

const isContactCompleted = () => {
  const contact = registerStore.state.contact
  if (!contact.title || !contact.firstName || !contact.lastName || !contact.email || !contact.organization || !contact.phone || !contact.street || !contact.zipCode || !contact.city || !contact.country) {
    return false
  }
  if (contact.hasBillingAddress) {
    const billing = contact.billingAddress || {}
    if (!billing.street || !billing.zipCode || !billing.city || !billing.country) {
      return false
    }
  }
  if (registerStore.state.enroll.registerType === '1') {
    const members = contact.teamMembers || []
    if (members.length === 0) return false
    for (const m of members) {
      if (!m.firstName || !m.lastName || !m.email || !m.phone) return false
    }
  }
  return true
}

const stepOrder = [
  '/register/enroll',
  '/register/consent',
  '/register/contact',
  '/register/signup',
  '/register/confirm',
  '/register/final'
]

const goTo = (path) => {
  if (route.path === path) {
    return
  }

  const currentIndex = stepOrder.indexOf(route.path)
  const targetIndex = stepOrder.indexOf(path)

  if (targetIndex <= currentIndex) {
    router.push(path)
    return
  }

  if (route.path === '/register/enroll') {
    if (!isEnrollCompleted()) {
      ElMessage.error(t('register.validation.enrollRequired'))
      return
    }
  }
  if (route.path === '/register/consent') {
    if (!isEnrollCompleted()) {
      ElMessage.error(t('register.validation.enrollRequired'))
      return
    }
    if (!isConsentCompleted()) {
      ElMessage.error(t('register.validation.consentRequired'))
      return
    }
  }
  if (route.path === '/register/contact') {
    if (!isEnrollCompleted()) {
      ElMessage.error(t('register.validation.enrollRequired'))
      return
    }
    if (!isConsentCompleted()) {
      ElMessage.error(t('register.validation.consentRequired'))
      return
    }
    if (!isContactCompleted()) {
      ElMessage.error(t('register.validation.contactRequired'))
      return
    }
  }
  if (path !== '/register/enroll' && path !== '/register/consent' && path !== '/register/contact') {
    if (!isEnrollCompleted()) {
      ElMessage.error(t('register.validation.enrollRequired'))
      return
    }
    if (!isConsentCompleted()) {
      ElMessage.error(t('register.validation.consentRequired'))
      return
    }
    if (!isContactCompleted()) {
      ElMessage.error(t('register.validation.contactRequired'))
      return
    }
  }
  router.push(path)
}
</script>

<style scoped>
.lang-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 8px 0;
}
.lang-btn {
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 14px;
  font-size: 14px;
  color: #333;
  transition: color 0.2s;
}
.lang-btn.active {
  color: #114C9A;
  font-weight: 600;
}
.lang-btn:hover {
  color: #114C9A;
}
.divider {
  margin: 0 2px;
  color: #bdc3c7;
}
:deep(#app){
}
.register-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0 10%;
  box-sizing: border-box;
  padding-top: 0;
  margin: 0;
}
.register-menu {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 12px 0;
}
.hI{
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.menu-items {
  display: flex;
  gap: 40px;
  justify-content: flex-start;
  flex-wrap: nowrap;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  padding: 0 20px;
  box-sizing: border-box;
}
.menu-items::-webkit-scrollbar {
  height: 4px;
}
.menu-items::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}
.menu-items::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.menu-item {
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  color: #333;
  font-size: 14px;
  transition: all 0.3s;
  white-space: nowrap;
  flex: 1;
  text-align: center;
  cursor: pointer;
}
.menu-item:hover {
  background: #f5f5f5;
  color: #7d7ef4;
}
.menu-item.active {
  background: #7d7ef4;
  color: #fff;
}
.register-content {
  width: 100%;
  flex: 1;
  background: #f7f7f7;
  padding: 20px 0;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .register-page {
    padding: 0;
  }
  .menu-items {
    gap: 0;
    padding: 0 8px;
  }
  .menu-item {
    padding: 8px 10px;
    font-size: 12px;
    flex: none;
    white-space: nowrap;
  }
  .register-content {
    padding: 10px 0;
  }
}
</style>
