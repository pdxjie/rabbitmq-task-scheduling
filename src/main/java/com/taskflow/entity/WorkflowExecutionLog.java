package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工作流执行历史实体
 */
@Data
@TableName("workflow_execution_log")
public class WorkflowExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workflowId;

    private String instanceId;

    private String workflowName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String status;

    private String executionParams;

    private String nodeStatuses;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
