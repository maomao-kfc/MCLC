<template>
  <div class="app-container">
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="paperTitle" label="论文标题">
          <el-input
            v-model="queryParams.paperTitle"
            placeholder="请输入论文标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="firstAuthor" label="第一作者">
          <el-input
            v-model="queryParams.firstAuthor"
            placeholder="请输入第一作者"
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
      <div class="table-section__toolbar">
        <div class="table-section__toolbar--actions">
          <el-button
            type="warning"
            :disabled="ids.length === 0"
            :loading="batchExportLoading"
            icon="download"
            @click="handleBatchExportPdf"
          >
            一键导出PDF
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
        <el-table-column label="姓名" min-width="120">
          <template #default="scope">{{ scope.row.surname }}{{ scope.row.name }}</template>
        </el-table-column>
        <el-table-column label="单位" prop="company" min-width="150" show-overflow-tooltip />
        <el-table-column label="论文标题" prop="paperTitle" min-width="200" show-overflow-tooltip />
        <el-table-column label="第一作者" prop="firstAuthor" min-width="120" />
        <el-table-column label="联系电话" prop="contactNumber" min-width="120" />
        <el-table-column
          label="联系邮箱"
          prop="contactEmail"
          min-width="180"
          show-overflow-tooltip
        />

        <el-table-column
          label="论文摘要"
          prop="paperAbstract"
          min-width="250"
          show-overflow-tooltip
        />
        <el-table-column label="关键词" prop="keywords" min-width="150" show-overflow-tooltip />
        <el-table-column label="论文附件" align="center" width="100">
          <template #default="scope">
            <el-button
              v-if="scope.row.appendix"
              type="primary"
              link
              @click="handleDownload(scope.row.appendix)"
            >
              查看
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.paperStatus === 1" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.paperStatus === 2" type="success">通过</el-tag>
            <el-tag
              v-else
              type="danger"
              style="cursor: pointer"
              @click="handleShowRejectReason(scope.row)"
            >
              拒绝
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="邮件状态" align="center" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.emailStatus === 0" type="info">未发送</el-tag>
            <el-tooltip v-else-if="scope.row.emailStatus === 1" content="部分或全部审稿邮件发送失败，请查看审核状态详情" placement="top">
              <el-tag type="danger" style="cursor: pointer">发送失败</el-tag>
            </el-tooltip>
            <el-tag v-else-if="scope.row.emailStatus === 2" type="success">已发送</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="260">
          <template #default="scope">
            <!--            <el-button type="primary" size="small" link icon="edit" @click="handleEditClick(scope.row)">编辑</el-button>-->
            <!--            <el-button type="danger" size="small" link icon="delete" @click="handleDelete(scope.row.id)">删除</el-button>-->
            <el-button
              v-if="scope.row.paperStatus === 1 && scope.row.emailStatus === 0"
              type="success"
              size="small"
              link
              icon="check"
              @click="handleAudit(scope.row)"
            >
              审核
            </el-button>
            <el-button
              v-if="scope.row.paperStatus === 1 && scope.row.emailStatus === 1"
              type="warning"
              size="small"
              link
              icon="check"
              @click="handleAudit(scope.row)"
            >
              重试发送
            </el-button>
            <el-button
              v-if="scope.row.paperStatus === 1 && scope.row.emailStatus === 2"
              type="info"
              size="small"
              link
              icon="check"
              disabled
            >
              已发送
            </el-button>
            <el-button type="primary" size="small" link @click="handleShowAuditStatus(scope.row)">
              查看审核状态
            </el-button>
            <el-button type="warning" size="small" link icon="download" @click="handleExportPdf(scope.row)">
              导出PDF
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
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="700px"
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓" prop="surname">
              <el-input v-model="formData.surname" placeholder="请输入姓" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="论文标题" prop="paperTitle">
              <el-input v-model="formData.paperTitle" placeholder="请输入论文标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="第一作者" prop="firstAuthor">
              <el-input v-model="formData.firstAuthor" placeholder="请输入第一作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactNumber">
              <el-input v-model="formData.contactNumber" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" />
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
            <el-form-item label="论文摘要" prop="paperAbstract">
              <el-input
                v-model="formData.paperAbstract"
                type="textarea"
                :rows="4"
                placeholder="请输入论文摘要"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="论文附件" prop="appendix">
              <el-upload :auto-upload="false" :limit="1">
                <el-button type="primary">上传论文</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="auditDialog.visible" title="审核论文投稿" width="550px">
      <el-form ref="auditFormRef" :model="auditDialog" label-width="100px">
        <template v-for="(expert, index) in auditDialog.experts" :key="index">
          <el-form-item
            :label="'专家' + (auditDialog.experts.length > 1 ? index + 1 : '') + '姓名'"
            :prop="'experts.' + index + '.name'"
            :rules="[{ required: true, message: '请输入专家姓名', trigger: 'blur' }]"
          >
            <div style="display: flex; gap: 8px; align-items: center">
              <el-input v-model="expert.name" placeholder="请输入专家姓名" style="flex: 1" />
              <span style="width: 24px; flex-shrink: 0" />
            </div>
          </el-form-item>
          <el-form-item
            :label="'专家' + (auditDialog.experts.length > 1 ? index + 1 : '') + '邮箱'"
            :prop="'experts.' + index + '.email'"
            :rules="[
              { required: true, message: '请输入专家邮箱', trigger: 'blur' },
              { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱格式', trigger: 'blur' },
            ]"
          >
            <div style="display: flex; gap: 8px; align-items: center">
              <el-input v-model="expert.email" placeholder="请输入专家邮箱" style="flex: 1" />
              <el-button
                v-if="auditDialog.experts.length > 1"
                type="danger"
                icon="delete"
                circle
                size="small"
                @click="removeExpert(index)"
              />
              <span v-else style="width: 24px; flex-shrink: 0" />
            </div>
          </el-form-item>
          <el-form-item
            :label="'确认邮箱'"
            :prop="'experts.' + index + '.confirmEmail'"
            :rules="[
              { required: true, message: '请再次输入专家邮箱', trigger: 'blur' },
              { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱格式', trigger: 'blur' },
              {
                validator: (rule: any, value: string, callback: Function) => {
                  if (value && value !== expert.email) {
                    callback(new Error('两次输入的邮箱不一致'));
                  } else {
                    callback();
                  }
                },
                trigger: 'blur',
              },
            ]"
          >
            <el-input v-model="expert.confirmEmail" placeholder="请再次输入专家邮箱" style="flex: 1" />
          </el-form-item>
        </template>
        <el-form-item v-if="auditDialog.experts.length < 3" label="">
          <el-button type="primary" link @click="addExpert">+ 添加专家</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="auditSubmitting" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectReasonDialog.visible" title="拒绝原因" width="500px">
      <p>{{ rejectReasonDialog.reason }}</p>
    </el-dialog>

    <el-dialog v-model="auditStatusDialog.visible" title="审核状态" width="900px">
      <el-table v-loading="auditStatusDialog.loading" :data="auditStatusDialog.list" border>
        <el-table-column label="专家姓名" prop="expertName" min-width="120" />
        <el-table-column
          label="专家邮箱"
          prop="expertEmail"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="发送状态" align="center" width="160">
          <template #default="scope">
            <el-tag v-if="scope.row.sendStatus === 1" type="warning">待发送</el-tag>
            <el-tag v-else-if="scope.row.sendStatus === 2" type="success">已发送</el-tag>
            <el-tooltip v-else-if="scope.row.sendStatus === 3" :content="scope.row.sendErrorMsg || '邮件发送失败'" placement="top">
              <el-tag type="danger" style="cursor: pointer">发送失败</el-tag>
            </el-tooltip>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" prop="sendTime" min-width="160" />
        <el-table-column label="审核时间" prop="reviewTime" min-width="160" />
        <el-table-column label="审核结果" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.reviewResult === 0" type="info">未审核</el-tag>
            <el-tag v-else-if="scope.row.reviewResult === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="scope.row.reviewResult === 2" type="danger">未通过</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          label="拒绝原因"
          prop="rejectReason"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column label="操作" align="center" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              :loading="scope.row.resending"
              @click="handleResendEmail(scope.row)"
            >
              重发邮件
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="auditStatusDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PaperSubmissionAPI from "@/api/portal/paperSubmission";
import { useUserStore } from "@/store";
import type { PaperSubmissionForm, PaperSubmissionQuery } from "@/types/api";

