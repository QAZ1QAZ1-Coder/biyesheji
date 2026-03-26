<template>
	<view class="product-detail">
		<!-- 商品图片 -->
		<view class="product-image-section">
			<image 
				:src="product.img || '/static/images/product-default.png'" 
				mode="aspectFill"
				class="product-image"
				@error="onImageError"
			></image>
		</view>
		
		<!-- 商品基本信息 -->
		<view class="product-info-section">
			<text class="product-name">{{ product.name || '商品名称' }}</text>
			<view class="price-section">
				<text class="current-price">¥{{ product.price || '0.00' }}</text>
				<text class="price-unit">/{{ product.unit || '斤' }}</text>
			</view>
			<text class="product-description">{{ product.descr || '商品描述' }}</text>
			
			<view class="product-tags">
				<view class="tag special-tag" v-if="product.specials">
					{{ product.specials }}
				</view>
			</view>
			
			<view class="product-meta">
				<view class="meta-item">
					<text class="meta-label">库存</text>
					<text class="meta-value" :class="{ 'low-stock': product.store <= 10 }">
						{{ product.store || 0 }}{{ product.unit || '斤' }}
					</text>
				</view>
			</view>
			
			<view class="stock-warning" v-if="product.store <= 10">
				<text class="warning-text">⚠️ 库存紧张，欲购从速</text>
			</view>
		</view>
		
		<!-- 收货地址 -->
		<view class="address-section" @click="showAddressSelect">
			<view class="section-header">
				<text class="section-title">收货地址</text>
				<text class="change-address">更换</text>
			</view>
			<view class="address-content" v-if="defaultAddress">
				<view class="address-main">
					<text class="consignee">{{ defaultAddress.consignee }}</text>
					<text class="phone">{{ defaultAddress.phone }}</text>
					<view v-if="defaultAddress.isDefault" class="default-tag">默认</view>
				</view>
				<text class="address-detail">
					{{ defaultAddress.province }}{{ defaultAddress.city }}{{ defaultAddress.district }}{{ defaultAddress.detail }}
				</text>
			</view>
			<view class="no-address" v-else>
				<uni-icons type="location" size="20" color="#999"></uni-icons>
				<text class="no-address-text">请选择收货地址</text>
			</view>
		</view>
		
		<!-- 购买数量 -->
		<view class="quantity-section">
			<view class="section-header">
				<text class="section-title">购买数量</text>
			</view>
			<view class="quantity-controls">
				<button class="quantity-btn" :disabled="quantity <= 1" @click="decreaseQuantity">-</button>
				<input 
					class="quantity-input" 
					type="number" 
					v-model="quantity" 
					@input="handleQuantityInput"
					:max="product.store"
				/>
				<button class="quantity-btn" :disabled="quantity >= product.store" @click="increaseQuantity">+</button>
			</view>
		</view>
		
		<!-- 底部操作栏 -->
		<view class="action-bar">
			<view class="total-price">
				<text class="total-label">总价:</text>
				<text class="total-amount">¥{{ totalPrice }}</text>
			</view>
			<button 
				class="buy-btn" 
				:class="{ 'disabled': !canBuy }"
				@click="handleBuy"
				:disabled="!canBuy"
			>
				{{ buyButtonText }}
			</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				productId: '',
				product: {
					store: 0,
					price: 0,
					unit: '斤'
				},
				quantity: 1,
				defaultAddress: null
			}
		},
		computed: {
			totalPrice() {
				const price = parseFloat(this.product.price) || 0
				return (price * this.quantity).toFixed(2)
			},
			canBuy() {
				const store = parseInt(this.product.store) || 0
				return this.quantity > 0 && this.defaultAddress && this.quantity <= store
			},
			buyButtonText() {
				if (!this.defaultAddress) return '请选择地址'
				if (this.quantity <= 0) return '选择数量'
				const store = parseInt(this.product.store) || 0
				if (this.quantity > store) return '库存不足'
				return '立即购买'
			}
		},
		onLoad(options) {
			this.productId = options.id
			if (options.quantity) {
				this.quantity = parseInt(options.quantity) || 1
			}
			this.loadProductDetail()
			this.loadDefaultAddress()
		},
		onShow() {
			// 检查是否有从地址页返回的数据
			this.checkAddressFromStorage()
		},
		methods: {
			async loadProductDetail() {
				try {
					const res = await this.$request.get(`/goods/detail/${this.productId}`)
					
					if (res && res.code === '200') {
						this.product = res.data || {}
					} else {
						await this.loadProductDetailFallback()
					}
				} catch (error) {
					console.error('加载商品详情失败:', error)
					await this.loadProductDetailFallback()
				}
			},
			
			async loadProductDetailFallback() {
				try {
					const res = await this.$request.get(`/goods/selectById/${this.productId}`)
					
					if (res && res.code === '200' && res.data) {
						this.product = res.data
					} else {
						uni.showToast({
							title: '商品不存在',
							icon: 'none'
						})
						setTimeout(() => {
							uni.navigateBack()
						}, 1500)
					}
				} catch (error) {
					console.error('备用加载也失败:', error)
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					})
				}
			},
			
			// 加载默认地址
			async loadDefaultAddress() {
				try {
					const user = uni.getStorageSync('user') || {}
					
					if (!user.id) {
						console.log('用户未登录')
						this.defaultAddress = null
						return
					}
					
					// 先检查是否有从地址页选择的地址
					const selectedAddress = uni.getStorageSync('selected_address')
					if (selectedAddress) {
						this.defaultAddress = selectedAddress
						// 清除存储，避免下次重复使用
						uni.removeStorageSync('selected_address')
						return
					}
					
					// 如果没有选择的地址，则加载默认地址
					const res = await this.$request.get(`/address/selectByUserId/${user.id}`)
					
					if (res && res.code === '200') {
						const addressList = res.data || []
						
						if (addressList.length > 0) {
							// 查找默认地址
							const defaultAddr = addressList.find(item => item.isDefault)
							// 如果没有默认地址，使用第一个地址
							this.defaultAddress = defaultAddr || addressList[0]
						} else {
							this.defaultAddress = null
						}
					} else {
						this.defaultAddress = null
					}
				} catch (error) {
					console.error('加载地址失败:', error)
					this.defaultAddress = null
				}
			},
			
			// 检查是否有从地址页返回的数据
			checkAddressFromStorage() {
				const selectedAddress = uni.getStorageSync('selected_address')
				if (selectedAddress) {
					this.defaultAddress = selectedAddress
					// 清除存储
					uni.removeStorageSync('selected_address')
					console.log('已从地址页接收到选择的地址:', selectedAddress)
				}
			},
			
			// 点击"更换"地址 - 跳转到地址管理页
			showAddressSelect() {
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
				
				// 跳转到地址管理页面
				uni.navigateTo({
					url: '/pages/address/address?from=detail',
					success: () => {
						console.log('跳转到地址管理页')
					}
				})
			},
			
			increaseQuantity() {
				const store = parseInt(this.product.store) || 0
				if (this.quantity < store) {
					this.quantity++
				} else {
					uni.showToast({
						title: `数量不能超过库存 ${store}`,
						icon: 'none'
					})
				}
			},
			
			decreaseQuantity() {
				if (this.quantity > 1) {
					this.quantity--
				}
			},
			
			handleQuantityInput(e) {
				let value = parseInt(e.detail.value) || 1
				const store = parseInt(this.product.store) || 0
				
				if (isNaN(value) || value < 1) {
					value = 1
				}
				
				if (value > store) {
					value = store
					uni.showToast({
						title: `数量不能超过库存量 ${store}`,
						icon: 'none'
					})
				}
				
				this.quantity = value
			},
			
			async handleBuy() {
				if (!this.canBuy) {
					if (!this.defaultAddress) {
						uni.showToast({ title: '请选择收货地址', icon: 'none' })
					} else if (this.quantity <= 0) {
						uni.showToast({ title: '请选择购买数量', icon: 'none' })
					} else {
						uni.showToast({ title: '购买数量不能超过库存', icon: 'none' })
					}
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
				
				const orderData = {
					goodsId: this.product.id,
					goodsName: this.product.name,
					goodsImg: this.product.img,
					price: this.product.price,
					num: this.quantity,
					total: this.totalPrice,
					userName: user.name,
					userId: user.id,
					status: '待支付',
					time: new Date().toLocaleString(),
					specials: this.product.specials,
					unit: this.product.unit,
					addressId: this.defaultAddress.id,
					addressInfo: `${this.defaultAddress.consignee} ${this.defaultAddress.phone} ${this.defaultAddress.province}${this.defaultAddress.city}${this.defaultAddress.district}${this.defaultAddress.detail}`
				}
				
				try {
					const res = await this.$request.post('/orders/add', orderData)
					
					if (res && res.code === '200') {
						uni.showToast({ 
							title: '购买成功，请在订单管理中查看', 
							icon: 'success',
							duration: 2000
						})
						
						setTimeout(() => {
							uni.switchTab({
								url: '/pages/order/order'
							})
						}, 1500)
					} else {
						uni.showToast({ title: res.msg || '购买失败', icon: 'none' })
					}
				} catch (error) {
					console.error('购买失败:', error)
					uni.showToast({ title: '购买失败，请重试', icon: 'none' })
				}
			},
			
			onImageError(e) {
				this.product.img = '/static/images/product-default.png'
			}
		}
	}
