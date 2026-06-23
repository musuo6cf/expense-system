package com.company.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.expense.entity.ExpenseItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExpenseItemMapper extends BaseMapper<ExpenseItem> {
}
