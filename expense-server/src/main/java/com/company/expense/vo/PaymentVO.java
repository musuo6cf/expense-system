package com.company.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentVO {

    private Long id;
    private String expenseNo;
    private String title;
    private Long applicantId;
    private String applicantName;
    private String applicantIcbcCardNo;
    private Long departmentId;
    private String departmentName;
    private BigDecimal totalAmount;
    private String reason;
    private Integer status;
    private List<PaymentRecordVO> paymentRecords;
    private LocalDateTime createTime;

    @Data
    public static class PaymentRecordVO {
        private Long id;
        private Long operatorId;
        private String operatorName;
        private String paymentMethod;
        private String bankTransactionNo;
        private LocalDateTime paymentTime;
    }
}
