# 小型企业费用报销管理系统

## 1. 项目简介

**Expense Reimbursement Management System** 是一款面向小型企业的费用报销管理平台，实现企业内部费用申请、审批、付款和归档全流程数字化管理。

传统纸质报销流程存在审批效率低、数据统计困难、流程不透明等问题。本系统通过信息化手段，实现报销申请、主管审批、财务审核、付款确认全过程线上管理，提高财务工作效率，降低管理成本。

**核心价值：**
- 报销流程电子化，告别纸质单据
- 角色权限分离，审批链路清晰
- 费用数据可视化，支持趋势分析
- 预算前置控制，降低企业成本

---

## 2. 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| JDK | 17 | Java 运行环境 |
| Spring Boot | 3.2.0 | 后端框架 |
| Spring MVC | 6.1.1 | Web 开发 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| JWT (jjwt) | 0.12.5 | 身份认证 |
| Spring Security Crypto | 6.2.0 | BCrypt 密码加密 |
| Spring Validation | - | 参数校验 |
| Knife4j | 4.5.0 | API 文档 |
| Lombok | - | 简化实体类 |
| MySQL | 8.3.0 | 关系数据库 |
| Redis | 7.x | 缓存 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue3 | ^3.4.0 | 前端框架 |
| Vite | ^5.1.0 | 构建工具 |
| TypeScript | ^5.3.0 | 类型安全 |
| Element Plus | ^2.5.0 | UI 组件库 |
| Pinia | ^2.1.0 | 状态管理 |
| Vue Router | ^4.3.0 | 路由管理 |
| Axios | ^1.6.0 | HTTP 请求 |
| ECharts | ^6.1.0 | 数据可视化 |

---

## 3. 系统架构

```
┌─────────────────────────────────────┐
│            Browser (Vue3)           │
│   Element Plus + Pinia + ECharts    │
└──────────────┬──────────────────────┘
               │ Axios (JSON/FormData)
               ↓
┌─────────────────────────────────────┐
│      Spring Boot 3 REST API         │
│  Controller → Service → Mapper      │
│  JWT Auth + RBAC + Validation       │
└──────────────┬──────────────────────┘
               │ MyBatis Plus + Redis
               ↓
┌─────────────────────────────────────┐
│         MySQL 8 + Redis 7           │
└─────────────────────────────────────┘
```

**分层设计：**

```
Controller  →  接收请求、参数校验、返回 Result<T>
    ↓
Service     →  核心业务逻辑、事务管理
    ↓
Mapper      →  MyBatis Plus BaseMapper、LambdaQueryWrapper
    ↓
Entity      →  数据库实体映射
```

---

## 4. 功能模块

### 4.1 用户认证模块
- JWT 无状态认证，Token 有效期 2 小时
- BCrypt 密码加密存储
- JwtInterceptor 拦截器统一校验
- 前端 Axios 拦截器自动携带 Token，401 自动跳转登录页

### 4.2 报销管理模块
- 新建报销单（标题、原因、费用明细列表）
- 编辑/删除（仅草稿状态可操作）
- 费用明细动态增删，自动计算总金额
- 报销编号自动生成（`EXP-yyyyMMdd-XXXXXX`）
- 凭证附件上传（jpg/jpeg/png/pdf，最大 10MB）
- 文件按 `uploads/yyyy/MM/` 分目录存储

### 4.3 主管审批模块
- 待审批列表（同部门、非本人、仅 MANAGER 角色）
- 审批通过：待主管审批 → 待财务审核
- 审批驳回：待主管审批 → 主管驳回
- 审批意见填写
- 审批历史时间线展示

### 4.4 财务审核模块
- 待审核列表（全公司、非本人、仅 FINANCE 角色）
- 审核通过：待财务审核 → 待付款
- 审核驳回：待财务审核 → 财务驳回

### 4.5 付款管理模块
- 待付款列表（仅 FINANCE 角色）
- 确认付款：待付款 → 已付款
- 付款方式选择（银行转账/现金/支付宝）
- 银行流水号记录
- 防重复付款校验

### 4.6 统计分析模块
- 统计卡片：我的报销 / 待审批 / 待审核 / 已付款
- 月报销趋势折线图（ECharts）
- 报销状态占比饼图（ECharts）
- 最近报销记录列表

### 4.7 个人中心
- 个人信息展示
- 修改密码（成功后自动登出）

### 4.8 权限模型（RBAC）

| 角色 | 编码 | 权限 |
|------|------|------|
| 系统管理员 | ADMIN | 用户管理 / 部门管理 / 系统配置 |
| 员工 | EMPLOYEE | 新建报销 / 查看本人报销 |
| 部门经理 | MANAGER | 审批本部门报销 |
| 财务人员 | FINANCE | 财务审核 / 确认付款 |

---

## 5. 项目结构

### 后端

