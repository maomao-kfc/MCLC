<template>
  <!-- 🔥 必须只有一个根节点（修复报错核心1） -->
  <div class="faq-wrapper">
    <h2 class="faq-main-title">常见问题解答(FAQ)</h2>

    <div class="faq-greeting">
      <p class="greeting-title">尊敬的各位嘉宾:</p>
      <p class="greeting-desc">
        欢迎您莅临本次大会!为了帮助您更好地规划行程并顺利参会,我们整理了以下常见问题与解答。请随时查阅,如有未尽事宜,敬请通过文末联系方式与我们联系。
      </p>
    </div>

    <div class="faq-section">
      <h3 class="section-title">一、大会基本信息</h3>
      <div class="faq-item">
        <p class="faq-question">Q1:大会的具体日期、时间和地点?</p>
        <p class="faq-answer">
          A:大会将于2026年1月15日至1月17日在上海绿地铂瑞酒店举行。<br>
          地址:上海市青浦区诸光路1588弄100号<br>
          大会日程详见:
          <a href="https://www.medcircle.cn/meeting/index/6148?sourceCode=null" target="_blank" class="faq-link">
            https://www.medcircle.cn/meeting/index/6148?sourceCode=null
          </a>
        </p>
      </div>
    </div>

    <div class="faq-section">
      <h3 class="section-title">二、注册与签到</h3>
      <div class="faq-item">
        <p class="faq-question">Q2:如何注册、签到？</p>
        <p class="faq-answer">A:请在官网注册，现场凭二维码签到。</p>
      </div>
    </div>

    <div class="faq-footer">
      <p class="closing-text">我们期待在上海与您相见,共襄学术盛举!</p>
      <div class="contact-info">
        <p>大会名称:中国“海上之光”眼视光学与视觉科学学术会议</p>
        <p>联系人:梅老师</p>
        <p>联系电话:13062687248</p>
        <p>联系邮箱:organizerMCLC@163.com</p>
      </div>
    </div>

    <!-- 🔥 图片路径匹配你的 PUBLIC 结构（修复404） -->
    <div class="city-bg">
      <el-image src="/assets/image/faq/changJianDiTu.png" fit="width" class="city-img" />
    </div>
  </div>
</template>

<script setup>
// 🔥 修复报错核心2：必须导入国际化
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>

<style scoped>
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