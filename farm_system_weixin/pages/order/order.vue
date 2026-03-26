<template>
	<view style="padding: 20rpx;">
		<!-- 状态筛选 -->
		<scroll-view scroll-x="true" style="white-space: nowrap; margin-bottom: 20rpx; padding: 10rpx 0;">
			<view style="display: inline-flex;">
				<view 
					v-for="status in statusList" 
					:key="status.value"
					:style="{
						padding: '12rpx 24rpx',
						margin: '0 10rpx',
						borderRadius: '8rpx',
						fontSize: '26rpx',
						backgroundColor: activeStatus === status.value ? '#4CD964' : '#f5f5f5',
						color: activeStatus === status.value ? '#fff' : '#666',
						border: activeStatus === status.value ? '1rpx solid #4CD964' : '1rpx solid #f0f0f0'
					}"
					@click="changeStatus(status.value)"
				>
					{{ status.label }}
				</view>
			</view>
		</scroll-view>
		
		<!-- 订单列表 -->
		<view>
			<view 
				v-for="item in orderList" 
				:key="item.id" 
				@click="goToOrderDetail(item.id)"
				:style="{
					marginBottom: '20rpx',
					padding: '30rpx',
					background: '#fff',
					borderRadius: '12rpx',
					boxShadow: '0 2rpx 10rpx rgba(0, 0, 0, 0.05)',
					cursor: 'pointer'
				}"
			>
				<!-- 订单头部：订单号和状态在一行 -->
				<view style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; padding-bottom: 20rpx; border-bottom: 1rpx solid #f0f0f0">
					<!-- 订单号 -->
					<view style="flex: 1; overflow: hidden; margin-right: 20rpx;">
						<text style="color: #666; font-size: 26rpx;">
							订单号：{{ item.orderNo }}
						</text>
					</view>
					<!-- 订单状态 -->
					<view style="flex-shrink: 0;">
						<text v-if="item.status === '待支付'" style="color: #e6a23c; font-size: 24rpx; font-weight: bold;">
							{{ item.status }}
						</text>
						<text v-else-if="item.status === '待发货'" style="color: #409eff; font-size: 24rpx; font-weight: bold;">
							{{ item.status }}
						</text>
						<text v-else-if="item.status === '待收货'" style="color: #409eff; font-size: 24rpx; font-weight: bold;">
							{{ item.status }}
						</text>
						<text v-else-if="item.status === '已完成'" style="color: #67c23a; font-size: 24rpx; font-weight: bold;">
							{{ item.status }}
						</text>
						<text v-else-if="item.status === '已取消'" style="color: #f56c6c; font-size: 24rpx; font-weight: bold;">
							{{ item.status }}
						</text>
					</view>
				</view>
				
				<!-- 商品信息 -->
				<view style="display: flex; align-items: center; margin-bottom: 20rpx;">
					<image 
						:src="item.goodsImg || '/static/images/product-default.png'" 
						:style="{
							width: '140rpx',
							height: '140rpx',
							borderRadius: '8rpx',
							marginRight: '20rpx',
							background: '#f5f5f5'
						}"
						mode="aspectFill"
					></image>
					<view style="flex: 1;">
						<text style="font-size: 30rpx; font-weight: bold; color: #333; line-height: 1.4; display: block; margin-bottom: 8rpx;">
							{{ item.goodsName }}
						</text>
						<text v-if="item.specials" style="font-size: 24rpx; color: #999; line-height: 1.3; display: block; margin-bottom: 12rpx;">
							{{ item.specials }}
						</text>
						<view style="display: flex; align-items: baseline;">
							<text style="font-size: 32rpx; font-weight: bold; color: #ff4757;">
								¥{{ item.price }}
							</text>
							<text v-if="item.unit" style="font-size: 24rpx; color: #999; margin: 0 8rpx;">
								/{{ item.unit }}
							</text>
							<text style="font-size: 26rpx; color: #666; margin-left: auto;">
								×{{ item.num }}
							</text>
						</view>
					</view>
				</view>
				
				<!-- 订单统计和时间 -->
				<view style="display: flex; justify-content: space-between; align-items: center; padding-top: 20rpx; border-top: 1rpx solid #f0f0f0;">
					<view style="flex: 1;">
						<text style="font-size: 24rpx; color: #999;">
							{{ formatTime(item.createTime) }}
						</text>
					</view>
					<view style="flex: 1; text-align: right;">
						<text style="font-size: 26rpx; color: #666; margin-right: 10rpx;">
							共{{ item.num }}件商品 合计: 
						</text>
						<text style="font-size: 32rpx; font-weight: bold; color: #ff4757;">
							¥{{ item.total }}
						</text>
					</view>
				</view>
				
				<!-- 操作按钮 -->
				<view style="display: flex; justify-content: flex-end; gap: 20rpx; margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #f0f0f0;">
					<!-- 待支付 -->
					<view v-if="item.status === '待支付'" style="display: flex; gap: 20rpx;">
						<view 
							@click.stop="handleCancel(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#fff',
								color: '#666',
								fontSize: '26rpx',
								borderRadius: '30rpx',
								border: '1rpx solid #e0e0e0'
							}"
						>
							取消订单
						</view>
						<view 
							@click.stop="handlePay(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#4CD964',
								color: '#fff',
								fontSize: '26rpx',
								borderRadius: '30rpx'
							}"
						>
							立即支付
						</view>
					</view>
					
					<!-- 待发货 -->
					<view v-if="item.status === '待发货'" style="display: flex; gap: 20rpx;">
						<view 
							@click.stop="contactService"
							:style="{
								padding: '12rpx 32rpx',
								background: '#fff',
								color: '#666',
								fontSize: '26rpx',
								borderRadius: '30rpx',
								border: '1rpx solid #e0e0e0'
							}"
						>
							联系客服
						</view>
						<view 
							@click.stop="handleBuyAgain(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#4CD964',
								color: '#fff',
								fontSize: '26rpx',
								borderRadius: '30rpx'
							}"
						>
							再次购买
						</view>
					</view>
					
					<!-- 待收货 -->
					<view v-if="item.status === '待收货'" style="display: flex; gap: 20rpx;">
						<view 
							@click.stop="contactService"
							:style="{
								padding: '12rpx 32rpx',
								background: '#fff',
								color: '#666',
								fontSize: '26rpx',
								borderRadius: '30rpx',
								border: '1rpx solid #e0e0e0'
							}"
						>
							联系客服
						</view>
						<view 
							@click.stop="handleConfirmReceipt(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#4CD964',
								color: '#fff',
								fontSize: '26rpx',
								borderRadius: '30rpx'
							}"
						>
							确认收货
						</view>
					</view>
					
					<!-- 已完成 -->
					<view v-if="item.status === '已完成'" style="display: flex; gap: 20rpx;">
						<view 
							@click.stop="handleBuyAgain(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#fff',
								color: '#666',
								fontSize: '26rpx',
								borderRadius: '30rpx',
								border: '1rpx solid #e0e0e0'
							}"
						>
							再次购买
						</view>
						<view 
							@click.stop="contactService"
							:style="{
								padding: '12rpx 32rpx',
								background: '#4CD964',
								color: '#fff',
								fontSize: '26rpx',
								borderRadius: '30rpx'
							}"
						>
							联系客服
						</view>
					</view>
					
					<!-- 已取消 -->
					<view v-if="item.status === '已取消'" style="display: flex; gap: 20rpx;">
						<view 
							@click.stop="handleDelete(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#fff',
								color: '#666',
								fontSize: '26rpx',
								borderRadius: '30rpx',
								border: '1rpx solid #e0e0e0'
							}"
						>
							删除订单
						</view>
						<view 
							@click.stop="handleBuyAgain(item)"
							:style="{
								padding: '12rpx 32rpx',
								background: '#4CD964',
								color: '#fff',
								fontSize: '26rpx',
								borderRadius: '30rpx'
							}"
						>
							再次购买
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 空状态 -->
		<view v-if="orderList.length === 0 && !loading" style="text-align: center; padding: 120rpx 30rpx;">
			<image src="/static/images/product-default.png" style="width: 300rpx; margin-bottom: 30rpx;" mode="widthFix"></image>
			<text style="display: block; margin-top: 20rpx; font-size: 28rpx; color: #999; margin-bottom: 30rpx;">
				暂无订单
			</text>
			<view 
				@click.stop="goShopping"
				:style="{
					display: 'inline-block',
					background: '#4CD964',
					color: '#fff',
					borderRadius: '30rpx',
					padding: '18rpx 50rpx',
					fontSize: '28rpx'
				}"
			>
				去购物
			</view>
		</view>
		
		<!-- 加载中 -->
		<view v-if="loading && orderList.length === 0" style="text-align: center; padding: 80rpx;">
			<view style="width: 60rpx; height: 60rpx; border: 4rpx solid #f3f3f3; border-top: 4rpx solid #4CD964; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 20rpx;"></view>
			<text style="color: #999; font-size: 28rpx;">加载中...</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				statusList: [
					{ label: '全部', value: '' },
					{ label: '待支付', value: '待支付' },
					{ label: '待发货', value: '待发货' },
					{ label: '待收货', value: '待收货' },
					{ label: '已完成', value: '已完成' },
					{ label: '已取消', value: '已取消' }
				],
				orderList: [],
				activeStatus: '',
				loading: false,
				orderId: 0
			}
		},
		onShow() {
			this.load()
		},
		onPullDownRefresh() {
			this.load().finally(() => {
				uni.stopPullDownRefresh()
			})
		},
		methods: {
			load() {
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
				
				let params = {
					userId: user.id,
					pageNum: 1,
					pageSize: 10
				}
				
				if (this.activeStatus) {
					params.status = this.activeStatus
				}
				
				this.$request.get('/orders/selectPage', params).then(res => {
					if (res && res.code === '200') {
						this.orderList = res.data?.list || []
					} else {
						uni.showToast({
							icon: 'none',
							title: res?.msg || '加载失败'
						})
						this.orderList = []
					}
				}).catch(err => {
					console.error('加载订单失败:', err)
					uni.showToast({
						icon: 'none',
						title: '网络错误'
					})
					this.orderList = []
				}).finally(() => {
					this.loading = false
				})
			},
			
			changeStatus(status) {
				this.activeStatus = status
				this.load()
			},
			
			handlePay(order) {
				if (order.status !== '待支付') {
					uni.showToast({
						title: '该订单无法支付',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '确认支付',
					content: `确定要支付订单 ${order.orderNo} 吗？\n金额：¥${order.total}`,
					success: (res) => {
						if (res.confirm) {
							const updateData = {
								...order,
								status: '待发货',
								id: order.id
							}
							
							this.$request.put('/orders/update', updateData).then(res => {
								if (res && res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '支付成功'
									})
									this.load()
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
			
			handleCancel(order) {
				if (order.status !== '待支付') {
					uni.showToast({
						title: '该订单无法取消',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '确认取消',
					content: `确定要取消订单 ${order.orderNo} 吗？`,
					success: (res) => {
						if (res.confirm) {
							const updateData = {
								...order,
								status: '已取消',
								id: order.id
							}
							
							this.$request.put('/orders/update', updateData).then(res => {
								if (res && res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '订单已取消'
									})
									this.load()
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
			
			handleConfirmReceipt(order) {
				if (order.status !== '待收货') {
					uni.showToast({
						title: '该订单无法确认收货',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '确认收货',
					content: `确定已收到商品吗？\n订单号：${order.orderNo}`,
					success: (res) => {
						if (res.confirm) {
							const updateData = {
								...order,
								status: '已完成',
								id: order.id
							}
							
							this.$request.put('/orders/update', updateData).then(res => {
								if (res && res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '确认收货成功'
									})
									this.load()
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
			
			handleDelete(order) {
				if (order.status !== '已取消') {
					uni.showToast({
						title: '只能删除已取消的订单',
						icon: 'none'
					})
					return
				}
				
				if (!order.id) {
					uni.showToast({
						title: '订单ID不能为空',
						icon: 'none'
					})
					return
				}
				
				uni.showModal({
					title: '删除订单',
					content: `确定要永久删除订单 ${order.orderNo} 吗？`,
					success: (res) => {
						if (res.confirm) {
							this.$request.delete('/orders/delete/' + order.id).then(res => {
								if (res.code === '200') {
									uni.showToast({
										icon: 'success',
										title: '删除成功'
									})
									this.load()
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
			
			handleBuyAgain(order) {
				if (!order.goodsId) {
					uni.showToast({
						title: '商品信息错误',
						icon: 'none'
					})
					return
				}
				
				uni.navigateTo({
					url: `/pages/product/detail?id=${order.goodsId}`
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
			
			goShopping() {
				uni.switchTab({
					url: '/pages/index/index'
				})
			},
			
			formatTime(time) {
				if (!time) return ''
				if (typeof time === 'number') {
					return new Date(time).toLocaleString()
				}
				return time
			},
			// 新增：跳转到订单详情
				goToOrderDetail(orderId) {
					if (!orderId) {
						uni.showToast({
							title: '订单ID不能为空',
							icon: 'none'
						})
						return
					}
					
					uni.navigateTo({
						url: `/pages/order/detail?id=${orderId}`
					})
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