<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserById, createUser, updateUser, getDepartmentList, getRoleList } from '@/api/user'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const userId = computed(() => String(route.params.id || ''))

const form = ref({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  icbcCardNo: '',
  departmentId: null as number | null,
  roleId: null as number | null
})

const departments = ref<any[]>([])
const roles = ref<any[]>([])
const submitting = ref(false)

async function fetchMeta() {
  const [deptRes, roleRes] = await Promise.all([getDepartmentList(), getRoleList()])
  departments.value = (deptRes as any).data || []
  roles.value = ((roleRes as any).data || []).filter((r: any) => r.roleCode !== 'ADMIN')
}

async function fetchUser() {
  const res: any = await getUserById(userId.value as any)
  const d = res.data
  form.value = {
    username: d.username,
    password: '',
    realName: d.realName || '',
    phone: d.phone || '',
    email: d.email || '',
    icbcCardNo: d.icbcCardNo || '',
    departmentId: d.departmentId || null,
    roleId: d.roleId || null
  }
}

async function handleSubmit() {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!isEdit.value && !form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!form.value.realName) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!form.value.roleId) {
    ElMessage.warning('请选择角色')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser({ id: userId.value, ...form.value })
      ElMessage.success('更新成功')
    } else {
      await createUser(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/user')
  } catch { /* handled by interceptor */ }
  finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchMeta()
  if (isEdit.value) {
    await fetchUser()
  }
})
</script>

<template>
  <div style="max-width: 600px">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑用户' : '新建用户' }}</span>
      </template>

      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" maxlength="50" />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="密码" required>
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" maxlength="50" />
        </el-form-item>

        <el-form-item label="姓名" required>
          <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="50" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>

        <el-form-item label="工行卡号">
          <el-input v-model="form.icbcCardNo" placeholder="请输入工商银行卡号" maxlength="30" />
        </el-form-item>

        <el-form-item label="部门">
          <el-select v-model="form.departmentId" placeholder="请选择部门" style="width: 100%" clearable>
            <el-option
              v-for="d in departments"
              :key="d.id"
              :label="d.departmentName"
              :value="d.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="角色" required>
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="r in roles"
              :key="r.id"
              :label="r.roleName"
              :value="r.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
          <el-button @click="router.push('/user')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
