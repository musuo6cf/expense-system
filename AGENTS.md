# AGENTS.md

# 小型企业费用报销管理系统 AI 开发规范

Version: 2.1

------

# 项目简介

本项目为 **小型企业费用报销管理系统（Expense Reimbursement Management System）**。

目标是实现企业内部费用申请、审批、付款和归档流程的数字化管理。

采用前后端分离架构：

```text
Vue3 + Element Plus
        ↓
Spring Boot 3 + MyBatis Plus
        ↓
MySQL 8
```

------

# 技术栈

## Frontend

- Vue3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

## Backend

- JDK17
- Spring Boot 3.x
- Spring MVC
- MyBatis Plus
- JWT
- Lombok
- Validation
- Logback

## Database

- MySQL 8

------

# AI Agent 工作原则

## 1. 优先可运行

优先生成能够直接运行的代码。

禁止：

- 输出伪代码
- 留 TODO
- 省略关键逻辑

生成代码必须完整。

------

## 2. 保持简单

遵循：

- KISS
- YAGNI

避免：

- 过度设计
- 微服务
- DDD
- 复杂抽象

当前项目采用单体架构。

------

## 3. 优先修改现有代码

禁止：

无意义地重新生成整个文件。

优先：

- 修改已有方法
- 保持原结构
- 最小改动

------

## 4. 每次只完成一个任务

一次只处理：

- 一个模块
- 一个页面
- 一个接口

禁止同时修改多个功能。

------

## 5. 保持代码一致性

遵循已有：

- 命名规范
- 文件结构
- 编码风格

禁止随意改变架构。

------

# 项目目录

## Frontend

```text
expense-web/
├── package.json
├── vite.config.ts
├── index.html
└── src
    ├── api           # 9 个 API 模块
    ├── assets
    ├── components
    ├── layout        # 角色感知菜单布局
    ├── router        # 21 条路由 + 导航守卫
    ├── stores        # Pinia 用户状态（持久化）
    ├── utils         # Axios 封装
    └── views
        ├── login
        ├── dashboard
        ├── expense
        ├── approval
        ├── finance
        ├── payment
        ├── profile
        └── user
```

## Backend

```text
expense-server/
├── pom.xml
└── src/main/java/com/company/expense
    ├── common        # Result / ResultCode
    ├── config        # MyBatisPlus / CORS / WebMvc / Password / Jackson
    ├── controller    # 10 个 Controller
    ├── service       # 9 个接口
    ├── service.impl  # 9 个实现
    ├── mapper        # 12 个 Mapper
    ├── entity        # 12 个 Entity
    ├── dto           # 8 个 DTO
    ├── vo            # 10 个 VO
    ├── exception     # BusinessException / GlobalExceptionHandler
    ├── interceptor   # JwtInterceptor
    ├── utils         # JwtUtil
    └── ExpenseApplication
```

------

# 数据库规范

数据库：

```text
expense_db
```

表名：

```text
snake_case
```

例如：

```text
sys_user
sys_role
expense
approval_record
```

字段：

```text
snake_case
```

主键：

```java
@TableId(type = IdType.ASSIGN_ID)
private Long id;
```

逻辑删除：

```java
@TableLogic
private Integer deleted;
```

------

# 后端开发规范

## Controller

仅负责：

- 接收请求
- 参数校验
- 返回结果

禁止编写业务逻辑。

返回统一使用：

```java
Result<T>
```

## Service

负责核心业务逻辑。

事务：

```java
@Transactional(rollbackFor = Exception.class)
```

## Mapper

优先使用 LambdaQueryWrapper / LambdaUpdateWrapper，避免直接写 SQL。

## Entity / DTO / VO

- Entity：数据库映射，禁止直接接收请求或返回
- DTO：请求参数
- VO：响应数据

## Jackson 配置

Snowflake 生成的 Long ID 必须序列化为 String，防止前端 JavaScript Number 精度丢失：

```java
// JacksonConfig.java
builder.serializerByType(Long.class, ToStringSerializer.instance);
```

前端统一使用 String 处理 ID。

## 分页查询权限

- 普通用户：只查自己的数据 `wrapper.eq(applicantId, userId)`
- FINANCE / ADMIN：查看全公司数据（跳过申请人过滤）

------

# 前端开发规范

使用：

```vue
<script setup lang="ts">
```

状态管理：Pinia

请求统一通过 `src/api/`，禁止页面直接 axios。

