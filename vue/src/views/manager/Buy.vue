<template>
  <div style="padding-bottom: 10px">
    <!-- 默认地址显示区域 -->
    <div class="card" style="margin-bottom: 10px" v-if="data.defaultAddress">
      <div style="margin-bottom: 10px; font-weight: bold; color: #333">收货地址</div>
      <div class="default-address">
        <div class="address-info">
          <div class="address-main">
            <span class="consignee">{{ data.defaultAddress.consignee }}</span>
            <span class="phone">{{ data.defaultAddress.phone }}</span>
            <el-tag type="danger" size="small">默认</el-tag>
          </div>
          <div class="address-detail">{{ data.defaultAddress.province }}{{ data.defaultAddress.city }}{{ data.defaultAddress.district }}{{ data.defaultAddress.detail }}</div>
        </div>
        <div class="address-actions">
          <el-button type="primary" text @click="showAddressSelectDialog">更换地址</el-button>
          <el-button type="text" @click="showAddressManager">管理地址</el-button>
        </div>
      </div>
    </div>

    <!-- 无默认地址时的提示 -->
    <div class="card" style="margin-bottom: 10px" v-else>
      <div style="margin-bottom: 10px; font-weight: bold; color: #333">收货地址</div>
      <div style="text-align: center; padding: 20px; color: #909399">
        <el-icon size="24" style="margin-bottom: 10px;"><Location /></el-icon>
        <div>暂无默认收货地址</div>
        <el-button type="primary" @click="showAddressDialog" style="margin-top: 10px">
          添加收货地址
        </el-button>
      </div>
    </div>

    <!-- 分类选择 - 添加滚动容器 -->
    <div class="card category-container" style="margin-bottom: 10px">
      <div class="category-scroll">
        <el-button :class="{'active': data.activeCategoryId === null}" @click="handleCategoryChange(null)" >全部</el-button>
        <el-button :class="{'active': data.activeCategoryId === item.id}" @click="handleCategoryChange(item.id)" v-for="item in data.categoryList" :key="item.id">{{ item.name }}</el-button>
      </div>
    </div>

    <!-- 搜索区域 - 响应式布局 -->
    <div class="search-container" style="margin-bottom: 10px">
      <el-input
          class="search-input"
          v-model="data.name"
          placeholder="请输入商品名称关键字查询"
          @keyup.enter="handleSearch"
          clearable
      />
      <div class="search-buttons">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset" v-if="data.name || data.activeCategoryId">重置</el-button>
      </div>
    </div>
  </div>

  <!-- 商品列表 - 响应式栅格 -->
  <el-row :gutter="10" v-if="data.total > 0">
    <el-col
        style="margin-bottom: 10px"
        :xs="24" :sm="12" :md="8" :lg="6"
        v-for="item in data.goodsList"
        :key="item.id"
    >
      <div class="card product-card">
        <!-- 可点击区域：图片和商品信息 -->
        <div class="clickable-area" @click="showProductDetail(item)">
          <div class="product-image-container">
            <img :src="item.img" alt="" class="product-image">
          </div>
          <div class="product-name">{{ item.name }}</div>
          <el-tooltip v-if="item.descr.length" :content="item.descr" effect="light" placement="top">
            <div class="product-descr line2">{{ item.descr }}</div>
          </el-tooltip>
          <div v-else class="product-descr line2">{{ item.descr }}</div>
          <div class="product-tag">
            <el-tag type="success">{{ item.specials }}</el-tag>
          </div>
        </div>

        <!-- 价格、库存和数量在同一行 -->
        <div class="product-controls">
          <div class="price-section">
            <strong class="current-price">¥{{ item.price }}</strong>
            <span class="unit">/{{ item.unit }}</span>
          </div>
          <div class="stock-section">
            库存: {{ item.store }}
          </div>
          <div class="quantity-section">
            <el-input-number
                @change="handleBuy(item)"
                v-model="item.num"
                class="quantity-input"
                :min="0"
                :max="999"
                @click.stop
                size="small"
            />
          </div>
        </div>

        <!-- 购买按钮区域 -->
        <div class="product-actions" v-if="item.num > 0">
          <div class="total-price">
            总价: <strong class="total-amount">¥{{item.total}}</strong>
          </div>
          <el-button
              type="primary"
              @click.stop="buy(item)"
              :disabled="!data.defaultAddress"
              class="buy-button"
          >
            购买
          </el-button>
        </div>
        <div v-if="item.num > 0 && !data.defaultAddress" class="address-warning">
          请先设置默认收货地址
        </div>
      </div>
    </el-col>
  </el-row>

  <div style="padding: 50px 0; text-align: center; font-size: 24px; color: #888" v-else="">暂无农产品...</div>

  <!-- 分页 -->
  <div class="pagination-container" v-if="data.total">
    <el-pagination
        layout="total, sizes, prev, pager, next"
        v-model:page-size="data.pageSize"
        v-model:current-page="data.pageNum"
        :total="data.total"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        :page-sizes="[8, 12, 16, 20]"
        :small="isMobile"
    />
  </div>

  <!-- 商品详情弹窗 - 响应式调整 -->
  <el-dialog
      v-model="data.detailVisible"
      :title="data.currentProduct ? data.currentProduct.name : '商品详情'"
      :width="dialogWidth"
      class="product-detail-dialog"
  >
    <div v-if="data.currentProduct" class="detail-content">
      <!-- 商品图片 -->
      <div class="detail-image">
        <img
            :src="data.currentProduct.img"
            :alt="data.currentProduct.name"
        >
      </div>

      <!-- 商品信息 -->
      <div class="detail-info">
        <h2 class="detail-title">{{ data.currentProduct.name }}</h2>

        <!-- 商品描述 -->
        <div class="detail-section">
          <div class="section-label">商品描述</div>
          <p class="section-content">{{ data.currentProduct.descr }}</p>
        </div>

        <!-- 商品特色 -->
        <div class="detail-section">
          <div class="section-label">商品特色</div>
          <el-tag type="success" size="large">{{ data.currentProduct.specials }}</el-tag>
        </div>

        <!-- 价格信息 -->
        <div class="detail-section">
          <div class="section-label">价格</div>
          <div class="price-display">
            ¥{{ data.currentProduct.price }}<span class="price-unit">/{{ data.currentProduct.unit }}</span>
          </div>
        </div>

        <!-- 库存信息 -->
        <div class="detail-section">
          <div class="section-label">库存信息</div>
          <div class="stock-display">
            当前库存: <span :class="{'low-stock': data.currentProduct.store <= 10}">{{ data.currentProduct.store }}{{ data.currentProduct.unit }}</span>
          </div>
          <div v-if="data.currentProduct.store <= 10" class="stock-warning">
            ⚠️ 库存紧张，欲购从速
          </div>
        </div>

        <!-- 地址信息 -->
        <div class="detail-section">
          <div class="section-label">收货地址</div>
          <div v-if="data.defaultAddress" class="selected-address">
            <div class="address-info">
              <div class="address-main">
                <span class="consignee">{{ data.defaultAddress.consignee }}</span>
                <span class="phone">{{ data.defaultAddress.phone }}</span>
                <el-tag type="danger" size="small">默认</el-tag>
              </div>
              <div class="address-detail">{{ data.defaultAddress.province }}{{ data.defaultAddress.city }}{{ data.defaultAddress.district }}{{ data.defaultAddress.detail }}</div>
            </div>
            <div class="address-actions">
              <el-button type="text" @click="showAddressSelectDialog">更换地址</el-button>
              <el-button type="text" @click="showAddressManager">管理地址</el-button>
            </div>
          </div>
          <div v-else class="no-address">
            <el-icon size="24" class="no-address-icon"><Location /></el-icon>
            <div>暂无默认收货地址</div>
            <el-button type="primary" @click="showAddressDialog" class="add-address-btn">
              添加收货地址
            </el-button>
          </div>
        </div>

        <!-- 购买操作区域 -->
        <div class="purchase-section">
          <div class="quantity-controls">
            <span class="quantity-label">购买数量:</span>
            <el-input-number
                v-model="data.detailNum"
                :min="0"
                :max="data.currentProduct.store"
                size="large"
                @change="handleDetailNumChange"
                class="detail-quantity-input"
            />
            <el-button
                type="primary"
                size="large"
                @click="buyFromDetail"
                :disabled="data.detailNum <= 0 || !data.defaultAddress"
                class="purchase-button"
            >
              立即购买
            </el-button>
          </div>

          <!-- 总价显示 -->
          <div v-if="data.detailNum > 0" class="total-section">
            <span class="total-label">小计:</span>
            <div class="total-display">
              <div class="total-amount-large">
                ¥{{ (data.currentProduct.price * data.detailNum).toFixed(2) }}
              </div>
              <div class="total-detail">
                {{ data.detailNum }}{{ data.currentProduct.unit }} × ¥{{ data.currentProduct.price }}/{{ data.currentProduct.unit }}
              </div>
            </div>
          </div>

          <div v-if="!data.defaultAddress && data.detailNum > 0" class="address-warning-detail">
            请先设置默认收货地址
          </div>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 地址选择弹窗 - 响应式调整 -->
  <el-dialog
      v-model="data.addressSelectVisible"
      title="选择收货地址"
      :width="dialogWidth"
      class="address-select-dialog"
  >
    <div v-if="data.addressList.length > 0" class="address-list">
      <div
          v-for="address in data.addressList"
          :key="address.id"
          :class="['address-select-item', { 'selected': data.selectedTempAddressId === address.id }]"
          @click="selectTempAddress(address.id)"
      >
        <div class="address-info">
          <div class="address-main">
            <span class="consignee">{{ address.consignee }}</span>
            <span class="phone">{{ address.phone }}</span>
            <el-tag v-if="address.isDefault" type="danger" size="small">默认</el-tag>
          </div>
          <div class="address-detail">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</div>
        </div>
      </div>
    </div>
    <div v-else class="no-address-message">
      暂无收货地址
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="data.addressSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddressChange" :disabled="!data.selectedTempAddressId">确认选择</el-button>
        <el-button @click="showAddressDialog">添加新地址</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 地址管理弹窗 - 响应式调整 -->
  <el-dialog
      v-model="data.addressDialogVisible"
      title="添加收货地址"
      :width="dialogWidth"
      class="address-dialog"
  >
    <el-form :model="data.addressForm" label-width="80px" :rules="data.addressRules" ref="addressFormRef">
      <el-form-item label="收货人" prop="consignee">
        <el-input v-model="data.addressForm.consignee" placeholder="请输入收货人姓名"></el-input>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="data.addressForm.phone" placeholder="请输入手机号"></el-input>
      </el-form-item>

      <!-- 手动输入省市区 - 响应式布局 -->
      <el-row :gutter="10" class="address-row">
        <el-col :xs="24" :sm="8">
          <el-form-item label="省份" prop="province">
            <el-input v-model="data.addressForm.province" placeholder="省"></el-input>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-form-item label="城市" prop="city">
            <el-input v-model="data.addressForm.city" placeholder="市"></el-input>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-form-item label="区域" prop="district">
            <el-input v-model="data.addressForm.district" placeholder="区/县"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="详细地址" prop="detail">
        <el-input
            v-model="data.addressForm.detail"
            type="textarea"
            placeholder="请输入详细地址"
            :rows="3"
        ></el-input>
      </el-form-item>
      <el-form-item label="默认地址">
        <el-switch v-model="data.addressForm.isDefault"></el-switch>
        <div class="default-tip">
          设置为默认收货地址
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="data.addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {reactive, ref, nextTick, computed, onMounted, onUnmounted} from "vue";
import request from "@/utils/request"
import { ElMessage } from 'element-plus'
import { Plus, Location } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const addressFormRef = ref()
const router = useRouter()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  categoryList: [],
  pageNum: 1,
  pageSize: 8,
  total: 0,
  goodsList: [],
  name: '',
  activeCategoryId: null,

  // 地址相关数据
  addressList: [],
  defaultAddress: null,
  addressSelectVisible: false,
  selectedTempAddressId: null,
  addressDialogVisible: false,
  addressForm: {
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: true
  },
  addressRules: {
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
  },
  // 详情弹窗相关数据
  detailVisible: false,
  currentProduct: null,
  detailNum: 0,

  // 响应式相关
  windowWidth: window.innerWidth
})

