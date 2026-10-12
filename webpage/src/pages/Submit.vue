<!-- 投稿页面 -->
<!-- 页面结构分为三大模块：1.投稿须知 2.投稿表单 3.稿件状态查询 -->

<template>
  <div class="submit-wrapper">
    <!-- 页面标题 -->
    <h2 class="page-title">{{ t('submit.pageTitle') }}</h2>

    <!-- 模块一：投稿须知 -->
    <!-- 用户必须勾选"同意须知"后，下方表单才可编辑 -->
    <div class="common-card notice-card">
      <h3 class="notice-title">{{ t('submit.noticeTitle') }}</h3>
      <!-- v-html 渲染富文本须知内容（支持换行、链接等 HTML 格式） -->
      <div class="notice-content" v-html="t('submit.noticeContent')"></div>
      
      <!-- 同意须知勾选框：isAgreeNotice 控制表单的禁用/启用状态 -->
      <div class="agree-check">
        <el-checkbox v-model="isAgreeNotice">
          {{ t('submit.agreeNotice') }}
        </el-checkbox>
      </div>
    </div>

    <!-- 模块二：投稿表单 -->
    <!-- form-disabled 类：未勾选同意时，表单区域半透明且不可交互 -->
    <div class="common-card form-card" :class="{ 'form-disabled': !isAgreeNotice }">
      <!-- 
        el-form 属性说明：
        :model="submitForm"   — 绑定表单数据对象
        :rules="submitRules"  — 绑定校验规则（computed 实现国际化响应式）
        ref="submitFormRef"   — 模板引用，用于调用 validate() 等方法
        :disabled="!isAgreeNotice" — 未勾选同意时禁用所有表单项
      -->
      <el-form 
        :model="submitForm" 
        :rules="submitRules" 
        ref="submitFormRef" 
        label-width="120px"
        size="large"
        :disabled="!isAgreeNotice"
      >
        <!-- 论文标题：仅限英文输入，watch自动过滤中文字符 -->
        <el-form-item :label="t('submit.form.paperTitle')" prop="paperTitle">
          <el-input 
            v-model="submitForm.paperTitle" 
            :placeholder="t('submit.form.paperTitlePlaceholder')"
          />
        </el-form-item>

        <!-- 第一作者：仅限英文输入，watch自动过滤中文字符 -->
        <el-form-item :label="t('submit.form.firstAuthor')" prop="firstAuthor">
          <el-input 
            v-model="submitForm.firstAuthor" 
            :placeholder="t('submit.form.firstAuthorPlaceholder')"
          />
        </el-form-item>

        <!-- 作者电话：校验中国大陆手机号格式 -->
        <el-form-item :label="t('submit.form.authorPhone')" prop="authorPhone">
          <el-input 
            v-model="submitForm.authorPhone" 
            :placeholder="t('submit.form.authorPhonePlaceholder')"
          />
        </el-form-item>

        <!-- 作者邮箱：校验邮箱格式 -->
        <el-form-item :label="t('submit.form.authorEmail')" prop="authorEmail">
          <el-input 
            v-model="submitForm.authorEmail" 
            :placeholder="t('submit.form.authorEmailPlaceholder')"
          />
        </el-form-item>

        <!-- 确认作者邮箱 -->
        <el-form-item :label="t('submit.form.confirmContactEmail')" prop="confirmAuthorEmail">
          <el-input 
            v-model="submitForm.confirmAuthorEmail" 
            :placeholder="t('submit.form.confirmContactEmailPlaceholder')"
          />
        </el-form-item>

        <!-- 所属单位：仅限英文输入，watch自动过滤中文字符 -->
        <el-form-item :label="t('submit.form.company')" prop="company">
          <el-input 
            v-model="submitForm.company" 
            :placeholder="t('submit.form.companyPlaceholder')"
          />
        </el-form-item>

        <!-- 
          论文摘要（核心改动区域）：
          - 移除了原来的 maxlength="500"（字符限制），改为单词数验证
          - textarea 下方显示实时单词数统计 "X / 500 词"
          - 超过500词时，数字变红（word-count-exceed 类）
          - 失焦时触发自定义 validator 校验单词数
        -->
        <el-form-item :label="t('submit.form.abstract')" prop="paperAbstract">
          <el-input 
            v-model="submitForm.paperAbstract" 
            type="textarea" 
            :rows="6"
            :placeholder="t('submit.form.abstractPlaceholder')"
            @input="handleAbstractInput"
          />
          <!-- 实时单词数显示 -->
          <div class="word-count" :class="{ 'word-count-exceed': abstractWordCount > MAX_ABSTRACT_WORDS }">
            {{ abstractWordCount }} / {{ MAX_ABSTRACT_WORDS }} {{ t('submit.form.wordUnit') }}
          </div>
        </el-form-item>

        <!-- 关键词：多个关键词用逗号分隔，watch自动过滤中文字符 -->
        <el-form-item :label="t('submit.form.keywords')" prop="keywords">
          <el-input 
            v-model="submitForm.keywords" 
            :placeholder="t('submit.form.keywordsPlaceholder')"
          />
          <div class="form-tips">{{ t('submit.form.keywordsTips') }}</div>
        </el-form-item>

        <!-- 
          论文文件上传：
          - action="#" 占位（实际不上传到服务器地址）
          - :auto-upload="false" 禁止自动上传，由 handleSubmit 手动提交
          - :limit="1" 只允许上传1个文件
          - accept=".pdf,.docx" 限制文件类型
          - beforeFileUpload: 上传前校验（类型+大小）
          - handleFileChange: 文件选择后保存到表单
        -->
        <el-form-item :label="t('submit.form.paperFile')" prop="paperFile" :required="false">
          <el-upload
            class="upload-demo"
            action="#"
            :before-upload="beforeFileUpload"
            :on-change="handleFileChange"
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.docx"
          >
            <el-button type="primary">{{ t('submit.form.uploadBtn') }}</el-button>
            <template #tip>
              <div class="el-upload__tip">
                {{ t('submit.form.uploadTips') }}
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 提交按钮：未勾选同意须知时禁用 -->
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

    <!-- 模块三：稿件状态查询 -->
    <!-- 投稿者通过邮箱+验证码查询自己的投稿记录，支持查看详情和修改待审核稿件 -->
    <div class="common-card status-card">
      <h3 class="status-title">{{ t('submit.statusTitle') }}</h3>

      <!-- 查询表单：邮箱 + 验证码 -->
      <div class="query-form">
        <el-form :inline="true" :model="queryForm" size="large">
          <el-form-item :label="t('submit.query.email')">
            <el-input
              v-model="queryForm.email"
              :placeholder="t('submit.query.emailPlaceholder')"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item :label="t('submit.query.code')">
            <div class="code-input-wrapper">
              <el-input
                v-model="queryForm.code"
                :placeholder="t('submit.query.codePlaceholder')"
                style="width: 140px"
              />
              <el-button
                type="primary"
                plain
                :disabled="codeCooldown > 0 || !queryForm.email"
                @click="handleSendCode"
                :loading="sendCodeLoading"
              >
                {{ codeCooldown > 0 ? `${codeCooldown}s` : t('submit.query.sendCode') }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQueryPapers" :loading="queryLoading">
              {{ t('submit.query.queryBtn') }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 查询结果表格 -->
      <el-table :data="paperList" v-loading="queryLoading" style="width: 100%" empty-text="">
        <el-table-column prop="paperTitle" :label="t('submit.table.title')" min-width="200" />
        <el-table-column prop="firstAuthor" :label="t('submit.table.firstAuthor')" width="120" />
        <el-table-column prop="paperStatus" :label="t('submit.table.status')" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.paperStatus)">
              {{ getStatusText(scope.row.paperStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('submit.table.operation')" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="viewPaperDetail(scope.row)">
              {{ t('submit.table.view') }}
            </el-button>
            <el-button
              link
              type="warning"
              @click="editPaper(scope.row)"
              v-if="scope.row.paperStatus === 1"
            >
              {{ t('submit.table.edit') }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-tip" v-if="hasQueried">{{ t('submit.query.emptyTip') }}</div>
          <div class="empty-tip" v-else>{{ t('submit.query.pleaseQuery') }}</div>
        </template>
      </el-table>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="t('submit.detailTitle')"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="1" border size="large" v-if="detailData">
        <el-descriptions-item :label="t('submit.detail.paperTitle')">{{ detailData.paperTitle }}</el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.firstAuthor')">{{ detailData.firstAuthor }}</el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.contactNumber')">{{ detailData.contactNumber }}</el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.contactEmail')">{{ detailData.contactEmail }}</el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.abstract')">{{ detailData.paperAbstract }}</el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.keywords')">{{ detailData.keywords }}</el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.status')">
          <el-tag :type="getStatusType(detailData.paperStatus)">{{ getStatusText(detailData.paperStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('submit.detail.rejectReason')" v-if="detailData.paperStatus === 3">
          <span style="color: #f56c6c">{{ detailData.rejectReason || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">{{ t('submit.detail.close') }}</el-button>
        <el-button
          type="primary"
          @click="editFromDetail"
          v-if="detailData && detailData.paperStatus === 1"
        >
          {{ t('submit.detail.editBtn') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="t('submit.editTitle')"
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
        <el-form-item :label="t('submit.form.paperTitle')" prop="paperTitle">
          <el-input v-model="editForm.paperTitle" :placeholder="t('submit.form.paperTitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('submit.form.firstAuthor')" prop="firstAuthor">
          <el-input v-model="editForm.firstAuthor" :placeholder="t('submit.form.firstAuthorPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('submit.form.authorPhone')" prop="contactNumber">
          <el-input v-model="editForm.contactNumber" :placeholder="t('submit.form.authorPhonePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('submit.form.abstract')" prop="paperAbstract">
          <el-input v-model="editForm.paperAbstract" type="textarea" :rows="4" :placeholder="t('submit.form.abstractPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('submit.form.keywords')" prop="keywords">
          <el-input v-model="editForm.keywords" :placeholder="t('submit.form.keywordsPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('submit.form.paperFile')" prop="appendix">
          <el-input v-model="editForm.appendix" :placeholder="t('submit.edit.appendixPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">{{ t('submit.edit.cancel') }}</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">{{ t('submit.edit.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
// 引入投稿接口
import { submitPaper, sendQueryCode, queryPapersByEmail, getPaperDetail, updatePaperBySubmitter } from '@/api/submit.js'

const { t } = useI18n()

/**
 * 单词计数函数（参考 Microsoft Word 规则）
 * 
 * 计数逻辑：
 * 1. 中文：每个汉字算 1 个词（正则 /[\u4e00-\u9fa5]/g 匹配 CJK 汉字）
 * 2. 英文：用正则匹配"单词"，每个匹配项算 1 个词
 *    正则：/[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:'[A-Za-z]+)?/g
 *    - don't → 1 词（撇号连接）
 *    - e-mail → 1 词（连字符连接）
 *    - 2024 → 1 词（纯数字）
 * 
 * 示例：countWords("hello word") → 2
 */
function countWords(text) {
  if (!text || !text.trim()) return 0
  const chineseChars = (text.match(/[\u4e00-\u9fa5]/g) || []).length
  const wordPattern = /[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:'[A-Za-z]+)?/g
  const englishWords = (text.match(wordPattern) || []).length
  return chineseChars + englishWords
}

// 1. 须知勾选状态（控制表单是否可编辑：未勾选时表单置灰且不可交互）
const isAgreeNotice = ref(false)

// 2. 表单相关
const submitFormRef = ref()
// 表单数据对象，字段与后端 PaperSubmission 实体对应
const submitForm = reactive({
  paperTitle: '',       // 论文标题
  firstAuthor: '',      // 第一作者
  authorPhone: '',      // 作者电话
  authorEmail: '',      // 作者邮箱
  confirmAuthorEmail: '',  // 确认作者邮箱
  company: '',          // 所属单位/医院
  paperAbstract: '',    // 论文摘要（需进行单词数验证，不超过500词）
  keywords: '',         // 关键词（多个用逗号分隔）
  paperFile: null       // 上传的论文文件（File 对象）
})

// ==================== 全英文输入过滤 ====================
//
// 功能说明：投稿页面要求全英文输入，自动过滤中文字符
// 实现方式：watch + nextTick
//   - watch 监听字段值变化，检测到中文字符时在 nextTick 中异步清除
//   - 使用 nextTick 是因为 v-model 会先更新值，需要在 DOM 更新后才能正确覆盖
//   - 这种方式比 @input 或 @update:model-value 更可靠，不会与 IME 输入法冲突
//
// 过滤字段：论文标题(paperTitle)、第一作者(firstAuthor)、所属单位(company)、关键词(keywords)
// 不过滤：联系电话(authorPhone)、联系邮箱(authorEmail) — 天然英文格式
// 不过滤：论文附件(paperFile) — 文件上传，非文本输入
// 摘要(paperAbstract)单独watch，因为还有单词数限制逻辑
//
// 正则说明：/[\u4e00-\u9fa5]/g 匹配所有 CJK 统一汉字（基本区）
// ==================== 全英文输入过滤 ====================

const CHINESE_CHAR_REGEX = /[\u4e00-\u9fa5]/g

// 需要过滤中文的字段列表（不含摘要，摘要单独watch）
const chineseFilterFields = ['paperTitle', 'firstAuthor', 'company', 'keywords']
chineseFilterFields.forEach(field => {
  watch(
    () => submitForm[field],
    (newVal) => {
      // 检测值中是否包含中文字符
      if (newVal && CHINESE_CHAR_REGEX.test(newVal)) {
        // 移除所有中文字符
        const filtered = newVal.replace(CHINESE_CHAR_REGEX, '')
        // nextTick 确保在 Vue 完成 DOM 更新后再修改值，避免与 v-model 冲突
        nextTick(() => {
          submitForm[field] = filtered
          ElMessage.warning(t('submit.form.chineseCharFiltered'))
        })
      }
    }
  )
})

// 摘要字段单独 watch（因为还有单词数限制逻辑，不能和上面合并）
watch(
  () => submitForm.paperAbstract,
  (newVal) => {
    // 检测值中是否包含中文字符
    if (newVal && CHINESE_CHAR_REGEX.test(newVal)) {
      // 移除所有中文字符
      const filtered = newVal.replace(CHINESE_CHAR_REGEX, '')
      // nextTick 确保在 Vue 完成 DOM 更新后再修改值，避免与 v-model 冲突
      nextTick(() => {
        submitForm.paperAbstract = filtered
        ElMessage.warning(t('submit.form.chineseCharFiltered'))
      })
    }
  }
)

// ==================== 摘要单词计数 ====================

// 摘要最大允许单词数
const MAX_ABSTRACT_WORDS = 500

// 摘要的实时单词数（computed 响应式，paperAbstract 变化时自动重新计算）
const abstractWordCount = computed(() => countWords(submitForm.paperAbstract))

// 记录上一次单词数未超限的有效值，用于超限时回退
const lastValidAbstract = ref('')

/**
 * 摘要输入处理：达到500词后禁止继续输入
 * 原理：记录上一次有效值，当新输入导致单词数超过500时，直接回退到有效值
 * 中文字符过滤已由 watch 处理，这里只处理单词数限制
 */
const handleAbstractInput = (value) => {
  if (countWords(value) > MAX_ABSTRACT_WORDS) {
    // 超过500词，回退到上次有效值
    if (lastValidAbstract.value) {
      submitForm.paperAbstract = lastValidAbstract.value
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
      submitForm.paperAbstract = truncated
      lastValidAbstract.value = truncated
    }
    ElMessage.warning(t('submit.form.abstractTruncated'))
  } else {
    // 未超限，更新值并记录有效值
    submitForm.paperAbstract = value
    lastValidAbstract.value = value
  }
}

// 表单校验规则（使用 computed 实现国际化响应式，切换语言时错误提示自动更新）
const submitRules = computed(() => ({
  paperTitle: [
    { required: true, message: t('submit.form.paperTitleRequired'), trigger: 'blur' }
  ],
  firstAuthor: [
    { required: true, message: t('submit.form.firstAuthorRequired'), trigger: 'blur' }
  ],
  authorPhone: [
    { required: true, message: t('submit.form.phoneRequired'), trigger: 'blur' },
    // 中国大陆手机号正则：1开头，第二位3-9，后面9位数字，共11位
    { pattern: /^1[3-9]\d{9}$/, message: t('submit.form.phoneInvalid'), trigger: 'blur' }
  ],
  authorEmail: [
    { required: true, message: t('submit.form.emailRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('submit.form.emailInvalid'), trigger: 'blur' }
  ],
  confirmAuthorEmail: [
    { required: true, message: t('submit.form.confirmEmailRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: t('submit.form.emailInvalid'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value !== submitForm.authorEmail) {
          callback(new Error(t('submit.form.confirmEmailMismatch')))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  company: [
    { required: true, message: t('submit.form.companyRequired'), trigger: 'blur' }
  ],
  paperAbstract: [
    // 必填校验
    { required: true, message: t('submit.form.abstractRequired'), trigger: 'blur' },
    // 自定义单词数校验（替代原来的 max: 500 字符限制）
    // 使用 validator 自定义校验函数，在失焦时触发
    { 
      validator: (rule, value, callback) => {
        // value 有值时才校验单词数（空值由上面的 required 规则处理）
        if (value && countWords(value) > MAX_ABSTRACT_WORDS) {
          // callback(new Error()) 表示校验失败，显示错误信息
          callback(new Error(t('submit.form.abstractWordLimit')))
        } else {
          // callback() 无参数表示校验通过
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  paperFile: []
}))

// 3. 文件上传处理
// 上传前校验：文件类型 + 文件大小
const beforeFileUpload = (file) => {
  // 提取文件扩展名（取最后一个点后的内容，转小写）
  const fileType = file.name.split('.').pop().toLowerCase()
  // 仅允许 pdf 和 docx，不支持 doc 格式（后端未实现 doc 转 pdf）
  const allowTypes = ['pdf', 'docx']
  // 类型校验：只允许 PDF 和 DOCX 文档
  if (!allowTypes.includes(fileType)) {
    ElMessage.error(t('submit.form.fileTypeError'))
    return false  // 返回 false 阻止上传
  }
  // 大小校验：不超过 10MB
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error(t('submit.form.fileSizeError'))
    return false
  }
  return true  // 返回 true 允许上传
}

// 文件选择变化时，将原生 File 对象保存到表单（因为 auto-upload=false，手动管理文件）
const handleFileChange = (file) => {
  submitForm.paperFile = file.raw  // file.raw 是 el-upload 封装后的原生 File 对象
}

// 4. 表单提交
const handleSubmit = async () => {
  // 触发表单校验，validate() 返回 Promise，校验失败会 reject
  // .catch(() => false) 捕获 reject 并返回 false，避免未处理的 Promise 异常
  const valid = await submitFormRef.value.validate().catch(() => false)
  if (!valid) return  // 校验不通过，直接返回

  try {
    // 构建 FormData（因为包含文件上传，不能用 JSON，必须用 multipart/form-data）
    const formData = new FormData()
    // 遍历表单所有字段，逐一 append 到 FormData
    Object.keys(submitForm).forEach(key => {
      formData.append(key, submitForm[key])
    })
    // 调用后端投稿接口
    await submitPaper(formData)

    ElMessage.success(t('submit.submitSuccess'))
    resetForm()
  } catch (err) {
    console.error('投稿失败：', err)
    ElMessage.error(t('submit.submitFail'))
  }
}

// 重置表单（调用 Element Plus 的 resetFields，会清空所有字段值和校验状态）
const resetForm = () => {
  submitFormRef.value?.resetFields()
  submitForm.paperFile = null  // 手动清空文件（resetFields 对 File 对象可能不生效）
}

// 5. 投稿查询相关
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
  paperTitle: '',
  firstAuthor: '',
  contactNumber: '',
  paperAbstract: '',
  keywords: '',
  appendix: ''
})
const editRules = computed(() => ({
  paperTitle: [{ required: true, message: t('submit.form.paperTitlePlaceholder'), trigger: 'blur' }],
  firstAuthor: [{ required: true, message: t('submit.form.firstAuthorPlaceholder'), trigger: 'blur' }]
}))

// 发送验证码
const handleSendCode = async () => {
  if (!queryForm.email) {
    ElMessage.warning(t('submit.query.emailPlaceholder'))
    return
  }
  try {
    sendCodeLoading.value = true
    await sendQueryCode(queryForm.email)
    ElMessage.success(t('submit.query.codeSent'))
    codeCooldown.value = 60
    const timer = setInterval(() => {
      codeCooldown.value--
      if (codeCooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error(e.message || t('submit.query.codeSendFail'))
  } finally {
    sendCodeLoading.value = false
  }
}

// 查询投稿列表
const handleQueryPapers = async () => {
  if (!queryForm.email || !queryForm.code) {
    ElMessage.warning(t('submit.query.fillRequired'))
    return
  }
  try {
    queryLoading.value = true
    hasQueried.value = true
    const res = await queryPapersByEmail(queryForm.email, queryForm.code)
    paperList.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || t('submit.query.queryFail'))
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
    ElMessage.error(e.message || t('submit.query.detailFail'))
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
    ElMessage.success(t('submit.edit.success'))
    editDialogVisible.value = false
    // 修改成功后刷新列表
    if (queryForm.email) {
      // 重新发送验证码并查询（验证码已一次性使用）
      handleSendCode()
    }
  } catch (e) {
    ElMessage.error(e.message || t('submit.edit.fail'))
  } finally {
    editLoading.value = false
  }
}

// 状态文本映射（数字状态码 → 国际化文本）
const getStatusText = (status) => {
  const map = { 1: t('submit.status.pending'), 2: t('submit.status.approved'), 3: t('submit.status.rejected') }
  return map[status] || t('submit.status.unknown')
}

// 状态标签类型映射（数字状态码 → Element Plus tag type）
const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}
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
.status-title {
  font-size: 18px;
  font-weight: 700;
  color: #114C9A;
  margin: 0 0 16px 0;
}
.query-form {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.code-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 20px 0;
}
</style>