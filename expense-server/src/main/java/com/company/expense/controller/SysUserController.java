package com.company.expense.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.expense.common.Result;
import com.company.expense.dto.PageQueryDTO;
import com.company.expense.dto.PasswordUpdateDTO;
import com.company.expense.entity.SysUser;
import com.company.expense.service.SysUserService;
import com.company.expense.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理", description = "用户CRUD相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<Page<UserVO>> page(PageQueryDTO pageQuery) {
        Page<UserVO> page = sysUserService.pageUsers(pageQuery);
        return Result.ok(page);
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        UserVO userVO = sysUserService.getUserById(id);
        return Result.ok(userVO);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SysUser user) {
        sysUserService.createUser(user);
        return Result.ok();
    }

    @Operation(summary = "编辑用户")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request,
                                        @Valid @RequestBody PasswordUpdateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        sysUserService.updatePassword(userId, dto);
        return Result.ok();
    }
}
