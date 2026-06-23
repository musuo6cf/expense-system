package com.company.expense.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.expense.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
