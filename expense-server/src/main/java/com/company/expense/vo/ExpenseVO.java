package com.company.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExpenseVO {

    private Long id;
    private String expenseNo;
    private String title;
    private Long applicantId;
    private String applicantName;
    private Long departmentId;
    private String departmentName;
    private BigDecimal totalAmount;
    private String reason;
    private Integer status;
    private List<ExpenseItemVO> items;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Data
    public static class ExpenseItemVO {

        private Long id;
        private String expenseType;
        private BigDecimal amount;
        private LocalDate expenseDate;
        private String description;
    }
}
