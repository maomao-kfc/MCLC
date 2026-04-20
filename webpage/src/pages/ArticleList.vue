<!-- src/pages/ArticleList.vue -->
<template>
  <div class="article-list">
    <div class="list-container">
      <!-- 页面头部 -->
      <div class="list-header">
        <h1 class="page-title">{{ t('speechCollection') }}</h1>
        <p class="page-desc">{{ t('collectionDesc') }}</p>
      </div>

      <!-- 文章卡片列表 -->
      <div class="article-grid">
        <div 
          class="article-card" 
          v-for="article in currentArticleList" 
          :key="article.id"
          @click="goToDetail(article.id)"
        >
          <div class="card-tag">NO.{{ article.id }}</div>
          <div class="card-content">
            <h2 class="article-title">{{ article.title }}</h2>
            <h3 class="article-subtitle">{{ article.subTitle }}</h3>
            <p class="article-en-subtitle">{{ article.enSubTitle }}</p>
            <div class="article-meta">
              <span>{{ article.publishTime }}</span>
              <span>{{ article.author }}</span>
            </div>
          </div>
          <div class="card-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 返回首页按钮 -->
      <div class="back-wrap">
        <el-button @click="$router.push('/')" class="back-btn">
          {{ t('backToHome') }}
        </el-button>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useLangStore } from '@/stores/lang.js'
// 引入文章数据源
import { articleList } from '@/data/article.js'
// 现有的组件
import Footer from '@/components/common/Footer.vue'
import { ArrowRight } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()
const langStore = useLangStore()

// 根据当前语言获取文章列表
const currentArticleList = computed(() => {
  return articleList[langStore.currentLang] || articleList.zh
})

// 跳转到文章详情页
const goToDetail = (id) => {
  router.push(`/article/${id}`)
}
</script>

<style scoped>
.article-list {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.list-container {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px 80px;
  box-sizing: border-box;
}

/* 页面头部 */
.list-header {
  text-align: center;
  margin-bottom: 60px;
}
.page-title {
  margin: 0 0 16px;
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(90deg, #7b61ff, #b886f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 4px;
}
.page-desc {
  margin: 0;
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

/* 文章网格布局 */
.article-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 60px;
}

/* 文章卡片 */
.article-card {
  width: 100%;
  padding: 32px;
  border-radius: 16px;
  border: 2px solid rgba(123, 97, 255, 0.1);
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 24px;
  transition: all 0.3s ease;
  cursor: pointer;
  box-sizing: border-box;
}
.article-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 24px rgba(123, 97, 255, 0.15);
  border-color: #7b61ff;
}

/* 期数标签 */
.card-tag {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7b61ff, #b886f0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

/* 卡片内容 */
.card-content {
  flex: 1;
  min-width: 0;
}
.article-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-subtitle {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #555;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-en-subtitle {
  margin: 0 0 12px;
  font-size: 14px;
  color: #999;
  font-style: italic;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.article-meta {
  display: flex;
  gap: 32px;
  font-size: 14px;
  color: #999;
}

/* 箭头 */
.card-arrow {
  flex-shrink: 0;
  font-size: 24px;
  color: #7b61ff;
  transition: all 0.3s ease;
}
.article-card:hover .card-arrow {
  transform: translateX(4px);
}

/* 返回按钮 */
.back-wrap {
  text-align: center;
}
.back-btn {
  padding: 10px 36px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(90deg, #7b61ff, #b886f0);
  border: none;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .list-container {
    padding: 40px 16px 40px;
  }
  .page-title {
    font-size: 26px;
    letter-spacing: 2px;
  }
  .article-card {
    padding: 20px;
    flex-wrap: wrap;
    gap: 16px;
  }
  .card-tag {
    width: 60px;
    height: 60px;
    font-size: 16px;
  }
  .article-title {
    font-size: 18px;
  }
  .article-subtitle {
    font-size: 16px;
  }
}
</style>