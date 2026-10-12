<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="name" label="姓名">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入姓名"
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
        :data="tableData"
        highlight-current-row
        border
        class="table-section__content"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="图片" align="center" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.imageHeader"
              :src="scope.row.imageHeader"
              :preview-src-list="[scope.row.imageHeader]"
              :preview-teleported="true"
              fit="cover"
              style="width: 60px; height: 60px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="姓名" prop="name" min-width="120" show-overflow-tooltip />
        <el-table-column label="职位" prop="job" min-width="150" show-overflow-tooltip />
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">
              中文
            </el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">
              英文
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" width="80" prop="sort" />

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

    <!-- 表单弹窗 -->
    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="600px"
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="图片" prop="imageHeader" class="image-upload-item">
              <el-upload
                :file-list="imageList"
                class="avatar-uploader"
                :limit="1"
                :auto-upload="false"
                :on-change="handleImageChange"
                :on-remove="handleRemove"
                :disabled="isUploading"
                accept="image/*"
              >
                <div v-if="isUploading" class="uploading-mask">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>上传中...</span>
                </div>
                <img v-else-if="formData.imageHeader" :src="formData.imageHeader" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <el-icon
                v-if="formData.imageHeader && !isUploading"
                class="delete-icon"
                @click.stop="handleRemove"
              >
                <Close />
              </el-icon>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="职位" prop="job">
              <el-input v-model="formData.job" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="语言类型" prop="countryType">
              <el-select v-model="formData.countryType" placeholder="请选择语言类型">
                <el-option label="中文" value="ZH" />
                <el-option label="英文" value="EN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="formData.sort" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Loading, Plus, Close } from "@element-plus/icons-vue";
import FileAPI from "@/api/file";
import ArchitectureAPI from "@/api/portal/architecture";

const loading = ref(false);
const total = ref(0);
const ids = ref<string[]>([]);
const tableData = ref<any[]>([]);
const queryFormRef = ref();
const formRef = ref<any>();
const isUploading = ref(false);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: "",
  countryType: "ZH",
});

const dialogState = reactive({
  visible: false,
  title: "",
});

const formData = reactive<any>({
  id: undefined,
  imageHeader: "",
  name: "",
  job: "",
  countryType: "ZH",
  status: 1,
  sort: 0,
});

const imageList = ref<any[]>([]);

const rules = computed(() => ({
  imageHeader: [{ required: true, message: "请上传图片", trigger: "change" }],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  job: [{ required: true, message: "请输入职位", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
}));

const handleQuery = () => {
  queryParams.pageNum = 1;
  fetchList();
};

const handleResetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item: any) => item.id);
};

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
      formData.imageHeader = imageUrl;
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
  formData.imageHeader = "";
  imageList.value = [];
}

const fetchList = async () => {
  loading.value = true;
  try {
    const data = await ArchitectureAPI.getPage(queryParams);
    tableData.value = data.list || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleCreateClick = async () => {
  dialogState.title = "新增";
  dialogState.visible = true;
  imageList.value = [];
  Object.assign(formData, {
    id: undefined,
    imageHeader: "",
    name: "",
    job: "",
    countryType: "ZH",
    status: 1,
    sort: 0,
  });
};

const handleEditClick = async (row: any) => {
  dialogState.title = "编辑";
  dialogState.visible = true;

  Object.assign(formData, { ...row });
  if (row.imageHeader) {
    imageList.value = [{ name: "", url: row.imageHeader }];
  } else {
    imageList.value = [];
  }
};

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm("确认删除该数据?", "警告", {
      type: "warning",
    });
    await ArchitectureAPI.deleteByIds(id);
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.log(e);
  }
};

const handleBatchDelete = async () => {
  if (ids.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 条数据?`, "警告", {
      type: "warning",
    });
    await ArchitectureAPI.deleteByIds(ids.value.join(","));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.log(e);
  }
};

const submitForm = async () => {
  try {
    const valid = await formRef.value?.validate();
    const submitData = JSON.parse(JSON.stringify(formData));
    if (formData.id) {
      await ArchitectureAPI.update(formData.id, submitData);
    } else {
      await ArchitectureAPI.create(submitData);
    }
    ElMessage.success("保存成功");
    closeDialog();
    fetchList();
  } catch (e: any) {
    if (e === false) {
      return;
    }
    console.error("提交失败:", e);
    ElMessage.error(e.message || "保存失败");
  }
};

const closeDialog = () => {
  dialogState.visible = false;
  formRef.value?.resetFields();
  imageList.value = [];
  isUploading.value = false;
};

fetchList();
</script>

<style scoped>
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #8c939d;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.image-upload-item {
  position: relative;
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
.delete-icon {
  position: absolute;
  top: -6px;
  right: -6px;
  z-index: 1;
  font-size: 14px;
  color: var(--el-color-danger);
  cursor: pointer;
}
</style>
