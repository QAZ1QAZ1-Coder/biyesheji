<template>
	<view class="container">
		<!-- 滚动内容 -->
		<scroll-view class="scroll-view" scroll-y>
			<!-- 头像区域 -->
			<view class="avatar-section">
				<view class="avatar-wrapper" @click="chooseAvatar">
					<image class="avatar-image" :src="getAvatarUrl(formData.avatar)" mode="aspectFill"></image>
					<view class="avatar-overlay">
						<uni-icons type="camera" size="30" color="#fff"></uni-icons>
					</view>
				</view>
				<text class="username">{{ formData.name || formData.username || '用户' }}</text>
				<text class="avatar-hint">点击头像可更换</text>
			</view>

			<!-- 基本信息表单 -->
			<view class="form-section">
				<!-- 手机号 -->
				<view class="form-item">
					<text class="form-label">手机号</text>
					<input 
						class="form-input" 
						type="number" 
						v-model="formData.phone"
						placeholder="请输入手机号"
						maxlength="11"
						@input="onPhoneInput"
					/>
				</view>

				<!-- 邮箱 -->
				<view class="form-item">
					<text class="form-label">邮箱</text>
					<input 
						class="form-input" 
						type="text" 
						v-model="formData.email"
						placeholder="请输入邮箱"
						@blur="validateEmail"
					/>
				</view>

				<!-- 性别 -->
				<view class="form-item">
					<text class="form-label">性别</text>
					<view class="gender-options">
						<view 
							class="gender-option" 
							:class="{ 'gender-selected': formData.sex === '男' }"
							@click="selectGender('男')"
						>
							<text>男</text>
						</view>
						<view 
							class="gender-option" 
							:class="{ 'gender-selected': formData.sex === '女' }"
							@click="selectGender('女')"
						>
							<text>女</text>
						</view>
						<view 
							class="gender-option" 
							:class="{ 'gender-selected': !formData.sex || formData.sex === '未知' }"
							@click="selectGender('未知')"
						>
							<text>未知</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 保存按钮 -->
			<view class="save-section">
				<button 
					class="save-btn" 
					:class="{ 'save-btn-disabled': !isFormValid || isSaving || !hasToken }"
					:disabled="!isFormValid || isSaving || !hasToken"
					@click="saveUserInfo"
				>
					<text v-if="!hasToken">请先登录</text>
					<text v-else-if="isSaving">保存中...</text>
					<text v-else>保存修改</text>
				</button>
			</view>
		</scroll-view>

		<!-- 图片选择器 -->
		<view class="avatar-picker" v-if="showAvatarPicker" @tap="closeAvatarPicker">
			<view class="picker-content" @tap.stop>
				<view class="picker-title">选择图片</view>
				<view class="picker-options">
					<view class="picker-option" @click="chooseImage('album')">
						<uni-icons type="image" size="24" color="#4CD964"></uni-icons>
						<text>从相册选择</text>
					</view>
				</view>
				<view class="picker-cancel" @click="closeAvatarPicker">
					<text>取消</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// 表单数据 - 参考 Android 端 User 对象
				formData: {
					id: null,
					avatar: '',
					name: '',
					username: '',
					phone: '',
					email: '',
					sex: '未知',
					token: '', // 参考 Android 端保存 token
					role: 'USER'
				},
				
				// 原始数据（用于比较是否有修改）
				originalData: {},
				
				// 状态
				isSaving: false,
				showAvatarPicker: false,
				isFormValid: false,
				hasToken: false,
				isLoadingFromServer: false,
				isDataLoadedFromServer: false,
				
				// 基础配置
				baseUrl: 'http://192.168.88.1:9090',
				defaultAvatar: '/static/images/default-avatar.png'
			}
		},
		onLoad() {
			this.loadUserInfo();
		},
		onShow() {
			// 监听用户信息更新事件
			uni.$on('updateUserInfo', this.handleUserInfoUpdate);
			
			// 页面重新显示时重新加载数据（静默）
			if (!this.isDataLoadedFromServer && !this.isLoadingFromServer) {
				this.loadUserFromServerSilently();
			}
		},
		onHide() {
			// 移除事件监听
			uni.$off('updateUserInfo', this.handleUserInfoUpdate);
		},
		onBackPress(options) {
			// 监听返回按钮
			if (this.hasUnsavedChanges()) {
				this.showUnsavedChangesDialog();
				return true; // 阻止默认返回行为
			}
			return false;
		},
		methods: {
			// 处理用户信息更新事件
			handleUserInfoUpdate(userInfo) {
				if (userInfo) {
					this.formData = {
						...this.formData,
						...userInfo
					};
				}
			},
			
			// 获取完整的头像URL
			getAvatarUrl(avatarPath) {
			    if (!avatarPath) {
			        return this.defaultAvatar;
			    }
			    
			    // 如果已经是完整URL，直接返回
			    if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
			        return avatarPath;
			    }
			    
			    // 处理不同格式的路径
			    if (avatarPath.startsWith('/files/')) {
			        // 从您的指定目录访问
			        return this.baseUrl + avatarPath;
			    } else if (avatarPath.startsWith('/uploads/')) {
			        // 从上传目录访问
			        return this.baseUrl + avatarPath;
			    } else if (avatarPath.startsWith('/')) {
			        // 其他相对路径
			        return this.baseUrl + avatarPath;
			    }
			    
			    // 默认处理
			    return this.baseUrl + '/' + avatarPath;
			},
			
			// 加载用户信息 - 参考 Android 端双重加载机制
			loadUserInfo() {
				try {
					// 1. 先加载本地数据，快速显示
					this.loadUserFromLocal();
					
					// 2. 然后从服务器获取最新数据（静默加载）
					this.loadUserFromServerSilently();
					
				} catch (error) {
					console.error('加载用户信息失败:', error);
					uni.showToast({
						title: '加载用户信息失败',
						icon: 'none'
					});
				}
			},
			
			// 从本地加载用户信息 - 参考 Android 端 loadUserFromLocal()
			loadUserFromLocal() {
				const user = uni.getStorageSync('user');
				const token = uni.getStorageSync('token');
				
				console.log('本地存储的用户信息:', user);
				console.log('本地存储的token:', token);
				
				// 更新是否有token的状态
				this.hasToken = !!token;
				
				if (!user) {
					console.warn('用户信息不存在');
					uni.showToast({
						title: '请先登录',
						icon: 'none',
						duration: 2000
					});
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/my/my'
						});
					}, 2000);
					return;
				}
				
				// 初始化表单数据 - 参考 Android 端 updateUIWithUserData()
				this.formData = {
					id: user.id || null,
					avatar: user.avatar || '',
					name: user.name || '',
					username: user.username || '',
					phone: user.phone || '',
					email: user.email || '',
					sex: user.sex || '未知',
					token: user.token || '',
					role: user.role || 'USER'
				};
				
				console.log('从本地存储初始化的表单数据:', this.formData);
				
				// 保存原始数据
				this.originalData = JSON.parse(JSON.stringify(this.formData));
				
				// 验证表单
				this.validateForm();
			},
			
			// 从服务器静默获取最新用户信息 - 修复版本，参考 Android 端实现
			async loadUserFromServerSilently() {
				try {
					const token = uni.getStorageSync('token');
					if (!token) {
						console.warn('没有token，跳过服务器请求');
						return;
					}

					if (this.isLoadingFromServer) {
						return; // 防止重复请求
					}

					this.isLoadingFromServer = true;

					console.log('开始静默从服务器获取用户信息，完整token:', token);
					console.log('请求URL:', `${this.baseUrl}/user/currentUser`);
					
					// 参考 Android 端的请求方式
					const res = await uni.request({
						url: `${this.baseUrl}/user/currentUser`,
						method: 'GET',
						header: {
							'Authorization': `Bearer ${token}` // 参考 Android 端格式
						},
						timeout: 8000
					});
					
					console.log('服务器静默响应完整信息:', res);
					console.log('状态码:', res.statusCode);
					console.log('响应数据:', res.data);
					
					this.isLoadingFromServer = false;
					
					// 处理 HTTP 状态码
					if (res.statusCode === 401) {
						console.warn('Token无效或已过期，状态码:', res.statusCode);
						// 静默处理，不提示用户，等待用户操作时再提示
						return;
					}
					
					if (res.statusCode !== 200) {
						console.warn('服务器响应异常，状态码:', res.statusCode);
						return;
					}
					
					// 解析响应数据，参考 Android 端 ResponseResult 结构
					const responseData = res.data;
					if (!responseData) {
						console.warn('服务器返回数据为空');
						return;
					}
					
					// 检查响应码，参考 Android 端逻辑
					if (responseData.code === 200 || responseData.code === '200') {
						const serverUser = responseData.data;
						
						if (!serverUser) {
							console.warn('服务器返回的用户数据为空');
							return;
						}
						
						console.log('服务器返回的最新用户数据:', serverUser);
						
						// 构建完整的用户对象，参考 Android 端处理逻辑
						const updatedData = {
							id: serverUser.id || this.formData.id,
							avatar: serverUser.avatar || this.formData.avatar,
							name: serverUser.name || this.formData.name,
							username: serverUser.username || this.formData.username,
							phone: serverUser.phone || this.formData.phone,
							email: serverUser.email || this.formData.email,
							sex: serverUser.sex || this.formData.sex || '未知',
							token: serverUser.token || this.formData.token || token,
							role: serverUser.role || this.formData.role || 'USER'
						};
						
						console.log('合并后的用户数据:', updatedData);
						
						// 更新表单数据
						this.formData = updatedData;
						this.isDataLoadedFromServer = true;
						
						// 更新本地存储（静默保存）
						const localUser = uni.getStorageSync('user') || {};
						const mergedUser = {
							...localUser,
							...updatedData
						};
						
						uni.setStorageSync('user', mergedUser);
						
						// 保存原始数据
						this.originalData = JSON.parse(JSON.stringify(updatedData));
						
						// 重新验证表单
						this.validateForm();
						
						// 检查是否有数据变化，参考 Android 端逻辑
						if (this.hasDataChanged(serverUser)) {
							console.log('检测到用户信息变化，自动更新');
							
							// 轻微提示用户信息已更新
							uni.showToast({
								title: '用户信息已同步',
								icon: 'none',
								duration: 1500
							});
							
							// 发送更新事件，通知其他页面
							uni.$emit('userInfoUpdated', mergedUser);
						}
						
					} else if (responseData.code === 401 || responseData.msg === 'Token无效或已过期' || 
								responseData.msg === '用户未登录') {
						console.warn('Token认证失败:', responseData.msg);
						// 静默处理
					} else {
						console.warn('服务器返回错误:', responseData.msg || responseData.code);
					}
					
				} catch (error) {
					this.isLoadingFromServer = false;
					console.warn('从服务器获取用户信息失败:', error);
					// 静默失败，不影响用户体验
				}
			},
			
			// 检查数据是否有变化 - 参考 Android 端 hasDataChanged()
			hasDataChanged(newUser) {
				const localUser = uni.getStorageSync('user');
				if (!localUser) {
					return false;
				}

				// 比较关键字段，参考 Android 端逻辑
				const compareFields = ['phone', 'email', 'sex', 'avatar', 'name'];
				
				for (const field of compareFields) {
					const oldValue = localUser[field] || '';
					const newValue = newUser[field] || '';
					
					if (oldValue !== newValue) {
						console.log(`字段 ${field} 发生变化: ${oldValue} -> ${newValue}`);
						return true;
					}
				}
				
				return false;
			},
			
			// 显示登录过期提示
			showLoginExpired() {
				uni.showModal({
					title: '提示',
					content: '登录已过期，请重新登录',
					showCancel: false,
					confirmText: '去登录',
					success: (res) => {
						if (res.confirm) {
							this.goToLogin();
						}
					}
				});
			},
			
			// 跳转到登录页面 - 参考 Android 端 goToLogin()
			goToLogin() {
				console.log('跳转到登录页面');
				
				// 清除本地登录信息
				uni.removeStorageSync('user');
				uni.removeStorageSync('token');
				this.hasToken = false;
				
				// 跳转到登录页面，参考 Android 端参数传递
				uni.reLaunch({
					url: '/pages/login/login?from=MyEdit&message=登录已过期，请重新登录'
				});
			},
			
			// 处理 Token 过期 - 参考 Android 端 handleTokenExpired()
			handleTokenExpired() {
				console.log('Token已过期');
				uni.showToast({
					title: '登录已过期，请重新登录',
					icon: 'none',
					duration: 2000
				});
				
				setTimeout(() => {
					this.goToLogin();
				}, 1500);
			},
			
			// 选择头像
			chooseAvatar() {
				if (!this.hasToken) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					return;
				}
				this.showAvatarPicker = true;
			},
			
			// 关闭头像选择器
			closeAvatarPicker() {
				this.showAvatarPicker = false;
			},
			
			// 选择图片
			chooseImage(sourceType) {
				this.closeAvatarPicker();
				
				uni.chooseImage({
					count: 1,
					sizeType: ['compressed'],
					sourceType: [sourceType],
					success: (res) => {
						this.uploadAvatar(res.tempFilePaths[0]);
					},
					fail: (err) => {
						console.error('选择图片失败:', err);
						if (!err.errMsg.includes('cancel')) {
							uni.showToast({
								title: '选择图片失败',
								icon: 'none'
							});
						}
					}
				});
			},
			
			// 上传头像 - 参考 Android 端 uploadAvatar()
			async uploadAvatar(tempFilePath) {
				uni.showLoading({
					title: '上传中...',
					mask: true
				});
				
				try {
					const token = uni.getStorageSync('token');
					if (!token) {
						uni.hideLoading();
						uni.showToast({
							title: '请先登录',
							icon: 'none'
						});
						return;
					}
					
					console.log('开始上传头像，使用token:', token.substring(0, 20) + '...');
					
					// 参考 Android 端使用带 Token 的上传接口
					const uploadTask = uni.uploadFile({
						url: `${this.baseUrl}/user/uploadAvatar`,
						filePath: tempFilePath,
						name: 'file',
						header: {
							'Authorization': `Bearer ${token}` // 参考 Android 端格式
						},
						success: (uploadRes) => {
							uni.hideLoading();
							console.log('上传头像原始响应:', uploadRes);
							
							try {
								const res = JSON.parse(uploadRes.data);
								console.log('上传头像解析响应:', res);
								
								if (res.code === 200 || res.code === '200') {
									// 更新头像
									const avatarUrl = res.data;
									console.log('获取到头像URL:', avatarUrl);
									
									if (avatarUrl) {
										this.formData.avatar = avatarUrl;
										
										// 更新本地存储
										const localUser = uni.getStorageSync('user') || {};
										const updatedUser = {
											...localUser,
											avatar: avatarUrl
										};
										
										uni.setStorageSync('user', updatedUser);
										
										uni.showToast({
											title: '头像上传成功',
											icon: 'success',
											duration: 2000
										});
										
										// 发送更新事件，参考 Android 端 setResult()
										uni.$emit('userInfoUpdated', updatedUser);
										
									} else {
										uni.showToast({
											title: '头像URL为空',
											icon: 'none'
										});
									}
									
								} else if (res.code === 401 || res.msg === 'Token无效或已过期') {
									// 处理 Token 过期，参考 Android 端
									console.warn('Token过期，响应码:', res.code);
									this.handleTokenExpired();
								} else {
									uni.showToast({
										title: res.msg || '上传失败',
										icon: 'none'
									});
								}
							} catch (parseError) {
								console.error('解析响应失败:', parseError);
								uni.showToast({
									title: '上传失败',
									icon: 'none'
								});
							}
						},
						fail: (err) => {
							uni.hideLoading();
							console.error('上传失败:', err);
							uni.showToast({
								title: '网络错误',
								icon: 'none'
							});
						}
					});
					
				} catch (error) {
					uni.hideLoading();
					console.error('上传异常:', error);
					uni.showToast({
						title: '上传失败',
						icon: 'none'
					});
				}
			},
			
			// 手机号输入处理
			onPhoneInput(e) {
				let value = e.detail.value;
				// 限制只能输入数字
				this.formData.phone = value.replace(/[^\d]/g, '');
				this.validateForm();
			},
			
			// 验证邮箱
			validateEmail() {
				const email = this.formData.email;
				if (email && !this.isValidEmail(email)) {
					uni.showToast({
						title: '请输入有效的邮箱地址',
						icon: 'none',
						duration: 2000
					});
				}
				this.validateForm();
			},
			
			// 邮箱验证函数
			isValidEmail(email) {
				const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
				return emailRegex.test(email);
			},
			
			// 选择性别
			selectGender(gender) {
				this.formData.sex = gender;
				this.validateForm();
			},
			
			// 验证表单 - 参考 Android 端验证逻辑
			validateForm() {
				const { phone, email } = this.formData;
				
				// 手机号验证，参考 Android 端
				if (!phone || phone.trim() === '') {
					this.isFormValid = false;
					return false;
				}
				
				if (phone.length !== 11) {
					this.isFormValid = false;
					return false;
				}
				
				// 邮箱验证，参考 Android 端要求
				if (!email || email.trim() === '') {
					this.isFormValid = false;
					return false;
				}
				
				if (!this.isValidEmail(email)) {
					this.isFormValid = false;
					return false;
				}
				
				this.isFormValid = true;
				return true;
			},
			
			// 检查是否有未保存的更改 - 参考 Android 端 hasUnsavedChanges()
			hasUnsavedChanges() {
				const current = JSON.stringify(this.formData);
				const original = JSON.stringify(this.originalData);
				const hasChanges = current !== original;
				
				console.log('检查未保存更改:', {
					current: this.formData,
					original: this.originalData,
					hasChanges: hasChanges
				});
				
				return hasChanges;
			},
			
			// 显示未保存更改对话框 - 参考 Android 端 showUnsavedChangesDialog()
			showUnsavedChangesDialog() {
				uni.showModal({
					title: '提示',
					content: '您有未保存的更改，确定要离开吗？',
					showCancel: true,
					cancelText: '继续编辑',
					confirmText: '离开',
					success: (res) => {
						if (res.confirm) {
							uni.navigateBack();
						}
					}
				});
			},
			
			// 保存用户信息 - 参考 Android 端 saveUserInfo() 和 updateUserToServer()
			async saveUserInfo() {
				// 检查是否有token
				if (!this.hasToken) {
					uni.showToast({
						title: '请先登录',
						icon: 'none'
					});
					return;
				}
				
				// 表单验证，参考 Android 端
				if (!this.validateForm()) {
					uni.showToast({
						title: '请填写正确的信息',
						icon: 'none'
					});
					return;
				}
				
				// 检查是否有更改，参考 Android 端
				if (!this.hasUnsavedChanges()) {
					uni.showToast({
						title: '信息未更改',
						icon: 'none'
					});
					return;
				}
				
				this.isSaving = true;
				
				try {
					const token = uni.getStorageSync('token');
					const localUser = uni.getStorageSync('user') || {};
					
					if (!token) {
						uni.showToast({
							title: '请先登录',
							icon: 'none'
						});
						this.isSaving = false;
						this.hasToken = false;
						return;
					}
					
					// 准备请求数据 - 参考 Android 端 User 对象结构
					const requestData = {
						id: this.formData.id || localUser.id,
						username: this.formData.username || localUser.username,
						name: this.formData.name || localUser.name,
						phone: this.formData.phone || '',
						email: this.formData.email || '',
						sex: this.formData.sex || '未知',
						avatar: this.formData.avatar || localUser.avatar || '',
						token: this.formData.token || localUser.token || token,
						role: this.formData.role || localUser.role || 'USER'
					};
					
					// 清理空值
					Object.keys(requestData).forEach(key => {
						if (requestData[key] === undefined || requestData[key] === null) {
							requestData[key] = '';
						}
					});
					
					console.log('保存数据:', requestData);
					console.log('使用的token长度:', token.length);
					
					// 调用更新接口 - 参考 Android 端 updateUserToServer()
					const res = await uni.request({
						url: `${this.baseUrl}/user/update`,
						method: 'PUT',
						header: {
							'Content-Type': 'application/json',
							'Authorization': `Bearer ${token}` // 参考 Android 端格式
						},
						data: requestData,
						timeout: 10000
					});
					
					console.log('保存响应完整信息:', res);
					console.log('响应状态码:', res.statusCode);
					console.log('响应数据:', res.data);
					
					// 处理 HTTP 401 错误，参考 Android 端
					if (res.statusCode === 401) {
						console.warn('HTTP 401 Unauthorized');
						this.handleTokenExpired();
						return;
					}
					
					if (res.data) {
						const response = res.data;
						
						// 检查响应码，参考 Android 端
						if (response.code === 200 || response.code === '200' || response.code === 'success') {
							// 更新本地存储，参考 Android 端
							const updatedUser = {
								...localUser,
								...this.formData,
								// 确保关键字段
								id: this.formData.id || localUser.id,
								token: token // 保持原有token
							};
							
							uni.setStorageSync('user', updatedUser);
							
							// 更新原始数据
							this.originalData = JSON.parse(JSON.stringify(this.formData));
							
							uni.showToast({
								title: '保存成功',
								icon: 'success',
								duration: 2000
							});
							
							// 发送更新事件，参考 Android 端 setResult(RESULT_OK)
							uni.$emit('userInfoUpdated', updatedUser);
							
							// 延迟返回，参考 Android 端
							setTimeout(() => {
								uni.navigateBack();
							}, 1500);
							
						} else if (response.code === 401 || response.msg === 'Token无效或已过期') {
							// 处理 Token 过期，参考 Android 端
							console.warn('Token过期，响应码:', response.code);
							this.handleTokenExpired();
						} else {
							// 其他错误
							uni.showToast({
								title: response.msg || '保存失败',
								icon: 'none'
							});
						}
					} else {
						uni.showToast({
							title: '服务器响应异常',
							icon: 'none'
						});
					}
					
				} catch (error) {
					console.error('保存失败:', error);
					
					// 网络错误时，保存到本地（静默保存） - 参考 Android 端
					const localUser = uni.getStorageSync('user') || {};
					const updatedUser = {
						...localUser,
						...this.formData
					};
					
					uni.setStorageSync('user', updatedUser);
					
					// 更新原始数据
					this.originalData = JSON.parse(JSON.stringify(this.formData));
					
					uni.showToast({
						title: '网络错误，已保存到本地',
						icon: 'none',
						duration: 2000
					});
					
					// 发送更新事件
					uni.$emit('userInfoUpdated', updatedUser);
					
					// 延迟返回
					setTimeout(() => {
						uni.navigateBack();
					}, 1500);
					
				} finally {
					this.isSaving = false;
				}
			}
		}
	}
