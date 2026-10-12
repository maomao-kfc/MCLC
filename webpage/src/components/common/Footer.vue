<!-- 页脚组件 -->
<template>
  <div class="diBu">
    <div class="xinXi">
      <el-image src="/assets/image/common/diLoGo.png" class="diLoGo" />
      <span class="diLoGoWenZi">{{ t('footer.copyright') }}</span>
      <a class="icp-link" href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">沪ICP备2026022893号-1</a>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>

<style scoped>
.diBu {
  width: 100%;
  height: 60px;
  background-color: #4A516E;
  display: flex;
  align-items: center;
  justify-content: center;
}

.diBu.fixed-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.xinXi {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.diLoGo {
  width: 120px;
  vertical-align: middle;
}
.diLoGoWenZi {
  color: #C9CEE7;
  font-size: 12px;
  line-height: 1;
}
.icp-link {
  color: #C9CEE7;
  font-size: 12px;
  text-decoration: none;
  line-height: 1;
}
.icp-link:hover {
  color: #fff;
}
</style>