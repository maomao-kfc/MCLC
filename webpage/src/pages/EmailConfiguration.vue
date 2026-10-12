<template>
  <div class="email-config-page">
    <div class="page-header">
      <h2 class="page-title">邮箱模板配置</h2>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-input
        v-model="queryParams.keywords"
        placeholder="搜索邮箱标题..."
        clearable
        style="width: 240px; margin-right: 12px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="状态"
        clearable
        style="width: 120px; margin-right: 12px"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" style="float: right" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增模板
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%; margin-top: 16px"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="emailTitle" label="邮箱标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
      <el-table-column prop="updateTime" label="更新时间" width="170" align="center" />
      <el-table-column label="操作" width="170" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row.id)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-area">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchTableData"
        @current-change="fetchTableData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="860px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="邮箱标题" prop="emailTitle">
          <el-input v-model="formData.emailTitle" placeholder="请输入邮箱标题" />
        </el-form-item>
        <el-form-item label="邮箱内容">
          <div class="editor-wrapper">
            <Toolbar
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
              mode="default"
            />
            <Editor
              v-model="formData.emailContent"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="handleEditorCreated"
            />
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
            placeholder="请输入排序"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, shallowRef, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import {
  getEmailTemplatePage,
  getEmailTemplateForm,
  saveEmailTemplate,
  updateEmailTemplate,
  deleteEmailTemplates
} from '@/api/emailTemplate'

// ---- 表格数据 ----
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keywords: '',
  status: undefined
})

// 获取表格数据
const fetchTableData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 清除空字符串和 undefined
    Object.keys(params).forEach((key) => {
      if (params[key] === '' || params[key] === undefined) {
        delete params[key]
      }
    })
    const res = await getEmailTemplatePage(params)
    tableData.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchTableData()
}

// 重置
const handleReset = () => {
  queryParams.keywords = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  fetchTableData()
}

// ---- 弹窗表单 ----
const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const submitLoading = ref(false)
const formRef = ref()
const editingId = ref(null)

const formData = reactive({
  emailTitle: '',
  emailContent: '',
  sort: 1,
  status: 1,
  remark: ''
})

const formRules = {
  emailTitle: [{ required: true, message: '请输入邮箱标题', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 重置表单
const resetForm = () => {
  editingId.value = null
  formData.emailTitle = ''
  formData.emailContent = ''
  formData.sort = 1
  formData.status = 1
  formData.remark = ''
  formRef.value?.resetFields()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增模板'
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (id) => {
  resetForm()
  dialogTitle.value = '编辑模板'
  editingId.value = id
  dialogVisible.value = true
  try {
    const res = await getEmailTemplateForm(id)
    if (res?.data) {
      const data = res.data
      formData.emailTitle = data.emailTitle || ''
      formData.emailContent = data.emailContent || ''
      formData.sort = data.sort ?? 1
      formData.status = data.status ?? 1
      formData.remark = data.remark || ''
    }
  } catch {
    ElMessage.error('获取模板详情失败')
    dialogVisible.value = false
  }
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { ...formData }
    if (editingId.value) {
      await updateEmailTemplate(editingId.value, data)
      ElMessage.success('修改成功')
    } else {
      await saveEmailTemplate(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTableData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除模板「${row.emailTitle}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEmailTemplates(row.id)
      ElMessage.success('删除成功')
      fetchTableData()
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// ---- 富文本编辑器 ----
const editorRef = shallowRef()
const toolbarConfig = {
  excludeKeys: []
}
const editorConfig = {
  placeholder: '请输入邮箱内容...',
  MENU_CONF: {}
}

editorConfig.MENU_CONF['uploadImage'] = {
  server: '/dev-api/api/v1/files/upload'
}

const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
    editorRef.value = null
  }
})

// 初始化加载
fetchTableData()
</script>

<style scoped>
.email-config-page {
  padding: 24px 32px;
  background: #fff;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-area {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  z-index: 100;
}
</style>
