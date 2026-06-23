import request from '@/utils/request'

export function login(username: string, password: string) {
  return request.post('/auth/login', { username, password })
}

export function logout() {
  return request.post('/auth/logout')
}
