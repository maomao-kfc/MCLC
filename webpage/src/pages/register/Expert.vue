<template>
  <div class="course-page">
    <!-- 按日期分组渲染课程 -->
    <div v-for="(day, idx) in sessions" :key="idx" class="day-section">
      <div class="section-header">{{ day.date }}</div>
      <div class="course-list">
        <div
            class="course-item"
            v-for="(course, cIdx) in day.items"
            :key="cIdx"
        >
          <el-checkbox v-model="course.selected" class="course-checkbox">
            {{ course.title }}
          </el-checkbox>
          <div class="course-details">
            <p class="detail-item">
              <span class="label">{{ t('register.learningObjectives') }}:</span>
              {{ course.objective }}
            </p>
            <p v-if="course.instructor" class="detail-item">
              <span class="label">课程{{ course.isOrganizer ? t('register.organizer') : t('register.director') }}:</span>
              {{ course.instructor }}
            </p>
            <p class="detail-item">
              <span class="label">{{ course.quotaLabel || t('register.quota') }}:</span>
              {{ course.quota }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="btn-group">
      <el-button class="back-btn" @click="handleBack">{{ t('register.back') }}</el-button>
      <el-button type="primary" class="next-btn" @click="handleNext">{{ t('register.next') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElCheckbox, ElButton } from 'element-plus'
import { useRegisterStore } from '@/stores/register'

const router = useRouter()
const { t } = useI18n()
const registerStore = useRegisterStore()

const handleBack = () => {
  router.push('/register/skills')
}

const handleNext = () => {
  registerStore.setExpertData(JSON.parse(JSON.stringify(sessions.value)))
  router.push('/register/confirm')
}

// 【动态课程数据】所有课程都在这里配置，页面自动渲染
const defaultSessions = [
  {
    date: '6月26日，星期五',
    items: [
      {
        title: '白内障手术中的挑战性场景: 13:15 - 14:15 45.00元',
        selected: false,
        objective: `完成本课程后，学员将能够：
识别高危白内障病例（识别增加手术复杂性的临床特征和患者因素，包括晶状体剥脱综合征、瞳孔过小、晶状体悬韧带松弛、晶状体核致密、既往眼科手术史和合并症）。使用专用手术工具和设备（在复杂情况下选择并恰当使用囊袋张力环、虹膜扩张器、飞秒激光辅助和其他辅助设备）。
针对复杂病例调整超声乳化技术（针对致密性白内障和眼部结构受损的情况，调整液流控制、能量设置和晶状体核分离策略）。处理瞳孔过小和晶状体悬韧带不稳定的病例（采用机械和药物方法安全地扩张瞳孔并稳定晶状体囊袋）。`,
        instructor: 'Boris Malyugin 教授',
        quota: '50'
      }
    ]
  },
  {
    date: '星期六 6月27日',
    items: [
      {
        title: '视网膜基础眼科知识: 17:15 - 18:15 45.00元',
        selected: false,
        objective: `本次重点教育课程将为年轻的专科医生和研究员提供简明扼要而又全面的眼科核心原理回顾，这些原理支撑着现代视网膜疾病的诊疗实践。课程将着重讲解视网膜解剖、影像判读、疾病机制和临床决策的关键概念，并强调如何将知识应用于日常患者诊疗。通过专家主导的讨论和临床见解，学员将加深对视网膜疾病准确诊断、最佳治疗方案和改善预后的关键主题的理解。`,
        instructor: 'Anat Loewenstein 教授',
        quota: '50',
        quotaLabel: '人数'
      },
      {
        title: 'PCR——玻璃体和细胞核的处理——我的最佳教学视频: 17:15 - 18:15 45.00元',
        selected: false,
        objective: `完成本课程后，学员将能够识别后囊破裂（PCR）的早期征象，理解前部玻璃体切除术的核心原则和适应症，并评估PCR后安全有效处理晶状体核的各种方案。此外，学员还将获得术后决策方面的实践经验，并掌握在术中并发症发生后清晰、体贴地为患者提供咨询的技能，从而帮助患者建立知情预期并获得最佳的医疗护理。`,
        instructor: 'David Chang教授',
        quota: '50'
      },
      {
        title: '功能性眼睑手术: 17:15 - 18:15 45.00元',
        selected: false,
        objective: `本课程将探讨上下眼睑手术，重点关注功能性眼睑位置异常。上眼睑疾病包括上睑下垂、睑退缩和麻痹性眼睑闭合不全。上眼睑手术包括提上睑肌前移/切除术、穆勒氏肌-结膜切除术、额肌悬吊术、全层睑缘切开术和上睑负重植入术。下眼睑疾病包括睑外翻、睑内翻和睑退缩。下眼睑手术包括外侧睑板条切除术、内翻缝合术、中面部提升术、全层皮肤移植术、睑缘牵开器复位术、前板层后退术、外翻缝合术和间隔物植入术（包括自体和合成材料）。`,
        instructor: 'Richard C. Allen 教授',
        quota: '50',
        quotaLabel: '席位'
      },
      {
        title: '儿童眼科检查核心技能: 17:15 - 18:15 45.00元',
        selected: false,
        objective: `本"专家见面会"课程旨在通过真实案例讨论和循序渐进的指导，使学员掌握儿科眼科检查的实用核心技能，包括有效管理儿童、与家长沟通以及应对临床实践中与检查相关的焦虑情绪的策略。`,
        instructor: 'Ken Nischal 教授',
        quota: '50'
      },
      {
        title: '眼内炎症评估: 17:15 - 18:15 45.00元',
        selected: false,
        objective: '',
        instructor: 'Marc de Smet 博士',
        isOrganizer: true,
        quota: '50',
        quotaLabel: '席位数量'
      }
    ]
  }
]

const savedSessions = registerStore.state.expert
const sessions = ref(savedSessions.length > 0 ? savedSessions : JSON.parse(JSON.stringify(defaultSessions)))

watch(() => sessions.value, (newVal) => {
  registerStore.setExpertData(JSON.parse(JSON.stringify(newVal)))
}, { deep: true })
</script>

<style scoped>
.course-page {
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.day-section {
  margin-bottom: 30px;
}

.section-header {
  background-color: #009688;
  color: white;
  font-size: 22px;
  font-weight: 600;
  padding: 12px;
  text-align: center;
  border-radius: 8px;
  margin-bottom: 20px;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.course-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
}

.course-checkbox {
  font-size: 15px;
  font-weight: 500;
}

.course-details {
  margin-top: 12px;
  padding-left: 24px;
}

.detail-item {
  margin: 6px 0;
  font-size: 14px;
  line-height: 1.6;
}

.label {
  color: #009688;
  font-weight: 600;
}

.btn-group {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}

.back-btn,
.next-btn {
  background-color: #7d7ef4;
  border: none;
  color: white;
  padding: 12px 60px;
  font-size: 16px;
  border-radius: 8px;
}
</style>
