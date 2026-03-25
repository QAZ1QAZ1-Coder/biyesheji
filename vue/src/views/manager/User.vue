<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-select v-model="data.queryType" style="width: 120px; margin-right: 10px" placeholder="查询类型">
        <el-option label="按名称" value="name"></el-option>
        <el-option label="按用户名" value="username"></el-option>
        <el-option label="按电话" value="phone"></el-option>
      </el-select>
      <el-input v-model="data.queryValue" style="width: 300px; margin-right: 10px" :placeholder="getPlaceholder()"></el-input>

      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="头像">
          <template #default="scope">
            <el-image
                :src="getAvatarUrl(scope.row.avatar)"
                style="width: 40px; height: 40px; border-radius: 50%"
                :preview-src-list="[getAvatarUrl(scope.row.avatar)]"
                preview-teleported
                hide-on-click-modal
            >
              <template #error>
                <div style="width: 40px; height: 40px; border-radius: 50%; background: #f5f5f5; display: flex; align-items: center; justify-content: center;">
                  <el-icon><User /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role">
          <template #default="scope">
            <span v-if="scope.row.role === 'USER'">普通用户</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" prop="sex"></el-table-column>
        <el-table-column label="电话" prop="phone"></el-table-column>
        <el-table-column label="邮箱" prop="email"></el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
      />
    </div>

    <el-dialog title="信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 50px" ref="formRef">
        <el-form-item label="头像" prop="avatar">
          <el-upload
              :action="uploadUrl"
              list-type="picture"
              :on-success="handleImgSuccess"
              :show-file-list="false"
          >
            <el-button type="primary">上传图片</el-button>
          </el-upload>
          <div v-if="data.form.avatar" style="margin-top: 10px">
            <el-image
                :src="getAvatarUrl(data.form.avatar)"
                style="width: 60px; height: 60px; border-radius: 50%"
                :preview-src-list="[getAvatarUrl(data.form.avatar)]"
                preview-teleported
                hide-on-click-modal
            >
              <template #error>
                <div style="width: 60px; height: 60px; border-radius: 50%; background: #f5f5f5; display: flex; align-items: center; justify-content: center;">
                  <el-icon><User /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="data.form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="data.form.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="data.form.phone" autocomplete="off" placeholder="请输入11位手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="data.form.email" autocomplete="off" placeholder="请输入邮箱地址" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { User } from '@element-plus/icons-vue'

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

// 创建表单引用
const formRef = ref()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  queryType: 'name',
  queryValue: null,
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 2, max: 20, message: '账号长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' },
      { min: 2, max: 20, message: '名称长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    email: [
      { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱格式', trigger: 'blur' }
    ]
  }
})

// 统一的头像URL处理函数
const getAvatarUrl = (avatar) => {
  // 如果头像为空或未定义，返回默认头像
  if (!avatar) {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }

  // 如果已经是完整的HTTP/HTTPS URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }

  // 如果是相对路径或文件名，拼接为完整URL
  const baseUrl = import.meta.env.VITE_BASE_URL || 'http://localhost:9090'
  return `${baseUrl}/files/${avatar}`
}

// 获取输入框的placeholder
const getPlaceholder = () => {
  switch(data.queryType) {
    case 'name': return '请输入名称查询'
    case 'username': return '请输入用户名查询'
    case 'phone': return '请输入电话查询'
    default: return '请输入查询内容'
  }
}

// 页码变化
const handleCurrentChange = (pageNum) => {
  data.pageNum = pageNum
  load()
}

// 每页条数变化
const handleSizeChange = (pageSize) => {
  data.pageSize = pageSize
  data.pageNum = 1
  load()
}

// 分页查询
const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize
  }

  if (data.queryValue) {
    params[data.queryType] = data.queryValue
  }

  request.get('/user/selectPage', { params })
      .then(res => {
        if (res.code === '200') {
          data.tableData = res.data?.list || []
          data.total = res.data?.total || 0
        } else {
          ElMessage.error(res.msg || '加载失败')
          data.tableData = []
          data.total = 0
        }
      })
      .catch(error => {
        console.error('加载用户列表失败:', error)
        ElMessage.error('加载用户列表失败')
        data.tableData = []
        data.total = 0
      })
}

// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/user/add', data.form)
      .then(res => {
        if (res.code === '200') {
          load()
          ElMessage.success('操作成功')
          data.formVisible = false
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      })
      .catch(error => {
        console.error('新增用户失败:', error)
        ElMessage.error('新增用户失败')
      })
}

// 编辑保存
const update = () => {
  request.put('/user/update', data.form)
      .then(res => {
        if (res.code === '200') {
          load()
          ElMessage.success('操作成功')
          data.formVisible = false
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      })
      .catch(error => {
        console.error('编辑用户失败:', error)
        ElMessage.error('编辑用户失败')
      })
}

// 弹窗保存
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 前端再次验证手机号格式（双重保险）
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(data.form.phone)) {
        ElMessage.error('请输入正确的手机号码格式')
        return
      }

      // 验证邮箱格式（如果邮箱不为空）
      if (data.form.email && data.form.email.trim() !== '') {
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
        if (!emailRegex.test(data.form.email)) {
          ElMessage.error('请输入正确的邮箱格式')
          return
        }
      }

      // data.form有id就是更新，没有就是新增
      data.form.id ? update() : add()
    } else {
      ElMessage.error('请正确填写表单信息')
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
      .then(() => {
        request.delete('/user/delete/' + id)
            .then(res => {
              if (res.code === '200') {
                load()
                ElMessage.success('操作成功')
              } else {
                ElMessage.error(res.msg || '删除失败')
              }
            })
            .catch(error => {
              console.error('删除用户失败:', error)
              ElMessage.error('删除用户失败')
            })
      })
      .catch(() => {
        // 用户取消删除
      })
}

// 重置
const reset = () => {
  data.queryValue = null
  load()
}

// 处理文件上传的钩子
const handleImgSuccess = (res) => {
  if (res.code === '200') {
    data.form.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '头像上传失败')
  }
}

load()
</script>

<style scoped>
.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 头像预览样式 */
:deep(.el-image-viewer__wrapper) {
  z-index: 9999 !important;
}

:deep(.el-image-viewer__close) {
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  line-height: 40px;
}
</style>