<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__content">
            <div class="stat-card__icon" style="background: #409eff">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-card__info">
              <div class="stat-card__title">注册用户</div>
              <div class="stat-card__value">1,234</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__content">
            <div class="stat-card__icon" style="background: #67c23a">
              <el-icon :size="30"><Money /></el-icon>
            </div>
            <div class="stat-card__info">
              <div class="stat-card__title">支付金额</div>
              <div class="stat-card__value">¥56,780</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__content">
            <div class="stat-card__icon" style="background: #e6a23c">
              <el-icon :size="30"><Document /></el-icon>
            </div>
            <div class="stat-card__info">
              <div class="stat-card__title">论文投稿</div>
              <div class="stat-card__value">89</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__content">
            <div class="stat-card__icon" style="background: #f56c6c">
              <el-icon :size="30"><House /></el-icon>
            </div>
            <div class="stat-card__info">
              <div class="stat-card__title">酒店预订</div>
              <div class="stat-card__value">256</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div>每月注册用户趋势</div>
          </template>
          <div class="chart-placeholder">
            <el-icon :size="50" color="#c0c4cc"><TrendCharts /></el-icon>
            <div style="margin-top: 10px; color: #909399">图表区域</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div>支付方式分布</div>
          </template>
          <div class="chart-placeholder">
            <el-icon :size="50" color="#c0c4cc"><PieChart /></el-icon>
            <div style="margin-top: 10px; color: #909399">图表区域</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div>最近操作记录</div>
          </template>
          <el-table :data="logList" border>
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="操作人" prop="operator" width="120" />
            <el-table-column label="操作类型" prop="action" width="150" />
            <el-table-column label="操作内容" prop="content" min-width="250" show-overflow-tooltip />
            <el-table-column label="操作时间" prop="createTime" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { User, Money, Document, House, TrendCharts, PieChart } from "@element-plus/icons-vue";

const logList = ref([
  { id: 1, operator: "张三", action: "用户注册", content: "新用户注册: 李四", createTime: "2024-01-15 10:30:00" },
  { id: 2, operator: "李四", action: "支付订单", content: "订单支付: ¥500", createTime: "2024-01-15 11:20:00" },
  { id: 3, operator: "王五", action: "论文投稿", content: "投稿论文: 人工智能研究", createTime: "2024-01-15 14:15:00" },
  { id: 4, operator: "赵六", action: "酒店预订", content: "预订酒店: 望江酒店", createTime: "2024-01-15 15:45:00" },
  { id: 5, operator: "管理员", action: "审核通过", content: "审核通过论文投稿", createTime: "2024-01-15 16:30:00" },
]);
</script>

<style scoped>
.app-container { padding: 20px; }
.stat-card { cursor: pointer; transition: transform 0.3s; }
.stat-card:hover { transform: translateY(-5px); }
.stat-card__content { display: flex; align-items: center; padding: 10px; }
.stat-card__icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-card__info { margin-left: 20px; flex: 1; }
.stat-card__title { font-size: 14px; color: #909399; }
.stat-card__value { font-size: 24px; font-weight: bold; color: #303133; margin-top: 5px; }
.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>