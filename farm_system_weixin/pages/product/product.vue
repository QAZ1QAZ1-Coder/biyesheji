<template>
	<view class="product-page">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-box">
				<uni-icons type="search" size="18" color="#999"></uni-icons>
				<input 
					v-model="searchKeyword" 
					class="search-input" 
					placeholder="请输入商品名称" 
					@confirm="searchProducts"
					@input="handleSearchInput"
				/>
				<!-- 添加清除按钮 -->
				<view 
					v-if="searchKeyword" 
					class="clear-btn" 
					@click="clearSearch"
				>
					<uni-icons type="clear" size="16" color="#999"></uni-icons>
				</view>
			</view>
		</view>
		
		<!-- 分类筛选 - 6个分类 -->
		<scroll-view class="category-scroll" scroll-x>
			<view class="category-list">
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '' }"
					@click="changeCategory('')"
				>
					全部
				</view>
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '1' }"
					@click="changeCategory('1')"
				>
					蔬菜
				</view>
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '2' }"
					@click="changeCategory('2')"
				>
					水果
				</view>
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '3' }"
					@click="changeCategory('3')"
				>
					农副产品
				</view>
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '4' }"
					@click="changeCategory('4')"
				>
					菌品
				</view>
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '5' }"
					@click="changeCategory('5')"
				>
					水产
				</view>
				<view 
					class="category-item" 
					:class="{ active: activeCategory === '6' }"
					@click="changeCategory('6')"
				>
					肉类
				</view>
			</view>
		</scroll-view>
		
		<!-- 搜索提示 - 有结果 -->
		<view class="search-tips" v-if="searchKeyword && productList.length > 0">
		    <text>搜索"{{ searchKeyword }}"的结果 ({{ productList.length }}个商品)</text>
		</view>
		
		<!-- 商品列表 -->
		<view class="product-list">
			<view 
				class="product-card" 
				v-for="product in productList" 
				:key="product.id"
			>
				<view class="product-image" @click="viewProductDetail(product.id)">
					<image 
						:src="product.img || '/static/images/product-default.png'" 
						mode="aspectFill"
						class="product-img"
						@error="onImageError"
					></image>
				</view>
				<view class="product-info">
					<text class="product-name" @click="viewProductDetail(product.id)">{{ product.name || '商品名称' }}</text>
					<text class="product-desc" @click="viewProductDetail(product.id)">{{ product.descr || '新鲜农产品' }}</text>
					
					<!-- 特色信息 -->
					<text class="product-specials" v-if="product.specials">{{ product.specials }}</text>
					
					<view class="product-bottom">
						<view class="price-section">
							<text class="product-price">¥{{ product.price || '0.00' }}</text>
							<text class="product-unit">/{{ product.unit || '斤' }}</text>
						</view>
						<view class="action-section">
							<text class="store">库存: {{ product.store || 0 }}</text>
							<button class="buy-btn" @click.stop="buyNow(product)">立即购买</button>
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 加载更多 -->
		<view class="load-more" v-if="hasMore && !searchKeyword">
			<text @click="loadMore">加载更多</text>
		</view>
		
		<!-- 搜索无结果 -->
		<view class="empty-state" v-if="productList.length === 0 && searchKeyword && !loading">
			<uni-icons type="search" size="60" color="#ccc"></uni-icons>
			<text class="empty-text">没有找到"{{ searchKeyword }}"相关的商品</text>
			<button class="retry-btn" @click="clearSearch">重新搜索</button>
		</view>
		
		<!-- 空状态 -->
		<view class="empty-state" v-if="productList.length === 0 && !searchKeyword && !loading">
			<uni-icons type="shop" size="60" color="#ccc"></uni-icons>
			<text class="empty-text">暂无商品</text>
		</view>
		
		<!-- 加载中 -->
		<view class="loading" v-if="loading">
			<text>加载中...</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				productList: [],
				searchKeyword: '',
				activeCategory: '',
				loading: false,
				pageNum: 1,
				pageSize: 10,
				hasMore: true,
				searchTimer: null, // 防抖定时器
				hasDefaultAddress: false, // 是否有默认地址
				isCreatingOrder: false, // 防止重复点击
				defaultAddress: null // 默认地址对象
			}
		},
		onLoad() {
			this.loadProducts()
			// 加载时检查默认地址
			this.checkDefaultAddress()
		},
		onShow() {
			// 每次页面显示时重新检查地址状态
			this.checkDefaultAddress()
		},
		onPullDownRefresh() {
			this.pageNum = 1
			this.loadProducts().finally(() => {
				uni.stopPullDownRefresh()
			})
		},
		methods: {
			// 检查是否有默认地址 - 修复API接口
			async checkDefaultAddress() {
				const user = uni.getStorageSync('user') || {}
				if (!user.id) {
					this.hasDefaultAddress = false
					this.defaultAddress = null
					return
				}
				
				try {
					// 修改：使用正确的API，根据用户ID获取地址
					const res = await this.$request.get(`/address/selectByUserId/${user.id}`)
					
					if (res && res.code === '200') {
						const addressList = res.data || []
						const defaultAddress = addressList.find(item => item.isDefault)
						
						// 如果没有默认地址但地址列表不为空，使用第一个地址
						if (!defaultAddress && addressList.length > 0) {
							this.defaultAddress = addressList[0]
							this.hasDefaultAddress = true
						} else if (defaultAddress) {
							this.defaultAddress = defaultAddress
							this.hasDefaultAddress = true
						} else {
							this.defaultAddress = null
							this.hasDefaultAddress = false
						}
						
						console.log('检查默认地址结果:', {
							userId: user.id,
							addressListLength: addressList.length,
							hasDefault: this.hasDefaultAddress,
							defaultAddress: this.defaultAddress
						})
					} else {
						console.log('获取地址失败:', res)
						this.defaultAddress = null
						this.hasDefaultAddress = false
					}
				} catch (error) {
					console.error('检查地址失败:', error)
					this.defaultAddress = null
					this.hasDefaultAddress = false
				}
			},
			
			async loadProducts() {
				this.loading = true
				try {
					const params = {
						pageNum: this.pageNum,
						pageSize: this.pageSize
					}
					
					if (this.activeCategory) {
						params.categoryId = this.activeCategory
					}
					
					// 修复：根据后端 GoodsMapper.xml，搜索参数应该是 name
					if (this.searchKeyword && this.searchKeyword.trim()) {
						params.name = this.searchKeyword.trim()
					}
					
					console.log('请求参数:', params)
					
					const res = await this.$request.get('/goods/list', params)
					
					console.log('API响应:', res)
					console.log('商品列表原始数据:', res.data?.list)
					
					if (res && res.code === '200') {
						const productListData = res.data.list || []
						console.log('商品数据详情:', productListData)
						
						// 调试：检查每个商品的字段
						productListData.forEach((product, index) => {
							console.log(`商品${index + 1}:`, {
								id: product.id,
								name: product.name,
								descr: product.descr,
								specials: product.specials,
								price: product.price,
								store: product.store,
								img: product.img
							})
						})
						
						if (this.pageNum === 1) {
							this.productList = productListData
						} else {
							this.productList = [...this.productList, ...productListData]
						}
						
						// 判断是否还有更多数据
						this.hasMore = this.productList.length < (res.data.total || 0)
						
					} else {
						console.log('API返回失败:', res)
						uni.showToast({
							title: res.msg || '加载失败',
							icon: 'none'
						})
					}
				} catch (error) {
					console.error('加载商品失败:', error)
					uni.showToast({
						title: '网络错误，请重试',
						icon: 'none'
					})
				} finally {
					this.loading = false
				}
			},
			
			// 立即购买 - 直接购买，不跳转详情页
			async buyNow(product) {
				// 防止重复点击
				if (this.isCreatingOrder) {
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
				
				// 检查默认地址
				if (!this.hasDefaultAddress || !this.defaultAddress) {
					// 没有默认地址，提示去设置
					uni.showModal({
						title: '提示',
						content: '请先设置默认收货地址',
						confirmText: '去设置',
						cancelText: '取消',
						success: (res) => {
							if (res.confirm) {
								uni.navigateTo({
									url: '/pages/address/address'
								})
							}
						}
					})
					return
				}
				
				// 有默认地址，直接购买
				await this.directBuy(product, this.defaultAddress)
			},
			
			// 直接购买方法
			async directBuy(product, defaultAddress) {
				this.isCreatingOrder = true
				
				const orderData = {
					goodsId: product.id,
					goodsName: product.name,
					goodsImg: product.img,
					price: product.price,
					num: 1, // 默认购买数量为1
					total: (parseFloat(product.price) * 1).toFixed(2),
					userId: uni.getStorageSync('user').id,
					status: '待支付',
					time: new Date().toLocaleString(),
					specials: product.specials,
					unit: product.unit,
					addressId: defaultAddress.id,
					addressInfo: `${defaultAddress.consignee} ${defaultAddress.phone} ${defaultAddress.province}${defaultAddress.city}${defaultAddress.district}${defaultAddress.detail}`
				}
				
				console.log('直接购买数据:', orderData)
				
				try {
					const res = await this.$request.post('/orders/add', orderData)
					console.log('直接购买响应:', res)
					
					if (res && res.code === '200') {
						uni.showToast({ 
							title: '购买成功', 
							icon: 'success',
							duration: 2000
						})
						
						// 跳转到订单页面
						setTimeout(() => {
							uni.switchTab({
								url: '/pages/order/order'
							})
						}, 1500)
					} else {
						uni.showToast({ 
							title: res.msg || '购买失败', 
							icon: 'none' 
						})
					}
				} catch (error) {
					console.error('直接购买失败:', error)
					uni.showToast({ 
						title: '购买失败，请重试', 
						icon: 'none' 
					})
				} finally {
					this.isCreatingOrder = false
				}
			},
			
			changeCategory(categoryId) {
				this.activeCategory = categoryId
				this.pageNum = 1
				this.loadProducts()
			},
			
			// 修复搜索方法
			searchProducts() {
				this.pageNum = 1
				this.hasMore = true
				this.loadProducts()
			},
			
			// 添加输入防抖
			handleSearchInput() {
				// 清除之前的定时器
				if (this.searchTimer) {
					clearTimeout(this.searchTimer)
				}
				
				// 设置新的定时器，500ms后执行搜索
				this.searchTimer = setTimeout(() => {
					this.searchProducts()
				}, 500)
			},
			
			// 清除搜索
			clearSearch() {
				this.searchKeyword = ''
				this.pageNum = 1
				this.loadProducts()
			},
			
			loadMore() {
				if (this.hasMore && !this.loading && !this.searchKeyword) {
					this.pageNum++
					this.loadProducts()
				}
			},
			
			// 查看商品详情 - 只跳转，不购买
			viewProductDetail(productId) {
				if (productId) {
					uni.navigateTo({
						url: `/pages/product/detail?id=${productId}`
					})
				} else {
					uni.showToast({
						title: '商品ID错误',
						icon: 'none'
					})
				}
			},
			
			onImageError(e) {
				console.log('图片加载失败:', e)
			}
		}
	}
