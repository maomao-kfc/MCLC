<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="status" label="凭证状态">
          <el-select v-model="queryParams.status" placeholder="全部" style="width: 160px" clearable>
            <el-option label="待核验" value="proof_submitted" />
            <el-option label="审核通过" value="review_passed" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item prop="name" label="姓名/邮箱">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入姓名或邮箱"
            clearable
            style="width: 200px"
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
        :data="proofList"
        highlight-current-row
        border
        class="table-section__content"
      >
        <el-table-column label="报名人" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.surname }}{{ row.name }}</template>
        </el-table-column>
        <el-table-column label="邮箱" prop="email" min-width="160" show-overflow-tooltip />
        <el-table-column label="手机号" prop="mobilePhone" min-width="120" show-overflow-tooltip />
        <el-table-column label="注册类型" prop="registerLabel" min-width="120" show-overflow-tooltip />
        <el-table-column label="应付金额" prop="paymentAmount" width="100" align="center" />
        <el-table-column label="申报金额" prop="declaredAmount" width="100" align="center" />
        <el-table-column label="OCR金额" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.ocrAmount">{{ row.ocrAmount }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="金额比对" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.amountMatch === 1" type="success" size="small">一致</el-tag>
            <el-tag v-else-if="row.amountMatch === 0" type="danger" size="small">不一致</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="凭证截图" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.fileUrl"
              :src="row.fileUrl"
              :preview-src-list="[row.fileUrl]"
              preview-teleported
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px"
            />
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" prop="declaredPayTime" width="160" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="160" align="center" />
        <el-table-column fixed="right" label="操作" width="180" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'proof_submitted'">
              <el-button type="success" size="small" link icon="check" @click="handleReview(row, 'pass')">通过</el-button>
              <el-button type="danger" size="small" link icon="close" @click="handleReview(row, 'reject')">驳回</el-button>
            </template>
            <el-button type="primary" size="small" link icon="view" @click="handleViewDetail(row)">详情</el-button>
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

    <!-- 审核驳回弹窗 -->
    <el-dialog v-model="rejectDialog.visible" title="驳回凭证" width="480px">
      <el-form label-width="80px">
        <el-form-item label="报名人">{{ rejectDialog.row?.surname }}{{ rejectDialog.row?.name }}</el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectDialog.reason" type="textarea" :rows="3" placeholder="请输入驳回原因（用户可见）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" :loading="reviewLoading" @click="submitReject">确认驳回</el-button>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 凭证详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="凭证详情" width="700px">
      <div v-if="detailDialog.row" class="proof-detail">
        <div class="proof-detail__image">
          <el-image
            v-if="detailDialog.row.fileUrl"
            :src="detailDialog.row.fileUrl"
            :preview-src-list="[detailDialog.row.fileUrl]"
            preview-teleported
            fit="contain"
            style="max-width: 100%; max-height: 400px"
          />
        </div>
        <el-descriptions :column="2" border class="proof-detail__info">
          <el-descriptions-item label="报名人">{{ detailDialog.row.surname }}{{ detailDialog.row.name }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailDialog.row.email }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailDialog.row.mobilePhone }}</el-descriptions-item>
          <el-descriptions-item label="注册类型">{{ detailDialog.row.registerLabel }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">{{ detailDialog.row.paymentAmount }}</el-descriptions-item>
          <el-descriptions-item label="申报金额">{{ detailDialog.row.declaredAmount }}</el-descriptions-item>
          <el-descriptions-item label="OCR金额">{{ detailDialog.row.ocrAmount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金额比对">
            <el-tag v-if="detailDialog.row.amountMatch === 1" type="success" size="small">一致</el-tag>
            <el-tag v-else-if="detailDialog.row.amountMatch === 0" type="danger" size="small">不一致</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailDialog.row.declaredPayTime }}</el-descriptions-item>
          <el-descriptions-item label="申报交易号">{{ detailDialog.row.declaredTradeNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="OCR交易单号">{{ detailDialog.row.ocrTradeNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="OCR商户单号">{{ detailDialog.row.ocrMerchantNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户备注" :span="2">{{ detailDialog.row.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailDialog.row.status)" size="small">{{ statusText(detailDialog.row.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">{{ detailDialog.row.reviewerName || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detailDialog.row.rejectReason" label="驳回原因" :span="2">
            <span class="text-danger">{{ detailDialog.row.rejectReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import PaymentProofAPI, { type ProofQueryParams } from "@/api/portal/paymentProof";

defineOptions({ name: "PaymentProof", inheritAttrs: false });

const queryFormRef = ref();
const loading = ref(false);
const reviewLoading = ref(false);
const total = ref(0);
const proofList = ref<any[]>([]);

const queryParams = reactive<ProofQueryParams>({
  pageNum: 1,
  pageSize: 10,
  status: "",
  name: "",
});

const rejectDialog = reactive({
  visible: false,
  row: null as any,
  reason: "",
});

const detailDialog = reactive({
  visible: false,
  row: null as any,
});

const statusTextMap: Record<string, string> = {
  proof_submitted: "待核验",
  review_passed: "审核通过",
  rejected: "已驳回",
  paid: "已支付",
  offline_paid: "线下登记",
};
const statusTagTypeMap: Record<string, "success" | "primary" | "info" | "warning" | "danger"> = {
  proof_submitted: "warning",
  review_passed: "primary",
  rejected: "danger",
  paid: "success",
  offline_paid: "success",
};
function statusText(status: string): string {
  return statusTextMap[status] || status;
}
function statusTagType(status: string): "success" | "primary" | "info" | "warning" | "danger" {
  return statusTagTypeMap[status] || "info";
}

async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const data = await PaymentProofAPI.getPage(queryParams);
    proofList.value = data.list;
    total.value = data.total ?? 0;
  } finally {
    loading.value = false;
  }
}

function handleQuery(): void {
  queryParams.pageNum = 1;
  fetchList();
}

function handleResetQuery(): void {
  queryFormRef.value?.resetFields();
  handleQuery();
}

/** 审核通过 */
async function handleReview(row: any, action: "pass" | "reject"): Promise<void> {
  if (action === "pass") {
    ElMessageBox.confirm(
      `确认通过「${row.surname}${row.name}」的支付凭证？通过后将进入对账确认环节。`,
      "审核确认",
      { confirmButtonText: "确认通过", cancelButtonText: "取消", type: "warning" }
    ).then(async () => {
      reviewLoading.value = true;
      try {
        await PaymentProofAPI.review({ proofId: row.id, action: "pass" });
        ElMessage.success("审核通过");
        fetchList();
      } finally {
        reviewLoading.value = false;
      }
    }).catch(() => {});
  } else {
    rejectDialog.row = row;
    rejectDialog.reason = "";
    rejectDialog.visible = true;
  }
}

/** 提交驳回 */
async function submitReject(): Promise<void> {
  if (!rejectDialog.reason.trim()) {
    ElMessage.warning("请输入驳回原因");
    return;
  }
  reviewLoading.value = true;
  try {
    await PaymentProofAPI.review({
      proofId: rejectDialog.row.id,
      action: "reject",
      rejectReason: rejectDialog.reason.trim(),
    });
    ElMessage.success("已驳回");
    rejectDialog.visible = false;
    fetchList();
  } finally {
    reviewLoading.value = false;
  }
}

/** 查看详情 */
function handleViewDetail(row: any): void {
  detailDialog.row = row;
  detailDialog.visible = true;
}

onMounted(() => {
  handleQuery();
});
</script>

<style scoped>
.text-muted {
  color: #c0c4cc;
}
.text-danger {
  color: #f56c6c;
}
.proof-detail__image {
  text-align: center;
  margin-bottom: 16px;
}
</style>