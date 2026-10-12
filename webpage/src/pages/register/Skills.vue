<template>
  <div class="workshop-page">
    <!-- 按日期分组渲染 -->
    <div v-for="(day, index) in sessions" :key="index" class="day-section">
      <div class="section-header">{{ day.date }}</div>

      <div class="session-list">
        <div
            class="session-item"
            v-for="(session, sIndex) in day.items"
            :key="sIndex"
        >
          <el-checkbox v-model="session.selected" class="session-checkbox">
            {{ session.title }}
          </el-checkbox>

          <div class="session-details">
            <p class="detail-item">
              <span class="label">{{ t('register.learningObjectives') }}:</span>
              {{ session.objective }}
            </p>

            <p v-if="session.instructor" class="detail-item">
              <span class="label">{{ t('register.courseDirector') }}:</span>
              {{ session.instructor }}
            </p>

            <p class="detail-item">
              <span class="label">{{ t('register.quota') }}:</span>
              {{ session.quota }}
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
  router.push('/register/signup')
}

const handleNext = () => {
  registerStore.setSkillsData(JSON.parse(JSON.stringify(sessions.value)))
  router.push('/register/expert')
}

// 【核心】动态课程数据，从这里配置，页面会自动渲染
const defaultSessions = [
  {
    date: '6月26日，星期五',
    items: [
      {
        title: '二期人工晶状体植入技术 - 第一节: 08:30 - 10:00 155.00 元',
        selected: false,
        objective: '本实践操作课程重点讲解二期人工晶状体植入术的手术技巧。课程内容涵盖术前评估、手术入路、晶状体囊膜处理、不同的人工晶状体固定方法（包括传统的睫状沟缝合固定、预置缝线支撑技术和巩膜层间固定）、围手术期并发症及相应的处理策略。课程的大部分时间将用于动物眼模型上的实际手术操作，旨在帮助学员掌握标准化的手术流程，提高临床手术技能。本课程适合具有基本白内障手术经验的眼科医生。',
        instructor: '姚克教授和张明智教授。',
        quota: '20人。'
      },
      {
        title: 'MIGS & MIBS 技能培训 - 第一节: 10:30 - 12:00 155.00 元',
        selected: false,
        objective: '这门90分钟的实践课程将为学员提供独特的学习和练习机会，使用不同的微创青光眼手术（MIGS）和微创滤过泡手术（MIBS）器械。学员将在国际专家的指导下，逐步学习新技术，专家将在整个课程中提供一对一的帮助和指导。本课程旨在帮助学员在支持和鼓励的环境中，熟练掌握iStent® Inject W、Hydrus® Microstent、XEN® gel stent和MINIject®等器械的使用。',
        instructor: '林珊教授',
        quota: '20'
      },
      {
        title: 'MIGS & MIBS 技能培训 - 第二节: 13:30 - 15:00 155.00 元',
        selected: false,
        objective: '这门90分钟的实践课程将为学员提供独特的学习和练习机会，使用不同的微创青光眼手术（MIGS）和微创滤过泡手术（MIBS）器械。学员将在国际专家的指导下，逐步学习新技术，专家将在整个课程中提供一对一的帮助和指导。本课程旨在帮助学员在支持和鼓励的环境中，熟练掌握iStent® Inject W、Hydrus® Microstent、XEN® gel stent和MINIject®等器械的使用。',
        instructor: '林珊教授',
        quota: '20'
      },
      {
        title: '二期人工晶状体植入技术 - 第二节: 15:30 - 17:00 155.00元',
        selected: false,
        objective: '本实践操作课程重点讲解二期人工晶状体植入术的手术技巧。课程内容涵盖术前评估、手术入路、晶状体囊膜处理、不同的人工晶状体固定方法（包括传统的睫状沟缝合固定、预置缝线支撑技术和巩膜层间固定）、围手术期并发症及相应的处理策略。课程的大部分时间将用于动物眼模型上的实际手术操作，旨在帮助学员掌握标准化的手术流程，提高临床手术技能。本课程适合具有基本白内障手术经验的眼科医生。',
        instructor: '姚克教授和张明智教授。',
        quota: '20人。'
      }
    ]
  },
  {
    date: '星期六 6月27日',
    items: [
      {
        title: '基础玻璃体切除术技能 - 第一节: 08:30 - 10:00 155.00 元',
        selected: false,
        objective: '学员将学习玻璃体切除术的基本技能，包括玻璃体切除术和显微镜的设置，以及抬头式3D观察系统。他们将在人工眼和模拟器上进行练习，以熟练掌握玻璃体切除术的基本技能。',
        instructor: '林伟清教授',
        quota: '20'
      },
      {
        title: '高级玻璃体切除术技能 - 第一节: 10:30 - 12:00 155.00 元',
        selected: false,
        objective: '高级玻璃体视网膜手术技能课程旨在为学员提供国际专家指导下的实践培训，以提升其处理复杂玻璃体视网膜疾病（如视网膜游离、晶状体核脱落、眼内异物等）的高级手术技能，并学掌握高级内界膜剥离技术和睫状沟固定式人工晶状体植入术的精髓。先决条件：本课程面向已具备玻璃体切除术基础知识并希望进一步提升技能的学员。',
        instructor: '林伟清教授。',
        quota: '20人'
      },
      {
        title: '基础超声乳化术技能 - 第一节: 13:30 - 15:00 155.00 元',
        selected: false,
        objective: '本课程将向学员介绍超声乳化白内障手术的基本原理。在简要回顾超声乳化术的原理和步骤后，大部分时间将用于超声乳化术关键要素的实践操作。本课程最适合初级超声乳化术医生或经验丰富的小切口白内障手术医生，尤其适合对超声乳化术不熟悉的学员。',
        instructor: '',
        quota: '20'
      },
      {
        title: '高级超声乳化技术 - 第一节: 15:30 - 17:00 155.00 元',
        selected: false,
        objective: '本课程旨在帮助学员提升超声乳化技术，并教授他们如何实际处理更复杂的白内障手术病例。在简要介绍之后，大部分时间将用于实践操作，学员将轮流练习超声乳化劈核技巧、小瞳孔处理、晶状体悬韧带病变处理和前部玻璃体切除术。本课程最适合希望进一步提升超声乳化技术的中级白内障手术医生或住院医师/专科医师。',
        instructor: '',
        quota: '20'
      }
    ]
  },
  {
    date: '6月28日，星期日',
    items: [
      {
        title: '基础超声乳化术技能 - 第二节: 08:30 - 10:00 155.00 元',
        selected: false,
        objective: '本课程将向学员介绍超声乳化白内障手术的基本原理。在简要回顾超声乳化术的原理和步骤后，大部分时间将用于超声乳化术关键要素的实践操作。本课程最适合初级超声乳化术医生或经验丰富的小切口白内障手术医生，尤其适合对超声乳化术不熟悉的学员。',
        instructor: '',
        quota: '20'
      },
      {
        title: '高级超声乳化技术 - 第二节: 10:30 - 12:00 155.00 元',
        selected: false,
        objective: '本课程旨在帮助学员提升超声乳化技术，并教授他们如何实际处理更复杂的白内障手术病例。在简要介绍之后，大部分时间将用于实践操作，学员将轮流练习超声乳化劈核技巧、小瞳孔处理、晶状体悬韧带病变处理和前部玻璃体切除术。本课程最适合希望进一步提升超声乳化技术的中级白内障手术医生或住院医师/专科医师。',
        instructor: '',
        quota: '20'
      },
      {
        title: 'DMEK/DSEK手术技巧 - 第一节: 13:30 - 15:00 155.00元',
        selected: false,
        objective: '学员将能够使用人工前房和预装角膜组织，练习DMEK技术，进行供体角膜组织的植入和定位。本课程旨在提升学员在真实临床环境中开展DMEK手术的技能和信心。',
        instructor: '',
        quota: '20'
      },
      {
        title: 'DMEK/DSEK手术技巧 - 第二节: 15:30 - 17:00 155.00元',
        selected: false,
        objective: '学员将能够使用人工前房和预装角膜组织，练习DMEK技术，进行供体角膜组织的植入和定位。本课程旨在提升学员在真实临床环境中开展DMEK手术的技能和信心。',
        instructor: '',
        quota: '20'
      }
    ]
  },
  {
    date: '6月29日，星期一',
    items: [
      {
        title: '基础玻璃体切除术技能 - 第二节: 08:30 - 10:00 155.00 元',
        selected: false,
        objective: '学员将学习玻璃体切除术的基本技能，包括玻璃体切除术和显微镜的设置，以及抬头式3D观察系统。他们将在人工眼和模拟器上进行练习，以熟练掌握玻璃体切除术的基本技能。',
        instructor: '林伟清教授',
        quota: '20'
      },
      {
        title: '高级玻璃体切除术技能 - 第二节: 10:30 - 12:00 155.00 元',
        selected: false,
        objective: '高级玻璃体视网膜手术技能课程旨在为学员提供国际专家指导下的实践培训，以提升其处理复杂玻璃体视网膜疾病（如视网膜游离、晶状体核脱落、眼内异物等）的高级手术技能，并学掌握高级内界膜剥离技术和睫状沟固定式人工晶状体植入术的精髓。先决条件：本课程面向已具备玻璃体切除术基础知识并希望进一步提升技能的学员。',
        instructor: '林伟清教授。',
        quota: '20人'
      }
    ]
  }
]

const savedSessions = registerStore.state.skills
const sessions = ref(savedSessions.length > 0 ? savedSessions : JSON.parse(JSON.stringify(defaultSessions)))

watch(() => sessions.value, (newVal) => {
  registerStore.setSkillsData(JSON.parse(JSON.stringify(newVal)))
}, { deep: true })
</script>

<style scoped>
.workshop-page {
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

.session-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.session-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
}

.session-checkbox {
  font-size: 15px;
  font-weight: 500;
}

.session-details {
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
