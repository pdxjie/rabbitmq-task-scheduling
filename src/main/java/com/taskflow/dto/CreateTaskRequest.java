package com.taskflow.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建任务请求 DTO
 */
@Data
public class CreateTaskRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotBlank(message = "任务类型不能为空")
    private String taskType; // MESSAGE/HTTP/SHELL/CODE

    @NotBlank(message = "触发类型不能为空")
    private String triggerType; // IMMEDIATE/DELAY/CRON

    private String cronExpression; // Cron 表达式（CRON 类型必填）

    private Integer delaySeconds; // 延迟秒数（DELAY 类型必填）

    private Integer priority = 5; // 优先级 1-10

    private Integer timeoutSeconds = 300; // 超时时间

    private Integer retryCount = 3; // 重试次数

    private String retryStrategy = "FIXED"; // 重试策略

    @NotNull(message = "集群ID不能为空")
    private Long clusterId;

    private String exchangeName; // 交换机名称

    private String routingKey; // 路由键

    private String queueName; // 队列名称

    @NotBlank(message = "任务内容不能为空")
    private String taskContent; // 任务内容

    private String taskParams; // 任务参数（JSON）

    private String description; // 任务描述
}
