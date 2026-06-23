# 数据库表设计

数据库名称：`expense_db`

## 表清单

| 表名                | 描述           |
| ------------------- | -------------- |
| sys_user            | 用户表         |
| sys_role            | 角色表         |
| sys_permission      | 权限表         |
| sys_user_role       | 用户角色关联表 |
| sys_role_permission | 角色权限关联表 |
| sys_department      | 部门表         |
| expense             | 报销单表       |
| expense_item        | 报销明细表     |
| approval_record     | 审批记录表     |
| attachment          | 附件表         |
| operation_log       | 操作日志表     |
| login_log           | 登录日志表     |

------

# 1. 用户表（sys_user）

| 字段名        | 类型         | 主键 | 非空 | 默认值            | 描述               |
| ------------- | ------------ | ---- | ---- | ----------------- | ------------------ |
| id            | BIGINT       | √    | √    | -                 | 用户ID             |
| username      | VARCHAR(50)  |      | √    | -                 | 登录账号           |
| password      | VARCHAR(255) |      | √    | -                 | 密码（BCrypt加密） |
| real_name     | VARCHAR(50)  |      | √    | -                 | 真实姓名           |
| phone         | VARCHAR(20)  |      |      | NULL              | 手机号             |
| email         | VARCHAR(100) |      |      | NULL              | 邮箱               |
| department_id | BIGINT       |      |      | NULL              | 部门ID             |
| status        | TINYINT      |      | √    | 1                 | 用户状态           |
| create_time   | DATETIME     |      | √    | CURRENT_TIMESTAMP | 创建时间           |
| update_time   | DATETIME     |      |      | CURRENT_TIMESTAMP | 更新时间           |
| deleted       | TINYINT      |      | √    | 0                 | 逻辑删除           |

------

# 2. 角色表（sys_role）

| 字段名      | 类型         | 主键 | 非空 | 描述     |
| ----------- | ------------ | ---- | ---- | -------- |
| id          | BIGINT       | √    | √    | 角色ID   |
| role_name   | VARCHAR(50)  |      | √    | 角色名称 |
| role_code   | VARCHAR(50)  |      | √    | 角色编码 |
| description | VARCHAR(255) |      |      | 备注     |
| create_time | DATETIME     |      | √    | 创建时间 |
| deleted     | TINYINT      |      | √    | 逻辑删除 |

示例：

| role_code | role_name  |
| --------- | ---------- |
| ADMIN     | 系统管理员 |
| EMPLOYEE  | 员工       |
| MANAGER   | 部门经理   |
| FINANCE   | 财务人员   |

------

# 3. 权限表（sys_permission）

| 字段名          | 类型         | 主键 | 非空 | 描述     |
| --------------- | ------------ | ---- | ---- | -------- |
| id              | BIGINT       | √    | √    | 权限ID   |
| permission_name | VARCHAR(50)  |      | √    | 权限名称 |
| permission_code | VARCHAR(100) |      | √    | 权限标识 |
| description     | VARCHAR(255) |      |      | 描述     |

示例：

| permission_code | 描述       |
| --------------- | ---------- |
| expense:add     | 新建报销单 |
| expense:update  | 修改报销单 |
| expense:approve | 审批报销   |
| expense:pay     | 财务付款   |
| user:manage     | 用户管理   |

------

# 4. 用户角色关联表（sys_user_role）

| 字段名  | 类型   | 主键 | 非空 | 描述   |
| ------- | ------ | ---- | ---- | ------ |
| id      | BIGINT | √    | √    | 主键   |
| user_id | BIGINT |      | √    | 用户ID |
| role_id | BIGINT |      | √    | 角色ID |

------

# 5. 角色权限关联表（sys_role_permission）

| 字段名        | 类型   | 主键 | 非空 | 描述   |
| ------------- | ------ | ---- | ---- | ------ |
| id            | BIGINT | √    | √    | 主键   |
| role_id       | BIGINT |      | √    | 角色ID |
| permission_id | BIGINT |      | √    | 权限ID |

------

# 6. 部门表（sys_department）

| 字段名          | 类型         | 主键 | 非空 | 描述       |
| --------------- | ------------ | ---- | ---- | ---------- |
| id              | BIGINT       | √    | √    | 部门ID     |
| department_name | VARCHAR(50)  |      | √    | 部门名称   |
| manager_id      | BIGINT       |      |      | 部门负责人 |
| description     | VARCHAR(255) |      |      | 备注       |
| create_time     | DATETIME     |      | √    | 创建时间   |

