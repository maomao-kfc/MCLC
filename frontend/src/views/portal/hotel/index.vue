<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="hotelName" label="酒店名称">
          <el-input
            v-model="queryParams.hotelName"
            placeholder="请输入酒店名称"
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
        <el-table-column label="酒店名称" prop="hotelName" min-width="80" show-overflow-tooltip />
        <el-table-column label="酒店电话" align="center" min-width="60" prop="hotelPhone" />
        <el-table-column label="酒店联系人" align="center" width="120" prop="hotelContact" />
        <el-table-column label="酒店地址" align="center" min-width="80" prop="hotelAddress" />
        <el-table-column label="订购链接" align="center" min-width="80" prop="hotelUrl" />
        <el-table-column label="酒店图片" align="center" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.hotelImage"
              :src="scope.row.hotelImage"
              :preview-src-list="[scope.row.hotelImage]"
              :preview-teleported="true"
              fit="cover"
              style="width: 60px; height: 60px"
            />
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
      width="600px"
      @close="closeDialog"
    >
      <el-form ref="roleFormRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="酒店名称" prop="hotelName">
          <el-input v-model="formData.hotelName" placeholder="请输入酒店名称" />
        </el-form-item>

        <el-form-item label="酒店电话" prop="hotelPhone">
          <el-input v-model="formData.hotelPhone" placeholder="请输入酒店电话" />
        </el-form-item>

        <el-form-item label="酒店联系人" prop="hotelContact">
          <el-input v-model="formData.hotelContact" placeholder="请输入酒店联系人" />
        </el-form-item>

        <el-form-item label="酒店地址" prop="hotelAddress">
          <el-input v-model="formData.hotelAddress" placeholder="请输入酒店地址" />
        </el-form-item>

        <el-form-item label="订购链接" >
          <el-input v-model="formData.hotelUrl" placeholder="请输入酒店链接地址" />
        </el-form-item>

        <el-form-item label="酒店图片" prop="hotelImage">
          <el-upload
            :file-list="imageList"
            class="avatar-uploader"
            :limit="1"
            :auto-upload="false"
            :on-change="handleImageChange"
            :disabled="isUploading"
            accept="image/*"
          >
            <div v-if="isUploading" class="uploading-mask">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
            <img
              v-else-if="formData.hotelImage"
              :src="formData.hotelImage"
              class="avatar"
              @click.stop="handleRemove"
            />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
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
import { useAppStore } from "@/store/modules/app";
import { DeviceEnum } from "@/enums/settings";
import { Plus, Loading } from "@element-plus/icons-vue";

import HotelAPI from "@/api/portal/hotel";
import FileAPI from "@/api/file";
import type { GuestItem, GuestQueryParams } from "@/types/api";

defineOptions({
  name: "Role",
  inheritAttrs: false,
});

const appStore = useAppStore();

const queryFormRef = ref();
const roleFormRef = ref();
const permTreeRef = ref();

const loading = ref(false);
const submitLoading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<GuestQueryParams>({
  pageNum: 1,
  pageSize: 10,
  countryType: "ZH",
});

// 特邀嘉宾表格数据
const roleList = ref<GuestItem[]>();

// 弹窗
const dialogState = reactive({
  title: "",
  visible: false,
});

const drawerSize = computed(() => (appStore.device === DeviceEnum.DESKTOP ? "600px" : "90%"));

// 角色表单
const formData = reactive({
  hotelName: "",
  hotelPhone: "",
  hotelContact: "",
  sort: 1,
  status: 1,
  hotelImage: "",
  hotelAddress: "",
  hotelUrl: "",
  countryType: "ZH",
});

const rules = reactive({
  hotelName: [{ required: true, message: "请输入酒店名称", trigger: "blur" }],
  hotelPhone: [{ required: true, message: "请输入酒店电话", trigger: "blur" }],
  hotelContact: [{ required: true, message: "请输入酒店联系人", trigger: "blur" }],
  hotelImage: [{ required: true, message: "请上传酒店图片", trigger: "change" }],
  hotelAddress: [{ required: true, message: "请输入酒店地址", trigger: "change" }],

});

