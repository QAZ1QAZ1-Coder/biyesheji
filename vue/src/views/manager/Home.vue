<template>
  <div class="home-container">
    <!-- 轮播图和公告左右布局 -->
    <div class="main-content">
      <!-- 左侧轮播图 -->
      <div class="carousel-section">
        <div class="section-title">家乡甄选 助农直达</div>
        <el-carousel :interval="4000" height="320px" indicator-position="outside">
          <el-carousel-item v-for="(item, index) in data.carouselList" :key="index">
            <div class="carousel-item">
              <img :src="item.image" :alt="item.title" />
              <div class="carousel-content">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 右侧系统公告 -->
      <div class="notice-section">
        <div class="section-title">
          系统公告
          <el-button
              v-if="data.user.role === 'ADMIN'"
              type="primary"
              link
              @click="goToNoticeManage"
              style="float: right; margin-top: -5px;"
          >
            管理公告
          </el-button>
        </div>
        <div class="notice-list">
          <div class="notice-item" v-for="item in data.noticeList" :key="item.id">
            <div class="notice-time">{{ formatTime(item.time) }}</div>
            <div class="notice-content">
              <h4>{{ item.title }}</h4>
              <p>{{ item.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 特色农产品推荐 -->
    <div class="featured-products">
      <div class="section-header">
        <h2>精选助农产品</h2>
        <p>精选优质农产品，助力乡村振兴</p>
      </div>

      <!-- 商品列表 - 响应式栅格 -->
      <el-row :gutter="10" v-if="data.featuredProducts.length > 0">
        <el-col
            style="margin-bottom: 10px"
            :xs="24" :sm="12" :md="8" :lg="8"
            v-for="product in data.featuredProducts"
            :key="product.id"
        >
          <div class="card product-card">
            <!-- 可点击区域：图片和商品信息 -->
            <div class="clickable-area" @click="showProductDetail(product)">
              <div class="product-image-container">
                <img :src="product.img || product.image" alt="" class="product-image">
              </div>
              <div class="product-name">{{ product.name }}</div>
              <el-tooltip v-if="product.descr.length" :content="product.descr" effect="light" placement="top">
                <div class="product-descr line2">{{ product.descr }}</div>
              </el-tooltip>
              <div v-else class="product-descr line2">{{ product.descr }}</div>
              <div class="product-tag">
                <el-tag type="success">{{ product.specials }}</el-tag>
              </div>
            </div>

            <!-- 价格、库存和数量在同一行 -->
            <div class="product-controls">
              <div class="price-section">
                <strong class="current-price">¥{{ product.price }}</strong>
                <span class="unit">/{{ product.unit }}</span>
              </div>
              <div class="stock-section">
                库存: {{ product.store }}
              </div>
              <div class="quantity-section">
                <el-input-number
                    @change="handleQuantityChange(product)"
                    v-model="product.num"
                    class="quantity-input"
                    :min="0"
                    :max="product.store"
                    @click.stop
                    size="small"
                />
              </div>
            </div>

            <!-- 购买按钮区域 -->
            <div class="product-actions" v-if="product.num > 0">
              <div class="total-price">
                总价: <strong class="total-amount">¥{{ product.total || 0 }}</strong>
              </div>
              <el-button
                  type="primary"
                  @click.stop="buy(product)"
                  :disabled="!hasDefaultAddress && data.user.role === 'USER'"
                  class="buy-button"
              >
                购买
              </el-button>
            </div>
            <div v-if="product.num > 0 && !hasDefaultAddress && data.user.role === 'USER'" class="address-warning">
              请先设置默认收货地址
            </div>
          </div>
        </el-col>
      </el-row>

      <div style="padding: 50px 0; text-align: center; font-size: 24px; color: #888" v-else>暂无农产品...</div>
    </div>

    <!-- 商品详情弹窗 -->
    <el-dialog
        v-model="data.detailVisible"
        :title="data.currentProduct ? data.currentProduct.name : '商品详情'"
        width="800px"
        class="product-detail-dialog"
    >
      <div v-if="data.currentProduct" style="display: flex; gap: 20px;">
        <!-- 商品图片 -->
        <div style="flex-shrink: 0;">
          <img
              :src="data.currentProduct.img"
              :alt="data.currentProduct.name"
              style="width: 300px; height: 300px; object-fit: cover; border-radius: 8px;"
          >
        </div>

        <!-- 商品信息 -->
        <div style="flex: 1">
          <h2 style="margin: 0 0 10px; color: #333; font-size: 24px">{{ data.currentProduct.name }}</h2>

          <!-- 商品描述 -->
          <div style="margin-bottom: 15px">
            <div style="color: #909399; font-size: 14px; margin-bottom: 5px">商品描述</div>
            <p style="color: #666; line-height: 1.6; margin: 0; font-size: 15px">{{ data.currentProduct.descr }}</p>
          </div>

          <!-- 商品特点 -->
          <div style="margin-bottom: 15px">
            <div style="color: #909399; font-size: 14px; margin-bottom: 5px">商品特点</div>
            <el-tag type="success" size="large">{{ data.currentProduct.specials }}</el-tag>
          </div>

          <!-- 价格信息 -->
          <div style="margin-bottom: 15px">
            <div style="color: #909399; font-size: 14px; margin-bottom: 5px">价格</div>
            <div style="font-size: 28px; color: red; font-weight: bold">
              ¥{{ data.currentProduct.price }}<span style="font-size: 16px; color: #666; margin-left: 5px">/{{ data.currentProduct.unit }}</span>
            </div>
          </div>

          <!-- 库存信息 -->
          <div style="margin-bottom: 20px">
            <div style="color: #909399; font-size: 14px; margin-bottom: 5px">库存信息</div>
            <div style="color: #666; font-size: 15px">
              当前库存: <span :style="{ color: data.currentProduct.store > 10 ? '#67C23A' : '#E6A23C' }">{{ data.currentProduct.store }}{{ data.currentProduct.unit }}</span>
            </div>
            <div v-if="data.currentProduct.store <= 10" style="color: #E6A23C; font-size: 13px; margin-top: 2px">
              ⚠️ 库存紧张，欲购从速
            </div>
          </div>

          <!-- 地址信息（仅对普通用户显示） -->
          <div v-if="data.user.role === 'USER'" style="margin-bottom: 15px">
            <div style="color: #909399; font-size: 14px; margin-bottom: 8px">收货地址</div>
            <div v-if="data.defaultAddress" class="selected-address">
              <div class="address-info">
                <div class="address-main">
                  <span class="consignee">{{ data.defaultAddress.consignee }}</span>
                  <span class="phone">{{ data.defaultAddress.phone }}</span>
                  <el-tag type="danger" size="small">默认</el-tag>
                </div>
                <div class="address-detail">{{ data.defaultAddress.province }}{{ data.defaultAddress.city }}{{ data.defaultAddress.district }}{{ data.defaultAddress.detail }}</div>
              </div>
              <div style="display: flex; gap: 10px;">
                <el-button type="text" @click="showAddressSelectDialog">更换地址</el-button>
                <el-button type="text" @click="showAddressManager">管理地址</el-button>
              </div>
            </div>
            <div v-else style="text-align: center; padding: 20px; color: #909399">
              <el-icon size="24" style="margin-bottom: 10px;"><Location /></el-icon>
              <div>暂无默认收货地址</div>
              <el-button type="primary" @click="showAddressDialog" style="margin-top: 10px">
                添加收货地址
              </el-button>
            </div>
          </div>

          <!-- 购买操作区域 -->
          <div style="margin-top: 25px; padding-top: 20px; border-top: 1px solid #eee">
            <div style="display: flex; align-items: center; gap: 15px; margin-bottom: 15px">
              <span style="color: #606266; font-size: 15px">购买数量:</span>
              <el-input-number
                  v-model="data.detailNum"
                  :min="0"
                  :max="data.currentProduct.store"
                  size="large"
                  @change="handleDetailNumChange"
              />
              <el-button
                  type="primary"
                  size="large"
                  @click="buyFromDetail"
                  :disabled="data.detailNum <= 0 || (data.user.role === 'USER' && !data.defaultAddress)"
                  style="min-width: 120px;"
              >
                立即购买
              </el-button>
            </div>

            <!-- 总价显示 -->
            <div v-if="data.detailNum > 0" style="display: flex; align-items: center; justify-content: space-between; background: #f8f9fa; padding: 12px 15px; border-radius: 6px;">
              <span style="color: #606266; font-size: 15px">小计:</span>
              <div style="text-align: right">
                <div style="font-size: 20px; color: red; font-weight: bold">
                  ¥{{ (data.currentProduct.price * data.detailNum).toFixed(2) }}
                </div>
                <div style="color: #909399; font-size: 13px; margin-top: 2px">
                  {{ data.detailNum }}{{ data.currentProduct.unit }} × ¥{{ data.currentProduct.price }}/{{ data.currentProduct.unit }}
                </div>
              </div>
            </div>

            <div v-if="!data.defaultAddress && data.detailNum > 0 && data.user.role === 'USER'" style="color: #e6a23c; font-size: 13px; text-align: center; margin-top: 10px">
              请先设置默认收货地址
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 地址选择弹窗 -->
    <el-dialog
        v-model="data.addressSelectVisible"
        title="选择收货地址"
        width="600px"
    >
      <div v-if="data.addressList.length > 0">
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
      <div v-else style="text-align: center; padding: 20px; color: #909399">
        暂无收货地址
      </div>

      <template #footer>
        <el-button @click="data.addressSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddressChange" :disabled="!data.selectedTempAddressId">确认选择</el-button>
        <el-button @click="showAddressDialog">添加新地址</el-button>
      </template>
    </el-dialog>

    <!-- 地址管理弹窗 -->
    <el-dialog
        v-model="data.addressDialogVisible"
        title="添加收货地址"
        width="600px"
    >
      <el-form :model="data.addressForm" label-width="80px" :rules="data.addressRules" ref="addressFormRef">
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="data.addressForm.consignee" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="data.addressForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>

        <!-- 手动输入省市区 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input v-model="data.addressForm.province" placeholder="省"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input v-model="data.addressForm.city" placeholder="市"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
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
          <div style="font-size: 12px; color: #666; margin-top: 5px;">
            设置为默认收货地址
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="data.addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref, computed, nextTick } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import { Location } from '@element-plus/icons-vue'

const router = useRouter();
const addressFormRef = ref();

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeList: [],
  featuredProducts: [],
  carouselList: [
    {
      image: "/banner1.png",
      title: "源于乡间，鲜花香火",
      description: "感受大自然的芬芳，品味乡村的淳朴气息"
    },
    {
      image: "/banner2.png",
      title: "源于乡间，鲜在舌尖",
      description: "从田间直达餐桌，每一口都是新鲜的味道"
    },
    {
      image: "/banner3.png",
      title: "绿城味道，家的回味",
      description: "让您在外也能品尝到家乡的温暖与记忆"
    }
  ],

  // 详情弹窗相关数据
  detailVisible: false,
  currentProduct: null,
  detailNum: 0,

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
  }
})

