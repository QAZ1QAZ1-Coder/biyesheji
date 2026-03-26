<template>
	<view style="padding: 20rpx; background-color: #f5f5f5; min-height: 100vh;">
		<!-- 收货地址卡片 - 参考商品详情页样式 -->
		<view v-if="orderInfo.addressInfo" 
			style="background: #fff; border-radius: 0; padding: 35rpx 30rpx; margin-bottom: 20rpx;"
			@click="showAddressSelect"
		>
			<view style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25rpx;">
				<text style="font-size: 32rpx; font-weight: bold; color: #333;">收货地址</text>
				<text 
					v-if="orderInfo.status === '待支付'" 
					style="font-size: 28rpx; color: #4CD964;"
				>
					更换
				</text>
			</view>
			
			<view style="position: relative;">
				<view style="display: flex; align-items: center; margin-bottom: 15rpx; flex-wrap: wrap;">
					<text style="font-size: 32rpx; font-weight: bold; color: #333; margin-right: 25rpx;">
						{{ getConsignee() }}
					</text>
					<text style="font-size: 28rpx; color: #666; margin-right: 25rpx;">
						{{ getPhone() }}
					</text>
					<view v-if="isDefaultAddress()" style="background: #ff4757; color: #fff; font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx;">
						默认
					</view>
				</view>
				<text style="font-size: 28rpx; color: #666; line-height: 1.5; display: block;">
					{{ getAddressDetail() }}
				</text>
			</view>
		</view>
		
		<!-- 无地址时显示 -->
		<view v-else-if="orderInfo.status === '待支付'" 
			style="background: #fff; border-radius: 0; padding: 35rpx 30rpx; margin-bottom: 20rpx;"
			@click="showAddressSelect"
		>
			<view style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25rpx;">
				<text style="font-size: 32rpx; font-weight: bold; color: #333;">收货地址</text>
				<text style="font-size: 28rpx; color: #4CD964;">
					选择地址
				</text>
			</view>
			
			<view style="display: flex; align-items: center; justify-content: center; padding: 50rpx 0; color: #999;">
				<uni-icons type="location" size="20" color="#999"></uni-icons>
				<text style="font-size: 28rpx; margin-left: 15rpx;">请选择收货地址</text>
			</view>
		</view>

		<!-- 订单状态卡片 -->
		<view style="background: #fff; border-radius: 0; padding: 30rpx; margin-bottom: 20rpx;">
			<view style="text-align: center; margin-bottom: 20rpx;">
				<!-- 修改：使用与order.vue相同的样式结构 -->
				<text v-if="orderInfo.status === '待支付'" style="color: #e6a23c; font-size: 24rpx; font-weight: bold;">
					{{ orderInfo.status || '未知状态' }}
				</text>
				<text v-else-if="orderInfo.status === '待发货'" style="color: #409eff; font-size: 24rpx; font-weight: bold;">
					{{ orderInfo.status || '未知状态' }}
				</text>
				<text v-else-if="orderInfo.status === '待收货'" style="color: #409eff; font-size: 24rpx; font-weight: bold;">
					{{ orderInfo.status || '未知状态' }}
				</text>
				<text v-else-if="orderInfo.status === '已完成'" style="color: #67c23a; font-size: 24rpx; font-weight: bold;">
					{{ orderInfo.status || '未知状态' }}
				</text>
				<text v-else-if="orderInfo.status === '已取消'" style="color: #f56c6c; font-size: 24rpx; font-weight: bold;">
					{{ orderInfo.status || '未知状态' }}
				</text>
				<text v-else style="color: #666; font-size: 24rpx; font-weight: bold;">
					{{ orderInfo.status || '未知状态' }}
				</text>
			</view>
			
			<view style="border-top: 1rpx dashed #e0e0e0; padding-top: 20rpx;">
				<view style="display: flex; justify-content: space-between; margin-bottom: 12rpx;">
					<text style="color: #999; font-size: 26rpx;">订单号：</text>
					<text style="color: #333; font-size: 26rpx; font-weight: bold;">{{ orderInfo.orderNo || '未知' }}</text>
				</view>
				<view style="display: flex; justify-content: space-between; margin-bottom: 12rpx;">
					<text style="color: #999; font-size: 26rpx;">下单时间：</text>
					<text style="color: #333; font-size: 26rpx;">{{ formatTime(orderInfo.time) }}</text>
				</view>
				<view style="display: flex; justify-content: space-between;">
					<text style="color: #999; font-size: 26rpx;">支付方式：</text>
					<text style="color: #333; font-size: 26rpx;">在线支付</text>
				</view>
			</view>
		</view>
		
		<!-- 商品信息 -->
		<view style="background: #fff; border-radius: 0; padding: 30rpx; margin-bottom: 20rpx;">
			<text style="font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 30rpx;">商品信息</text>
			
			<view style="display: flex; align-items: center; padding-bottom: 30rpx; border-bottom: 1rpx solid #f0f0f0; margin-bottom: 30rpx;">
				<image 
					:src="orderInfo.goodsImg || '/static/images/product-default.png'" 
					style="width: 160rpx; height: 160rpx; border-radius: 12rpx; margin-right: 20rpx; background: #f5f5f5;"
					mode="aspectFill"
				></image>
				<view style="flex: 1;">
					<text style="font-size: 32rpx; font-weight: bold; color: #333; line-height: 1.4; display: block; margin-bottom: 12rpx;">
						{{ orderInfo.goodsName || '未知商品' }}
					</text>
					<text v-if="orderInfo.specials" style="font-size: 26rpx; color: #999; line-height: 1.3; display: block; margin-bottom: 16rpx;">
						{{ orderInfo.specials }}
					</text>
					<view style="display: flex; align-items: baseline; justify-content: space-between;">
						<view>
							<text style="font-size: 36rpx; font-weight: bold; color: #ff4757;">
								¥{{ formatPrice(orderInfo.price) }}
							</text>
							<text v-if="orderInfo.unit" style="font-size: 26rpx; color: #999; margin: 0 8rpx;">
								/{{ orderInfo.unit }}
							</text>
						</view>
						<text style="font-size: 30rpx; color: #666;">
							×{{ orderInfo.num || 1 }}
						</text>
					</view>
				</view>
			</view>
			
			<!-- 订单金额明细 -->
			<view>
				<view style="display: flex; justify-content: space-between; margin-bottom: 20rpx;">
					<text style="font-size: 28rpx; color: #666;">商品金额</text>
					<text style="font-size: 28rpx; color: #333;">¥{{ formatPrice(calculateGoodsTotal()) }}</text>
				</view>
				<view style="display: flex; justify-content: space-between; margin-bottom: 20rpx;">
					<text style="font-size: 28rpx; color: #666;">运费</text>
					<text style="font-size: 28rpx; color: #333;">¥0.00</text>
				</view>
				<view style="display: flex; justify-content: space-between; margin-bottom: 20rpx;">
					<text style="font-size: 28rpx; color: #666;">优惠金额</text>
					<text style="font-size: 28rpx; color: #ff4757;">-¥0.00</text>
				</view>
				<view style="display: flex; justify-content: space-between; padding-top: 20rpx; border-top: 1rpx solid #f0f0f0;">
					<text style="font-size: 32rpx; font-weight: bold; color: #333;">实付金额</text>
					<text style="font-size: 36rpx; font-weight: bold; color: #ff4757;">¥{{ formatPrice(orderInfo.total) || formatPrice(calculateGoodsTotal()) }}</text>
				</view>
			</view>
		</view>
		
		<!-- 订单追踪 -->
		<view style="background: #fff; border-radius: 0; padding: 30rpx; margin-bottom: 120rpx;">
			<text style="font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 30rpx;">订单追踪</text>
			
			<view v-for="(log, index) in orderLogs" :key="index" style="margin-bottom: 30rpx;">
				<view style="display: flex;">
					<view style="position: relative; margin-right: 20rpx;">
						<view v-if="index === 0" style="width: 24rpx; height: 24rpx; background: #4CD964; border-radius: 50%;"></view>
						<view v-else style="width: 24rpx; height: 24rpx; background: #e0e0e0; border-radius: 50%;"></view>
						<view v-if="index < orderLogs.length - 1" style="position: absolute; left: 12rpx; top: 24rpx; width: 2rpx; height: 50rpx; background: #e0e0e0;"></view>
					</view>
					
					<view style="flex: 1;">
						<text style="font-size: 28rpx; color: #333; display: block; margin-bottom: 8rpx;">{{ log.action }}</text>
						<text style="font-size: 24rpx; color: #999;">{{ log.time }}</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 底部操作按钮 - 添加.stop修饰符 -->
		<view style="position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 20rpx 30rpx; border-top: 1rpx solid #f0f0f0;">
			<view style="display: flex; justify-content: space-between; gap: 20rpx;">
				<!-- 待支付 -->
				<view v-if="orderInfo.status === '待支付'" style="display: flex; width: 100%; gap: 20rpx;">
					<view 
						@click.stop="handleCancel"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#fff',
							color: '#666',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							border: '2rpx solid #e0e0e0',
							textAlign: 'center'
						}"
					>
						取消订单
					</view>
					<view 
						@click.stop="handlePay"
						:style="{
							flex: 2,
							padding: '24rpx 0',
							background: '#4CD964',
							color: '#fff',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							textAlign: 'center'
						}"
					>
						立即支付 ¥{{ formatPrice(orderInfo.total) || formatPrice(calculateGoodsTotal()) }}
					</view>
				</view>
				
				<!-- 待发货 -->
				<view v-if="orderInfo.status === '待发货'" style="display: flex; width: 100%; gap: 20rpx;">
					<view 
						@click.stop="contactService"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#fff',
							color: '#666',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							border: '2rpx solid #e0e0e0',
							textAlign: 'center'
						}"
					>
						联系客服
					</view>
					<view 
						@click.stop="handleBuyAgain"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#4CD964',
							color: '#fff',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							textAlign: 'center'
						}"
					>
						再次购买
					</view>
				</view>
				
				<!-- 待收货 -->
				<view v-if="orderInfo.status === '待收货'" style="display: flex; width: 100%; gap: 20rpx;">
					<view 
						@click.stop="contactService"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#fff',
							color: '#666',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							border: '2rpx solid #e0e0e0',
							textAlign: 'center'
						}"
					>
						联系客服
					</view>
					<view 
						@click.stop="handleConfirmReceipt"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#4CD964',
							color: '#fff',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							textAlign: 'center'
						}"
					>
						确认收货
					</view>
				</view>
				
				<!-- 已完成 -->
				<view v-if="orderInfo.status === '已完成'" style="display: flex; width: 100%; gap: 20rpx;">
					<view 
						@click.stop="handleBuyAgain"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#fff',
							color: '#409eff',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							border: '2rpx solid #e0e0e0',
							textAlign: 'center'
						}"
					>
						再次购买
					</view>
					<view 
						@click.stop="contactService"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#4CD964',
							color: '#fff',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							textAlign: 'center'
						}"
					>
						联系客服
					</view>
				</view>
				
				<!-- 已取消 -->
				<view v-if="orderInfo.status === '已取消'" style="display: flex; width: 100%; gap: 20rpx;">
					<view 
						@click.stop="handleDelete"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#fff',
							color: '#666',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							border: '2rpx solid #e0e0e0',
							textAlign: 'center'
						}"
					>
						删除订单
					</view>
					<view 
						@click.stop="handleBuyAgain"
						:style="{
							flex: 1,
							padding: '24rpx 0',
							background: '#4CD964',
							color: '#fff',
							fontSize: '30rpx',
							borderRadius: '40rpx',
							textAlign: 'center'
						}"
					>
						再次购买
					</view>
				</view>
			</view>
		</view>
		
		<!-- 底部安全区域 -->
		<view style="height: 120rpx;"></view>
		
		<!-- 加载中 -->
		<view v-if="loading" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(255,255,255,0.9); display: flex; justify-content: center; align-items: center; z-index: 999;">
			<view style="text-align: center;">
				<view style="width: 80rpx; height: 80rpx; border: 6rpx solid #f3f3f3; border-top: 6rpx solid #4CD964; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 20rpx;"></view>
				<text style="color: #333; font-size: 32rpx;">加载中...</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				orderInfo: {
					id: '',
					orderNo: '',
					status: '',
					goodsName: '',
					goodsImg: '',
					specials: '',
					price: 0,
					unit: '',
					num: 1,
					total: 0,
					time: '',
					addressInfo: '',
					goodsId: '',
					addressId: ''
				},
				orderLogs: [],
				loading: false,
				orderId: '',
				// 解析后的地址信息
				parsedAddress: {
					consignee: '',
					phone: '',
					province: '',
					city: '',
					district: '',
					detail: '',
					isDefault: false
				}
			}
		},
		onLoad(options) {
			if (options.id) {
				this.orderId = options.id
				this.loadOrderDetail()
			} else {
				uni.showToast({
					title: '订单不存在',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			}
		},
		onShow() {
			// 从地址管理页返回时，检查是否有选择的地址并更新
			this.checkAndUpdateAddress()
		},
		methods: {
			// 加载订单详情
			loadOrderDetail() {
				this.loading = true
				
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none',
						duration: 1500
					})
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					}, 1500)
					this.loading = false
					return
				}
				
				this.$request.get('/orders/selectById/' + this.orderId).then(res => {
					console.log('订单详情接口返回数据:', res)
					if (res && res.code === '200') {
						this.orderInfo = res.data || {}
						// 解析地址信息
						this.parseAddressInfo()
						// 生成订单日志
						this.generateOrderLogs()
					} else {
						uni.showToast({
							icon: 'none',
							title: res?.msg || '加载失败'
						})
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					}
				}).catch(err => {
					console.error('加载订单详情失败:', err)
					uni.showToast({
						icon: 'none',
						title: '网络错误'
					})
					setTimeout(() => {
						uni.navigateBack()
					}, 1500)
				}).finally(() => {
					this.loading = false
				})
			},
			
			// 解析地址信息
			parseAddressInfo() {
				if (!this.orderInfo.addressInfo) {
					this.parsedAddress = {
						consignee: '',
						phone: '',
						province: '',
						city: '',
						district: '',
						detail: '',
						isDefault: false
					}
					return
				}
				
				const addressStr = this.orderInfo.addressInfo
				console.log('解析地址字符串:', addressStr)
				
				try {
					// 假设地址格式为：收货人 手机号 省份城市区县详细地址
					// 例如："周肖宇 13565656565 四川省成都市金堂县白果街道 成都工业职业技术学院"
					
					// 分割字符串
					const parts = addressStr.split(' ')
					
					if (parts.length >= 2) {
						const consignee = parts[0]
						const phone = parts[1]
						
						// 剩余部分是地址
						let addressPart = parts.slice(2).join(' ')
						
						// 解析地址部分
						let province = ''
						let city = ''
						let district = ''
						let detail = addressPart
						let isDefault = false
						
						// 检查是否包含默认标记
						if (addressPart.includes('默认')) {
							isDefault = true
							addressPart = addressPart.replace('默认', '').trim()
							detail = addressPart
						}
						
						// 简单的地址解析（可以根据实际情况调整）
						// 这里假设地址格式为：省份+城市+区县+详细地址
						// 例如："四川省成都市金堂县白果街道 成都工业职业技术学院"
						
						// 可以在这里添加更复杂的地址解析逻辑
						// 暂时只简单分割
						
						this.parsedAddress = {
							consignee,
							phone,
							province: province || '',
							city: city || '',
							district: district || '',
							detail: detail || addressPart,
							isDefault
						}
						
						console.log('解析后的地址信息:', this.parsedAddress)
					} else {
						// 如果格式不符合预期，使用原字符串
						this.parsedAddress = {
							consignee: '',
							phone: '',
							province: '',
							city: '',
							district: '',
							detail: addressStr,
							isDefault: false
						}
					}
				} catch (error) {
					console.error('解析地址信息失败:', error)
					this.parsedAddress = {
						consignee: '',
						phone: '',
						province: '',
						city: '',
						district: '',
						detail: addressStr,
						isDefault: false
					}
				}
			},
			
			// 获取收货人姓名
			getConsignee() {
				return this.parsedAddress.consignee || '未知收货人'
			},
			
			// 获取手机号
			getPhone() {
				return this.parsedAddress.phone || '未知手机号'
			},
			
			// 获取详细地址
			getAddressDetail() {
				const province = this.parsedAddress.province || ''
				const city = this.parsedAddress.city || ''
				const district = this.parsedAddress.district || ''
				const detail = this.parsedAddress.detail || ''
				
				// 如果有省份城市区县信息，拼接显示
				if (province || city || district) {
					return `${province}${city}${district}${detail}`
				}
				return detail || this.orderInfo.addressInfo || '未知地址'
			},
			
			// 是否默认地址
			isDefaultAddress() {
				return this.parsedAddress.isDefault || false
			},
			
			// 检查并更新地址 - 从地址管理页返回时调用
			checkAndUpdateAddress() {
				const selectedAddress = uni.getStorageSync('selected_address')
				const fromPage = uni.getStorageSync('from_page')
				
				// 检查是否是从地址管理页返回，并且是为当前订单选择的地址
				if (selectedAddress && fromPage === 'order_detail') {
					console.log('从地址管理页返回，更新订单地址:', selectedAddress)
					
					// 更新订单地址信息
					this.updateOrderAddress(selectedAddress)
					
					// 清除存储
					uni.removeStorageSync('selected_address')
					uni.removeStorageSync('from_page')
					uni.removeStorageSync('order_for_address')
				}
			},
			
			// 显示地址选择
			showAddressSelect() {
				if (this.orderInfo.status !== '待支付') {
					uni.showToast({
						title: '当前订单状态不可修改地址',
						icon: 'none'
					})
					return
				}
				
				const user = uni.getStorageSync('user') || {}
				if (!user.id) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					})
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/login/login'
						})
					}, 1500)
					return
				}
				
				// 存储当前订单信息
				uni.setStorageSync('order_for_address', {
					orderId: this.orderId,
					fromPage: 'order_detail'
				})
				
				// 跳转到地址管理页面
				uni.navigateTo({
					url: `/pages/address/address?from=order_detail&orderId=${this.orderId}`,
					success: () => {
						console.log('跳转到地址管理页选择新地址')
					}
				})
			},
			
			// 更新订单地址
			updateOrderAddress(newAddress) {
				// 构建地址字符串（与图片样式一致）
				let addressStr = `${newAddress.consignee} ${newAddress.phone} ${newAddress.province}${newAddress.city}${newAddress.district}${newAddress.detail}`
				
				// 如果是默认地址，添加默认标记
				if (newAddress.isDefault) {
					addressStr += ' 默认'
				}
				
				// 更新本地显示
				this.orderInfo.addressInfo = addressStr
				this.orderInfo.addressId = newAddress.id
				
				// 更新解析后的地址信息
				this.parsedAddress = {
					consignee: newAddress.consignee,
					phone: newAddress.phone,
					province: newAddress.province,
					city: newAddress.city,
					district: newAddress.district,
					detail: newAddress.detail,
					isDefault: newAddress.isDefault || false
				}
				
				// 调用后端接口更新订单地址
				const updateData = {
					...this.orderInfo,
					addressId: newAddress.id,
					addressInfo: addressStr
				}
				
				// 移除不需要的字段
				delete updateData.createTime
				delete updateData.updateTime
				
				console.log('更新订单地址数据:', updateData)
				
				// 显示加载中
				uni.showLoading({
					title: '更新中...'
				})
				
				this.$request.put('/orders/update', updateData).then(res => {
					uni.hideLoading()
					
					if (res && res.code === '200') {
						uni.showToast({
							icon: 'success',
							title: '地址更新成功'
						})
						
						// 重新加载订单详情，确保数据一致
						this.loadOrderDetail()
					} else {
						uni.showToast({
							icon: 'none',
							title: res.msg || '地址更新失败'
						})
					}
				}).catch((err) => {
					uni.hideLoading()
					console.error('更新订单地址失败:', err)
					uni.showToast({
						icon: 'none',
						title: '网络错误'
					})
				})
			},
			
			generateOrderLogs() {
				const logs = []
				const createTime = this.orderInfo.time
				
				logs.push({
					action: '订单创建成功',
					time: this.formatTime(createTime)
				})
				
				if (this.orderInfo.status === '待支付') {
					logs.push({
						action: '等待买家付款',
						time: '等待中...'
					})
				} else if (this.orderInfo.status === '待发货') {
					logs.push({
						action: '买家已付款',
						time: this.formatTime(createTime)
					})
					logs.push({
						action: '等待卖家发货',
						time: '等待中...'
					})
				} else if (this.orderInfo.status === '待收货') {
					logs.push({
						action: '买家已付款',
						time: this.formatTime(createTime)
					})
					logs.push({
						action: '卖家已发货',
						time: this.formatTime(createTime)
					})
					logs.push({
						action: '等待买家收货',
						time: '等待中...'
					})
				} else if (this.orderInfo.status === '已完成') {
					logs.push({
						action: '买家已付款',
						time: this.formatTime(createTime)
					})
					logs.push({
						action: '卖家已发货',
						time: this.formatTime(createTime)
					})
					logs.push({
						action: '买家已收货',
						time: this.formatTime(createTime)
					})
					logs.push({
						action: '订单已完成',
						time: this.formatTime(createTime)
					})
				} else if (this.orderInfo.status === '已取消') {
					logs.push({
						action: '订单已取消',
						time: this.formatTime(createTime)
					})
				}
				
				this.orderLogs = logs
			},
			
			calculateGoodsTotal() {
				const price = parseFloat(this.orderInfo.price) || 0
				const num = parseInt(this.orderInfo.num) || 1
				return (price * num).toFixed(2)
			},
			
			formatPrice(price) {
				if (!price && price !== 0) return '0.00'
				const num = parseFloat(price)
				if (isNaN(num)) return '0.00'
				return num.toFixed(2)
			},
			
			handlePay() {
				if (this.orderInfo.status !== '待支付') {
					uni.showToast({
						title: '该订单无法支付',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '确认支付',
					content: `确定要支付订单 ${this.orderInfo.orderNo} 吗？\n金额：¥${this.formatPrice(this.orderInfo.total) || this.formatPrice(this.calculateGoodsTotal())}`,
					success: (res) => {
						if (res.confirm) {
							const updateData = {
								...this.orderInfo,
								status: '待发货'
							}
							
							this.$request.put('/orders/update', updateData).then(res => {
								if (res && res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '支付成功'
									})
									this.loadOrderDetail()
								} else {
									uni.showToast({
										icon: 'none',
										title: res.msg || '支付失败'
									})
								}
							}).catch(() => {
								uni.showToast({
									icon: 'none',
									title: '支付失败'
								})
							})
						}
					}
				})
			},
			
			handleCancel() {
				if (this.orderInfo.status !== '待支付') {
					uni.showToast({
						title: '该订单无法取消',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '确认取消',
					content: `确定要取消订单 ${this.orderInfo.orderNo} 吗？`,
					success: (res) => {
						if (res.confirm) {
							const updateData = {
								...this.orderInfo,
								status: '已取消'
							}
							
							this.$request.put('/orders/update', updateData).then(res => {
								if (res && res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '订单已取消'
									})
									this.loadOrderDetail()
								} else {
									uni.showToast({
										icon: 'none',
										title: res.msg || '取消失败'
									})
								}
							}).catch(() => {
								uni.showToast({
									icon: 'none',
									title: '取消失败'
								})
							})
						}
					}
				})
			},
			
			handleConfirmReceipt() {
				if (this.orderInfo.status !== '待收货') {
					uni.showToast({
						title: '该订单无法确认收货',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '确认收货',
					content: `确定已收到商品吗？\n订单号：${this.orderInfo.orderNo}`,
					success: (res) => {
						if (res.confirm) {
							const updateData = {
								...this.orderInfo,
								status: '已完成'
							}
							
							this.$request.put('/orders/update', updateData).then(res => {
								if (res && res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '确认收货成功'
									})
									this.loadOrderDetail()
								} else {
									uni.showToast({
										icon: 'none',
										title: res.msg || '操作失败'
										})
									}
								}).catch(() => {
									uni.showToast({
										icon: 'none',
										title: '操作失败'
									})
								})
							}
						}
					})
				},
				
				handleDelete() {
					if (this.orderInfo.status !== '已取消') {
						uni.showToast({
							title: '只能删除已取消的订单',
							icon: 'none'
						})
						return
					}
					
					uni.showModal({
						title: '删除订单',
						content: `确定要永久删除订单 ${this.orderInfo.orderNo} 吗？`,
						success: (res) => {
							if (res.confirm) {
								this.$request.delete('/orders/delete/' + this.orderInfo.id).then(res => {
									if (res.code === '200') {
										uni.showToast({
											icon: 'success',
											title: '删除成功'
										})
										setTimeout(() => {
											uni.navigateBack()
										}, 1500)
									} else {
										uni.showToast({
											icon: 'none',
											title: res.msg
										})
									}
								}).catch(() => {
									uni.showToast({
										icon: 'none',
										title: '删除失败'
									})
								})
							}
						}
					})
				},
				
				handleBuyAgain() {
					if (!this.orderInfo.goodsId) {
						uni.showToast({
							title: '商品信息错误',
							icon: 'none'
						})
						return
					}
					
					uni.navigateTo({
						url: `/pages/product/detail?id=${this.orderInfo.goodsId}`
					})
				},
				
				contactService() {
					uni.showModal({
						title: '联系客服',
						content: '客服电话：11111111\n工作时间：9:00-18:00',
						showCancel: false,
						confirmText: '确定'
					})
				},
				
				formatTime(time) {
					if (!time) return ''
					if (typeof time === 'number') {
						const date = new Date(time)
						return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
					}
					return time
				}
			}
		}
</script>

<style>
	@keyframes spin {
		0% { transform: rotate(0deg); }
		100% { transform: rotate(360deg); }
	}
</style>