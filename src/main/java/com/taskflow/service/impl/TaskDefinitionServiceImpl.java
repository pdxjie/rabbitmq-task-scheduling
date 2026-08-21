package com.taskflow.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.mapper.TaskDefinitionMapper;
import com.taskflow.service.DelayTaskService;
import com.taskflow.service.TaskDefinitionService;
import com.taskflow.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 任务定义服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDefinitionServiceImpl extends ServiceImpl<TaskDefinitionMapper, TaskDefinition>
        implements TaskDefinitionService {

    private final TaskExecutorService taskExecutorService;
    private final DelayTaskService delayTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskDefinition createTask(TaskDefinition task) {
        // 设置初始状态
        if (task.getStatus() == null) {
            task.setStatus("ENABLED");
        }
        if (task.getPriority() == null) {
            task.setPriority(5);
        }
        if (task.getRetryCount() == null) {
            task.setRetryCount(3);
        }
        if (task.getTimeoutSeconds() == null) {
            task.setTimeoutSeconds(300);
        }
        if (task.getExecuteCount() == null) {
            task.setExecuteCount(0L);
        }
        if (task.getSuccessCount() == null) {
            task.setSuccessCount(0L);
        }
        if (task.getFailCount() == null) {
            task.setFailCount(0L);
        }

        // 如果是 Cron 任务，计算下次执行时间
        if ("CRON".equals(task.getTriggerType()) && task.getCronExpression() != null) {
            calculateNextExecuteTime(task);
        }

        save(task);
        log.info("任务创建成功: {}", task.getTaskName());
        return task;
    }

    @Override
    public boolean updateTask(TaskDefinition task) {
        // 如果是 Cron 任务，重新计算下次执行时间
        if ("CRON".equals(task.getTriggerType()) && task.getCronExpression() != null) {
            calculateNextExecuteTime(task);
        }
        return updateById(task);
    }

    @Override
    public boolean enableTask(Long taskId) {
        TaskDefinition task = getById(taskId);
        if (task != null) {
            task.setStatus("ENABLED");
            // 重新计算下次执行时间
            if ("CRON".equals(task.getTriggerType())) {
                calculateNextExecuteTime(task);
            }
            return updateById(task);
        }
        return false;
    }

    @Override
    public boolean disableTask(Long taskId) {
        TaskDefinition task = getById(taskId);
        if (task != null) {
            task.setStatus("DISABLED");
            return updateById(task);
        }
        return false;
    }

    @Override
    public void executeTaskNow(Long taskId) {
        TaskDefinition task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (!"ENABLED".equals(task.getStatus())) {
            throw new RuntimeException("任务未启用: " + task.getTaskName());
        }

        log.info("立即执行任务: {}", task.getTaskName());

        // 根据触发类型选择执行方式
        if ("DELAY".equals(task.getTriggerType()) && task.getDelaySeconds() != null) {
            // 延迟任务
            delayTaskService.sendDelayTask(task, task.getDelaySeconds());
        } else {
            // 立即执行
            taskExecutorService.executeTaskAsync(task);
        }
    }

    @Override
    public List<TaskDefinition> getTasksByCluster(Long clusterId) {
        return list(new LambdaQueryWrapper<TaskDefinition>()
                .eq(TaskDefinition::getClusterId, clusterId)
                .orderByDesc(TaskDefinition::getCreatedAt));
    }

    @Override
    public List<TaskDefinition> getPendingCronTasks() {
        LocalDateTime now = LocalDateTime.now();
        return list(new LambdaQueryWrapper<TaskDefinition>()
                .eq(TaskDefinition::getStatus, "ENABLED")
                .eq(TaskDefinition::getTriggerType, "CRON")
                .le(TaskDefinition::getNextExecuteTime, now)
                .isNotNull(TaskDefinition::getNextExecuteTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStats(Long taskId, boolean success) {
        TaskDefinition task = getById(taskId);
        if (task != null) {
            task.setExecuteCount(task.getExecuteCount() + 1);
            task.setLastExecuteTime(LocalDateTime.now());

            if (success) {
                task.setSuccessCount(task.getSuccessCount() + 1);
            } else {
                task.setFailCount(task.getFailCount() + 1);
            }

            updateById(task);
        }
    }

    @Override
    public void calculateNextExecuteTime(TaskDefinition task) {
        if (task.getCronExpression() == null) {
            return;
        }

        try {
            CronExpression cronExpression = CronExpression.parse(task.getCronExpression());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime next = cronExpression.next(now);

            if (next != null) {
                task.setNextExecuteTime(next);
                log.debug("任务 {} 下次执行时间: {}", task.getTaskName(), next);
            }
        } catch (Exception e) {
            log.error("计算下次执行时间失败: {}, Cron: {}", task.getTaskName(), task.getCronExpression(), e);
        }
    }
}