const userStore = useUserStore();

const loading = ref(false);
const total = ref(0);
const ids = ref<string[]>([]);
const tableData = ref<any[]>([]);
const dataTableRef = ref();
const queryFormRef = ref();
const formRef = ref<any>();

const queryParams = reactive<PaperSubmissionQuery>({
  pageNum: 1,
  pageSize: 10,
  paperTitle: "",
  firstAuthor: "",
});

const dialogState = reactive({ visible: false, title: "" });

const initialFormData: PaperSubmissionForm = {
  name: "",
  surname: "",
  company: "",
  paperTitle: "",
  firstAuthor: "",
  contactNumber: "",
  contactEmail: "",
  paperAbstract: "",
  keywords: "",
  appendix: "",
};

const formData = reactive<PaperSubmissionForm>({ ...initialFormData });

const auditFormRef = ref<any>();
const auditSubmitting = ref(false);

const auditDialog = reactive({
  visible: false,
  id: null as number | null,
  experts: [{ name: "", email: "" }] as { name: string; email: string }[],
  currentRow: null as any,
});

const rejectReasonDialog = reactive({
  visible: false,
  reason: "",
});

const auditStatusDialog = reactive({
  visible: false,
  loading: false,
  list: [] as any[],
  submissionId: null as number | null,
});