</script>

<style>
	.product-page {
		padding: 20rpx;
		background-color: #f8f8f8;
		min-height: 100vh;
	}
	
	.search-bar {
		margin-bottom: 20rpx;
	}
	
	.search-box {
		display: flex;
		align-items: center;
		background: #fff;
		border-radius: 50rpx;
		padding: 20rpx 30rpx;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
		position: relative;
	}
	
	.search-input {
		flex: 1;
		margin-left: 20rpx;
		font-size: 28rpx;
	}
	
	.clear-btn {
		padding: 10rpx;
		margin-left: 10rpx;
	}
	
	.category-scroll {
		white-space: nowrap;
		margin-bottom: 30rpx;
	}
	
	.category-list {
		display: inline-flex;
		padding: 10rpx 0;
	}
	
	.category-item {
		padding: 15rpx 30rpx;
		background: #fff;
		border-radius: 50rpx;
		margin-right: 20rpx;
		font-size: 26rpx;
		color: #666;
		border: 2rpx solid #f0f0f0;
		flex-shrink: 0;
	}
	
	.category-item.active {
		background: #4CD964;
		color: #fff;
		border-color: #4CD964;
	}
	
	.search-tips {
		padding: 20rpx 0;
		font-size: 26rpx;
		color: #666;
		text-align: center;
	}
	
	.product-list {
		display: grid;
		gap: 20rpx;
	}
	
	.product-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 30rpx;
		display: flex;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
	}
	
	.product-image {
		width: 180rpx;
		height: 180rpx;
		border-radius: 16rpx;
		overflow: hidden;
		margin-right: 30rpx;
		flex-shrink: 0;
	}
	
	.product-img {
		width: 100%;
		height: 100%;
		background: #f0f0f0;
	}
	
	.product-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-height: 180rpx;
	}
	
	.product-name {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
	
	.product-desc {
		font-size: 26rpx;
		color: #666;
		line-height: 1.4;
		margin-bottom: 8rpx;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}
	
	.product-specials {
		font-size: 24rpx;
		color: #999;
		line-height: 1.4;
		margin-bottom: 20rpx;
		display: -webkit-box;
		-webkit-line-clamp: 1;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}
	
	.product-bottom {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		margin-top: auto;
	}
	
	.price-section {
		display: flex;
		align-items: baseline;
	}
	
	.product-price {
		font-size: 36rpx;
		font-weight: bold;
		color: #ff4757;
	}
	
	.product-unit {
		font-size: 24rpx;
		color: #999;
		margin-left: 8rpx;
	}
	
	.action-section {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}
	
	.store {
		font-size: 24rpx;
		color: #999;
	}
	
	.buy-btn {
		background: #4CD964;
		color: #fff;
		border: none;
		border-radius: 8rpx;
		padding: 12rpx 20rpx;
		font-size: 24rpx;
		min-width: 120rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}
	
	.load-more {
		text-align: center;
		padding: 40rpx;
		color: #4CD964;
		font-size: 28rpx;
	}
	
	.empty-state {
		text-align: center;
		padding: 100rpx 0;
		color: #ccc;
	}
	
	.empty-text {
		display: block;
		margin-top: 20rpx;
		font-size: 28rpx;
	}
	
	.retry-btn {
		margin-top: 30rpx;
		background: #4CD964;
		color: #fff;
		border: none;
		border-radius: 30rpx;
		padding: 15rpx 40rpx;
		font-size: 26rpx;
	}
	
	.loading {
		text-align: center;
		padding: 40rpx;
		color: #999;
		font-size: 28rpx;
	}
</style>