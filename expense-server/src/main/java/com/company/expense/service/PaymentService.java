package com.company.expense.service;

import com.company.expense.dto.PaymentDTO;
import com.company.expense.vo.PaymentVO;

import java.util.List;

public interface PaymentService {

    List<PaymentVO> pendingPayments(Long userId);

    PaymentVO getPaymentDetail(Long expenseId);

    void pay(PaymentDTO dto, Long userId);
}
