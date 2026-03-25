<template>
  <div>
    <!-- 顶部导航栏 - 固定定位 -->
    <div style="height: 60px; background-color: #fff; display: flex; align-items: center; border-bottom: 1px solid #ddd; position: fixed; top: 0; left: 0; right: 0; z-index: 1000;">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #39bf23">助农·农产品销售系统</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <!-- 用户信息下拉菜单 -->
        <el-dropdown @command="handleCommand" style="cursor: pointer;">
          <div style="display: flex; align-items: center; padding: 8px 12px; border-radius: 4px; transition: background-color 0.3s;"
               @mouseenter="hoverUser = true"
               @mouseleave="hoverUser = false"
               :style="{ backgroundColor: hoverUser ? '#f5f7fa' : 'transparent' }">
            <img
                style="width: 40px; height: 40px; border-radius: 50%"
                :src="getUserAvatar(data.user.avatar)"
                alt=""
                onerror="this.src='https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            >
            <span style="margin-left: 8px; font-size: 14px; color: #606266">{{ data.user.name }}</span>
            <el-icon style="margin-left: 4px; color: #909399"><arrow-down /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="person">
                <el-icon><User /></el-icon>
                <span style="margin-left: 8px">个人信息</span>
              </el-dropdown-item>
              <el-dropdown-item command="password">
                <el-icon><Lock /></el-icon>
                <span style="margin-left: 8px">修改密码</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                <span style="margin-left: 8px">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div style="display: flex; margin-top: 60px;">
      <!-- 侧边栏菜单 - 固定定位 -->
      <div style="width: 200px; border-right: 1px solid #ddd; position: fixed; top: 60px; bottom: 0; overflow-y: auto; background: white; z-index: 999;">
        <el-menu
            router
            style="border: none"
            :default-active="router.currentRoute.value.path"
            :default-openeds="['1', '2']"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>
          <el-menu-item index="/buy" v-if="data.user.role === 'USER'">
            <el-icon><Goods /></el-icon>
            <span>农产品购买</span>
          </el-menu-item>
          <el-menu-item index="/orders" >
            <el-icon><Tickets /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/address" v-if="data.user.role === 'USER'">
            <el-icon><Location /></el-icon>
            <span>地址管理</span>
          </el-menu-item>
          <el-sub-menu index="1" v-if="data.user.role === 'ADMIN'">
            <template #title>
              <el-icon><Menu /></el-icon>
              <span>农产品管理</span>
            </template>
            <el-menu-item index="/category">
              <el-icon><Menu /></el-icon>
              <span>农产品分类管理</span>
            </el-menu-item>
            <el-menu-item index="/goods">
              <el-icon><Goods /></el-icon>
              <span>农产品管理</span>
            </el-menu-item>
            <el-menu-item index="/goodsStock">
              <el-icon><SoldOut /></el-icon>
              <span>农产品进货管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/notice" v-if="data.user.role === 'ADMIN'">
            <el-icon><Bell /></el-icon>
            <span>系统公告管理</span>
          </el-menu-item>
          <el-sub-menu index="2" v-if="data.user.role === 'ADMIN'">
            <template #title>
              <el-icon><Memo /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin">
              <el-icon><User /></el-icon>
              <span>管理员信息</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>普通用户信息</span>
            </el-menu-item>
            <el-menu-item index="/adminAddress">
              <el-icon><Location /></el-icon>
              <span>用户地址管理</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- 主要内容区域 -->
      <div style="flex: 1; width: 0; background-color: #f8f8ff; padding: 10px; margin-left: 200px; min-height: calc(100vh - 80px);">
        <router-view @updateUser="updateUser" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";
import { onMounted } from 'vue'
import {
  HomeFilled,
  Goods,
  Tickets,
  Location,
  Menu,
  SoldOut,
  Bell,
  Memo,
  User,
  Lock,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

onMounted(() => {
  console.log('=== Manager用户信息调试 ===')
  console.log('完整用户信息:', data.user)
  console.log('头像字段值:', data.user.avatar)
  console.log('头像处理后的URL:', getUserAvatar(data.user.avatar))
  console.log('Token:', data.user.token)
  console.log('LocalStorage:', localStorage.getItem('system-user'))
})

const hoverUser = ref(false)

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'person':
      router.push('/person')
      break
    case 'password':
      router.push('/password')
      break
    case 'logout':
      logout()
      break
  }
}

// 修复头像URL显示
// 在 Manager.vue 中修改 getUserAvatar 函数
const getUserAvatar = (avatar) => {
  console.log('原始头像URL:', avatar)

  if (!avatar) {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }

  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http')) {
    return avatar
  }

  // 如果是文件名，拼接完整URL
  return `http://192.168.88.1:9090/files/preview/${avatar}`
}


const logout = () => {
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
  router.push('/login')
}
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #e0edfd !important;
}
.el-menu-item:hover {
  color: #1967e3;
}
:deep(th)  {
  color: #333;
}

/* 下拉菜单样式优化 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  padding: 8px 16px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

/* 侧边栏滚动条样式 */
:deep(.el-menu) {
  height: 100%;
}

/* 可选：美化滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>