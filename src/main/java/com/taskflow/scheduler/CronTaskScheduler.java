package com.taskflow.scheduler;

import com.taskflow.entity.TaskDefinition;
import com.taskflow.service.TaskDefinitionService;
import com.taskflow.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cron 任务调度器
 * 每 10 秒扫描一次待执行的 Cron 任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CronTaskScheduler {

    private final TaskDefinitionService taskService;
    private final TaskExecutorService taskExecutor;

    /**
     * 扫描并执行到期的 Cron 任务
     * 使用 fixedDelay 确保上一次执行完成后再开始下一次
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void scanAndExecuteCronTasks() {
        try {
            // 获取所有需要执行的 Cron 任务
            List<TaskDefinition> tasks = taskService.getPendingCronTasks();

            if (tasks.isEmpty()) {
                return;
            }

            log.info("发现 {} 个待执行的 Cron 任务", tasks.size());

            for (TaskDefinition task : tasks) {
                try {
                    // 异步执行任务
                    taskExecutor.executeTaskAsync(task);

                    // 计算并更新下次执行时间
                    taskService.calculateNextExecuteTime(task);
                    taskService.updateById(task);

                    log.info("Cron 任务已提交执行: {}, 下次执行时间: {}",
                            task.getTaskName(), task.getNextExecuteTime());

                } catch (Exception e) {
                    log.error("Cron 任务执行失败: {}", task.getTaskName(), e);
                }
            }

        } catch (Exception e) {
            log.error("扫描 Cron 任务失败", e);
        }
    }

    /**
     * 统计任务执行情况（每分钟一次）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void reportTaskStats() {
        try {
            List<TaskDefinition> allTasks = taskService.list();
            long enabledCount = allTasks.stream()
                    .filter(t -> "ENABLED".equals(t.getStatus()))
                    .count();
            long cronTaskCount = allTasks.stream()
                    .filter(t -> "CRON".equals(t.getTriggerType()))
                    .count();

            log.info("任务统计 - 总数: {}, 启用: {}, Cron任务: {}",
                    allTasks.size(), enabledCount, cronTaskCount);

        } catch (Exception e) {
            log.error("统计任务失败", e);
        }
    }
}
