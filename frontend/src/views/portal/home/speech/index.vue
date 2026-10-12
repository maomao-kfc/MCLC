<template>
  <div class="app-container">
    <!-- 搜索区域 -->
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

        <el-form-item prop="countryType" label="语言类型">
          <el-select v-model="queryParams.countryType" placeholder="全部" style="width: 120px">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
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
        <el-table-column label="姓名" prop="name" min-width="120" show-overflow-tooltip />
        <el-table-column label="职务" prop="position" min-width="120" show-overflow-tooltip />
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="个人图片" align="center" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.images"
              :src="scope.row.images"
              :preview-src-list="[scope.row.images]"
              :preview-teleported="true"
              fit="cover"
              style="width: 60px; height: 60px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="封面" align="center" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.cover"
              :src="scope.row.cover"
              :preview-src-list="[scope.row.cover]"
              :preview-teleported="true"
              fit="cover"
              style="width: 60px; height: 60px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">
              中文
            </el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">
              英文
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" width="80" prop="sort" />
        <el-table-column label="专访视频" align="center" width="120">
          <template #default="scope">
            <div
              v-if="scope.row.video"
              class="video-preview"
              @click="handlePreviewVideo(scope.row.video)"
            >
              <video
                :src="scope.row.video"
                muted
                style="width: 80px; height: 60px; object-fit: cover"
              />
              <div class="video-play-icon">
                <el-icon><VideoCamera /></el-icon>
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
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

    <!-- 视频预览弹窗 -->
    <el-dialog v-model="videoPreviewVisible" title="视频播放" width="800px" destroy-on-close>
      <video :src="currentVideoUrl" controls style="width: 100%; max-height: 500px" />
    </el-dialog>

    <!-- 表单弹窗 -->
    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="900px"
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="职务" prop="position">
              <el-input v-model="formData.position" placeholder="请输入职务" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="座右铭" prop="motto">
              <el-input v-model="formData.motto" placeholder="请输入医生座右铭" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="个人图片" prop="images" class="image-upload-item">
              <el-upload
                :file-list="imageList"
                class="avatar-uploader"
                :limit="1"
                :auto-upload="false"
                :on-change="handleImageChange"
                :on-remove="handleRemove"
                :disabled="isUploading"
                accept="image/*"
              >
                <div v-if="isUploading" class="uploading-mask">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>上传中...</span>
                </div>
                <img v-else-if="formData.images" :src="formData.images" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <el-icon
                v-if="formData.images && !isUploading"
                class="delete-icon"
                @click.stop="handleRemove"
              >
                <Close />
              </el-icon>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面" prop="cover" class="image-upload-item">
              <el-upload
                :file-list="coverList"
                class="avatar-uploader"
                :limit="1"
                :auto-upload="false"
                :on-change="handleCoverChange"
                :on-remove="handleCoverRemove"
                :disabled="isCoverUploading"
                accept="image/*"
              >
                <div v-if="isCoverUploading" class="uploading-mask">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>上传中...</span>
                </div>
                <img v-else-if="formData.cover" :src="formData.cover" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <el-icon
                v-if="formData.cover && !isCoverUploading"
                class="delete-icon"
                @click.stop="handleCoverRemove"
              >
                <Close />
              </el-icon>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="个人介绍" prop="introduction">
              <el-input
                v-model="formData.introduction"
                type="textarea"
                :rows="3"
                placeholder="请输入个人介绍"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="报告内容" prop="content">
              <QuillEditor
                ref="contentQuillEditorRef"
                v-model:content="formData.content"
                content-type="html"
                theme="snow"
                :toolbar="toolbarOptions"
                @ready="onContentReady"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="专访视频" prop="video" class="image-upload-item">
              <el-upload
                :file-list="videoList"
                class="avatar-uploader"
                :limit="1"
                :auto-upload="false"
                :on-change="handleVideoChange"
                :on-remove="handleVideoRemove"
                :disabled="isVideoUploading"
                accept="video/*"
              >
                <div v-if="isVideoUploading" class="uploading-mask">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>上传中...</span>
                </div>
                <div
                  v-else-if="formData.video"
                  class="avatar video-box"
                  @click.stop="handlePreviewVideo(formData.video)"
                >
                  <video
                    v-if="formData.video"
                    :src="formData.video"
                    muted
                    style="width: 100%; height: 100%; object-fit: cover"
                  />
                  <div v-else class="video-cover-empty">
                    <el-icon class="avatar-uploader-icon"><VideoCamera /></el-icon>
                  </div>
                  <div class="video-play-icon">
                    <el-icon><VideoCamera /></el-icon>
                  </div>
                </div>
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <el-icon
                v-if="formData.video && !isVideoUploading"
                class="delete-icon"
                @click.stop="handleVideoRemove"
              >
                <Close />
              </el-icon>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="参考文献" prop="reference">
              <QuillEditor
                ref="refQuillEditorRef"
                v-model:content="formData.reference"
                content-type="html"
                theme="snow"
                :toolbar="toolbarOptions"
                @ready="onRefReady"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="语言类型" prop="countryType">
              <el-select v-model="formData.countryType" placeholder="请选择语言类型">
                <el-option label="中文" value="ZH" />
                <el-option label="英文" value="EN" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="formData.sort" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, toRaw, nextTick } from "vue";
