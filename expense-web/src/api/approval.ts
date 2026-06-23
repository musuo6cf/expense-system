import request from '@/utils/request'

export function getPendingList() {
  return request.get('/approval/pending')
}

export function getApprovalDetail(expenseId: number) {
  return request.get(`/approval/${expenseId}`)
}

export function passApproval(expenseId: number, comment: string) {
  return request.post('/approval/pass', { expenseId, comment })
}

export function rejectApproval(expenseId: number, comment: string) {
  return request.post('/approval/reject', { expenseId, comment })
}
