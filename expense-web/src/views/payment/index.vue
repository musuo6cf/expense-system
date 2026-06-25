<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPendingList } from '@/api/payment'
import { exportExpenses } from '@/api/expense'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])

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

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getPendingList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleView(id: number) {
  router.push(`/payment/${id}`)
}

async function handleExport() {
  try {
    await exportExpenses({ status: 5 })
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => fetchData())
</script>

<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>待付款</span>
          <div>
            <el-button type="success" @click="handleExport">导出 Excel</el-button>
            <el-button @click="fetchData">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="expenseNo" label="报销编号" width="200" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status).type" size="small">
              {{ getStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row.id)">
              付款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="list.length === 0 && !loading" style="text-align: center; padding: 40px; color: #909399">
        暂无待付款报销单
      </div>
    </el-card>
  </div>
</template>
