package com.taskflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.common.Result;
import com.taskflow.dto.TaskStatisticsDTO;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.entity.TaskExecutionLog;
import com.taskflow.mapper.TaskDefinitionMapper;
import com.taskflow.mapper.TaskExecutionLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计分析控制器
 */
@Tag(name = "统计分析", description = "任务执行统计和分析")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final TaskDefinitionMapper taskMapper;
    private final TaskExecutionLogMapper logMapper;

    @Operation(summary = "获取任务统计")
    @GetMapping("/task")
    public Result<TaskStatisticsDTO> getTaskStatistics() {
        TaskStatisticsDTO stats = new TaskStatisticsDTO();

        // 获取所有任务
        List<TaskDefinition> allTasks = taskMapper.selectList(null);

        // 基础统计
        stats.setTotalTasks((long) allTasks.size());
        stats.setEnabledTasks(allTasks.stream()
                .filter(t -> "ENABLED".equals(t.getStatus()))
                .count());
        stats.setDisabledTasks(allTasks.stream()
                .filter(t -> "DISABLED".equals(t.getStatus()))
                .count());

        // 按触发类型统计
        stats.setCronTasks(allTasks.stream()
                .filter(t -> "CRON".equals(t.getTriggerType()))
                .count());
        stats.setDelayTasks(allTasks.stream()
                .filter(t -> "DELAY".equals(t.getTriggerType()))
                .count());
        stats.setImmediateTasks(allTasks.stream()
                .filter(t -> "IMMEDIATE".equals(t.getTriggerType()))
                .count());

        // 今日执行统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<TaskExecutionLog> todayLogs = logMapper.selectList(
                new LambdaQueryWrapper<TaskExecutionLog>()
                        .ge(TaskExecutionLog::getStartTime, todayStart)
        );

        stats.setTodayExecutions((long) todayLogs.size());
        stats.setTodaySuccess(todayLogs.stream()
                .filter(log -> "SUCCESS".equals(log.getStatus()))
                .count());
        stats.setTodayFailed(todayLogs.stream()
                .filter(log -> "FAILED".equals(log.getStatus()))
                .count());

        // 计算成功率
        if (stats.getTodayExecutions() > 0) {
            stats.setSuccessRate(
                    (double) stats.getTodaySuccess() / stats.getTodayExecutions() * 100
            );
        } else {
            stats.setSuccessRate(0.0);
        }

        stats.setStatisticsTime(LocalDateTime.now());

        return Result.success(stats);
    }
}
