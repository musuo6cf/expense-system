<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExpensePage, deleteExpense, submitExpense, exportExpenses } from '@/api/expense'

const router = useRouter()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '草稿', type: 'info' },
  1: { text: '待主管审批', type: 'warning' },
  2: { text: '主管驳回', type: 'danger' },
  3: { text: '待财务审核', type: 'warning' },
  4: { text: '财务驳回', type: 'danger' },
  5: { text: '待付款', type: '' },
  6: { text: '已付款', type: 'success' }
}

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getExpensePage({ page: page.value, size: size.value })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleView(id: number) {
  router.push(`/expense/${id}`)
}

function handleEdit(id: number) {
  router.push(`/expense/edit/${id}`)
}

function handleCreate() {
  router.push('/expense/edit')
}

async function handleSubmit(id: string) {
  try {
    await ElMessageBox.confirm('提交后将无法修改，确定提交吗？', '确认提交', { type: 'warning' })
    await submitExpense(id)
    ElMessage.success('提交成功，请等待主管审批')
    fetchList()
  } catch { /* cancelled */ }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该报销单吗？', '提示', { type: 'warning' })
    await deleteExpense(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch { /* cancelled */ }
}

async function handleExport() {
  try {
    await exportExpenses()
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

function handleSizeChange(val: number) {
  size.value = val
  fetchList()
}

function handlePageChange(val: number) {
  page.value = val
  fetchList()
}

function getStatusTag(status: number) {
  const s = statusMap[status]
  return s || { text: '未知', type: 'info' }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>报销列表</span>
          <div>
            <el-button type="success" @click="handleExport">导出 Excel</el-button>
            <el-button type="primary" @click="handleCreate">新增报销</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="expenseNo" label="报销编号" width="200" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status).type" size="small">
              {{ getStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row.id)">查看</el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="success"
              size="small"
              @click="handleSubmit(row.id)"
            >
              提交
            </el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="warning"
              size="small"
              @click="handleEdit(row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>
