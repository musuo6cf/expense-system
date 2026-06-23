package com.company.expense.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String reason;

    @NotEmpty(message = "报销明细不能为空")
    @Valid
    private List<ExpenseItemDTO> items;

    @Data
    public static class ExpenseItemDTO {

        @NotBlank(message = "费用类型不能为空")
        private String expenseType;

        @NotNull(message = "金额不能为空")
        private BigDecimal amount;

        @NotNull(message = "消费日期不能为空")
        private LocalDate expenseDate;

        private String description;
    }
}
