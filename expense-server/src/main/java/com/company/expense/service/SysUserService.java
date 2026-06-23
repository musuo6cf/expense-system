package com.company.expense.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.company.expense.dto.PageQueryDTO;
import com.company.expense.dto.PasswordUpdateDTO;
import com.company.expense.entity.SysUser;
import com.company.expense.vo.UserVO;

public interface SysUserService extends IService<SysUser> {

    Page<UserVO> pageUsers(PageQueryDTO pageQuery);

    UserVO getUserById(Long id);

    void createUser(SysUser user);

    void updateUser(SysUser user);

    void deleteUser(Long id);

    void updatePassword(Long userId, PasswordUpdateDTO dto);
}