const rules = {
  paperTitle: [{ required: true, message: "请输入论文标题", trigger: "blur" }],
  firstAuthor: [{ required: true, message: "请输入第一作者", trigger: "blur" }],
  contactNumber: [{ required: true, message: "请输入联系电话", trigger: "blur" }],
  contactEmail: [{ required: true, message: "请输入联系邮箱", trigger: "blur" }],
};

const fetchList = async () => {
  loading.value = true;
  try {
    const res = await PaperSubmissionAPI.getPage(queryParams);
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

const handleCreateClick = () => {
  dialogState.title = "论文投稿";
  dialogState.visible = true;
  Object.assign(formData, { ...initialFormData });
};

const handleEditClick = (row: any) => {
  dialogState.title = "编辑";
  dialogState.visible = true;
  formData.id = row.id;
  formData.name = row.name;
  formData.surname = row.surname;
  formData.paperTitle = row.paperTitle;
  formData.firstAuthor = row.firstAuthor;
  formData.contactNumber = row.contactNumber;
  formData.contactEmail = row.contactEmail;
  formData.paperAbstract = row.paperAbstract;
  formData.keywords = row.keywords;
  formData.appendix = row.appendix;
};

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm("确认删除该数据?", "警告", { type: "warning" });
    await PaperSubmissionAPI.deleteByIds(String(id));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.error(e);
  }
};

