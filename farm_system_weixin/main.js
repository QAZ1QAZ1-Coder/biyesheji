import App from './App'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
import request from './utils/request'  // 引入请求配置

Vue.config.productionTip = false

// 安装请求插件
Vue.use(request)

App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
import request from './utils/request'

export function createApp() {
  const app = createSSRApp(App)
  
  // VUE3 安装请求插件
  app.use(request)
  
  return {
    app
  }
}
// #endif