package com.company.expense.service;

import com.company.expense.dto.FinanceApprovalDTO;
import com.company.expense.vo.FinanceApprovalVO;

import java.util.List;

public interface FinanceApprovalService {

    List<FinanceApprovalVO> pendingExpenses(Long userId);

    FinanceApprovalVO getApprovalDetail(Long expenseId);

    void pass(FinanceApprovalDTO dto, Long userId);

    void reject(FinanceApprovalDTO dto, Long userId);
}
