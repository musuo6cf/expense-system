-- ============================================
-- 小型企业费用报销管理系统 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS expense_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE expense_db;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT        NOT NULL COMMENT '用户ID',
    username      VARCHAR(50)   NOT NULL COMMENT '登录账号',
    password      VARCHAR(255)  NOT NULL COMMENT '密码（BCrypt加密）',
    real_name     VARCHAR(50)   NOT NULL COMMENT '真实姓名',
    phone         VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
    email         VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    department_id BIGINT        DEFAULT NULL COMMENT '部门ID',
    status        TINYINT       NOT NULL DEFAULT 1 COMMENT '用户状态: 1-启用 0-禁用',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 角色表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT        NOT NULL COMMENT '角色ID',
    role_name   VARCHAR(50)   NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)   NOT NULL COMMENT '角色编码',
    description VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================
-- 3. 权限表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT        NOT NULL COMMENT '权限ID',
    permission_name VARCHAR(50)   NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100)  NOT NULL COMMENT '权限标识',
    description     VARCHAR(255)  DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================
-- 4. 用户角色关联表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================
-- 5. 角色权限关联表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT NOT NULL COMMENT '主键',
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================
-- 6. 部门表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_department (
    id              BIGINT        NOT NULL COMMENT '部门ID',
    department_name VARCHAR(50)   NOT NULL COMMENT '部门名称',
    manager_id      BIGINT        DEFAULT NULL COMMENT '部门负责人ID',
    description     VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ============================================
-- 7. 报销单表
-- ============================================
CREATE TABLE IF NOT EXISTS expense (
    id            BIGINT          NOT NULL COMMENT '报销单ID',
    expense_no    VARCHAR(50)     NOT NULL COMMENT '报销编号',
    title         VARCHAR(100)    NOT NULL COMMENT '标题',
    applicant_id  BIGINT          NOT NULL COMMENT '申请人ID',
    department_id BIGINT          NOT NULL COMMENT '所属部门ID',
    total_amount  DECIMAL(10,2)   NOT NULL COMMENT '总金额',
    reason        VARCHAR(500)    DEFAULT NULL COMMENT '报销原因',
    status        TINYINT         NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿 1-待主管审批 2-主管驳回 3-待财务审核 4-财务驳回 5-待付款 6-已付款 7-已归档',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_expense_no (expense_no),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销单表';

-- ============================================
-- 8. 报销明细表
-- ============================================
CREATE TABLE IF NOT EXISTS expense_item (
    id           BIGINT          NOT NULL COMMENT '明细ID',
    expense_id   BIGINT          NOT NULL COMMENT '报销单ID',
    expense_type VARCHAR(50)     NOT NULL COMMENT '费用类型',
    amount       DECIMAL(10,2)   NOT NULL COMMENT '金额',
    expense_date DATE            NOT NULL COMMENT '消费日期',
    description  VARCHAR(255)    DEFAULT NULL COMMENT '说明',
    PRIMARY KEY (id),
    KEY idx_expense_id (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销明细表';

-- ============================================
-- 9. 审批记录表
-- ============================================
CREATE TABLE IF NOT EXISTS approval_record (
    id             BIGINT        NOT NULL COMMENT '记录ID',
    expense_id     BIGINT        NOT NULL COMMENT '报销单ID',
    approver_id    BIGINT        NOT NULL COMMENT '审批人ID',
    approve_result TINYINT       NOT NULL COMMENT '审批结果: 1-通过 2-驳回',
    comment        VARCHAR(500)  DEFAULT NULL COMMENT '审批意见',
    approve_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    PRIMARY KEY (id),
    KEY idx_expense_id (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';

-- ============================================
-- 10. 附件表
-- ============================================
CREATE TABLE IF NOT EXISTS attachment (
    id          BIGINT        NOT NULL COMMENT '附件ID',
    expense_id  BIGINT        NOT NULL COMMENT '报销单ID',
    file_name   VARCHAR(255)  NOT NULL COMMENT '文件名称',
    file_path   VARCHAR(500)  NOT NULL COMMENT '文件路径',
    file_size   BIGINT        DEFAULT NULL COMMENT '文件大小（字节）',
    upload_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_expense_id (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

-- ============================================
-- 11. 操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS operation_log (
    id             BIGINT        NOT NULL COMMENT '日志ID',
    user_id        BIGINT        NOT NULL COMMENT '用户ID',
    operation      VARCHAR(100)  NOT NULL COMMENT '操作名称',
    method         VARCHAR(200)  DEFAULT NULL COMMENT '请求方法',
    ip             VARCHAR(50)   DEFAULT NULL COMMENT 'IP地址',
    operation_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 12. 登录日志表
-- ============================================
CREATE TABLE IF NOT EXISTS login_log (
    id         BIGINT       NOT NULL COMMENT '日志ID',
    username   VARCHAR(50)  NOT NULL COMMENT '用户名',
    ip         VARCHAR(50)  DEFAULT NULL COMMENT '登录IP',
    login_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '登录状态: 1-成功 0-失败',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认角色
INSERT INTO sys_role (id, role_name, role_code, description, create_time) VALUES
(1, '系统管理员', 'ADMIN',    '系统管理员，拥有所有权限', NOW()),
(2, '员工',       'EMPLOYEE', '普通员工，可提交报销申请', NOW()),
(3, '部门经理',   'MANAGER',  '部门经理，可审批本部门报销', NOW()),
(4, '财务人员',   'FINANCE',  '财务人员，可审核和付款', NOW());

-- 默认权限
INSERT INTO sys_permission (id, permission_name, permission_code, description) VALUES
(1,  '新建报销',   'expense:add',     '新建报销单'),
(2,  '修改报销',   'expense:update',  '修改报销单'),
(3,  '删除报销',   'expense:delete',  '删除报销单'),
(4,  '审批报销',   'expense:approve', '审批报销单'),
(5,  '财务付款',   'expense:pay',     '财务付款'),
(6,  '用户管理',   'user:manage',     '用户管理'),
(7,  '角色管理',   'role:manage',     '角色管理'),
(8,  '部门管理',   'dept:manage',     '部门管理'),
(9,  '查看统计',   'stats:view',      '查看统计数据'),
(10, '系统配置',   'system:config',   '系统配置');

-- 角色-权限关联
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
(1,  1, 1), (2,  1, 2), (3,  1, 3), (4,  1, 4), (5,  1, 5),
(6,  1, 6), (7,  1, 7), (8,  1, 8), (9,  1, 9), (10, 1, 10),
(11, 2, 1), (12, 2, 2),
(13, 3, 4),
(14, 4, 5);

-- 默认部门
INSERT INTO sys_department (id, department_name, description, create_time) VALUES
(1, '总经办', '总经理办公室', NOW()),
(2, '技术部', '技术研发部门', NOW()),
(3, '市场部', '市场营销部门', NOW()),
(4, '财务部', '财务管理部门', NOW());

-- 默认管理员账号: admin / admin123
-- BCrypt 加密后的密码
INSERT INTO sys_user (id, username, password, real_name, phone, email, department_id, status, create_time) VALUES
(1, 'admin', '$2b$10$K23qpphNcM9j/saYkocCou4BCGSiL/5vhm9xk6eJ0eWx7jJK/WSKK', '系统管理员', '13800000000', 'admin@company.com', 1, 1, NOW());

-- 管理员角色分配
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);
