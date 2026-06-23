package com.company.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long expenseId;
    private Long approverId;
    private Integer approveResult;
    private String comment;
    private LocalDateTime approveTime;
}