</script>

<style scoped>
	/* 样式保持不变，与之前的相同 */
	.product-detail {
		background: #f8f8f8;
		min-height: 100vh;
		padding-bottom: 140rpx;
	}
	
	.product-image-section {
		width: 100%;
		height: 400rpx;
		background: #fff;
	}
	
	.product-image {
		width: 100%;
		height: 100%;
	}
	
	.product-info-section {
		background: #fff;
		padding: 40rpx 30rpx;
		margin: 20rpx 0;
		border-radius: 0;
	}
	
	.product-name {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		display: block;
		margin-bottom: 25rpx;
		line-height: 1.5;
	}
	
	.price-section {
		display: flex;
		align-items: baseline;
		margin-bottom: 25rpx;
	}
	
	.current-price {
		font-size: 48rpx;
		font-weight: bold;
		color: #ff4757;
	}
	
	.price-unit {
		font-size: 28rpx;
		color: #999;
		margin-left: 15rpx;
	}
	
	.product-description {
		font-size: 30rpx;
		color: #666;
		line-height: 1.6;
		display: block;
		margin-bottom: 25rpx;
	}
	
	.product-tags {
		margin-bottom: 25rpx;
	}
	
	.tag {
		display: inline-block;
		padding: 12rpx 24rpx;
		border-radius: 24rpx;
		font-size: 24rpx;
		margin-right: 15rpx;
	}
	
	.special-tag {
		background: #e8f5e8;
		color: #4CD964;
		border: 1rpx solid #4CD964;
	}
	
	.product-meta {
		display: flex;
		justify-content: flex-start;
		margin-bottom: 15rpx;
		padding: 20rpx 0;
		border-top: 1rpx solid #f0f0f0;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.meta-item {
		display: flex;
		flex-direction: row;
		align-items: center;
	}
	
	.meta-label {
		font-size: 26rpx;
		color: #999;
		margin-right: 12rpx;
	}
	
	.meta-value {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
	}
	
	.low-stock {
		color: #e6a23c;
	}
	
	.stock-warning {
		margin-top: 20rpx;
		padding: 20rpx;
		background: #fdf6ec;
		border-radius: 12rpx;
		text-align: center;
	}
	
	.warning-text {
		font-size: 26rpx;
		color: #e6a23c;
	}
	
	.address-section {
		background: #fff;
		padding: 35rpx 30rpx;
		margin: 20rpx 0;
		border-radius: 0;
	}
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 25rpx;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}
	
	.change-address {
		font-size: 28rpx;
		color: #4CD964;
	}
	
	.address-main {
		display: flex;
		align-items: center;
		margin-bottom: 15rpx;
		flex-wrap: wrap;
	}
	
	.consignee {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-right: 25rpx;
	}
	
	.phone {
		font-size: 28rpx;
		color: #666;
		margin-right: 25rpx;
	}
	
	.default-tag {
		background: #ff4757;
		color: #fff;
		font-size: 22rpx;
		padding: 6rpx 16rpx;
		border-radius: 20rpx;
	}
	
	.address-detail {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
		display: block;
	}
	
	.no-address {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 50rpx 0;
		color: #999;
	}
	
	.no-address-text {
		font-size: 28rpx;
		margin-left: 15rpx;
	}
	
	.quantity-section {
		background: #fff;
		padding: 35rpx 30rpx;
		margin: 20rpx 0;
		border-radius: 0;
	}
	
	.quantity-controls {
		display: flex;
		align-items: center;
		justify-content: flex-start;
	}
	
	.quantity-btn {
		width: 80rpx;
		height: 80rpx;
		background: #f0f0f0;
		border: none;
		border-radius: 12rpx;
		font-size: 36rpx;
		color: #333;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.quantity-btn:disabled {
		color: #ccc;
		background: #f8f8f8;
	}
	
	.quantity-input {
		width: 140rpx;
		height: 80rpx;
		text-align: center;
		margin: 0 25rpx;
		border: 2rpx solid #f0f0f0;
		border-radius: 12rpx;
		font-size: 32rpx;
		background: #fff;
	}
	
	.action-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background: #fff;
		padding: 20rpx 30rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.08);
		height: 120rpx;
		box-sizing: border-box;
		border-top: 1rpx solid #f0f0f0;
	}
	
	.total-price {
		display: flex;
		align-items: baseline;
		flex: 1;
	}
	
	.total-label {
		font-size: 28rpx;
		color: #333;
		margin-right: 12rpx;
	}
	
	.total-amount {
		font-size: 36rpx;
		font-weight: bold;
		color: #ff4757;
	}
	
	.buy-btn {
		background: #4CD964;
		color: #fff;
		border: none;
		border-radius: 8rpx;
		padding: 20rpx 40rpx;
		font-size: 28rpx;
		font-weight: 500;
		min-width: 160rpx;
		height: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 12rpx rgba(76, 217, 100, 0.3);
		transition: all 0.3s ease;
	}
	
	.buy-btn:active {
		transform: scale(0.98);
		box-shadow: 0 2rpx 8rpx rgba(76, 217, 100, 0.4);
	}
	
	.buy-btn.disabled {
		background: #ccc;
		color: #fff;
		box-shadow: none;
	}
	
	.buy-btn.disabled:active {
		transform: none;
		box-shadow: none;
	}
</style>