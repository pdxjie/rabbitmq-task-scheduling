package com.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskflow.entity.RabbitMQCluster;

import java.util.List;

/**
 * RabbitMQ 集群服务接口
 */
public interface RabbitMQClusterService extends IService<RabbitMQCluster> {

    /**
     * 测试集群连接
     */
    boolean testConnection(RabbitMQCluster cluster);

    /**
     * 获取集群信息
     */
    RabbitMQCluster getClusterInfo(Long clusterId);

    /**
     * 获取所有活跃集群
     */
    List<RabbitMQCluster> getActiveClusters();

    /**
     * 更新集群健康度
     */
    void updateHealthScore(Long clusterId, Integer score);
}
