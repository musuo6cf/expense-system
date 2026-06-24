package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.BatchReviewExecuteDTO;
import com.company.expense.entity.ApprovalRecord;
import com.company.expense.entity.Expense;
import com.company.expense.entity.ExpenseItem;
import com.company.expense.entity.SysRole;
import com.company.expense.entity.SysUserRole;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.ApprovalRecordMapper;
import com.company.expense.mapper.ExpenseItemMapper;
import com.company.expense.mapper.ExpenseMapper;
import com.company.expense.mapper.SysRoleMapper;
import com.company.expense.mapper.SysUserRoleMapper;
import com.company.expense.service.BatchReviewService;
import com.company.expense.vo.BatchReviewPreviewVO;
import com.company.expense.vo.BatchReviewResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchReviewServiceImpl implements BatchReviewService {

    private static final int APPROVE_REJECT = 2;

    private static final Map<String, BigDecimal> BUDGET_LIMITS = new LinkedHashMap<>();
    static {
        BUDGET_LIMITS.put("餐饮费用", new BigDecimal("600"));
        BUDGET_LIMITS.put("办公费用", new BigDecimal("1000"));
        BUDGET_LIMITS.put("招待费用", new BigDecimal("600"));
        BUDGET_LIMITS.put("住宿费用", new BigDecimal("1000"));
        BUDGET_LIMITS.put("交通费用", new BigDecimal("2000"));
        BUDGET_LIMITS.put("培训费用", new BigDecimal("500"));
        BUDGET_LIMITS.put("其他费用", new BigDecimal("500"));
    }

    private final ExpenseMapper expenseMapper;
    private final ExpenseItemMapper expenseItemMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public BatchReviewPreviewVO preview(Long userId) {
        if (!hasFinanceRole(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        LambdaQueryWrapper<Expense> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Expense::getStatus, Expense.STATUS_PENDING_FINANCE)
                .orderByDesc(Expense::getCreateTime);
        List<Expense> expenses = expenseMapper.selectList(wrapper);

        List<BatchReviewPreviewVO.PassItem> passList = new ArrayList<>();
        List<BatchReviewPreviewVO.RejectItem> rejectList = new ArrayList<>();

        for (Expense expense : expenses) {
            LambdaQueryWrapper<ExpenseItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(ExpenseItem::getExpenseId, expense.getId());
            List<ExpenseItem> items = expenseItemMapper.selectList(itemWrapper);

            List<String> reasons = new ArrayList<>();
            for (ExpenseItem item : items) {
                BigDecimal limit = BUDGET_LIMITS.get(item.getExpenseType());
                if (limit != null) {
                    if (item.getAmount().compareTo(limit) > 0) {
                        reasons.add(item.getExpenseType() + "金额超出标准（" + item.getAmount() + " > " + limit + "）");
                    }
                } else {
                    BigDecimal fallback = BUDGET_LIMITS.get("其他费用");
                    if (item.getAmount().compareTo(fallback) > 0) {
                        reasons.add(item.getExpenseType() + "金额超出标准（" + item.getAmount() + " > " + fallback + "）");
                    }
                }
            }

            if (reasons.isEmpty()) {
                BatchReviewPreviewVO.PassItem passItem = new BatchReviewPreviewVO.PassItem();
                passItem.setExpenseId(expense.getId());
                passItem.setExpenseNo(expense.getExpenseNo());
                passItem.setTitle(expense.getTitle());
                passList.add(passItem);
            } else {
                BatchReviewPreviewVO.RejectItem rejectItem = new BatchReviewPreviewVO.RejectItem();
                rejectItem.setExpenseId(expense.getId());
                rejectItem.setExpenseNo(expense.getExpenseNo());
                rejectItem.setTitle(expense.getTitle());
                rejectItem.setReasons(reasons);
                rejectList.add(rejectItem);
            }
        }

        BatchReviewPreviewVO vo = new BatchReviewPreviewVO();
        vo.setPassList(passList);
        vo.setRejectList(rejectList);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchReviewResultVO execute(BatchReviewExecuteDTO dto, Long userId) {
        if (!hasFinanceRole(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        List<Long> rejectIds = dto.getRejectIds() != null ? dto.getRejectIds() : List.of();

        for (Long expenseId : rejectIds) {
            Expense expense = expenseMapper.selectById(expenseId);
            if (expense == null || expense.getStatus() != Expense.STATUS_PENDING_FINANCE) {
                continue;
            }
            expense.setStatus(Expense.STATUS_FINANCE_REJECTED);
            expenseMapper.updateById(expense);

            ApprovalRecord record = new ApprovalRecord();
            record.setExpenseId(expenseId);
            record.setApproverId(userId);
            record.setApproveResult(APPROVE_REJECT);
            record.setComment("预检查发现消费金额超出标准");
            record.setApproveTime(LocalDateTime.now());
            approvalRecordMapper.insert(record);
        }

        BatchReviewResultVO result = new BatchReviewResultVO();
        result.setTotal(rejectIds.size());
        result.setRejectCount(rejectIds.size());
        return result;
    }

    private boolean hasFinanceRole(Long userId) {
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<Long> roleIds = sysUserRoleMapper.selectList(urWrapper).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(SysRole::getId, roleIds).eq(SysRole::getRoleCode, "FINANCE");
        return sysRoleMapper.selectCount(roleWrapper) > 0;
    }
}
