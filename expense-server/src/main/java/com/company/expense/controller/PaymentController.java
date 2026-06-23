package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.dto.PaymentDTO;
import com.company.expense.service.PaymentService;
import com.company.expense.vo.PaymentVO;
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

@Tag(name = "付款管理", description = "财务付款相关接口")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "查询待付款列表")
    @GetMapping("/pending")
    public Result<List<PaymentVO>> pending(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<PaymentVO> list = paymentService.pendingPayments(userId);
        return Result.ok(list);
    }

    @Operation(summary = "查询报销单付款详情")
    @GetMapping("/{expenseId}")
    public Result<PaymentVO> getDetail(@PathVariable Long expenseId) {
        PaymentVO vo = paymentService.getPaymentDetail(expenseId);
        return Result.ok(vo);
    }

    @Operation(summary = "确认付款")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody PaymentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        paymentService.pay(dto, userId);
        return Result.ok();
    }
}
