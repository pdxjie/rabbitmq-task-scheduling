package com.taskflow.monitor.service;

import com.taskflow.monitor.model.ClusterMetrics;
import com.taskflow.monitor.model.QueueMetrics;
import com.taskflow.monitor.model.SystemMetrics;

import java.util.List;

/**
 * 监控服务接口
 */
public interface MonitorService {

    /**
     * 获取系统监控指标
     */
    SystemMetrics getSystemMetrics();

    /**
     * 获取集群监控指标
     */
    ClusterMetrics getClusterMetrics(Long clusterId);

    /**
     * 获取所有集群监控指标
     */
    List<ClusterMetrics> getAllClusterMetrics();

    /**
     * 获取队列监控指标
     */
    List<QueueMetrics> getQueueMetrics(Long clusterId);

    /**
     * 获取指定队列监控指标
     */
    QueueMetrics getQueueMetrics(Long clusterId, String queueName);

    /**
     * 刷新集群监控数据
     */
    void refreshClusterMetrics(Long clusterId);

    /**
     * 刷新所有集群监控数据
     */
    void refreshAllMetrics();
}
