# 项目状态报告

> 最后更新：2026-06-25 | 当前版本：v1.7 | 分支：main

---

## 1. 当前状态总览

| 维度 | 状态 |
|------|:--:|
| 后端编译 | ✅ BUILD SUCCESS (79 源文件) |
| 前端构建 | ✅ BUILD SUCCESS (31 源文件) |
| 数据库 | ✅ 12 张表 + 种子数据 + 测试报销数据 |
| 运行状态 | ✅ 前后端均正常运行 |
| 远程仓库 | ✅ https://github.com/musuo6cf/expense-system.git |

---

## 2. 已实现功能

### 后端 (Spring Boot 3 + MyBatis Plus)

```
expense-server/src/main/java/com/company/expense/
├── controller/    10 个
│   AuthController              登录 / 登出
│   SysUserController           用户 CRUD + 修改密码（含角色分配）
│   ExpenseController           报销单 CRUD + 提交
│   AttachmentController        附件上传 / 下载 / 删除
│   ApprovalController          主管审批（通过/驳回）
│   FinanceApprovalController   财务审核（通过/驳回）
│   PaymentController           付款管理
│   BatchReviewController       财务批量审批（预检查 + 执行）
│   SysDepartmentController     部门列表
│   SysRoleController           角色列表
├── service/       9 接口 + 9 实现
│   + BatchReviewService        批量审批服务
├── mapper/        12 个 MyBatis Plus Mapper
├── entity/        12 个实体
├── dto/           8 个请求 DTO
│   + BatchReviewExecuteDTO
├── vo/            10 个响应 VO
│   + BatchReviewPreviewVO / BatchReviewResultVO
├── config/        5 个配置类
├── exception/     BusinessException + GlobalExceptionHandler（含重复键处理）
├── interceptor/   JwtInterceptor
└── utils/         JwtUtil
```

### 前端 (Vue3 + TypeScript + Element Plus)

```
expense-web/src/
├── api/           9 个模块
│   auth.ts / user.ts / expense.ts / approval.ts
│   finance.ts / payment.ts / dashboard.ts
│   finance-batch.ts       批量审批 API
├── components/    1 个
│   BatchReviewDialog.vue  批量审批弹窗
├── views/         9 个页面组
│   login/ / dashboard/ / expense/ / approval/
│   finance/ / payment/ / profile/
│   user/                  用户管理列表 / 编辑（ADMIN only）
├── stores/user.ts
├── router/        21 条路由
├── layout/        角色感知菜单（含用户管理入口）
└── utils/request.ts
```

### API 全览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| Auth | POST | /api/auth/login | 登录 |
| Auth | POST | /api/auth/logout | 登出 |
| User | GET | /api/user/page | 分页用户（含角色信息） |
| User | GET | /api/user/{id} | 用户详情（含角色） |
| User | POST | /api/user | 新增用户（含角色分配） |
| User | PUT | /api/user | 编辑用户（含角色更新） |
| User | DELETE | /api/user/{id} | 删除用户（含角色清理） |
| User | PUT | /api/user/password | 修改密码 |
| Department | GET | /api/department/list | 部门列表 |
| Role | GET | /api/role/list | 角色列表 |
| Expense | POST | /api/expense | 新建报销 |
| Expense | GET | /api/expense/page | 分页查询 |
| Expense | GET | /api/expense/{id} | 报销详情 |
| Expense | PUT | /api/expense/{id} | 编辑报销 |
| Expense | DELETE | /api/expense/{id} | 删除报销 |
| Expense | POST | /api/expense/submit/{id} | 提交报销 |
| Attachment | POST | /api/attachment/upload | 上传附件 |
| Attachment | GET | /api/attachment/expense/{expenseId} | 附件列表 |
| Attachment | DELETE | /api/attachment/{id} | 删除附件 |
| Attachment | GET | /api/attachment/download/{id} | 下载/预览 |
| Approval | GET | /api/approval/pending | 待审批列表 |
| Approval | GET | /api/approval/{expenseId} | 审批详情（含费用明细） |
| Approval | POST | /api/approval/pass | 审批通过 |
| Approval | POST | /api/approval/reject | 审批驳回 |
| Finance | GET | /api/finance/pending | 待审核列表 |
| Finance | GET | /api/finance/{expenseId} | 审核详情（含费用明细） |
| Finance | POST | /api/finance/pass | 审核通过 |
| Finance | POST | /api/finance/reject | 审核驳回 |
| BatchReview | POST | /api/finance/batch-review/preview | 批量审批预检查 |
| BatchReview | POST | /api/finance/batch-review/execute | 批量审批执行（仅驳回超标单） |
| Payment | GET | /api/payment/pending | 待付款列表 |
| Payment | GET | /api/payment/{expenseId} | 付款详情（含申请人卡号） |
| Payment | POST | /api/payment/pay | 确认付款（固定工行转账） |
| **Export** | **GET** | **/api/expense/export** | **导出报销为 Excel（支持 status 筛选）** |