</script>

<style scoped>
	/* 样式保持不变 */
	.container {
		min-height: 100vh;
		background-color: #f8f9fa;
	}
	
	.scroll-view {
		height: calc(100vh - 44px);
		padding: 20rpx;
	}
	
	/* 头像区域 */
	.avatar-section {
		background-color: #ffffff;
		border-radius: 16rpx;
		padding: 40rpx 0;
		margin-bottom: 30rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}
	
	.avatar-wrapper {
		width: 160rpx;
		height: 160rpx;
		border-radius: 50%;
		position: relative;
		overflow: hidden;
		margin-bottom: 20rpx;
	}
	
	.avatar-image {
		width: 100%;
		height: 100%;
	}
	
	.avatar-overlay {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		opacity: 0;
		transition: opacity 0.3s;
	}
	
	.avatar-wrapper:active .avatar-overlay {
		opacity: 1;
	}
	
	.username {
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
		margin-bottom: 8rpx;
	}
	
	.avatar-hint {
		font-size: 24rpx;
		color: #999999;
	}
	
	/* 表单区域 */
	.form-section {
		background-color: #ffffff;
		border-radius: 16rpx;
		padding: 0 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}
	
	.form-item {
		padding: 30rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.form-item:last-child {
		border-bottom: none;
	}
	
	.form-label {
		font-size: 28rpx;
		color: #333333;
		margin-bottom: 20rpx;
		display: block;
	}
	
	.form-input {
		height: 80rpx;
		font-size: 28rpx;
		color: #333333;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		padding: 0 20rpx;
		background-color: #fafafa;
	}
	
	/* 性别选项 */
	.gender-options {
		display: flex;
		justify-content: space-between;
	}
	
	.gender-option {
		flex: 1;
		height: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border: 1rpx solid #e0e0e0;
		border-radius: 8rpx;
		margin-right: 20rpx;
		background-color: #fafafa;
	}
	
	.gender-option:last-child {
		margin-right: 0;
	}
	
	.gender-option text {
		font-size: 28rpx;
		color: #666666;
	}
	
	.gender-selected {
		background-color: #4CD964;
		border-color: #4CD964;
	}
	
	.gender-selected text {
		color: #ffffff;
	}
	
	/* 保存按钮 */
	.save-section {
		padding: 20rpx 0;
	}
	
	.save-btn {
		background-color: #4CD964;
		color: white;
		border: none;
		border-radius: 40rpx;
		padding: 20rpx;
		font-size: 30rpx;
		font-weight: 500;
		height: 90rpx;
		line-height: 50rpx;
		transition: all 0.3s;
	}
	
	.save-btn-disabled {
		background-color: #cccccc !important;
		opacity: 0.7;
	}
	
	.save-btn:active:not(.save-btn-disabled) {
		background-color: #3ac753;
		transform: scale(0.98);
	}
	
	/* 头像选择器 */
	.avatar-picker {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: flex-end;
		z-index: 999;
	}
	
	.picker-content {
		width: 100%;
		background-color: #ffffff;
		border-radius: 20rpx 20rpx 0 0;
		padding-bottom: env(safe-area-inset-bottom);
	}
	
	.picker-title {
		text-align: center;
		padding: 30rpx 0;
		font-size: 32rpx;
		color: #333333;
		font-weight: bold;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.picker-options {
		padding: 20rpx 0;
	}
	
	.picker-option {
		padding: 30rpx 40rpx;
		display: flex;
		align-items: center;
	}
	
	.picker-option text {
		font-size: 28rpx;
		color: #333333;
		margin-left: 20rpx;
	}
	
	.picker-option:active {
		background-color: #f5f5f5;
	}
	
	.picker-cancel {
		padding: 30rpx 40rpx;
		text-align: center;
		border-top: 1rpx solid #f0f0f0;
		margin-top: 10rpx;
	}
	
	.picker-cancel text {
		font-size: 28rpx;
		color: #666666;
	}
	
	.picker-cancel:active {
		background-color: #f5f5f5;
	}
</style>