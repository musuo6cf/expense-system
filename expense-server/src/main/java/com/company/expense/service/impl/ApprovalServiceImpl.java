package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.ApprovalDTO;
import com.company.expense.entity.ApprovalRecord;
import com.company.expense.entity.Expense;
import com.company.expense.entity.SysDepartment;
import com.company.expense.entity.SysRole;
import com.company.expense.entity.SysUser;
import com.company.expense.entity.ExpenseItem;
import com.company.expense.entity.SysUserRole;
import com.company.expense.mapper.ExpenseItemMapper;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.ApprovalRecordMapper;
import com.company.expense.mapper.ExpenseMapper;
import com.company.expense.mapper.SysDepartmentMapper;
import com.company.expense.mapper.SysRoleMapper;
import com.company.expense.mapper.SysUserMapper;
import com.company.expense.mapper.SysUserRoleMapper;
import com.company.expense.service.ApprovalService;
import com.company.expense.vo.ApprovalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private static final int APPROVE_PASS = 1;
    private static final int APPROVE_REJECT = 2;

    private final ExpenseMapper expenseMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final ExpenseItemMapper expenseItemMapper;

    @Override
    public List<ApprovalVO> pendingExpenses(Long userId) {
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null || currentUser.getDepartmentId() == null) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!hasManagerRole(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        LambdaQueryWrapper<Expense> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Expense::getStatus, Expense.STATUS_PENDING_MANAGER)
                .eq(Expense::getDepartmentId, currentUser.getDepartmentId())
                .ne(Expense::getApplicantId, userId)
                .orderByDesc(Expense::getCreateTime);

        List<Expense> expenses = expenseMapper.selectList(wrapper);
        return expenses.stream().map(e -> toApprovalVO(e, false)).collect(Collectors.toList());
    }

    @Override
    public ApprovalVO getApprovalDetail(Long expenseId) {
        Expense expense = expenseMapper.selectById(expenseId);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        return toApprovalVO(expense, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pass(ApprovalDTO dto, Long userId) {
        Expense expense = validateAndGetExpense(dto.getExpenseId(), userId);

        expense.setStatus(Expense.STATUS_PENDING_FINANCE);
        expenseMapper.updateById(expense);

        saveRecord(expense.getId(), userId, APPROVE_PASS, dto.getComment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(ApprovalDTO dto, Long userId) {
        Expense expense = validateAndGetExpense(dto.getExpenseId(), userId);

        expense.setStatus(Expense.STATUS_MANAGER_REJECTED);
        expenseMapper.updateById(expense);

        saveRecord(expense.getId(), userId, APPROVE_REJECT, dto.getComment());
    }

    // ========== helpers ==========

    private Expense validateAndGetExpense(Long expenseId, Long userId) {
        Expense expense = expenseMapper.selectById(expenseId);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        if (expense.getStatus() != Expense.STATUS_PENDING_MANAGER) {
            throw new BusinessException(ResultCode.EXPENSE_STATUS_ERROR);
        }
        if (expense.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "不能审批自己的报销单");
        }

        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null || !expense.getDepartmentId().equals(currentUser.getDepartmentId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!hasManagerRole(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return expense;
    }

    private void saveRecord(Long expenseId, Long approverId, int result, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setExpenseId(expenseId);
        record.setApproverId(approverId);
        record.setApproveResult(result);
        record.setComment(comment);
        record.setApproveTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    private boolean hasManagerRole(Long userId) {
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<Long> roleIds = sysUserRoleMapper.selectList(urWrapper).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(SysRole::getId, roleIds).eq(SysRole::getRoleCode, "MANAGER");
        return sysRoleMapper.selectCount(roleWrapper) > 0;
    }

    private ApprovalVO toApprovalVO(Expense expense, boolean includeRecords) {
        ApprovalVO vo = new ApprovalVO();
        vo.setId(expense.getId());
        vo.setExpenseNo(expense.getExpenseNo());
        vo.setTitle(expense.getTitle());
        vo.setApplicantId(expense.getApplicantId());
        vo.setDepartmentId(expense.getDepartmentId());
        vo.setTotalAmount(expense.getTotalAmount());
        vo.setReason(expense.getReason());
        vo.setStatus(expense.getStatus());
        vo.setCreateTime(expense.getCreateTime());

        SysUser applicant = sysUserMapper.selectById(expense.getApplicantId());
        if (applicant != null) {
            vo.setApplicantName(applicant.getRealName());
        }

        SysDepartment dept = sysDepartmentMapper.selectById(expense.getDepartmentId());
        if (dept != null) {
            vo.setDepartmentName(dept.getDepartmentName());
        }

        if (includeRecords) {
            // expense items
            LambdaQueryWrapper<ExpenseItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(ExpenseItem::getExpenseId, expense.getId());
            List<ExpenseItem> items = expenseItemMapper.selectList(itemWrapper);
            vo.setItems(items.stream().map(item -> {
                ApprovalVO.ExpenseItemVO itemVO = new ApprovalVO.ExpenseItemVO();
                itemVO.setId(item.getId());
                itemVO.setExpenseType(item.getExpenseType());
                itemVO.setAmount(item.getAmount());
                itemVO.setExpenseDate(item.getExpenseDate());
                itemVO.setDescription(item.getDescription());
                return itemVO;
            }).collect(Collectors.toList()));

            // approval records
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ApprovalRecord::getExpenseId, expense.getId())
                    .orderByDesc(ApprovalRecord::getApproveTime);
            List<ApprovalRecord> records = approvalRecordMapper.selectList(recordWrapper);

            List<Long> approverIds = records.stream()
                    .map(ApprovalRecord::getApproverId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> approverNameMap = Map.of();
            if (!approverIds.isEmpty()) {
                approverNameMap = sysUserMapper.selectBatchIds(approverIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
            }

            List<ApprovalVO.Record> voRecords = new ArrayList<>();
            for (ApprovalRecord record : records) {
                ApprovalVO.Record r = new ApprovalVO.Record();
                r.setId(record.getId());
                r.setApproverId(record.getApproverId());
                r.setApproverName(approverNameMap.get(record.getApproverId()));
                r.setApproveResult(record.getApproveResult());
                r.setApproveResultText(record.getApproveResult() == APPROVE_PASS ? "通过" : "驳回");
                r.setComment(record.getComment());
                r.setApproveTime(record.getApproveTime());
                voRecords.add(r);
            }
            vo.setRecords(voRecords);
        }

        return vo;
    }
}
