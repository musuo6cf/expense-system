package com.company.expense.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.expense.common.Result;
import com.company.expense.dto.ExpenseDTO;
import com.company.expense.service.ExpenseService;
import com.company.expense.vo.ExpenseExportVO;
import com.company.expense.vo.ExpenseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "报销管理", description = "报销单CRUD相关接口")
@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "新建报销单")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ExpenseDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long expenseId = expenseService.createExpense(dto, userId);
        return Result.ok(expenseId);
    }

    @Operation(summary = "分页查询报销单")
    @GetMapping("/page")
    public Result<Page<ExpenseVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) String keyword,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ExpenseVO> voPage = expenseService.pageExpenses(page, size, status, keyword, userId);
        return Result.ok(voPage);
    }

    @Operation(summary = "根据ID查询报销单")
    @GetMapping("/{id}")
    public Result<ExpenseVO> getById(@PathVariable Long id) {
        ExpenseVO vo = expenseService.getExpenseById(id);
        return Result.ok(vo);
    }

    @Operation(summary = "编辑报销单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody ExpenseDTO dto,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        expenseService.updateExpense(id, dto, userId);
        return Result.ok();
    }

    @Operation(summary = "删除报销单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        expenseService.deleteExpense(id, userId);
        return Result.ok();
    }

    @Operation(summary = "提交报销单")
    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        expenseService.submitExpense(id, userId);
        return Result.ok();
    }

    @Operation(summary = "导出报销单为Excel")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String keyword,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        Long userId = (Long) request.getAttribute("userId");
        List<ExpenseExportVO> list = expenseService.exportExpenses(status, keyword, userId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = URLEncoder.encode("报销导出_" + timestamp + ".xlsx", StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        EasyExcel.write(response.getOutputStream(), ExpenseExportVO.class)
                .sheet("报销明细")
                .doWrite(list);
    }
}
