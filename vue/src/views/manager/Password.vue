<template>
  <div style="width: 50%">
    <div class="card" style="padding: 30px">
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="原密码">
          <el-input v-model="data.form.password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="data.form.newPassword" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="data.form.confirmPassword" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <div style="text-align: center">
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue"
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const data = reactive({
  form: {
    password: '',
    newPassword: '',
    confirmPassword: ''
  },
  userInfo: JSON.parse(localStorage.getItem('system-user') || '{}')
})

// 修改密码
const save = () => {
  // 验证输入
  if (!data.form.password) {
    ElMessage.error('请输入原密码')
    return
  }
  if (!data.form.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (!data.form.confirmPassword) {
    ElMessage.error('请确认新密码')
    return
  }

  // 验证密码一致性
  if (data.form.newPassword !== data.form.confirmPassword) {
    ElMessage.error('新密码和确认密码不一致')
    return
  }

  // 构建请求数据
  const requestData = {
    username: data.userInfo.username,
    password: data.form.password,
    newPassword: data.form.newPassword,
    role: data.userInfo.role || 'USER'
  }

  console.log('修改密码请求数据:', requestData)

  request.put('/updatePassword', requestData).then(res => {
    if (res.code === '200') {
      ElMessage.success('修改密码成功')
      // 清空缓存
      localStorage.removeItem('system-user')
      // 跳转到登录页
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '修改密码失败')
    }
  }).catch(error => {
    console.error('修改密码错误:', error)
    ElMessage.error('修改密码失败，请重试')
  })
}
</script>