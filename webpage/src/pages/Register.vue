<!-- 注册页面 -->

<template>
  <div class="register-page">
    <h2>用户注册</h2>
    <!-- 若需要区分个人/团队，可显示不同表单 -->
    <div v-if="registerType === 'personal'">
      <h3>个人注册</h3>
    </div>
    <div v-else>
      <h3>团队注册</h3>
    </div>

    <form @submit.prevent="handleRegister">
      <div class="form-item">
        <label>账号：</label>
        <input v-model="form.username" type="text" placeholder="请输入账号" required />
      </div>
      <div class="form-item">
        <label>密码：</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" required />
      </div>
      <!-- 团队注册可额外加字段（如企业名称） -->
      <div v-if="registerType === 'team'" class="form-item">
        <label>企业名称：</label>
        <input v-model="form.company" type="text" placeholder="请输入企业名称" />
      </div>
      <button type="submit" class="submit-btn">提交注册</button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
// 接收侧边栏传递的注册类型（个人/团队）
const registerType = ref('personal')
// 注册表单数据
const form = ref({
  username: '',
  password: '',
  company: '' // 团队注册专属
})

onMounted(() => {
  // 读取路由参数，区分个人/团队注册
  if (route.query.type === 'team') {
    registerType.value = 'team'
  }
})

// 注册提交逻辑（预留后端接口对接）
const handleRegister = async () => {
  try {
    // 1. 表单校验（前端）
    if (!form.value.username || !form.value.password) {
      alert('账号/密码不能为空！')
      return
    }

    // 2. 对接后端接口（此处为预留位置）
    // const res = await fetch('/api/register', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     type: registerType.value,
    //     ...form.value
    //   })
    // })
    // const data = await res.json()
    // if (data.code === 200) {
    //   alert('注册成功！')
    //   router.push('/login') // 注册成功跳登录
    // } else {
    //   alert('注册失败：' + data.msg)
    // }

    // 前端临时模拟成功
    alert('注册请求已提交（前端测试），账号：' + form.value.username)
    form.value = { username: '', password: '', company: '' }
  } catch (err) {
    console.error('注册失败：', err)
    alert('注册异常，请重试！')
  }
}
</script>

<style scoped>
.register-page {
  width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
}
.form-item {
  margin: 15px 0;
  display: flex;
  align-items: center;
}
.form-item label {
  width: 80px;
}
.form-item input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.submit-btn {
  width: 100%;
  padding: 10px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>