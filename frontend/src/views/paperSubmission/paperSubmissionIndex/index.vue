<template>
  <div class="app-container">
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="title" label="论文标题">
          <el-input v-model="queryParams.title" placeholder="请输入论文标题" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="author" label="第一作者">
          <el-input v-model="queryParams.author" placeholder="请输入第一作者" clearable @keyup.enter="handleQuery" />
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
          <el-button type="success" icon="plus" @click="handleCreateClick()">论文投稿</el-button>
          <el-button type="danger" :disabled="ids.length === 0" icon="delete" @click="handleBatchDelete()">删除</el-button>
        </div>
      </div>

      <el-table ref="dataTableRef" v-loading="loading" :data="tableData" highlight-current-row border class="table-section__content" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="论文标题" prop="title" min-width="200" show-overflow-tooltip />
        <el-table-column label="第一作者" prop="author" min-width="120" />
        <el-table-column label="联系电话" prop="phone" min-width="120" />
        <el-table-column label="联系邮箱" prop="email" min-width="180" show-overflow-tooltip />
        <el-table-column label="所属单位" prop="unit" min-width="180" show-overflow-tooltip />
        <el-table-column label="论文摘要" prop="summary" min-width="250" show-overflow-tooltip />
        <el-table-column label="关键词" prop="keywords" min-width="150" show-overflow-tooltip />
        <el-table-column label="论文附件" align="center" width="100">
          <template #default="scope">
            <el-button v-if="scope.row.fileUrl" type="primary" link>查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="投稿状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">通过</el-tag>
            <el-tag v-else type="danger">拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" link icon="edit" @click="handleEditClick(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" link icon="delete" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-if="total > 0" v-model:total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="fetchList" />
    </el-card>

    <el-dialog v-model="dialogState.visible" :title="dialogState.title" width="700px" @close="closeDialog">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="论文标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入论文标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="第一作者" prop="author">
              <el-input v-model="formData.author" placeholder="请输入第一作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属单位" prop="unit">
              <el-input v-model="formData.unit" placeholder="请输入所属单位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="关键词" prop="keywords">
              <el-input v-model="formData.keywords" placeholder="请输入关键词，用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="论文摘要" prop="summary">
              <el-input v-model="formData.summary" type="textarea" :rows="4" placeholder="请输入论文摘要" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="论文附件" prop="fileUrl">
              <el-upload :auto-upload="false" :limit="1">
                <el-button type="primary">上传论文</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="投稿状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="0">待审核</el-radio>
                <el-radio :value="1">通过</el-radio>
                <el-radio :value="2">拒绝</el-radio>
              </el-radio-group>
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
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const total = ref(0);
const ids = ref<string[]>([]);
const tableData = ref<any[]>([]);
const queryFormRef = ref();
const formRef = ref<any>();

const queryParams = reactive({ pageNum: 1, pageSize: 10, title: "", author: "" });

const dialogState = reactive({ visible: false, title: "" });

const formData = reactive<any>({
  id: undefined,
  title: "",
  author: "",
  phone: "",
  email: "",
  unit: "",
  summary: "",
  keywords: "",
  fileUrl: "",
  status: 0,
});

const rules = {
  title: [{ required: true, message: "请输入论文标题", trigger: "blur" }],
  author: [{ required: true, message: "请输入第一作者", trigger: "blur" }],
  phone: [{ required: true, message: "请输入联系电话", trigger: "blur" }],
  email: [
    { required: true, message: "请输入联系邮箱", trigger: "blur" },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  unit: [{ required: true, message: "请输入所属单位", trigger: "blur" }],
};

const fetchList = async () => {
  loading.value = true;
  setTimeout(() => { tableData.value = []; total.value = 0; loading.value = false; }, 300);
};

const handleQuery = () => { queryParams.pageNum = 1; fetchList(); };
const handleResetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleSelectionChange = (selection: any[]) => { ids.value = selection.map((item) => item.id); };

const handleCreateClick = () => {
  dialogState.title = "论文投稿";
  dialogState.visible = true;
  Object.assign(formData, { id: undefined, title: "", author: "", phone: "", email: "", unit: "", summary: "", keywords: "", fileUrl: "", status: 0 });
};

const handleEditClick = (row: any) => {
  dialogState.title = "编辑";
  dialogState.visible = true;
  Object.assign(formData, row);
};

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm("确认删除该数据?", "警告", { type: "warning" });
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) { console.log(e); }
};

const handleBatchDelete = async () => {
  if (ids.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 条数据?`, "警告", { type: "warning" });
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) { console.log(e); }
};

const submitForm = async () => {
  try {
    await formRef.value?.validate();
    ElMessage.success("保存成功");
    closeDialog();
    fetchList();
  } catch (e: any) { if (e !== false) console.error(e); }
};

const closeDialog = () => { dialogState.visible = false; formRef.value?.resetFields(); };

fetchList();
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-section { margin-bottom: 16px; }
.table-section { margin-top: 16px; }
</style>
