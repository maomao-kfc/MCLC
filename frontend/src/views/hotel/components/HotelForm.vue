<!-- 酒店弹窗 -->

<template>
  <el-dialog v-model="dialogVisible" :title="title" width="600px" @close="handleClose">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="酒店名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入酒店名称" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="formData.phone" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="地址" prop="address">
        <el-input v-model="formData.address" placeholder="请输入地址" />
      </el-form-item>
      <el-form-item label="详情介绍" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入详情介绍"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">已取消合作</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { addHotel, updateHotel, getHotelDetail } from "@/api/hotel/hotel";
import type { Hotel } from "../types";

interface Props {
  visible: boolean;
  title: string;
  formData: Partial<Hotel>;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
  (e: "success"): void;
}>();

const dialogVisible = ref(props.visible);
const loading = ref(false);
const formRef = ref<FormInstance>();

// 内部表单数据，初始化时合并传入的 formData
const formData = reactive<Partial<Hotel>>({
  name: "",
  phone: "",
  address: "",
  description: "",
  status: 1,
});

// 监听 visible 变化同步至父组件
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val;
    if (val) {
      // 弹窗打开时，如果有 id 则请求详情
      if (props.formData.id) {
        fetchDetail(props.formData.id);
      } else {
        // 新增场景，重置为传入的默认值
        Object.assign(
          formData,
          { name: "", phone: "", address: "", description: "", status: 1 },
          props.formData
        );
      }
    }
  },
  { immediate: true }
);

watch(dialogVisible, (val) => {
  emit("update:visible", val);
});

// 表单校验规则
const rules: FormRules = {
  name: [{ required: true, message: "请输入酒店名称", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
};

/**
 * 获取详情数据（编辑时）
 */
async function fetchDetail(id: number) {
  loading.value = true;
  try {
    const { data } = await getHotelDetail(id);
    Object.assign(formData, data);
  } finally {
    loading.value = false;
  }
}

/**
 * 提交表单
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    if (formData.id) {
      await updateHotel(formData.id, formData);
      ElMessage.success("修改成功");
    } else {
      await addHotel(formData);
      ElMessage.success("新增成功");
    }
    emit("success");
  } finally {
    loading.value = false;
  }
}

/**
 * 关闭弹窗时重置表单
 */
function handleClose() {
  formRef.value?.resetFields();
  // 清空临时 id 防止下次打开误判
  formData.id = undefined;
}
</script>
