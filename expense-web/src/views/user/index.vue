<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserPage, deleteUser } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '禁用', type: 'danger' },
  1: { text: '启用', type: 'success' }
}

function getStatusTag(status: number) {
  return statusMap[status] || { text: '未知', type: 'info' }
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getUserPage(page.value, size.value, keyword.value || undefined)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handlePageChange(p: number) {
  page.value = p
  fetchData()
}

function handleSizeChange(s: number) {
  size.value = s
  page.value = 1
  fetchData()
}

function handleCreate() {
  router.push('/user/edit')
}

function handleEdit(id: string) {
  router.push(`/user/edit/${id}`)
}

async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm('确定删除该用户吗？', '确认删除', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* handled by interceptor */ }
}

onMounted(() => fetchData())
</script>

<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>用户管理</span>
          <el-button type="primary" @click="handleCreate">新建用户</el-button>
        </div>
      </template>

      <!-- Search -->
      <div style="margin-bottom: 16px; display: flex; gap: 8px">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名或姓名"
          style="width: 260px"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.roleName" size="small">{{ row.roleName }}</el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="部门" width="120">
          <template #default="{ row }">
            {{ row.departmentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status).type" size="small">
              {{ getStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>

      <div v-if="list.length === 0 && !loading" style="text-align: center; padding: 40px; color: #909399">
        暂无用户
      </div>
    </el-card>
  </div>
</template>
