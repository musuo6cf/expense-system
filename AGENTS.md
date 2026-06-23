# AGENTS.md

# 小型企业费用报销管理系统 AI 开发规范

Version: 1.0

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
src

├── api
├── assets
├── components
├── layout
├── router
├── stores
├── utils
├── views

│   ├── login
│   ├── dashboard
│   ├── expense
│   ├── approval
│   ├── finance
│   ├── statistics
│   └── system

├── App.vue
└── main.ts
```

## Backend

```text
com.company.expense

├── common
├── config
├── controller
├── service
├── service.impl
├── mapper
├── entity
├── dto
├── vo
├── exception
├── interceptor
├── utils
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

例如：

```text
create_time
update_time
real_name
department_id
```

主键：

统一：

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

返回：

统一使用：

```java
Result<T>
```

例如：

```java
@GetMapping("/{id}")
public Result<ExpenseVO> getById()
```

------

## Service

负责：

- 核心业务逻辑

事务：

```java
@Transactional(
    rollbackFor = Exception.class
)
```

------

## Mapper

使用：

MyBatis Plus

优先：

```java
LambdaQueryWrapper
LambdaUpdateWrapper
```

避免直接写 SQL。

------

## Entity

实体类：

```java
@Data
@TableName()
```

禁止：

VO 和 Entity 混用。

------

## DTO

用于：

请求参数。

禁止直接使用 Entity 接收请求。

------

## VO

用于：

响应数据。

禁止直接返回 Entity。

------

# 前端开发规范

使用：

```vue
<script setup lang="ts">
```

状态管理：

Pinia

请求：

统一：

```text
src/api
```

禁止页面直接 axios。

页面：

```text
views
```

组件：

```text
components
```

公共方法：

```text
utils
```

------

# API 规范

统一前缀：

```text
/api
```

RESTful 风格：

```text
GET
POST
PUT
DELETE
```

统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

------

# 权限模型

采用：

RBAC

数据库：

```text
sys_user

sys_role

sys_permission

sys_user_role

sys_role_permission
```

角色：

```text
ADMIN

EMPLOYEE

MANAGER

FINANCE
```

------

# 报销流程

```text
员工提交
    ↓
待主管审批
    ↓
待财务审核
    ↓
待付款
    ↓
已付款
    ↓
已归档
```

驳回：

```text
主管驳回

财务驳回
```

员工修改后重新提交。

------

# 开发顺序

Phase 1

基础框架

- 登录
- JWT
- 用户管理

Phase 2

报销模块

- 新建报销单
- 上传附件
- 查询

Phase 3

审批模块

- 主管审批
- 财务审核

Phase 4

付款模块

- 付款信息
- 自动归档

Phase 5

统计模块

- ECharts
- Excel导出

Phase 6

系统模块

- 部门管理
- 角色权限
- 日志

------

# AI 编码要求

生成代码时：

必须：

✅ 完整代码

✅ 可运行

✅ 包含 import

✅ 保持项目结构

✅ 最小修改

优先：

1. 可运行
2. 可维护
3. 简洁

禁止：

❌ 重构整个项目

❌ 引入新框架

❌ 修改目录结构

❌ 留空实现

❌ 输出伪代码

------

# 当前目标

构建一个稳定、简单、可维护的小型企业费用报销管理系统。

优先完成 MVP：

登录 → 报销 → 审批 → 财务付款 → 统计分析。

暂不考虑：

- 微服务
- DDD
- MQ
- 分布式事务

保持单体架构，快速交付。