<!-- 首页 -->

<template>
  <div class="home">
    <!-- 组织架构 -->
    <div class="architecture-section">
      <h2 class="module-title">{{ t('organization') }}</h2>
	  <!-- <h2 class="module-title">2027MCLC会议组织架构</h2> -->
      <div class="architecture-grid">
        <div class="architecture-card" v-for="item in architectureList" :key="item.id">
          <div class="arch-img-wrap">
            <el-image :src="item?.imageHeader" fit="contain" :alt="item?.name">
              <template #error>
                <div class="img-error">
                  <span>{{ item?.name?.charAt(0) }}</span>
                </div>
              </template>
            </el-image>
          </div>
          <div class="arch-info">
            <p class="arch-name">{{ item?.name }}</p>
            <p class="arch-job">{{ item?.job }}</p>
          </div>
        </div>
      </div>
    </div>
	
	<div class="featuredGuestSpeeches">
	  <h2 class="module-title">{{ t('speechCollection') }}</h2>
	  <!-- <h2 class="module-title">2026MCLC会议特邀嘉宾演讲集锦</h2> -->
	  <!-- 9个嘉宾卡片 3*3布局 -->
	  <div class="speeches-grid">
	    <div
	        class="speech-card"
	        v-for="item in speechList"
	        :key="item.id"
	        @click="$router.push(`/article/${item.id}`)"
	    >
	      <!-- 嘉宾图片 -->
	      <div class="card-img-wrap">
	        <el-image
	            :src="item?.cover"
	            fit="contain"
	            :alt="item?.title"
	        >
	          <!-- 图片加载失败的兜底 -->
	          <template #error>
	            <div class="img-error">
	              <span>NO.{{ id }}</span>
	            </div>
	          </template>
	        </el-image>
	      </div>
	      <!-- 卡片hover文字 -->
	      <div class="card-hover-mask">
	         <p class="mask-text">{{ t('viewSpeechDetail') }}</p>
	      </div>
	    </div>
	  </div>
	</div>

    <!-- 主办方 / 承办方 -->
    <div class="organizer-section">
      <div class="organizer-row">
        <span class="organizer-label">{{ t('organizers.host') }}：</span>
        <span class="organizer-value">{{ meetingSetting?.sponsor }}</span>
      </div>
      <div class="organizer-row">
        <span class="organizer-label">{{ t('organizers.organizer') }}：</span>
        <span class="organizer-value">{{ meetingSetting?.organizer }}</span>
      </div>
    </div>

    <!-- 精彩瞬间模块 -->
    <div class="splendidMoment">
      <h1 class="moment-title">{{ t('gallery') }}</h1>
      <div class="moment-grid">
        <div class="moment-item" v-for="i in jct" :key="i.id">
          <el-image :src="i?.momentsImage" fit="cover"
                    :preview-src-list="[i?.momentsImage]" :initial-index="0" preview-teleported
                    :hide-on-click-modal="true" />
        </div>
      </div>
    </div>

    <!-- 底部城市线稿 -->
    <div class="diTu">
      <el-image src="/assets/image/home/homeDiBu.png" fit="width"></el-image>
    </div>
  </div>
</template>

<script setup>
import {getMomentsList} from '@/api/portal/moments'
import {getArchitecturePage} from '@/api/portal/architecture'
import {getMeetingSettingList} from '@/api/portal/meetSetting'
import { useGuestStore } from '@/stores/guest.js'
import {
  ref,
  computed, onMounted, watch
} from 'vue'
import {
  useLangStore
} from '@/stores/lang.js'
import {
  useI18n
} from 'vue-i18n'
import {getSpeechList} from "@/api/portal/speech";
const {
  t
} = useI18n()
const guestStore = useGuestStore()
const langStore = useLangStore()
const lbt = ref([])
const jct = ref([])
const architectureList = ref([])
const meetingSetting = ref(null)
const speechList = ref([])
let fetchId = 0

