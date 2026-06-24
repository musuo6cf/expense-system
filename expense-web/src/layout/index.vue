<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowDown, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
}
</script>

<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <span class="header-title">费用报销管理系统</span>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleLogout">
          <span class="user-info">
            {{ userStore.realName }} <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="layout-aside">
        <el-menu
          :default-active="router.currentRoute.value.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/dashboard">
            <template #title>首页</template>
          </el-menu-item>
          <el-sub-menu v-if="!userStore.roles.includes('FINANCE')" index="expense">
            <template #title>报销管理</template>
            <el-menu-item index="/expense">报销列表</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="userStore.roles.includes('MANAGER')" index="approval">
            <template #title>审批中心</template>
            <el-menu-item index="/approval">待主管审批</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="userStore.roles.includes('FINANCE')" index="finance">
            <template #title>财务中心</template>
            <el-menu-item index="/finance">财务审核</el-menu-item>
            <el-menu-item index="/payment">待付款</el-menu-item>
            <el-menu-item index="/finance/paid">已付款</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="system">
            <template #title>系统设置</template>
            <el-menu-item index="/profile">个人中心</el-menu-item>
            <el-menu-item v-if="userStore.roles.includes('ADMIN')" index="/user">用户管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
}
.layout-header {
  background-color: #304156;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-title {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  color: #bfcbd9;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.layout-aside {
  background-color: #304156;
}
.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
