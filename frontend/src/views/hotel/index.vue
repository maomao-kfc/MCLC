<!-- 酒店页面 -->

<template>
  <div class="app-container">
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="name" label="酒店名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入酒店名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status" label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="已取消合作" :value="0" />
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
          <el-button type="success" icon="plus" @click="handleCreateClick">新增</el-button>
          <el-button
            type="danger"
            :disabled="selectedIds.length === 0"
            icon="delete"
            @click="handleBatchDelete"
          >
            删除
          </el-button>
        </div>
      </div>

      <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="hotelList"
        highlight-current-row
        border
        class="table-section__content"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="酒店名称" prop="name" min-width="150" />
        <el-table-column label="联系电话" prop="phone" width="140" />
        <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="info">已取消合作</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              link
              icon="view"
              @click="handleDetailClick(scope.row.id)"
            >
              详情
            </el-button>
            <el-button
              type="primary"
              size="small"
              link
              icon="edit"
              @click="handleEditClick(scope.row.id)"
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

    <!-- 表单组件 (新增/编辑) -->
    <HotelForm
      v-model:visible="formDialogVisible"
      :form-data="currentFormData"
      :title="formTitle"
      @success="handleFormSuccess"
    />

    <!-- 详情抽屉组件 -->
    <HotelDetail
      v-model:visible="detailDrawerVisible"
      :hotel-id="currentDetailId"
      @close="detailDrawerVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import { getHotelPage, deleteHotel } from "@/api/hotel/hotel";
import type { Hotel } from "./types";
import HotelForm from "./components/HotelForm.vue";
import HotelDetail from "./components/HotelDetail.vue";

defineOptions({
  name: "Hotel",
  inheritAttrs: false,
});

// DOM 引用
const queryFormRef = ref<FormInstance>();

// 数据状态
const loading = ref(false);
const total = ref(0);
const hotelList = ref<Hotel[]>([]);
const selectedIds = ref<number[]>([]);

// 查询参数
const queryParams = reactive({
  name: "",
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

// 表单弹窗控制
const formDialogVisible = ref(false);
const formTitle = ref("");
const currentFormData = ref<Partial<Hotel>>({});

// 详情抽屉控制
const detailDrawerVisible = ref(false);
const currentDetailId = ref<number>();

/**
 * 获取列表数据
 */
async function fetchList() {
  loading.value = true;
  try {
    const res = await getHotelPage(queryParams);
    // MyBatis-Plus 分页返回：{ records: [], total: 0, ... }
    hotelList.value = res.records || [];
    total.value = res.total ?? 0;
  } catch (error) {
    console.error("获取酒店列表失败:", error);
  } finally {
    loading.value = false;
  }
}

/**
 * 搜索
 */
function handleQuery() {
  queryParams.pageNum = 1;
  fetchList();
}

/**
 * 重置
 */
function handleResetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.name = "";
  queryParams.status = undefined;
  handleQuery();
}

/**
 * 多选变化
 */
function handleSelectionChange(selection: Hotel[]) {
  selectedIds.value = selection.map((item) => item.id as number);
}

/**
 * 新增
 */
function handleCreateClick() {
  formTitle.value = "新增酒店";
  currentFormData.value = { status: 1 }; // 默认正常状态
  formDialogVisible.value = true;
}

/**
 * 编辑
 */
function handleEditClick(id: number) {
  formTitle.value = "编辑酒店";
  // 注意：这里直接传 id，由 HotelForm 内部请求详情
  currentFormData.value = { id };
  formDialogVisible.value = true;
}

/**
 * 查看详情
 */
function handleDetailClick(id: number) {
  currentDetailId.value = id;
  detailDrawerVisible.value = true;
}

/**
 * 表单操作成功后的回调
 */
function handleFormSuccess() {
  formDialogVisible.value = false;
  fetchList();
}

/**
 * 单个删除
 */
function handleDelete(id?: number) {
  const idsToDelete = id ? String(id) : selectedIds.value.join(",");
  if (!idsToDelete) {
    ElMessage.warning("请选择要删除的项");
    return;
  }

  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    loading.value = true;
    try {
      await deleteHotel(idsToDelete);
      ElMessage.success("删除成功");
      handleQuery();
    } finally {
      loading.value = false;
    }
  });
}

/**
 * 批量删除
 */
function handleBatchDelete() {
  handleDelete();
}

onMounted(() => {
  console.log("进入");
  fetchList();
});
</script>

<style scoped></style>
