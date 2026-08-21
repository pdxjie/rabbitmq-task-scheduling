package com.taskflow.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.entity.RabbitMQCluster;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.entity.TaskExecutionLog;
import com.taskflow.entity.WorkflowDefinition;
import com.taskflow.mapper.RabbitMQClusterMapper;
import com.taskflow.mapper.TaskDefinitionMapper;
import com.taskflow.mapper.TaskExecutionLogMapper;
import com.taskflow.mapper.WorkflowDefinitionMapper;
import com.taskflow.monitor.model.ClusterMetrics;
import com.taskflow.monitor.model.QueueMetrics;
import com.taskflow.monitor.model.SystemMetrics;
import com.taskflow.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final TaskDefinitionMapper taskMapper;
    private final TaskExecutionLogMapper logMapper;
    private final WorkflowDefinitionMapper workflowMapper;
    private final RabbitMQClusterMapper clusterMapper;

    @Override
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();

        // 任务统计
        List<TaskDefinition> allTasks = taskMapper.selectList(null);
        metrics.setTotalTasks((long) allTasks.size());
        metrics.setEnabledTasks(allTasks.stream()
                .filter(t -> "ENABLED".equals(t.getStatus()))
                .count());
        metrics.setRunningTasks(0L); // TODO: 从缓存或实时状态获取

        // 今日执行统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<TaskExecutionLog> todayLogs = logMapper.selectList(
                new LambdaQueryWrapper<TaskExecutionLog>()
                        .ge(TaskExecutionLog::getStartTime, todayStart)
        );

        metrics.setTodayExecutions((long) todayLogs.size());
        metrics.setTodaySuccess(todayLogs.stream()
                .filter(log -> "SUCCESS".equals(log.getStatus()))
                .count());
        metrics.setTodayFailed(todayLogs.stream()
                .filter(log -> "FAILED".equals(log.getStatus()))
                .count());

        // 成功率
        if (metrics.getTodayExecutions() > 0) {
            metrics.setSuccessRate(
                    (double) metrics.getTodaySuccess() / metrics.getTodayExecutions() * 100
            );
        } else {
            metrics.setSuccessRate(0.0);
        }

        // 平均执行时长
        Double avgTime = todayLogs.stream()
                .filter(log -> log.getDurationMs() != null)
                .mapToLong(TaskExecutionLog::getDurationMs)
                .average()
                .orElse(0.0);
        metrics.setAvgExecutionTime(avgTime);

        // 工作流统计
        List<WorkflowDefinition> allWorkflows = workflowMapper.selectList(null);
        metrics.setTotalWorkflows((long) allWorkflows.size());
        metrics.setRunningWorkflows(0L); // TODO: 从执行器获取

        // 集群统计
        List<RabbitMQCluster> allClusters = clusterMapper.selectList(null);
        metrics.setTotalClusters((long) allClusters.size());
        metrics.setActiveClusters(allClusters.stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .count());

        // JVM 指标
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        metrics.setJvmHeapUsed(heapUsed);
        metrics.setJvmHeapMax(heapMax);

        // 线程数
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        metrics.setThreadCount(threadBean.getThreadCount());

        // CPU 使用率（简化实现）
        metrics.setCpuUsage(0.0); // TODO: 实现 CPU 监控

        metrics.setCollectTime(LocalDateTime.now());

        return metrics;
    }

    @Override
    public ClusterMetrics getClusterMetrics(Long clusterId) {
        RabbitMQCluster cluster = clusterMapper.selectById(clusterId);
        if (cluster == null) {
            return null;
        }

        ClusterMetrics metrics = new ClusterMetrics();
        metrics.setClusterId(clusterId);
        metrics.setClusterName(cluster.getClusterName());
        metrics.setHealthStatus(cluster.getStatus());
        metrics.setCollectTime(LocalDateTime.now());

        // TODO: 调用 RabbitMQ Management API 获取实时指标
        // 这里先返回模拟数据
        metrics.setNodeCount(1);
        metrics.setQueueCount(0);
        metrics.setExchangeCount(0);
        metrics.setConnectionCount(0);
        metrics.setChannelCount(0);
        metrics.setConsumerCount(0);
        metrics.setTotalMessages(0L);
        metrics.setReadyMessages(0L);
        metrics.setUnackedMessages(0L);
        metrics.setPublishRate(0.0);
        metrics.setConsumeRate(0.0);
        metrics.setAckRate(0.0);

        return metrics;
    }

    @Override
    public List<ClusterMetrics> getAllClusterMetrics() {
        List<RabbitMQCluster> clusters = clusterMapper.selectList(
                new LambdaQueryWrapper<RabbitMQCluster>()
                        .eq(RabbitMQCluster::getStatus, "ACTIVE")
        );

        List<ClusterMetrics> metricsList = new ArrayList<>();
        for (RabbitMQCluster cluster : clusters) {
            ClusterMetrics metrics = getClusterMetrics(cluster.getId());
            if (metrics != null) {
                metricsList.add(metrics);
            }
        }

        return metricsList;
    }

    @Override
    public List<QueueMetrics> getQueueMetrics(Long clusterId) {
        // TODO: 调用 RabbitMQ Management API 获取队列指标
        return new ArrayList<>();
    }

    @Override
    public QueueMetrics getQueueMetrics(Long clusterId, String queueName) {
        // TODO: 调用 RabbitMQ Management API 获取指定队列指标
        return null;
    }

    @Override
    public void refreshClusterMetrics(Long clusterId) {
        log.info("刷新集群监控数据: {}", clusterId);
        // TODO: 实现定时刷新逻辑
    }

    @Override
    public void refreshAllMetrics() {
        log.info("刷新所有集群监控数据");
        List<RabbitMQCluster> clusters = clusterMapper.selectList(
                new LambdaQueryWrapper<RabbitMQCluster>()
                        .eq(RabbitMQCluster::getStatus, "ACTIVE")
        );

        for (RabbitMQCluster cluster : clusters) {
            refreshClusterMetrics(cluster.getId());
        }
    }
}
