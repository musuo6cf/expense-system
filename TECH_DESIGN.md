# 小型企业费用报销管理系统技术设计文档



Version：1.0
	Author：项目组
	Date：2026-06-23

------

# 1. 项目概述

## 1.1 项目名称

小型企业费用报销管理系统（Expense Reimbursement Management System）

## 1.2 项目背景

传统企业费用报销流程依赖纸质单据和人工审批，存在流程复杂、审批效率低、数据统计困难等问题。

本系统旨在实现企业费用报销流程的信息化管理，支持员工在线提交报销申请、领导审批、财务审核以及费用统计分析，提高企业财务管理效率。

## 1.3 技术目标

- 前后端分离架构
- 支持 RESTful API
- 实现 RBAC 权限控制
- 支持 JWT 身份认证
- 支持文件上传
- 支持分页查询和条件搜索
- 支持系统日志记录
- 便于后期扩展和部署

------

# 2. 系统架构设计

采用 B/S（Browser/Server）架构，前后端分离模式。

```
Browser
    │
Vue3 + Element Plus
    │
Axios
    │
Spring Boot REST API
    │
Service
    │
MyBatis Plus
    │
MySQL + Redis
```

系统整体采用分层设计：

```
Presentation Layer（表现层）
        ↓
Controller Layer（控制层）
        ↓
Service Layer（业务层）
        ↓
Persistence Layer（持久层）
        ↓
Database Layer（数据库层）
```

------

# 3. 技术栈选型

## 3.1 前端技术

| 技术         | 版本   | 作用         |
| ------------ | ------ | ------------ |
| Vue3         | Latest | 前端框架     |
| Vite         | Latest | 项目构建工具 |
| Element Plus | Latest | UI组件库     |
| Vue Router   | 4.x    | 路由管理     |
| Pinia        | Latest | 状态管理     |
| Axios        | Latest | HTTP请求     |
| ECharts      | Latest | 数据可视化   |

### 选型原因

- Vue3 响应式性能优秀；
- Element Plus 适用于后台管理系统；
- Pinia 轻量、易维护；
- Vite 编译速度快。

------

## 3.2 后端技术

| 技术              | 版本  | 作用       |
| ----------------- | ----- | ---------- |
| JDK               | 17    | 开发环境   |
| Spring Boot       | 3.x   | 后端框架   |
| Spring MVC        | 6.x   | Web开发    |
| MyBatis Plus      | 3.5.x | ORM框架    |
| Spring Validation | -     | 参数校验   |
| JWT               | -     | 身份认证   |
| Lombok            | -     | 简化实体类 |
| Logback           | -     | 日志管理   |
| Knife4j           | 4.x   | API文档    |

### 选型原因

- Spring Boot 开发效率高；
- MyBatis Plus 提供自动 CRUD；
- JWT 实现无状态认证；
- Knife4j 方便接口测试。

------

## 3.3 数据存储

| 技术  | 版本  | 用途       |
| ----- | ----- | ---------- |
| MySQL | 8.3.0 | 关系数据库 |
| Redis | 7.x   | 缓存       |

### MySQL

用于存储：

- 用户信息
- 部门信息
- 报销单信息
- 审批记录
- 文件附件信息

### Redis

用于：

- Token缓存
- 验证码缓存
- 热点数据缓存

------

# 4. 系统模块划分

系统分为六大模块。

## 4.1 用户认证模块

功能：

- 登录
- 注销
- Token刷新
- 修改密码

角色：

- 员工
- 部门经理
- 财务人员
- 系统管理员

------

## 4.2 报销管理模块

功能：

- 新建报销单
- 保存草稿
- 提交申请
- 修改报销单
- 删除报销单
- 查询报销记录

报销类别：

- 差旅费
- 办公费用
- 招待费用
- 培训费用
- 交通费用
- 其他费用

------

## 4.3 审批模块

功能：

- 查看待审批列表
- 审批通过
- 审批拒绝
- 填写审批意见

审批流程：

```
员工
 ↓
部门经理审批
 ↓
财务审核
 ↓
完成报销
```

------

## 4.4 财务管理模块

功能：

- 财务审核
- 确认付款
- 查看付款记录

------

## 4.5 系统管理模块

功能：

### 用户管理

- 新增用户
- 修改用户
- 删除用户

### 角色管理

- 分配角色
- 权限配置

### 部门管理

- 部门维护

------

## 4.6 数据统计模块

功能：

- 月度费用统计
- 分类费用统计
- 报销趋势分析

图表展示：

- 柱状图
- 饼图
- 折线图

------

# 5. 权限设计

采用 RBAC 模型。

```
User
 ↓
Role
 ↓
Permission
```

