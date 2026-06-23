import { getExpensePage } from './expense'
import { getPendingList as getApprovalPending } from './approval'
import { getPendingList as getFinancePending } from './finance'
import { getPendingList as getPaymentPending } from './payment'

export async function getDashboardStats(roles: string[]) {
  const [myRes, paidRes, recentRes] = await Promise.all([
    getExpensePage({ page: 1, size: 1 }).catch(() => ({ data: { total: 0 } })),
    getExpensePage({ page: 1, size: 1, status: 6 }).catch(() => ({ data: { total: 0 } })),
    getExpensePage({ page: 1, size: 5 }).catch(() => ({ data: { records: [] } }))
  ])

  let approvalCount = 0
  let financeCount = 0
  let paymentCount = 0

  if (roles.includes('MANAGER')) {
    try { const r: any = await getApprovalPending(); approvalCount = r.data?.length || 0 } catch { /**/ }
  }
  if (roles.includes('FINANCE')) {
    try { const r: any = await getFinancePending(); financeCount = r.data?.length || 0 } catch { /**/ }
    try { const r: any = await getPaymentPending(); paymentCount = r.data?.length || 0 } catch { /**/ }
  }

  return {
    myCount: (myRes as any).data?.total || 0,
    approvalCount,
    financeCount,
    paidCount: (paidRes as any).data?.total || 0,
    recentRecords: (recentRes as any).data?.records || []
  }
}

export async function getChartData() {
  const res: any = await getExpensePage({ page: 1, size: 200 }).catch(() => ({ data: { records: [] } }))
  const records: any[] = res.data?.records || []

  const monthMap: Record<string, number> = {}
  const statusMap: Record<number, number> = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0 }

  for (const r of records) {
    const month = r.createTime?.substring(0, 7) || ''
    if (month) {
      monthMap[month] = (monthMap[month] || 0) + (Number(r.totalAmount) || 0)
    }
    const s = r.status
    if (statusMap[s] !== undefined) statusMap[s]++
  }

  const months = Object.keys(monthMap).sort()
  const amounts = months.map(m => monthMap[m])

  return { months, amounts, statusMap }
}
