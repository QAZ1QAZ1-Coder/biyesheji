import {createRouter, createWebHistory} from 'vue-router'
import { ElMessage } from 'element-plus';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/Manager.vue'),
      redirect: '/login',
      children: [
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'password', component: () => import('@/views/manager/Password.vue')},
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'notice', component: () => import('@/views/manager/Notice.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'goodsStock', component: () => import('@/views/manager/GoodsStock.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'buy', component: () => import('@/views/manager/Buy.vue')},
        { path: 'orders', component: () => import('@/views/manager/Orders.vue')},
        // 普通用户地址管理
        {
          path: 'address', component: () => import('@/views/manager/Address.vue'),
          meta: { requiresUser: true,roles: ['USER'] }
        },
        // 管理员用户地址管理
        {
          path: 'adminAddress', component: () => import('@/views/manager/AdminAddress.vue'),
          meta: { requiresAdmin: true,roles: ['ADMIN'] }
        },
      ]
    },
    { path: '/login', component: () => import('@/views/Login.vue')},
    { path: '/register', component: () => import('@/views/Register.vue')},
  ]
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('system-user') || '{}');


  // 检查登录状态
  if (to.meta.requiresAuth && !user.id) {
    next('/login');
    return;
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin && user.role !== 'ADMIN') {
    ElMessage.error('无权限访问');
    next('/home');
    return;
  }

  // 检查普通用户权限
  if (to.meta.requiresUser && user.role !== 'USER') {
    ElMessage.error('无权限访问');
    next('/home');
    return;
  }

  next();
});

export default router