路由参数 ID 使用 String 类型（Long 精度问题）：

```typescript
const expenseId = String(route.params.id)  // 不是 Number()
```

------

# API 规范

统一前缀：`/api`

RESTful 风格：GET / POST / PUT / DELETE

统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

--- 已实现 API 全览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| Auth | POST | /api/auth/login | 登录 |
| Auth | POST | /api/auth/logout | 登出 |
| User | GET | /api/user/page | 分页用户 |
| User | GET | /api/user/{id} | 用户详情 |
| User | POST | /api/user | 新增用户 |
| User | PUT | /api/user | 编辑用户 |
| User | DELETE | /api/user/{id} | 删除用户 |
| User | PUT | /api/user/password | 修改密码 |
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
| Approval | GET | /api/approval/{expenseId} | 审批详情 |
| Approval | POST | /api/approval/pass | 审批通过 |
| Approval | POST | /api/approval/reject | 审批驳回 |
| Finance | GET | /api/finance/pending | 待审核列表 |
| Finance | GET | /api/finance/{expenseId} | 审核详情 |
| Finance | POST | /api/finance/pass | 审核通过 |
| Finance | POST | /api/finance/reject | 审核驳回 |
| Payment | GET | /api/payment/pending | 待付款列表 |
| Payment | GET | /api/payment/{expenseId} | 付款详情 |
| Payment | POST | /api/payment/pay | 确认付款 |
| Department | GET | /api/department/list | 部门列表 |
| Role | GET | /api/role/list | 角色列表 |
| BatchReview | POST | /api/finance/batch-review/preview | 批量审批预检查 |
| BatchReview | POST | /api/finance/batch-review/execute | 批量审批执行 |

------

# 批量审批规则

费用标准（任意明细超出标准 → 驳回，全部符合 → 保留待人工审核）：

| 费用类型 | 上限 |
|----------|:--:|
| 餐饮费用 | 600 |
| 办公费用 | 1000 |
| 招待费用 | 600 |
| 住宿费用 | 1000 |
| 交通费用 | 2000 |
| 培训费用 | 500 |
| 其他费用 | 500 |

------

# 权限模型

采用 RBAC。

角色：

| 编码 | 名称 | 权限 |
|------|------|------|
| ADMIN | 系统管理员 | 查看全公司数据 |
| EMPLOYEE | 员工 | 新建报销 / 查看本人报销 |
| MANAGER | 部门经理 | 审批本部门报销（不能审自己） |
| FINANCE | 财务人员 | 财务审核 / 付款 / 查看全公司 |

------

# 报销流程

```text
员工提交 → 待主管审批(1) → 待财务审核(3) → 待付款(5) → 已付款(6)
                 ↘ 主管驳回(2)      ↘ 财务驳回(4)

主管提交 → 直接进入待财务审核(3)，跳过主管审批
```

------

# 开发顺序

Phase 1 ✅ 基础框架 + JWT + 用户管理

Phase 2 ✅ 报销模块 (CRUD + 附件上传/下载 + 提交)

Phase 3 ✅ 审批模块 (主管审批 + 财务审核)

Phase 4 ✅ 付款模块 (付款确认 + 防重复)

Phase 5 ✅ 统计模块 (ECharts + 角色感知仪表盘)

Phase 6 🔜 系统模块 (操作日志 / Excel 导出)

Phase 7 🔜 增强 (Redis 缓存 / WebSocket / Docker)

------

# 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin1 | ADMIN |
| employee1 | employee | EMPLOYEE |
| manager1 | manager | MANAGER |
| finance1 | finance | FINANCE |

------

# 环境信息

- MySQL 数据库：expense_db（账号 root / 151158，服务名 MySQL83）
- Maven 路径：C:\Users\30920\.maven\bin\mvn.cmd
- 后端端口：8080（context-path: /api）
- 前端端口：3000（Vite 代理 /api → localhost:8080）
- 上传目录：expense-server/uploads/yyyy/MM/

------

# 关键约定

1. Long ID 必须序列化为 String（JacksonConfig）
2. 前端 ID 必须用 String 类型
3. FINANCE/ADMIN 的 pageExpenses 不加 applicantId 过滤
4. 主管提交直接进入待财务审核
5. 附件下载路径已排除 JWT 拦截
6. Dashboard 调用受限 API 前需判断角色
7. 创建报销后跳转编辑页（非列表页），以便用户上传附件
8. 不要修改已有表结构
9. 修改文件数遵守用户指定的上限
