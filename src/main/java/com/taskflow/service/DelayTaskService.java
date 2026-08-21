package com.taskflow.service;

import com.taskflow.entity.TaskDefinition;

/**
 * 延迟任务服务接口
 */
public interface DelayTaskService {

    /**
     * 发送延迟任务
     *
     * @param task 任务定义
     * @param delaySeconds 延迟秒数
     */
    void sendDelayTask(TaskDefinition task, int delaySeconds);

    /**
     * 选择合适的延迟队列
     *
     * @param delaySeconds 延迟秒数
     * @return 队列名称
     */
    String selectDelayQueue(int delaySeconds);
}