------

# 7. 报销单表（expense）

| 字段名        | 类型          | 主键 | 非空 | 描述     |
| ------------- | ------------- | ---- | ---- | -------- |
| id            | BIGINT        | √    | √    | 报销单ID |
| expense_no    | VARCHAR(50)   |      | √    | 报销编号 |
| title         | VARCHAR(100)  |      | √    | 标题     |
| applicant_id  | BIGINT        |      | √    | 申请人ID |
| department_id | BIGINT        |      | √    | 所属部门 |
| total_amount  | DECIMAL(10,2) |      | √    | 总金额   |
| reason        | VARCHAR(500)  |      |      | 报销原因 |
| status        | TINYINT       |      | √    | 状态     |
| create_time   | DATETIME      |      | √    | 创建时间 |
| update_time   | DATETIME      |      |      | 更新时间 |
| deleted       | TINYINT       |      | √    | 逻辑删除 |

状态：

| 状态值 | 含义     |
| ------ | -------- |
| 0      | 草稿     |
| 1      | 待审批   |
| 2      | 审批通过 |
| 3      | 审批拒绝 |
| 4      | 已付款   |

------

# 8. 报销明细表（expense_item）

| 字段名       | 类型          | 主键 | 非空 | 描述     |
| ------------ | ------------- | ---- | ---- | -------- |
| id           | BIGINT        | √    | √    | 明细ID   |
| expense_id   | BIGINT        |      | √    | 报销单ID |
| expense_type | VARCHAR(50)   |      | √    | 费用类型 |
| amount       | DECIMAL(10,2) |      | √    | 金额     |
| expense_date | DATE          |      | √    | 消费日期 |
| description  | VARCHAR(255)  |      |      | 说明     |

------

# 9. 审批记录表（approval_record）

| 字段名         | 类型         | 主键 | 非空 | 描述     |
| -------------- | ------------ | ---- | ---- | -------- |
| id             | BIGINT       | √    | √    | 记录ID   |
| expense_id     | BIGINT       |      | √    | 报销单ID |
| approver_id    | BIGINT       |      | √    | 审批人ID |
| approve_result | TINYINT      |      | √    | 审批结果 |
| comment        | VARCHAR(500) |      |      | 审批意见 |
| approve_time   | DATETIME     |      | √    | 审批时间 |

审批结果：

| 值   | 含义 |
| ---- | ---- |
| 1    | 通过 |
| 2    | 驳回 |

------

# 10. 附件表（attachment）

| 字段名      | 类型         | 主键 | 非空 | 描述     |
| ----------- | ------------ | ---- | ---- | -------- |
| id          | BIGINT       | √    | √    | 附件ID   |
| expense_id  | BIGINT       |      | √    | 报销单ID |
| file_name   | VARCHAR(255) |      | √    | 文件名称 |
| file_path   | VARCHAR(500) |      | √    | 文件路径 |
| file_size   | BIGINT       |      |      | 文件大小 |
| upload_time | DATETIME     |      | √    | 上传时间 |

------

# 11. 操作日志表（operation_log）

| 字段名         | 类型         | 主键 | 非空 | 描述     |
| -------------- | ------------ | ---- | ---- | -------- |
| id             | BIGINT       | √    | √    | 日志ID   |
| user_id        | BIGINT       |      | √    | 用户ID   |
| operation      | VARCHAR(100) |      | √    | 操作名称 |
| method         | VARCHAR(200) |      |      | 请求方法 |
| ip             | VARCHAR(50)  |      |      | IP地址   |
| operation_time | DATETIME     |      | √    | 操作时间 |

------

# 12. 登录日志表（login_log）

| 字段名     | 类型        | 主键 | 非空 | 描述     |
| ---------- | ----------- | ---- | ---- | -------- |
| id         | BIGINT      | √    | √    | 日志ID   |
| username   | VARCHAR(50) |      | √    | 用户名   |
| ip         | VARCHAR(50) |      |      | 登录IP   |
| login_time | DATETIME    |      | √    | 登录时间 |
| status     | TINYINT     |      | √    | 登录状态 |