package com.company.expense.service;

import com.company.expense.dto.BatchReviewExecuteDTO;
import com.company.expense.vo.BatchReviewPreviewVO;
import com.company.expense.vo.BatchReviewResultVO;

public interface BatchReviewService {

    BatchReviewPreviewVO preview(Long userId);

    BatchReviewResultVO execute(BatchReviewExecuteDTO dto, Long userId);
}
