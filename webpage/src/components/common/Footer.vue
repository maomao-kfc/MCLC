<!-- 页脚组件 -->
<template>
  <div class="diBu">
    <div class="xinXi">
      <el-image src="/assets/image/common/diLoGo.png" class="diLoGo" />
      <span class="diLoGoWenZi">大会官网内容未经允许，严禁拷贝或转载，否则追究法律责任！|京ICP备15042885号-1</span>
    </div>
  </div>
</template>

<script setup>
	
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
</style>