### 批量审批规则

| 费用类型 | 上限 |
|----------|:--:|
| 餐饮费用 | 600 |
| 办公费用 | 1000 |
| 招待费用 | 600 |
| 住宿费用 | 1000 |
| 交通费用 | 2000 |
| 培训费用 | 500 |
| 其他费用 | 500 |

- 任意明细超出标准 → 驳回
- 全部明细符合标准 → 保留待财务人工审核（不自动通过）

---

## 3. 测试账号

| 用户名 | 密码 | 角色 | 部门 |
|--------|------|------|------|
| `admin` | `admin1` | ADMIN | 总经办 |
| `employee1` | `employee` | EMPLOYEE | 技术部 |
| `employee2` | — | EMPLOYEE | 技术部 |
| `manager1` | `manager` | MANAGER | 技术部 |
| `manager2` | — | MANAGER | 市场部 |
| `finance1` | `finance` | FINANCE | 财务部 |

---

## 4. 快速启动（下次继续工作）

### 环境
- JDK 17 | Maven 3.9 | Node 18+ | MySQL 8.3 (服务名 MySQL83)

### 启动后端
```powershell
cd "D:\Project\enterprise system\expense-server"
C:\Users\30920\.maven\bin\mvn.cmd spring-boot:run
# → http://localhost:8080/api
```

### 启动前端
```powershell
cd "D:\Project\enterprise system\expense-web"
npm run dev
# → http://localhost:3000
```

### 数据库
- MySQL 账号：root / 151158
- 数据库名：expense_db
- 初始化：`init.sql` 建表 + 种子数据
- 迁移：`V2__add_icbc_card_no.sql` 工行卡号字段

---

## 5. 关键约定

1. Long ID 必须序列化为 String（JacksonConfig），前端 ID 用 String
2. FINANCE/ADMIN 的 pageExpenses 不加 applicantId 过滤
3. 主管提交直接进入待财务审核，跳过主管审批
4. 附件下载路径已排除 JWT 拦截
5. 批量审批只驳回超标单，合规单需财务人工审核
6. 付款仅支持"向工商银行卡号转账"，自动显示申请人卡号
7. 不要修改已有表结构（新列通过迁移脚本添加）

---

## 6. 已知待改进项

| # | 项目 | 优先级 |
|---|------|:---:|
| 1 | 缺少操作日志记录功能 | 中 |
| 2 | 审批记录在 expense/detail 页未展示真实数据 | 中 |
| 3 | 没有报销单提交后的撤销/撤回功能 | 中 |
| 4 | 批量审批不处理通过（需人工逐笔审核） | 设计如此 |
| 5 | ~~Excel 导出~~ | ✅ 已完成 |
| 6 | WebSocket 消息推送 | 低 |

---

## 7. Git 标签历史

```
v1.7  Excel export (5 pages: expense list / approval / finance review / payment / paid)
v1.6  batch finance review / user management / ICBC card
v1.5  ICBC card / payment simplification / expense items
v1.4  debug3 (dashboard/profile fixes)
...
```

远程：`https://github.com/musuo6cf/expense-system.git`