const imageList = ref<any[]>([]);
const isUploading = ref(false);

async function handleImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (file) {
    try {
      isUploading.value = true;
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      formData.hotelImage = imageUrl;
      imageList.value = [{ name: file.name, url: imageUrl }];
    } catch {
      ElMessage.error("上传失败");
      imageList.value = [];
    } finally {
      isUploading.value = false;
    }
  }
}

function handleRemove() {
  formData.hotelImage = "";
  imageList.value = [];
}

// 选中的角色
interface CheckedRole {
  id?: string;
  name?: string;
}
const checkedRole = ref<CheckedRole>({});
const assignPermDialogVisible = ref(false);

const permKeywords = ref("");
const isExpanded = ref(true);

const parentChildLinked = ref(true);

/**
 * 加载角色列表数据
 */
async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const data = await HotelAPI.getPage(queryParams);
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
  formData.hotelName = "";
  formData.hotelPhone = "";
  formData.hotelContact = "";
  formData.sort = 1;
  formData.status = 1;
  formData.hotelImage = "";
  formData.countryType = "ZH";
  imageList.value = [];
  isUploading.value = false;
}

/**
 * 新增按钮点击事件
 */
async function handleCreateClick(): Promise<void> {
  resetForm();
  dialogState.title = "新增酒店";
  openDialog();
}

/**
 * 编辑按钮点击事件
 * @param roleId 角色ID
 */
async function handleEditClick(row: any): Promise<void> {
  dialogState.title = "修改酒店";
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
      await HotelAPI.update(roleId, submitData);
      ElMessage.success("修改成功");
    } else {
      await HotelAPI.create(submitData);
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
      HotelAPI.deleteByIds(roleIds)
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

// 打开分配菜单权限弹窗
async function handleAssignPermClick(row: GuestItem): Promise<void> {
  const roleId = row.id;
  if (roleId) {
    assignPermDialogVisible.value = true;
    loading.value = true;

    checkedRole.value.id = roleId;
    checkedRole.value.name = row.name;

    // 回显角色已拥有的菜单
    HotelAPI.getRoleMenuIds(roleId)
      .then((data) => {
        const checkedMenuIds = data;
        checkedMenuIds.forEach((menuId) => permTreeRef.value!.setChecked(menuId, true, false));
      })
      .finally(() => {
        loading.value = false;
      });
  }
}

// 分配菜单权限提交
function handleAssignPermSubmit() {
  const roleId = checkedRole.value.id;
  if (roleId) {
    const checkedMenuIds: number[] = permTreeRef
      .value!.getCheckedNodes(false, true)
      .map((node: any) => node.value);

    loading.value = true;
    HotelAPI.updateRoleMenus(roleId, checkedMenuIds)
      .then(() => {
        ElMessage.success("分配权限成功");
        assignPermDialogVisible.value = false;
        handleResetQuery();
      })
      .finally(() => {
        loading.value = false;
      });
  }
}

// 展开/收缩 菜单权限树
function togglePermTree(): void {
  isExpanded.value = !isExpanded.value;
  if (permTreeRef.value) {
    Object.values(permTreeRef.value.store.nodesMap).forEach((node: any) => {
      if (isExpanded.value) {
        node.expand();
      } else {
        node.collapse();
      }
    });
  }
}

// 权限筛选
watch(permKeywords, (val) => {
  permTreeRef.value!.filter(val);
});

function handlePermFilter(
  value: string,
  data: {
    [key: string]: any;
  }
) {
  if (!value) return true;
  return data.label.includes(value);
}

// 父子菜单节点是否联动
function handleParentChildLinkedChange(val: any): void {
  parentChildLinked.value = val;
}

onMounted(() => {
  handleQuery();
});
</script>

<style scoped>
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
:deep(.avatar-uploader .el-upload) {
  width: 100% !important;
  height: 100% !important;
}
.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.uploading-mask {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--el-color-primary);
}
.is-loading {
  font-size: 20px;
  animation: rotation 1s linear infinite;
}
@keyframes rotation {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
