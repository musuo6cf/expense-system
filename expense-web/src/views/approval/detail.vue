<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApprovalDetail, passApproval, rejectApproval } from '@/api/approval'
import { getAttachments, downloadAttachment } from '@/api/expense'

const route = useRoute()
const router = useRouter()
const expenseId = String(route.params.id)

const detail = ref<any>(null)
const attachments = ref<any[]>([])
const comment = ref('')
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
  const res: any = await getApprovalDetail(expenseId)
  detail.value = res.data
  const attRes: any = await getAttachments(expenseId)
  attachments.value = attRes.data || []
}

async function handlePass() {
  try {
    await ElMessageBox.confirm('确定审批通过吗？', '确认审批', { type: 'success' })
  } catch {
    return
  }
  submitting.value = true
  try {
    await passApproval(expenseId, comment.value)
    ElMessage.success('审批通过')
    router.push('/approval')
  } finally {
    submitting.value = false
  }
}

async function handleReject() {
  try {
    await ElMessageBox.confirm('确定驳回该报销单吗？', '确认驳回', { type: 'warning' })
  } catch {
    return
  }
  submitting.value = true
  try {
    await rejectApproval(expenseId, comment.value)
    ElMessage.success('已驳回')
    router.push('/approval')
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

    <!-- Expense Items -->
    <el-card style="margin-bottom: 16px">
      <template #header><span>费用明细</span></template>
      <el-table :data="detail.items" border stripe>
        <el-table-column prop="expenseType" label="费用类型" width="140" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="expenseDate" label="日期" width="140" />
        <el-table-column prop="description" label="说明" />
      </el-table>
    </el-card>

    <!-- Attachments -->
    <el-card style="margin-bottom: 16px">
      <template #header><span>凭证附件</span></template>
      <div v-if="attachments.length === 0" style="color: #909399">暂无附件</div>
      <div v-else v-for="att in attachments" :key="att.id" style="margin-bottom: 8px">
        <el-button link type="primary" size="small" @click="downloadAttachment(att.id)">
          {{ att.fileName }}
        </el-button>
        <span style="color: #909399; font-size: 12px">
          {{ (att.fileSize / 1024).toFixed(1) }}KB · {{ att.uploadTime }}
        </span>
      </div>
    </el-card>

    <!-- Action (only for PENDING_MANAGER status) -->
    <el-card v-if="detail.status === 1" style="margin-bottom: 16px">
      <template #header><span>审批操作</span></template>
      <el-input
        v-model="comment"
        type="textarea"
        :rows="3"
        placeholder="请输入审批意见（选填）"
        maxlength="500"
        show-word-limit
      />
      <div style="margin-top: 16px; text-align: center">
        <el-button
          type="success"
          :loading="submitting"
          @click="handlePass"
        >
          通 过
        </el-button>
        <el-button
          type="danger"
          :loading="submitting"
          style="margin-left: 24px"
          @click="handleReject"
        >
          驳 回
        </el-button>
      </div>
    </el-card>
  </div>
</template>
