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

export function submitExpense(id: string) {
  return request.post(`/expense/submit/${id}`)
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

export function downloadAttachment(id: string) {
  window.open(`${import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'}/attachment/download/${id}`, '_blank')
}

export async function exportExpenses(params?: { status?: number; keyword?: string }) {
  const res = await request.get('/expense/export', {
    params,
    responseType: 'blob'
  })
  // Parse filename from Content-Disposition header
  const disposition = res.headers['content-disposition'] as string
  let filename = 'expense_export.xlsx'
  if (disposition) {
    const match = disposition.match(/filename\*?=(?:UTF-8'')?([^;]+)/)
    if (match) {
      filename = decodeURIComponent(match[1])
    }
  }
  // Trigger browser download
  const url = window.URL.createObjectURL(res.data as Blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
