package com.taskflow.monitor.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统监控指标
 */
@Data
public class SystemMetrics {

    /**
     * 任务总数
     */
    private Long totalTasks;

    /**
     * 启用任务数
     */
    private Long enabledTasks;

    /**
     * 运行中任务数
     */
    private Long runningTasks;

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
     * 平均执行时长（毫秒）
     */
    private Double avgExecutionTime;

    /**
     * 工作流总数
     */
    private Long totalWorkflows;

    /**
     * 运行中工作流数
     */
    private Long runningWorkflows;

    /**
     * 集群总数
     */
    private Long totalClusters;

    /**
     * 活跃集群数
     */
    private Long activeClusters;

    /**
     * JVM 堆内存使用（MB）
     */
    private Long jvmHeapUsed;

    /**
     * JVM 堆内存最大（MB）
     */
    private Long jvmHeapMax;

    /**
     * CPU 使用率（%）
     */
    private Double cpuUsage;

    /**
     * 线程数
     */
    private Integer threadCount;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
}
