import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue')
        },
        {
          path: 'expense',
          name: 'ExpenseList',
          component: () => import('@/views/expense/index.vue')
        },
        {
          path: 'expense/edit',
          name: 'ExpenseCreate',
          component: () => import('@/views/expense/edit.vue')
        },
        {
          path: 'expense/edit/:id',
          name: 'ExpenseEdit',
          component: () => import('@/views/expense/edit.vue')
        },
        {
          path: 'expense/:id',
          name: 'ExpenseDetail',
          component: () => import('@/views/expense/detail.vue')
        },
        {
          path: 'approval',
          name: 'ApprovalList',
          component: () => import('@/views/approval/index.vue')
        },
        {
          path: 'approval/:id',
          name: 'ApprovalDetail',
          component: () => import('@/views/approval/detail.vue')
        },
        {
          path: 'finance',
          name: 'FinanceList',
          component: () => import('@/views/finance/index.vue')
        },
        {
          path: 'finance/paid',
          name: 'FinancePaid',
          component: () => import('@/views/finance/paid.vue')
        },
        {
          path: 'finance/:id',
          name: 'FinanceDetail',
          component: () => import('@/views/finance/detail.vue')
        },
        {
          path: 'payment',
          name: 'PaymentList',
          component: () => import('@/views/payment/index.vue')
        },
        {
          path: 'payment/:id',
          name: 'PaymentDetail',
          component: () => import('@/views/payment/detail.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/index.vue')
        },
        {
          path: 'user',
          name: 'UserList',
          component: () => import('@/views/user/index.vue')
        },
        {
          path: 'user/edit',
          name: 'UserCreate',
          component: () => import('@/views/user/edit.vue')
        },
        {
          path: 'user/edit/:id',
          name: 'UserEdit',
          component: () => import('@/views/user/edit.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
