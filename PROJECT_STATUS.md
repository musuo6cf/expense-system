# 项目状态报告

> 最后更新：2026-06-23 | 当前版本：v1.4 | 分支：main

---

## 1. 当前状态总览

| 维度 | 状态 |
|------|:--:|
| 后端编译 | ✅ BUILD SUCCESS (74 源文件) |
| 前端构建 | ✅ BUILD SUCCESS (26 源文件) |
| 数据库 | ✅ 12 张表 + 种子数据 |
| 运行状态 | ✅ 前后端均正常运行 |
| 远程仓库 | ✅ https://github.com/musuo6cf/expense-system.git |

---

## 2. 已实现功能

### 后端 (Spring Boot 3 + MyBatis Plus)

```
expense-server/src/main/java/com/company/expense/
├── controller/    7 个
│   AuthController          登录 / 登出
│   SysUserController       用户 CRUD + 修改密码
│   ExpenseController       报销单 CRUD + 提交
│   AttachmentController    附件上传 / 下载 / 删除
│   ApprovalController      主管审批（通过/驳回）
│   FinanceApprovalController 财务审核（通过/驳回）
│   PaymentController       付款管理
├── service/       7 接口 + 7 实现
├── mapper/        12 个 MyBatis Plus Mapper
├── entity/        12 个实体
├── dto/           7 个请求 DTO
├── vo/            7 个响应 VO
├── config/        5 个配置类
│   JacksonConfig          Long→String 序列化（修复 JS 精度丢失）
│   MyBatisPlusConfig      分页插件
│   CorsConfig             跨域
│   WebMvcConfig           JWT 拦截器 + 下载路径排除
│   PasswordConfig         BCrypt
├── exception/     BusinessException + GlobalExceptionHandler
├── interceptor/   JwtInterceptor
└── utils/         JwtUtil
```

### 前端 (Vue3 + TypeScript + Element Plus)

```
expense-web/src/
├── api/           7 个模块
│   auth.ts        login / logout
│   user.ts        getUserById / updatePassword
│   expense.ts     报销 CRUD + 附件上传/下载/删除 + 提交
│   approval.ts    主管审批 pending/pass/reject/detail
│   finance.ts     财务审核 pending/pass/reject/detail
│   payment.ts     付款 pending/detail/pay
│   dashboard.ts   首页统计（角色感知）
├── views/         8 个页面组
│   login/         登录页
│   dashboard/     首页（财务/非财务双布局 + ECharts）
│   expense/       报销列表 / 编辑（含附件上传）/ 详情
│   approval/      待审批列表 / 审批详情（含附件查看 + 通过/驳回）
│   finance/       财务审核列表 / 审核详情（含附件） / 已付款记录
│   payment/       待付款列表 / 付款详情
│   profile/       个人中心 / 修改密码
├── stores/user.ts Pinia 用户状态（持久化 localStorage）
├── router/        18 条路由 + 导航守卫
├── layout/        角色感知菜单布局
└── utils/request.ts Axios 封装（Token 自动携带 + 401 拦截）
```

### 报销状态流转

```
草稿(0) → 待主管审批(1) → 待财务审核(3) → 待付款(5) → 已付款(6)
                ↘ 主管驳回(2)      ↘ 财务驳回(4)

特殊：主管提交 → 直接进入财务审核(3)，跳过主管审批
```

---

## 3. 测试账号

| 用户名 | 密码 | 角色 | 部门 | 菜单 |
|--------|------|------|------|------|
| `admin` | `admin123` | ADMIN | 总经办 | 首页/报销管理/系统设置 |
| `employee1` | `employee` | EMPLOYEE | 技术部 | 首页/报销管理/系统设置 |
| `manager1` | `manager` | MANAGER | 技术部 | 首页/报销管理/审批中心/系统设置 |
| `finance1` | `finance` | FINANCE | 财务部 | 首页/财务中心/系统设置 |

---

## 4. 已修复的关键问题

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | 点击报销单显示"不存在" | JS Number 精度丢失（Snowflake 19位ID） | Jackson Long→String + 前端 String ID |
| 2 | 首页弹三次"无权限访问" | Dashboard 无条件调用角色受限 API | 根据 roles 判断是否调用 |
| 3 | 没有提交按钮 | 缺失 submitExpense 接口 | 后端新增 POST /api/expense/submit/{id} + 前端按钮 |
| 4 | 新建报销无附件上传 | 创建后跳转列表而非编辑页 | createExpense 返回 ID → 跳转编辑页（含上传） |
| 5 | 主管审批后卡住 | 主管不能审自己的单子 | 主管提交直接进入财务审核(3) |
| 6 | 财务端看不到已付款 | pageExpenses 强制过滤 applicant_id | FINANCE/ADMIN 跳过申请人过滤 |
| 7 | 审批/财务页无附件展示 | 未调用 getAttachments | 审批详情和财务详情均加附件卡片 |
| 8 | 附件无法在线查看 | 无下载端点 | GET /api/attachment/download/{id} + 前端链接 |

---

## 5. 快速启动（下次继续工作）

### 环境
- JDK 17 | Maven 3.9 | Node 18+ | MySQL 8.3 (服务名 MySQL83)

### 启动后端
```powershell
cd "D:\Project\enterprise system\expense-server"
mvn spring-boot:run
# → http://localhost:8080/api
# → API 文档: http://localhost:8080/api/doc.html
```

### 启动前端
```powershell
cd "D:\Project\enterprise system\expense-web"
npm run dev
# → http://localhost:3000
```

### Maven 路径
```
C:\Users\30920\.maven\bin\mvn.cmd
```

---

## 6. 已知待改进项

| # | 项目 | 优先级 |
|---|------|:---:|
| 1 | `dist/` 和 `target/` 未完全被 .gitignore 排除 | 中 |
| 2 | 附件下载无需 Token（已排除拦截器） | 低 |
| 3 | 缺少操作日志记录功能 | 低 |
| 4 | 审批记录在 expense/detail 页未展示真实数据 | 中 |
| 5 | 没有报销单提交后的撤销/撤回功能 | 中 |
| 6 | 首次启动需手动生成 BCrypt 密码哈希 | 低 |

---

## 7. 下一步开发方向

- [ ] 操作日志记录（登录日志 + 操作日志）
- [ ] Excel 导出
- [ ] 部门预算控制（超额预警）
- [ ] WebSocket 消息推送
- [ ] Redis 缓存集成
- [ ] Docker 部署方案
- [ ] Flowable 工作流引擎

---

## 8. Git 历史

```
882eb95 debug3                                    ← HEAD, main, v1.4
e16ec4f feat(dashboard): debug2
032eeca feat(profile): debug 1
...
```

远程：`git@github.com:musuo6cf/expense-system.git`
