import request from '@/utils/request'

export function getPendingList() {
  return request.get('/finance/pending')
}

export function getFinanceDetail(expenseId: number) {
  return request.get(`/finance/${expenseId}`)
}

export function passFinance(expenseId: number, comment: string) {
  return request.post('/finance/pass', { expenseId, comment })
}

export function rejectFinance(expenseId: number, comment: string) {
  return request.post('/finance/reject', { expenseId, comment })
}
