package com.company.expense.controller;

import com.company.expense.common.Result;
import com.company.expense.entity.SysRole;
import com.company.expense.mapper.SysRoleMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "角色管理", description = "角色列表接口")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper sysRoleMapper;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(sysRoleMapper.selectList(null));
    }
}
