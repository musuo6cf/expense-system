package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.PageQueryDTO;
import com.company.expense.dto.PasswordUpdateDTO;
import com.company.expense.entity.SysDepartment;
import com.company.expense.entity.SysRole;
import com.company.expense.entity.SysUser;
import com.company.expense.entity.SysUserRole;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.SysDepartmentMapper;
import com.company.expense.mapper.SysRoleMapper;
import com.company.expense.mapper.SysUserMapper;
import com.company.expense.mapper.SysUserRoleMapper;
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
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
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

        // load roles for all users
        Map<Long, SysUserRole> roleMap = Map.of();
        Map<Long, String> roleNameMap = Map.of();
        List<Long> userIds = userPage.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            List<SysUserRole> allRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
            roleMap = allRoles.stream().collect(Collectors.toMap(SysUserRole::getUserId, r -> r, (a, b) -> a));
            List<Long> roleIds = allRoles.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList());
            if (!roleIds.isEmpty()) {
                roleNameMap = sysRoleMapper.selectBatchIds(roleIds).stream()
                        .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
            }
        }

        Map<Long, String> finalDeptMap = deptMap;
        Map<Long, SysUserRole> finalRoleMap = roleMap;
        Map<Long, String> finalRoleNameMap = roleNameMap;
        List<UserVO> records = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setIcbcCardNo(user.getIcbcCardNo());
            vo.setDepartmentId(user.getDepartmentId());
            vo.setDepartmentName(finalDeptMap.get(user.getDepartmentId()));
            SysUserRole ur = finalRoleMap.get(user.getId());
            if (ur != null) {
                vo.setRoleId(ur.getRoleId());
                vo.setRoleName(finalRoleNameMap.get(ur.getRoleId()));
            }
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
        vo.setIcbcCardNo(user.getIcbcCardNo());
        vo.setDepartmentId(user.getDepartmentId());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        if (user.getDepartmentId() != null) {
            SysDepartment dept = sysDepartmentMapper.selectById(user.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getDepartmentName());
            }
        }

        // role info
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, id);
        SysUserRole ur = sysUserRoleMapper.selectOne(urWrapper);
        if (ur != null) {
            vo.setRoleId(ur.getRoleId());
            SysRole role = sysRoleMapper.selectById(ur.getRoleId());
            if (role != null) {
                vo.setRoleName(role.getRoleName());
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

        // assign role
        if (user.getRoleId() != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(user.getRoleId());
            sysUserRoleMapper.insert(ur);
        }
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

        // update role
        if (user.getRoleId() != null) {
            LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.eq(SysUserRole::getUserId, user.getId());
            sysUserRoleMapper.delete(urWrapper);
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(user.getRoleId());
            sysUserRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // remove role mapping
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, id);
        sysUserRoleMapper.delete(urWrapper);

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
