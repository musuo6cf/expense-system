package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.PaymentDTO;
import com.company.expense.entity.ApprovalRecord;
import com.company.expense.entity.Expense;
import com.company.expense.entity.SysDepartment;
import com.company.expense.entity.SysRole;
import com.company.expense.entity.SysUser;
import com.company.expense.entity.SysUserRole;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.ApprovalRecordMapper;
import com.company.expense.mapper.ExpenseMapper;
import com.company.expense.mapper.SysDepartmentMapper;
import com.company.expense.mapper.SysRoleMapper;
import com.company.expense.mapper.SysUserMapper;
import com.company.expense.mapper.SysUserRoleMapper;
import com.company.expense.service.PaymentService;
import com.company.expense.vo.PaymentVO;
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
public class PaymentServiceImpl implements PaymentService {

    private static final int PAYMENT_RECORD = 3;

    private final ExpenseMapper expenseMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    public List<PaymentVO> pendingPayments(Long userId) {
        if (!hasFinanceRole(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        LambdaQueryWrapper<Expense> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Expense::getStatus, Expense.STATUS_PENDING_PAYMENT)
                .orderByDesc(Expense::getCreateTime);

        List<Expense> expenses = expenseMapper.selectList(wrapper);
        return expenses.stream().map(e -> toVO(e, false)).collect(Collectors.toList());
    }

    @Override
    public PaymentVO getPaymentDetail(Long expenseId) {
        Expense expense = expenseMapper.selectById(expenseId);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        return toVO(expense, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(PaymentDTO dto, Long userId) {
        if (!hasFinanceRole(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        Expense expense = expenseMapper.selectById(dto.getExpenseId());
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        if (expense.getStatus() != Expense.STATUS_PENDING_PAYMENT) {
            if (expense.getStatus() == Expense.STATUS_PAID) {
                throw new BusinessException(ResultCode.EXPENSE_STATUS_ERROR.getCode(), "该报销单已付款，请勿重复操作");
            }
            throw new BusinessException(ResultCode.EXPENSE_STATUS_ERROR);
        }

        expense.setStatus(Expense.STATUS_PAID);
        expenseMapper.updateById(expense);

        ApprovalRecord record = new ApprovalRecord();
        record.setExpenseId(dto.getExpenseId());
        record.setApproverId(userId);
        record.setApproveResult(PAYMENT_RECORD);
        record.setComment(dto.getPaymentMethod() + "|" + dto.getBankTransactionNo());
        record.setApproveTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    // ========== helpers ==========

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

    private PaymentVO toVO(Expense expense, boolean includeRecords) {
        PaymentVO vo = new PaymentVO();
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
            vo.setApplicantIcbcCardNo(applicant.getIcbcCardNo());
        }
        SysDepartment dept = sysDepartmentMapper.selectById(expense.getDepartmentId());
        if (dept != null) {
            vo.setDepartmentName(dept.getDepartmentName());
        }

        if (includeRecords) {
            LambdaQueryWrapper<ApprovalRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ApprovalRecord::getExpenseId, expense.getId())
                    .eq(ApprovalRecord::getApproveResult, PAYMENT_RECORD)
                    .orderByDesc(ApprovalRecord::getApproveTime);
            List<ApprovalRecord> records = approvalRecordMapper.selectList(recordWrapper);

            List<Long> operatorIds = records.stream()
                    .map(ApprovalRecord::getApproverId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> nameMap = Map.of();
            if (!operatorIds.isEmpty()) {
                nameMap = sysUserMapper.selectBatchIds(operatorIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
            }

            List<PaymentVO.PaymentRecordVO> voRecords = new ArrayList<>();
            for (ApprovalRecord record : records) {
                PaymentVO.PaymentRecordVO r = new PaymentVO.PaymentRecordVO();
                r.setId(record.getId());
                r.setOperatorId(record.getApproverId());
                r.setOperatorName(nameMap.get(record.getApproverId()));
                r.setPaymentTime(record.getApproveTime());

                String comment = record.getComment();
                if (comment != null && comment.contains("|")) {
                    String[] parts = comment.split("\\|", 2);
                    r.setPaymentMethod(parts[0]);
                    r.setBankTransactionNo(parts.length > 1 ? parts[1] : "");
                }

                voRecords.add(r);
            }
            vo.setPaymentRecords(voRecords);
        }

        return vo;
    }
}
