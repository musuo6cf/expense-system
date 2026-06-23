package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.ExpenseDTO;
import com.company.expense.entity.Expense;
import com.company.expense.entity.ExpenseItem;
import com.company.expense.entity.SysDepartment;
import com.company.expense.entity.SysUser;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.ExpenseItemMapper;
import com.company.expense.mapper.ExpenseMapper;
import com.company.expense.mapper.SysDepartmentMapper;
import com.company.expense.mapper.SysUserMapper;
import com.company.expense.service.ExpenseService;
import com.company.expense.vo.ExpenseVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final ExpenseItemMapper expenseItemMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createExpense(ExpenseDTO dto, Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Expense expense = new Expense();
        expense.setExpenseNo(generateExpenseNo());
        expense.setTitle(dto.getTitle());
        expense.setApplicantId(userId);
        expense.setDepartmentId(user.getDepartmentId());
        expense.setReason(dto.getReason());
        expense.setStatus(Expense.STATUS_DRAFT);

        BigDecimal totalAmount = dto.getItems().stream()
                .map(ExpenseDTO.ExpenseItemDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        expense.setTotalAmount(totalAmount);

        expenseMapper.insert(expense);

        List<ExpenseItem> items = new ArrayList<>();
        for (ExpenseDTO.ExpenseItemDTO itemDTO : dto.getItems()) {
            ExpenseItem item = new ExpenseItem();
            item.setExpenseId(expense.getId());
            item.setExpenseType(itemDTO.getExpenseType());
            item.setAmount(itemDTO.getAmount());
            item.setExpenseDate(itemDTO.getExpenseDate());
            item.setDescription(itemDTO.getDescription());
            items.add(item);
        }
        for (ExpenseItem item : items) {
            expenseItemMapper.insert(item);
        }
    }

    @Override
    public Page<ExpenseVO> pageExpenses(Integer page, Integer size, Integer status, String keyword, Long userId) {
        LambdaQueryWrapper<Expense> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Expense::getApplicantId, userId);
        if (status != null) {
            wrapper.eq(Expense::getStatus, status);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Expense::getTitle, keyword)
                    .or()
                    .like(Expense::getExpenseNo, keyword));
        }
        wrapper.orderByDesc(Expense::getCreateTime);

        Page<Expense> expensePage = expenseMapper.selectPage(new Page<>(page, size), wrapper);

        Map<Long, String> deptMap = buildDeptMap(expensePage.getRecords());

        Page<ExpenseVO> voPage = new Page<>(page, size);
        voPage.setTotal(expensePage.getTotal());

        List<ExpenseVO> records = expensePage.getRecords().stream()
                .map(e -> toVO(e, deptMap, false))
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public ExpenseVO getExpenseById(Long id) {
        Expense expense = expenseMapper.selectById(id);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        return toVO(expense, buildDeptMap(List.of(expense)), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExpense(Long id, ExpenseDTO dto, Long userId) {
        Expense expense = expenseMapper.selectById(id);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        if (!expense.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (expense.getStatus() != Expense.STATUS_DRAFT) {
            throw new BusinessException(ResultCode.EXPENSE_STATUS_ERROR);
        }

        expense.setTitle(dto.getTitle());
        expense.setReason(dto.getReason());

        BigDecimal totalAmount = dto.getItems().stream()
                .map(ExpenseDTO.ExpenseItemDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        expense.setTotalAmount(totalAmount);

        expenseMapper.updateById(expense);

        LambdaQueryWrapper<ExpenseItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(ExpenseItem::getExpenseId, id);
        expenseItemMapper.delete(itemWrapper);

        List<ExpenseItem> items = new ArrayList<>();
        for (ExpenseDTO.ExpenseItemDTO itemDTO : dto.getItems()) {
            ExpenseItem item = new ExpenseItem();
            item.setExpenseId(id);
            item.setExpenseType(itemDTO.getExpenseType());
            item.setAmount(itemDTO.getAmount());
            item.setExpenseDate(itemDTO.getExpenseDate());
            item.setDescription(itemDTO.getDescription());
            items.add(item);
        }
        for (ExpenseItem item : items) {
            expenseItemMapper.insert(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExpense(Long id, Long userId) {
        Expense expense = expenseMapper.selectById(id);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        if (!expense.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (expense.getStatus() != Expense.STATUS_DRAFT) {
            throw new BusinessException(ResultCode.EXPENSE_STATUS_ERROR);
        }

        expenseMapper.deleteById(id);

        LambdaQueryWrapper<ExpenseItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(ExpenseItem::getExpenseId, id);
        expenseItemMapper.delete(itemWrapper);
    }

    // ========== helper methods ==========

    private String generateExpenseNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "EXP-" + date + "-" + suffix;
    }

    private ExpenseVO toVO(Expense expense, Map<Long, String> deptMap, boolean includeItems) {
        ExpenseVO vo = new ExpenseVO();
        vo.setId(expense.getId());
        vo.setExpenseNo(expense.getExpenseNo());
        vo.setTitle(expense.getTitle());
        vo.setApplicantId(expense.getApplicantId());
        vo.setDepartmentId(expense.getDepartmentId());
        vo.setDepartmentName(deptMap.get(expense.getDepartmentId()));
        vo.setTotalAmount(expense.getTotalAmount());
        vo.setReason(expense.getReason());
        vo.setStatus(expense.getStatus());
        vo.setCreateTime(expense.getCreateTime());
        vo.setUpdateTime(expense.getUpdateTime());

        SysUser applicant = sysUserMapper.selectById(expense.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getRealName());
        }

        if (includeItems) {
            LambdaQueryWrapper<ExpenseItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(ExpenseItem::getExpenseId, expense.getId());
            List<ExpenseItem> items = expenseItemMapper.selectList(itemWrapper);
            vo.setItems(items.stream().map(item -> {
                ExpenseVO.ExpenseItemVO itemVO = new ExpenseVO.ExpenseItemVO();
                itemVO.setId(item.getId());
                itemVO.setExpenseType(item.getExpenseType());
                itemVO.setAmount(item.getAmount());
                itemVO.setExpenseDate(item.getExpenseDate());
                itemVO.setDescription(item.getDescription());
                return itemVO;
            }).collect(Collectors.toList()));
        }

        return vo;
    }

    private Map<Long, String> buildDeptMap(List<Expense> expenses) {
        List<Long> deptIds = expenses.stream()
                .map(Expense::getDepartmentId)
                .distinct()
                .collect(Collectors.toList());
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return sysDepartmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDepartmentName));
    }
}