const fetchData = async () => {
  const id = ++fetchId
  speechList.value = []
  try {
    const speeechRes = await getSpeechList()
    if (id !== fetchId) return
    console.log("speeechRes",speeechRes)
    if(speeechRes?.msg === '成功'){
      const seen = new Set()
      const uniqueData = (speeechRes.data || []).filter(item => {
        const key = item.id
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
      speechList.value = uniqueData
      await guestStore.gethwList()
      await guestStore.getGuestList(speechList.value)
    }
    const res = await getMomentsList()
    if (id !== fetchId) return
    if(res?.msg === '成功'){
      let dataList = JSON.parse(JSON.stringify( res?.data))
      lbt.value = dataList.filter(item => item.type === '1')
      jct.value = dataList.filter(item => item.type === '2')
    }
    const archRes = await getArchitecturePage({ pageNum: 1, pageSize: 100 })
    if (id !== fetchId) return
    if (archRes?.msg === '成功') {
      architectureList.value = archRes?.data?.list || []
    }
    const meetRes = await getMeetingSettingList()
    if (id !== fetchId) return
    if (meetRes?.msg === '成功') {
      meetingSetting.value = meetRes?.data?.[0] || null
    }
  } catch {
    if (id === fetchId) {
      fetchId = 0
    }
  }
}

onMounted(() => {
  fetchData()
})

watch(() => langStore.currentLang, () => {
  fetchData()
})
// 1. 定义多语言图片路径
const imageLists = {
  zh: [
    '/assets/image/home/1756834161653040827.jpg',
    '/assets/image/home/1756965923078044126.png',
  ],
  en: [
    '/assets/image/home/1756966563760043072Y2.jpg',
    '/assets/image/home/1757305589205047731Y3.png',
  ]
}

// 2. 计算属性：根据当前语言，动态获取对应的图片数组
const currentImageList = computed(() => {
  return imageLists[langStore.currentLang] || imageLists.zh
})

// 3. 定义「精彩瞬间」图片列表
const momentImageList = ref(
    Array.from({
      length: 12
    }, (_, index) => `/assets/image/home/jingCaiJiLu${index + 1}.jpg`)
)

</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  overflow: hidden;
}

/* ==================== 组织架构样式 ==================== */
.architecture-section {
  width: 100%;
  max-width: 1200px;
  margin: 60px auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.architecture-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.architecture-card {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: #fff;
}

.architecture-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(123, 97, 255, 0.2);
}

.arch-img-wrap {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
}

.arch-img-wrap :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.arch-info {
  padding: 16px 20px 20px;
  text-align: center;
}

.arch-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.arch-job {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* ==================== 主办方 / 承办方样式 ==================== */
.organizer-section {
  width: 100%;
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
  box-sizing: border-box;
  text-align: center;
}

.organizer-row {
  display: flex;
  padding: 10px 0;
  font-size: 16px;
  color: #333;
  padding-left: 25%;
}

.organizer-label {
  font-weight: 600;
}

.organizer-value {
  color: #555;
}

/* ==================== 精彩瞬间核心样式 ==================== */
.splendidMoment {
  width: 100%;
  max-width: 1200px;
  margin: 60px auto 40px;
  padding: 0 20px;
  box-sizing: border-box;
}

.moment-title {
  margin: 0 0 40px;
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(90deg, #7b61ff, #b886f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
  letter-spacing: 4px;
}

.moment-title::before,
.moment-title::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 120px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #7b61ff, transparent);
  transform: translateY(-50%);
}

.moment-title::before {
  left: 50%;
  margin-left: -320px;
}

.moment-title::after {
  right: 50%;
  margin-right: -320px;
}

.moment-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  width: 100%;
}

.moment-item {
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(123, 97, 255, 0.2);
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.moment-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(123, 97, 255, 0.2);
  border-color: #7b61ff;
}

.moment-item :deep(.el-image) {
  width: 100%;
  height: 100%;
  display: block;
}

/* 底部城市线稿样式 */
.diTu {
  width: 100%;
  margin-top: 20px;
}

.diTu :deep(.el-image) {
  width: 100%;
  height: auto;
  display: block;
}

/* ==================== 文章跳转图片样式 ==================== */
.article-btn-wrap {
  width: 100%;
  text-align: center;
  margin: 40px auto 20px;
}

/* 图片容器 */
.article-btn {
  display: inline-block;
  border: none;
  padding: 0;
  background: none;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

/* 跳转图片样式 */
.article-btn-img {
  width: 100%;
  max-width: 320px;
  /* 图片最大宽度 */
  height: auto;
  /* 保持宽高比 */
  display: block;
}

/* 图片hover动效 */
.article-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(123, 97, 255, 0.3);
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 992px) {
  .moment-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .architecture-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .moment-title {
    font-size: 26px;
  }

  .moment-title::before,
  .moment-title::after {
    width: 80px;
  }

  .moment-title::before {
    margin-left: -220px;
  }

  .moment-title::after {
    margin-right: -220px;
  }
}

@media (max-width: 576px) {
  .moment-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .architecture-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .splendidMoment {
    margin: 40px auto 20px;
  }

  .moment-title {
    font-size: 22px;
    letter-spacing: 2px;
  }

  .moment-title::before,
  .moment-title::after {
    display: none;
  }

  /* 移动端图片适配 */
  .article-btn-img {
    max-width: 280px;
    /* 移动端缩小图片宽度 */
  }
}

/* 模块容器 */
.featuredGuestSpeeches {
  width: 100%;
  max-width: 1200px;
  margin: 60px auto;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 模块标题 */
.module-title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin: 0 0 40px;
  letter-spacing: 2px;
}

/* 3*3网格布局 */
.speeches-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 每行3个 */
  gap: 24px; /* 卡片间距 */

}

/* 卡片样式 */
.speech-card {
  position: relative;
  width: 100%;
  height: auto; /* 图片宽高比 */
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background-color: #F6F7FF;
}

/* 卡片hover效果 */
.speech-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(123, 97, 255, 0.15);
}

/* 图片容器 */
.card-img-wrap {
  width: 100%;
  height: 100%;
}
.card-img-wrap :deep(.el-image) {
  width: 100%;
  height: 100%;
}

/* 图片加载失败兜底 */
.img-error {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #7b61ff, #b886f0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

/* hover遮罩 */
.card-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.speech-card:hover .card-hover-mask {
  opacity: 1;
}
.mask-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .speeches-grid {
    grid-template-columns: repeat(2, 1fr); /* 手机端每行2个 */
    gap: 16px;
  }
  .module-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
  .mask-text {
    font-size: 14px;
  }
  .organizer-row {
    padding-left: 0;
  }
}
</style>

<style>
/* 图片预览弹窗样式 */
.el-image-viewer__img {
  max-width: 80vw !important;
  max-height: 80vh !important;
  width: auto !important;
  height: auto !important;
}

.el-image-viewer__mask {
  background-color: rgba(0, 0, 0, 0.7) !important;
}

.el-image-viewer__btn {
  font-size: 20px !important;
}
</style>
