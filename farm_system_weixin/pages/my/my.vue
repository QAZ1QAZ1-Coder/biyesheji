<template>
	<view class="container">
		<!-- 用户信息卡片（可点击编辑） -->
		<view class="user-card" @click="goUserInfo">
			<view class="card-content">
				<view class="user-info-section">
					<view class="user-avatar">
						<image :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
					</view>
					<view class="user-info">
						<text class="username">{{ userInfo.name || userInfo.username || '未登录' }}</text>
						<text class="phone" v-if="userInfo.phone">{{ userInfo.phone }}</text>
						<text class="welcome">欢迎使用助农系统</text>
					</view>
				</view>
				<view class="edit-icon">
					<uni-icons type="arrowright" size="20" color="#999"></uni-icons>
				</view>
			</view>
		</view>

		<!-- 功能菜单 -->
		<view class="menu-section">
			<!-- 地址管理 -->
			<view class="menu-item" @click="goAddressManage">
				<view class="menu-left">
					<view class="menu-icon">
						<uni-icons type="location" size="20" color="#4CD964"></uni-icons>
					</view>
					<text class="menu-text">地址管理</text>
				</view>
				<uni-icons type="arrowright" size="16" color="#999"></uni-icons>
			</view>

			<!-- 分割线 -->
			<view class="divider"></view>

			<!-- 我的订单 -->
			<view class="menu-item" @click="goOrders">
				<view class="menu-left">
					<view class="menu-icon">
						<uni-icons type="cart" size="20" color="#4CD964"></uni-icons>
					</view>
					<text class="menu-text">我的订单</text>
				</view>
				<uni-icons type="arrowright" size="16" color="#999"></uni-icons>
			</view>

			<!-- 分割线 -->
			<view class="divider"></view>

			<!-- 账户安全 -->
			<view class="menu-item" @click="goSecurity">
				<view class="menu-left">
					<view class="menu-icon">
						<uni-icons type="locked" size="20" color="#4CD964"></uni-icons>
					</view>
					<text class="menu-text">账户安全</text>
				</view>
				<uni-icons type="arrowright" size="16" color="#999"></uni-icons>
			</view>

			<!-- 分割线 -->
			<view class="divider"></view>

			<!-- 关于我们 -->
			<view class="menu-item" @click="goAbout">
				<view class="menu-left">
					<view class="menu-icon">
						<uni-icons type="info" size="20" color="#4CD964"></uni-icons>
					</view>
					<text class="menu-text">关于我们</text>
				</view>
				<uni-icons type="arrowright" size="16" color="#999"></uni-icons>
			</view>
		</view>

		<!-- 退出登录 -->
		<view class="logout-section">
			<button class="logout-btn" @click="logout">退出登录</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userInfo: {}
			}
		},
		onShow() {
			this.loadUserInfo()
		},
		methods: {
			// 加载用户信息
			loadUserInfo() {
				const user = uni.getStorageSync('user');
				if (user) {
					// 处理头像URL
					let avatar = user.avatar || '';
					const baseUrl = 'http://192.168.88.1:9090';
					
					// 如果头像不是完整URL，拼接基础URL
					if (avatar) {
						if (!avatar.startsWith('http://') && !avatar.startsWith('https://')) {
							if (avatar.startsWith('/')) {
								avatar = baseUrl + avatar;
							} else {
								avatar = baseUrl + '/' + avatar;
							}
						}
					} else {
						avatar = '/static/default-avatar.png';
					}
					
					this.userInfo = {
						...user,
						avatar: avatar
					}
				} else {
					console.log('用户未登录')
				}
			},

			// 跳转到其他页面
			goUserInfo() {
				uni.navigateTo({
					url: '/pages/my/mydetil'
				})
			},

			goAddressManage() {
				uni.navigateTo({
					url: '/pages/address/address'
				})
			},

			goOrders() {
				uni.switchTab({
					url: '/pages/order/order'
				})
			},

			goSecurity() {
				uni.navigateTo({
					url: '/pages/user/security'
				})
			},

			goAbout() {
				uni.navigateTo({
					url: '/pages/about/about'
				})
			},

			// 退出登录
			logout() {
				uni.showModal({
					title: '确认退出',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							uni.removeStorageSync('user')
							uni.reLaunch({
								url: '/pages/login/login'
							})
						}
					}
				})
			}
		}
	}
</script>

<style scoped>
	.container {
		min-height: 100vh;
		background-color: #f8f9fa;
		padding: 20rpx;
	}

	/* 用户卡片样式 */
	.user-card {
		background-color: #ffffff;
		border-radius: 16rpx;
		margin-bottom: 24rpx;
		padding: 32rpx 24rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}

	.card-content {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.user-info-section {
		display: flex;
		align-items: center;
		flex: 1;
	}

	.user-avatar {
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		overflow: hidden;
		margin-right: 24rpx;
		background-color: #f0f0f0;
	}

	.user-avatar image {
		width: 100%;
		height: 100%;
	}

	.user-info {
		flex: 1;
	}

	.username {
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
		display: block;
		margin-bottom: 8rpx;
	}

	.phone {
		font-size: 26rpx;
		color: #666666;
		display: block;
		margin-bottom: 8rpx;
	}

	.welcome {
		font-size: 24rpx;
		color: #999999;
	}

	.edit-icon {
		color: #999;
	}

	/* 功能菜单样式 - 降低高度 */
	.menu-section {
		background: white;
		border-radius: 16rpx;
		overflow: hidden;
		margin-bottom: 30rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}

	.menu-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 24rpx 20rpx;
		height: 44px;
		background-color: #ffffff;
	}

	.menu-left {
		display: flex;
		align-items: center;
	}

	.menu-icon {
		margin-right: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		width: 40rpx;
	}

	.menu-text {
		font-size: 30rpx;
		color: #333333;
	}

	.divider {
		height: 1rpx;
		background-color: #f0f0f0;
		margin-left: 64rpx; /* icon宽度 + margin-right */
		margin-right: 20rpx;
	}

	/* 退出登录按钮 - 调整大小 */
	.logout-section {
		padding: 20rpx 0;
	}

	.logout-btn {
		background-color: #ff4757;
		color: white;
		border: none;
		border-radius: 30rpx;
		padding: 20rpx;
		font-size: 28rpx;
		font-weight: 500;
		height: 90rpx;
		line-height: 50rpx;
		transition: all 0.3s;
	}

	.logout-btn:active {
		background-color: #e84151;
		transform: scale(0.98);
	}
</style>