// 计算属性：是否为移动设备
const isMobile = computed(() => {
  return data.windowWidth < 768
})

// 计算属性：弹窗宽度
const dialogWidth = computed(() => {
  if (data.windowWidth < 768) {
    return '90%'
  } else if (data.windowWidth < 992) {
    return '80%'
  } else {
    return '800px'
  }
})

// 监听窗口大小变化
const handleResize = () => {
  data.windowWidth = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})


// 加载地址列表并设置默认地址
const loadAddresses = () => {
  console.log('加载地址列表，用户ID:', data.user.id)

  const apiUrl = data.user.id ? `/address/selectByUserId/${data.user.id}` : '/address/selectByUserId'

  request.get(apiUrl).then(res => {
    console.log('地址列表响应:', res)
    if (res.code === '200') {
      data.addressList = res.data || []
      console.log('加载到的地址数量:', data.addressList.length)

      // 查找默认地址
      data.defaultAddress = data.addressList.find(item => item.isDefault) || null

      if (data.defaultAddress) {
        console.log('找到默认地址:', data.defaultAddress.id)
      } else if (data.addressList.length > 0) {
        // 如果没有默认地址，使用第一个地址
        data.defaultAddress = data.addressList[0]
        console.log('使用第一个地址作为默认:', data.defaultAddress.id)
      } else {
        data.defaultAddress = null
        console.log('没有可用地址')
      }
    } else {
      console.error('加载地址失败:', res.msg)
      data.addressList = []
      data.defaultAddress = null
    }
  }).catch(error => {
    console.error('加载地址异常:', error)
    data.addressList = []
    data.defaultAddress = null
  })
}