```
expense-server/
├── pom.xml
└── src/main/java/com/company/expense/
    ├── ExpenseApplication.java          # 启动类
    ├── common/
    │   ├── Result.java                  # 统一响应 {code, message, data}
    │   └── ResultCode.java              # 状态码枚举
    ├── config/                          # MyBatisPlus / CORS / WebMvc / Password
    ├── controller/                      # 7 个 REST Controller
    │   ├── AuthController.java          # 登录 / 登出
    │   ├── SysUserController.java       # 用户 CRUD + 修改密码
    │   ├── ExpenseController.java       # 报销单 CRUD
    │   ├── AttachmentController.java    # 附件上传 / 查询 / 删除
    │   ├── ApprovalController.java      # 主管审批
    │   ├── FinanceApprovalController.java # 财务审核
    │   └── PaymentController.java       # 付款管理
    ├── service/                         # 7 个接口 + 7 个实现
    ├── mapper/                          # 12 个 MyBatis Plus Mapper
    ├── entity/                          # 12 个数据库实体
    ├── dto/                             # 7 个请求 DTO
    ├── vo/                              # 7 个响应 VO
    ├── exception/                       # 全局异常处理
    ├── interceptor/                     # JWT 拦截器
    └── utils/                           # JWT 工具类
```

### 前端

```
expense-web/
├── package.json
├── vite.config.ts                      # Vite + @ 别名 + API 代理
├── index.html
└── src/
    ├── main.ts                         # 入口：挂载 Element Plus + Pinia + Router
    ├── App.vue                         # 根组件
    ├── api/                            # 7 个 API 模块
    │   ├── auth.ts                     # 登录 / 登出
    │   ├── user.ts                     # 用户信息 / 修改密码
    │   ├── expense.ts                  # 报销 CRUD + 附件
    │   ├── approval.ts                 # 主管审批
    │   ├── finance.ts                  # 财务审核
    │   ├── payment.ts                  # 付款
    │   └── dashboard.ts               # 首页统计
    ├── stores/
    │   └── user.ts                     # Pinia 用户状态（持久化）
    ├── router/
    │   └── index.ts                    # 15 条路由 + 导航守卫
    ├── utils/
    │   └── request.ts                  # Axios 封装（Token / 401 / 错误处理）
    ├── layout/
    │   └── index.vue                   # 角色感知菜单布局
    └── views/
        ├── login/index.vue             # 登录页
        ├── dashboard/index.vue         # 首页（ECharts 统计）
        ├── expense/                    # 报销列表 / 编辑 / 详情
        ├── approval/                   # 待审批列表 / 审批操作
        ├── finance/                    # 财务审核列表 / 审核操作
        ├── payment/                    # 待付款列表 / 确认付款
        └── profile/index.vue           # 个人中心 / 修改密码
```

---

## 6. 数据库设计

数据库名称：`expense_db`，字符集 `utf8mb4`，存储引擎 `InnoDB`。

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `sys_user` | 用户表 | username, password(BCrypt), real_name, department_id, status |
| `sys_role` | 角色表 | role_name, role_code (ADMIN/EMPLOYEE/MANAGER/FINANCE) |
| `sys_permission` | 权限表 | permission_name, permission_code |
| `sys_user_role` | 用户角色关联 | user_id, role_id |
| `sys_role_permission` | 角色权限关联 | role_id, permission_id |
| `sys_department` | 部门表 | department_name, manager_id |
| `expense` | 报销单表 | expense_no, title, total_amount, reason, status |
| `expense_item` | 报销明细表 | expense_id, expense_type, amount, expense_date |
| `approval_record` | 审批记录表 | expense_id, approver_id, approve_result, comment |
| `attachment` | 附件表 | expense_id, file_name, file_path, file_size |
| `operation_log` | 操作日志表 | user_id, operation, method, ip |
| `login_log` | 登录日志表 | username, ip, login_time, status |

**报销状态流转：**

```
草稿(0) → 待主管审批(1) → 待财务审核(3) → 待付款(5) → 已付款(6)
                ↘ 主管驳回(2)      ↘ 财务驳回(4)
```

---

## 7. 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.9+

### 后端启动

```bash
# 1. 创建数据库并导入初始化脚本
mysql -u root -p < expense-server/src/main/resources/db/init.sql

# 2. 修改数据库连接配置（如需要）
# 编辑 expense-server/src/main/resources/application.yml
#   datasource.username / datasource.password

# 3. 启动应用
cd expense-server
mvn spring-boot:run
```

后端运行在 `http://localhost:8080/api`，API 文档：`http://localhost:8080/api/doc.html`

### 前端启动

```bash
# 1. 安装依赖
cd expense-web
npm install

# 2. 启动开发服务器
npm run dev
```

前端运行在 `http://localhost:3000`，自动代理 API 请求到后端。

### 构建部署

```bash
# 后端打包
cd expense-server && mvn package -DskipTests

# 前端构建
cd expense-web && npm run build
# 产出在 expense-web/dist/，部署到 Nginx
```

