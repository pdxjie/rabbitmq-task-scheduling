package com.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskflow.entity.TaskDefinition;

import java.util.List;

/**
 * 任务定义服务接口
 */
public interface TaskDefinitionService extends IService<TaskDefinition> {

    /**
     * 创建任务
     */
    TaskDefinition createTask(TaskDefinition task);

    /**
     * 更新任务
     */
    boolean updateTask(TaskDefinition task);

    /**
     * 启用任务
     */
    boolean enableTask(Long taskId);

    /**
     * 禁用任务
     */
    boolean disableTask(Long taskId);

    /**
     * 立即执行任务
     */
    void executeTaskNow(Long taskId);

    /**
     * 获取指定集群的所有任务
     */
    List<TaskDefinition> getTasksByCluster(Long clusterId);

    /**
     * 获取所有需要调度的 Cron 任务
     */
    List<TaskDefinition> getPendingCronTasks();

    /**
     * 更新任务执行统计
     */
    void updateTaskStats(Long taskId, boolean success);

    /**
     * 计算下次执行时间
     */
    void calculateNextExecuteTime(TaskDefinition task);
}
