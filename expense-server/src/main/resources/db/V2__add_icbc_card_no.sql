-- ============================================
-- 添加工商银行卡号字段
-- ============================================

-- 1. 在 sys_user 表增加 icbc_card_no 列
ALTER TABLE sys_user
    ADD COLUMN icbc_card_no VARCHAR(30) DEFAULT NULL COMMENT '工商银行卡号'
    AFTER email;

-- 2. 为已有 EMPLOYEE / MANAGER 角色用户生成工行卡号
--    使用 RAND(user.id) 种子确保每个用户生成唯一值
UPDATE sys_user u
INNER JOIN (
    SELECT DISTINCT ur.user_id
    FROM sys_user_role ur
    INNER JOIN sys_role r ON ur.role_id = r.id
    WHERE r.role_code IN ('EMPLOYEE', 'MANAGER')
) AS tmp ON u.id = tmp.user_id
SET u.icbc_card_no = CONCAT('622202', LPAD(FLOOR(RAND(u.id) * 10000000000000), 13, '0'));
