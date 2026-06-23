package com.company.expense.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FinanceApprovalDTO {

    @NotNull(message = "报销单ID不能为空")
    private Long expenseId;

    private String comment;
}
