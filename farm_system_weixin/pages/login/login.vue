<template>
	<view style="padding: 20rpx;">
		<view class="box" style="padding: 60rpx 30rpx;">
			<view style="font-size: 40rpx; font-weight: bold; text-align: center; margin-bottom: 20rpx;">助农·农产品销售系统</view>
			<view style="font-size: 32rpx; text-align: center; color: #666; margin-bottom: 60rpx;">欢迎登录</view>
			
			<uni-forms ref="formRef" :modelValue="form" :rules="rules" validateTrigger="blur">
				<uni-forms-item name="username" required>
					<uni-easyinput prefixIcon="person" v-model="form.username" placeholder="请输入账号"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item name="password" required>
					<uni-easyinput type="password" prefixIcon="locked" v-model="form.password" placeholder="请输入密码"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item>
					<button @click="login" type="primary" style="width: 100%; height: 80rpx; line-height: 80rpx; background-color: #4CD964; border-color: #4CD964; border-radius: 40rpx; margin-top: 40rpx;">登录</button>
				</uni-forms-item>
			</uni-forms>
		</view>
		
		<view style="text-align: center; margin-top: 40rpx;">
			还没有账号？请<navigator url="/pages/register/register" style="display: inline; margin-left: 5rpx; color: #4CD964;">注册</navigator>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				form: { 
					username: '',
					password: '',
					role: 'USER'  // 参考 Android 端固定角色
				},
				rules: {
					username: {
						rules: [
							{
								required: true,
								errorMessage: '请输入账号',
							},
							{
								minLength: 1,
								maxLength: 20,
								errorMessage: '账号长度在1-20个字符之间',
							}
						],
					},
					password: {
						rules: [
							{
								required: true,
								errorMessage: '请输入密码',
							},
							{
								minLength: 3,
								errorMessage: '密码长度不能少于3位',
							}
						],
					},
				},
				baseUrl: 'http://192.168.88.1:9090'
			}
		},
		methods: {
			login() {
				this.$refs.formRef.validate().then(res => {
					// 显示加载中
					uni.showLoading({
						title: '登录中...',
						mask: true
					})
					
					// 使用 uni.request 直接调用 API，参考 Android 端的结构
					uni.request({
						url: `${this.baseUrl}/login`,
						method: 'POST',
						header: {
							'Content-Type': 'application/json'
						},
						data: this.form,
						success: (res) => {
							uni.hideLoading()
							console.log('登录响应:', res)
							
							if (res.data.code === '200' || res.data.code === 200) { // 登录成功
								// 参考 Android 端，获取完整的登录返回数据
								const loginData = res.data.data
								
								if (!loginData) {
									uni.showToast({
										icon: 'none',
										title: '登录返回数据异常'
									})
									return
								}
								
								// 构建完整的用户对象，参考 Android 端的 LoginAccount 和 User
								const user = {
									id: loginData.id || null,
									username: loginData.username || '',
									name: loginData.name || '',
									phone: loginData.phone || '',
									email: loginData.email || '',
									sex: loginData.sex || '未知',
									avatar: loginData.avatar || '',
									role: loginData.role || 'USER',
									token: loginData.token || '',
									// 保存其他可能需要的字段
									...loginData
								}
								
								// 存储用户信息（完整对象）
								uni.setStorageSync('user', user)
								
								// 存储token（确保 token 正确存储）
								if (loginData.token) {
									uni.setStorageSync('token', loginData.token)
								} else if (res.data.token) {
									uni.setStorageSync('token', res.data.token)
								} else {
									// 如果没有返回 token，使用用户ID生成一个
									uni.setStorageSync('token', `user_${user.id}`)
								}
								
								console.log('存储的完整用户信息:', user)
								console.log('存储的token:', uni.getStorageSync('token'))
								
								uni.showToast({
									icon: 'success',
									title: '登录成功',
									duration: 1500
								})
								
								setTimeout(() => {
									uni.switchTab({
										url: '/pages/index/index'
									})
								}, 1500)
							} else {
								uni.showToast({
									icon: 'none',
									title: res.data.msg || '登录失败'
								})
							}
						},
						fail: (err) => {
							uni.hideLoading()
							console.log('请求错误', err)
							uni.showToast({
								icon: 'none',
								title: '网络错误，请重试'
							})
						}
					})
				}).catch(err => {
					console.log('表单验证错误', err)
					uni.showToast({
						icon: 'none',
						title: '请填写完整信息'
					})
				})
			}
		}
	}
</script>

<style>
	.box {
		background: #fff;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1);
		margin-top: 100rpx;
	}
</style>