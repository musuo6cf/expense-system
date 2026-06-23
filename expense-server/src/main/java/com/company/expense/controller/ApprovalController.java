package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.dto.ApprovalDTO;
import com.company.expense.service.ApprovalService;
import com.company.expense.vo.ApprovalVO;
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

@Tag(name = "审批管理", description = "主管审批相关接口")
@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "查询待审批列表")
    @GetMapping("/pending")
    public Result<List<ApprovalVO>> pending(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ApprovalVO> list = approvalService.pendingExpenses(userId);
        return Result.ok(list);
    }

    @Operation(summary = "查询报销单审批详情")
    @GetMapping("/{expenseId}")
    public Result<ApprovalVO> getDetail(@PathVariable Long expenseId) {
        ApprovalVO vo = approvalService.getApprovalDetail(expenseId);
        return Result.ok(vo);
    }

    @Operation(summary = "审批通过")
    @PostMapping("/pass")
    public Result<Void> pass(@Valid @RequestBody ApprovalDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        approvalService.pass(dto, userId);
        return Result.ok();
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/reject")
    public Result<Void> reject(@Valid @RequestBody ApprovalDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        approvalService.reject(dto, userId);
        return Result.ok();
    }
}
