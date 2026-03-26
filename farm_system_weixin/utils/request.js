// utils/request.js
const baseURL = 'http://localhost:9090'

const request = (options) => {
    return new Promise((resolve, reject) => {
        uni.request({
            url: baseURL + options.url,
            method: options.method || 'GET',
            data: options.data,
            header: {
                'Content-Type': 'application/json',
                ...options.header
            },
            success: (res) => {
                // 统一处理响应
                if (res.statusCode === 200) {
                    resolve(res.data)
                } else {
                    reject(res)
                }
            },
            fail: (err) => {
                console.error('请求失败:', err)
                reject(err)
            }
        })
    })
}

export default {
    install(Vue) {
        Vue.prototype.$request = {
            get: (url, data) => request({ url, method: 'GET', data }),
            post: (url, data) => request({ url, method: 'POST', data }),
            put: (url, data) => request({ url, method: 'PUT', data }),
            delete: (url, data) => request({ url, method: 'DELETE', data })
        }
    }
}