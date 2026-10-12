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
      <div class="register-item" :class="{ disabled: !registrationEnabled }" @click="showPersonalDialog">
        <el-image class="register-icon orange" src="/assets/image/common/ge.png" />
        <p>{{ t('register.personal') }}</p>
      </div>
<!--      <div class="register-item" @click="showTeamDialog">-->
<!--        <el-image class="register-icon blue" src="/assets/image/common/tuan.png" />-->
<!--        <p>{{ t('register.team') }}</p>-->
<!--      </div>-->
      <div class="register-item" @click="showPaperDialog">
        <el-image class="register-icon green" src="/assets/image/common/touGao.png" />
        <p>{{ t('register.submit') }}</p>
      </div>
    </div>

    <!-- 个人注册弹窗 -->
    <el-dialog v-model="personalDialogVisible" :title="t('register.personal')" width="420px">
      <el-form :model="personalForm" label-width="80px">
        <el-form-item :label="t('register.account')">
          <el-input v-model="personalForm.username" :placeholder="t('register.inputAccount')" />
        </el-form-item>
        <el-form-item :label="t('register.password')">
          <el-input v-model="personalForm.password" type="password" :placeholder="t('register.inputPassword')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="personalDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitPersonalRegister">{{ t('common.submit') }}</el-button>
      </template>
    </el-dialog>

    <!-- 团队注册弹窗 -->
    <el-dialog v-model="teamDialogVisible" :title="t('register.team')" width="420px">
      <el-form :model="teamForm" label-width="80px">
        <el-form-item :label="t('register.account')">
          <el-input v-model="teamForm.username" :placeholder="t('register.inputAccount')" />
        </el-form-item>
        <el-form-item :label="t('register.password')">
          <el-input v-model="teamForm.password" type="password" :placeholder="t('register.inputPassword')" />
        </el-form-item>
        <el-form-item :label="t('register.company')">
          <el-input v-model="teamForm.company" :placeholder="t('register.inputCompany')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teamDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitTeamRegister">{{ t('common.submit') }}</el-button>
      </template>
    </el-dialog>

    <!-- 论文初稿弹窗 -->
    <el-dialog
      v-model="paperDialogVisible"
      :title="t('register.submit')"
      width="70%"
      top="5vh"
      :close-on-click-modal="false"
      class="paper-dialog"
    >
      <div class="dialog-content">
        <!-- 投稿须知 -->
        <div class="dialog-notice" :class="{ collapsed: !noticeExpanded }">
          <div class="notice-header" @click="noticeExpanded = !noticeExpanded">
            <h4 class="notice-title">{{ t('submit.noticeTitle') }}</h4>
            <span class="notice-arrow">{{ noticeExpanded ? '▲' : '▼' }}</span>
          </div>
          <div class="notice-body">
            <div class="notice-body-content" v-show="noticeExpanded">
              <div class="notice-content" v-html="t('submit.noticeContent')"></div>
            </div>
            <div class="agree-check">
              <el-checkbox v-model="isAgreeNotice">
                {{ t('submit.agreeNotice') }}
              </el-checkbox>
            </div>
          </div>
        </div>

        <!-- 投稿表单 -->
        <div class="dialog-form">
          <el-form :model="paperForm" label-width="120px" class="paper-form" size="large" :disabled="!isAgreeNotice">
            <el-form-item :label="t('submit.form.paperTitle')" required>
              <el-input v-model="paperForm.paperTitle" :placeholder="t('submit.form.paperTitlePlaceholder')" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item :label="t('submit.form.firstAuthor')" required>
              <el-input v-model="paperForm.firstAuthor" :placeholder="t('submit.form.firstAuthorPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('submit.form.authorPhone')" required>
              <el-input v-model="paperForm.authorPhone" :placeholder="t('submit.form.authorPhonePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('submit.form.authorEmail')" required>
              <el-input v-model="paperForm.authorEmail" :placeholder="t('submit.form.authorEmailPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('submit.form.company')" required>
              <el-input v-model="paperForm.company" :placeholder="t('submit.form.companyPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('submit.form.abstract')" required>
              <el-input v-model="paperForm.paperAbstract" type="textarea" :rows="6" :placeholder="t('submit.form.abstractPlaceholder')" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item :label="t('submit.form.keywords')">
              <el-input v-model="paperForm.keywords" :placeholder="t('submit.form.keywordsPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('submit.form.paperFile')">
              <el-upload
                ref="uploadRef"
                class="paper-upload"
                :auto-upload="false"
                :limit="1"
                accept=".pdf,.doc,.docx"
                :on-change="handleFileChange"
              >
                <el-button type="primary" :disabled="!isAgreeNotice">{{ t('submit.form.uploadBtn') }}</el-button>
                <template #tip>
                  <div class="el-upload__tip">{{ t('submit.form.uploadTips') }}</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>
        </div>

        <!-- 稿件状态查询 -->
        <div class="dialog-status">
          <div class="status-header">
            <h4>{{ t('submit.statusTitle') }}</h4>
            <el-button type="primary" plain size="small" @click="queryPaperStatus">
              {{ t('submit.statusQueryBtn') }}
            </el-button>
          </div>
          <el-table :data="paperList" v-loading="statusLoading" size="small" max-height="200">
            <el-table-column prop="paperTitle" :label="t('submit.table.title')" min-width="150" />
            <el-table-column prop="submitTime" :label="t('submit.table.submitTime')" width="150" />
            <el-table-column prop="status" :label="t('submit.table.status')" width="100">
              <template #default="scope">
                <el-tag size="small" :type="getStatusType(scope.row.status)">
                  {{ t(`submit.status.${scope.row.status}`) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('submit.table.operation')" width="100">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="viewPaperDetail(scope.row)">
                  {{ t('submit.table.view') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="paperDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitPaper" :disabled="!isAgreeNotice">{{ t('common.submit') }}</el-button>
      </template>
    </el-dialog>
    <!-- 重要日期 -->
    <div class="chapter">
      <h3 class="module-title">{{ t('importantDates.title') }}</h3>
      <div class="date-row">
        <span class="date-label">{{ t('importantDates.conferenceDate') }}</span>
        <span class="date-value">{{ objVal?.time }}</span>
      </div>
      <div class="date-row">
        <span class="date-label" v-html="t('importantDates.paperSubmissionDeadline')"></span>
        <span class="date-value">{{ objVal?.paperDeadline }}</span>
      </div>
      <div class="date-row">
        <span class="date-label">{{ t('importantDates.registrationDeadline') }}</span>
        <span class="date-value">{{ objVal?.stopTime }}</span>
      </div>
      <div class="date-row">
        <span class="date-label">{{ t('importantDates.onSiteCheckIn') }}</span>
        <span class="date-value">{{ objVal?.xcTime }}</span>
      </div>
    </div>
    <!-- 联系我们 -->
    <div class="contactUs">
      <h3 class="module-title">{{ t('contact.us') }}</h3>
      <h4 class="meeting-name">{{ objVal?.title }}</h4>

      <div class="contact-block" v-for="(item,index) in contactList" :key="item.id" >
        <h5 class="block-title">{{ getContactTitle(item?.title) }}</h5>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>
          <span>{{ item?.contactName }}</span>
        </div>
        <div class="contact-row" v-show="item?.contactPhone">
          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>
          <span>{{item?.contactPhone}}</span>
        </div>
        <div class="contact-row">
          <span class="contact-label">{{ t('contactInfo.email') }}：</span>
          <span>{{item?.contactMail}}</span>
        </div>
      </div>
      <!-- 1. 授权主办方 -->
<!--      <div class="contact-block">-->
<!--        <h5 class="block-title">{{ t('contactInfo.authorizedHost') }} {{ t('contactInfo.contactInfo') }}</h5>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>-->
<!--          <span>{{ currentLang === 'zh' ? '袁老师 / 梅老师' : 'Ms. Yuan / Ms. Mei' }}</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>-->
<!--          <span>13062687248</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.email') }}：</span>-->
<!--          <span>organizerMCLC@163.com</span>-->
<!--        </div>-->
<!--      </div>-->

      <!-- 2. 注册缴费 -->
<!--      <div class="contact-block">-->
<!--        <h5 class="block-title">{{ t('contactInfo.registrationPayment') }}</h5>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>-->
<!--          <span>{{ currentLang === 'zh' ? '袁老师 / 邓老师' : 'Ms. Yuan / Ms. Deng' }}</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>-->
<!--          <span>13701963591</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.email') }}：</span>-->
<!--          <span>registrationMCLC@163.com</span>-->
<!--        </div>-->
<!--      </div>-->

      <!-- 3. 学术投稿 -->
<!--      <div class="contact-block">-->
<!--        <h5 class="block-title">{{ t('contactInfo.academicSubmission') }}</h5>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>-->
<!--          <span>{{ currentLang === 'zh' ? '周老师 / 邓老师' : 'Mr. Zhou / Ms. Deng' }}</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>-->
<!--          <span>13701963591</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.email') }}：</span>-->
<!--          <span>academicMCLC@163.com</span>-->
<!--        </div>-->
<!--      </div>-->
<!--    -->
      <!-- 4. 商务合作 -->
<!--      <div class="contact-block">-->
<!--        <h5 class="block-title">{{ t('contactInfo.businessCooperation') }}</h5>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.contactPerson') }}：</span>-->
<!--          <span>{{ currentLang === 'zh' ? '梅老师' : 'Ms. Mei' }}</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.phone') }}：</span>-->
<!--          <span>13062687248</span>-->
<!--        </div>-->
<!--        <div class="contact-row">-->
<!--          <span class="contact-label">{{ t('contactInfo.email') }}：</span>-->
<!--          <span>organizerMCLC@163.com</span>-->
<!--        </div>-->
<!--      </div>-->
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import {storeToRefs} from "pinia";
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPageContact } from  '@/api/portal/contact'
import { useGuestStore } from '@/stores/guest.js'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const guestStore = useGuestStore()
const { hwList } = storeToRefs(guestStore)
const { t } = useI18n()
const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)

const registrationEnabled = computed(() => {
  return hwList.value?.[0]?.registrationEnabled === 1
})

const scheduleEnabled = computed(() => {
  return hwList.value?.[0]?.scheduleEnabled === 1
})

// 弹窗状态
const personalDialogVisible = ref(false)
const teamDialogVisible = ref(false)
const paperDialogVisible = ref(false)

// 表单数据
const personalForm = ref({ username: '', password: '' })
const teamForm = ref({ username: '', password: '', company: '' })
const paperForm = ref({
  paperTitle: '',
  firstAuthor: '',
  authorPhone: '',
  authorEmail: '',
  company: '',
  paperAbstract: '',
  keywords: '',
  paperFile: null
})

// ==================== 全英文输入过滤 ====================
//
// 功能说明：投稿弹窗中要求全英文输入，自动过滤中文字符
// 实现方式：watch + nextTick（同 FirstDraft.vue / Submit.vue）
//
// 过滤字段：论文标题(paperTitle)、第一作者(firstAuthor)、所属单位(company)、摘要(paperAbstract)、关键词(keywords)
// 不过滤：联系电话(authorPhone)、联系邮箱(authorEmail) — 天然英文格式
// 不过滤：论文附件(paperFile) — 文件上传，非文本输入
//
// 注意：paperForm 是 ref，访问属性需用 paperForm.value[field]
// ==================== 全英文输入过滤 ====================

const CHINESE_CHAR_REGEX = /[\u4e00-\u9fa5]/g

// 需要过滤中文的字段列表
const chineseFilterFields = ['paperTitle', 'firstAuthor', 'company', 'paperAbstract', 'keywords']
chineseFilterFields.forEach(field => {
  watch(
    () => paperForm.value[field],
    (newVal) => {
      // 检测值中是否包含中文字符
      if (newVal && CHINESE_CHAR_REGEX.test(newVal)) {
        // nextTick 确保在 Vue 完成 DOM 更新后再修改值，避免与 v-model 冲突
        nextTick(() => {
          paperForm.value[field] = newVal.replace(CHINESE_CHAR_REGEX, '')
          ElMessage.warning(t('submit.form.chineseCharFiltered'))
        })
      }
    }
  )
})

// 论文投稿状态相关
const isAgreeNotice = ref(false)
const noticeExpanded = ref(true)
const statusLoading = ref(false)
const paperList = ref([])

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    reviewing: 'info',
    accepted: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || 'info'
}

const queryPaperStatus = () => {
  statusLoading.value = true
  setTimeout(() => {
    paperList.value = []
    statusLoading.value = false
  }, 500)
}

const viewPaperDetail = (row) => {
  ElMessageBox.alert(`
    <p>论文标题：${row.paperTitle}</p>
    <p>提交时间：${row.submitTime}</p>
    <p>当前状态：${t(`submit.status.${row.status}`)}</p>
  `, t('submit.detailTitle'), {
    dangerouslyUseHTMLString: true,
    confirmButtonText: t('submit.confirm')
  })
}

// 打开弹窗
const showPersonalDialog = () => {
  if (!registrationEnabled.value) {
    ElMessage.warning(t('register.registrationClosed'))
    return
  }
  ElMessageBox.confirm(t('sidebar.confirmPersonalReg'), t('sidebar.prompt'), {
    confirmButtonText: t('sidebar.confirmBtn'),
    cancelButtonText: t('sidebar.cancelBtn'),
    type: 'warning'
  }).then(() => {
    const route = router.resolve('/register?typeStatus=1')
    window.open(window.location.origin + route.href, '_blank')
  }).catch(() => {})
}

const showTeamDialog = () => {
  ElMessageBox.confirm(t('sidebar.confirmTeamReg'), t('sidebar.prompt'), {
    confirmButtonText: t('sidebar.confirmBtn'),
    cancelButtonText: t('sidebar.cancelBtn'),
    type: 'warning'
  }).then(() => {
    const route = router.resolve('/register?typeStatus=2')
    window.open(window.location.origin + route.href, '_blank')
  }).catch(() => {})
}

const showPaperDialog = () => {
  const route = router.resolve({ path: '/first-draft' })
  window.open(window.location.origin + route.href, '_blank')
}

const handleFileChange = (file) => {
  paperForm.value.paperFile = file.raw
}

// 提交
const submitPersonalRegister = () => {
  if (!personalForm.value.username || !personalForm.value.password) {
    ElMessage.warning(t('register.fillRequired'))
    return
  }
  ElMessage.success(t('register.registerSuccess'))
  personalDialogVisible.value = false
}

const submitTeamRegister = () => {
  if (!teamForm.value.username || !teamForm.value.password) {
    ElMessage.warning(t('register.fillRequired'))
    return
  }
  ElMessage.success(t('register.registerSuccess'))
  teamDialogVisible.value = false
}

const submitPaper = () => {
  if (!paperForm.value.paperTitle || !paperForm.value.firstAuthor || !paperForm.value.authorEmail) {
    ElMessage.warning(t('register.fillRequired'))
    return
  }
  ElMessage.success(t('submit.submitSuccess'))
  paperDialogVisible.value = false
}

// 倒计时逻辑
const countdownText = ref('')
let timer = null

const parseDate = (val) => {
  if (!val) return null
  const parts = val.split(' ')
  const [year, month, day] = parts[0].split('-')
  if (parts.length >= 2) {
    const [hour, minute, second] = parts[1].split(':')
    return new Date(Number(year), Number(month) - 1, Number(day), Number(hour) || 0, Number(minute) || 0, Number(second) || 0)
  }
  return new Date(Number(year), Number(month) - 1, Number(day), 0, 0, 0)
}

const startDate = computed(() => parseDate(hwList.value?.[0]?.conferenceDateStart))
const endDate = computed(() => parseDate(hwList.value?.[0]?.conferenceDateEnd))

const updateCountdown = () => {
  const now = new Date()
  if (!startDate.value) {
    countdownText.value = t('countDown.opened')
    return
  }
  if (now >= endDate.value) {
    countdownText.value = t('countDown.ended')
    return
  }
  if (now >= startDate.value) {
    countdownText.value = t('countDown.opened')
    return
  }
  const diff = startDate.value - now
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % 86400000) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % 3600000) / (1000 * 60))
  const seconds = Math.floor((diff % 60000) / 1000)
  countdownText.value = `${days}${t('countDown.days')} ${hours}${t('countDown.hours')} ${minutes}${t('countDown.minutes')} ${seconds}${t('countDown.seconds')}`
}
const contactList = ref([])
const formatDate = (val) => {
    if (!val) return ''
    const [year, month, day] = val.split(' ')[0].split('-')
    const m = String(Number(month)).padStart(2, '0')
    const d = String(Number(day)).padStart(2, '0')
    if (langStore.currentLang === 'en') {
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      return `${months[Number(month) - 1]} ${Number(day)}, ${year}`
    }
    return `${year}年${m}月${d}日`
}