import { QuillEditor } from "@vueup/vue-quill";
import "@vueup/vue-quill/dist/vue-quill.snow.css";
import { ElMessage, ElMessageBox } from "element-plus";
import { Loading, Plus, Close, VideoCamera } from "@element-plus/icons-vue";
import FileAPI from "@/api/file";
import SpeechAPI from "@/api/portal/speech";
import type { GuestItem, GuestQueryParams } from "@/types/api";

let contentQuillInstance = null;
let refQuillInstance = null;

const loading = ref(false);
const total = ref(0);
const ids = ref<string[]>([]);
const tableData = ref<GuestItem[]>([]);
const queryFormRef = ref();
const formRef = ref<any>();
const isUploading = ref(false);
const isCoverUploading = ref(false);
const isVideoUploading = ref(false);
const introQuillEditorRef = ref();
const contentQuillEditorRef = ref();
const refQuillEditorRef = ref();

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: "",
  countryType: "ZH",
});

const dialogState = reactive({
  visible: false,
  title: "",
});

const formData = reactive<any>({
  id: undefined,
  name: "",
  position: "",
  title: "",
  motto: "",
  images: "",
  cover: "",
  introduction: "",
  content: "",
  video: "",
  reference: "",
  status: 1,
  sort: 0,
  countryType: "",
});

const imageList = ref<any[]>([]);
const coverList = ref<any[]>([]);
const videoList = ref<any[]>([]);

const rules = computed(() => ({
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  position: [{ required: true, message: "请输入职务", trigger: "blur" }],
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  motto: [{ required: true, message: "请输入座右铭", trigger: "blur" }],
  images: [{ required: true, message: "请上传个人图片", trigger: "change" }],
  cover: [{ required: true, message: "请上传封面", trigger: "change" }],
  introduction: [{ required: true, message: "请输入个人介绍", trigger: "blur" }],
  content: [{ required: true, message: "请输入报告内容", trigger: ["change", "blur"] }],
  reference: [{ required: true, message: "请输入参考文献", trigger: ["change", "blur"] }],
  video: [{ required: false, message: "请上传专访视频", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
}));

const toolbarOptions = {
  container: [
    ["bold", "italic", "underline", "strike"],
    ["blockquote", "code-block"],
    [{ header: [1, 2, 3, false] }],
    [{ list: "ordered" }, { list: "bullet" }],
    [{ indent: "-1" }, { indent: "+1" }],
    [{ direction: "rtl" }],
    [{ color: [] }, { background: [] }],
    [{ font: [] }, { align: [] }],
    ["image"],
    ["clean"],
  ],
};

const handleEditorImageUpload = async (quill: any) => {
  if (!quill) return;
  const input = document.createElement("input");
  input.setAttribute("type", "file");
  input.setAttribute("accept", "image/*");
  input.click();
  input.onchange = async () => {
    const file = input.files?.[0];
    if (!file) return;
    try {
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      const range = quill.getSelection();
      quill.insertEmbed(range.index, "image", imageUrl);
      ElMessage.success("图片上传成功");
    } catch {
      ElMessage.error("图片上传失败");
    }
    input.remove();
  };
};

const onContentReady = (quill: any) => {
  contentQuillInstance = quill;
  const toolbar = quill.getModule("toolbar");
  toolbar.addHandler("image", () => {
    handleEditorImageUpload(quill);
  });
};

const onRefReady = (quill: any) => {
  refQuillInstance = quill;
  const toolbar = quill.getModule("toolbar");
  toolbar.addHandler("image", () => {
    handleEditorImageUpload(quill);
  });
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

async function handleImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (file) {
    try {
      isUploading.value = true;
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      formData.images = imageUrl;
      imageList.value = [{ name: file.name, url: imageUrl }];
    } catch {
      ElMessage.error("上传失败");
      imageList.value = [];
    } finally {
      isUploading.value = false;
    }
  }
}

function handleRemove() {
  formData.images = "";
  imageList.value = [];
}

async function handleCoverChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (file) {
    try {
      isCoverUploading.value = true;
      const res = await FileAPI.uploadFile(file);
      const imageUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!imageUrl) {
        throw new Error("上传失败");
      }
      formData.cover = imageUrl;
      coverList.value = [{ name: file.name, url: imageUrl }];
    } catch {
      ElMessage.error("上传失败");
      coverList.value = [];
    } finally {
      isCoverUploading.value = false;
    }
  }
}

function handleCoverRemove() {
  formData.cover = "";
  coverList.value = [];
}

