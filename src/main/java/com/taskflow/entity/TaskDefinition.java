package com.taskflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务定义实体
 */
@Data
@TableName("task_definition")
public class TaskDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskName;

    private String taskType;

    private String triggerType;

    private String cronExpression;

    private Integer delaySeconds;

    private Integer priority;

    private Integer timeoutSeconds;

    private Integer retryCount;

    private String retryStrategy;

    private Long clusterId;

    private String exchangeName;

    private String routingKey;

    private String queueName;

    private String taskContent;

    private String taskParams;

    private String status;

    private LocalDateTime nextExecuteTime;

    private LocalDateTime lastExecuteTime;

    private Long executeCount;

    private Long successCount;

    private Long failCount;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Boolean deleted;
}