const handleBatchDelete = async () => {
  if (ids.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 条数据?`, "警告", {
      type: "warning",
    });
    await PaperSubmissionAPI.deleteByIds(ids.value.join(","));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.error(e);
  }
};

const handleAudit = (row: any) => {
  auditDialog.id = row.id;
  auditDialog.experts = [{ name: "", email: "", confirmEmail: "" }];
  auditDialog.currentRow = row;
  auditDialog.visible = true;
};

const addExpert = () => {
  if (auditDialog.experts.length < 3) {
    auditDialog.experts.push({ name: "", email: "", confirmEmail: "" });
  }
};

const removeExpert = (index: number) => {
  if (auditDialog.experts.length > 1) {
    auditDialog.experts.splice(index, 1);
  }
};

const submitAudit = async () => {
  if (auditDialog.id == null) return;
  if (!auditFormRef.value) return;
  try {
    await auditFormRef.value.validate();
    auditSubmitting.value = true;
    await PaperSubmissionAPI.review(auditDialog.id, {
      emailsList: auditDialog.experts.map((e) => ({ name: e.name, email: e.email })),
      url: auditDialog.currentRow?.appendix || "",
      senderName: userStore.userInfo.nickname || userStore.userInfo.username || "",
    });
    ElMessage.success("已提交至邮件服务器");
    auditDialog.visible = false;
    auditFormRef.value.resetFields();
    fetchList();
  } catch (e: any) {
    if (e !== false) console.error(e);
  } finally {
    auditSubmitting.value = false;
  }
};

const submitForm = async () => {
  try {
    await formRef.value?.validate();
    if (formData.id) {
      await PaperSubmissionAPI.update(formData.id, { ...formData });
    } else {
      await PaperSubmissionAPI.create({ ...formData });
    }
    ElMessage.success("保存成功");
    closeDialog();
    fetchList();
  } catch (e: any) {
    if (e !== false) console.error(e);
  }
};

const handleDownload = (url: string) => {
  window.open(url, "_blank");
};

/**
 * 导出论文投稿为 PDF
 *
 * 后端生成PDF文件，前端通过axios blob方式下载：
 * - 有附件时：封面页（4行2列表格）+ 附件内容合并
 * - 无附件时：只显示信息表格
 * - 文件名格式：姓名.pdf
 *
 * 防重复点击：按记录ID隔离，同一条记录3秒冷却期，不同记录互不影响
 */

/** 每条记录的最后导出时间戳，防止同一条记录重复点击 */
const exportPdfLocks = new Map<number, number>();
/** 同一条记录的导出冷却期（毫秒） */
const EXPORT_COOLDOWN = 3000;
/** 批量导出限流间隔（毫秒），不同记录之间的最小间隔，防止服务端过载 */
const EXPORT_RATE_LIMIT = 500;

const handleExportPdf = async (row: any) => {
  const now = Date.now();
  const lastExportTime = exportPdfLocks.get(row.id);
  if (lastExportTime && now - lastExportTime < EXPORT_COOLDOWN) {
    ElMessage.warning("该记录正在导出或刚导出完成，请稍后再试");
    return;
  }
  exportPdfLocks.set(row.id, now);

  try {
    ElMessage.info("正在生成PDF，请稍候...");
    const res = await PaperSubmissionAPI.exportPdf(row.id);
    const blobData = (res as any)?.data || res;
    const blob = new Blob([blobData], { type: "application/pdf" });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${row.name || "论文信息"}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success("PDF导出成功");
  } catch (e) {
    console.error("导出PDF失败:", e);
    ElMessage.error("导出PDF失败，请稍后重试");
  }
};

/**
 * 一键导出：批量导出选中的论文PDF
 *
 * 逐个下载选中行的PDF，每个下载间隔0.5秒限流防崩
 * 同一条记录3秒冷却期内跳过，不同记录互不影响
 */
const batchExportLoading = ref(false);

const handleBatchExportPdf = async () => {
  if (ids.value.length === 0) {
    ElMessage.warning("请先选择要导出的论文");
    return;
  }

  batchExportLoading.value = true;
  let successCount = 0;
  let failCount = 0;
  let skipCount = 0;

  const selectedRows = tableData.value.filter((row) => ids.value.includes(row.id));

  for (let i = 0; i < selectedRows.length; i++) {
    const row = selectedRows[i];
    const now = Date.now();
    const lastExportTime = exportPdfLocks.get(row.id);

    // 同一条记录3秒冷却期内跳过
    if (lastExportTime && now - lastExportTime < EXPORT_COOLDOWN) {
      skipCount++;
      continue;
    }

    // 限流：不同记录之间间隔0.5秒（第一条不需要等）
    if (i > 0 && successCount > 0) {
      await new Promise((resolve) => setTimeout(resolve, EXPORT_RATE_LIMIT));
    }

    try {
      exportPdfLocks.set(row.id, Date.now());
      const res = await PaperSubmissionAPI.exportPdf(row.id);
      const blobData = (res as any)?.data || res;
      const blob = new Blob([blobData], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `${row.name || "论文信息"}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      successCount++;
    } catch (e) {
      console.error(`导出PDF失败 (id=${row.id}):`, e);
      failCount++;
    }
  }

  batchExportLoading.value = false;
  // 导出完成后清空选中状态
  ids.value = [];
  dataTableRef.value?.clearSelection();

  const tips: string[] = [];
  if (successCount > 0) tips.push(`成功 ${successCount} 份`);
  if (failCount > 0) tips.push(`失败 ${failCount} 份`);
  if (skipCount > 0) tips.push(`跳过 ${skipCount} 份（冷却中）`);

  if (successCount > 0) {
    ElMessage.success(`导出完成：${tips.join("，")}`);
  } else if (skipCount === selectedRows.length) {
    ElMessage.warning("所选记录均在冷却中，请稍后再试");
  } else {
    ElMessage.error("全部导出失败，请稍后重试");
  }
};

const handleShowRejectReason = (row: any) => {
  rejectReasonDialog.reason = row.rejectReason || "无";
  rejectReasonDialog.visible = true;
};

const handleShowAuditStatus = async (row: any) => {
  auditStatusDialog.visible = true;
  auditStatusDialog.loading = true;
  auditStatusDialog.submissionId = row.id;
  try {
    auditStatusDialog.list = await PaperSubmissionAPI.getExpertReviewList(row.id);
  } catch (e) {
    console.error(e);
    auditStatusDialog.list = [];
  } finally {
    auditStatusDialog.loading = false;
  }
};

/** 重发审稿邮件 */
const handleResendEmail = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认重新发送审稿邮件给 ${row.expertName}（${row.expertEmail}）？`,
      "重发邮件",
      { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" }
    );
  } catch {
    return; // 取消
  }

  row.resending = true;
  try {
    const result = await PaperSubmissionAPI.resendEmail(row.id);
    if (result.success) {
      ElMessage.success(`邮件已成功重发给 ${row.expertEmail}`);
      // 刷新审核状态列表和主列表，确保状态同步
      auditStatusDialog.list = await PaperSubmissionAPI.getExpertReviewList(auditStatusDialog.submissionId);
      fetchList();
    } else {
      ElMessage.error(`邮件重发失败，请稍后重试`);
    }
  } catch (e: any) {
    ElMessage.error(e?.message || "邮件重发失败");
  } finally {
    row.resending = false;
  }
};

const closeDialog = () => {
  dialogState.visible = false;
  formRef.value?.resetFields();
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
