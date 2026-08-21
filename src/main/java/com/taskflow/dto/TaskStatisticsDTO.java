package com.taskflow.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务统计 DTO
 */
@Data
public class TaskStatisticsDTO {

    /**
     * 任务总数
     */
    private Long totalTasks;

    /**
     * 启用任务数
     */
    private Long enabledTasks;

    /**
     * 禁用任务数
     */
    private Long disabledTasks;

    /**
     * 今日执行次数
     */
    private Long todayExecutions;

    /**
     * 今日成功次数
     */
    private Long todaySuccess;

    /**
     * 今日失败次数
     */
    private Long todayFailed;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * Cron 任务数
     */
    private Long cronTasks;

    /**
     * 延迟任务数
     */
    private Long delayTasks;

    /**
     * 立即执行任务数
     */
    private Long immediateTasks;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;
}
