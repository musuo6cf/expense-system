package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.expense.common.ResultCode;
import com.company.expense.dto.LoginDTO;
import com.company.expense.entity.SysRole;
import com.company.expense.entity.SysUser;
import com.company.expense.entity.SysUserRole;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.SysRoleMapper;
import com.company.expense.mapper.SysUserMapper;
import com.company.expense.mapper.SysUserRoleMapper;
import com.company.expense.service.AuthService;
import com.company.expense.utils.JwtUtil;
import com.company.expense.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        List<String> roles = getUserRoles(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRoles(roles);

        return loginVO;
    }

    @Override
    public void logout() {
        // Stateless JWT — logout handled by frontend removing token
    }

    private List<String> getUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, userId);
        List<Long> roleIds = sysUserRoleMapper.selectList(urWrapper)
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return sysRoleMapper.selectBatchIds(roleIds)
                .stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
}