## 员工

权限：

- 创建报销单
- 查看自己的报销记录

## 部门经理

权限：

- 审批报销单

## 财务人员

权限：

- 财务审核
- 确认付款

## 管理员

权限：

- 用户管理
- 部门管理
- 系统配置

------

# 6. 身份认证方案

采用 JWT + 拦截器方案。

认证流程：

```
用户登录
 ↓
服务器生成 JWT
 ↓
前端保存 Token
 ↓
请求 Header 携带 Token
 ↓
JwtInterceptor 校验
 ↓
放行请求
```

Header：

```
Authorization: Bearer Token
```

Token 默认有效期：

```
2小时
```

------

# 7. 文件上传设计

支持上传：

- jpg
- png
- pdf

实现方式：

Spring Boot MultipartFile

目录结构：

```
upload

├── image
├── invoice
└── temp
```

数据库保存：

```
file_name
file_path
file_size
upload_time
```

后期可扩展：

- MinIO
- OSS对象存储

------

# 8. 日志设计

日志框架：

Logback

日志类型：

### 登录日志

记录：

- 用户
- 登录时间
- IP地址

### 操作日志

记录：

- 新增报销单
- 审批操作
- 删除操作

### 异常日志

记录：

- 系统异常
- SQL异常

------

# 9. API设计规范

采用 RESTful 风格。

### 用户模块

```
POST /api/auth/login
POST /api/auth/logout
PUT  /api/user/password
```

### 报销模块

```
GET    /api/expense/page
GET    /api/expense/{id}
POST   /api/expense
PUT    /api/expense
DELETE /api/expense/{id}
```

### 审批模块

```
GET /api/approval/page
POST /api/approval/pass
POST /api/approval/reject
```

------

# 10. 异常处理设计

统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误示例：

```json
{
  "code": 500,
  "message": "服务器异常",
  "data": null
}
```

采用：

```
GlobalExceptionHandler
```

统一处理：

- RuntimeException
- BusinessException
- ValidationException

------

# 11. 项目目录设计

## 前端目录

```
src
├── api
├── assets
├── components
├── layout
├── router
├── stores
├── utils
├── views
│    ├── login
│    ├── expense
│    ├── approval
│    ├── finance
│    └── system
├── App.vue
└── main.js
```

------

## 后端目录

```
com.company.expense

├── common
│
├── config
│
├── controller
│
├── service
│
├── service.impl
│
├── mapper
│
├── entity
│
├── dto
│
├── vo
│
├── interceptor
│
├── exception
│
├── utils
│
└── ExpenseApplication
```

------

# 12. 数据库设计

核心表：

```
sys_user
sys_role
sys_department
expense
expense_item
approval_record
attachment
operation_log
```

数据库字符集：

```
utf8mb4
```

存储引擎：

```
InnoDB
```

------

# 13. 缓存设计

Redis Key设计：

```
login:token:{userId}

captcha:{uuid}

statistics:month

statistics:year
```

缓存时间：

| 类型     | 过期时间 |
| -------- | -------- |
| Token    | 2h       |
| 验证码   | 5min     |
| 首页统计 | 30min    |

------

# 14. 安全设计

## 密码加密

采用：

```
BCryptPasswordEncoder
```

禁止明文存储密码。

## SQL注入防护

使用：

- MyBatis Plus
- 参数绑定

## XSS防护

前端过滤输入内容。

## CORS跨域

配置：

```
CorsConfig
```

------

# 15. 部署方案

开发环境：

```
Windows

JDK17
Node.js
MySQL8
Redis
```

生产环境：

```
Linux
Docker
Nginx

Vue项目
↓
Spring Boot Jar
↓
MySQL
↓
Redis
```

部署结构：

```
Client
    ↓
Nginx
    ↓
Spring Boot
    ↓
MySQL + Redis
```

------

# 16. 后续扩展方向

### 工作流引擎

集成：

- Flowable
- Activiti

实现复杂审批流程。

### 消息通知

支持：

- 邮件通知
- WebSocket实时消息

### 对象存储

接入：

- MinIO
- 阿里云OSS

### Docker部署

支持容器化部署。

### 微服务改造

基于：

- Spring Cloud Alibaba

实现服务拆分。

------

# 17. 技术方案总结

前端：

```
Vue3 + Vite + Element Plus + Pinia + Axios
```

后端：

```
Spring Boot3 + MyBatis Plus + JWT + Redis
```

数据库：

```
MySQL8.3.0
```

部署：

```
Linux + Docker + Nginx
```

系统采用经典前后端分离架构，满足小型企业费用报销业务需求，具备良好的扩展性、可维护性和部署能力，可支持后续业务规模增长及功能升级。