<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="title" label="会议主题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入会议主题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="countryType" label="语言类型">
          <el-select v-model="queryParams.countryType" placeholder="全部" style="width: 120px">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
          </el-select>
        </el-form-item>

        <el-form-item prop="sponsor" label="主办方">
          <el-input
            v-model="queryParams.sponsor"
            placeholder="请输入主办方"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="organizer" label="承办方">
          <el-input
            v-model="queryParams.organizer"
            placeholder="请输入承办方"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="registrationEnabled" label="开启注册">
          <el-select v-model="queryParams.registrationEnabled" placeholder="全部" style="width: 120px" clearable>
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item prop="scheduleEnabled" label="开启日程">
          <el-select v-model="queryParams.scheduleEnabled" placeholder="全部" style="width: 120px" clearable>
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
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
          <el-button type="success" icon="plus" @click="handleCreateClick()">新增</el-button>
          <el-button
            type="danger"
            :disabled="ids.length === 0"
            icon="delete"
            @click="handleBatchDelete()"
          >
            删除
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
        <el-table-column label="会议主题" prop="title" width="150" show-overflow-tooltip />
        <el-table-column label="开始时间" align="center" width="160">
          <template #default="scope">
            {{ scope.row.startTmie ? formatDate(scope.row.startTmie) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center" width="160">
          <template #default="scope">
            {{ scope.row.endTmie ? formatDate(scope.row.endTmie) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="常规注册开始" align="center" width="160">
          <template #default="scope">
            {{ scope.row.generalRegStart ? formatDate(scope.row.generalRegStart) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="常规注册结束" align="center" width="160">
          <template #default="scope">
            {{ scope.row.generalRegEnd ? formatDate(scope.row.generalRegEnd) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="早鸟优惠注册 & 学生注册开始" align="center" width="160">
          <template #default="scope">
            {{ scope.row.earlyBirdRegStart ? formatDate(scope.row.earlyBirdRegStart) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="早鸟优惠注册 & 学生注册结束" align="center" width="160">
          <template #default="scope">
            {{ scope.row.earlyBirdRegEnd ? formatDate(scope.row.earlyBirdRegEnd) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="会议举办开始" align="center" width="160">
          <template #default="scope">
            {{ scope.row.conferenceDateStart ? formatDate(scope.row.conferenceDateStart) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="会议举办结束" align="center" width="160">
          <template #default="scope">
            {{ scope.row.conferenceDateEnd ? formatDate(scope.row.conferenceDateEnd) : "-" }}
          </template>
        </el-table-column>
        <el-table-column label="主题图片" align="center" min-width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.titleImage"
              :src="scope.row.titleImage"
              :preview-src-list="[scope.row.titleImage]"
              :preview-teleported="true"
              fit="cover"
              style="width: 60px; height: 60px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="会前图片" align="center" min-width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.hqapImage"
              :src="scope.row.hqapImage"
              :preview-src-list="[scope.row.hqapImage]"
              :preview-teleported="true"
              fit="cover"
              style="width: 60px; height: 60px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">中文</el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">英文</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="主办方" prop="sponsor" min-width="120" show-overflow-tooltip />
        <el-table-column label="承办方" prop="organizer" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开启注册" align="center" width="90">
          <template #default="scope">
            <el-tag v-if="scope.row.registrationEnabled === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会议日程名称" prop="scheduleTitle" min-width="150" show-overflow-tooltip />
        <el-table-column label="会议日程描述" prop="scheduleDescription" min-width="180" show-overflow-tooltip />
        <el-table-column label="是否展示日程" align="center" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.showSchedule === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否开启日程" align="center" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.scheduleEnabled === 1" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" width="80" prop="sort" />

        <el-table-column fixed="right" label="操作" width="280">
          <template #default="scope">
            <el-button
              type="info"
              size="small"
              link
              icon="view"
              @click="handleQueryClick(scope.row)"
            >
              查询
            </el-button>
            <el-button
              type="primary"
              size="small"
              link
              icon="edit"
              @click="handleEditClick(scope.row)"
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

    <!-- 表单弹窗 -->
    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="700px"
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="会议主题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入会议主题" />
        </el-form-item>
        <el-form-item label="会议开始时间" prop="startTmie">
          <el-date-picker
            v-model="formData.startTmie"
            type="datetime"
            placeholder="请选择会议开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会议结束时间" prop="endTmie">
          <el-date-picker
            v-model="formData.endTmie"
            type="datetime"
            placeholder="请选择会议结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="常规注册开始时间" prop="generalRegStart">
          <el-date-picker
            v-model="formData.generalRegStart"
            type="datetime"
            placeholder="请选择常规注册开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="常规注册结束时间" prop="generalRegEnd">
          <el-date-picker
            v-model="formData.generalRegEnd"
            type="datetime"
            placeholder="请选择常规注册结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="早鸟优惠注册 & 学生注册开始时间" prop="earlyBirdRegStart">
          <el-date-picker
            v-model="formData.earlyBirdRegStart"
            type="datetime"
            placeholder="请选择早鸟优惠注册 & 学生注册开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="早鸟优惠注册 & 学生注册结束时间" prop="earlyBirdRegEnd">
          <el-date-picker
            v-model="formData.earlyBirdRegEnd"
            type="datetime"
            placeholder="请选择早鸟优惠注册 & 学生注册结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会议举办开始时间" prop="conferenceDateStart">
          <el-date-picker
            v-model="formData.conferenceDateStart"
            type="datetime"
            placeholder="请选择会议举办开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会议举办结束时间" prop="conferenceDateEnd">
          <el-date-picker
            v-model="formData.conferenceDateEnd"
            type="datetime"
            placeholder="请选择会议举办结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="主题图片" prop="titleImage" class="image-upload-item">
          <el-upload
            :file-list="titleImageList"
            class="avatar-uploader"
            :limit="1"
            :auto-upload="false"
            :on-change="handleTitleImageChange"
            :on-remove="handleTitleImageRemove"
            :disabled="isTitleImageUploading"
            accept="image/*,.svg,image/svg+xml"
          >
            <div v-if="isTitleImageUploading" class="uploading-mask">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
            <img v-else-if="formData.titleImage" :src="formData.titleImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-icon
            v-if="formData.titleImage && !isTitleImageUploading"
            class="delete-icon"
            @click.stop="handleTitleImageRemove"
          >
            <Close />
          </el-icon>
        </el-form-item>
        <el-form-item label="会前安排图片" prop="hqapImage" class="image-upload-item">
          <el-upload
            :file-list="hqapImageList"
            class="avatar-uploader"
            :limit="1"
            :auto-upload="false"
            :on-change="handleHqapImageChange"
            :on-remove="handleHqapImageRemove"
            :disabled="isHqapImageUploading"
            accept="image/*,.svg,image/svg+xml"
          >
            <div v-if="isHqapImageUploading" class="uploading-mask">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
            <img v-else-if="formData.hqapImage" :src="formData.hqapImage" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <el-icon
            v-if="formData.hqapImage && !isHqapImageUploading"
            class="delete-icon"
            @click.stop="handleHqapImageRemove"
          >
            <Close />
          </el-icon>
        </el-form-item>
        <el-form-item label="语言类型" prop="countryType">
          <el-select v-model="formData.countryType" placeholder="请选择语言类型">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
          </el-select>
        </el-form-item>
        <el-form-item label="主办方" prop="sponsor">
          <el-input v-model="formData.sponsor" placeholder="请输入主办方" />
        </el-form-item>
        <el-form-item label="承办方" prop="organizer">
          <el-input v-model="formData.organizer" placeholder="请输入承办方" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="会议日程名称" prop="scheduleTitle">
          <el-input v-model="formData.scheduleTitle" placeholder="请输入会议日程名称" />
        </el-form-item>
        <el-form-item label="会议日程描述" prop="scheduleDescription">
          <el-input v-model="formData.scheduleDescription" placeholder="请输入会议日程描述" />
        </el-form-item>
        <el-form-item label="是否展示日程" prop="showSchedule">
          <el-radio-group v-model="formData.showSchedule">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否开启注册" prop="registrationEnabled">
          <el-radio-group v-model="formData.registrationEnabled">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否开启日程" prop="scheduleEnabled">
          <el-radio-group v-model="formData.scheduleEnabled">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查询详情弹窗 -->
    <el-dialog
      v-model="queryDialogVisible"
      title="查询详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会议主题">{{ queryData.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="语言类型">
          <el-tag v-if="queryData.countryType === 'ZH' || queryData.countryType === 'zh'">中文</el-tag>
          <el-tag v-else-if="queryData.countryType === 'EN' || queryData.countryType === 'en'">英文</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="会议开始时间">{{ queryData.startTmie ? formatDate(queryData.startTmie) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="会议结束时间">{{ queryData.endTmie ? formatDate(queryData.endTmie) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="常规注册开始时间">{{ queryData.generalRegStart ? formatDate(queryData.generalRegStart) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="常规注册结束时间">{{ queryData.generalRegEnd ? formatDate(queryData.generalRegEnd) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="早鸟优惠注册 & 学生注册开始时间">{{ queryData.earlyBirdRegStart ? formatDate(queryData.earlyBirdRegStart) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="早鸟优惠注册 & 学生注册结束时间">{{ queryData.earlyBirdRegEnd ? formatDate(queryData.earlyBirdRegEnd) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="会议举办开始时间">{{ queryData.conferenceDateStart ? formatDate(queryData.conferenceDateStart) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="会议举办结束时间">{{ queryData.conferenceDateEnd ? formatDate(queryData.conferenceDateEnd) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="主办方">{{ queryData.sponsor || '-' }}</el-descriptions-item>
        <el-descriptions-item label="承办方">{{ queryData.organizer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="queryData.status === 1" type="success">正常</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否开启注册">
          <el-tag v-if="queryData.registrationEnabled === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否开启日程">
          <el-tag v-if="queryData.scheduleEnabled === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="会议日程名称">{{ queryData.scheduleTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会议日程描述">{{ queryData.scheduleDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ queryData.sort ?? 0 }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="queryDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Loading, Plus, Close } from "@element-plus/icons-vue";
import FileAPI from "@/api/file";
import SettingAPI from "@/api/portal/setting";
import type { GuestItem, GuestQueryParams } from "@/types/api";

const loading = ref(false);
const total = ref(0);
const ids = ref<string[]>([]);
const tableData = ref<GuestItem[]>([]);
const queryFormRef = ref();
const formRef = ref<any>();
const isTitleImageUploading = ref(false);
const isHqapImageUploading = ref(false);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: "",
  countryType: "ZH",
  sponsor: "",
  organizer: "",
  registrationEnabled: undefined as number | undefined,
  scheduleEnabled: undefined as number | undefined,
});

const dialogState = reactive({
  visible: false,
  title: "",
});

const queryDialogVisible = ref(false);
const queryData = ref<any>({});

const formData = reactive<any>({
  id: undefined,
  title: "",
  startTmie: null,
  endTmie: null,
  generalRegStart: null,
  generalRegEnd: null,
  earlyBirdRegStart: null,
  earlyBirdRegEnd: null,
  conferenceDateStart: null,
  conferenceDateEnd: null,
  titleImage: "",
  hqapImage: "",
  status: 1,
  sort: 0,
  countryType: "",
  sponsor: "",
  organizer: "",
  scheduleTitle: "",
  scheduleDescription: "",
  showSchedule: 1,
  registrationEnabled: 1,
  scheduleEnabled: 1,
});

const titleImageList = ref<any[]>([]);
const hqapImageList = ref<any[]>([]);

const rules = {
  title: [{ required: true, message: "请输入会议主题", trigger: "blur" }],
  startTmie: [{ required: true, message: "请选择会议开始时间", trigger: "change" }],
  endTmie: [{ required: true, message: "请选择会议结束时间", trigger: "change" }],
  generalRegStart: [{ required: true, message: "请选择常规注册开始时间", trigger: "change" }],
  generalRegEnd: [{ required: true, message: "请选择常规注册结束时间", trigger: "change" }],
  earlyBirdRegStart: [{ required: true, message: "请选择早鸟优惠注册 & 学生注册开始时间", trigger: "change" }],
  earlyBirdRegEnd: [{ required: true, message: "请选择早鸟优惠注册 & 学生注册结束时间", trigger: "change" }],
  conferenceDateStart: [{ required: true, message: "请选择会议举办开始时间", trigger: "change" }],
  conferenceDateEnd: [{ required: true, message: "请选择会议举办结束时间", trigger: "change" }],
  titleImage: [{ required: true, message: "请上传主题图片", trigger: "change" }],
  hqapImage: [{ required: true, message: "请上传会前安排图片", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  scheduleTitle: [{ required: true, message: "请输入会议日程名称", trigger: "blur" }],
  scheduleDescription: [{ required: true, message: "请输入会议日程描述", trigger: "blur" }],
};

const formatDate = (date: string) => {
  if (!date) return "";
  return date.replace("T", " ").substring(0, 19);
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
  ids.value = selection.map((item: any) => item.id);
};

async function handleTitleImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (file) {
    try {
      isTitleImageUploading.value = true;
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      formData.titleImage = imageUrl;
      titleImageList.value = [{ name: file.name, url: imageUrl }];
    } catch {
      ElMessage.error("上传失败");
      titleImageList.value = [];
    } finally {
      isTitleImageUploading.value = false;
    }
  }
}

function handleTitleImageRemove() {
  formData.titleImage = "";
  titleImageList.value = [];
}

async function handleHqapImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (file) {
    try {
      isHqapImageUploading.value = true;
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      formData.hqapImage = imageUrl;
      hqapImageList.value = [{ name: file.name, url: imageUrl }];
    } catch {
      ElMessage.error("上传失败");
      hqapImageList.value = [];
    } finally {
      isHqapImageUploading.value = false;
    }
  }
}

function handleHqapImageRemove() {
  formData.hqapImage = "";
  hqapImageList.value = [];
}

const fetchList = async () => {
  loading.value = true;
  try {
    const data = await SettingAPI.getPage(queryParams as GuestQueryParams);
    tableData.value = data.list || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleCreateClick = () => {
  dialogState.title = "新增";
  dialogState.visible = true;
  titleImageList.value = [];
  hqapImageList.value = [];
  Object.assign(formData, {
    id: undefined,
    title: "",
    startTmie: null,
    endTmie: null,
    generalRegStart: null,
    generalRegEnd: null,
    earlyBirdRegStart: null,
    earlyBirdRegEnd: null,
    conferenceDateStart: null,
    conferenceDateEnd: null,
    titleImage: "",
    hqapImage: "",
    status: 1,
    sort: 0,
    countryType: "",
    sponsor: "",
    organizer: "",
    scheduleTitle: "",
    scheduleDescription: "",
    showSchedule: 1,
    registrationEnabled: 1,
    scheduleEnabled: 1,
  });
};

const handleEditClick = async (row: GuestItem) => {
  dialogState.title = "编辑";
  dialogState.visible = true;
  Object.assign(formData, row);
  if (row.titleImage) {
    titleImageList.value = [{ name: "", url: row.titleImage }];
  } else {
    titleImageList.value = [];
  }
  if (row.hqapImage) {
    hqapImageList.value = [{ name: "", url: row.hqapImage }];
  } else {
    hqapImageList.value = [];
  }
};

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm("确认删除该数据?", "警告", {
      type: "warning",
    });
    await SettingAPI.deleteByIds(id);
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.log(e);
  }
};

const handleQueryClick = (row: GuestItem) => {
  queryData.value = JSON.parse(JSON.stringify(row));
  queryDialogVisible.value = true;
};

const handleBatchDelete = async () => {
  if (ids.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 条数据?`, "警告", {
      type: "warning",
    });
    await SettingAPI.deleteByIds(ids.value.join(","));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.log(e);
  }
};

const submitForm = async () => {
  try {
    const valid = await formRef.value?.validate().catch(() => false);
    if (!valid) {
      ElMessage.warning("请检查表单填写");
      return;
    }
    console.log(formData.id, "formData.id");
    const submitData = JSON.parse(JSON.stringify(formData));
    if (formData.id) {
      await SettingAPI.update(formData.id, submitData);
    } else {
      await SettingAPI.create(submitData);
    }
    ElMessage.success("保存成功");
    closeDialog();
    fetchList();
  } catch (e: any) {
    console.error("提交失败:", e);
    ElMessage.error(e.message || "保存失败");
  }
};

const closeDialog = () => {
  dialogState.visible = false;
  formRef.value?.resetFields();
  titleImageList.value = [];
  hqapImageList.value = [];
  isTitleImageUploading.value = false;
  isHqapImageUploading.value = false;
};

fetchList();
</script>

<style scoped>
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #8c939d;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.image-upload-item {
  position: relative;
}
:deep(.avatar-uploader .el-upload) {
  width: 100% !important;
  height: 100% !important;
}
.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.uploading-mask {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--el-color-primary);
}
.is-loading {
  font-size: 20px;
  animation: rotation 1s linear infinite;
}
@keyframes rotation {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.delete-icon {
  position: absolute;
  top: -6px;
  right: -6px;
  z-index: 1;
  font-size: 14px;
  color: var(--el-color-danger);
  cursor: pointer;
}
</style>
