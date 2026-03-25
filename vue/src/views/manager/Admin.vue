<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px">
        <div>
          <el-button type="primary" @click="handleAdd">新增</el-button>
        </div>
        <div style="color: #666; font-size: 14px">
          共 <strong style="color: #409eff">{{ data.total }}</strong> 条记录
        </div>
      </div>

      <el-table :data="data.tableData" stripe v-if="data.tableData.length > 0">
        <el-table-column label="ID" prop="id" width="80"></el-table-column>
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
            <el-tag v-if="scope.row.role === 'ADMIN'" type="success">管理员</el-tag>
            <span v-else>{{ scope.row.role }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空数据提示 -->
      <div v-else style="text-align: center; padding: 50px; color: #909399">
        <el-icon size="48" style="margin-bottom: 16px;"><User /></el-icon>
        <div>暂无管理员数据</div>
        <div style="margin-top: 10px; font-size: 12px; color: #c0c4cc;">
          当前查询条件: 第{{ data.pageNum }}页，每页{{ data.pageSize }}条
        </div>
        <el-button type="primary" @click="handleAdd" style="margin-top: 16px">
          添加管理员
        </el-button>
      </div>
    </div>

    <div class="card" v-if="data.tableData.length > 0">
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

    <el-dialog :title="data.form.id ? '编辑管理员' : '新增管理员'" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px" ref="formRef">
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
        <el-form-item label="账号" prop="username" :rules="[{ required: true, message: '请输入账号', trigger: 'blur' }]">
          <el-input v-model="data.form.username" autocomplete="off" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名称" />
        </el-form-item>
        <!-- 新增时显示密码字段，编辑时不显示 -->
        <el-form-item v-if="!data.form.id" label="密码" prop="password" :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]">
          <el-input v-model="data.form.password" type="password" autocomplete="new-password" placeholder="请输入密码" />
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
import { reactive, onMounted, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { User } from '@element-plus/icons-vue'

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'
const formRef = ref()

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null
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

// 页码变化
const handleCurrentChange = (pageNum) => {
  data.pageNum = pageNum
  load()
}

// 每页条数变化
const handleSizeChange = (pageSize) => {
  data.pageSize = pageSize
  data.pageNum = 1  // 重置到第一页
  load()
}

// 分页查询
const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    name: data.name
  }

  request.get('/admin/selectPage', { params })
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
        console.error('加载管理员列表失败:', error)
        ElMessage.error('加载管理员列表失败')
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
  // 表单验证
  if (!data.form.username || !data.form.name || !data.form.password) {
    ElMessage.error('请填写完整信息')
    return
  }

  const formData = {
    ...data.form,
    role: 'ADMIN'
  }

  request.post('/admin/add', formData)
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
        console.error('新增管理员失败:', error)
        ElMessage.error('新增管理员失败')
      })
}

// 编辑保存
const update = () => {
  // 表单验证
  if (!data.form.username || !data.form.name) {
    ElMessage.error('请填写完整信息')
    return
  }

  // 编辑时不发送密码字段，除非用户明确修改了密码
  const formData = { ...data.form }
  if (!formData.password) {
    delete formData.password
  }

  request.put('/admin/update', formData)
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
        console.error('编辑管理员失败:', error)
        ElMessage.error('编辑管理员失败')
      })
}

// 弹窗保存
const save = () => {
  // data.form有id就是更新，没有就是新增
  if (data.form.id) {
    update()
  } else {
    add()
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
      .then(() => {
        request.delete('/admin/delete/' + id)
            .then(res => {
              if (res.code === '200') {
                load()
                ElMessage.success('操作成功')
              } else {
                ElMessage.error(res.msg || '删除失败')
              }
            })
            .catch(error => {
              console.error('删除管理员失败:', error)
              ElMessage.error('删除管理员失败')
            })
      })
      .catch(() => {
        // 用户取消删除
      })
}

// 重置
const reset = () => {
  data.name = null
  data.pageNum = 1
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

// 组件挂载时加载数据
onMounted(() => {
  load()
})
</script>

<style scoped>
.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-pagination) {
  justify-content: center;
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