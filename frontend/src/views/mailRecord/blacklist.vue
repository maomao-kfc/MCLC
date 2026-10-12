<template>
  <div class="app-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">手动添加</el-button>
      <el-button @click="handleRefresh">刷新</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column label="ID" prop="id" width="70" />
      <el-table-column label="黑名单邮箱" prop="email" min-width="220" show-overflow-tooltip />
      <el-table-column label="来源" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.source === 'AUTO' ? 'danger' : 'warning'" size="small">
            {{ scope.row.source === 'AUTO' ? '自动检测' : '手动添加' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="退信原因" prop="reason" min-width="250" show-overflow-tooltip />
      <el-table-column label="累计退信次数" prop="bounceCount" align="center" width="120" />
      <el-table-column label="最近退信时间" prop="lastBounceTime" width="170" />
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-popconfirm title="确定移除该黑名单？" @confirm="handleRemove(scope.row)">
            <template #reference>
              <el-button type="primary" size="small" link>移除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      class="pagination"
      @size-change="handleSearch"
      @current-change="handleSearch"
    />

    <!-- 添加弹窗 -->
    <el-dialog v-model="addDialog.visible" title="添加黑名单" width="500px" @close="resetAddDialog">
      <el-form ref="addFormRef" :model="addDialog" :rules="addFormRules" label-width="80px">
        <el-form-item label="邮箱地址" prop="email">
          <el-input v-model="addDialog.email" placeholder="请输入要加入黑名单的邮箱地址" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input
            v-model="addDialog.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入加入黑名单的原因（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="addDialog.submitting" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { getBlacklistList, addBlacklist, removeBlacklist } from "@/api/portal/mailBlacklist";

defineOptions({ name: "MailBlacklist" });

const loading = ref(false);
const tableData = ref<any[]>([]);
const total = ref(0);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
});

const addDialog = reactive({
  visible: false,
  email: "",
  reason: "",
  submitting: false,
});

const addFormRef = ref<FormInstance>();

const EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

const addFormRules: FormRules = {
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { pattern: EMAIL_PATTERN, message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
};

/** 查询列表 */
async function handleSearch() {
  loading.value = true;
  try {
    const res: any = await getBlacklistList(queryParams);
    tableData.value = res.list || [];
    total.value = res.total ?? 0;
  } catch (e: any) {
    ElMessage.error("查询黑名单失败：" + (e.message || "未知错误"));
  } finally {
    loading.value = false;
  }
}

/** 刷新 */
function handleRefresh() {
  queryParams.pageNum = 1;
  handleSearch();
}

/** 手动添加 */
function handleAdd() {
  addDialog.visible = true;
}

/** 提交添加 */
async function submitAdd() {
  const valid = await addFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  addDialog.submitting = true;
  try {
    await addBlacklist(addDialog.email.trim(), addDialog.reason.trim() || undefined);
    ElMessage.success("添加成功");
    addDialog.visible = false;
    resetAddDialog();
    handleSearch();
  } catch (e: any) {
    ElMessage.error("添加失败：" + (e.message || "未知错误"));
  } finally {
    addDialog.submitting = false;
  }
}

/** 移除 */
async function handleRemove(row: any) {
  try {
    await removeBlacklist(row.id);
    ElMessage.success("移除成功");
    handleSearch();
  } catch (e: any) {
    ElMessage.error("移除失败：" + (e.message || "未知错误"));
  }
}

/** 重置弹窗 */
function resetAddDialog() {
  addDialog.email = "";
  addDialog.reason = "";
}

onMounted(() => {
  handleSearch();
});

/** 页面被 keep-alive 缓存后，从菜单再次进入时自动刷新数据 */
onActivated(() => {
  handleSearch();
});
</script>

<style scoped>
.action-bar {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>