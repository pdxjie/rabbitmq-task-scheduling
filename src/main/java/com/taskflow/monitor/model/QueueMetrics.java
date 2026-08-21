package com.taskflow.monitor.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 队列监控指标
 */
@Data
public class QueueMetrics {

    /**
     * 队列名称
     */
    private String queueName;

    /**
     * 虚拟主机
     */
    private String vhost;

    /**
     * 消息总数
     */
    private Long messages;

    /**
     * 就绪消息数
     */
    private Long messagesReady;

    /**
     * 未确认消息数
     */
    private Long messagesUnacked;

    /**
     * 消费者数量
     */
    private Integer consumers;

    /**
     * 内存使用（字节）
     */
    private Long memory;

    /**
     * 发布速率
     */
    private Double publishRate;

    /**
     * 消费速率
     */
    private Double consumeRate;

    /**
     * 确认速率
     */
    private Double ackRate;

    /**
     * 队列状态
     */
    private String state;

    /**
     * 是否持久化
     */
    private Boolean durable;

    /**
     * 是否自动删除
     */
    private Boolean autoDelete;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
}
