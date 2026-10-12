<template>
  <div class="relative p-6">
    <!-- github 角标 -->
<!--    <github-corner class="absolute top-0 right-0 z-1 border-0" />-->

    <el-card shadow="never" class="mt-2">
      <div class="flex flex-wrap">
        <!-- 左侧问候语区域 -->
        <div class="flex-1 flex items-start">
          <div style="width: 80px; height: 80px; overflow: hidden; border-radius: 50%">
            <img
              :src="userStore.userInfo.avatar + '?imageView2/1/w/80/h/80'"
              class="w80px h80px rounded-full"
              style="width: 100%; height: 100%; object-fit: cover; object-position: center"
            />
          </div>
          <div class="ml-5">
            <p class="text-base font-semibold text-[--el-text-color-primary] leading-tight">
              {{ greetings }}
            </p>
            <p class="text-sm text-gray">今日天气晴朗，气温在15℃至25℃之间，东南风。</p>
          </div>
        </div>
        </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "Dashboard",
  inheritAttrs: false,
});

import { dayjs } from "element-plus";
import { ref } from "vue";
import { useRouter } from "vue-router";
import StatisticsAPI from "@/api/system/statistics";
import type { VisitStatsDetail, VisitTrendDetail } from "@/types/api";
import { useUserStore } from "@/store/modules/user";
import { formatGrowthRate } from "@/utils";
import { useTransition, useDateFormat } from "@vueuse/core";
import { CircleCheck, CircleClose, Loading, Clock, Menu } from "@element-plus/icons-vue";
import { useOnlineCount, useRecentMenus } from "@/composables";

const router = useRouter();

// 在线用户数量组件相关
const { onlineUserCount, lastUpdateTime, isConnected, connectionState } = useOnlineCount();

// 最近访问菜单
const { recentMenus, clearRecentMenus } = useRecentMenus();
console.log("最近访问1111111",recentMenus);
const listArray = ref([
  {
    path: "/userNum/userNum",
    title: "会议报名",
  },
  {
    path: "/userNum/userNum",
    title: "酒店预订",
  },
  {
    path: "/userNum/userNum",
    title: "论文投稿",
  },
])
// 格式化时间戳
const formattedTime = computed(() => {
  if (!lastUpdateTime.value) return "--";
  return useDateFormat(lastUpdateTime, "HH:mm:ss").value;
});

const sseStatusText = computed(() => {
  if (!isConnected.value) {
    return connectionState.value === "CONNECTING" ? "连接中" : "未连接";
  }
  return "已连接";
});

const sseStatusClass = computed(() => {
  if (isConnected.value)
    return "text-[--el-color-success] bg-[--el-color-success-light-9] border-[--el-color-success-light-7]";
  return connectionState.value === "CONNECTING"
    ? "text-[--el-color-warning] bg-[--el-color-warning-light-9] border-[--el-color-warning-light-7]"
    : "text-[--el-color-danger] bg-[--el-color-danger-light-9] border-[--el-color-danger-light-7]";
});

const userStore = useUserStore();

// 当前时间（用于计算问候语）
const currentDate = new Date();

// 问候语：根据当前小时返回不同问候语
const greetings = computed(() => {
  const hours = currentDate.getHours();
  const nickname = userStore.userInfo.nickname;
  if (hours >= 6 && hours < 8) {
    return "晨起披衣出草堂，轩窗已自喜微凉🌅！";
  } else if (hours >= 8 && hours < 12) {
    return `上午好，${nickname}！`;
  } else if (hours >= 12 && hours < 18) {
    return `下午好，${nickname}！`;
  } else if (hours >= 18 && hours < 24) {
    return `晚上好，${nickname}！`;
  } else {
    return "偷偷向银河要了一把碎星，只等你闭上眼睛撒入你的梦中，晚安🌛！";
  }
});

// 访客统计数据加载状态
const visitStatsLoading = ref(true);
// 访客统计数据
const visitStatsData = ref<VisitStatsDetail>({
  todayUvCount: 0,
  uvGrowthRate: 0,
  totalUvCount: 0,
  todayPvCount: 0,
  pvGrowthRate: 0,
  totalPvCount: 0,
});

const uvGrowthText = computed(() => {
  if (
    visitStatsData.value.uvGrowthRate === undefined ||
    visitStatsData.value.uvGrowthRate === null
  ) {
    return "--";
  }
  return formatGrowthRate(visitStatsData.value.uvGrowthRate);
});

const pvGrowthText = computed(() => {
  if (
    visitStatsData.value.pvGrowthRate === undefined ||
    visitStatsData.value.pvGrowthRate === null
  ) {
    return "--";
  }
  return formatGrowthRate(visitStatsData.value.pvGrowthRate);
});

