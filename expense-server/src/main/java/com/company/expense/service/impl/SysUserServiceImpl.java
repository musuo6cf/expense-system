package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.PageQueryDTO;
import com.company.expense.dto.PasswordUpdateDTO;
import com.company.expense.entity.SysDepartment;
import com.company.expense.entity.SysUser;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.SysDepartmentMapper;
import com.company.expense.mapper.SysUserMapper;
import com.company.expense.service.SysUserService;
import com.company.expense.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Page<UserVO> pageUsers(PageQueryDTO pageQuery) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(pageQuery.getKeyword())) {
            wrapper.like(SysUser::getUsername, pageQuery.getKeyword())
                    .or()
                    .like(SysUser::getRealName, pageQuery.getKeyword());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        List<Long> deptIds = userPage.getRecords().stream()
                .map(SysUser::getDepartmentId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> deptMap = Map.of();
        if (!deptIds.isEmpty()) {
            deptMap = sysDepartmentMapper.selectBatchIds(deptIds).stream()
                    .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDepartmentName));
        }

        Page<UserVO> voPage = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        voPage.setTotal(userPage.getTotal());

        Map<Long, String> finalDeptMap = deptMap;
        List<UserVO> records = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setDepartmentId(user.getDepartmentId());
            vo.setDepartmentName(finalDeptMap.get(user.getDepartmentId()));
            vo.setStatus(user.getStatus());
            vo.setCreateTime(user.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setDepartmentId(user.getDepartmentId());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        if (user.getDepartmentId() != null) {
            SysDepartment dept = sysDepartmentMapper.selectById(user.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getDepartmentName());
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        sysUserMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        SysUser existingUser = sysUserMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        user.setPassword(null);
        user.setUsername(null);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        sysUserMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserMapper.updateById(user);
    }
}