---

## 8. 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `admin` | `admin123` | ADMIN | 系统管理员 |

初始化脚本额外预置了 4 个部门（总经办、技术部、市场部、财务部）和 4 个默认角色。

---

## 9. API 文档

启动后端后访问 Knife4j 文档：`http://localhost:8080/api/doc.html`

### 接口一览

**认证 (Auth)**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户登出 |

**用户管理 (User)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/page` | 分页查询用户 |
| GET | `/api/user/{id}` | 查询用户详情 |
| POST | `/api/user` | 新增用户 |
| PUT | `/api/user` | 编辑用户 |
| DELETE | `/api/user/{id}` | 删除用户 |
| PUT | `/api/user/password` | 修改密码 |

**报销管理 (Expense)**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/expense` | 新建报销单 |
| GET | `/api/expense/page` | 分页查询报销单 |
| GET | `/api/expense/{id}` | 查询报销单详情 |
| PUT | `/api/expense/{id}` | 编辑报销单 |
| DELETE | `/api/expense/{id}` | 删除报销单 |

**附件管理 (Attachment)**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/attachment/upload` | 上传附件 |
| GET | `/api/attachment/expense/{expenseId}` | 查询附件列表 |
| DELETE | `/api/attachment/{id}` | 删除附件 |

**主管审批 (Approval)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/approval/pending` | 待审批列表 |
| GET | `/api/approval/{expenseId}` | 审批详情 |
| POST | `/api/approval/pass` | 审批通过 |
| POST | `/api/approval/reject` | 审批驳回 |

**财务审核 (Finance)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/finance/pending` | 待审核列表 |
| GET | `/api/finance/{expenseId}` | 审核详情 |
| POST | `/api/finance/pass` | 审核通过 |
| POST | `/api/finance/reject` | 审核驳回 |

**付款管理 (Payment)**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/payment/pending` | 待付款列表 |
| GET | `/api/payment/{expenseId}` | 付款详情 |
| POST | `/api/payment/pay` | 确认付款 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

## 10. 系统截图

### 登录页
用户名密码登录，默认 `admin / admin123`，支持回车提交。

### 首页 Dashboard
- 顶部：4 个统计卡片（我的报销数 / 待主管审批 / 待财务审核 / 已付款）
- 左侧：月报销趋势折线图
- 右侧：报销状态占比饼图
- 底部：最近报销记录列表

### 报销管理
- 报销列表：分页表格，状态标签颜色区分，草稿可编辑/删除
- 新增/编辑：标题 + 原因 + 费用明细动态表格（增删行）+ 附件上传
- 详情：基本信息 + 明细 + 附件 + 审批记录预留区

### 审批中心
- 待审批列表：报销编号 / 标题 / 申请人 / 部门 / 总金额 / 状态
- 审批详情：基本信息 + 审批历史时间线 + 通过/驳回操作

### 财务中心
- 财务审核：待审核列表 + 审核操作（通过/驳回）
- 待付款：付款表单（付款方式 + 流水号）+ 防重复付款

### 个人中心
- 基本信息展示 + 修改密码（成功后自动登出）

---

## 11. 项目特色

### 特色一：完整报销流程
从草稿 → 主管审批 → 财务审核 → 付款确认，全链路闭环管理，每步操作留痕可追溯。

### 特色二：RBAC 权限控制
基于角色的访问控制，菜单和操作按钮根据用户角色动态显示，前端 + 后端双重校验。

### 特色三：单体内聚架构
遵循 KISS/YAGNI 原则，采用单体架构而非微服务，降低部署和维护复杂度，后续可按需拆分。

### 特色四：前后端分离
Vue3 + Spring Boot 完全解耦，前端 Vite 代理开发，后端专注 API，Nginx 统一部署。

### 特色五：完整 API 文档
集成 Knife4j，启动即可在线调试所有接口，无需额外工具。

### 特色六：数据可视化
集成 ECharts，首页提供折线图、饼图等多维度费用统计分析。

---

## 12. 后续规划

### Phase 2 计划

| 模块 | 内容 |
|------|------|
| Redis 缓存 | Token 缓存、热点数据缓存、验证码 |
| Excel 导出 | 报销数据导出为 Excel |
| 消息通知 | WebSocket 实时推送审批通知 |
| 邮件通知 | 审批结果邮件通知 |
| 操作日志 | 审计日志记录和查询 |

### Phase 3 计划

| 模块 | 内容 |
|------|------|
| Flowable 工作流 | 可配置审批流程引擎 |
| 多级审批 | 金额 > 3000 自动进入高级审批 |
| 部门预算控制 | 预算额度设置 + 超额预警 |
| 数据备份 | 自动/手动数据库备份和恢复 |
| Docker 部署 | 容器化部署方案 |

### 暂不考虑

- 微服务改造（Spring Cloud Alibaba）
- 分布式事务
- 消息队列（MQ）
- DDD 领域驱动设计
