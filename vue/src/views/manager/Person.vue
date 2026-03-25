<template>
  <div style="width: 50%">
    <div class="card" style="padding: 30px">
      <el-form :model="form" label-width="100px" style="padding-right: 50px">
        <div style="margin: 20px 0; text-align: center">
          <el-upload :show-file-list="false" class="avatar-uploader" :action="uploadUrl" :on-success="handleFileUpload">
            <img v-if="form.avatar" :src="getImageUrl(form.avatar)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>
        <el-form-item label="账号">
          <el-input disabled v-model="form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>

        <!-- 普通用户显示额外字段 -->
        <div v-if="form.role === 'USER'">
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="form.sex">
              <el-radio label="男">男</el-radio>
              <el-radio label="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="form.phone" autocomplete="off" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" autocomplete="off" />
          </el-form-item>
        </div>

        <div style="text-align: center">
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Plus } from '@element-plus/icons-vue'

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

// 使用 reactive 管理表单数据
const form = reactive({
  id: '',
  username: '',
  name: '',
  avatar: '',
  sex: '',
  phone: '',
  email: '',
  role: ''
})

// 图片URL处理
const getImageUrl = (avatar) => {
  if (!avatar) {
    return ''
  }

  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http')) {
    // 修复URL中的路径：download -> preview
    if (avatar.includes('/files/download/')) {
      avatar = avatar.replace('/files/download/', '/files/preview/')
    }
    return avatar
  }

  // 如果是文件名，拼接完整URL
  return `http://192.168.88.1:9090/files/preview/${avatar}`
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 获取本地存储的用户信息
    const localUser = JSON.parse(localStorage.getItem('system-user') || '{}')

    if (!localUser.id) {
      ElMessage.error('用户未登录')
      return
    }

    let res
    if (localUser.role === 'ADMIN') {
      // 管理员根据ID查询管理员信息
      res = await request.get('/admin/selectById/' + localUser.id)
    } else {
      // 普通用户根据ID查询用户信息
      res = await request.get('/user/selectById/' + localUser.id)
    }

    if (res.code === '200' && res.data) {
      // 直接使用后端返回的字段名
      const userData = res.data
      const formattedData = {
        id: userData.id,
        username: userData.username,
        name: userData.name,
        avatar: userData.avatar || '',
        sex: userData.sex || '',
        phone: userData.phone,
        email: userData.email,
        role: userData.role || localUser.role // 保留原有角色
      }

      // 将转换后的数据设置到表单中
      Object.assign(form, formattedData)

      // 更新本地存储（保留token）
      const updatedUser = {
        ...localUser,
        ...formattedData,
        token: localUser.token // 保留token
      }
      localStorage.setItem('system-user', JSON.stringify(updatedUser))
    } else {
      ElMessage.error(res.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const handleFileUpload = (file) => {
  if (file && file.data) {
    form.avatar = file.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

const emit = defineEmits(["updateUser"])

// 保存个人信息
const save = async () => {
  if (!form.name) {
    ElMessage.error('请输入名称')
    return
  }

  // 根据角色构建不同的请求数据
  let requestData = {}

  if (form.role === 'ADMIN') {
    // 管理员更新数据
    requestData = {
      id: form.id,
      username: form.username,
      name: form.name,
      avatar: form.avatar,
      role: form.role // 添加 role 字段
    }
  } else {
    // 普通用户更新数据
    requestData = {
      id: form.id,
      username: form.username,
      name: form.name,
      avatar: form.avatar,
      sex: form.sex,
      phone: form.phone,
      email: form.email,
      role: form.role // 添加 role 字段
    }
  }

  // 调试：查看要发送的数据
  console.log('保存数据:', requestData)

  try {
    const apiUrl = form.role === 'ADMIN' ? '/admin/update' : '/user/update'
    const res = await request.put(apiUrl, requestData)

    if (res.code === '200') {
      ElMessage.success('更新成功')
      // 重新加载用户信息确保数据同步
      await loadUserInfo()
      emit('updateUser')
    } else {
      ElMessage.error(res.msg || '更新失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 组件挂载时加载用户信息
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 120px;
  height: 120px;
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}
</style>