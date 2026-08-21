package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务执行日志实体
 */
@Data
@TableName("task_execution_log")
public class TaskExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String traceId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String status;

    private String result;

    private String errorMessage;

    private String errorStack;

    private Integer retryCount;

    private String workerId;

    private String workerIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
