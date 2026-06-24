<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { previewBatchReview, executeBatchReview } from '@/api/finance-batch'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ 'update:visible': [value: boolean]; done: [] }>()

const loading = ref(false)
const executing = ref(false)

const passList = ref<any[]>([])
const rejectList = ref<any[]>([])

async function fetchPreview() {
  loading.value = true
  try {
    const res: any = await previewBatchReview()
    const data = res.data
    passList.value = data.passList || []
    rejectList.value = data.rejectList || []
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}

async function handleExecute() {
  if (rejectList.value.length === 0) {
    ElMessage.info('没有需要驳回的报销单')
    return
  }
  executing.value = true
  try {
    const rejectIds = rejectList.value.map((item: any) => item.expenseId)
    const res: any = await executeBatchReview({ rejectIds })
    const data = res.data
    ElMessage.success(
      `批量审批完成\n总数：${data.total}\n驳回：${data.rejectCount}`
    )
    emit('done')
    closeDialog()
  } catch {
    /* handled by interceptor */
  } finally {
    executing.value = false
  }
}

function closeDialog() {
  emit('update:visible', false)
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      passList.value = []
      rejectList.value = []
      fetchPreview()
    }
  }
)
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="批量审批预检查结果"
    width="650px"
    :close-on-click-modal="false"
    @close="closeDialog"
  >
    <div v-loading="loading" style="min-height: 120px">
      <template v-if="!loading">
        <!-- Pass List (informational, not auto-approved) -->
        <div style="margin-bottom: 20px">
          <h4 style="color: #409eff; margin: 0 0 10px 0">
            合规 · 待人工审核（{{ passList.length }}）
          </h4>
          <div
            v-for="item in passList"
            :key="item.expenseId"
            style="padding: 4px 0; color: #606266"
          >
            ✓ {{ item.expenseNo }} {{ item.title }}
          </div>
          <div v-if="passList.length === 0" style="color: #909399">无</div>
        </div>

        <!-- Reject List (will be auto-rejected) -->
        <div style="margin-bottom: 20px">
          <h4 style="color: #f56c6c; margin: 0 0 10px 0">
            超标 · 批量驳回（{{ rejectList.length }}）
          </h4>
          <div v-if="rejectList.length === 0" style="color: #909399">无</div>
          <div
            v-for="item in rejectList"
            :key="item.expenseId"
            style="padding: 8px 0; border-bottom: 1px solid #ebeef5"
          >
            <div style="color: #f56c6c">
              ✗ {{ item.expenseNo }} {{ item.title }}
            </div>
            <div style="color: #909399; font-size: 13px; margin-top: 4px">
              原因：
            </div>
            <ul style="margin: 4px 0 0 0; padding-left: 20px; color: #909399; font-size: 13px">
              <li v-for="(reason, idx) in item.reasons" :key="idx">{{ reason }}</li>
            </ul>
          </div>
        </div>
      </template>
    </div>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" :loading="executing" :disabled="rejectList.length === 0" @click="handleExecute">
        确认执行
      </el-button>
    </template>
  </el-dialog>
</template>
