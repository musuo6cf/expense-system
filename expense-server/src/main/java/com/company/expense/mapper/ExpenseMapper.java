package com.company.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.expense.entity.Expense;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExpenseMapper extends BaseMapper<Expense> {
}
