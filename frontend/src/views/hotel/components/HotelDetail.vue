<!-- 酒店详情抽屉 -->

<template>
  <el-drawer
    v-model="visible"
    :title="`酒店详情 - ${detail.name || ''}`"
    size="600px"
    @close="handleClose"
  >
    <el-descriptions :column="1" border v-loading="loading">
      <el-descriptions-item label="酒店名称">
        {{ detail.name }}
      </el-descriptions-item>
      <el-descriptions-item label="联系电话">
        {{ detail.phone }}
      </el-descriptions-item>
      <el-descriptions-item label="地址">
        {{ detail.address }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag v-if="detail.status === 1" type="success">正常</el-tag>
        <el-tag v-else type="info">已取消合作</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="详情介绍">
        {{ detail.description || "暂无介绍" }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { getHotelDetail } from "@/api/hotel/hotel";
import type { Hotel } from "../types";

interface Props {
  visible: boolean;
  hotelId?: number;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
  (e: "close"): void;
}>();

const visible = ref(props.visible);
const loading = ref(false);
const detail = ref<Partial<Hotel>>({});

watch(
  () => props.visible,
  (val) => {
    visible.value = val;
    if (val && props.hotelId) {
      fetchDetail(props.hotelId);
    }
  }
);

watch(visible, (val) => {
  emit("update:visible", val);
  if (!val) {
    emit("close");
  }
});

async function fetchDetail(id: number) {
  loading.value = true;
  try {
    const { data } = await getHotelDetail(id);
    detail.value = data;
  } finally {
    loading.value = false;
  }
}

function handleClose() {
  detail.value = {};
}
</script>
