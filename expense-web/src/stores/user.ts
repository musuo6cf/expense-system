import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || 0)
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')
  const roles = ref<string[]>(JSON.parse(localStorage.getItem('roles') || '[]'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(usernameVal: string, password: string) {
    const res: any = await loginApi(usernameVal, password)
    const d = res.data
    token.value = d.token
    userId.value = d.userId
    username.value = d.username
    realName.value = d.realName
    roles.value = d.roles
    localStorage.setItem('token', d.token)
    localStorage.setItem('userId', String(d.userId))
    localStorage.setItem('username', d.username)
    localStorage.setItem('realName', d.realName)
    localStorage.setItem('roles', JSON.stringify(d.roles))
  }

  function logout() {
    logoutApi().finally(() => {
      token.value = ''
      userId.value = 0
      username.value = ''
      realName.value = ''
      roles.value = []
      localStorage.clear()
      router.push('/login')
    })
  }

  return { token, userId, username, realName, roles, isLoggedIn, login, logout }
})
