import request from '@/utils/request'

export function getUserById(id: number) {
  return request.get(`/user/${id}`)
}

export function updatePassword(oldPassword: string, newPassword: string) {
  return request.put('/user/password', { oldPassword, newPassword })
}
