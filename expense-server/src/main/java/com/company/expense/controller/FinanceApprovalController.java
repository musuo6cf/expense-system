package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.dto.FinanceApprovalDTO;
import com.company.expense.service.FinanceApprovalService;
import com.company.expense.vo.FinanceApprovalVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "财务审核", description = "财务审核相关接口")
@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceApprovalController {

    private final FinanceApprovalService financeApprovalService;

    @Operation(summary = "查询待财务审核列表")
    @GetMapping("/pending")
    public Result<List<FinanceApprovalVO>> pending(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FinanceApprovalVO> list = financeApprovalService.pendingExpenses(userId);
        return Result.ok(list);
    }

    @Operation(summary = "查询报销单审核详情")
    @GetMapping("/{expenseId}")
    public Result<FinanceApprovalVO> getDetail(@PathVariable Long expenseId) {
        FinanceApprovalVO vo = financeApprovalService.getApprovalDetail(expenseId);
        return Result.ok(vo);
    }

    @Operation(summary = "财务审核通过")
    @PostMapping("/pass")
    public Result<Void> pass(@Valid @RequestBody FinanceApprovalDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        financeApprovalService.pass(dto, userId);
        return Result.ok();
    }

    @Operation(summary = "财务审核驳回")
    @PostMapping("/reject")
    public Result<Void> reject(@Valid @RequestBody FinanceApprovalDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        financeApprovalService.reject(dto, userId);
        return Result.ok();
    }
}