const objVal = computed(() => {
  let obj = {}
  obj.time = formatDate(hwList.value?.[0]?.startTmie) + '-' + formatDate(hwList.value?.[0]?.endTmie)
  obj.stopTime = formatDate(hwList.value?.[0]?.generalRegStart) + '-' + formatDate(hwList.value?.[0]?.generalRegEnd)
  obj.xcTime = formatDate(hwList.value?.[0]?.conferenceDateStart) + '-' + formatDate(hwList.value?.[0]?.conferenceDateEnd)
  obj.paperDeadline = formatDate(hwList.value?.[0]?.earlyBirdRegStart) + '-' + formatDate(hwList.value?.[0]?.earlyBirdRegEnd)
  obj.title = hwList.value?.[0]?.title
  return obj
})
const contactTitleMap = {
  '授权主办方': 'contactInfo.authorizedHost',
  '授权主办方联系信息': ['contactInfo.authorizedHost', 'contactInfo.contactInfo'],
  '注册缴费': 'contactInfo.registrationPayment',
  '学术投稿': 'contactInfo.academicSubmission',
  '商务合作': 'contactInfo.businessCooperation'
}

const getContactTitle = (title) => {
  if (!title) return ''
  const mapping = contactTitleMap[title]
  if (!mapping) return title
  if (Array.isArray(mapping)) {
    return mapping.map(k => t(k)).join(' ')
  }
  return t(mapping)
}

