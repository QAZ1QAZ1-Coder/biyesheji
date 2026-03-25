<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-button type="primary" @click="handleAdd">新增地址</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="收货人" prop="consignee" width="120"></el-table-column>
        <el-table-column label="手机号" prop="phone" width="130"></el-table-column>
        <el-table-column label="省份" prop="province" width="100"></el-table-column>
        <el-table-column label="城市" prop="city" width="100"></el-table-column>
        <el-table-column label="区域" prop="district" width="100"></el-table-column>
        <el-table-column label="详细地址" prop="detail" min-width="200"></el-table-column>
        <el-table-column label="默认地址" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault" type="success">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.tableData.length === 0">
      <el-empty description="暂无收货地址">
        <el-button type="primary" @click="handleAdd">新增地址</el-button>
      </el-empty>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog
        :title="data.dialogTitle"
        v-model="data.dialogVisible"
        width="600px"
        @close="handleDialogClose"
    >
      <el-form
          ref="formRef"
          :model="data.form"
          :rules="data.rules"
          label-width="80px"
      >
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="data.form.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="data.form.phone" placeholder="请输入手机号码" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input v-model="data.form.province" placeholder="省" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input v-model="data.form.city" placeholder="市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区域" prop="district">
              <el-input v-model="data.form.district" placeholder="区/县" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="detail">
          <el-input
              v-model="data.form.detail"
              type="textarea"
              :rows="3"
              placeholder="请输入详细地址（街道、门牌号等）"
          />
        </el-form-item>

        <el-form-item label="默认地址">
          <el-checkbox v-model="data.form.isDefault">设为默认收货地址</el-checkbox>
          <div style="font-size: 12px; color: #666; margin-top: 5px;">
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="data.dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import request from "@/utils/request";
import { useRouter } from 'vue-router';

const formRef = ref();
const router = useRouter();

const data = reactive({
  tableData: [],
  dialogVisible: false,
  dialogTitle: '新增地址',
  form: {
    id: null,
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  },
  rules: {
    consignee: [
      { required: true, message: '请输入收货人姓名', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号码', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    province: [
      { required: true, message: '请输入省份', trigger: 'blur' }
    ],
    city: [
      { required: true, message: '请输入城市', trigger: 'blur' }
    ],
    district: [
      { required: true, message: '请输入区域', trigger: 'blur' }
    ],
    detail: [
      { required: true, message: '请输入详细地址', trigger: 'blur' }
    ]
  }
});

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  if (time.includes(' ')) {
    return time.split(' ')[0];
  }
  return time;
};

// 获取当前用户
const getUser = () => {
  return JSON.parse(localStorage.getItem('system-user') || '{}');
};

// 检查用户权限
const checkPermission = () => {
  const user = getUser();
  if (user.role === 'ADMIN') {
    ElMessage.warning('管理员请使用用户地址管理功能');
    router.push('/adminAddress');
    return false;
  }

  // 添加用户ID检查
  if (!user.id) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return false;
  }

  return true;
};



// 加载地址列表
const loadAddressList = () => {
  if (!checkPermission()) return;

  const user = getUser();
  if (!user.id) return;

  request.get(`/address/selectByUserId/${user.id}`).then(res => {
    data.tableData = res.data || [];
  });
};

// 新增地址
const handleAdd = () => {
  if (!checkPermission()) return;
  data.dialogTitle = '新增地址';
  data.dialogVisible = true;
};

// 编辑地址
const handleEdit = (item) => {
  if (!checkPermission()) return;
  data.dialogTitle = '编辑地址';
  data.form = { ...item };
  data.dialogVisible = true;
};

// 删除地址
const handleDelete = (item) => {
  if (!checkPermission()) return;
  ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/address/delete/${item.id}`).then(res => {
      ElMessage.success('删除成功');
      loadAddressList();
    });
  }).catch(() => {});
};

// 提交表单
const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const user = getUser();
      const formData = {
        ...data.form,
        userId: user.id
      };

      const requestPromise = data.form.id
          ? request.put('/address/update', formData)
          : request.post('/address/add', formData);

      requestPromise.then(res => {
        ElMessage.success(data.form.id ? '修改成功' : '新增成功');
        data.dialogVisible = false;
        loadAddressList();
      });
    }
  });
};

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields();
  data.form = {
    id: null,
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: false
  };
};

// 初始化加载
onMounted(() => {
  loadAddressList();
});
</script>

<style scoped>
.card {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>