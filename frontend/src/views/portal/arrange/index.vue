<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="arrangeRoom" label="会议室">
          <el-input
            v-model="queryParams.arrangeRoom"
            placeholder="请输入会议室"
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

        <el-form-item prop="arrangeType" label="安排类型">
          <el-select v-model="queryParams.arrangeType" placeholder="全部" style="width: 140px" clearable>
            <el-option label="标题描述" value="HEADER" />
            <el-option label="板块" value="BLOCK" />
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
        :data="arrangeList"
        highlight-current-row
        border
        class="table-section__content"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="会议室" prop="arrangeRoom" min-width="150" show-overflow-tooltip />
        <el-table-column label="会议内容" min-width="200">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.arrangeContent"
              placement="top"
              :enterable="false"
              effect="light"
            >
              <template #content>
                <div v-html="scope.row.arrangeContent"></div>
              </template>
              <div class="text-content">{{ getPlainText(scope.row.arrangeContent) }}</div>
            </el-tooltip>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">中文</el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">英文</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="安排类型" align="center" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.arrangeType === 'HEADER'" type="warning">标题描述</el-tag>
            <el-tag v-else-if="scope.row.arrangeType === 'BLOCK'" type="info">板块</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "正常" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
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

    <!-- 会前安排表单弹窗 -->
    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="600px"
      @close="closeDialog"
    >
      <el-form ref="arrangeFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="会议室" prop="arrangeRoom">
          <el-input v-model="formData.arrangeRoom" placeholder="请输入会议室" />
        </el-form-item>

        <el-form-item label="会议内容" prop="arrangeContent">
          <QuillEditor
            ref="quillEditorRef"
            v-model:content="formData.arrangeContent"
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

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>

        <el-form-item label="语言类型" prop="countryType">
          <el-select v-model="formData.countryType" placeholder="请选择语言类型">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
          </el-select>
        </el-form-item>

        <el-form-item label="安排类型" prop="arrangeType">
          <el-select v-model="formData.arrangeType" placeholder="请选择安排类型">
            <el-option label="标题描述" value="HEADER" />
            <el-option label="板块" value="BLOCK" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
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
import ArrangeAPI from "@/api/portal/arrange";
import { QuillEditor } from "@vueup/vue-quill";
import { nextTick } from "vue";
import "@vueup/vue-quill/dist/vue-quill.snow.css";

defineOptions({
  name: "Arrange",
  inheritAttrs: false,
});

const queryFormRef = ref();
const arrangeFormRef = ref();
const quillEditorRef = ref();

const loading = ref(false);
const submitLoading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  arrangeRoom: "",
  countryType: "ZH",
  arrangeType: "",
});

// 会前安排表格数据
const arrangeList = ref<any[]>([]);

// 弹窗
const dialogState = reactive({
  title: "",
  visible: false,
});

// 会前安排表单
const formData = reactive({
  id: undefined as number | undefined,
  arrangeRoom: "",
  arrangeContent: "",
  sort: 0,
  status: 1,
  countryType: "",
  arrangeType: "BLOCK",
});

const rules = reactive({
  arrangeRoom: [{ required: true, message: "请输入会议室", trigger: "blur" }],
  arrangeContent: [{ required: true, message: "请输入会议内容", trigger: "blur" }],
});

/**
 * 加载会前安排列表数据
 */
async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const data = await ArrangeAPI.getPage(queryParams);
    arrangeList.value = data.list;
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
  if (!html) return "";
  const temp = document.createElement("div");
  temp.innerHTML = html;
  return temp.textContent || "";
}

/**
 * 重置表单数据和验证状态
 */
function resetForm(): void {
  arrangeFormRef.value?.resetFields();
  arrangeFormRef.value?.clearValidate();

  formData.id = undefined;
  formData.arrangeRoom = "";
  formData.arrangeContent = "";
  formData.sort = 0;
  formData.status = 1;
  formData.countryType = "";
  formData.arrangeType = "BLOCK";

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
  dialogState.title = "新增会前安排";
  openDialog();
}

/**
 * 编辑按钮点击事件
 * @param row 行数据
 */
async function handleEditClick(row: any): Promise<void> {
  dialogState.title = "修改会前安排";
  Object.assign(formData, row);
  openDialog();
}

// 提交会前安排表单
async function handleSubmit(): Promise<void> {
  const valid = await arrangeFormRef.value?.validate().then(
    () => true,
    () => false
  );
  if (!valid) return;
  const submitData = { ...formData };
  submitLoading.value = true;
  try {
    const arrangeId = formData.id;
    if (arrangeId) {
      await ArrangeAPI.update(arrangeId, submitData);
      ElMessage.success("修改成功");
    } else {
      await ArrangeAPI.create(submitData);
      ElMessage.success("新增成功");
    }
    closeDialog();
    handleResetQuery();
  } finally {
    submitLoading.value = false;
  }
}

// 删除会前安排
function handleDelete(id?: number): void {
  const arrangeIds = id ? String(id) : ids.value.join(",");
  if (!arrangeIds) {
    ElMessage.warning("请勾选删除项");
    return;
  }

  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(
    async () => {
      loading.value = true;
      try {
        await ArrangeAPI.delete(arrangeIds);
        ElMessage.success("删除成功");
        handleResetQuery();
      } finally {
        loading.value = false;
      }
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
  max-width: 200px;
}
</style>
