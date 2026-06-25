package com.company.expense.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.expense.dto.ExpenseDTO;
import com.company.expense.vo.ExpenseExportVO;
import com.company.expense.vo.ExpenseVO;

import java.util.List;

public interface ExpenseService {

    Long createExpense(ExpenseDTO dto, Long userId);

    Page<ExpenseVO> pageExpenses(Integer page, Integer size, Integer status, String keyword, Long userId);

    ExpenseVO getExpenseById(Long id);

    void updateExpense(Long id, ExpenseDTO dto, Long userId);

    void deleteExpense(Long id, Long userId);

    void submitExpense(Long id, Long userId);

    List<ExpenseExportVO> exportExpenses(Integer status, String keyword, Long userId);
}
