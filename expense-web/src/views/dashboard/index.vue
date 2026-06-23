<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardStats, getChartData } from '@/api/dashboard'
import * as echarts from 'echarts'

const userStore = useUserStore()

const myCount = ref(0)
const approvalCount = ref(0)
const financeCount = ref(0)
const paidCount = ref(0)
const recentRecords = ref<any[]>([])

const trendChart = ref<HTMLDivElement>()
const pieChart = ref<HTMLDivElement>()

const statusLabels: Record<number, string> = {
  0: '草稿', 1: '待主管审批', 2: '主管驳回', 3: '待财务审核', 4: '财务驳回', 5: '待付款', 6: '已付款'
}

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

async function loadStats() {
  const stats = await getDashboardStats(userStore.roles)
  myCount.value = stats.myCount
  approvalCount.value = stats.approvalCount
  financeCount.value = stats.financeCount
  paidCount.value = stats.paidCount
  recentRecords.value = stats.recentRecords
}

async function loadCharts() {
  const data = await getChartData()

  // Trend chart
  if (trendChart.value) {
    const trend = echarts.init(trendChart.value)
    trend.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.months },
      yAxis: { type: 'value', name: '金额(元)' },
      series: [{
        data: data.amounts,
        type: 'line',
        smooth: true,
        areaStyle: { color: 'rgba(64,158,255,0.15)' },
        itemStyle: { color: '#409eff' }
      }],
      grid: { left: 50, right: 20, top: 20, bottom: 30 }
    })
    window.addEventListener('resize', () => trend.resize())
  }

  // Pie chart
  if (pieChart.value) {
    const pie = echarts.init(pieChart.value)
    const pieData = Object.entries(data.statusMap)
      .filter(([, v]) => v > 0)
      .map(([k, v]) => ({ name: statusLabels[Number(k)] || k, value: v }))
    pie.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['50%', '55%'],
        data: pieData,
        label: { show: true, formatter: '{b}\n{d}%' },
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 }
      }],
      color: ['#909399', '#e6a23c', '#f56c6c', '#e6a23c', '#f56c6c', '#409eff', '#67c23a']
    })
    window.addEventListener('resize', () => pie.resize())
  }
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  loadCharts()
})
</script>

<template>
  <div>
    <!-- Welcome -->
    <div style="margin-bottom: 16px; font-size: 18px; color: #303133">
      欢迎回来，<strong>{{ userStore.realName }}</strong>
    </div>

    <!-- Row 1: Stat cards -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="color: #909399; font-size: 14px">我的报销数</div>
            <div style="font-size: 32px; font-weight: bold; color: #409eff; margin: 8px 0">{{ myCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="color: #909399; font-size: 14px">待主管审批</div>
            <div style="font-size: 32px; font-weight: bold; color: #e6a23c; margin: 8px 0">{{ approvalCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="color: #909399; font-size: 14px">待财务审核</div>
            <div style="font-size: 32px; font-weight: bold; color: #e6a23c; margin: 8px 0">{{ financeCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center">
            <div style="color: #909399; font-size: 14px">已付款</div>
            <div style="font-size: 32px; font-weight: bold; color: #67c23a; margin: 8px 0">{{ paidCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 2 & 3: Charts -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="14">
        <el-card>
          <template #header><span>月报销趋势</span></template>
          <div ref="trendChart" style="height: 320px" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span>报销状态占比</span></template>
          <div ref="pieChart" style="height: 320px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 4: Recent records -->
    <el-card>
      <template #header><span>最近报销记录</span></template>
      <el-table :data="recentRecords" stripe>
        <el-table-column prop="expenseNo" label="报销编号" width="200" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status).type" size="small">
              {{ getStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <div v-if="recentRecords.length === 0" style="text-align: center; padding: 24px; color: #909399">
        暂无报销记录
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.el-row {
  margin-bottom: 16px;
}
</style>
