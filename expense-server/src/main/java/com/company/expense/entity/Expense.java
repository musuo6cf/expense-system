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
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
