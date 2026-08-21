package com.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskflow.entity.TaskExecutionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务执行日志 Mapper
 */
@Mapper
public interface TaskExecutionLogMapper extends BaseMapper<TaskExecutionLog> {
}
