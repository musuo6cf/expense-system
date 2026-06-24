import request from '@/utils/request'

export function previewBatchReview() {
  return request.post('/finance/batch-review/preview')
}

export function executeBatchReview(data: { rejectIds: number[] }) {
  return request.post('/finance/batch-review/execute', data)
}
