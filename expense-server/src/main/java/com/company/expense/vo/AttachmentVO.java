package com.company.expense.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentVO {

    private Long id;
    private Long expenseId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime uploadTime;
}
