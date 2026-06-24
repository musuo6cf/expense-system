<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaymentDetail, payExpense } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const expenseId = String(route.params.id)

const detail = ref<any>(null)
const paymentMethod = ref('向工商银行卡号转账')
const submitting = ref(false)

const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '草稿', type: 'info' },
  1: { text: '待主管审批', type: 'warning' },
  2: { text: '主管驳回', type: 'danger' },
  3: { text: '待财务审核', type: 'warning' },
  4: { text: '财务驳回', type: 'danger' },
  5: { text: '待付款', type: '' },
  6: { text: '已付款', type: 'success' }
}

function getStatusTag(status: number) {
  return statusMap[status] || { text: '未知', type: 'info' }
}

async function fetchDetail() {
  const res: any = await getPaymentDetail(expenseId)
  detail.value = res.data
}

async function handlePay() {
  try {
    await ElMessageBox.confirm(
      `确认付款 ¥${detail.value.totalAmount}？`,
      '确认付款',
      { type: 'warning' }
    )
  } catch {
    return
  }
  submitting.value = true
  try {
    await payExpense(expenseId, paymentMethod.value, '')
    ElMessage.success('付款成功')
    router.push('/payment')
  } finally {
    submitting.value = false
  }
}

onMounted(() => fetchDetail())
</script>

<template>
  <div v-if="detail" style="max-width: 900px">
    <!-- Basic Info -->
    <el-card style="margin-bottom: 16px">
      <template #header><span>基本信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报销编号">{{ detail.expenseNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(detail.status).type">
            {{ getStatusTag(detail.status).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="报销原因" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Payment Records -->
    <el-card style="margin-bottom: 16px">
      <template #header><span>付款记录</span></template>
      <div v-if="!detail.paymentRecords || detail.paymentRecords.length === 0" style="color: #909399">
        暂无付款记录
      </div>
      <el-timeline v-else>
        <el-timeline-item
          v-for="(r, i) in detail.paymentRecords"
          :key="i"
          type="success"
          :timestamp="r.paymentTime"
        >
          <div>
            <strong>{{ r.operatorName }}</strong> 完成付款
          </div>
          <div style="color: #606266; margin-top: 4px">
            方式：{{ r.paymentMethod }} · 流水号：{{ r.bankTransactionNo }}
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- Payment Action (only for PENDING_PAYMENT status) -->
    <el-card v-if="detail.status === 5">
      <template #header><span>确认付款</span></template>
      <el-form label-width="100px">
        <el-form-item label="付款方式">
          <span>向工商银行卡号转账</span>
        </el-form-item>
        <el-form-item label="收款卡号">
          <span>{{ detail.applicantIcbcCardNo || '未登记' }}</span>
        </el-form-item>
      </el-form>
      <div style="text-align: center; margin-top: 16px">
        <el-button type="primary" :loading="submitting" size="large" @click="handlePay">
          确认付款
        </el-button>
      </div>
    </el-card>
  </div>
</template>
