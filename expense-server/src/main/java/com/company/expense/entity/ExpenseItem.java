package com.company.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("expense_item")
public class ExpenseItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long expenseId;
    private String expenseType;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
}
