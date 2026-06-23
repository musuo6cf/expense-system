package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.service.AttachmentService;
import com.company.expense.vo.AttachmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "附件管理", description = "文件上传、查询、删除相关接口")
@RestController
@RequestMapping("/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public Result<AttachmentVO> upload(@RequestParam Long expenseId,
                                        @RequestParam MultipartFile file,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AttachmentVO vo = attachmentService.upload(expenseId, file, userId);
        return Result.ok(vo);
    }

    @Operation(summary = "查询报销单附件列表")
    @GetMapping("/expense/{expenseId}")
    public Result<List<AttachmentVO>> listByExpenseId(@PathVariable Long expenseId) {
        List<AttachmentVO> list = attachmentService.listByExpenseId(expenseId);
        return Result.ok(list);
    }

    @Operation(summary = "删除附件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        attachmentService.delete(id, userId);
        return Result.ok();
    }
}
