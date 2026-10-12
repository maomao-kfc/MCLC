<template>
  <!-- 🔥 必须只有一个根节点（修复报错核心1） -->
  <div class="faq-wrapper">
    <h2 class="faq-main-title">{{ t('faqPage.title') }}</h2>

<!--    <div class="faq-greeting">-->
<!--      <p class="greeting-title">{{ t('faqPage.greeting') }}</p>-->
<!--      <p class="greeting-desc">-->
<!--        {{ t('faqPage.greetingDesc') }}-->
<!--      </p>-->
<!--    </div>-->
    <div class="faq-section" v-for="item in faqList" :key="item.id">
      <h3 class="section-title">{{ item?.title
        }}</h3>
      <div class="faq-item" v-html="item?.questionDetail">
      </div>
    </div>

    <!-- 🔥 图片路径匹配你的 PUBLIC 结构（修复404） -->
    <div class="city-bg">
      <el-image src="/assets/image/faq/changJianDiTu.png" fit="width" class="city-img" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
import { getPageQuest } from  '@/api/portal/answer'
import {onMounted} from "vue";

const { t } = useI18n()
const langStore = useLangStore()
const faqList = ref()

const fetchData = async () => {
  const res = await getPageQuest()
  if(res?.msg === '成功'){
    faqList.value = JSON.parse(JSON.stringify( res?.data))
  }
}

onMounted(() => {
  fetchData()
})

watch(() => langStore.currentLang, () => {
  fetchData()
})
</script>

<style scoped lang="scss">
.faq-wrapper {
  width: 100%;
  background: #ffffff;
  border-radius: 12px;
  padding: 30px 24px;
  position: relative;
  overflow: hidden;
}

.faq-main-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(90deg, #114C9A, #7952b3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 24px;
}

.faq-greeting {
  margin-bottom: 32px;
}
.greeting-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}
.greeting-desc {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}

.faq-section {
  margin-bottom: 24px;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #114C9A;
  margin: 0 0 16px;
  text-align: left;
}

.faq-item {
  margin-bottom: 16px;
  padding-left: 25px;
  box-sizing: border-box;
  text-align: left;
}
.faq-question {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px;
}
.faq-answer {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}
.faq-link {
  color: #114C9A;
  text-decoration: underline;
}

.faq-footer {
  margin-top: 40px;
}
.closing-text {
  font-size: 16px;
  font-weight: 600;
  color: #7952b3;
  margin-bottom: 16px;
}
.contact-info p {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  margin: 4px 0;
}

.city-bg {
  width: 100%;
  margin-top: 30px;
}
.city-img {
  width: 100%;
  height: auto;
}
</style>
