<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserById, updatePassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref<any>({
  phone: '',
  email: '',
  departmentName: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const submitting = ref(false)

onMounted(async () => {
  try {
    const res: any = await getUserById(userStore.userId)
    const d = res.data
    userInfo.value = {
      phone: d.phone || '',
      email: d.email || '',
      departmentName: d.departmentName || ''
    }
  } catch { /* ignore */ }
})

async function handleChangePassword() {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入旧密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  submitting.value = true
  try {
    await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } catch { /* handled by interceptor */ }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <div style="max-width: 600px">
    <el-card style="margin-bottom: 16px">
      <template #header><span>基本信息</span></template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userStore.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ userInfo.departmentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in userStore.roles" :key="r" size="small" style="margin-right: 4px">{{ r }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card>
      <template #header><span>修改密码</span></template>
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleChangePassword">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
