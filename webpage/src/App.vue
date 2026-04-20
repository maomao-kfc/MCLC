<template>
  <!-- 顶部语言切换 -->
  <HeaderLang />
  
  <!-- 页面主体 -->
  <div class="page-content">
    <div class="block">
      <!-- 首页顶部大图 -->
      <el-image class="hI" src="/assets/image/home/homepageImage.png" />
      
      <!-- 导航栏 -->
      <NavBar />

      <div class="bottom-row">
        <!-- 路由出口 -->
        <div class="zuo">
          <router-view />
        </div>
        
        <!-- 右侧侧边栏 -->
        <Sidebar />
      </div>
    </div>
  </div>

  <!-- 页脚 -->
  <Footer />
</template>

<script setup>
// 导入公共组件
import HeaderLang from '@/components/common/HeaderLang.vue'
import NavBar from '@/components/common/NavBar.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Footer from '@/components/common/Footer.vue'
</script>

<style scoped>
.page-content {
  width: 100%;
  background: #F7F7F7;
  padding-bottom: 30px;
  margin-top: 20px;
}
.block {
  margin-top: 20px;
  text-align: center;
}
.hI {
  width: 1200px;
  max-width: 100%;
  height: auto;
}
.bottom-row {
  display: flex;
  width: 1200px;
  max-width: 100%;
  margin: 20px auto;
  gap: 16px;
  align-items: flex-start;
}
.zuo {
  width: 800px;
  flex-shrink: 0;
}
/* 响应式布局 */
@media (max-width: 1300px) {
  .bottom-row {
    width: 100%;
    padding: 0 20px;
  }
  .zuo {
    width: calc(100% - 360px);
    min-width: 0;
  }
}
@media (max-width: 992px) {
  .bottom-row {
    flex-direction: column;
  }
  .zuo {
    width: 100%;
  }
}
</style>