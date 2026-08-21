package com.taskflow.monitor.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 集群监控指标
 */
@Data
public class ClusterMetrics {

    /**
     * 集群ID
     */
    private Long clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 节点数量
     */
    private Integer nodeCount;

    /**
     * 队列总数
     */
    private Integer queueCount;

    /**
     * 交换机总数
     */
    private Integer exchangeCount;

    /**
     * 连接数
     */
    private Integer connectionCount;

    /**
     * 通道数
     */
    private Integer channelCount;

    /**
     * 消费者数量
     */
    private Integer consumerCount;

    /**
     * 消息总数
     */
    private Long totalMessages;

    /**
     * 就绪消息数
     */
    private Long readyMessages;

    /**
     * 未确认消息数
     */
    private Long unackedMessages;

    /**
     * 发布速率（消息/秒）
     */
    private Double publishRate;

    /**
     * 消费速率（消息/秒）
     */
    private Double consumeRate;

    /**
     * 确认速率（消息/秒）
     */
    private Double ackRate;

    /**
     * 内存使用（MB）
     */
    private Long memoryUsed;

    /**
     * 内存限制（MB）
     */
    private Long memoryLimit;

    /**
     * 磁盘使用（MB）
     */
    private Long diskUsed;

    /**
     * 磁盘限制（MB）
     */
    private Long diskLimit;

    /**
     * 文件描述符使用
     */
    private Integer fdUsed;

    /**
     * 文件描述符限制
     */
    private Integer fdLimit;

    /**
     * 运行时间（秒）
     */
    private Long uptime;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
}
