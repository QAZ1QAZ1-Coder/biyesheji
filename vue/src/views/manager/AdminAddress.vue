<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-select v-model="data.queryType" style="width: 120px; margin-right: 10px" placeholder="查询类型">
        <el-option label="按收货人" value="consignee"></el-option>
        <el-option label="按手机号" value="phone"></el-option>
        <el-option label="按用户名" value="userName"></el-option>
      </el-select>
      <el-input v-model="data.queryValue" style="width: 300px; margin-right: 10px" :placeholder="getPlaceholder()"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="所属用户" prop="userName" width="120"></el-table-column>
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
        <el-table-column label="操作" align="center" width="100" fixed="right">
          <template #default="scope">
            <el-button type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.tableData.length === 0">
      <el-empty description="暂无地址数据"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import request from "@/utils/request";

const data = reactive({
  tableData: [],
  allTableData: [],
  queryType: 'consignee',
  queryValue: ''
});

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  if (time.includes(' ')) {
    return time.split(' ')[0];
  }
  return time;
};

// 获取输入框的placeholder
const getPlaceholder = () => {
  switch(data.queryType) {
    case 'consignee': return '请输入收货人查询'
    case 'phone': return '请输入手机号查询'
    case 'userName': return '请输入用户名查询'
    default: return '请输入查询内容'
  }
}

// 加载所有用户地址
const loadAllAddresses = () => {
  request.get('/address/admin/all').then(res => {
    data.allTableData = res.data || [];
    data.tableData = data.allTableData;
  }).catch(error => {
    console.error('加载地址数据失败:', error);
    ElMessage.error('加载数据失败');
  });
};

// 搜索功能
const load = () => {
  if (!data.queryValue) {
    data.tableData = data.allTableData;
    return;
  }

  const keyword = data.queryValue.toLowerCase();
  data.tableData = data.allTableData.filter(item => {
    switch(data.queryType) {
      case 'consignee':
        return item.consignee?.toLowerCase().includes(keyword);
      case 'phone':
        return item.phone?.includes(keyword);
      case 'userName':
        return item.userName?.toLowerCase().includes(keyword);
      default:
        return true;
    }
  });
};

// 重置搜索
const reset = () => {
  data.queryValue = '';
  data.tableData = data.allTableData;
}

// 删除地址（管理员权限）
const handleDelete = (item) => {
  ElMessageBox.confirm(`确定要删除用户"${item.userName}"的地址吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/address/admin/delete/${item.id}`).then(res => {
      ElMessage.success('删除成功');
      loadAllAddresses();
    }).catch(error => {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    });
  }).catch(() => {});
};

// 初始化加载
onMounted(() => {
  loadAllAddresses();
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