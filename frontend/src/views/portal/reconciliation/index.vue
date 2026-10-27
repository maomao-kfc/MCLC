<template>
  <div class="app-container">
    <!-- 导入区域 -->
    <el-card shadow="hover" class="import-section">
      <div class="import-bar">
        <div class="import-bar__left">
          <span class="import-bar__title">协会对账Excel导入</span>
          <span class="import-bar__tip">导入协会导出的收款明细Excel，系统将自动按姓名+手机+金额+时间四维度匹配报名记录</span>
        </div>
        <el-upload
          :show-file-list="false"
          accept=".xlsx,.xls"
          :auto-upload="false"
          :on-change="handleImportChange"
        >
          <el-button type="primary" icon="upload" :loading="importLoading">导入对账Excel</el-button>
        </el-upload>
      </div>
      <el-alert
        v-if="importResult"
        :title="importResult.summary"
        :type="importResult.unmatchedCount > 0 ? 'warning' : 'success'"
        :description="importResult.detail"
        show-icon
        closable
        class="import-result"
      />
    </el-card>

    <!-- Tab区域 -->
    <el-tabs v-model="activeTab" class="recon-tabs">
      <!-- 对账明细 -->
      <el-tab-pane label="对账明细" name="records">
        <div class="filter-section">
          <el-form :inline="true">
            <el-form-item label="批次">
              <el-select v-model="recordQuery.batchId" placeholder="全部批次" clearable style="width: 220px" @change="handleRecordQuery">
                <el-option v-for="b in batchList" :key="b.id" :label="`${b.batchNo}（${b.createTime}）`" :value="b.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="匹配状态">
              <el-select v-model="recordQuery.matchStatus" placeholder="全部" clearable style="width: 140px" @change="handleRecordQuery">
                <el-option label="自动匹配" value="auto_matched" />
                <el-option label="人工确认" value="manual_matched" />
                <el-option label="未匹配" value="unmatched" />
                <el-option label="已驳回" value="rejected" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <el-card shadow="hover">
          <el-table v-loading="recordLoading" :data="recordList" border highlight-current-row>
            <el-table-column label="付款人" prop="rawName" min-width="100" show-overflow-tooltip />
            <el-table-column label="手机号" prop="rawPhone" min-width="120" show-overflow-tooltip />
            <el-table-column label="金额" prop="rawAmount" width="100" align="center" />
            <el-table-column label="支付时间" prop="rawPayTime" width="160" align="center" />
            <el-table-column label="交易号" prop="rawTradeNo" min-width="140" show-overflow-tooltip />
            <el-table-column label="匹配状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="matchStatusTagType(row.matchStatus)" size="small">{{ matchStatusText(row.matchStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="匹配得分" width="90" align="center">
              <template #default="{ row }">
                <span v-if="row.matchScore !== null">{{ row.matchScore }}</span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="匹配详情" prop="matchDetail" min-width="180" show-overflow-tooltip />
            <el-table-column label="关联报名ID" width="120" align="center">
              <template #default="{ row }">
                <span v-if="row.matchedRegistrationId">{{ row.matchedRegistrationId }}</span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="160" align="center">
              <template #default="{ row }">
                <template v-if="row.matchStatus === 'unmatched'">
                  <el-button type="primary" size="small" link icon="link" @click="openConfirmDialog(row)">确认匹配</el-button>
                  <el-button type="danger" size="small" link icon="close" @click="handleReject(row)">驳回</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-if="recordTotal > 0"
            v-model:total="recordTotal"
            v-model:page="recordQuery.pageNum"
            v-model:limit="recordQuery.pageSize"
            @pagination="fetchRecords"
          />
        </el-card>
      </el-tab-pane>

      <!-- 对账批次 -->
      <el-tab-pane label="对账批次" name="batches">
        <el-card shadow="hover">
          <el-table v-loading="batchLoading" :data="batchList" border highlight-current-row>
            <el-table-column label="批次号" prop="batchNo" min-width="180" show-overflow-tooltip />
            <el-table-column label="文件名" prop="fileName" min-width="200" show-overflow-tooltip />
            <el-table-column label="总条数" prop="totalCount" width="90" align="center" />
            <el-table-column label="已匹配" width="90" align="center">
              <template #default="{ row }">
                <span class="text-success">{{ row.matchedCount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="未匹配" width="90" align="center">
              <template #default="{ row }">
                <span :class="row.unmatchedCount > 0 ? 'text-danger' : ''">{{ row.unmatchedCount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作人" prop="operatorName" width="110" align="center" />
            <el-table-column label="导入时间" prop="createTime" width="170" align="center" />
          </el-table>
          <pagination
            v-if="batchTotal > 0"
            v-model:total="batchTotal"
            v-model:page="batchQuery.pageNum"
            v-model:limit="batchQuery.pageSize"
            @pagination="fetchBatches"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 人工确认匹配弹窗 -->
    <el-dialog v-model="confirmDialog.visible" title="人工确认匹配" width="500px">
      <el-form label-width="110px">
        <el-descriptions :column="1" border class="confirm-desc">
          <el-descriptions-item label="协会付款人">{{ confirmDialog.row?.rawName }}</el-descriptions-item>
          <el-descriptions-item label="付款金额">{{ confirmDialog.row?.rawAmount }}</el-descriptions-item>
          <el-descriptions-item label="付款时间">{{ confirmDialog.row?.rawPayTime }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="报名记录ID" required style="margin-top: 16px">
          <el-input-number v-model="confirmDialog.registrationId" :min="1" placeholder="请输入注册用户ID" style="width: 100%" />
        </el-form-item>
        <el-form-item>
          <span class="text-muted">提示：可在「注册用户管理」页面查找对应报名记录的ID</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="confirmLoading" @click="submitConfirmMatch">确认匹配并标记已支付</el-button>
        <el-button @click="confirmDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import ReconciliationAPI, { type BatchQueryParams, type RecordQueryParams } from "@/api/portal/reconciliation";

defineOptions({ name: "Reconciliation", inheritAttrs: false });

const activeTab = ref("records");
const importLoading = ref(false);
const importResult = ref<any>(null);

// 批次
const batchLoading = ref(false);
const batchList = ref<any[]>([]);
const batchTotal = ref(0);
const batchQuery = reactive<BatchQueryParams>({ pageNum: 1, pageSize: 10 });

// 明细
const recordLoading = ref(false);
const recordList = ref<any[]>([]);
const recordTotal = ref(0);
const recordQuery = reactive<RecordQueryParams>({
  pageNum: 1,
  pageSize: 10,
  batchId: undefined,
  matchStatus: "",
});

// 确认匹配弹窗
const confirmDialog = reactive({
  visible: false,
  row: null as any,
  registrationId: undefined as number | undefined,
});
const confirmLoading = ref(false);

const matchStatusTextMap: Record<string, string> = {
  auto_matched: "自动匹配",
  manual_matched: "人工确认",
  unmatched: "未匹配",
  rejected: "已驳回",
};
const matchStatusTagTypeMap: Record<string, "success" | "primary" | "info" | "warning" | "danger"> = {
  auto_matched: "success",
  manual_matched: "success",
  unmatched: "warning",
  rejected: "info",
};
function matchStatusText(status: string): string {
  return matchStatusTextMap[status] || status;
}
function matchStatusTagType(status: string): "success" | "primary" | "info" | "warning" | "danger" {
  return matchStatusTagTypeMap[status] || "info";
}

/** 导入Excel */
async function handleImportChange(uploadFile: any): Promise<void> {
  const raw = uploadFile.raw;
  if (!raw) return;
  if (!/\.(xlsx|xls)$/i.test(raw.name)) {
    ElMessage.warning("仅支持 .xlsx / .xls 格式文件");
    return;
  }
  importLoading.value = true;
  importResult.value = null;
  try {
    const data = await ReconciliationAPI.importExcel(raw);
    importResult.value = {
      summary: `导入完成：共 ${data.totalCount} 条，自动匹配 ${data.matchedCount} 条，未匹配 ${data.unmatchedCount} 条`,
      detail: data.unmatchedCount > 0 ? "未匹配的记录请在「对账明细」中人工确认或驳回" : "全部记录已自动匹配成功",
      unmatchedCount: data.unmatchedCount,
    };
    ElMessage.success("对账Excel导入成功");
    fetchBatches();
    fetchRecords();
  } catch (e: any) {
    ElMessage.error(e?.message || "导入失败");
  } finally {
    importLoading.value = false;
  }
}

async function fetchBatches(): Promise<void> {
  batchLoading.value = true;
  try {
    const data = await ReconciliationAPI.getBatchPage(batchQuery);
    batchList.value = data.list;
    batchTotal.value = data.total ?? 0;
  } finally {
    batchLoading.value = false;
  }
}

async function fetchRecords(): Promise<void> {
  recordLoading.value = true;
  try {
    const data = await ReconciliationAPI.getRecordPage(recordQuery);
    recordList.value = data.list;
    recordTotal.value = data.total ?? 0;
  } finally {
    recordLoading.value = false;
  }
}

function handleRecordQuery(): void {
  recordQuery.pageNum = 1;
  fetchRecords();
}

/** 打开确认匹配弹窗 */
function openConfirmDialog(row: any): void {
  confirmDialog.row = row;
  confirmDialog.registrationId = undefined;
  confirmDialog.visible = true;
}

/** 提交人工确认匹配 */
async function submitConfirmMatch(): Promise<void> {
  if (!confirmDialog.registrationId) {
    ElMessage.warning("请输入注册用户ID");
    return;
  }
  confirmLoading.value = true;
  try {
    await ReconciliationAPI.confirmMatch(confirmDialog.row.id, confirmDialog.registrationId);
    ElMessage.success("匹配确认成功，已标记该报名为已支付");
    confirmDialog.visible = false;
    fetchRecords();
  } finally {
    confirmLoading.value = false;
  }
}

/** 驳回对账记录 */
function handleReject(row: any): void {
  ElMessageBox.confirm(
    `确认驳回该笔对账记录？（付款人：${row.rawName}，金额：${row.rawAmount}）`,
    "驳回确认",
    { confirmButtonText: "确认驳回", cancelButtonText: "取消", type: "warning" }
  ).then(async () => {
    await ReconciliationAPI.rejectMatch(row.id);
    ElMessage.success("已驳回");
    fetchRecords();
  }).catch(() => {});
}

onMounted(() => {
  fetchBatches();
  fetchRecords();
});
</script>

<style scoped>
.import-section {
  margin-bottom: 16px;
}
.import-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.import-bar__title {
  font-size: 15px;
  font-weight: 600;
  display: block;
  margin-bottom: 4px;
}
.import-bar__tip {
  font-size: 12px;
  color: #999;
}
.import-result {
  margin-top: 12px;
}
.recon-tabs {
  margin-top: 4px;
}
.text-muted {
  color: #c0c4cc;
}
.text-success {
  color: #67c23a;
}
.text-danger {
  color: #f56c6c;
}
.confirm-desc {
  margin-bottom: 4px;
}
</style>