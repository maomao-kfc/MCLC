<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="keywords" label="标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="countryType" label="语言类型">
          <el-select v-model="queryParams.countryType" placeholder="全部" style="width: 120px">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
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
        :data="roleList"
        highlight-current-row
        border
        class="table-section__content"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="问题/答案" prop="questionDetail" min-width="250">
          <template #default="scope">
            <el-tooltip
              v-if="isTextOverflow(scope.row.questionDetail)"
              :content="getPlainText(scope.row.questionDetail)"
              placement="top"
              :enterable="false"
              effect="light"
            >
              <div class="text-content">{{ getPlainText(scope.row.questionDetail) }}</div>
            </el-tooltip>
            <div v-else class="text-content">{{ getPlainText(scope.row.questionDetail) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">中文</el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">英文</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="排序" align="center" width="80" prop="sort" />

        <el-table-column fixed="right" label="操作" width="220">
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

    <!-- 角色表单弹窗 -->
    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="900px"
      @close="closeDialog"
    >
      <el-form ref="roleFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="问题/答案" prop="questionDetail">
          <QuillEditor
            ref="quillEditorRef"
            v-model:content="formData.questionDetail"
            content-type="html"
            theme="snow"
            :toolbar="[
              ['bold', 'italic', 'underline', 'strike'],
              ['blockquote', 'code-block'],
              [{ header: [1, 2, 3, false] }],
              [{ list: 'ordered' }, { list: 'bullet' }],
              [{ indent: '-1' }, { indent: '+1' }],
              [{ direction: 'rtl' }],
              [{ color: [] }, { background: [] }],
              [{ font: [] }, { align: [] }],
              ['clean'],
            ]"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>

        <el-form-item label="语言类型" prop="countryType">
          <el-select v-model="formData.countryType" placeholder="请选择语言类型">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            controls-position="right"
            :min="0"
            style="width: 100px"
          />
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
import AnswerAPI from "@/api/portal/answer";
import type { GuestItem, GuestQueryParams } from "@/types/api";

defineOptions({
  name: "Role",
  inheritAttrs: false,
});

const queryFormRef = ref();
const roleFormRef = ref();
const quillEditorRef = ref();

const loading = ref(false);
const submitLoading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<GuestQueryParams>({
  pageNum: 1,
  pageSize: 10,
  title: "",
  countryType: "ZH",
});

// 特邀嘉宾表格数据
const roleList = ref<GuestItem[]>();

// 弹窗
const dialogState = reactive({
  title: "",
  visible: false,
});

// 角色表单
const formData = reactive({
  title: "",
  questionDetail: "",
  remark: "",
  sort: 1,
  status: 1,
  countryType: "",
});

const rules = reactive({
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  questionDetail: [{ required: true, message: "请输入问题/答案", trigger: "blur" }],
});

/**
 * 加载角色列表数据
 */
async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const data = await AnswerAPI.getPage(queryParams);
    roleList.value = data.list;
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
}

// 查询（重置页码后获取数据）
function handleQuery(): void {
  queryParams.pageNum = 1;
  fetchList();
}

/**
 * 重置查询条件
 */
function resetQuery(): void {
  queryParams.title = "";
  queryParams.countryType = "ZH";
  queryFormRef.value?.resetFields();
}

/**
 * 重置查询条件并重新查询
 */
function handleResetQuery(): void {
  resetQuery();
  handleQuery();
}

// 行复选框选中
function handleSelectionChange(selection: any): void {
  ids.value = selection.map((item: any) => item.id);
}

/**
 * 打开表单弹窗
 */
function openDialog(): void {
  dialogState.visible = true;
}

/**
 * 关闭表单弹窗
 */
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
  const plainText = getPlainText(text);
  return plainText.length > 50;
}

/**
 * 重置表单数据和验证状态
 */
function resetForm(): void {
  roleFormRef.value?.resetFields();
  roleFormRef.value?.clearValidate();

  formData.id = undefined;
  formData.title = "";
  formData.questionDetail = "";
  formData.remark = "";
  formData.sort = 1;
  formData.status = 1;
  formData.countryType = "";

  nextTick(() => {
    if (quillEditorRef.value) {
      quillEditorRef.value.setContents([]);
    }
  });
}

/**
 * 新增按钮点击事件
 */
async function handleCreateClick(): Promise<void> {
  resetForm();
  dialogState.title = "新增问题解答";
  openDialog();
}

/**
 * 编辑按钮点击事件
 * @param row 行数据
 */
async function handleEditClick(row: any): Promise<void> {
  dialogState.title = "修改问题解答";
  Object.assign(formData, row);
  openDialog();
}

// 提交角色表单
async function handleSubmit(): Promise<void> {
  const valid = await roleFormRef.value?.validate().then(
    () => true,
    () => false
  );
  if (!valid) return;

  const submitData = { ...formData };

  submitLoading.value = true;
  try {
    const roleId = formData.id;
    if (roleId) {
      await AnswerAPI.update(roleId, submitData);
      ElMessage.success("修改成功");
    } else {
      await AnswerAPI.create(submitData);
      ElMessage.success("新增成功");
    }
    closeDialog();
    handleResetQuery();
  } finally {
    submitLoading.value = false;
  }
}

// 删除角色
function handleDelete(roleId?: number): void {
  const roleIds = roleId ? String(roleId) : ids.value.join(",");
  if (!roleIds) {
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
      AnswerAPI.deleteByIds(roleIds)
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

/**
 * 批量删除按钮点击事件
 */
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
.text-content {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
}
</style>
