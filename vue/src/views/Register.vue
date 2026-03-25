<template>
  <div class="login-container">
    <div class="login-content">
      <div class="system-title">助农·农产品销售系统</div>
      <div class="login-box">
        <div class="login-title">欢迎注册</div>
        <el-form :model="formData" ref="formRef" :rules="rules">
          <el-form-item prop="username">
            <el-input :prefix-icon="User" size="large" v-model="formData.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input :prefix-icon="Phone" size="large" v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input :prefix-icon="Lock" size="large" v-model="formData.password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input :prefix-icon="Lock" size="large" v-model="formData.confirmPassword" placeholder="请确认密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button size="large" type="primary" style="width: 100%" @click="register">注册</el-button>
          </el-form-item>
        </el-form>
        <div class="register-link">
          已有账号？请 <a href="/login">登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import { User, Lock, Phone } from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";

// 密码验证规则
const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 手机号验证规则
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'USER'
})

const rules = reactive({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' },
  ],
})

// 监听用户名变化，自动设置姓名字段
watch(() => formData.username, (newUsername) => {
  if (newUsername) {
    formData.name = newUsername;
  }
})

const formRef = ref()

// 点击注册按钮的时候会触发这个方法
const register = () => {
  formRef.value.validate((valid => {
    if (valid) {
      // 确保姓名字段设置为用户名
      if (!formData.name && formData.username) {
        formData.name = formData.username;
      }

      // 创建要发送的数据，不包含confirmPassword
      const submitData = {
        username: formData.username,
        phone: formData.phone,
        password: formData.password,
        name: formData.name || formData.username,
        role: 'USER'
      }

      // 调用后台的接口
      request.post('/register', submitData).then(res => {
        if (res.code === '200') {
          ElMessage.success("注册成功")
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })).catch(error => {
    console.error(error)
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background-image: url("../assets/imgs/bg2.png");
  background-size: cover;
  background-position: center;
  padding-right: 8%;
}

.login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 480px;
}

.system-title {
  color: #39bf23;
  font-size: 42px;
  text-align: center;
  font-weight: bold;
  margin-bottom: 50px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
  line-height: 1.3;
}

.login-box {
  width: 100%;
  padding: 50px 40px;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  background-color: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-title {
  font-weight: bold;
  font-size: 28px;
  text-align: center;
  margin-bottom: 40px;
  color: #39bf23; /* 修改为系统主题绿色 */
  letter-spacing: 2px;
}

.el-form-item {
  margin-bottom: 28px;
}

/* 增大输入框高度 */
:deep(.el-input--large) {
  --el-input-height: 52px;
}

:deep(.el-input__wrapper) {
  padding: 0 15px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #39bf23;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #39bf23;
  box-shadow: 0 0 0 2px rgba(57, 191, 35, 0.2);
}

/* 输入框图标颜色 */
:deep(.el-input__prefix) .el-icon {
  color: #39bf23;
  font-size: 18px;
}

/* 增大按钮高度 */
:deep(.el-button--large) {
  --el-button-size: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background-color: #39bf23;
  border-color: #39bf23;
}

:deep(.el-button--large:hover) {
  background-color: #2fa01c;
  border-color: #2fa01c;
}

.register-link {
  text-align: right;
  margin-top: 20px;
  font-size: 15px;
  color: #666;
}

.register-link a {
  color: #39bf23;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: color 0.3s ease;
}

.register-link a:hover {
  text-decoration: underline;
  color: #2fa01c;
}

/* 输入框占位符颜色 */
:deep(.el-input__inner::placeholder) {
  color: #999;
}

/* 表单标签颜色（如果有的话） */
:deep(.el-form-item__label) {
  color: #39bf23;
}

@media (max-width: 1200px) {
  .login-container {
    padding-right: 15%;
  }
}

@media (max-width: 768px) {
  .login-container {
    justify-content: center;
    padding-right: 0;
    padding: 20px;
  }

  .system-title {
    font-size: 32px;
    margin-bottom: 30px;
  }

  .login-box {
    width: 100%;
    max-width: 450px;
    padding: 40px 30px;
  }

  .login-title {
    font-size: 24px;
    margin-bottom: 30px;
  }
}

@media (max-width: 480px) {
  .system-title {
    font-size: 28px;
  }

  .login-box {
    padding: 30px 20px;
  }

  .login-title {
    font-size: 22px;
  }
}
</style>