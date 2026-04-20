<!-- 投稿页面 -->

<template>
  <div class="submit-wrapper">
    <!-- 页面标题 -->
    <h2 class="page-title">{{ t('submit.pageTitle') }}</h2>

    <!-- 投稿须知模块 -->
    <div class="common-card notice-card">
      <h3 class="notice-title">{{ t('submit.noticeTitle') }}</h3>
      <div class="notice-content" v-html="t('submit.noticeContent')"></div>
      
      <!-- 同意须知勾选 -->
      <div class="agree-check">
        <el-checkbox v-model="isAgreeNotice">
          {{ t('submit.agreeNotice') }}
        </el-checkbox>
      </div>
    </div>

    <!-- 投稿表单（仅勾选同意后可编辑） -->
    <div class="common-card form-card" :class="{ 'form-disabled': !isAgreeNotice }">
      <el-form 
        :model="submitForm" 
        :rules="submitRules" 
        ref="submitFormRef" 
        label-width="120px"
        size="large"
        :disabled="!isAgreeNotice"
      >
        <el-form-item :label="t('submit.form.paperTitle')" prop="paperTitle">
          <el-input 
            v-model="submitForm.paperTitle" 
            :placeholder="t('submit.form.paperTitlePlaceholder')"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="t('submit.form.firstAuthor')" prop="firstAuthor">
          <el-input 
            v-model="submitForm.firstAuthor" 
            :placeholder="t('submit.form.firstAuthorPlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="t('submit.form.authorPhone')" prop="authorPhone">
          <el-input 
            v-model="submitForm.authorPhone" 
            :placeholder="t('submit.form.authorPhonePlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="t('submit.form.authorEmail')" prop="authorEmail">
          <el-input 
            v-model="submitForm.authorEmail" 
            :placeholder="t('submit.form.authorEmailPlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="t('submit.form.company')" prop="company">
          <el-input 
            v-model="submitForm.company" 
            :placeholder="t('submit.form.companyPlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="t('submit.form.abstract')" prop="paperAbstract">
          <el-input 
            v-model="submitForm.paperAbstract" 
            type="textarea" 
            :rows="6"
            :placeholder="t('submit.form.abstractPlaceholder')"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item :label="t('submit.form.keywords')" prop="keywords">
          <el-input 
            v-model="submitForm.keywords" 
            :placeholder="t('submit.form.keywordsPlaceholder')"
          />
          <div class="form-tips">{{ t('submit.form.keywordsTips') }}</div>
        </el-form-item>

        <el-form-item :label="t('submit.form.paperFile')" prop="paperFile">
          <el-upload
            class="upload-demo"
            action="#"
            :before-upload="beforeFileUpload"
            :on-change="handleFileChange"
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.doc,.docx"
          >
            <el-button type="primary">{{ t('submit.form.uploadBtn') }}</el-button>
            <template #tip>
              <div class="el-upload__tip">
                {{ t('submit.form.uploadTips') }}
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            @click="handleSubmit"
            :disabled="!isAgreeNotice"
            style="width: 200px; margin-right: 16px;"
          >
            {{ t('submit.form.submitBtn') }}
          </el-button>
          <el-button size="large" @click="resetForm">
            {{ t('submit.form.resetBtn') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 稿件状态查询 -->
    <div class="common-card status-card">
      <div class="status-header">
        <h3 class="status-title">{{ t('submit.statusTitle') }}</h3>
        <el-button type="primary" plain @click="queryPaperStatus">
          {{ t('submit.statusQueryBtn') }}
        </el-button>
      </div>

      <el-table :data="paperList" v-loading="statusLoading" style="width: 100%">
        <el-table-column prop="paperTitle" :label="t('submit.table.title')" min-width="200" />
        <el-table-column prop="submitTime" :label="t('submit.table.submitTime')" width="180" />
        <el-table-column prop="status" :label="t('submit.table.status')" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ t(`submit.status.${scope.row.status}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('submit.table.operation')" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="viewPaperDetail(scope.row)">
              {{ t('submit.table.view') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入投稿接口
import { submitPaper, getPaperList } from '@/api/submit.js'

const { t } = useI18n()

// 1. 须知勾选状态
const isAgreeNotice = ref(false)

// 2. 表单相关
const submitFormRef = ref()
const submitForm = reactive({
  paperTitle: '',
  firstAuthor: '',
  authorPhone: '',
  authorEmail: '',
  company: '',
  paperAbstract: '',
  keywords: '',
  paperFile: null
})

// 表单校验规则
const submitRules = computed(() => ({
  paperTitle: [
    { required: true, message: t('submit.form.paperTitleRequired'), trigger: 'blur' }
  ],
  firstAuthor: [
    { required: true, message: t('submit.form.firstAuthorRequired'), trigger: 'blur' }
  ],
  authorPhone: [
    { required: true, message: t('submit.form.phoneRequired'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('submit.form.phoneInvalid'), trigger: 'blur' }
  ],
  authorEmail: [
    { required: true, message: t('submit.form.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('submit.form.emailInvalid'), trigger: 'blur' }
  ],
  company: [
    { required: true, message: t('submit.form.companyRequired'), trigger: 'blur' }
  ],
  paperAbstract: [
    { required: true, message: t('submit.form.abstractRequired'), trigger: 'blur' },
    { max: 500, message: t('submit.form.abstractLength'), trigger: 'blur' }
  ],
  paperFile: [
    { required: true, message: t('submit.form.fileRequired'), trigger: 'change' }
  ]
}))

// 3. 文件上传处理
const beforeFileUpload = (file) => {
  const fileType = file.name.split('.').pop().toLowerCase()
  const allowTypes = ['pdf', 'doc', 'docx']
  if (!allowTypes.includes(fileType)) {
    ElMessage.error(t('submit.form.fileTypeError'))
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error(t('submit.form.fileSizeError'))
    return false
  }
  return true
}
const handleFileChange = (file) => {
  submitForm.paperFile = file.raw
}

// 4. 表单提交
const handleSubmit = async () => {
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    // 对接后端接口
    const formData = new FormData()
    Object.keys(submitForm).forEach(key => {
      formData.append(key, submitForm[key])
    })
    await submitPaper(formData)

    ElMessage.success(t('submit.submitSuccess'))
    resetForm()
    // 提交后自动刷新稿件列表
    queryPaperStatus()
  } catch (err) {
    console.error('投稿失败：', err)
    ElMessage.error(t('submit.submitFail'))
  }
}

// 重置表单
const resetForm = () => {
  submitFormRef.value?.resetFields()
  submitForm.paperFile = null
}

// 5. 稿件状态查询
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
const queryPaperStatus = async () => {
  statusLoading.value = true
  try {
    const res = await getPaperList()
    paperList.value = res.data || []
  } catch (err) {
    ElMessage.error(t('submit.queryFail'))
  } finally {
    statusLoading.value = false
  }
}
const viewPaperDetail = (row) => {
  ElMessageBox.alert(`
    <p>论文标题：${row.paperTitle}</p>
    <p>提交时间：${row.submitTime}</p>
    <p>当前状态：${t(`submit.status.${row.status}`)}</p>
    <p>审核意见：${row.reviewRemark || t('submit.noRemark')}</p>
  `, t('submit.detailTitle'), {
    dangerouslyUseHTMLString: true,
    confirmButtonText: t('submit.confirm')
  })
}

// 页面加载时查询稿件状态
queryPaperStatus()
</script>

<style scoped>
.submit-wrapper {
  width: 100%;
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
  margin-bottom: 16px;
  text-align: center;
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
.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.status-title {
  font-size: 18px;
  font-weight: 700;
  color: #114C9A;
  margin: 0;
}
</style>