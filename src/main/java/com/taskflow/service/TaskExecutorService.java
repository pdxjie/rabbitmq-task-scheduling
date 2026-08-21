package com.taskflow.service;

import com.taskflow.entity.TaskDefinition;

/**
 * 任务执行服务接口
 */
public interface TaskExecutorService {

    /**
     * 执行任务
     */
    void executeTask(TaskDefinition task);

    /**
     * 执行任务（异步）
     */
    void executeTaskAsync(TaskDefinition task);

    /**
     * 发送消息到 RabbitMQ
     */
    void sendMessageToQueue(TaskDefinition task, String message);

    /**
     * 执行 HTTP 请求任务
     */
    String executeHttpTask(TaskDefinition task);

    /**
     * 执行 Shell 脚本任务
     */
    String executeShellTask(TaskDefinition task);
}
