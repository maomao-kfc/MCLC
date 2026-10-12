<template>
  <div class="lang-bar">
    <button :class="['lang-btn', { active: currentLang === 'en' }]" @click="switchLang('en')">English</button>
    <span class="divider">|</span>
    <button :class="['lang-btn', { active: currentLang === 'zh' }]" @click="switchLang('zh')">简体中文</button>
  </div>
  <div class="first-draft-wrapper">
    <h2 class="page-title">{{ t('firstDraft.pageTitle') }}</h2>

    <div class="common-card notice-card">
      <h3 class="notice-title">{{ t('firstDraft.noticeTitle') }}</h3>
      <div class="notice-subtitle">{{ t('firstDraft.conferenceInfo') }}</div>
      <div class="notice-subtitle">{{ t('firstDraft.procedureSubtitle') }}</div>
      <div class="notice-content" v-html="t('firstDraft.noticeContent')"></div>

      <div class="agree-check">
        <el-checkbox v-model="isAgreeNotice">
          {{ t('firstDraft.agreeNotice') }}
        </el-checkbox>
      </div>
    </div>

    <div class="common-card form-card" :class="{ 'form-disabled': !isAgreeNotice }">
      <el-form
        :model="draftForm"
        :rules="draftRules"
        ref="draftFormRef"
        label-width="120px"
        size="large"
        :disabled="!isAgreeNotice"
      >
        <el-form-item :label="t('firstDraft.form.paperTitle')" prop="paperTitle">
          <el-input
            v-model="draftForm.paperTitle"
            :placeholder="t('firstDraft.form.paperTitlePlaceholder')"
            @focus="clearFieldValidate('paperTitle')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.name')" prop="name">
          <el-input
            v-model="draftForm.name"
            :placeholder="t('firstDraft.form.namePlaceholder')"
            @focus="clearFieldValidate('name')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.company')" prop="company">
          <el-input
            v-model="draftForm.company"
            :placeholder="t('firstDraft.form.companyPlaceholder')"
            @focus="clearFieldValidate('company')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.contactNumber')" prop="contactNumber">
          <el-input
            v-model="draftForm.contactNumber"
            :placeholder="t('firstDraft.form.contactNumberPlaceholder')"
            @focus="clearFieldValidate('contactNumber')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.firstAuthor')" prop="firstAuthor">
          <el-input
            v-model="draftForm.firstAuthor"
            :placeholder="t('firstDraft.form.firstAuthorPlaceholder')"
            @focus="clearFieldValidate('firstAuthor')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.contactEmail')" prop="contactEmail">
          <el-input
            v-model="draftForm.contactEmail"
            :placeholder="t('firstDraft.form.contactEmailPlaceholder')"
            @focus="clearFieldValidate('contactEmail')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.confirmContactEmail')" prop="confirmContactEmail">
          <el-input
            v-model="draftForm.confirmContactEmail"
            :placeholder="t('firstDraft.form.confirmContactEmailPlaceholder')"
            @focus="clearFieldValidate('confirmContactEmail')"
          />
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.abstract')" prop="paperAbstract">
          <el-input
            v-model="draftForm.paperAbstract"
            type="textarea"
            :rows="6"
            :placeholder="t('firstDraft.form.abstractPlaceholder')"
            @input="handleAbstractInput"
            @focus="clearFieldValidate('paperAbstract')"
          />
          <!-- 实时单词数显示 -->
          <div class="word-count" :class="{ 'word-count-exceed': abstractWordCount > MAX_ABSTRACT_WORDS }">
            {{ abstractWordCount }} / {{ MAX_ABSTRACT_WORDS }} {{ t('firstDraft.form.wordUnit') }}
          </div>
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.keywords')" prop="keywords">
          <el-input
            v-model="draftForm.keywords"
            :placeholder="t('firstDraft.form.keywordsPlaceholder')"
            @focus="clearFieldValidate('keywords')"
          />
          <div class="form-tips">{{ t('firstDraft.form.keywordsTips') }}</div>
        </el-form-item>

        <el-form-item :label="t('firstDraft.form.appendix')" prop="appendix">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            action="#"
            :before-upload="beforeFileUpload"
            :on-change="handleFileChange"
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.docx"
            :disabled="uploading"
          >
            <el-button type="primary" :loading="uploading">{{ t('firstDraft.form.uploadBtn') }}</el-button>
            <template #tip>
              <div class="el-upload__tip">
                {{ t('firstDraft.form.uploadTips') }}
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleSubmit"
            :disabled="!isAgreeNotice"
            style="width: 200px; margin-right: 16px;"
          >
            {{ t('firstDraft.form.submitBtn') }}
          </el-button>
          <el-button size="large" @click="resetForm">
            {{ t('firstDraft.form.resetBtn') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 模块三：投稿状态查询 -->
    <!-- 投稿者通过邮箱+验证码查询自己的投稿记录，支持查看详情和修改待审核稿件 -->
    <div class="common-card status-card">
      <h3 class="status-title">{{ t('firstDraft.queryTitle') }}</h3>

      <!-- 查询表单：邮箱 + 验证码 -->
      <div class="query-form">
        <el-form :inline="true" :model="queryForm" size="large">
          <el-form-item :label="t('firstDraft.query.email')">
            <el-input
              v-model="queryForm.email"
              :placeholder="t('firstDraft.query.emailPlaceholder')"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item :label="t('firstDraft.query.code')">
            <div class="code-input-wrapper">
              <el-input
                v-model="queryForm.code"
                :placeholder="t('firstDraft.query.codePlaceholder')"
                style="width: 140px"
              />
              <el-button
                type="primary"
                plain
                :disabled="codeCooldown > 0 || !queryForm.email"
                @click="handleSendCode"
                :loading="sendCodeLoading"
              >
                {{ codeCooldown > 0 ? `${codeCooldown}s` : t('firstDraft.query.sendCode') }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQueryPapers" :loading="queryLoading">
              {{ t('firstDraft.query.queryBtn') }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 查询结果表格 -->
      <el-table :data="paperList" v-loading="queryLoading" style="width: 100%" empty-text="">
        <el-table-column prop="paperTitle" :label="t('firstDraft.queryTable.title')" min-width="200" />
        <el-table-column prop="firstAuthor" :label="t('firstDraft.queryTable.firstAuthor')" width="120" />
        <el-table-column prop="paperStatus" :label="t('firstDraft.queryTable.status')" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.paperStatus)">
              {{ getStatusText(scope.row.paperStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('firstDraft.queryTable.operation')" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="viewPaperDetail(scope.row)">
              {{ t('firstDraft.queryTable.view') }}
            </el-button>
            <el-button
              link
              type="warning"
              @click="editPaper(scope.row)"
              v-if="scope.row.paperStatus === 1"
            >
              {{ t('firstDraft.queryTable.edit') }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-tip" v-if="hasQueried">{{ t('firstDraft.query.emptyTip') }}</div>
          <div class="empty-tip" v-else>{{ t('firstDraft.query.pleaseQuery') }}</div>
        </template>
      </el-table>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="t('firstDraft.detailTitle')"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="1" border size="large" v-if="detailData">
        <el-descriptions-item :label="t('firstDraft.detail.paperTitle')">{{ detailData.paperTitle }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.name')">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.company')">{{ detailData.company }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.firstAuthor')">{{ detailData.firstAuthor }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.contactNumber')">{{ detailData.contactNumber }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.contactEmail')">{{ detailData.contactEmail }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.abstract')">{{ detailData.paperAbstract }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.keywords')">{{ detailData.keywords }}</el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.status')">
          <el-tag :type="getStatusType(detailData.paperStatus)">{{ getStatusText(detailData.paperStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('firstDraft.detail.rejectReason')" v-if="detailData.paperStatus === 3">
          <span style="color: #f56c6c">{{ detailData.rejectReason || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">{{ t('firstDraft.detail.close') }}</el-button>
        <el-button
          type="primary"
          @click="editFromDetail"
          v-if="detailData && detailData.paperStatus === 1"
        >
          {{ t('firstDraft.detail.editBtn') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="t('firstDraft.editTitle')"
      width="600px"
      destroy-on-close
    >
      <el-form
        :model="editForm"
        :rules="editRules"
        ref="editFormRef"
        label-width="120px"
        size="large"
      >
        <el-form-item :label="t('firstDraft.form.paperTitle')" prop="paperTitle">
          <el-input v-model="editForm.paperTitle" :placeholder="t('firstDraft.form.paperTitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('firstDraft.form.firstAuthor')" prop="firstAuthor">
          <el-input v-model="editForm.firstAuthor" :placeholder="t('firstDraft.form.firstAuthorPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('firstDraft.form.company')" prop="company">
          <el-input v-model="editForm.company" :placeholder="t('firstDraft.form.companyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('firstDraft.form.contactNumber')" prop="contactNumber">
          <el-input v-model="editForm.contactNumber" :placeholder="t('firstDraft.form.contactNumberPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('firstDraft.form.abstract')" prop="paperAbstract">
          <el-input v-model="editForm.paperAbstract" type="textarea" :rows="4" :placeholder="t('firstDraft.form.abstractPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('firstDraft.form.keywords')" prop="keywords">
          <el-input v-model="editForm.keywords" :placeholder="t('firstDraft.form.keywordsPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('firstDraft.form.appendix')" prop="appendix">
          <el-input v-model="editForm.appendix" :placeholder="t('firstDraft.edit.appendixPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">{{ t('firstDraft.edit.cancel') }}</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">{{ t('firstDraft.edit.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { addContribution } from '@/api/contribution'
import { sendQueryCode, queryPapersByEmail, getPaperDetail, updatePaperBySubmitter } from '@/api/submit'
import { uploadFiles } from "@/api/upload";
import { useLangStore } from '@/stores/lang.js'
import { useUserStore } from '@/stores/user'
const { locale, t } = useI18n()

/**
 * 单词计数函数（参考 Microsoft Word 规则）
 * 中文每个汉字算1词，英文按空格分词，don't算1词，e-mail算1词
 */
function countWords(text) {
  if (!text || !text.trim()) return 0
  const chineseChars = (text.match(/[\u4e00-\u9fa5]/g) || []).length
  const wordPattern = /[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:'[A-Za-z]+)?/g
  const englishWords = (text.match(wordPattern) || []).length
  return chineseChars + englishWords
}

// 摘要最大允许单词数
const MAX_ABSTRACT_WORDS = 500

// 摘要实时单词数
const abstractWordCount = computed(() => countWords(draftForm.paperAbstract))

// 记录上一次单词数未超限的有效值，用于超限时回退
const lastValidAbstract = ref('')

/**
 * 摘要输入处理：达到500词后禁止继续输入
 * 原理：记录上一次有效值，当新输入导致单词数超过500时，直接回退到有效值
 * 这样用户在达到500词后，任何输入都不会生效
 */
const handleAbstractInput = (value) => {
  if (countWords(value) > MAX_ABSTRACT_WORDS) {
    // 超过500词，回退到上次有效值
    if (lastValidAbstract.value) {
      draftForm.paperAbstract = lastValidAbstract.value
    } else {
      // 首次输入就超限（如粘贴长文本），截断到500词
      const combinedPattern = /[\u4e00-\u9fa5]|[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:'[A-Za-z]+)?/g
      let match, lastValidIndex = 0, wordCount = 0
      while ((match = combinedPattern.exec(value)) !== null) {
        wordCount++
        if (wordCount >= MAX_ABSTRACT_WORDS) {
          lastValidIndex = match.index + match[0].length
          break
        }
      }
      const truncated = value.substring(0, lastValidIndex)
      draftForm.paperAbstract = truncated
      lastValidAbstract.value = truncated
    }
    ElMessage.warning(t('firstDraft.form.abstractTruncated'))
  } else {
    // 未超限，记录当前有效值
    lastValidAbstract.value = value
  }
}

const langStore = useLangStore()
const currentLang = computed(() => langStore.currentLang)

const switchLang = (lang) => {
  langStore.setLang(lang)
  locale.value = lang
}

const userStore = useUserStore()

const isAgreeNotice = ref(false)

const generateId = () => Date.now() * 1000 + Math.floor(Math.random() * 1000)

const getContributionId = () => {
  const storedId = sessionStorage.getItem('submissionUserId')
  return storedId || String(generateId())
}

const uploading = ref(false)
const uploadRef = ref()

const draftFormRef = ref()

// 用户聚焦输入框时清除该字段的验证错误（替代3秒自动清除，避免滚动后错误提示消失）
const clearFieldValidate = (prop) => {
  draftFormRef.value?.clearValidate(prop)
}

// 提交校验失败时，滚动到第一个校验失败的字段，确保用户能看到错误提示
const scrollToFirstError = () => {
  nextTick(() => {
    const firstErrorEl = document.querySelector('.form-card .el-form-item.is-error')
    if (firstErrorEl) {
      firstErrorEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  })
}

const draftForm = reactive({
  paperTitle: '',
  name: '',
  company: '',
  firstAuthor: '',
  contactNumber: '',
  contactEmail: '',
  confirmContactEmail: '',
  paperAbstract: '',
  keywords: '',
  appendix: '',
  sort: 0,
  status: 1,
  createBy: sessionStorage.getItem('submissionUserId') ? Number(sessionStorage.getItem('submissionUserId')) || Math.floor(Math.random() * 100000) : Math.floor(Math.random() * 100000)
})

// ==================== 全英文输入过滤 ====================
// 
// 功能说明：投稿页面要求全英文输入，自动过滤中文字符
// 实现方式：watch + nextTick
//   - watch 监听字段值变化，检测到中文字符时在 nextTick 中异步清除
//   - 使用 nextTick 是因为 v-model 会先更新值，需要在 DOM 更新后才能正确覆盖
//   - 这种方式比 @input 或 @update:model-value 更可靠，不会与 IME 输入法冲突
//
// 过滤字段：论文标题(paperTitle)、姓名(name)、第一作者(firstAuthor)、摘要(paperAbstract)、关键词(keywords)
// 不过滤：联系电话(contactNumber)、联系邮箱(contactEmail) — 天然英文格式
// 不过滤：论文附件(appendix) — 文件上传，非文本输入
//
// 正则说明：/[\u4e00-\u9fa5]/g 匹配所有 CJK 统一汉字（基本区）
// ==================== 全英文输入过滤 ====================

const CHINESE_CHAR_REGEX = /[\u4e00-\u9fa5]/g

// 需要过滤中文的字段列表
const chineseFilterFields = ['paperTitle', 'name', 'firstAuthor', 'paperAbstract', 'keywords']
chineseFilterFields.forEach(field => {
  watch(
    () => draftForm[field],
    (newVal) => {
      // 检测值中是否包含中文字符
      if (newVal && CHINESE_CHAR_REGEX.test(newVal)) {
        // 移除所有中文字符
        const filtered = newVal.replace(CHINESE_CHAR_REGEX, '')
        // nextTick 确保在 Vue 完成 DOM 更新后再修改值，避免与 v-model 冲突
        nextTick(() => {
          draftForm[field] = filtered
          ElMessage.warning(t('firstDraft.form.chineseCharFiltered'))
        })
      }
    }
  )
})

const draftRules = computed(() => ({
  paperTitle: [
    { required: true, message: t('firstDraft.form.paperTitleRequired'), trigger: 'blur' }
  ],
  name: [
    { required: true, message: t('firstDraft.form.nameRequired'), trigger: 'blur' }
  ],
  company: [
    { required: true, message: t('firstDraft.form.companyRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value.trim() === '') {
          draftForm.company = ''
          callback(new Error(t('firstDraft.form.companySpaceOnly')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  firstAuthor: [
    { required: true, message: t('firstDraft.form.firstAuthorRequired'), trigger: 'blur' }
  ],
  contactNumber: [
    { required: true, message: t('firstDraft.form.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('firstDraft.form.phoneInvalid'), trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: t('firstDraft.form.emailRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('firstDraft.form.emailInvalid'), trigger: 'blur' }
  ],
  confirmContactEmail: [
    { required: true, message: t('firstDraft.form.confirmEmailRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('firstDraft.form.emailInvalid'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value !== draftForm.contactEmail) {
          callback(new Error(t('firstDraft.form.confirmEmailMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  paperAbstract: [
    { required: true, message: t('firstDraft.form.abstractRequired'), trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value && countWords(value) > MAX_ABSTRACT_WORDS) {
          callback(new Error(t('firstDraft.form.abstractWordLimit')))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  appendix: []
}))

/**
 * 文件上传前校验
 * - 类型限制：仅允许 PDF 和 DOCX 格式
 * - 大小限制：不超过 10MB
 */
const beforeFileUpload = (file) => {
  const fileType = file.name.split('.').pop().toLowerCase()
  // 仅允许 pdf 和 docx，不支持 doc 格式
  const allowTypes = ['pdf', 'docx']
  if (!allowTypes.includes(fileType)) {
    ElMessage.error(t('firstDraft.form.fileTypeError'))
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error(t('firstDraft.form.fileSizeError'))
    return false
  }
  return true
}

const handleFileChange = async (file) => {
  const rawFile = file.raw
  if (!rawFile) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file1', rawFile)
    const res = await uploadFiles(formData)
    if (res.code === '00000' || res.code === 200) {
      draftForm.appendix = res.data.url || res.url || ''
      ElMessage.success('上传成功')
    } else {
      uploadRef.value?.clearFiles()
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (err) {
    uploadRef.value?.clearFiles()
    ElMessage.error('上传失败'+err)
  } finally {
    uploading.value = false
  }
}

const handleSubmit = async () => {
  if (uploading.value) {
    ElMessage.warning('文件正在上传中，请稍后')
    return
  }
  try {
    await draftFormRef.value.validate()
  } catch (e) {
    scrollToFirstError()
    return
  }

  const payload = {
    paperTitle: draftForm.paperTitle,
    name: draftForm.name,
    company: draftForm.company,
    firstAuthor: draftForm.firstAuthor,
    contactNumber: draftForm.contactNumber,
    contactEmail: draftForm.contactEmail,
    paperAbstract: draftForm.paperAbstract,
    keywords: draftForm.keywords,
    appendix: draftForm.appendix,
    contributionId: draftForm.contributionId || undefined
  }

  try {
    const res = await addContribution(payload)
    if (res.code === 200 || res.code === '00000' || res.data) {
      ElMessage.success(t('firstDraft.submitSuccess'))
      resetForm()
      draftForm.createBy = sessionStorage.getItem('submissionUserId') ? Number(sessionStorage.getItem('submissionUserId')) || Math.floor(Math.random() * 100000) : Math.floor(Math.random() * 100000)
    } else {
      ElMessage.error(res.msg || '投稿失败')
    }
  } catch (err) {
    // 提取后端返回的具体错误信息
    const errMsg = err?.response?.data?.msg || err?.response?.data?.message || err?.message || '网络请求失败，请稍后重试'
    ElMessage.error(errMsg)
    console.error('投稿提交错误：', err?.response?.data || err)
  }
}

const resetForm = () => {
  draftFormRef.value?.resetFields()
  draftForm.appendix = ''
  uploadRef.value?.clearFiles()
}

// ==================== 模块三：投稿查询相关 ====================
const queryLoading = ref(false)
const sendCodeLoading = ref(false)
const codeCooldown = ref(0)
const hasQueried = ref(false)
const paperList = ref([])

// 查询表单
const queryForm = reactive({
  email: '',
  code: ''
})

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref(null)

// 修改弹窗
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  contactEmail: '',
  company: '',
  paperTitle: '',
  firstAuthor: '',
  contactNumber: '',
  paperAbstract: '',
  keywords: '',
  appendix: ''
})
const editRules = computed(() => ({
  paperTitle: [{ required: true, message: t('firstDraft.form.paperTitlePlaceholder'), trigger: 'blur' }],
  firstAuthor: [{ required: true, message: t('firstDraft.form.firstAuthorPlaceholder'), trigger: 'blur' }],
  company: [{ required: true, message: t('firstDraft.form.companyRequired'), trigger: 'blur' }]
}))

// 发送验证码
const handleSendCode = async () => {
  if (!queryForm.email) {
    ElMessage.warning(t('firstDraft.query.emailPlaceholder'))
    return
  }
  try {
    sendCodeLoading.value = true
    await sendQueryCode(queryForm.email)
    ElMessage.success(t('firstDraft.query.codeSent'))
    codeCooldown.value = 60
    const timer = setInterval(() => {
      codeCooldown.value--
      if (codeCooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error(e.message || t('firstDraft.query.codeSendFail'))
  } finally {
    sendCodeLoading.value = false
  }
}

// 查询投稿列表
const handleQueryPapers = async () => {
  if (!queryForm.email || !queryForm.code) {
    ElMessage.warning(t('firstDraft.query.fillRequired'))
    return
  }
  try {
    queryLoading.value = true
    hasQueried.value = true
    const res = await queryPapersByEmail(queryForm.email, queryForm.code)
    paperList.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || t('firstDraft.query.queryFail'))
  } finally {
    queryLoading.value = false
  }
}

// 查看详情
const viewPaperDetail = async (row) => {
  try {
    const res = await getPaperDetail(row.id, row.contactEmail)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error(e.message || t('firstDraft.query.detailFail'))
  }
}

// 从详情弹窗进入修改
const editFromDetail = () => {
  detailDialogVisible.value = false
  editPaper(detailData.value)
}

// 打开修改弹窗
const editPaper = (row) => {
  editForm.id = row.id
  editForm.contactEmail = row.contactEmail
  editForm.company = row.company || ''
  editForm.paperTitle = row.paperTitle
  editForm.firstAuthor = row.firstAuthor
  editForm.contactNumber = row.contactNumber || ''
  editForm.paperAbstract = row.paperAbstract || ''
  editForm.keywords = row.keywords || ''
  editForm.appendix = row.appendix || ''
  editDialogVisible.value = true
}

// 提交修改
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate()
  try {
    editLoading.value = true
    await updatePaperBySubmitter({ ...editForm })
    ElMessage.success(t('firstDraft.edit.success'))
    editDialogVisible.value = false
    // 修改成功后刷新列表（重新发送验证码）
    if (queryForm.email) {
      handleSendCode()
    }
  } catch (e) {
    ElMessage.error(e.message || t('firstDraft.edit.fail'))
  } finally {
    editLoading.value = false
  }
}

// 状态文本映射（数字状态码 → 国际化文本）
const getStatusText = (status) => {
  const map = { 1: t('firstDraft.status.pending'), 2: t('firstDraft.status.approved'), 3: t('firstDraft.status.rejected') }
  return map[status] || t('firstDraft.status.unknown')
}

// 状态标签类型映射（数字状态码 → Element Plus tag type）
const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  const storedId = sessionStorage.getItem('submissionUserId')
  if (storedId) {
    draftForm.contributionId = storedId
    draftForm.createBy = Number(storedId) || draftForm.createBy
  }
})
</script>

<style scoped>
.first-draft-wrapper {
  width: 100%;
  padding: 48px 0 0 0;
}
.lang-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 40px;
  padding: 0 20px;
  background: #fff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}
.lang-btn {
  border: none;
  background: none;
  cursor: pointer;
  padding: 4px 12px;
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
  margin: 0 6px;
  color: #bdc3c7;
}
.page-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(90deg, #114C9A, #7952b3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 24px;
  text-align: center;
}
.common-card {
  border-radius: 12px;
  background: #fff;
  padding: 24px;
  margin-bottom: 24px;
}
.notice-title {
  font-size: 20px;
  font-weight: 700;
  color: #114C9A;
  margin-bottom: 12px;
  text-align: center;
}
.notice-subtitle {
  display: block;
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  color: #114C9A;
  margin-bottom: 6px;
  text-align: center;
  overflow: visible;
  white-space: normal;
}
.notice-content {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
  margin-bottom: 20px;
}
.agree-check {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
.form-disabled {
  opacity: 0.6;
  pointer-events: none;
}
.form-tips {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
<style scoped>
.first-draft-wrapper {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 30px 20px;
  padding-top: 50px;
}

/* 主标题 */
.page-title {
  font-size: 30px;
  font-weight: 700;
  background: linear-gradient(90deg, #114c9a, #7952b3, #a153e9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 36px;
  text-align: center;
  letter-spacing: 1px;
}

/* 通用卡片 */
.common-card {
  border-radius: 16px;
  background: #ffffff;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.common-card:hover {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
}

/* 公告卡片 */
.notice-title {
  font-size: 22px;
  font-weight: 700;
  color: #114c9a;
  margin-bottom: 12px;
  text-align: center;
  letter-spacing: 0.5px;
}
.notice-subtitle {
  display: block;
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  color: #114c9a;
  margin-bottom: 6px;
  text-align: center;
  letter-spacing: 0.5px;
  overflow: visible;
  white-space: normal;
}

.notice-content {
  font-size: 15px;
  line-height: 1.9;
  color: #444;
  max-height: 420px;
  overflow-y: auto;
  padding-right: 12px;
  margin-bottom: 24px;
  text-align: justify;
}

/* 滚动条美化 */
.notice-content::-webkit-scrollbar {
  width: 6px;
}
.notice-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}
.notice-content::-webkit-scrollbar-track {
  background: #f6f6f6;
}

/* 同意勾选 */
.agree-check {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  font-size: 15px;
}

/* 表单禁用状态 */
.form-disabled {
  opacity: 0.65;
  pointer-events: none;
  filter: grayscale(0.2);
}

/* 表单提示文字 */
.form-tips {
  font-size: 13px;
  color: #888;
  margin-top: 6px;
  line-height: 1.4;
}
.word-count {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}
.word-count-exceed {
  color: #f56c6c;
  font-weight: 600;
}

/* 表单间距优化 */
:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  font-size: 15px;
}

/* 按钮组居中美化 */
:deep(.el-form-item:last-child) {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 输入框、上传组件样式升级 */
:deep(.el-input__wrapper),
:deep(.el-upload__tip) {
  border-radius: 8px !important;
  box-shadow: none !important;
  border: 1px solid #e5e6eb;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c1c4cd;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #7952b3;
  box-shadow: 0 0 0 2px rgba(121, 82, 179, 0.1) !important;
}

/* 投稿查询模块 */
.status-title {
  font-size: 20px;
  font-weight: 700;
  color: #114c9a;
  margin: 0 0 20px 0;
  text-align: center;
}
.query-form {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.code-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 24px 0;
  font-size: 14px;
}
</style>
