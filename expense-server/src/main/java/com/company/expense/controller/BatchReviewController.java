package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.dto.BatchReviewExecuteDTO;
import com.company.expense.service.BatchReviewService;
import com.company.expense.vo.BatchReviewPreviewVO;
import com.company.expense.vo.BatchReviewResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "批量审批", description = "财务批量审批接口")
@RestController
@RequestMapping("/finance/batch-review")
@RequiredArgsConstructor
public class BatchReviewController {

    private final BatchReviewService batchReviewService;

    @PostMapping("/preview")
    public Result<BatchReviewPreviewVO> preview(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BatchReviewPreviewVO vo = batchReviewService.preview(userId);
        return Result.ok(vo);
    }

    @PostMapping("/execute")
    public Result<BatchReviewResultVO> execute(@Valid @RequestBody BatchReviewExecuteDTO dto,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BatchReviewResultVO vo = batchReviewService.execute(dto, userId);
        return Result.ok(vo);
    }
}
