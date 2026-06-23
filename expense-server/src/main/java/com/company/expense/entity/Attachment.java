package com.company.expense.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long expenseId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime uploadTime;
}
