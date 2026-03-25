<template>
  <div class="login-container">
    <div class="login-content">
      <div class="system-title">助农·农产品销售系统</div>
      <div class="login-box">
        <div class="login-title">欢迎登录</div>
        <el-form :model="data.form" ref="formRef" :rules="data.rules">
          <el-form-item prop="username">
            <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item prop="role">
            <el-select size="large" style="width: 100%" v-model="data.form.role">
              <el-option value="ADMIN" label="管理员"></el-option>
              <el-option value="USER" label="普通用户"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button size="large" type="primary" style="width: 100%" @click="login">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="register-link">
          还没有账号？请 <a href="/register">注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const data = reactive({
  form: { role: 'ADMIN' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
    ],
  }
})

const formRef = ref()

// 点击登录按钮的时候会触发这个方法
// 在 Login.vue 的 login 方法中修改
const login = () => {
  formRef.value.validate((valid => {
    if (valid) {
      // 调用后台的接口
      request.post('/login', data.form).then(res => {
        console.log('登录接口返回:', res)

        if (res.code === '200') {
          ElMessage.success("登录成功")

          // 适配后端返回的数据结构
          const backendData = res.data
          console.log('后端返回的原始数据:', backendData)

          // 直接使用正确的字段名，不要进行字段映射
          const userInfo = {
            id: backendData.id,
            username: backendData.username,
            name: backendData.name,
            // 直接使用 avatar 字段
            avatar: backendData.avatar || '',
            // 直接使用 sex 字段
            sex: backendData.sex || '',
            phone: backendData.phone,
            email: backendData.email,
            role: backendData.role,
            password: backendData.password,
            // 生成临时token，因为后端返回的token为null
            token: backendData.token || generateTempToken()
          }

          console.log('转换后的用户信息:', userInfo)

          localStorage.setItem('system-user', JSON.stringify(userInfo))
          router.push('/home')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })).catch(error => {
    console.error(error)
  })
}

// 生成临时token
const generateTempToken = () => {
  return 'token_' + Date.now() + '_' + Math.random().toString(36).substr(2)
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

/* 增大选择器 */
:deep(.el-select--large) {
  --el-input-height: 52px;
}

/* 选择器下拉菜单样式 */
:deep(.el-select-dropdown) {
  border: 1px solid #39bf23;
}

:deep(.el-select-dropdown__item.selected) {
  color: #39bf23;
  font-weight: 600;
}

:deep(.el-select-dropdown__item:hover) {
  background-color: rgba(57, 191, 35, 0.1);
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

  .login-content {
    margin-right: 0;
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