// 显示地址选择弹窗
const showAddressSelectDialog = () => {
  data.selectedTempAddressId = data.defaultAddress ? data.defaultAddress.id : null
  data.addressSelectVisible = true
}

// 选择临时地址
const selectTempAddress = (addressId) => {
  data.selectedTempAddressId = addressId
}

// 确认地址变更
const confirmAddressChange = () => {
  if (!data.selectedTempAddressId) {
    ElMessage.warning('请选择一个地址')
    return
  }

  const selectedAddress = data.addressList.find(item => item.id === data.selectedTempAddressId)
  if (selectedAddress) {
    data.defaultAddress = selectedAddress
    ElMessage.success('地址已更换')
    data.addressSelectVisible = false
  }
}

// 显示地址管理页面
const showAddressManager = () => {
  router.push('/address')
}

// 显示地址弹窗
const showAddressDialog = () => {
  data.addressForm = {
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: true // 新地址默认设为默认地址
  }
  data.addressDialogVisible = true

  // 重置表单验证
  nextTick(() => {
    if (addressFormRef.value) {
      addressFormRef.value.clearValidate()
    }
  })
}

// 保存地址
const saveAddress = () => {
  if (!addressFormRef.value) return

  addressFormRef.value.validate((valid) => {
    if (!valid) {
      ElMessage.warning('请填写完整的地址信息')
      return
    }

    const addressData = {
      ...data.addressForm,
      userId: data.user.id
    }

    console.log('保存地址数据:', addressData)

    request.post('/address/add', addressData).then(res => {
      console.log('保存地址响应:', res)
      if (res.code === '200') {
        ElMessage.success('地址保存成功')
        data.addressDialogVisible = false
        // 重新加载地址列表
        loadAddresses()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    }).catch(error => {
      console.error('保存地址异常:', error)
      ElMessage.error('保存失败，请重试')
    })
  })
}

// 显示商品详情
const showProductDetail = (product) => {
  data.currentProduct = product
  data.detailNum = product.num || 0
  data.detailVisible = true

  // 确保地址列表已加载
  if (!data.defaultAddress) {
    loadAddresses()
  }
}

// 详情页数量变化
const handleDetailNumChange = (value) => {
  if (value > data.currentProduct.store) {
    data.detailNum = data.currentProduct.store
    ElMessage.warning(`数量不能超过库存量 ${data.currentProduct.store}`)
  }
}

// 从详情页购买
const buyFromDetail = () => {
  if (data.detailNum <= 0) {
    ElMessage.warning('请选择购买数量')
    return
  }

  if (data.detailNum > data.currentProduct.store) {
    ElMessage.warning('购买数量不能超过库存')
    return
  }

  if (!data.defaultAddress) {
    ElMessage.warning('请先设置默认收货地址')
    return
  }

  const orderData = {
    goodsId: data.currentProduct.id,
    goodsName: data.currentProduct.name,
    goodsImg: data.currentProduct.img,
    price: data.currentProduct.price,
    num: data.detailNum,
    total: (data.currentProduct.price * data.detailNum).toFixed(2),
    userName: data.user.name,
    userId: data.user.id,
    status: '待支付',
    time: new Date().toLocaleString(),
    specials: data.currentProduct.specials,
    unit: data.currentProduct.unit,
    addressId: data.defaultAddress.id,
    addressInfo: `${data.defaultAddress.consignee} ${data.defaultAddress.phone} ${data.defaultAddress.province}${data.defaultAddress.city}${data.defaultAddress.district}${data.defaultAddress.detail}`
  }

  request.post('/orders/add', orderData).then(res => {
    if (res.code === '200') {
      ElMessage.success('购买成功，请在订单管理中查看')

      // 更新主列表中的数量
      const goods = data.goodsList.find(item => item.id === data.currentProduct.id)
      if (goods) {
        goods.num = 0
        goods.total = 0
      }

      // 更新库存显示
      data.currentProduct.store -= data.detailNum

      // 重置并关闭弹窗
      data.detailNum = 0
      data.detailVisible = false

      // 重新加载数据确保库存同步
      load()
    } else {
      ElMessage.error(res.msg)
    }
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

// 购买商品
// 方法1：直接购买（商品列表页）
const buy = (goods) => {
  if (!data.defaultAddress) {
    ElMessage.warning('请先设置默认收货地址')
    return
  }
  let orderData = {
    goodsId: goods.id,// 商品ID
    goodsName: goods.name,// 商品名称
    goodsImg: goods.img,// 商品图片
    price: goods.price,// 单价
    num: goods.num,//购买数量
    total: goods.total,//总价
    userName: data.user.name,// 用户名
    userId: data.user.id,// 用户ID
    status: '待支付',// 订单状态
    time: new Date().toLocaleString(),// 下单时间
    specials: goods.specials,// 商品特色
    unit: goods.unit,// 商品单位
    addressId: data.defaultAddress.id,// 收货地址ID
    addressInfo: `${data.defaultAddress.consignee} ${data.defaultAddress.phone}
     ${data.defaultAddress.province}${data.defaultAddress.city}
     ${data.defaultAddress.district}${data.defaultAddress.detail}`// 收货地址信息
  }
  //第一步：发送POST请求到后端
  request.post('/orders/add', orderData).then(res => {
    if (res.code === '200') {
      ElMessage.success('购买成功，请在订单管理中查看')
      // 重置商品数量
      goods.num = 0
      goods.total = 0
      // 重新加载更新库存
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleBuy = (goods) => {
  goods.total = (goods.price * goods.num).toFixed(2)
}

// 搜索功能
const handleSearch = () => {
  data.pageNum = 1
  load()
}

// 重置搜索条件
const handleReset = () => {
  data.name = ''
  data.activeCategoryId = null
  data.pageNum = 1
  load()
}

// 分类切换
const handleCategoryChange = (categoryId) => {
  data.activeCategoryId = categoryId
  data.pageNum = 1
  load()
}

// 获取分类数据
request.get('/category/selectAll').then(res => {
  data.categoryList = res.data
})

// 分页查询
const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      categoryId: data.activeCategoryId
    }
  }).then(res => {
    data.goodsList = res.data?.list || []
    data.total = res.data?.total || 0
    data.goodsList.forEach(item => {
      item.num = item.num || 0
      item.total = item.total || 0
    })
  })
}

// 初始化
load()
loadAddresses()
</script>

<style scoped>
/* 基础样式 */
.card {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 地址相关样式 */
.default-address {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  flex-wrap: wrap;
}

.address-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  width: 100%;
}

@media (min-width: 768px) {
  .address-actions {
    margin-top: 0;
    width: auto;
    flex-shrink: 0;
  }

  .default-address {
    flex-wrap: nowrap;
  }
}

/* 分类选择样式 */
.category-container {
  overflow-x: auto;
}

.category-scroll {
  display: flex;
  gap: 10px;
  min-width: min-content;
  padding-bottom: 5px;
}

.category-scroll .el-button {
  flex-shrink: 0;
  white-space: nowrap;
}

/* 搜索区域样式 */
.search-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-buttons {
  display: flex;
  gap: 10px;
}

@media (min-width: 768px) {
  .search-container {
    flex-direction: row;
    align-items: center;
  }

  .search-input {
    flex: 1;
    min-width: 200px;
  }
}

/* 商品卡片样式 */
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.product-image-container {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 6px;
  margin-bottom: 12px;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.clickable-area:hover .product-image {
  transform: scale(1.05);
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin: 5px 0;
  color: #333;
  line-height: 1.4;
}

.product-descr {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
  height: 40px;
  overflow: hidden;
}

.product-tag {
  margin: 5px 0 10px 0;
}

/* 商品控制区域 */
.product-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 10px 0;
  color: #666;
  font-size: 14px;
}

.price-section, .stock-section, .quantity-section {
  display: flex;
  align-items: center;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4757;
}

.unit {
  font-size: 12px;
  color: #999;
  margin-left: 2px;
}

.quantity-input {
  width: 100%;
}

@media (min-width: 576px) {
  .product-controls {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .quantity-input {
    width: 120px;
  }
}

/* 购买按钮区域 */
.product-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: stretch;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.total-price {
  font-size: 14px;
  color: #666;
  text-align: center;
}

.total-amount {
  font-size: 16px;
  color: #ff4757;
  margin-left: 4px;
}

.buy-button {
  width: 100%;
}

@media (min-width: 576px) {
  .product-actions {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .buy-button {
    width: auto;
  }
}

.address-warning {
  font-size: 12px;
  color: #e6a23c;
  text-align: center;
  margin-top: 5px;
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 详情弹窗样式 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image {
  flex-shrink: 0;
  text-align: center;
}

.detail-image img {
  width: 100%;
  max-width: 300px;
  height: auto;
  object-fit: cover;
  border-radius: 8px;
}

.detail-info {
  flex: 1;
}

.detail-title {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 20px;
  line-height: 1.3;
}

.detail-section {
  margin-bottom: 15px;
}

.section-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.section-content {
  color: #666;
  line-height: 1.6;
  margin: 0;
  font-size: 15px;
}

.price-display {
  font-size: 24px;
  color: red;
  font-weight: bold;
}

.price-unit {
  font-size: 16px;
  color: #666;
  margin-left: 5px;
}

.stock-display {
  color: #666;
  font-size: 15px;
}

.low-stock {
  color: #E6A23C;
}

.stock-warning {
  color: #E6A23C;
  font-size: 13px;
  margin-top: 2px;
}

.no-address {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.no-address-icon {
  margin-bottom: 10px;
}

.add-address-btn {
  margin-top: 10px;
}

.purchase-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.quantity-controls {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 15px;
}

.quantity-label {
  color: #606266;
  font-size: 15px;
}

.detail-quantity-input {
  width: 100%;
}

.purchase-button {
  width: 100%;
}

.total-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8f9fa;
  padding: 12px 15px;
  border-radius: 6px;
  margin-bottom: 15px;
}

.total-label {
  color: #606266;
  font-size: 15px;
}

.total-amount-large {
  font-size: 20px;
  color: red;
  font-weight: bold;
}

.total-detail {
  color: #909399;
  font-size: 13px;
  margin-top: 2px;
}

.address-warning-detail {
  color: #e6a23c;
  font-size: 13px;
  text-align: center;
  margin-top: 10px;
}

@media (min-width: 768px) {
  .detail-content {
    flex-direction: row;
  }

  .detail-image img {
    width: 300px;
    height: 300px;
  }

  .quantity-controls {
    flex-direction: row;
    align-items: center;
  }

  .detail-quantity-input {
    width: 120px;
  }

  .purchase-button {
    width: auto;
    min-width: 120px;
  }
}

/* 地址选择弹窗样式 */
.address-list {
  max-height: 400px;
  overflow-y: auto;
}

.address-select-item {
  padding: 15px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-select-item:hover {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.address-select-item.selected {
  border-color: #409eff;
  background-color: #f0f7ff;
}

.no-address-message {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

@media (min-width: 576px) {
  .dialog-footer {
    justify-content: flex-end;
    flex-wrap: nowrap;
  }
}

/* 地址表单样式 */
.address-row {
  margin-bottom: 0;
}

.default-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

/* 通用样式 */
.line2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.active {
  color: white !important;
  background-color: #1967e3 !important;
}

.clickable-area {
  cursor: pointer;
  transition: all 0.3s ease;
}

.clickable-area:hover {
  opacity: 0.9;
}

.address-info {
  flex: 1;
}

.address-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.consignee {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.phone {
  color: #666;
  font-size: 14px;
}

.address-detail {
  color: #666;
  line-height: 1.4;
  font-size: 14px;
  word-break: break-all;
}

/* 弹窗头部优化 */
:deep(.product-detail-dialog .el-dialog__header) {
  border-bottom: 1px solid #eee;
  margin-right: 0;
  padding: 15px 20px;
}

:deep(.product-detail-dialog .el-dialog__body) {
  padding: 20px;
}

/* 移动端优化 */
@media (max-width: 767px) {
  .card {
    padding: 12px;
  }

  .product-image-container {
    height: 160px;
  }

  .product-name {
    font-size: 15px;
  }

  .current-price {
    font-size: 16px;
  }

  :deep(.el-dialog) {
    margin: 20px 10px !important;
    width: auto !important;
  }

  :deep(.el-dialog__header) {
    padding: 12px 15px;
  }

  :deep(.el-dialog__body) {
    padding: 15px;
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .product-controls {
    font-size: 13px;
  }

  .current-price {
    font-size: 15px;
  }

  .product-actions {
    font-size: 13px;
  }

  .total-amount {
    font-size: 15px;
  }
}
</style>