package com.company.expense.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovalVO {

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
    private List<Record> records;
    private LocalDateTime createTime;

    @Data
    public static class Record {
        private Long id;
        private Long approverId;
        private String approverName;
        private Integer approveResult;
        private String approveResultText;
        private String comment;
        private LocalDateTime approveTime;
    }
}
