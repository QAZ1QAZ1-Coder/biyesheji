<template>
	<view class="container">
		<!-- 轮播图 -->
		<view class="banner-section">
			<swiper class="banner-swiper" :autoplay="true" :interval="3000" :duration="500" circular>
				<swiper-item v-for="(banner, index) in banners" :key="index">
					<image 
						:src="banner.image" 
						mode="aspectFill" 
						class="banner-image"
						@error="onBannerImageError"
					></image>
				</swiper-item>
			</swiper>
		</view>
		
		<!-- 公告栏（仿跑腿系统样式） -->
		<view class="notice-section" v-if="notices.length > 0">
			<uni-notice-bar 
				show-icon 
				:single="true"
				:text="currentNoticeContent"
			></uni-notice-bar>
		</view>
		
		<!-- 热门推荐 -->
		<view class="section">
			<view class="section-title">
				<text class="title">热门推荐</text>
				<text class="subtitle">源于乡间鲜在舌尖</text>
			</view>
			<view class="product-list" v-if="hotProducts.length > 0">
				<view 
					class="product-card" 
					v-for="product in hotProducts" 
					:key="product.id"
				>
					<view class="product-image" @click="viewProductDetail(product.id)">
						<image 
							:src="product.img || '/static/images/product-default.png'" 
							mode="aspectFill"
							class="product-img"
							@error="onProductImageError"
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
			<view class="empty-state" v-else>
				<text>暂无商品数据</text>
			</view>
		</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// 固定推荐的商品ID列表 - 参考HomeActivity.java
				featuredProductIds: [8, 40, 65, 85, 146, 161],
				hotProducts: [],
				notices: [], // 公告列表
				currentNoticeContent: '', // 当前显示的公告内容
				noticeInterval: null, // 公告轮播定时器
				currentNoticeIndex: 0, // 当前公告索引
				banners: [
					{ image: '/static/images/banner1.png' },
					{ image: '/static/images/banner2.png' },
					{ image: '/static/images/banner3.png' }
				],
				hasDefaultAddress: false,
				isCreatingOrder: false,
				defaultAddress: null
			}
		},
		onLoad() {
			this.loadHomeData()
			this.checkDefaultAddress()
		},
		onShow() {
			this.checkDefaultAddress()
			// 页面显示时启动公告轮播
			this.startNoticeRotation()
		},
		onHide() {
			// 页面隐藏时清除定时器
			this.clearNoticeRotation()
		},
		onUnload() {
			// 页面卸载时清除定时器
			this.clearNoticeRotation()
		},
		onPullDownRefresh() {
			this.loadHomeData().finally(() => {
				uni.stopPullDownRefresh()
			})
		},
		methods: {
			// 启动公告轮播
			startNoticeRotation() {
				if (this.notices.length <= 1) return
				
				this.clearNoticeRotation()
				
				this.noticeInterval = setInterval(() => {
					this.currentNoticeIndex++
					if (this.currentNoticeIndex >= this.notices.length) {
						this.currentNoticeIndex = 0
					}
					this.currentNoticeContent = this.notices[this.currentNoticeIndex].title
				}, 5000) // 5秒切换一次
			},
			
			// 清除公告轮播定时器
			clearNoticeRotation() {
				if (this.noticeInterval) {
					clearInterval(this.noticeInterval)
					this.noticeInterval = null
				}
			},
			
			async checkDefaultAddress() {
				const user = uni.getStorageSync('user') || {}
				if (!user.id) {
					this.hasDefaultAddress = false
					this.defaultAddress = null
					return
				}
				
				try {
					const res = await this.$request.get(`/address/selectByUserId/${user.id}`)
					
					if (res && res.code === '200') {
						const addressList = res.data || []
						const defaultAddress = addressList.find(item => item.isDefault)
						
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
						
						console.log('首页检查默认地址结果:', {
							userId: user.id,
							addressListLength: addressList.length,
							hasDefault: this.hasDefaultAddress,
							defaultAddress: this.defaultAddress
						})
					} else {
						this.defaultAddress = null
						this.hasDefaultAddress = false
					}
				} catch (error) {
					console.error('首页检查地址失败:', error)
					this.defaultAddress = null
					this.hasDefaultAddress = false
				}
			},
			
			async loadHomeData() {
				try {
					console.log('开始加载首页数据...')
					
					// 1. 先加载所有商品
					const goodsRes = await this.$request.get('/goods/selectAll')
					
					// 2. 加载公告
					const noticeRes = await this.$request.get('/notice/selectAll')
					
					console.log('商品数据响应:', goodsRes)
					console.log('公告数据响应:', noticeRes)
					
					if (goodsRes && goodsRes.code === '200' && goodsRes.data) {
						const allGoods = goodsRes.data || []
						
						// 筛选固定ID的商品 - 参考HomeActivity.java
						const featuredGoodsList = []
						for (const goods of allGoods) {
							for (const featuredId of this.featuredProductIds) {
								if (goods.id && goods.id === featuredId) {
									featuredGoodsList.push(goods)
									break
								}
							}
							if (featuredGoodsList.length >= 6) {
								break
							}
						}
						
						// 处理商品数据格式
						this.hotProducts = featuredGoodsList.map(product => ({
							id: product.id,
							name: product.name,
							price: product.price,
							img: product.img ? product.img.replace('localhost', '192.168.88.1') : '',
							descr: product.description || product.descr,
							specials: product.tags || product.specials,
							unit: product.unit,
							store: product.stock || product.store
						}))
						
						console.log('固定推荐商品数据:', this.hotProducts)
					} else {
						console.log('商品API返回失败')
						this.hotProducts = []
					}
					
					// 处理公告数据
					if (noticeRes && noticeRes.code === '200' && noticeRes.data) {
						this.notices = noticeRes.data || []
						console.log('公告数据:', this.notices)
						
						// 设置初始公告内容
						if (this.notices.length > 0) {
							this.currentNoticeContent = this.notices[0].title
							this.currentNoticeIndex = 0
							
							// 启动公告轮播
							this.startNoticeRotation()
						}
					} else {
						console.log('公告API返回失败')
						this.notices = []
					}
					
				} catch (error) {
					console.error('加载首页数据失败:', error)
					uni.showToast({
						title: '加载失败，请检查网络',
						icon: 'none'
					})
				}
			},
			
			// 立即购买
			async buyNow(product) {
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
				
				if (!this.hasDefaultAddress || !this.defaultAddress) {
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
					num: 1,
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
			
			// 查看商品详情
			viewProductDetail(productId) {
				if (productId) {
					console.log('跳转到商品详情，ID:', productId)
					uni.navigateTo({
						url: `/pages/product/detail?id=${productId}`
					}).then(() => {
						console.log('跳转成功')
					}).catch(err => {
						console.error('跳转失败:', err)
						uni.showToast({
							title: '商品详情页暂不可用',
							icon: 'none'
						})
					})
				} else {
					uni.showToast({
						title: '商品信息错误',
						icon: 'none'
					})
				}
			},
			
			onBannerImageError(e) {
				console.log('轮播图加载失败:', e)
			},
			
			onProductImageError(e) {
				console.log('商品图片加载失败:', e)
			}
		}
	}
</script>

<style>
	.container {
		padding: 20rpx;
		background-color: #f8f8f8;
		min-height: 100vh;
	}
	
	/* 轮播图样式 */
	.banner-section {
		height: 300rpx;
		margin-bottom: 20rpx;
		border-radius: 16rpx;
		overflow: hidden;
	}
	
	.banner-swiper {
		height: 100%;
	}
	
	.banner-image {
		width: 100%;
		height: 100%;
	}
	
	/* 公告栏样式（仿跑腿系统） */
	.notice-section {
		margin-bottom: 20rpx;
		background: #fff;
		border-radius: 12rpx;
		padding: 0 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
	}
	
	.section {
		margin-bottom: 40rpx;
		background: #fff;
		padding: 30rpx;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
	}
	
	.section-title {
		margin-bottom: 30rpx;
	}
	
	.section-title .title {
		font-size: 32rpx;
		font-weight: bold;
		display: block;
		margin-bottom: 8rpx;
	}
	
	.section-title .subtitle {
		font-size: 24rpx;
		color: #666;
	}
	
	/* 商品列表样式 */
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
	
	.empty-state {
		text-align: center;
		padding: 60rpx 0;
		color: #999;
		font-size: 28rpx;
	}
</style>