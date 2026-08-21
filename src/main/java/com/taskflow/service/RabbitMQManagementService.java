package com.taskflow.service;

import com.taskflow.dto.*;
import java.util.List;
import java.util.Map;

public interface RabbitMQManagementService {

    // ============ Overview ============
    /**
     * 获取概览信息
     */
    RabbitMQOverviewDTO getOverview(Long clusterId);

    // ============ Queues ============
    /**
     * 获取所有队列
     */
    List<RabbitMQQueueDTO> getAllQueues(Long clusterId);

    /**
     * 获取指定虚拟主机的队列
     */
    List<RabbitMQQueueDTO> getQueuesByVhost(Long clusterId, String vhost);

    /**
     * 获取队列详情
     */
    RabbitMQQueueDTO getQueueDetail(Long clusterId, String vhost, String queueName);

    /**
     * 创建队列
     */
    void createQueue(Long clusterId, String vhost, String queueName, Map<String, Object> config);

    /**
     * 清空队列
     */
    void purgeQueue(Long clusterId, String vhost, String queueName);

    /**
     * 删除队列
     */
    void deleteQueue(Long clusterId, String vhost, String queueName);

    // ============ Exchanges ============
    /**
     * 获取所有交换机
     */
    List<RabbitMQExchangeDTO> getAllExchanges(Long clusterId);

    /**
     * 获取指定虚拟主机的交换机
     */
    List<RabbitMQExchangeDTO> getExchangesByVhost(Long clusterId, String vhost);

    /**
     * 创建交换机
     */
    void createExchange(Long clusterId, String vhost, String exchangeName, Map<String, Object> config);

    /**
     * 删除交换机
     */
    void deleteExchange(Long clusterId, String vhost, String exchangeName);

    // ============ Bindings ============
    /**
     * 获取所有绑定关系
     */
    List<RabbitMQBindingDTO> getAllBindings(Long clusterId);

    /**
     * 获取队列的绑定关系
     */
    List<RabbitMQBindingDTO> getQueueBindings(Long clusterId, String vhost, String queueName);

    /**
     * 创建绑定关系
     */
    void createBinding(Long clusterId, String vhost, String source, String destination,
                      String destinationType, String routingKey, Map<String, Object> arguments);

    /**
     * 删除绑定关系
     */
    void deleteBinding(Long clusterId, String vhost, String source, String destination,
                      String destinationType, String propertiesKey);

    // ============ Messages ============
    /**
     * 获取队列中的消息
     */
    List<RabbitMQMessageDTO> getMessages(Long clusterId, String vhost, String queueName, Integer count);

    /**
     * 发布消息到交换机
     */
    void publishMessage(Long clusterId, String vhost, String exchange, String routingKey,
                       String payload, Map<String, Object> properties);

    // ============ Connections ============
    /**
     * 获取所有连接
     */
    List<RabbitMQConnectionDTO> getAllConnections(Long clusterId);

    /**
     * 关闭连接
     */
    void closeConnection(Long clusterId, String connectionName);

    // ============ Channels ============
    /**
     * 获取所有通道
     */
    List<RabbitMQChannelDTO> getAllChannels(Long clusterId);

    // ============ Virtual Hosts ============
    /**
     * 获取所有虚拟主机
     */
    List<RabbitMQVhostDTO> getAllVhosts(Long clusterId);

    /**
     * 创建虚拟主机
     */
    void createVhost(Long clusterId, String vhost);

    /**
     * 删除虚拟主机
     */
    void deleteVhost(Long clusterId, String vhost);

    // ============ Nodes ============
    /**
     * 获取所有节点
     */
    List<RabbitMQNodeDTO> getAllNodes(Long clusterId);
}