async function handleVideoChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (file) {
    try {
      isVideoUploading.value = true;
      const res = await FileAPI.uploadFile(file);
      const videoUrl = Array.isArray(res) ? res[0]?.url || res[0] : res.url;
      if (!videoUrl) {
        throw new Error("上传失败");
      }
      formData.video = videoUrl;
      videoList.value = [{ name: file.name, url: videoUrl }];

      const coverUrl = await extractVideoCover(file);
      if (coverUrl) {
        // formData.cover = coverUrl;
        coverList.value = [{ name: "视频封面", url: coverUrl }];
      }
    } catch {
      ElMessage.error("上传失败");
      videoList.value = [];
    } finally {
      isVideoUploading.value = false;
    }
  }
}

async function extractVideoCover(file: File): Promise<string> {
  return new Promise((resolve) => {
    const video = document.createElement("video");
    video.preload = "metadata";
    video.muted = true;
    video.playsInline = true;
    video.autoplay = false;

    const url = URL.createObjectURL(file);
    video.src = url;

    video.onloadeddata = () => {
      video.currentTime = 1;
    };

    video.onseeked = () => {
      const canvas = document.createElement("canvas");
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;

      const ctx = canvas.getContext("2d");
      if (ctx) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const coverUrl = canvas.toDataURL("image/jpeg", 0.8);
        URL.revokeObjectURL(url);
        resolve(coverUrl);
      } else {
        URL.revokeObjectURL(url);
        resolve("");
      }
    };

    video.onerror = () => {
      URL.revokeObjectURL(url);
      resolve("");
    };
  });
}

function handleVideoRemove() {
  formData.video = "";
  videoList.value = [];
}

const fetchList = async () => {
  loading.value = true;
  try {
    const data = await SpeechAPI.getPage(queryParams as GuestQueryParams);
    tableData.value = data.list || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleCreateClick = async () => {
  dialogState.title = "新增";
  dialogState.visible = true;
  imageList.value = [];
  coverList.value = [];
  videoList.value = [];
  Object.assign(formData, {
    id: undefined,
    name: "",
    position: "",
    title: "",
    motto: "",
    images: "",
    cover: "",
    introduction: "",
    content: "",
    video: "",
    reference: "",
    status: 1,
    sort: 0,
    countryType: "ZH",
  });
  await nextTick();
  formRef.value?.resetFields();
  if (contentQuillInstance) {
    contentQuillInstance.setContents([]);
  }
  if (refQuillInstance) {
    refQuillInstance.setContents([]);
  }
};

const handleEditClick = async (row: GuestItem) => {
  dialogState.title = "编辑";
  dialogState.visible = true;
  const res = await SpeechAPI.getDetail(row.id as string);

  Object.assign(formData, { ...row, ...res });
  if (row.images) {
    imageList.value = [{ name: "", url: row.images }];
  } else {
    imageList.value = [];
  }
  if (row.cover) {
    coverList.value = [{ name: "", url: row.cover }];
  } else {
    coverList.value = [];
  }
  if (row.video) {
    videoList.value = [{ name: "", url: row.video }];
  } else {
    videoList.value = [];
  }
};

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm("确认删除该数据?", "警告", {
      type: "warning",
    });
    await SpeechAPI.deleteByIds(id);
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.log(e);
  }
};

const handleBatchDelete = async () => {
  if (ids.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 条数据?`, "警告", {
      type: "warning",
    });
    await SpeechAPI.deleteByIds(ids.value.join(","));
    ElMessage.success("删除成功");
    fetchList();
  } catch (e) {
    console.log(e);
  }
};

const submitForm = async () => {
  try {
    const valid = await formRef.value?.validate();
    const submitData = JSON.parse(JSON.stringify(formData));
    if (formData.id) {
      await SpeechAPI.update(formData.id, submitData);
    } else {
      await SpeechAPI.create(submitData);
    }
    ElMessage.success("保存成功");
    closeDialog();
    fetchList();
  } catch (e: any) {
    if (e === false) {
      return;
    }
    console.error("提交失败:", e);
    ElMessage.error(e.message || "保存失败");
  }
};

const closeDialog = () => {
  dialogState.visible = false;
  formRef.value?.resetFields();
  imageList.value = [];
  coverList.value = [];
  videoList.value = [];
  isUploading.value = false;
  isCoverUploading.value = false;
  isVideoUploading.value = false;
};

const videoPreviewVisible = ref(false);
const currentVideoUrl = ref("");

const handlePreviewVideo = (url: string) => {
  currentVideoUrl.value = url;
  videoPreviewVisible.value = true;
};

fetchList();
</script>

<style scoped>
:deep(.ql-container) {
  width: 100%;
  height: inherit;
}
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
.video-preview {
  position: relative;
  cursor: pointer;
  display: inline-block;
}
.video-play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  color: #fff;
  pointer-events: none;
}
</style>
