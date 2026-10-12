<template>
  <div class="app-container">
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
        <el-form-item prop="email" label="邮箱">
          <el-input
            v-model="queryParams.email"
            placeholder="请输入邮箱"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item class="search-buttons">
          <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
          <el-button icon="refresh" @click="handleResetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="hover" class="table-section">
      <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="tableData"
        highlight-current-row
        border
        class="table-section__content"
      >
        <el-table-column label="姓名" min-width="120" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.surname }}{{ scope.row.name }}</template>
        </el-table-column>
        <el-table-column label="邮箱" prop="email" min-width="180" show-overflow-tooltip />
        <el-table-column label="手机号" prop="mobilePhone" min-width="130" show-overflow-tooltip />
        <el-table-column label="组织" prop="organization" min-width="150" show-overflow-tooltip />
        <el-table-column label="国家" prop="country" min-width="80" show-overflow-tooltip />
        <el-table-column label="抵扣码" min-width="100" align="center">
          <template #default="scope">
            {{ scope.row.deductionCode || "无" }}
          </template>
        </el-table-column>
        <!--          <template #default="scope">-->
        <!--            <el-tag v-if="scope.row.registrationType === 'IND'" type="success">IND</el-tag>-->
        <!--            <el-tag v-else-if="scope.row.registrationType === 'WHS'" type="warning">WHS</el-tag>-->
        <!--            <el-tag v-else type="info">{{ scope.row.registrationType || '-' }}</el-tag>-->
        <!--          </template>-->
        <!--        </el-table-column>-->
        <!--        <el-table-column label="状态" align="center" width="80">-->
        <!--          <template #default="scope">-->
        <!--            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>-->
        <!--            <el-tag v-else type="info">禁用</el-tag>-->
        <!--          </template>-->
        <!--        </el-table-column>-->
        <el-table-column
          label="应付金额"
          prop="paymentAmount"
          min-width="120"
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.paymentAmount != null ? "¥" + scope.row.paymentAmount : "-" }}
          </template>
        </el-table-column>
        <el-table-column
          label="实际付款金额"
          prop="actualPayment"
          min-width="130"
          show-overflow-tooltip
        >
          <template #default="scope">
            {{ scope.row.actualPayment != null ? "¥" + scope.row.actualPayment : "-" }}
          </template>
        </el-table-column>
        <el-table-column
          label="注册类型"
          prop="registerLabel"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column label="付款方式" min-width="110" show-overflow-tooltip>
          <template #default="scope">
            {{ paymentMethodMap[scope.row.paymentMethod] || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="银行上传凭证" min-width="120" align="center">
          <template #default="scope">
            <el-image
              v-if="scope.row.picBank"
              :src="scope.row.picBank"
              fit="cover"
              :preview-teleported="true"
              style="width: 50px; height: 50px;"
              :preview-src-list="[scope.row.picBank]"
              :hide-on-click-modal="true"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          label="注册日期"
          prop="registrationDate"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column label="操作" align="center" width="100" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.reviewStatus === 0"
              type="primary"
              link
              size="small"
              @click="handleAudit(scope.row)"
            >
              审核
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

    <el-dialog
      v-model="auditDialogVisible"
      title="审核"
      width="500px"
      :close-on-click-modal="false"
      @close="handleAuditDialogClose"
    >
      <div class="audit-user-info">
        <p>
          <strong>姓名：</strong>
          {{ currentRow?.surname }}{{ currentRow?.name }}
        </p>
        <p>
          <strong>邮箱：</strong>
          {{ currentRow?.email }}
        </p>
        <p>
          <strong>抵扣码：</strong>
          {{ currentRow?.deductionCode || "无" }}
        </p>
      </div>
      <div v-if="auditStep === 'reason'" class="audit-reject-section">
        <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules">
          <el-form-item prop="reason">
            <el-input
              v-model="rejectForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入拒绝原因"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <template v-if="auditStep === 'action'">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="success" :loading="auditLoading" @click="handleApprove">通过</el-button>
          <el-button type="danger" :loading="auditLoading" @click="auditStep = 'reason'">
            拒绝
          </el-button>
        </template>
        <template v-else>
          <el-button @click="auditStep = 'action'">返回</el-button>
          <el-button type="danger" :loading="auditLoading" @click="handleReject">
            确认拒绝
          </el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import RegisterUserAPI from "@/api/portal/registerUser";
import type { RegisterUserItem } from "@/types/api";

const loading = ref(false);
const total = ref(0);
const tableData = ref<RegisterUserItem[]>([]);
const queryFormRef = ref();
const rejectFormRef = ref();

const auditDialogVisible = ref(false);
const auditLoading = ref(false);
const auditStep = ref<"action" | "reason">("action");
const currentRow = ref<RegisterUserItem | null>(null);
const rejectForm = reactive({
  reason: "",
});

const rejectRules = {
  reason: [{ required: true, message: "请输入拒绝原因", trigger: "blur" }],
};

const paymentMethodMap: Record<number, string> = {
  1: "信用卡",
  2: "银行转账",
  3: "微信",
  4: "支付宝",
};

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: "",
  email: "",
  registrationType: "",
} as any);

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await RegisterUserAPI.getPage(queryParams);
    tableData.value = res.list;
    total.value = res.total;
  } catch (e: any) {
    ElMessage.error(e?.msg || "查询失败");
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

const handleAudit = (row: RegisterUserItem) => {
  currentRow.value = row;
  auditStep.value = "action";
  auditDialogVisible.value = true;
};

const handleAuditDialogClose = () => {
  currentRow.value = null;
  auditStep.value = "action";
  rejectForm.reason = "";
  rejectFormRef.value?.resetFields();
};

const handleApprove = async () => {
  if (!currentRow.value?.id) return;
  auditLoading.value = true;
  try {
    await RegisterUserAPI.audit(currentRow.value.id, {
      reviewStatus: 1,
      deductionCode: currentRow.value.deductionCode,
    });
    ElMessage.success("审核通过");
    auditDialogVisible.value = false;
    fetchList();
  } catch (e: any) {
    ElMessage.error(e?.msg || "操作失败");
  } finally {
    auditLoading.value = false;
  }
};

const handleReject = async () => {
  if (!currentRow.value?.id) return;
  const valid = await rejectFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  auditLoading.value = true;
  try {
    await RegisterUserAPI.audit(currentRow.value.id, {
      reviewStatus: 2,
      rejectReason: rejectForm.reason,
      deductionCode: currentRow.value.deductionCode,
    });
    ElMessage.success("已拒绝");
    auditDialogVisible.value = false;
    fetchList();
  } catch (e: any) {
    ElMessage.error(e?.msg || "操作失败");
  } finally {
    auditLoading.value = false;
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