// 获取完整用户信息
// 在 Home.vue 的 loadUserInfo 方法中修改
const loadUserInfo = async () => {
  try {
    const localUser = JSON.parse(localStorage.getItem('system-user') || '{}')

    // 如果已经有用户数据，直接使用
    if (localUser.id) {
      data.user = { ...localUser }
      return
    }

    const res = await request.get('/user/currentUser')
    if (res.code === '200' && res.data) {
      // 直接使用正确的字段名
      const updatedUser = {
        ...res.data,
        token: localUser.token || generateTempToken()
      }
      data.user = updatedUser
      localStorage.setItem('system-user', JSON.stringify(updatedUser))
    }
  } catch (error) {
    console.error('获取用户信息异常:', error)
  }
}

// 头像URL修复函数
const fixAvatarUrl = (avatar) => {
  if (!avatar) return ''

  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http')) {
    return avatar
  }

  // 如果是文件名，拼接完整URL
  return `http://192.168.88.1:9090/files/preview/${avatar}`
}

// 生成临时token
const generateTempToken = () => {
  return 'temp_token_' + Date.now() + '_' + Math.random().toString(36).substr(2)
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserInfo()  // 先加载完整用户信息
  loadFeaturedProducts()
  loadNotices()
  if (data.user.role === 'USER') {
    loadAddresses()
  }
})

