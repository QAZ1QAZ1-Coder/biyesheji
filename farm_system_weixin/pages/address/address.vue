<template>
	<view class="address-container">
		<!-- 地址列表 -->
		<scroll-view class="address-list" scroll-y :style="{height: scrollViewHeight + 'px'}">
			<!-- 空状态 -->
			<view v-if="addressList.length === 0" class="empty-state">
				<image src="/static/images/product-default.png" class="empty-image" mode="widthFix"></image>
				<text class="empty-text">暂无地址</text>
				<text class="empty-hint">点击下方按钮添加您的第一份地址吧</text>
				<button class="add-first-btn" @click="addNewAddress">添加地址</button>
			</view>

			<!-- 地址列表 -->
			<view v-else>
				<view 
					v-for="(item, index) in addressList" 
					:key="item.id" 
					class="address-item"
				>
					<!-- 地址卡片 -->
					<view class="address-card" @click="handleAddressClick(item)">
						<view class="address-main">
							<view class="address-info">
								<view class="address-header-line">
									<text class="consignee">{{ item.consignee }}</text>
									<text class="phone">{{ item.phone }}</text>
									<view v-if="item.isDefault" class="default-tag">
										<text>默认</text>
									</view>
								</view>
								<view class="address-detail">
									<text class="region">{{ item.province }}{{ item.city }}{{ item.district }}</text>
									<text class="detail">{{ item.detail }}</text>
								</view>
							</view>
						</view>

						<!-- 操作按钮区域 -->
						<view class="address-actions">
							<view class="action-buttons">
								<view v-if="!item.isDefault" class="action-btn-wrapper">
									<button class="action-btn secondary" @click.stop="setDefaultAddress(item)">
										设为默认
									</button>
								</view>
								
								<view class="action-btn-wrapper">
									<button class="action-btn primary" @click.stop="editAddress(item)">
										编辑
									</button>
								</view>
								
								<view class="action-btn-wrapper">
									<button class="action-btn danger" @click.stop="deleteAddress(item, index)">
										删除
									</button>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- 添加地址浮动按钮 -->
		<view class="fab-container" v-if="addressList.length > 0">
			<button class="fab-button" @click="addNewAddress">
				<uni-icons type="plus" size="20" color="#fff"></uni-icons>
				<text class="fab-text">添加新地址</text>
			</button>
		</view>

		<!-- 地址编辑弹窗 -->
		<uni-popup ref="editPopup" type="center" background-color="#fff">
			<view class="edit-popup">
				<view class="popup-header">
					<text class="popup-title">{{ isEditing ? '编辑地址' : '新增地址' }}</text>
				</view>

				<scroll-view class="edit-form" scroll-y>
					<view class="form-container">
						<!-- 收货人 -->
						<view class="form-section">
							<text class="section-label">收货人</text>
							<view class="input-wrapper">
								<input 
									v-model="addressForm.consignee" 
									class="form-input"
									placeholder="请输入收货人姓名"
									placeholder-class="placeholder"
								/>
							</view>
						</view>

						<!-- 手机号 -->
						<view class="form-section">
							<text class="section-label">手机号</text>
							<view class="input-wrapper">
								<input 
									v-model="addressForm.phone" 
									class="form-input"
									placeholder="请输入手机号"
									placeholder-class="placeholder"
									type="number"
									maxlength="11"
								/>
							</view>
						</view>

						<!-- 所在地区 -->
						<view class="form-section">
							<text class="section-label">所在地区</text>
							<view class="region-inputs">
								<view class="region-item">
									<view class="region-input-wrapper">
										<input 
											v-model="addressForm.province" 
											class="form-input region-input"
											placeholder="省份"
											placeholder-class="placeholder"
										/>
									</view>
								</view>
								<view class="region-item">
									<view class="region-input-wrapper">
										<input 
											v-model="addressForm.city" 
											class="form-input region-input"
											placeholder="城市"
											placeholder-class="placeholder"
										/>
									</view>
								</view>
								<view class="region-item">
									<view class="region-input-wrapper">
										<input 
											v-model="addressForm.district" 
											class="form-input region-input"
											placeholder="区县"
											placeholder-class="placeholder"
										/>
									</view>
								</view>
							</view>
						</view>

						<!-- 详细地址 -->
						<view class="form-section">
							<text class="section-label">详细地址</text>
							<view class="input-wrapper">
								<textarea 
									v-model="addressForm.detail" 
									class="form-textarea"
									placeholder="请输入详细地址，如街道、小区、门牌号等"
									placeholder-class="placeholder"
									maxlength="100"
								/>
							</view>
						</view>

						<!-- 设为默认 -->
						<view class="default-setting">
							<text class="default-label">设为默认地址</text>
							<switch :checked="addressForm.isDefault" @change="onDefaultChange" color="#4CD964" />
						</view>
					</view>
				</scroll-view>

				<view class="edit-actions">
					<button class="cancel-btn" @click="closeEditPopup">取消</button>
					<button class="confirm-btn" @click="saveAddress">保存</button>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				addressList: [],
				addressForm: {
					consignee: '',
					phone: '',
					province: '',
					city: '',
					district: '',
					detail: '',
					isDefault: false
				},
				isEditing: false,
				editingAddressId: null,
				scrollViewHeight: 0,
				fromDetail: false  ,// 是否从商品详情页跳转过来
				orderId: '',       // 订单ID（如果从订单详情页跳转）
				fromOrderDetail: false  // 是否从订单详情页跳转过来
			}
		},
		onLoad(options) {
			// 检查是否从商品详情页跳转过来
			if (options.from === 'detail') {
				this.fromDetail = true
				console.log('从商品详情页跳转过来')
			}
			// 新增：检查是否从订单详情页跳转过来
			else if (options.from === 'order_detail') {
				this.fromDetail = true
				this.fromOrderDetail = true
				this.orderId = options.orderId
				console.log('从订单详情页跳转过来，订单ID:', this.orderId)
				
				// 获取存储的订单信息
				const orderInfo = uni.getStorageSync('order_for_address')
				if (orderInfo) {
					console.log('有订单信息:', orderInfo)
				}
			}
			this.loadAddressList()
		},
		onShow() {
			this.calcScrollViewHeight()
		},
		onReady() {
			this.calcScrollViewHeight()
		},
		methods: {
			// 计算scroll-view高度
			calcScrollViewHeight() {
				const systemInfo = uni.getSystemInfoSync()
				const windowHeight = systemInfo.windowHeight
				const safeAreaBottom = systemInfo.safeAreaInsets?.bottom || 0
				const fabHeight = this.addressList.length > 0 ? 120 : 0
				
				this.scrollViewHeight = windowHeight - safeAreaBottom - fabHeight
			},
			
			// 修改地址卡片点击事件方法
			handleAddressClick(address) {
				if (this.fromDetail) {
					// 如果是从详情页跳转过来的，选择地址并返回
					this.selectAddressForDetail(address)
				} else {
					// 正常模式，可以点击编辑或其他操作
					// 这里可以什么都不做，或者跳转到编辑
				}
			},
			
			// 修改选择地址方法
			selectAddressForDetail(address) {
				console.log('为详情页选择地址:', address)
				
				// 将选择的地址存储到本地存储
				uni.setStorageSync('selected_address', address)
				
				// 设置来源页面
				if (this.fromOrderDetail) {
					uni.setStorageSync('from_page', 'order_detail')
					console.log('为订单详情页选择地址，订单ID:', this.orderId)
				} else {
					uni.setStorageSync('from_page', 'product_detail')
				}
				
				// 返回上一页
				uni.navigateBack({
					delta: 1,
					success: () => {
						console.log('返回详情页，已传递地址')
					}
				})
			},
			
			// 加载地址列表
			async loadAddressList() {
				try {
					const user = uni.getStorageSync('user')
					if (!user || !user.id) {
						uni.showToast({
							icon: 'none',
							title: '请先登录'
						})
						setTimeout(() => {
							uni.navigateTo({
								url: '/pages/login/login'
							})
						}, 1500)
						return
					}

					uni.showLoading({
						title: '加载中...'
					})

					const res = await this.$request.get(`/address/selectByUserId/${user.id}`)
					
					uni.hideLoading()
					
					if (res.code === '200') {
						this.addressList = res.data || []
						this.calcScrollViewHeight()
					} else {
						uni.showToast({
							icon: 'none',
							title: '加载失败：' + (res.msg || '未知错误')
						})
						this.addressList = []
					}
				} catch (error) {
					console.error('加载地址列表失败:', error)
					uni.hideLoading()
					uni.showToast({
						icon: 'none',
						title: '网络错误，请重试'
					})
					this.addressList = []
				}
			},
			
			// 添加新地址
			addNewAddress() {
				this.isEditing = false
				this.editingAddressId = null
				this.addressForm = {
					consignee: '',
					phone: '',
					province: '',
					city: '',
					district: '',
					detail: '',
					isDefault: this.addressList.length === 0
				}
				
				this.$refs.editPopup.open()
			},
			
			// 编辑地址
			editAddress(address) {
				this.isEditing = true
				this.editingAddressId = address.id
				this.addressForm = {
					consignee: address.consignee || '',
					phone: address.phone || '',
					province: address.province || '',
					city: address.city || '',
					district: address.district || '',
					detail: address.detail || '',
					isDefault: address.isDefault || false
				}
				
				this.$refs.editPopup.open()
			},
			
			// 默认地址切换
			onDefaultChange(e) {
				this.addressForm.isDefault = e.detail.value
			},
			
			// 保存地址
			async saveAddress() {
				try {
					// 表单验证
					if (!this.addressForm.consignee) {
						uni.showToast({
							icon: 'none',
							title: '请输入收货人姓名'
						})
						return
					}
					
					if (!this.addressForm.phone) {
						uni.showToast({
							icon: 'none',
							title: '请输入手机号'
						})
						return
					}
					
					if (!/^1[3-9]\d{9}$/.test(this.addressForm.phone)) {
						uni.showToast({
							icon: 'none',
							title: '手机号格式不正确'
						})
						return
					}
					
					if (!this.addressForm.province) {
						uni.showToast({
							icon: 'none',
							title: '请输入省份'
						})
						return
					}
					
					if (!this.addressForm.city) {
						uni.showToast({
							icon: 'none',
							title: '请输入城市'
						})
						return
					}
					
					if (!this.addressForm.district) {
						uni.showToast({
							icon: 'none',
							title: '请输入区县'
						})
						return
					}
					
					if (!this.addressForm.detail) {
						uni.showToast({
							icon: 'none',
							title: '请输入详细地址'
						})
						return
					}
					
					const user = uni.getStorageSync('user')
					if (!user || !user.id) {
						uni.showToast({
							icon: 'none',
							title: '请先登录'
						})
						return
					}
					
					// 构建请求数据
					const addressData = {
						...this.addressForm,
						userId: user.id
					}
					
					uni.showLoading({
						title: '保存中...'
					})
					
					let res
					if (this.isEditing) {
						// 更新地址
						addressData.id = this.editingAddressId
						res = await this.$request.put('/address/update', addressData)
					} else {
						// 新增地址
						res = await this.$request.post('/address/add', addressData)
					}
					
					uni.hideLoading()
					
					if (res.code === '200') {
						uni.showToast({
							icon: 'success',
							title: '保存成功'
						})
						this.closeEditPopup()
						// 重新加载地址列表
						await this.loadAddressList()
					} else {
						uni.showToast({
							icon: 'none',
							title: res.msg || '保存失败'
						})
					}
				} catch (error) {
					uni.hideLoading()
					console.error('保存地址失败:', error)
					uni.showToast({
						icon: 'none',
						title: '保存失败，请重试'
					})
				}
			},
			
			// 设置默认地址
			async setDefaultAddress(address) {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({
						icon: 'none',
						title: '请先登录'
					})
					return
				}
				
				uni.showLoading({
					title: '设置中...'
				})
				
				try {
					const res = await this.$request.put(`/address/setDefault/${address.id}?userId=${user.id}`)
					
					uni.hideLoading()
					
					if (res.code === '200') {
						uni.showToast({
							icon: 'success',
							title: '设置成功'
						})
						// 重新加载地址列表
						await this.loadAddressList()
					} else {
						uni.showToast({
							icon: 'none',
							title: res.msg || '设置失败'
						})
					}
				} catch (error) {
					uni.hideLoading()
					console.error('设置默认地址失败:', error)
					uni.showToast({
						icon: 'none',
						title: '网络错误，请重试'
					})
				}
			},
			
			// 删除地址
			deleteAddress(address, index) {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除这个地址吗？',
					success: async (res) => {
						if (res.confirm) {
							uni.showLoading({
								title: '删除中...'
							})
							
							try {
								const result = await this.$request.delete('/address/delete/' + address.id)
								
								uni.hideLoading()
								
								if (result.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '删除成功'
									})
									// 重新加载地址列表
									await this.loadAddressList()
								} else {
									uni.showToast({
										icon: 'none',
										title: result.msg || '删除失败'
									})
								}
							} catch (error) {
								uni.hideLoading()
								console.error('删除地址失败:', error)
								uni.showToast({
									icon: 'none',
									title: '网络错误，请重试'
								})
							}
						}
					}
				})
			},
			
			// 关闭编辑弹窗
			closeEditPopup() {
				this.$refs.editPopup.close()
				this.addressForm = {
					consignee: '',
					phone: '',
					province: '',
					city: '',
					district: '',
					detail: '',
					isDefault: false
				}
			}
		}
	}
