<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getExpensePage, exportExpenses } from '@/api/expense'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getExpensePage({ page: page.value, size: size.value, status: 6 })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData())

async function handleExport() {
  try {
    await exportExpenses({ status: 6 })
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}
</script>

<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>已付款记录</span>
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
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="success" size="small">已付款</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <div v-if="list.length === 0 && !loading" style="text-align: center; padding: 40px; color: #909399">
        暂无已付款记录
      </div>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>
