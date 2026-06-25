package com.company.expense.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseExportVO {

    @ExcelProperty("报销单号")
    @ColumnWidth(22)
    private String expenseNo;

    @ExcelProperty("标题")
    @ColumnWidth(25)
    private String title;

    @ExcelProperty("申请人")
    @ColumnWidth(12)
    private String applicantName;

    @ExcelProperty("部门")
    @ColumnWidth(12)
    private String departmentName;

    @ExcelProperty("费用类型")
    @ColumnWidth(14)
    private String expenseType;

    @ExcelProperty("金额")
    @ColumnWidth(12)
    @NumberFormat("#,##0.00")
    private BigDecimal amount;

    @ExcelProperty("费用日期")
    @ColumnWidth(14)
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate expenseDate;

    @ExcelProperty("费用说明")
    @ColumnWidth(30)
    private String itemDescription;

    @ExcelProperty("状态")
    @ColumnWidth(16)
    private String statusText;

    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
