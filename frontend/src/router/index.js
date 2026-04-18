import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      // 主布局，所有需要登录的页面都在这里
      path: '/',
      component: () => import('../views/LayoutView.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'knowledge',
          name: 'knowledge',
          component: () => import('../views/KnowledgeView.vue')
        },
        {
          path: 'chat/:kbId',
          name: 'chat',
          component: () => import('../views/ChatView.vue')
        }
      ]
    }
  ]
})

// 路由守卫：没有 token 就跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router