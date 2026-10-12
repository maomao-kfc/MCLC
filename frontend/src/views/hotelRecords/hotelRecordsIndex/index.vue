<template>
  <div class="app-container">
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="guestName" label="入住人">
          <el-input v-model="queryParams.guestName" placeholder="请输入入住人" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="hotelName" label="酒店名称">
          <el-input v-model="queryParams.hotelName" placeholder="请输入酒店名称" clearable @keyup.enter="handleQuery" />
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
          <el-button type="danger" :disabled="ids.length === 0" icon="delete" @click="handleBatchDelete()">删除</el-button>
        </div>
      </div>

      <el-table ref="dataTableRef" v-loading="loading" :data="tableData" highlight-current-row border class="table-section__content" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="订单号" prop="orderNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="入住人" prop="guestName" min-width="100" />
        <el-table-column label="联系电话" prop="phone" min-width="120" />
        <el-table-column label="酒店名称" prop="hotelName" min-width="150" show-overflow-tooltip />
        <el-table-column label="房型" prop="roomType" min-width="100" />
        <el-table-column label="入住日期" prop="checkInDate" min-width="120" />
        <el-table-column label="退房日期" prop="checkOutDate" min-width="120" />
        <el-table-column label="入住天数" align="center" width="100">
          <template #default="scope">{{ scope.row.days }}晚</template>
        </el-table-column>
        <el-table-column label="订单金额" prop="amount" min-width="120">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column label="预订状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待入住</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">已入住</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="info">已完成</el-tag>
            <el-tag v-else type="danger">已取消</el-tag>
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

    <el-dialog v-model="dialogState.visible" :title="dialogState.title" width="600px" @close="closeDialog">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="formData.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="入住人" prop="guestName">
          <el-input v-model="formData.guestName" placeholder="请输入入住人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="酒店名称" prop="hotelName">
          <el-input v-model="formData.hotelName" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="房型" prop="roomType">
          <el-input v-model="formData.roomType" placeholder="请输入房型" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker v-model="formData.checkInDate" type="date" placeholder="请选择入住日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退房日期" prop="checkOutDate">
              <el-date-picker v-model="formData.checkOutDate" type="date" placeholder="请选择退房日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="订单金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预订状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">待入住</el-radio>
            <el-radio :value="1">已入住</el-radio>
            <el-radio :value="2">已完成</el-radio>
            <el-radio :value="3">已取消</el-radio>
          </el-radio-group>
        </el-form-item>
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

const queryParams = reactive({ pageNum: 1, pageSize: 10, guestName: "", hotelName: "" });

const dialogState = reactive({ visible: false, title: "" });

const formData = reactive<any>({
  id: undefined,
  orderNo: "",
  guestName: "",
  phone: "",
  hotelName: "",
  roomType: "",
  checkInDate: "",
  checkOutDate: "",
  days: 1,
  amount: 0,
  status: 0,
});

const rules = {
  orderNo: [{ required: true, message: "请输入订���号", trigger: "blur" }],
  guestName: [{ required: true, message: "请输入入住人", trigger: "blur" }],
  phone: [{ required: true, message: "请输入联系电话", trigger: "blur" }],
};

const fetchList = async () => {
  loading.value = true;
  setTimeout(() => { tableData.value = []; total.value = 0; loading.value = false; }, 300);
};

const handleQuery = () => { queryParams.pageNum = 1; fetchList(); };
const handleResetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleSelectionChange = (selection: any[]) => { ids.value = selection.map((item) => item.id); };

const handleCreateClick = () => {
  dialogState.title = "新增";
  dialogState.visible = true;
  Object.assign(formData, { id: undefined, orderNo: "", guestName: "", phone: "", hotelName: "", roomType: "", checkInDate: "", checkOutDate: "", days: 1, amount: 0, status: 0 });
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