package com.company.expense.service;

import com.company.expense.dto.ApprovalDTO;
import com.company.expense.vo.ApprovalVO;

import java.util.List;

public interface ApprovalService {

    List<ApprovalVO> pendingExpenses(Long userId);

    ApprovalVO getApprovalDetail(Long expenseId);

    void pass(ApprovalDTO dto, Long userId);

    void reject(ApprovalDTO dto, Long userId);
}
