/**会议日程管理 - 双语字段模型 */

<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="meetName" label="会议名称">
          <el-input v-model="queryParams.meetName" placeholder="请输入会议名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item class="search-buttons">
          <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
          <el-button icon="refresh" @click="handleResetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="hover" class="table-section">
      <el-table v-loading="loading" :data="meetingList" border row-key="meetingId">
        <!-- 展开行：显示该会议下的板块列表 -->
        <el-table-column type="expand">
          <template #default="{ row: meeting }">
            <div style="padding: 12px 48px">
              <div style="margin-bottom: 10px">
                <el-button type="success" size="small" icon="plus" @click="handleCreateSession(meeting.meetingId)">新增板块</el-button>
              </div>
              <el-table v-if="meeting.sessions && meeting.sessions.length > 0" :data="meeting.sessions" border size="small">
                <el-table-column label="板块标题" min-width="200">
                  <template #default="{ row: session }">
                    {{ session.title }}
                    <span v-if="session.titleEn" class="en-text">/ {{ session.titleEn }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="类型" width="140" align="center">
                  <template #default="{ row: session }">
                    <el-tag size="small">{{ getItemTypeLabel(session.itemType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="主持人" min-width="140">
                  <template #default="{ row: session }">
                    {{ session.chair }}
                    <span v-if="session.chairEn" class="en-text">/ {{ session.chairEn }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="条目数" width="80" align="center">
                  <template #default="{ row: session }">{{ session.items?.length || 0 }}</template>
                </el-table-column>
                <el-table-column label="参与人数" width="90" align="center">
                  <template #default="{ row: session }">{{ session.participants?.length || 0 }}</template>
                </el-table-column>
                <el-table-column label="排序" prop="sortOrder" width="70" align="center" />
                <el-table-column label="操作" width="210" align="center">
                  <template #default="{ row: session, $index: sessionIndex }">
                    <el-button type="primary" size="small" link icon="edit" @click="handleEditSession(session)">编辑</el-button>
                    <el-button type="danger" size="small" link icon="delete" @click="handleDeleteSession(session.id)">删除</el-button>
                    <el-button size="small" link icon="top" @click="handleMoveSessionUp(sessionIndex, meeting)" :disabled="sessionIndex === 0">上移</el-button>
                    <el-button size="small" link icon="bottom" @click="handleMoveSessionDown(sessionIndex, meeting)" :disabled="sessionIndex === (meeting.sessions?.length || 0) - 1">下移</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无板块，请点击上方按钮新增" :image-size="60" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="会议名称" min-width="220">
          <template #default="{ row }">
            {{ row.meetingName }}
            <span v-if="row.meetingNameEn" class="en-text">/ {{ row.meetingNameEn }}</span>
          </template>
        </el-table-column>
        <el-table-column label="日期" prop="date" width="180" align="center" />
        <el-table-column label="会场" min-width="160">
          <template #default="{ row }">
            {{ row.hall }}
            <span v-if="row.hallEn" class="en-text">/ {{ row.hallEn }}</span>
          </template>
        </el-table-column>
        <el-table-column label="板块数" width="80" align="center">
          <template #default="{ row }">{{ row.sessions?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link icon="edit" @click="handleEditMeeting(row)">编辑</el-button>
            <el-button type="danger" size="small" link icon="delete" @click="handleDeleteMeeting(row.meetingId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-if="total > 0" v-model:total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="fetchList" />
    </el-card>

    <!-- 板块编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="1200px" @close="closeDialog" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="板块标题" prop="title">
              <el-input v-model="formData.title" placeholder="中文标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文标题">
              <el-input v-model="formData.titleEn" placeholder="English Title" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主持人">
              <el-input v-model="formData.chairperson" placeholder="中文主持人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文主持人">
              <el-input v-model="formData.chairpersonEn" placeholder="Moderator" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="板块类型" prop="itemType">
              <el-select v-model="formData.itemType" placeholder="请选择类型" style="width: 100%">
                <el-option v-for="opt in SESSION_TYPE_OPTIONS" :key="opt.value" :label="`${opt.label} (${opt.labelEn})`" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="formData.sortOrder" :min="1" :max="999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="嘉宾">
              <el-input v-model="formData.panelists" placeholder="中文嘉宾（逗号分隔）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文嘉宾">
              <el-input v-model="formData.panelistsEn" placeholder="Panelists (comma separated)" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="中文备注" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文备注">
              <el-input v-model="formData.remarkEn" type="textarea" :rows="2" placeholder="Remark" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 日程条目 -->
        <el-divider content-position="left">日程条目</el-divider>
        <div class="table-toolbar">
          <el-button type="primary" size="small" icon="plus" @click="addItem">添加条目</el-button>
        </div>
        <el-table :data="formData.items" border size="small" style="width: 100%" :span-method="itemSpanMethod">
          <el-table-column label="独立时间" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row._timeIndependent" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="时间" width="130" align="center">
            <template #default="{ row }">
              <el-input v-model="row.timePeriod" placeholder="09:00-10:00" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="条目类型" width="130">
            <template #default="{ row }">
              <el-select v-model="row.itemType" placeholder="类型" size="small" style="width: 100%" @change="(val: string) => onItemTypeChange(row, val)">
                <el-option v-for="opt in ITEM_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="题目(中)" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.title" placeholder="中文题目" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="题目(英)" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.titleEn" placeholder="Topic" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="讲者(中)" width="110">
            <template #default="{ row }">
              <el-input v-model="row.speakerName" placeholder="讲者" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="讲者(英)" width="110">
            <template #default="{ row }">
              <el-input v-model="row.speakerNameEn" placeholder="Speaker" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="单位(中)" width="120">
            <template #default="{ row }">
              <el-input v-model="row.speakerOrg" placeholder="单位" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="单位(英)" width="120">
            <template #default="{ row }">
              <el-input v-model="row.speakerOrgEn" placeholder="Org" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="排序" width="70">
            <template #default="{ row }">
              <el-input-number v-model="row.sortOrder" :min="1" :max="999" size="small" controls-position="right" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ $index }">
              <el-button size="small" link icon="top" @click="moveItemUp($index)" :disabled="$index === 0" />
              <el-button size="small" link icon="bottom" @click="moveItemDown($index)" :disabled="$index === (formData.items?.length || 0) - 1" />
              <el-button type="danger" link icon="delete" size="small" @click="removeItem($index)" />
            </template>
          </el-table-column>
        </el-table>

        <!-- 参与人（Panel Discussion） -->
        <template v-if="formData.itemType === 'DISCUSSION'">
          <el-divider content-position="left">参与人 (Panel Discussion)</el-divider>
          <el-row :gutter="20" style="margin-bottom: 12px">
            <el-col :span="8">
              <el-form-item label="时间段" label-width="70px" style="margin-bottom: 0">
                <el-input v-model="formData.timePeriod" placeholder="09:00-10:30" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="时间段(英)" label-width="85px" style="margin-bottom: 0">
                <el-input v-model="formData.timePeriodEn" placeholder="09:00-10:30" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-bottom: 12px">
            <el-col :span="12">
              <el-form-item label="条目标题(中)" label-width="100px" style="margin-bottom: 0">
                <el-input v-model="formData.topicContent" type="textarea" :rows="4" placeholder="真实世界近视病例专题讨论&#10;-角膜塑形术&#10;-多焦点接触镜" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="条目标题(英)" label-width="100px" style="margin-bottom: 0">
                <el-input v-model="formData.topicContentEn" type="textarea" :rows="4" placeholder="Panel discussion on real world myopia cases&#10;-orthokeratology&#10;-multifocal contact lens" />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="table-toolbar">
            <el-button type="primary" size="small" icon="plus" @click="addParticipant">添加参与人</el-button>
          </div>
          <el-table :data="formData.participants" border size="small" style="width: 100%">
            <el-table-column label="姓名(中)" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.name" placeholder="中文姓名" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="姓名(英)" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.nameEn" placeholder="Name" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="职称(中)" width="140">
              <template #default="{ row }">
                <el-input v-model="row.title" placeholder="中文职称" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="职称(英)" width="140">
              <template #default="{ row }">
                <el-input v-model="row.titleEn" placeholder="Title" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="单位(中)" width="140">
              <template #default="{ row }">
                <el-input v-model="row.organization" placeholder="中文单位" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="单位(英)" width="140">
              <template #default="{ row }">
                <el-input v-model="row.organizationEn" placeholder="Organization" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="排序" width="70">
              <template #default="{ row }">
                <el-input-number v-model="row.sortOrder" :min="1" :max="999" size="small" controls-position="right" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ $index }">
                <el-button size="small" link icon="top" @click="moveParticipantUp($index)" :disabled="$index === 0" />
                <el-button size="small" link icon="bottom" @click="moveParticipantDown($index)" :disabled="$index === (formData.participants?.length || 0) - 1" />
                <el-button type="danger" link icon="delete" size="small" @click="removeParticipant($index)" />
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
        <el-button @click="closeDialog">取消</el-button>
      </template>
    </el-dialog>

    <!-- 会议编辑弹窗 -->
    <el-dialog v-model="meetingDialogVisible" title="编辑会议" width="700px" @close="closeMeetingDialog" destroy-on-close>
      <el-form ref="meetingFormRef" :model="meetingFormData" :rules="meetingRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会议名称" prop="name">
              <el-input v-model="meetingFormData.name" placeholder="中文会议名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文名称">
              <el-input v-model="meetingFormData.nameEn" placeholder="English Name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开会日期" prop="meetingDate">
              <el-date-picker v-model="meetingFormData.meetingDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="meetingFormData.sortOrder" :min="1" :max="999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会场">
              <el-input v-model="meetingFormData.venue" placeholder="中文会场" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文会场">
              <el-input v-model="meetingFormData.venueEn" placeholder="Venue" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="meetingFormData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="meetingSubmitLoading" @click="handleMeetingSubmit">确定</el-button>
        <el-button @click="closeMeetingDialog">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import MeetingScheduleAPI from "@/api/portal/meeting";
import type { MeetingScheduleVO, SessionVO, MeetingSessionForm, MeetingEditForm, AgendaItemForm, ParticipantForm } from "@/types/meeting/meeting";
import { ITEM_TYPE_OPTIONS, SESSION_TYPE_OPTIONS } from "@/types/meeting/meeting";

defineOptions({ name: "MeetingManagement", inheritAttrs: false });

const queryFormRef = ref();
const formRef = ref();

const loading = ref(false);
const submitLoading = ref(false);
const total = ref(0);

const queryParams = reactive({
  meetName: "",
  pageNum: 1,
  pageSize: 10,
});

const meetingList = ref<MeetingScheduleVO[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref("");
const currentMeetingId = ref<number>(1);

const formData = reactive<MeetingSessionForm>({
  id: undefined,
  meetingId: undefined,
  title: "",
  titleEn: "",
  chairperson: "",
  chairpersonEn: "",
  panelists: "",
  panelistsEn: "",
  timePeriod: "",
  timePeriodEn: "",
  topicContent: "",
  topicContentEn: "",
  remark: "",
  remarkEn: "",
  itemType: "",
  sortOrder: 1,
  items: [],
  participants: [],
});

// 会议编辑弹窗状态
const meetingDialogVisible = ref(false);
const meetingSubmitLoading = ref(false);
const meetingFormRef = ref();
const meetingFormData = reactive<MeetingEditForm>({
  meetingId: 0,
  name: "",
  nameEn: "",
  meetingDate: "",
  venue: "",
  venueEn: "",
  sortOrder: 1,
  status: 1,
});

const meetingRules = reactive({
  name: [{ required: true, message: "请输入会议名称", trigger: "blur" }],
  meetingDate: [{ required: true, message: "请选择开会日期", trigger: "change" }],
});

const rules = reactive({
  title: [{ required: true, message: "请输入板块标题", trigger: "blur" }],
  itemType: [{ required: true, message: "请选择板块类型", trigger: "change" }],
});

/** itemType枚举标签（板块列表展示用，需包含开幕式/闭幕式） */
function getItemTypeLabel(value: string): string {
  const opt = SESSION_TYPE_OPTIONS.find((o) => o.value === value) || ITEM_TYPE_OPTIONS.find((o) => o.value === value);
  return opt ? opt.label : value || "-";
}

/** 加载会议列表 */
async function fetchList(): Promise<void> {
  loading.value = true;
  try {
    const res = await MeetingScheduleAPI.getPage(queryParams);
    meetingList.value = res?.records ?? [];
    total.value = res?.total ?? 0;
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

/** 新增板块 */
function handleCreateSession(meetingId: number): void {
  resetForm();
  currentMeetingId.value = meetingId;
  formData.meetingId = meetingId;
  dialogTitle.value = "新增板块";
  dialogVisible.value = true;
}

/** 编辑板块 */
function handleEditSession(session: SessionVO): void {
  resetForm();
  formData.id = session.id;
  formData.title = session.title || "";
  formData.titleEn = session.titleEn || "";
  formData.chairperson = session.chair || "";
  formData.chairpersonEn = session.chairEn || "";
  formData.panelists = session.panelists || "";
  formData.panelistsEn = session.panelistsEn || "";
  formData.timePeriod = session.timePeriod || "";
  formData.timePeriodEn = session.timePeriodEn || "";
  formData.topicContent = session.topicContent || "";
  formData.topicContentEn = session.topicContentEn || "";
  formData.remark = session.remark || "";
  formData.remarkEn = session.remarkEn || "";
  formData.itemType = session.itemType || "";
  formData.sortOrder = session.sortOrder ?? 1;

  // 映射条目
  formData.items = (session.items || []).map((item) => ({
    id: item.id,
    title: item.topic || "",
    titleEn: item.topicEn || "",
    type: item.type || "",
    typeEn: item.typeEn || "",
    itemType: item.itemType || "",
    timePeriod: item.time || "",
    speakerName: item.speaker || "",
    speakerNameEn: item.speakerEn || "",
    speakerOrg: item.affiliation || "",
    speakerOrgEn: item.affiliationEn || "",
    sortOrder: item.sortOrder ?? 1,
    speakerId: item.speakerId,
    _timeIndependent: false,
  })) as AgendaItemForm[];

  // 映射参与人
  formData.participants = (session.participants || []).map((p) => ({
    name: p.name || "",
    nameEn: p.nameEn || "",
    title: p.speakerTitle || "",
    titleEn: p.speakerTitleEn || "",
    organization: p.affiliation || "",
    organizationEn: p.affiliationEn || "",
    sortOrder: p.sortOrder || 1,
  })) as ParticipantForm[];

  dialogTitle.value = "编辑板块";
  dialogVisible.value = true;
}

/** 删除板块 */
function handleDeleteSession(sessionId: number): void {
  ElMessageBox.confirm("确认删除该板块及其所有条目和参与人？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    loading.value = true;
    try {
      await MeetingScheduleAPI.deleteSession(sessionId);
      ElMessage.success("删除成功");
      fetchList();
    } finally {
      loading.value = false;
    }
  });
}

/** 编辑会议 */
function handleEditMeeting(row: MeetingScheduleVO): void {
  meetingFormData.meetingId = row.meetingId;
  meetingFormData.name = row.meetingName || "";
  meetingFormData.nameEn = row.meetingNameEn || "";
  meetingFormData.meetingDate = row.meetingDate || "";
  meetingFormData.venue = row.hall || "";
  meetingFormData.venueEn = row.hallEn || "";
  meetingFormData.sortOrder = row.sortOrder ?? 1;
  meetingFormData.status = row.status ?? 1;
  meetingDialogVisible.value = true;
}

/** 会议编辑提交 */
async function handleMeetingSubmit(): Promise<void> {
  const valid = await meetingFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  meetingSubmitLoading.value = true;
  try {
    const params: Record<string, any> = {
      name: meetingFormData.name,
      nameEn: meetingFormData.nameEn || undefined,
      meetingDate: meetingFormData.meetingDate || undefined,
      venue: meetingFormData.venue || undefined,
      venueEn: meetingFormData.venueEn || undefined,
      sortOrder: meetingFormData.sortOrder,
      status: meetingFormData.status,
    };
    await MeetingScheduleAPI.updateMeeting(meetingFormData.meetingId, params);
    ElMessage.success("修改成功");
    closeMeetingDialog();
    fetchList();
  } finally {
    meetingSubmitLoading.value = false;
  }
}

function closeMeetingDialog(): void {
  meetingDialogVisible.value = false;
  meetingFormRef.value?.resetFields();
  meetingFormRef.value?.clearValidate();
}

/** 删除会议 */
function handleDeleteMeeting(meetingId: number): void {
  ElMessageBox.confirm("确认删除该会议及其所有板块、条目和参与人？此操作不可恢复！", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    loading.value = true;
    try {
      await MeetingScheduleAPI.deleteMeeting(meetingId);
      ElMessage.success("删除成功");
      fetchList();
    } finally {
      loading.value = false;
    }
  });
}

/** 条目时间合并单元格：同时间段的时间列合并，勾选"独立时间"的行不参与合并 */
function itemSpanMethod({ row, column, rowIndex, columnIndex }: { row: AgendaItemForm; column: any; rowIndex: number; columnIndex: number }): { rowspan: number; colspan: number } {
  // 时间列现在是第1列（index=1），第0列是复选框列（不合并）
  if (columnIndex === 1) {
    const items = formData.items || [];
    if (!items.length) return { rowspan: 1, colspan: 1 };

    // 勾选了"独立时间"的行，始终独立显示
    if (row._timeIndependent) return { rowspan: 1, colspan: 1 };

    const currentTime = row.timePeriod || "";
    // 前一行是独立时间行 或 时间不同 → 当前行是合并组的起始行
    const prevItem = items[rowIndex - 1];
    const prevTime = prevItem?.timePeriod || "";
    const prevIndependent = prevItem?._timeIndependent || false;

    if (rowIndex === 0 || prevIndependent || prevTime !== currentTime) {
      // 计算连续相同时间段且非独立时间的行数
      let span = 1;
      for (let i = rowIndex + 1; i < items.length; i++) {
        if ((items[i]?.timePeriod || "") === currentTime && !items[i]?._timeIndependent) {
          span++;
        } else {
          break;
        }
      }
      return { rowspan: span, colspan: 1 };
    }
    // 非起始行隐藏
    return { rowspan: 0, colspan: 0 };
  }
  return { rowspan: 1, colspan: 1 };
}

/** 条目类型变更时自动填充type/typeEn */
function onItemTypeChange(row: AgendaItemForm, val: string): void {
  const opt = ITEM_TYPE_OPTIONS.find((o) => o.value === val);
  if (opt) {
    row.type = opt.label;
    row.typeEn = opt.labelEn;
  }
}

/** 添加条目 */
function addItem(): void {
  formData.items = formData.items || [];
  formData.items.push({
    title: "",
    titleEn: "",
    itemType: "",
    timePeriod: "",
  timePeriodEn: "",
    speakerName: "",
    speakerNameEn: "",
    speakerOrg: "",
    speakerOrgEn: "",
    sortOrder: (formData.items?.length || 0) + 1,
    _timeIndependent: false,
  });
}

/** 移除条目 */
function removeItem(index: number): void {
  formData.items?.splice(index, 1);
}

/** 添加参与人 */
function addParticipant(): void {
  formData.participants = formData.participants || [];
  formData.participants.push({
    name: "",
    nameEn: "",
    title: "",
    titleEn: "",
    organization: "",
    organizationEn: "",
    sortOrder: (formData.participants?.length || 0) + 1,
  });
}

/** 移除参与人 */
function removeParticipant(index: number): void {
  formData.participants?.splice(index, 1);
}

/** 条目上移 */
function moveItemUp(index: number): void {
  if (index <= 0 || !formData.items) return;
  const items = formData.items;
  [items[index], items[index - 1]] = [items[index - 1], items[index]];
}

/** 条目下移 */
function moveItemDown(index: number): void {
  if (!formData.items || index >= formData.items.length - 1) return;
  const items = formData.items;
  [items[index], items[index + 1]] = [items[index + 1], items[index]];
}

/** 参与人上移 */
function moveParticipantUp(index: number): void {
  if (index <= 0 || !formData.participants) return;
  const participants = formData.participants;
  [participants[index], participants[index - 1]] = [participants[index - 1], participants[index]];
}

/** 参与人下移 */
function moveParticipantDown(index: number): void {
  if (!formData.participants || index >= formData.participants.length - 1) return;
  const participants = formData.participants;
  [participants[index], participants[index + 1]] = [participants[index + 1], participants[index]];
}

/** 板块上移（展开行内操作） */
async function handleMoveSessionUp(sessionIndex: number, meeting: MeetingScheduleVO): Promise<void> {
  if (sessionIndex <= 0 || !meeting.sessions) return;
  const sessions = meeting.sessions;
  // 交换sortOrder值
  const sortOrderA = sessions[sessionIndex].sortOrder;
  const sortOrderB = sessions[sessionIndex - 1].sortOrder;
  sessions[sessionIndex].sortOrder = sortOrderB;
  sessions[sessionIndex - 1].sortOrder = sortOrderA;
  // 交换数组位置
  [sessions[sessionIndex], sessions[sessionIndex - 1]] = [sessions[sessionIndex - 1], sessions[sessionIndex]];
  // 调用后端批量更新排序
  try {
    await MeetingScheduleAPI.batchUpdateSessionSortOrder([
      { id: sessions[sessionIndex].id, sortOrder: sessions[sessionIndex].sortOrder },
      { id: sessions[sessionIndex - 1].id, sortOrder: sessions[sessionIndex - 1].sortOrder },
    ]);
    ElMessage.success("排序已更新");
  } catch {
    ElMessage.error("排序更新失败");
    fetchList(); // 刷新恢复
  }
}

/** 板块下移（展开行内操作） */
async function handleMoveSessionDown(sessionIndex: number, meeting: MeetingScheduleVO): Promise<void> {
  if (!meeting.sessions || sessionIndex >= meeting.sessions.length - 1) return;
  const sessions = meeting.sessions;
  // 交换sortOrder值
  const sortOrderA = sessions[sessionIndex].sortOrder;
  const sortOrderB = sessions[sessionIndex + 1].sortOrder;
  sessions[sessionIndex].sortOrder = sortOrderB;
  sessions[sessionIndex + 1].sortOrder = sortOrderA;
  // 交换数组位置
  [sessions[sessionIndex], sessions[sessionIndex + 1]] = [sessions[sessionIndex + 1], sessions[sessionIndex]];
  // 调用后端批量更新排序
  try {
    await MeetingScheduleAPI.batchUpdateSessionSortOrder([
      { id: sessions[sessionIndex].id, sortOrder: sessions[sessionIndex].sortOrder },
      { id: sessions[sessionIndex + 1].id, sortOrder: sessions[sessionIndex + 1].sortOrder },
    ]);
    ElMessage.success("排序已更新");
  } catch {
    ElMessage.error("排序更新失败");
    fetchList(); // 刷新恢复
  }
}

/** 提交表单 */
async function handleSubmit(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitLoading.value = true;
  try {
    // 提交前自动按行顺序重算sortOrder（条目、参与人）
    (formData.items || []).forEach((item, idx) => { item.sortOrder = idx + 1; });
    (formData.participants || []).forEach((p, idx) => { p.sortOrder = idx + 1; });

    // 剥离 UI-only 字段，避免发送到后端
    const submitData = {
      ...formData,
      items: (formData.items || []).map(({ _timeIndependent, ...rest }) => rest),
    };
    if (submitData.id) {
      await MeetingScheduleAPI.updateSession(submitData.id, submitData);
      ElMessage.success("修改成功");
    } else {
      await MeetingScheduleAPI.createSession(submitData);
      ElMessage.success("新增成功");
    }
    closeDialog();
    fetchList();
  } finally {
    submitLoading.value = false;
  }
}

function closeDialog(): void {
  dialogVisible.value = false;
  resetForm();
}

function resetForm(): void {
  formRef.value?.resetFields();
  formRef.value?.clearValidate();
  formData.id = undefined;
  formData.meetingId = undefined;
  formData.title = "";
  formData.titleEn = "";
  formData.chairperson = "";
  formData.chairpersonEn = "";
  formData.panelists = "";
  formData.panelistsEn = "";
  formData.timePeriod = "";
  formData.timePeriodEn = "";
  formData.topicContent = "";
  formData.topicContentEn = "";
  formData.remark = "";
  formData.remarkEn = "";
  formData.itemType = "";
  formData.sortOrder = 1;
  formData.items = [];
  formData.participants = [];
}

onMounted(() => {
  handleQuery();
});
</script>

<style scoped lang="scss">
.en-text {
  color: #999;
  font-size: 12px;
}

.table-toolbar {
  margin-bottom: 10px;
}
</style>