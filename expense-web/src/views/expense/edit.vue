<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  getExpenseById, createExpense, updateExpense, submitExpense,
  uploadAttachment, getAttachments, deleteAttachment
} from '@/api/expense'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const expenseId = computed(() => String(route.params.id || ''))

const form = ref({
  title: '',
  reason: ''
})

interface Item {
  expenseType: string
  amount: number
  expenseDate: string
  description: string
}
const items = ref<Item[]>([])

const attachments = ref<any[]>([])
const submitting = ref(false)
const uploadLoading = ref(false)

const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + (Number(item.amount) || 0), 0).toFixed(2)
})

const typeOptions = ['差旅费', '办公费用', '招待费用', '培训费用', '交通费用', '其他费用']

function addItem() {
  items.value.push({ expenseType: '', amount: 0, expenseDate: '', description: '' })
}

function removeItem(index: number) {
  items.value.splice(index, 1)
}

async function loadExpense() {
  const res: any = await getExpenseById(expenseId.value)
  const d = res.data
  form.value.title = d.title
  form.value.reason = d.reason || ''
  if (d.items && d.items.length > 0) {
    items.value = d.items.map((i: any) => ({
      expenseType: i.expenseType,
      amount: i.amount,
      expenseDate: i.expenseDate,
      description: i.description || ''
    }))
  }
}

async function fetchAttachments() {
  const res: any = await getAttachments(expenseId.value)
  attachments.value = res.data || []
}

async function handleUpload(options: any) {
  uploadLoading.value = true
  try {
    await uploadAttachment(expenseId.value, options.file)
    ElMessage.success('上传成功')
    fetchAttachments()
  } finally {
    uploadLoading.value = false
  }
}

async function handleDeleteAttachment(id: number) {
  await deleteAttachment(id)
  ElMessage.success('删除成功')
  fetchAttachments()
}

async function handleSubmit() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (items.value.length === 0) {
    ElMessage.warning('请添加至少一条费用明细')
    return
  }
  for (const item of items.value) {
    if (!item.expenseType || !item.amount || !item.expenseDate) {
      ElMessage.warning('请完善费用明细信息')
      return
    }
  }

  const data = {
    title: form.value.title,
    reason: form.value.reason,
    items: items.value.map(i => ({
      expenseType: i.expenseType,
      amount: Number(i.amount),
      expenseDate: i.expenseDate,
      description: i.description
    }))
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateExpense(expenseId.value, data)
      ElMessage.success('保存成功')
    } else {
      const res: any = await createExpense(data)
      const newId = res.data
      ElMessage.success('创建成功，请上传凭证附件')
      router.push(`/expense/edit/${newId}`)
      return
    }
    router.push('/expense')
  } finally {
    submitting.value = false
  }
}

async function handleSubmitAndSend() {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (items.value.length === 0) {
    ElMessage.warning('请添加至少一条费用明细')
    return
  }
  for (const item of items.value) {
    if (!item.expenseType || !item.amount || !item.expenseDate) {
      ElMessage.warning('请完善费用明细信息')
      return
    }
  }

  const data = {
    title: form.value.title,
    reason: form.value.reason,
    items: items.value.map(i => ({
      expenseType: i.expenseType,
      amount: Number(i.amount),
      expenseDate: i.expenseDate,
      description: i.description
    }))
  }

  submitting.value = true
  try {
    await updateExpense(expenseId.value, data)
    await submitExpense(expenseId.value)
    ElMessage.success('已保存并提交，请等待主管审批')
    router.push('/expense')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadExpense()
    fetchAttachments()
  } else {
    addItem()
  }
})
</script>

<template>
  <div>
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑报销单' : '新增报销单' }}</span>
      </template>

      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入报销标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="报销原因">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入报销原因"
            maxlength="500"
          />
        </el-form-item>
      </el-form>

      <h4 style="margin: 16px 0">费用明细</h4>
      <el-table :data="items" border>
        <el-table-column label="费用类型" width="160">
          <template #default="{ row, $index }">
            <el-select v-model="row.expenseType" placeholder="选择类型">
              <el-option
                v-for="t in typeOptions"
                :key="t"
                :label="t"
                :value="t"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="140">
          <template #default="{ row }">
            <el-input-number
              v-model="row.amount"
              :min="0"
              :precision="2"
              placeholder="0.00"
            />
          </template>
        </el-table-column>
        <el-table-column label="日期" width="160">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.expenseDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.description" placeholder="说明" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeItem($index)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 12px">
        <el-button type="primary" link @click="addItem">
          <el-icon><Plus /></el-icon> 新增明细
        </el-button>
        <span style="float: right; font-size: 16px; font-weight: bold">
          合计：¥{{ totalAmount }}
        </span>
      </div>

      <!-- Attachments (only in edit mode) -->
      <div v-if="isEdit" style="margin-top: 24px">
        <h4>凭证附件</h4>
        <el-upload
          :http-request="handleUpload"
          :before-upload="(f: any) => {
            const ext = f.name.split('.').pop()?.toLowerCase()
            const allowed = ['jpg','jpeg','png','pdf']
            if (!ext || !allowed.includes(ext)) { ElMessage.error('仅支持 jpg/jpeg/png/pdf'); return false }
            if (f.size > 10*1024*1024) { ElMessage.error('文件不能超过10MB'); return false }
            return true
          }"
          :show-file-list="false"
          accept=".jpg,.jpeg,.png,.pdf"
        >
          <el-button :loading="uploadLoading">上传附件</el-button>
        </el-upload>

        <div v-if="attachments.length > 0" style="margin-top: 8px">
          <el-tag
            v-for="att in attachments"
            :key="att.id"
            closable
            style="margin-right: 8px; margin-bottom: 4px"
            @close="handleDeleteAttachment(att.id)"
          >
            {{ att.fileName }}
          </el-tag>
        </div>
      </div>

      <div style="margin-top: 24px; text-align: center">
        <el-button @click="router.push('/expense')">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
        <el-button
          v-if="isEdit"
          type="success"
          :loading="submitting"
          @click="handleSubmitAndSend"
        >
          保存并提交
        </el-button>
      </div>
    </el-card>
  </div>
</template>
