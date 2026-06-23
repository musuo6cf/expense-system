package com.company.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("expense")
public class Expense {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String expenseNo;
    private String title;
    private Long applicantId;
    private Long departmentId;
    private BigDecimal totalAmount;
    private String reason;
    /**
     * 状态: 0-草稿 1-待主管审批 2-主管驳回 3-待财务审核 4-财务驳回 5-待付款 6-已付款 7-已归档
     */
    private Integer status;

    // 状态常量
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PENDING_MANAGER = 1;
    public static final int STATUS_MANAGER_REJECTED = 2;
    public static final int STATUS_PENDING_FINANCE = 3;
    public static final int STATUS_FINANCE_REJECTED = 4;
    public static final int STATUS_PENDING_PAYMENT = 5;
    public static final int STATUS_PAID = 6;
    public static final int STATUS_ARCHIVED = 7;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
