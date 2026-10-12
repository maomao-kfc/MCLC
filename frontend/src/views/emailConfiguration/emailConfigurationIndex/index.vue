<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="keywords" label="邮箱标题">
          <el-input
            v-model="queryParams.keywords"
            placeholder="请输入邮箱标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="status" label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item class="search-buttons">
          <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
          <el-button icon="refresh" @click="handleResetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="hover" class="table-section">
      <div class="table-section__toolbar">
        <div class="table-section__toolbar--actions">
          <el-button type="success" icon="plus" @click="handleCreateClick()">新增</el-button>
          <el-button
            type="danger"
            :disabled="ids.length === 0"
            icon="delete"
            @click="handleBatchDelete()"
          >
            删除
          </el-button>
        </div>
      </div>

      <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="tableData"
        highlight-current-row
        border
        class="table-section__content"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="邮箱标题" prop="emailTitle" min-width="180" show-overflow-tooltip />
        <el-table-column label="模板类型" prop="emailType" min-width="120" />
        <el-table-column label="邮箱内容" prop="emailContent" min-width="200">
          <template #default="scope">
            <el-tooltip
              v-if="isTextOverflow(scope.row.emailContent)"
              :content="getPlainText(scope.row.emailContent)"
              placement="top"
              :enterable="false"
              effect="light"
            >
              <div class="text-content">{{ getPlainText(scope.row.emailContent) }}</div>
            </el-tooltip>
            <div v-else class="text-content">{{ getPlainText(scope.row.emailContent) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" width="80" prop="sort" />
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
        <el-table-column label="创建时间" align="center" width="170" prop="createTime" />
        <el-table-column label="更新时间" align="center" width="170" prop="updateTime" />
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              link
              icon="edit"
              @click="handleEditClick(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              icon="delete"
              @click="handleDelete(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchList"
      />
    </el-card>

    <!-- 邮箱模板表单弹窗 -->
    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="900px"
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="邮箱标题" prop="emailTitle">
          <el-input v-model="formData.emailTitle" placeholder="请输入邮箱标题" />
        </el-form-item>
        <el-form-item label="模板类型" prop="emailType">
          <el-select v-model="formData.emailType" placeholder="请选择模板类型">
            <el-option label="专家审核模板" value="专家审核模板"></el-option>
            <el-option label="论文审核通过模板" value="论文审核通过模板"></el-option>
            <el-option label="论文审核未通过模板" value="论文审核未通过模板"></el-option>
            <el-option label="注册通过模板" value="注册通过模板"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱内容" prop="emailContent">
          <QuillEditor
            ref="quillEditorRef"
            v-model:content="formData.emailContent"
            content-type="html"
            theme="snow"
            :toolbar="toolbarOptions"
            @ready="onEditorReady"
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            controls-position="right"
            :min="0"
            style="width: 100px"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
          <el-button @click="closeDialog">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { QuillEditor } from "@vueup/vue-quill";
import { nextTick } from "vue";
import "@vueup/vue-quill/dist/vue-quill.snow.css";
import EmailTemplateAPI from "@/api/system/emailTemplate";
import FileAPI from "@/api/file";
import type { EmailTemplateItem, EmailTemplateQueryParams, EmailTemplateForm } from "@/types/api";

defineOptions({
  name: "EmailConfiguration",
  inheritAttrs: false,
});

let quillInstance: any = null;

const queryFormRef = ref();
const formRef = ref();
const quillEditorRef = ref();

const loading = ref(false);
const submitLoading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<EmailTemplateQueryParams>({
  pageNum: 1,
  pageSize: 10,
  keywords: "",
  status: undefined,
});

const tableData = ref<EmailTemplateItem[]>([]);

const dialogState = reactive({
  title: "",
  visible: false,
});

const formData = reactive<EmailTemplateForm>({
  emailTitle: "",
  emailType: "",
  emailContent: "",
  sort: 1,
  status: 1,
  remark: "",
  emailType: "",
});

const rules = reactive({
  emailTitle: [{ required: true, message: "请输入邮箱标题", trigger: "blur" }],
  emailContent: [{ required: true, message: "请输入邮箱内容", trigger: "blur" }],
  emailType: [{ required: true, message: "请选择模板类型", trigger: "change" }],
});

const toolbarOptions = {
  container: [
    ["bold", "italic", "underline", "strike"],
    ["blockquote", "code-block"],
    [{ header: [1, 2, 3, false] }],
    [{ list: "ordered" }, { list: "bullet" }],
    [{ indent: "-1" }, { indent: "+1" }],
    [{ direction: "rtl" }],
    [{ color: [] }, { background: [] }],
    [{ font: [] }, { align: [] }],
    ["link"],
    ["image", "pdf", "docx"],
    ["clean"],
  ],
};

const createFileInput = (accept: string) => {
  const input = document.createElement("input");
  input.setAttribute("type", "file");
  input.setAttribute("accept", accept);
  return input;
};

const handleEditorImageUpload = async (quill: any) => {
  if (!quill) return;
  const input = createFileInput("image/*");
  input.click();
  input.onchange = async () => {
    const file = input.files?.[0];
    if (!file) return;
    try {
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      const range = quill.getSelection();
      quill.insertEmbed(range.index, "image", imageUrl);
      ElMessage.success("图片上传成功");
    } catch {
      ElMessage.error("图片上传失败");
    }
    input.remove();
  };
};

const handleEditorFileUpload = async (quill: any, accept: string, label: string) => {
  if (!quill) return;
  const input = createFileInput(accept);
  input.click();
  input.onchange = async () => {
    const file = input.files?.[0];
    if (!file) return;
    try {
      const res = await FileAPI.uploadFile(file);
      const fileUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      const fileName = Array.isArray(res) ? res[0]?.name || file.name : res.name || file.name;
      if (!fileUrl) {
        throw new Error("获取文件地址失败");
      }
      const range = quill.getSelection();
      const index = range ? range.index : quill.getLength() - 1;
      quill.insertText(index, ` ${label} ${fileName} `, { link: fileUrl });
      ElMessage.success(`${label}上传成功`);
    } catch (e: any) {
      console.error("文件上传失败:", e);
      ElMessage.error(`${label}上传失败`);
    }
    input.remove();
  };
};

const onEditorReady = (quill: any) => {
  quillInstance = quill;
  const toolbar = quill.getModule("toolbar");
  toolbar.addHandler("image", () => {
    handleEditorImageUpload(quill);
  });
  // 自定义 PDF/DOCX 按钮通过 DOM 绑定（Quill addHandler 对未注册格式不生效）
  nextTick(() => {
    const toolbarEl = toolbar.container;
    const pdfBtn = toolbarEl?.querySelector(".ql-pdf");
    const docxBtn = toolbarEl?.querySelector(".ql-docx");
    if (pdfBtn) {
      pdfBtn.addEventListener("click", () => handleEditorFileUpload(quill, ".pdf", " PDF"));
    }
    if (docxBtn) {
      docxBtn.addEventListener("click", () => handleEditorFileUpload(quill, ".doc,.docx", " DOCX"));
    }
  });
};

/** 加载列表数据 */
async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const data = await EmailTemplateAPI.getPage(queryParams);
    tableData.value = data.list;
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
}

/** 查询（重置页码后获取数据） */
function handleQuery(): void {
  queryParams.pageNum = 1;
  fetchList();
}

/** 重置查询条件并重新查询 */
function handleResetQuery(): void {
  queryParams.keywords = "";
  queryParams.status = undefined;
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 行复选框选中 */
function handleSelectionChange(selection: any): void {
  ids.value = selection.map((item: any) => item.id);
}

/** 打开表单弹窗 */
function openDialog(): void {
  dialogState.visible = true;
}

/** 关闭表单弹窗 */
function closeDialog(): void {
  dialogState.visible = false;
  resetForm();
}

function getPlainText(html: string): string {
  const temp = document.createElement("div");
  temp.innerHTML = html;
  return temp.textContent || "";
}

function isTextOverflow(text: string): boolean {
  if (!text) return false;
  const plainText = getPlainText(text);
  return plainText.length > 50;
}

/** 重置表单数据和验证状态 */
function resetForm(): void {
  formRef.value?.resetFields();
  formRef.value?.clearValidate();

  formData.id = undefined;
  formData.emailTitle = "";
  formData.emailType = "";
  formData.emailContent = "";
  formData.sort = 1;
  formData.status = 1;
  formData.remark = "";

  nextTick(() => {
    if (quillEditorRef.value) {
      quillEditorRef.value.setContents([]);
    }
  });
}

/** 新增按钮点击事件 */
async function handleCreateClick(): Promise<void> {
  resetForm();
  dialogState.title = "新增邮箱模板";
  openDialog();
}

/** 编辑按钮点击事件 */
async function handleEditClick(row: EmailTemplateItem): Promise<void> {
  dialogState.title = "编辑邮箱模板";
  const emailType = row.emailType;
  if (!emailType) {
    ElMessage.warning("模板类型不存在");
    return;
  }
  try {
    const data = await EmailTemplateAPI.getFormData(emailType);
    Object.assign(formData, data);
    openDialog();
  } catch {
    ElMessage.error("获取模板详情失败");
  }
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  const valid = await formRef.value?.validate().then(
    () => true,
    () => false
  );
  if (!valid) return;

  const submitData = { ...formData };

  submitLoading.value = true;
  try {
    const templateId = formData.id;
    if (templateId) {
      await EmailTemplateAPI.update(templateId, submitData);
      ElMessage.success("修改成功");
    } else {
      await EmailTemplateAPI.create(submitData);
      ElMessage.success("新增成功");
    }
    closeDialog();
    handleResetQuery();
  } finally {
    submitLoading.value = false;
  }
}

/** 删除邮箱模板 */
function handleDelete(templateId?: number): void {
  const templateIds = templateId ? String(templateId) : ids.value.join(",");
  if (!templateIds) {
    ElMessage.warning("请勾选删除项");
    return;
  }

  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(
    () => {
      loading.value = true;
      EmailTemplateAPI.deleteByIds(templateIds)
        .then(() => {
          ElMessage.success("删除成功");
          handleResetQuery();
        })
        .finally(() => (loading.value = false));
    },
    () => {
      ElMessage.info("已取消删除");
    }
  );
}

/** 批量删除按钮点击事件 */
function handleBatchDelete(): void {
  handleDelete();
}

onMounted(() => {
  handleQuery();
});
</script>

<style scoped>
:deep(.ql-container) {
  width: 100%;
  height: inherit;
}

:deep(.ql-pdf)::before,
:deep(.ql-docx)::before {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  text-align: center;
  border-radius: 2px;
}

:deep(.ql-pdf)::before {
  content: "PDF";
  color: #e74c3c;
  background: #fde8e8;
  padding: 0 4px;
}

:deep(.ql-docx)::before {
  content: "DOC";
  color: #2980b9;
  background: #e6f2fb;
  padding: 0 4px;
}

.text-content {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
}
</style>
