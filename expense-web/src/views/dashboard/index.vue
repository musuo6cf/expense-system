<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="dashboard-container">
    <el-card>
      <template #header>
        <span>欢迎回来，{{ userStore.realName }}</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">
          {{ userStore.userId }}
        </el-descriptions-item>
        <el-descriptions-item label="用户名">
          {{ userStore.username }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ userStore.realName }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag
            v-for="role in userStore.roles"
            :key="role"
            type="primary"
            size="small"
            style="margin-right: 4px"
          >
            {{ role }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-container {
  max-width: 800px;
}
</style>
