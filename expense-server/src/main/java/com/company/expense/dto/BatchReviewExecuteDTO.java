package com.company.expense.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchReviewExecuteDTO {

    @NotNull(message = "驳回ID列表不能为空")
    private List<Long> rejectIds;
}