const fetchContactData = async () => {
  const res = await getPageContact()
  if(res?.msg === '成功'){
    contactList.value = JSON.parse(JSON.stringify( res?.data))
  }
}

onMounted(async() => {
  await guestStore.gethwList()
  await fetchContactData()
  updateCountdown()
  timer = setInterval(updateCountdown, 1000)
})

watch(() => langStore.currentLang, async () => {
  await guestStore.gethwList()
  await fetchContactData()
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
  background: #7d7ef4;
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
.register-item.disabled {
  opacity: 0.4;
  pointer-events: none;
  cursor: not-allowed;
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
  text-align: left;
}
.date-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
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
.paper-form .el-form-item {
  margin-bottom: 18px;
}
.paper-upload {
  width: 100%;
}
.dialog-notice {
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
}
.dialog-notice.collapsed {
  max-height: 48px;
  overflow: hidden;
}
.dialog-notice .notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f5f5;
  cursor: pointer;
}
.dialog-notice .notice-header:hover {
  background: #eee;
}
.dialog-notice .notice-title {
  font-size: 16px;
  font-weight: bold;
  color: #114C9A;
  margin: 0;
}
.dialog-notice .notice-arrow {
  color: #114C9A;
  font-size: 12px;
}
.dialog-notice .notice-body {
  padding: 16px;
}
.dialog-notice .notice-body-content {
  max-height: 250px;
  overflow-y: auto;
  margin-bottom: 12px;
}
.dialog-notice .notice-content {
  font-size: 13px;
  line-height: 1.6;
  color: #666;
}
.dialog-notice .agree-check {
  padding-top: 12px;
  border-top: 1px solid #eee;
  text-align: center;
}
.dialog-status {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
.dialog-status .status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.dialog-status .status-header h4 {
  margin: 0;
  font-size: 15px;
  color: #114C9A;
}

@media (max-width: 768px) {
  :deep(.paper-dialog) {
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
  }
}
</style>
