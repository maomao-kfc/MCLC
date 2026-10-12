<template>
  <div class="app-container">
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="checkName" label="签到人姓名">
          <el-input v-model="queryParams.checkName" placeholder="请输入签到人姓名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="checkEmail" label="签到人邮箱">
          <el-input v-model="queryParams.checkEmail" placeholder="请输入签到人邮箱" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="checkNumber" label="签到人编号">
          <el-input v-model="queryParams.checkNumber" placeholder="请输入签到人编号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="checkStatus" label="签到状态">
          <el-input v-model="queryParams.checkStatus" placeholder="请输入签到状态" clearable @keyup.enter="handleQuery" />
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
          <el-button type="danger" :disabled="ids.length === 0" icon="delete" @click="handleBatchDelete">删除</el-button>
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
        <el-table-column label="签到记录ID" prop="id" min-width="120" align="center" />
<!--        <el-table-column label="关联签到ID" prop="checkId" min-width="120" align="center" />-->
        <el-table-column label="签到时间" prop="checkTime" min-width="160" show-overflow-tooltip />
        <el-table-column label="签到人姓名" prop="checkName" min-width="120" />
        <el-table-column label="签到人手机号" prop="checkNumber" min-width="140" />
        <el-table-column label="签到人邮箱" prop="checkEmail" min-width="200" show-overflow-tooltip />
        <el-table-column label="签到状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.checkStatus" type="success">{{ scope.row.checkStatus }}</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="100" align="center">
          <template #default="scope">
            <el-button type="danger" size="small" link icon="delete" @click="handleDelete(scope.row.id)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import UserCheckInAPI from "@/api/portal/userCheckIn";
import type { UserCheckInQueryParams } from "@/types/api";

const loading = ref(false);
const total = ref(0);
const ids = ref<string[]>([]);
const tableData = ref<any[]>([]);
const queryFormRef = ref();

const queryParams = reactive<UserCheckInQueryParams>({
  pageNum: 1,
  pageSize: 10,
  checkName: "",
  checkEmail: "",
  checkNumber: "",
  checkStatus: "",
});

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await UserCheckInAPI.getPage(queryParams);
    tableData.value = res.list;
    total.value = res.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  fetchList();
};

const handleResetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleSelectionChange = (selection: any[]) => {
  ids.value = selection.map((item) => item.id);
};

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm("确认删除该签到记录?", "警告", { type: "warning" });
    await UserCheckInAPI.deleteByIds(String(id));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.error(e);
  }
};

const handleBatchDelete = async () => {
  if (ids.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 条签到记录?`, "警告", {
      type: "warning",
    });
    await UserCheckInAPI.deleteByIds(ids.value.join(","));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.error(e);
  }
};

fetchList();
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-section {
  margin-bottom: 16px;
}
.table-section {
  margin-top: 16px;
}
</style>
