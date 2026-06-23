<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getExpenseById, getAttachments } from '@/api/expense'

const route = useRoute()
const expenseId = String(route.params.id)

const expense = ref<any>(null)
const attachments = ref<any[]>([])

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

onMounted(async () => {
  const res: any = await getExpenseById(expenseId)
  expense.value = res.data

  const attRes: any = await getAttachments(expenseId)
  attachments.value = attRes.data || []
})
</script>

<template>
  <div v-if="expense" style="max-width: 900px">
    <el-card style="margin-bottom: 16px">
      <template #header><span>基本信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报销编号">{{ expense.expenseNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ expense.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(expense.status).type">
            {{ getStatusTag(expense.status).text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ expense.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ expense.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ expense.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ expense.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ expense.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报销原因" :span="2">{{ expense.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-bottom: 16px">
      <template #header><span>费用明细</span></template>
      <el-table :data="expense.items" border stripe>
        <el-table-column prop="expenseType" label="费用类型" width="140" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="expenseDate" label="日期" width="140" />
        <el-table-column prop="description" label="说明" />
      </el-table>
    </el-card>

    <el-card style="margin-bottom: 16px">
      <template #header><span>凭证附件</span></template>
      <div v-if="attachments.length === 0" style="color: #909399">暂无附件</div>
      <div v-else v-for="att in attachments" :key="att.id" style="margin-bottom: 8px">
        <span>{{ att.fileName }}</span>
        <span style="color: #909399; margin-left: 8px; font-size: 12px">
          {{ (att.fileSize / 1024).toFixed(1) }}KB · {{ att.uploadTime }}
        </span>
      </div>
    </el-card>

    <el-card>
      <template #header><span>审批记录</span></template>
      <div style="color: #909399">暂无审批记录</div>
    </el-card>
  </div>
</template>
