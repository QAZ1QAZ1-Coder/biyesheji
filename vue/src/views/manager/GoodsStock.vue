<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-select v-model="data.queryType" style="width: 140px; margin-right: 10px" placeholder="查询类型">
        <el-option label="按农产品名称" value="goodsName"></el-option>
        <el-option label="按进货渠道" value="channel"></el-option>
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
        <el-table-column label="农产品名称" prop="goodsName"></el-table-column>
        <el-table-column label="进货数量" prop="num"></el-table-column>
        <el-table-column label="进货渠道" prop="channel"></el-table-column>
        <el-table-column label="进货日期" prop="date"></el-table-column>
        <el-table-column label="备注" prop="comment"></el-table-column>
        <el-table-column label="操作" header-align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
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

    <el-dialog title="进货信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="农产品" prop="goodsName">
          <!-- 修改为可创建新选项的下拉框 -->
          <el-select
              v-model="data.form.goodsName"
              filterable
              clearable
              allow-create
              default-first-option
              placeholder="请输入或选择农产品"
              style="width: 100%"
              @change="handleGoodsNameChange"
          >
            <el-option
                v-for="item in data.goodsList"
                :key="item.id"
                :label="item.name"
                :value="item.name"
            >
              <div class="goods-option">
                <span>{{ item.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  库存: {{ item.store }} {{ item.unit }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item  label="进货数量" prop="num" :rules="[{ required: true, message: '进货数量不能为空' }]">
          <el-input-number :min="1" v-model="data.form.num"></el-input-number>
        </el-form-item>

        <el-form-item label="进货渠道" prop="channel">
          <el-input v-model="data.form.channel" placeholder="请输入进货渠道"></el-input>
        </el-form-item>

        <el-form-item label="进货日期" prop="date">
          <el-date-picker v-model="data.form.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD"></el-date-picker>
        </el-form-item>

        <el-form-item label="备注" prop="comment">
          <el-input v-model="data.form.comment" placeholder="请输入备注"></el-input>
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
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {
    goodsName: '',
    goodsId: null,
    num: null,
    channel: '',
    date: '',
    comment: ''
  },
  tableData: [],
  goodsList: [],
  queryType: 'goodsName',
  queryValue: null
})

// 获取输入框的placeholder
const getPlaceholder = () => {
  switch(data.queryType) {
    case 'goodsName': return '请输入农产品名称查询'
    case 'channel': return '请输入进货渠道查询'
    default: return '请输入查询内容'
  }
}

// 农产品名称变化时，检查是否已存在
const handleGoodsNameChange = (goodsName) => {
  if (!goodsName) {
    data.form.goodsId = null
    return
  }

  // 查找对应的商品ID
  const goods = data.goodsList.find(item => item.name === goodsName)
  if (goods) {
    data.form.goodsId = goods.id
    data.form.goodsName = goods.name
  } else {
    // 如果不存在，goodsId设为null，后端会自动创建
    data.form.goodsId = null
  }
}

// 获取农产品数据
request.get('/goods/selectAll').then(res =>{
  data.goodsList = res.data || []
})

// 分页查询
const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize
  }

  if (data.queryValue) {
    params[data.queryType] = data.queryValue
  }

  request.get('/goodsStock/selectPage', {
    params: params
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
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

// 新增
const handleAdd = () => {
  data.form = {
    goodsName: '',
    goodsId: null,
    num: null,
    channel: '',
    date: '',
    comment: ''
  }
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))

  // 编辑时需要设置goodsName（从数据库查询到的是goodsName字段）
  // 同时也要设置goodsId
  const goods = data.goodsList.find(item => item.id === row.goodsId)
  if (goods) {
    data.form.goodsId = goods.id
    data.form.goodsName = goods.name
  }

  data.formVisible = true
}

// 新增保存
const add = () => {
  // 验证农产品是否已输入
  if (!data.form.goodsName || data.form.goodsName.trim() === '') {
    ElMessage.error('请输入农产品名称')
    return
  }

  // 验证进货数量
  if (!data.form.num || data.form.num < 1) {
    ElMessage.error('请输入正确的进货数量')
    return
  }

  // 发送到后端，后端会根据goodsName是否已存在来处理
  request.post('/goodsStock/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false

      // 刷新商品列表
      request.get('/goods/selectAll').then(res =>{
        data.goodsList = res.data || []
      })
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  // 验证农产品是否已输入
  if (!data.form.goodsName || data.form.goodsName.trim() === '') {
    ElMessage.error('请输入农产品名称')
    return
  }

  // 验证进货数量
  if (!data.form.num || data.form.num < 1) {
    ElMessage.error('请输入正确的进货数量')
    return
  }

  request.put('/goodsStock/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false

      // 刷新商品列表
      request.get('/goods/selectAll').then(res =>{
        data.goodsList = res.data || []
      })
    } else {
      ElMessage.error(res.msg)
    }
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
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/goodsStock/delete/' + id).then(res => {
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
  load()
}

load()
</script>

<style scoped>
.goods-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
</style>