// 数字过渡动画
const transitionUvCount = useTransition(
  computed(() => visitStatsData.value.todayUvCount),
  {
    duration: 1000,
    transition: [0.25, 0.1, 0.25, 1.0], // CSS cubic-bezier
  }
);

const transitionTotalUvCount = useTransition(
  computed(() => visitStatsData.value.totalUvCount),
  {
    duration: 1200,
    transition: [0.25, 0.1, 0.25, 1.0],
  }
);

const transitionPvCount = useTransition(
  computed(() => visitStatsData.value.todayPvCount),
  {
    duration: 1000,
    transition: [0.25, 0.1, 0.25, 1.0],
  }
);

const transitionTotalPvCount = useTransition(
  computed(() => visitStatsData.value.totalPvCount),
  {
    duration: 1200,
    transition: [0.25, 0.1, 0.25, 1.0],
  }
);

// 过渡结果可能是 Ref<number>，为模板中使用做类型和格式处理（避免 TS 报错）
const displayTransitionUvCount = computed(() =>
  Math.round(Number((transitionUvCount as any)?.value ?? transitionUvCount))
);
const displayTransitionTotalUvCount = computed(() =>
  Math.round(Number((transitionTotalUvCount as any)?.value ?? transitionTotalUvCount))
);
const displayTransitionPvCount = computed(() =>
  Math.round(Number((transitionPvCount as any)?.value ?? transitionPvCount))
);
const displayTransitionTotalPvCount = computed(() =>
  Math.round(Number((transitionTotalPvCount as any)?.value ?? transitionTotalPvCount))
);

// 访问趋势日期范围（单位：天）
const visitTrendDateRange = ref(7);
// 访问趋势图表配置
const visitTrendChartOptions = ref();

/**
 * 获取访客统计数据
 */
const fetchVisitStatsData = () => {
  StatisticsAPI.getVisitOverview()
    .then((data) => {
      visitStatsData.value = data;
    })
    .finally(() => {
      visitStatsLoading.value = false;
    });
};

/**
 * 获取访问趋势数据，并更新图表配置
 */
const fetchVisitTrendData = () => {
  const startDate = dayjs()
    .subtract(visitTrendDateRange.value - 1, "day")
    .toDate();
  const endDate = new Date();

  StatisticsAPI.getVisitTrend({
    startDate: dayjs(startDate).format("YYYY-MM-DD"),
    endDate: dayjs(endDate).format("YYYY-MM-DD"),
  }).then((data) => {
    updateVisitTrendChartOptions(data);
  });
};

/**
 * 更新访问趋势图表的配置项
 *
 * @param data - 访问趋势数据
 */
const updateVisitTrendChartOptions = (data: VisitTrendDetail) => {
  visitTrendChartOptions.value = {
    tooltip: {
      trigger: "axis",
    },
    legend: {
      data: ["浏览量(PV)", "访客量UV)"],
      bottom: 0,
    },
    grid: {
      left: "1%",
      right: "5%",
      bottom: "10%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: data.dates,
    },
    yAxis: {
      type: "value",
      splitLine: {
        show: true,
        lineStyle: {
          type: "dashed",
        },
      },
    },
    series: [
      {
        name: "浏览量(PV)",
        type: "line",
        data: data.pvList,
        areaStyle: {
          color: "rgba(64, 158, 255, 0.1)",
        },
        smooth: true,
        itemStyle: {
          color: "#4080FF",
        },
        lineStyle: {
          color: "#4080FF",
        },
      },
      {
        name: "访客量UV)",
        type: "line",
        data: data.ipList,
        areaStyle: {
          color: "rgba(103, 194, 58, 0.1)",
        },
        smooth: true,
        itemStyle: {
          color: "#67C23A",
        },
        lineStyle: {
          color: "#67C23A",
        },
      },
    ],
  };
};

/**
 * 根据增长率计算对应的 CSS 类名
 *
 * @param growthRate - 增长率数值
 */
const computeGrowthRateClass = (growthRate?: number): string => {
  if (!growthRate) {
    return "text-[--el-color-info]";
  }
  if (growthRate > 0) {
    return "text-[--el-color-danger]";
  } else if (growthRate < 0) {
    return "text-[--el-color-success]";
  } else {
    return "text-[--el-color-info]";
  }
};

// 监听访问趋势日期范围的变化，重新获取趋势数据
watch(
  () => visitTrendDateRange.value,
  () => {
    // fetchVisitTrendData();
  },
  { immediate: true }
);

// 组件挂载后加载访客统计数据和通知公告数据
onMounted(() => {
  // fetchVisitStatsData();
});
</script>

<style lang="scss" scoped>
// 暂无自定义样式
</style>