// 计算属性：是否有默认地址
const hasDefaultAddress = computed(() => {
  return !!data.defaultAddress
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.split(' ')[0] // 只显示日期部分
}

// 加载地址列表
const loadAddresses = () => {
  if (data.user.role !== 'USER') return

  const apiUrl = data.user.id ? `/address/selectByUserId/${data.user.id}` : '/address/selectByUserId'

  request.get(apiUrl).then(res => {
    if (res.code === '200') {
      data.addressList = res.data || []
      data.defaultAddress = data.addressList.find(item => item.isDefault) ||
          (data.addressList.length > 0 ? data.addressList[0] : null)
    }
  })
}

// 显示商品详情
const showProductDetail = (product) => {
  data.currentProduct = product
  data.detailNum = product.num || 0
  data.detailVisible = true

  if (data.user.role === 'USER') {
    loadAddresses()
  }
}

// 数量变化处理
const handleQuantityChange = (product) => {
  product.total = (product.price * product.num).toFixed(2)
}

// 详情页数量变化
const handleDetailNumChange = (value) => {
  if (value > data.currentProduct.store) {
    data.detailNum = data.currentProduct.store
    ElMessage.warning(`数量不能超过库存量 ${data.currentProduct.store}`)
  }
}

// 购买商品
const buy = (product) => {
  // 管理员点击购买时跳转到农产品管理
  if (data.user.role === 'ADMIN') {
    router.push('/goods')
    return
  }

  if (data.user.role === 'USER' && !data.defaultAddress) {
    ElMessage.warning('请先设置默认收货地址')
    return
  }

  if (product.num <= 0) {
    ElMessage.warning('请选择购买数量')
    return
  }

  if (product.num > product.store) {
    ElMessage.warning('购买数量不能超过库存')
    return
  }

  const orderData = {
    goodsId: product.id,
    goodsName: product.name,
    goodsImg: product.img,
    price: product.price,
    num: product.num,
    total: (product.price * product.num).toFixed(2),
    userName: data.user.name,
    userId: data.user.id,
    status: '待支付',
    time: new Date().toLocaleString(),
    specials: product.specials,
    unit: product.unit
  }

  // 如果是普通用户，添加地址信息
  if (data.user.role === 'USER' && data.defaultAddress) {
    orderData.addressId = data.defaultAddress.id
    orderData.addressInfo = `${data.defaultAddress.consignee} ${data.defaultAddress.phone} ${data.defaultAddress.province}${data.defaultAddress.city}${data.defaultAddress.district}${data.defaultAddress.detail}`
  }

  request.post('/orders/add', orderData).then(res => {
    if (res.code === '200') {
      ElMessage.success('购买成功，请在订单管理中查看')

      // 重置商品数量
      product.num = 0
      product.total = 0

      // 重新加载数据确保库存同步
      loadFeaturedProducts()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 从详情页购买
const buyFromDetail = () => {
  // 管理员点击购买时跳转到农产品管理
  if (data.user.role === 'ADMIN') {
    router.push('/goods')
    data.detailVisible = false
    return
  }

  if (data.detailNum <= 0) {
    ElMessage.warning('请选择购买数量')
    return
  }

  if (data.detailNum > data.currentProduct.store) {
    ElMessage.warning('购买数量不能超过库存')
    return
  }

  if (data.user.role === 'USER' && !data.defaultAddress) {
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
    unit: data.currentProduct.unit
  }

  // 如果是普通用户，添加地址信息
  if (data.user.role === 'USER' && data.defaultAddress) {
    orderData.addressId = data.defaultAddress.id
    orderData.addressInfo = `${data.defaultAddress.consignee} ${data.defaultAddress.phone} ${data.defaultAddress.province}${data.defaultAddress.city}${data.defaultAddress.district}${data.defaultAddress.detail}`
  }

  request.post('/orders/add', orderData).then(res => {
    if (res.code === '200') {
      ElMessage.success('购买成功，请在订单管理中查看')

      // 更新主列表中的数量
      const product = data.featuredProducts.find(item => item.id === data.currentProduct.id)
      if (product) {
        product.num = 0
        product.total = 0
      }

      // 更新库存显示
      data.currentProduct.store -= data.detailNum

      // 重置并关闭弹窗
      data.detailNum = 0
      data.detailVisible = false

      // 重新加载数据确保库存同步
      loadFeaturedProducts()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 地址相关方法
const showAddressSelectDialog = () => {
  data.selectedTempAddressId = data.defaultAddress ? data.defaultAddress.id : null
  data.addressSelectVisible = true
}

const selectTempAddress = (addressId) => {
  data.selectedTempAddressId = addressId
}

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

const showAddressManager = () => {
  router.push('/address')
}

const showAddressDialog = () => {
  data.addressForm = {
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: true
  }
  data.addressDialogVisible = true

  nextTick(() => {
    if (addressFormRef.value) {
      addressFormRef.value.clearValidate()
    }
  })
}

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

    request.post('/address/add', addressData).then(res => {
      if (res.code === '200') {
        ElMessage.success('地址保存成功')
        data.addressDialogVisible = false
        loadAddresses()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    })
  })
}

// 跳转到公告管理页面
const goToNoticeManage = () => {
  router.push('/notice')
}

// 获取指定ID的商品作为推荐
const loadFeaturedProducts = () => {
  // 手动指定要显示的商品ID
  const featuredIds = [8, 40, 65, 85, 146, 161]

  request.get('/goods/selectAll').then(res => {
    if (res.data) {
      data.featuredProducts = res.data
          .filter(product => featuredIds.includes(product.id))
          .slice(0, 6)
          .map(product => ({
            ...product,
            tag: getProductTag(product),
            num: 0,
            total: 0
          }))
    }
  })
}

// 根据商品信息生成标签
const getProductTag = (product) => {
  if (product.price < 10) return '特惠'
  if (product.store < 20) return '热销'
  if (product.price > 100) return '精选'
  return '新品'
}

// 获取公告数据
const loadNotices = () => {
  request.get('/notice/selectAll').then(res => {
    data.noticeList = res.data || []
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadFeaturedProducts()
  loadNotices()
  if (data.user.role === 'USER') {
    loadAddresses()
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
  align-items: start;
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
  color: #333;
}

/* 轮播图样式 */
.carousel-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
  flex: 1;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 20px;
}

.carousel-content h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.carousel-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 公告样式 */
.notice-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.notice-list {
  flex: 1;
  overflow-y: auto;
}

.notice-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.notice-content h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.notice-content p {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

/* 特色农产品推荐 */
.featured-products {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 10px 0;
}

.section-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 基础样式 */
.card {
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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
  position: relative;
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

/* 通用样式 */
.line2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.clickable-area {
  cursor: pointer;
  transition: all 0.3s ease;
}

.clickable-area:hover {
  opacity: 0.9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

/* 详情弹窗样式优化 */
.product-detail-dialog .el-dialog__header {
  border-bottom: 1px solid #eee;
  margin-right: 0;
}

.product-detail-dialog .el-dialog__body {
  padding: 20px;
}

/* 地址相关样式 */
.selected-address {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  flex-wrap: wrap;
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