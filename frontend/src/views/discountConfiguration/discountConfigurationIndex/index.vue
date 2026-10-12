<template>
  <div class="app-container">
    <el-card shadow="hover">
      <el-form ref="formRef" :model="formData" label-width="100px">
        <el-form-item
          v-for="(item, index) in formData.list"
          :key="index"
          :label="'规则' + (index + 1)"
          :prop="'list.' + index"
        >
          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap; width: 100%">
            <el-form-item
              :prop="'list.' + index + '.minPeople'"
              :rules="[{ required: true, message: '请输入', trigger: 'blur' }]"
              label-width="0"
              style="margin-bottom: 0"
            >
              <el-input-number
                v-model="item.minPeople"
                :min="1"
                placeholder="最少人数"
                controls-position="right"
                style="width: 130px"
              />
            </el-form-item>
            <span style="color: #999; margin: 0 4px">~</span>
            <el-form-item
              :prop="'list.' + index + '.maxPeople'"
              :rules="[{ required: true, message: '请输入', trigger: 'blur' }]"
              label-width="0"
              style="margin-bottom: 0"
            >
              <el-input-number
                v-model="item.maxPeople"
                :min="1"
                placeholder="最多人数"
                controls-position="right"
                style="width: 130px"
              />
            </el-form-item>
            <el-form-item
              :prop="'list.' + index + '.discount'"
              :rules="[
                { required: true, message: '请输入', trigger: 'blur' },
                {
                  type: 'number',
                  min: 0.1,
                  max: 10,
                  message: '请输入0.1~10之间的折扣',
                  trigger: 'blur',
                },
              ]"
              label="折扣"
              label-width="60px"
              style="margin-bottom: 0"
            >
              <el-input-number
                v-model="item.discount"
                :min="0.1"
                :max="10"
                :precision="1"
                :step="0.1"
                placeholder="几折"
                controls-position="right"
                style="width: 120px"
              />
            </el-form-item>
            <el-button
              v-if="formData.list.length > 1"
              type="danger"
              icon="delete"
              circle
              size="small"
              @click="removeRow(index)"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" link @click="addRow">+ 添加规则</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import DiscountConfigurationAPI from "@/api/portal/discountConfiguration";

const formRef = ref<any>();

const formData = reactive({
  list: [
    {
      minPeople: null as number | null,
      maxPeople: null as number | null,
      discount: null as number | null,
    },
  ],
});

const addRow = () => {
  formData.list.push({ minPeople: null, maxPeople: null, discount: null });
};

const removeRow = (index: number) => {
  if (formData.list.length > 1) {
    formData.list.splice(index, 1);
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    await DiscountConfigurationAPI.saveAll({ list: formData.list as any });
    ElMessage.success("保存成功");
  } catch (e: any) {
    if (e !== false) console.error(e);
  }
};

const fetchConfig = async () => {
  try {
    const data = await DiscountConfigurationAPI.getAll();
    if (data && data.length > 0) {
      formData.list = data.map((item) => ({
        minPeople: item.minPeople,
        maxPeople: item.maxPeople,
        discount: item.discount,
      }));
    }
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  fetchConfig();
});
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
