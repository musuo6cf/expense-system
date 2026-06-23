package com.company.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentDTO {

    @NotNull(message = "报销单ID不能为空")
    private Long expenseId;

    @NotBlank(message = "付款方式不能为空")
    private String paymentMethod;

    @NotBlank(message = "银行流水号不能为空")
    private String bankTransactionNo;
}
