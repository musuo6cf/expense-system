import request from '@/utils/request'

export function getUserPage(page: number, size: number, keyword?: string) {
  return request.get('/user/page', { params: { page, size, keyword } })
}

export function getUserById(id: string) {
  return request.get(`/user/${id}`)
}

export function createUser(data: any) {
  return request.post('/user', data)
}

export function updateUser(data: any) {
  return request.put('/user', data)
}

export function deleteUser(id: string) {
  return request.delete(`/user/${id}`)
}

export function updatePassword(oldPassword: string, newPassword: string) {
  return request.put('/user/password', { oldPassword, newPassword })
}

export function getDepartmentList() {
  return request.get('/department/list')
}

export function getRoleList() {
  return request.get('/role/list')
}
