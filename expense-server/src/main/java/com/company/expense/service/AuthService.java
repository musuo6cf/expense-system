package com.company.expense.service;

import com.company.expense.dto.LoginDTO;
import com.company.expense.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    void logout();
}
