package com.taskflow.monitor.controller;

import com.taskflow.common.Result;
import com.taskflow.monitor.model.ClusterMetrics;
import com.taskflow.monitor.model.QueueMetrics;
import com.taskflow.monitor.model.SystemMetrics;
import com.taskflow.monitor.service.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控控制器
 */
@Tag(name = "监控管理", description = "实时监控和性能指标")
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "获取系统监控指标")
    @GetMapping("/system")
    public Result<SystemMetrics> getSystemMetrics() {
        SystemMetrics metrics = monitorService.getSystemMetrics();
        return Result.success(metrics);
    }

    @Operation(summary = "获取集群监控指标")
    @GetMapping("/cluster/{clusterId}")
    public Result<ClusterMetrics> getClusterMetrics(@PathVariable Long clusterId) {
        ClusterMetrics metrics = monitorService.getClusterMetrics(clusterId);
        return metrics != null ? Result.success(metrics) : Result.error("集群不存在");
    }

    @Operation(summary = "获取所有集群监控指标")
    @GetMapping("/cluster/all")
    public Result<List<ClusterMetrics>> getAllClusterMetrics() {
        List<ClusterMetrics> metrics = monitorService.getAllClusterMetrics();
        return Result.success(metrics);
    }

    @Operation(summary = "获取队列监控指标")
    @GetMapping("/cluster/{clusterId}/queues")
    public Result<List<QueueMetrics>> getQueueMetrics(@PathVariable Long clusterId) {
        List<QueueMetrics> metrics = monitorService.getQueueMetrics(clusterId);
        return Result.success(metrics);
    }

    @Operation(summary = "获取指定队列监控指标")
    @GetMapping("/cluster/{clusterId}/queue/{queueName}")
    public Result<QueueMetrics> getQueueMetrics(
            @PathVariable Long clusterId,
            @PathVariable String queueName) {
        QueueMetrics metrics = monitorService.getQueueMetrics(clusterId, queueName);
        return metrics != null ? Result.success(metrics) : Result.error("队列不存在");
    }

    @Operation(summary = "刷新集群监控数据")
    @PostMapping("/cluster/{clusterId}/refresh")
    public Result<String> refreshClusterMetrics(@PathVariable Long clusterId) {
        monitorService.refreshClusterMetrics(clusterId);
        return Result.success("监控数据刷新成功");
    }

    @Operation(summary = "刷新所有监控数据")
    @PostMapping("/refresh")
    public Result<String> refreshAllMetrics() {
        monitorService.refreshAllMetrics();
        return Result.success("所有监控数据刷新成功");
    }
}
