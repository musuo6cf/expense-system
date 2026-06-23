import request from '@/utils/request'

export function getExpensePage(params: {
  page: number
  size: number
  status?: number | null
  keyword?: string
}) {
  return request.get('/expense/page', { params })
}

export function getExpenseById(id: number) {
  return request.get(`/expense/${id}`)
}

export function createExpense(data: {
  title: string
  reason: string
  items: { expenseType: string; amount: number; expenseDate: string; description: string }[]
}) {
  return request.post('/expense', data)
}

export function updateExpense(id: number, data: {
  title: string
  reason: string
  items: { expenseType: string; amount: number; expenseDate: string; description: string }[]
}) {
  return request.put(`/expense/${id}`, data)
}

export function deleteExpense(id: number) {
  return request.delete(`/expense/${id}`)
}

export function uploadAttachment(expenseId: number, file: File) {
  const formData = new FormData()
  formData.append('expenseId', String(expenseId))
  formData.append('file', file)
  return request.post('/attachment/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getAttachments(expenseId: number) {
  return request.get(`/attachment/expense/${expenseId}`)
}

export function deleteAttachment(id: number) {
  return request.delete(`/attachment/${id}`)
}
