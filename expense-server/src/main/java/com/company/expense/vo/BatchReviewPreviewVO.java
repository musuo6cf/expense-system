package com.company.expense.vo;

import lombok.Data;

import java.util.List;

@Data
public class BatchReviewPreviewVO {

    private List<PassItem> passList;
    private List<RejectItem> rejectList;

    @Data
    public static class PassItem {
        private Long expenseId;
        private String expenseNo;
        private String title;
    }

    @Data
    public static class RejectItem {
        private Long expenseId;
        private String expenseNo;
        private String title;
        private List<String> reasons;
    }
}