</script>

<style scoped>
	/* 样式保持不变，与之前的相同 */
	.address-container {
		background-color: #f5f5f5;
		min-height: 100vh;
		font-size: 14px;
		box-sizing: border-box;
	}
	
	.address-list {
		padding: 20rpx;
		box-sizing: border-box;
	}
	
	.address-item {
		margin-bottom: 20rpx;
	}
	
	.address-card {
		background: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
		box-sizing: border-box;
	}
	
	/* 添加点击效果 */
	.address-card:active {
		opacity: 0.7;
	}
	
	.address-main {
		margin-bottom: 24rpx;
	}
	
	.address-info {
		flex: 1;
	}
	
	.address-header-line {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
		flex-wrap: wrap;
	}
	
	.consignee {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-right: 20rpx;
	}
	
	.phone {
		font-size: 28rpx;
		color: #666;
		margin-right: 20rpx;
	}
	
	.address-detail {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
	}
	
	.region {
		color: #333;
		margin-right: 10rpx;
	}
	
	.detail {
		color: #666;
	}
	
	.default-tag {
		background-color: #4CD964;
		border-radius: 20rpx;
		padding: 6rpx 16rpx;
	}
	
	.default-tag text {
		font-size: 24rpx;
		color: #fff;
	}
	
	.address-actions {
		border-top: 1rpx solid #f0f0f0;
		padding-top: 24rpx;
		box-sizing: border-box;
	}
	
	.action-buttons {
		display: flex;
		justify-content: flex-end;
		gap: 20rpx;
	}
	
	.action-btn-wrapper {
		flex-shrink: 0;
	}
	
	.action-btn {
		border: none;
		border-radius: 30rpx;
		padding: 15rpx 30rpx;
		font-size: 26rpx;
		min-width: 140rpx;
		height: 64rpx;
		line-height: 34rpx;
		transition: all 0.3s;
		box-sizing: border-box;
	}
	
	.action-btn.primary {
		background: #4CD964;
		color: #fff;
	}
	
	.action-btn.primary:active {
		background: #3ac253;
	}
	
	.action-btn.secondary {
		background: #fff;
		color: #666;
		border: 1rpx solid #e0e0e0;
	}
	
	.action-btn.secondary:active {
		background: #f8f8f8;
	}
	
	.action-btn.danger {
		background: #fff;
		color: #ff4757;
		border: 1rpx solid #ff4757;
	}
	
	.action-btn.danger:active {
		background: #fff0f0;
	}
	
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 120rpx 60rpx;
		box-sizing: border-box;
	}
	
	.empty-image {
		width: 240rpx;
		height: 240rpx;
		margin-bottom: 40rpx;
	}
	
	.empty-text {
		font-size: 32rpx;
		color: #999;
		margin-bottom: 16rpx;
	}
	
	.empty-hint {
		font-size: 26rpx;
		color: #ccc;
		margin-bottom: 60rpx;
	}
	
	.add-first-btn {
		background-color: #4CD964;
		color: #fff;
		border: none;
		border-radius: 30rpx;
		padding: 20rpx 60rpx;
		font-size: 28rpx;
		width: auto;
		height: 80rpx;
		line-height: 40rpx;
		box-sizing: border-box;
	}
	
	.add-first-btn:active {
		background-color: #3ac253;
	}
	
	.fab-container {
		position: fixed;
		bottom: 40rpx;
		left: 0;
		right: 0;
		display: flex;
		justify-content: center;
		padding: 0 40rpx;
		box-sizing: border-box;
	}
	
	.fab-button {
		background-color: #4CD964;
		color: #fff;
		border: none;
		border-radius: 30rpx;
		padding: 20rpx 40rpx;
		font-size: 28rpx;
		box-shadow: 0 4rpx 12rpx rgba(76, 217, 100, 0.3);
		display: flex;
		align-items: center;
		justify-content: center;
		height: 80rpx;
		line-height: 40rpx;
		box-sizing: border-box;
	}
	
	.fab-text {
		margin-left: 12rpx;
	}
	
	.fab-button:active {
		background-color: #3ac253;
		transform: scale(0.98);
	}
	
	.edit-popup {
		background: white;
		border-radius: 24rpx;
		width: 670rpx !important;
		max-height: 85vh;
		overflow: hidden;
		position: relative;
		box-sizing: border-box;
		margin: 0 auto;
		left: 0;
		right: 0;
	}
	
	.popup-header {
		padding: 40rpx 40rpx 30rpx;
		text-align: center;
		border-bottom: 1rpx solid #f8f8f8;
		box-sizing: border-box;
		width: 100%;
	}
	
	.popup-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
	}
	
	.edit-form {
		max-height: 65vh;
		padding: 0 40rpx;
		box-sizing: border-box;
		width: 100%;
	}
	
	.form-container {
		padding: 30rpx 0;
		box-sizing: border-box;
		width: 100%;
	}
	
	.form-section {
		margin-bottom: 40rpx;
		width: 100%;
		box-sizing: border-box;
	}
	
	.section-label {
		display: block;
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
		margin-bottom: 20rpx;
		width: 100%;
		text-align: left;
	}
	
	.input-wrapper {
		background: #f8f9fa;
		border-radius: 16rpx;
		padding: 0 24rpx;
		transition: all 0.3s;
		width: 100%;
		box-sizing: border-box;
	}
	
	.input-wrapper:active {
		background: #f0f0f0;
	}
	
	.form-input {
		width: 100%;
		height: 90rpx;
		font-size: 30rpx;
		color: #333;
		background: transparent;
		border: none;
		outline: none;
		box-sizing: border-box;
	}
	
	.placeholder {
		font-size: 30rpx;
		color: #999;
	}
	
	.region-inputs {
		display: flex;
		justify-content: space-between;
		gap: 16rpx;
		width: 100%;
		box-sizing: border-box;
	}
	
	.region-item {
		flex: 1;
		min-width: 0;
		box-sizing: border-box;
	}
	
	.region-input-wrapper {
		background: #f8f9fa;
		border-radius: 16rpx;
		padding: 0 16rpx;
		transition: all 0.3s;
		width: 100%;
		height: 90rpx;
		display: flex;
		align-items: center;
		box-sizing: border-box;
	}
	
	.region-input {
		height: 100%;
		font-size: 30rpx;
		color: #333;
		width: 100%;
		text-align: center;
		background: transparent;
		border: none;
		outline: none;
		box-sizing: border-box;
	}
	
	.form-textarea {
		width: 100%;
		height: 160rpx;
		font-size: 30rpx;
		color: #333;
		background: transparent;
		border: none;
		outline: none;
		padding: 24rpx 0;
		line-height: 1.5;
		resize: none;
		box-sizing: border-box;
	}
	
	.default-setting {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx 0 20rpx;
		border-top: 1rpx solid #f8f8f8;
		margin-top: 10rpx;
		width: 100%;
		box-sizing: border-box;
	}
	
	.default-label {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
	}
	
	.edit-actions {
		display: flex;
		gap: 24rpx;
		padding: 30rpx 40rpx 40rpx;
		border-top: 1rpx solid #f8f8f8;
		background: #f8f9fa;
		width: 100%;
		box-sizing: border-box;
	}
	
	.cancel-btn,
	.confirm-btn {
		flex: 1;
		border-radius: 30rpx;
		padding: 24rpx;
		font-size: 32rpx;
		font-weight: 500;
		height: 96rpx;
		line-height: 48rpx;
		transition: all 0.3s;
		box-sizing: border-box;
	}
	
	.cancel-btn {
		background: #fff;
		color: #666;
		border: 1rpx solid #e0e0e0;
	}
	
	.cancel-btn:active {
		background: #f8f8f8;
		transform: scale(0.98);
	}
	
	.confirm-btn {
		background: #4CD964;
		color: white;
		border: none;
	}
	
	.confirm-btn:active {
		background: #3ac253;
		transform: scale(0.98);
	}
</style>