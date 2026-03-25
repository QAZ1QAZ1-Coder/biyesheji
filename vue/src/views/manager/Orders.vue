<template>
  <div>
    <!-- 查询条件部分保持不变 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-select v-model="data.queryType" style="width: 140px; margin-right: 10px" placeholder="查询类型">
        <el-option label="按订单编号" value="orderNo"></el-option>
        <el-option label="按农产品名称" value="goodsName"></el-option>
        <el-option v-if="data.user.role === 'ADMIN'" label="按下单人" value="userName"></el-option>
        <el-option label="按订单状态" value="status"></el-option>
      </el-select>

      <template v-if="data.queryType === 'status'">
        <el-select v-model="data.queryValue" style="width: 300px; margin-right: 10px" placeholder="请选择订单状态" clearable>
          <el-option label="待支付" value="待支付"></el-option>
          <el-option label="待发货" value="待发货"></el-option>
          <el-option label="待收货" value="待收货"></el-option>
          <el-option label="已完成" value="已完成"></el-option>
          <el-option label="已取消" value="已取消"></el-option>
        </el-select>
      </template>
      <template v-else>
        <el-input v-model="data.queryValue" style="width: 300px; margin-right: 10px" :placeholder="getPlaceholder()"></el-input>
      </template>

      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="订单编号" prop="orderNo"></el-table-column>
        <el-table-column label="农产品名称" prop="goodsName"></el-table-column>
        <el-table-column label="农产品图片" prop="goodsImg">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px; border-radius: 5px" :src="scope.row.goodsImg"
                      :preview-src-list="[scope.row.goodsImg]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <el-table-column label="购买数量" prop="num"></el-table-column>
        <el-table-column label="下单人" prop="userName"></el-table-column>

        <!-- 新增地址信息列 -->
        <el-table-column label="收货地址" prop="addressInfo" min-width="200">
          <template #default="scope">
            <div v-if="scope.row.addressInfo">
              <el-tooltip :content="scope.row.addressInfo" placement="top">
                <span class="address-text">{{ scope.row.addressInfo }}</span>
              </el-tooltip>
            </div>
            <div v-else style="color: #999">无地址信息</div>
          </template>
        </el-table-column>

        <el-table-column label="订单状态" prop="status">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '已取消'" type="danger">已取消</el-tag>
            <el-tag v-if="scope.row.status === '待支付'" type="warning">待支付</el-tag>
            <el-tag v-if="scope.row.status === '待发货'" type="primary">待发货</el-tag>
            <el-tag v-if="scope.row.status === '待收货'" type="primary">待收货</el-tag>
            <el-tag v-if="scope.row.status === '已完成'" type="success">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="time"></el-table-column>
        <el-table-column label="操作"  width="160">
          <template #default="scope">
            <el-button v-if="data.user.role === 'USER' && scope.row.status === '待支付'" type="primary" @click="changeStatus(scope.row,'待发货')">支付</el-button>
            <el-button v-if="data.user.role === 'ADMIN'&& scope.row.status === '待发货'" type="primary" @click="changeStatus(scope.row,'待收货')">发货</el-button>
            <el-button v-if="data.user.role === 'USER' && scope.row.status === '待收货'" type="success" @click="changeStatus(scope.row,'已完成')">收货</el-button>
            <el-button v-if="data.user.role === 'USER' && scope.row.status === '待支付'" type="danger" @click="changeStatus(scope.row,'已取消')">取消</el-button>
            <el-button v-if="data.user.role === 'ADMIN'" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件保持不变 -->
    <div class="card" v-if="data.total">
      <el-pagination
          background
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  // 查询条件
  queryType: 'orderNo', // 查询类型
  queryValue: null      // 查询值
})

// 获取输入框的placeholder
const getPlaceholder = () => {
  switch(data.queryType) {
    case 'orderNo': return '请输入订单编号查询'
    case 'goodsName': return '请输入农产品名称查询'
    case 'userName': return '请输入下单人姓名查询'
    case 'status': return '请选择订单状态'
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
  data.pageNum = 1  // 重置到第一页
  load()
}

// 分页查询
const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize
  }

  // 根据查询类型动态设置参数
  if (data.queryValue) {
    params[data.queryType] = data.queryValue
  }

  // 如果是普通用户，只查询自己的订单
  if (data.user.role === 'USER') {
    params.userId = data.user.id
  }

  request.get('/orders/selectPage', {
    params: params
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}

// 操作按钮
const changeStatus = (row,status) => {
  row.status = status
  request.put('/orders/update', row).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/orders/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.queryValue = null
  data.pageNum = 1
  load()
}

// 处理文件上传的钩子
const handleImgSuccess = (res) => {
  data.form.avatar = res.data  // res.data就是文件上传返回的文件路径，获取到路径后赋值表单的属性
}

load()
</script>

<style scoped>
.address-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}
</style>