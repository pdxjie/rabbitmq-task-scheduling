package com.taskflow.controller;

import com.taskflow.common.Result;
import com.taskflow.entity.RabbitMQCluster;
import com.taskflow.service.RabbitMQClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RabbitMQ 集群管理控制器
 */
@Tag(name = "集群管理", description = "RabbitMQ 集群管理接口")
@RestController
@RequestMapping("/cluster")
@RequiredArgsConstructor
public class RabbitMQClusterController {

    private final RabbitMQClusterService clusterService;

    @Operation(summary = "获取所有集群")
    @GetMapping("/list")
    public Result<List<RabbitMQCluster>> listClusters() {
        return Result.success(clusterService.list());
    }

    @Operation(summary = "获取活跃集群")
    @GetMapping("/active")
    public Result<List<RabbitMQCluster>> getActiveClusters() {
        return Result.success(clusterService.getActiveClusters());
    }

    @Operation(summary = "获取集群详情")
    @GetMapping("/{id}")
    public Result<RabbitMQCluster> getCluster(@PathVariable Long id) {
        return Result.success(clusterService.getById(id));
    }

    @Operation(summary = "创建集群")
    @PostMapping
    public Result<RabbitMQCluster> createCluster(@RequestBody RabbitMQCluster cluster) {
        // 测试连接
        boolean connected = clusterService.testConnection(cluster);
        if (!connected) {
            return Result.error("集群连接失败，请检查配置");
        }

        clusterService.save(cluster);
        return Result.success("集群创建成功", cluster);
    }

    @Operation(summary = "更新集群")
    @PutMapping("/{id}")
    public Result<String> updateCluster(@PathVariable Long id, @RequestBody RabbitMQCluster cluster) {
        cluster.setId(id);
        clusterService.updateById(cluster);
        return Result.success("集群更新成功");
    }

    @Operation(summary = "删除集群")
    @DeleteMapping("/{id}")
    public Result<String> deleteCluster(@PathVariable Long id) {
        clusterService.removeById(id);
        return Result.success("集群删除成功");
    }

    @Operation(summary = "测试集群连接")
    @PostMapping("/test")
    public Result<String> testConnection(@RequestBody RabbitMQCluster cluster) {
        boolean connected = clusterService.testConnection(cluster);
        if (connected) {
            return Result.success("连接成功");
        } else {
            return Result.error("连接失败，请检查配置");
        }
    }
}
