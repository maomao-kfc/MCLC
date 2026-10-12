<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="title" label="标题">
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
        <el-table-column
          label="联系人姓名"
          prop="contactName"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          label="联系人电话"
          prop="contactPhone"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          label="联系人邮箱"
          prop="contactMail"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">中文</el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">英文</el-tag>
            <span v-else>-</span>
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
      width="600px"
      @close="closeDialog"
    >
      <el-form ref="roleFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="formData.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系人电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系人电话" />
        </el-form-item>

        <el-form-item label="联系人邮箱" prop="contactMail">
          <el-input v-model="formData.contactMail" placeholder="请输入联系人邮箱" />
        </el-form-item>

        <el-form-item label="语言类型" prop="countryType">
          <el-select v-model="formData.countryType" placeholder="请选择语言类型">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
          <el-button @click="closeDialog">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import ContactAPI from "@/api/portal/contact";
import type { GuestItem, GuestQueryParams } from "@/types/api";
import ArrangeAPI from "@/api/portal/arrange";

defineOptions({
  name: "Role",
  inheritAttrs: false,
});

const queryFormRef = ref();
const roleFormRef = ref();

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
  contactName: "",
  contactPhone: "",
  contactMail: "",
  countryType: "",
  sort: 0,
});

const rules = reactive({
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  contactName: [{ required: true, message: "请输入联系人姓名", trigger: "blur" }],
  contactMail: [{ required: true, message: "请输入联系人邮箱", trigger: "blur" }],
});

/**
 * 加载角色列表数据
 */
async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const data = await ContactAPI.getPage(queryParams);
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

/**
 * 重置表单数据和验证状态
 */
function resetForm(): void {
  roleFormRef.value?.resetFields();
  roleFormRef.value?.clearValidate();

  formData.id = undefined;
  formData.title = "";
  formData.contactName = "";
  formData.contactPhone = "";
  formData.contactMail = "";
  formData.countryType = "";
  formData.sort = 0;
}

/**
 * 新增按钮点击事件
 */
async function handleCreateClick(): Promise<void> {
  resetForm();
  dialogState.title = "新增联系我们";
  openDialog();
}

/**
 * 编辑按钮点击事件
 * @param row 行数据
 */
async function handleEditClick(row: any): Promise<void> {
  dialogState.title = "修改联系我们";
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
      await ContactAPI.update(roleId, submitData);
      ElMessage.success("修改成功");
    } else {
      await ContactAPI.create(submitData);
      ElMessage.success("新增成功");
    }
    closeDialog();
    handleResetQuery();
  } finally {
    submitLoading.value = false;
  }
}

// 删除角色
function handleDelete(id?: number): void {
  const roleIds = id ? String(id) : ids.value.join(",");
  if (!roleIds) {
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
      await ContactAPI.deleteByIds(roleIds);
      ElMessage.success("删除成功");
      handleResetQuery();
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

<style scoped></style>
