<template>
  <div class="checkin-page">
    <div class="checkin-back" @click="goBack">
      <el-icon :size="20"><ArrowLeft /></el-icon>
      <span>返回</span>
    </div>

    <div v-if="loading" class="checkin-loading">
      <el-icon class="is-loading" :size="48" color="#fff"><Loading /></el-icon>
    </div>

    <template v-else-if="userInfo">
      <div class="checkin-header">
        <h1 class="checkin-title">现场签到</h1>
        <p class="checkin-subtitle">请确认信息后点击签到</p>
      </div>

      <div class="checkin-card" :class="{ 'checkin-card--done': checkedIn }">
        <div class="checkin-card__avatar">
          <span class="checkin-card__initial">
            {{ (userInfo.surname || userInfo.name || "?").charAt(0).toUpperCase() }}
          </span>
        </div>
        <div class="checkin-card__name">{{ userInfo.surname }}{{ userInfo.name }}</div>
        <div class="checkin-card__info">
          <div class="checkin-card__row">
            <el-icon class="checkin-card__icon"><Message /></el-icon>
            <span>{{ userInfo.email || "-" }}</span>
          </div>
          <div class="checkin-card__row">
            <el-icon class="checkin-card__icon"><Phone /></el-icon>
            <span>{{ userInfo.mobilePhone || "-" }}</span>
          </div>
        </div>
        <div class="checkin-card__footer">
          <el-button
            v-if="checkedIn"
            type="success"
            disabled
            round
            size="large"
          >
            <el-icon><Check /></el-icon> 已签到
          </el-button>
          <el-button
            v-else
            type="primary"
            :loading="checking"
            round
            size="large"
            @click="handleCheckIn"
          >
            签到
          </el-button>
        </div>
      </div>
    </template>

    <div v-else class="checkin-empty">
      <el-icon :size="64" color="rgba(255,255,255,0.5)"><UserFilled /></el-icon>
      <p>未找到签到人员</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
// import RegisterUserAPI from "@/api/portal/paperSubmission";
import RegisterUserAPI from "@/api/portal/registerUser";
import type { PaperSubmissionPageVO } from "@/types/api";

const route = useRoute();
const router = useRouter();

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.replace("/");
  }
};

const loading = ref(false);
const checking = ref(false);
const checkedIn = ref(false);
const userInfo = ref<PaperSubmissionPageVO | null>(null);

const fetchData = async () => {
  if (!route.query.id) {
    return;
  }
  loading.value = true;
  try {
    const res = await RegisterUserAPI.getPage({
      pageNum: 1,
      pageSize: 1,
      ...route.query,
    } as any);
    userInfo.value = res.list?.[0] ?? null;
  } catch {
    //
  } finally {
    loading.value = false;
  }
};

const handleCheckIn = async () => {
  checking.value = true;
  try {
    const now = new Date();
    const pad = (n: number) => String(n).padStart(2, "0");
    const timeStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;

    await RegisterUserAPI.checkIn({
      checkId: route.query.id,
      checkTime: timeStr,
      checkName: (userInfo.value?.surname || "") + (userInfo.value?.name || ""),
      checkNumber: userInfo.value?.mobilePhone || "",
      checkEmail: userInfo.value?.email || "",
    });
    checkedIn.value = true;
    ElMessage.success("签到成功");
  } catch (e: any) {
    ElMessage.error(e?.msg || "签到失败");
  } finally {
    checking.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.checkin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
}

.checkin-back {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  font-size: 15px;
  user-select: none;
  transition: color 0.2s;
}

.checkin-back:hover {
  color: #fff;
}

.checkin-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkin-header {
  text-align: center;
  margin-bottom: 40px;
}

.checkin-title {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.checkin-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.checkin-card {
  width: 420px;
  background: #fff;
  border-radius: 20px;
  padding: 44px 40px 36px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.16);
}

.checkin-card--done {
  opacity: 0.8;
}

.checkin-card__avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.checkin-card__initial {
  font-size: 34px;
  font-weight: 700;
  color: #fff;
}

.checkin-card__name {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

.checkin-card__info {
  text-align: left;
  margin-bottom: 28px;
}

.checkin-card__row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #606266;
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
}

.checkin-card__row:last-child {
  border-bottom: none;
}

.checkin-card__icon {
  color: #909399;
  flex-shrink: 0;
}

.checkin-card__footer {
  padding-top: 4px;
}

.checkin-empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
}

.checkin-empty p {
  margin-top: 16px;
  font-size: 16px;
}
</style>
