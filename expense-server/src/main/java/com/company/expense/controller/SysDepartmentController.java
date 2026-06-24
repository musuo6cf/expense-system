package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.entity.SysDepartment;
import com.company.expense.mapper.SysDepartmentMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "部门管理", description = "部门列表接口")
@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class SysDepartmentController {

    private final SysDepartmentMapper sysDepartmentMapper;

    @GetMapping("/list")
    public Result<List<SysDepartment>> list() {
        return Result.ok(sysDepartmentMapper.selectList(null));
    }
}
