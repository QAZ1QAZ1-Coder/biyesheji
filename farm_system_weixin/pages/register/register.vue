<template>
	<view style="padding: 20rpx;">
		<view class="box" style="padding: 60rpx 30rpx;">
			<view style="font-size: 40rpx; font-weight: bold; text-align: center; margin-bottom: 20rpx;">助农·农产品销售系统</view>
			<view style="font-size: 32rpx; text-align: center; color: #666; margin-bottom: 60rpx;">用户注册</view>
			
			<uni-forms ref="formRef" :modelValue="form" :rules="rules" validateTrigger="blur">
				<uni-forms-item name="username" required>
					<uni-easyinput prefixIcon="person" v-model="form.username" placeholder="请输入账号" />
				</uni-forms-item>
				<uni-forms-item name="phone" required>
					<uni-easyinput prefixIcon="phone" v-model="form.phone" placeholder="请输入手机号" />
				</uni-forms-item>
				<uni-forms-item name="password" required>
					<uni-easyinput type="password" prefixIcon="locked" v-model="form.password" placeholder="请输入密码（不少于6位）" />
				</uni-forms-item>
				<uni-forms-item name="passwordConfirm" required>
					<uni-easyinput type="password" prefixIcon="locked" v-model="form.passwordConfirm" placeholder="请确认密码" />
				</uni-forms-item>
				<uni-forms-item>
					<button @click="register" type="primary" style="width: 100%; height: 80rpx; line-height: 80rpx; background-color: #4CD964; border-color: #4CD964; border-radius: 40rpx; margin-top: 40rpx;">注册</button>
				</uni-forms-item>
			</uni-forms>
		</view>
		
		<view style="text-align: center; margin-top: 40rpx;">
			已有账号？请 <text @click="goLogin" style="display: inline; margin-left: 5rpx; color: #4CD964;">登录</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				form: { 
					role: 'USER',
					name: '', // 后端需要name字段，默认用账号名
					phone: ''
				},
				rules: {
					username: {
						rules: [
							{
								required: true,
								errorMessage: '请输入账号',
							},
							{
								minLength: 3,
								maxLength: 20,
								errorMessage: '账号长度在3-20个字符之间',
							}
						],
					},
					phone: {
						rules: [
							{
								required: true,
								errorMessage: '请输入手机号',
							},
							 {
							            minLength: 11,
							            maxLength: 11,
							            errorMessage: '手机号必须是11位',
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
								minLength: 6,
								errorMessage: '密码长度不能少于6位',
							}
						],
					},
					passwordConfirm: {
						rules: [
							{
								required: true,
								errorMessage: '请确认密码',
							},
							{
								validateFunction: (rule, value, data, callback) => {
									if (value !== this.form.password) {
										callback('两次输入的密码不一致')
									}
									return true
								}
							}
						],
					}
				}
			}
		},
		methods: {
			goLogin() {
				uni.redirectTo({
					url: '/pages/login/login'
				})
			},
			register() {
				this.$refs.formRef.validate().then(() => {
					// 显示加载中
					uni.showLoading({
						title: '注册中...',
						mask: true
					})
					
					// 设置name为username，因为后端需要name字段
					const registerData = {
						...this.form,
						name: this.form.username // 用账号作为姓名
					}
					
					this.$request.post('/register', registerData).then(res => {
						uni.hideLoading()
						if (res.code === '200') { // 注册成功
							uni.showToast({
								icon: 'success',
								title: '注册成功',
								duration: 1500
							})
							setTimeout(() => {
								uni.redirectTo({
									url: '/pages/login/login'
								})
							}, 1500)
						} else {
							uni.showToast({
								icon: 'none',
								title: res.msg || '注册失败'
							})
						}
					}).catch(err => {
						uni.hideLoading()
						console.log('请求错误', err)
						uni.showToast({
							icon: 'none',
							title: '网络错误，请重试'
						})
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
		margin-top: 60rpx;
	}
</style>