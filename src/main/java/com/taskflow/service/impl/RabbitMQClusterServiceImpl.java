package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.taskflow.entity.RabbitMQCluster;
import com.taskflow.mapper.RabbitMQClusterMapper;
import com.taskflow.service.RabbitMQClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RabbitMQ 集群服务实现
 */
@Slf4j
@Service
public class RabbitMQClusterServiceImpl extends ServiceImpl<RabbitMQClusterMapper, RabbitMQCluster>
        implements RabbitMQClusterService {

    @Override
    public boolean testConnection(RabbitMQCluster cluster) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(cluster.getHost());
        factory.setPort(cluster.getPort());
        factory.setVirtualHost(cluster.getVhost());
        factory.setUsername(cluster.getUsername());
        factory.setPassword(cluster.getPassword());
        factory.setConnectionTimeout(5000);

        try (Connection connection = factory.newConnection()) {
            log.info("集群连接测试成功: {}", cluster.getClusterName());

            // 更新最后连接时间
            cluster.setLastConnectTime(LocalDateTime.now());
            cluster.setStatus("ACTIVE");
            updateById(cluster);

            return true;
        } catch (Exception e) {
            log.error("集群连接测试失败: {}, 错误: {}", cluster.getClusterName(), e.getMessage());

            // 更新状态为错误
            cluster.setStatus("ERROR");
            updateById(cluster);

            return false;
        }
    }

    @Override
    public RabbitMQCluster getClusterInfo(Long clusterId) {
        return getById(clusterId);
    }

    @Override
    public List<RabbitMQCluster> getActiveClusters() {
        return list(new LambdaQueryWrapper<RabbitMQCluster>()
                .eq(RabbitMQCluster::getStatus, "ACTIVE")
                .orderByDesc(RabbitMQCluster::getCreatedAt));
    }

    @Override
    public void updateHealthScore(Long clusterId, Integer score) {
        RabbitMQCluster cluster = getById(clusterId);
        if (cluster != null) {
            cluster.setHealthScore(score);
            updateById(cluster);
        }
    }
}
