<template>
  <div class="app-container">
    <div class="filter-section">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="字段名称" prop="fieldName">
          <el-input
            v-model="queryParams.fieldName"
            placeholder="字段名称"
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
            @click="handleDelete()"
          >
            删除
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        highlight-current-row
        :data="tableData"
        border
        class="table-section__content"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="字段名称" prop="fieldName" />
        <el-table-column label="常规有效期" prop="normalValidDate">
          <template #default="scope">
            {{ scope.row.normalValidDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="截止日期" prop="deadlineDate" />
        <el-table-column label="常规有效期金额" prop="normalAmount">
          <template #default="scope">
            ¥{{ scope.row.normalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="截止日期金额" prop="deadlineAmount">
          <template #default="scope">
            ¥{{ scope.row.deadlineAmount }}
          </template>
        </el-table-column>
        <el-table-column label="语言类型" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.countryType === 'ZH' || scope.row.countryType === 'zh'">中文</el-tag>
            <el-tag v-else-if="scope.row.countryType === 'EN' || scope.row.countryType === 'en'">英文</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column fixed="right" label="操作" align="center" width="150">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              icon="edit"
              @click.stop="handleEditClick(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              icon="delete"
              @click.stop="handleDelete(scope.row.id)"
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
        @pagination="fetchData"
      />
    </el-card>

    <el-dialog
      v-model="dialogState.visible"
      :title="dialogState.title"
      width="600px"
      @close="closeDialog"
    >
      <el-form ref="dataFormRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="字段名称" prop="fieldName">
          <el-input v-model="formData.fieldName" placeholder="请输入字段名称" />
        </el-form-item>

        <el-form-item label="常规有效期" prop="normalValidDate">
          <el-date-picker
            v-model="formData.normalValidDate"
            type="date"
            placeholder="选择常规有效期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="截至日期" prop="deadlineDate">
          <el-date-picker
            v-model="formData.deadlineDate"
            type="date"
            placeholder="选择截至日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="常规有效期金额" prop="normalAmount">
          <el-input-number
            v-model="formData.normalAmount"
            :min="0"
            :precision="2"
            placeholder="请输入常规有效期金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="截止日期金额" prop="deadlineAmount">
          <el-input-number
            v-model="formData.deadlineAmount"
            :min="0"
            :precision="2"
            placeholder="请输入截止日期金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="语言类型" prop="countryType">
          <el-select v-model="formData.countryType" placeholder="请选择语言类型">
            <el-option label="中文" value="ZH" />
            <el-option label="英文" value="EN" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            placeholder="请输入排序"
            style="width: 100%"
          />
        </el-form-item>

<!--        <el-form-item label="备注">-->
<!--          <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />-->
<!--        </el-form-item>-->
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
          <el-button @click="closeDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import RegistrationTypeAPI from "@/api/system/registrationType";
defineOptions({
  name: "Registration",
  inheritAttrs: false,
});

import type { FormInstance, FormRules } from "element-plus";
import type {
  RegistrationTypeItem,
  RegistrationTypeQueryParams,
  RegistrationTypeForm,
} from "@/types/api";

const queryFormRef = ref<FormInstance>();
const dataFormRef = ref<FormInstance>();

const queryParams = reactive<RegistrationTypeQueryParams>({
  pageNum: 1,
  pageSize: 10,
  fieldName: "",
  countryType: "ZH",
});

const tableData = ref<RegistrationTypeItem[]>();

const total = ref(0);
const loading = ref(false);
const submitting = ref(false);
const ids = ref<(number | string)[]>([]);

const dialogState = reactive({
  title: "",
  visible: false,
});

const formData = reactive<RegistrationTypeForm>({
  fieldName: "",
  normalAmount: 0,
  deadlineAmount: 0,
  countryType: "",
});

const rules: FormRules = {
  fieldName: [{ required: true, message: "请输入字段名称", trigger: "blur" }],
  deadlineDate: [{ required: true, message: "请选择截至日期", trigger: "change" }],
  normalAmount: [{ required: true, message: "请输入常规有效期金额", trigger: "blur" }],
  deadlineAmount: [{ required: true, message: "请输入截止日期金额", trigger: "blur" }],
};

function fetchData(): void {
  loading.value = true;
  RegistrationTypeAPI.getPage(queryParams)
    .then((data) => {
      tableData.value = data.list;
      total.value = data.total ?? 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

function handleQuery(): void {
  queryParams.pageNum = 1;
  fetchData();
}

function handleResetQuery(): void {
  queryFormRef.value?.resetFields();
  queryParams.pageNum = 1;
  fetchData();
}

function handleSelectionChange(selection: RegistrationTypeItem[]): void {
  ids.value = selection.map((item) => item.id);
}

function handleCreateClick(): void {
  resetForm();
  dialogState.visible = true;
  dialogState.title = "新增注册类别";
}

function handleEditClick(row: any): void {
  resetForm();
  dialogState.visible = true;
  dialogState.title = "修改注册类别";
  Object.assign(formData, row);
}

function resetForm(): void {
  dataFormRef.value?.clearValidate();
  formData.id = undefined;
  formData.fieldName = "";
  formData.normalValidDate = undefined;
  formData.deadlineDate = undefined;
  formData.normalAmount = 0;
  formData.deadlineAmount = 0;
  formData.sort = undefined;
  formData.remark = undefined;
  formData.countryType = "";
}

function handleSubmit(): void {
  if (submitting.value) return;
  dataFormRef.value?.validate((isValid) => {
    if (isValid) {
      submitting.value = true;
      const id = formData.id;
      if (id) {
        RegistrationTypeAPI.update(id, formData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            handleQuery();
          })
          .finally(() => (submitting.value = false));
      } else {
        RegistrationTypeAPI.create(formData)
          .then(() => {
            ElMessage.success("新增成功");
            closeDialog();
            handleQuery();
          })
          .finally(() => (submitting.value = false));
      }
    }
  });
}

function closeDialog(): void {
  dialogState.visible = false;
  dataFormRef.value?.resetFields();
  resetForm();
}

function handleDelete(id?: number): void {
  const deleteIds = [id || ids.value].join(",");
  if (!deleteIds) {
    ElMessage.warning("请勾选删除项");
    return;
  }
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(
    () => {
      RegistrationTypeAPI.deleteByIds(deleteIds).then(() => {
        ElMessage.success("删除成功");
        handleResetQuery();
      });
    },
    () => {
      ElMessage.info("已取消删除");
    }
  );
}

onMounted(() => {
  fetchData();
});
</script>
