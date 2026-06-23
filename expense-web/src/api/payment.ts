import request from '@/utils/request'

export function getPendingList() {
  return request.get('/payment/pending')
}

export function getPaymentDetail(expenseId: number) {
  return request.get(`/payment/${expenseId}`)
}

export function payExpense(expenseId: number, paymentMethod: string, bankTransactionNo: string) {
  return request.post('/payment/pay', { expenseId, paymentMethod, bankTransactionNo })
}
