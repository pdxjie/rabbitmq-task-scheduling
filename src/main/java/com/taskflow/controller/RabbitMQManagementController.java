package com.taskflow.controller;

import com.taskflow.common.Result;
import com.taskflow.dto.*;
import com.taskflow.service.RabbitMQManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rabbitmq")
@RequiredArgsConstructor
public class RabbitMQManagementController {

    private final RabbitMQManagementService managementService;

    // ============ Overview ============

    /**
     * 获取概览信息
     */
    @GetMapping("/overview")
    public Result<RabbitMQOverviewDTO> getOverview(@RequestParam Long clusterId) {
        RabbitMQOverviewDTO overview = managementService.getOverview(clusterId);
        return Result.success(overview);
    }

    // ============ Queues ============

    /**
     * 获取所有队列
     */
    @GetMapping("/queues")
    public Result<List<RabbitMQQueueDTO>> getAllQueues(@RequestParam Long clusterId) {
        List<RabbitMQQueueDTO> queues = managementService.getAllQueues(clusterId);
        return Result.success(queues);
    }

    /**
     * 获取指定虚拟主机的队列
     */
    @GetMapping("/queues/{vhost}")
    public Result<List<RabbitMQQueueDTO>> getQueuesByVhost(
            @RequestParam Long clusterId,
            @PathVariable String vhost) {
        List<RabbitMQQueueDTO> queues = managementService.getQueuesByVhost(clusterId, vhost);
        return Result.success(queues);
    }

    /**
     * 获取队列详情
     */
    @GetMapping("/queue/detail")
    public Result<RabbitMQQueueDTO> getQueueDetail(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String queueName) {
        RabbitMQQueueDTO queue = managementService.getQueueDetail(clusterId, vhost, queueName);
        return Result.success(queue);
    }

    /**
     * 创建队列
     */
    @PostMapping("/queue")
    public Result<Void> createQueue(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String queueName,
            @RequestBody Map<String, Object> config) {
        managementService.createQueue(clusterId, vhost, queueName, config);
        return Result.success();
    }

    /**
     * 清空队列
     */
    @DeleteMapping("/queue/purge")
    public Result<Void> purgeQueue(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String queueName) {
        managementService.purgeQueue(clusterId, vhost, queueName);
        return Result.success();
    }

    /**
     * 删除队列
     */
    @DeleteMapping("/queue")
    public Result<Void> deleteQueue(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String queueName) {
        managementService.deleteQueue(clusterId, vhost, queueName);
        return Result.success();
    }

    // ============ Exchanges ============

    /**
     * 获取所有交换机
     */
    @GetMapping("/exchanges")
    public Result<List<RabbitMQExchangeDTO>> getAllExchanges(@RequestParam Long clusterId) {
        List<RabbitMQExchangeDTO> exchanges = managementService.getAllExchanges(clusterId);
        return Result.success(exchanges);
    }

    /**
     * 获取指定虚拟主机的交换机
     */
    @GetMapping("/exchanges/{vhost}")
    public Result<List<RabbitMQExchangeDTO>> getExchangesByVhost(
            @RequestParam Long clusterId,
            @PathVariable String vhost) {
        List<RabbitMQExchangeDTO> exchanges = managementService.getExchangesByVhost(clusterId, vhost);
        return Result.success(exchanges);
    }

    /**
     * 创建交换机
     */
    @PostMapping("/exchange")
    public Result<Void> createExchange(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String exchangeName,
            @RequestBody Map<String, Object> config) {
        managementService.createExchange(clusterId, vhost, exchangeName, config);
        return Result.success();
    }

    /**
     * 删除交换机
     */
    @DeleteMapping("/exchange")
    public Result<Void> deleteExchange(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String exchangeName) {
        managementService.deleteExchange(clusterId, vhost, exchangeName);
        return Result.success();
    }

    // ============ Bindings ============

    /**
     * 获取所有绑定关系
     */
    @GetMapping("/bindings")
    public Result<List<RabbitMQBindingDTO>> getAllBindings(@RequestParam Long clusterId) {
        List<RabbitMQBindingDTO> bindings = managementService.getAllBindings(clusterId);
        return Result.success(bindings);
    }

    /**
     * 获取队列的绑定关系
     */
    @GetMapping("/queue/bindings")
    public Result<List<RabbitMQBindingDTO>> getQueueBindings(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String queueName) {
        List<RabbitMQBindingDTO> bindings = managementService.getQueueBindings(clusterId, vhost, queueName);
        return Result.success(bindings);
    }

    /**
     * 创建绑定关系
     */
    @PostMapping("/binding")
    public Result<Void> createBinding(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String destinationType,
            @RequestParam String routingKey,
            @RequestBody(required = false) Map<String, Object> arguments) {
        managementService.createBinding(clusterId, vhost, source, destination, destinationType, routingKey, arguments);
        return Result.success();
    }

    /**
     * 删除绑定关系
     */
    @DeleteMapping("/binding")
    public Result<Void> deleteBinding(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String destinationType,
            @RequestParam String propertiesKey) {
        managementService.deleteBinding(clusterId, vhost, source, destination, destinationType, propertiesKey);
        return Result.success();
    }

    // ============ Messages ============

    /**
     * 获取队列中的消息
     */
    @GetMapping("/messages")
    public Result<List<RabbitMQMessageDTO>> getMessages(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String queueName,
            @RequestParam(defaultValue = "10") Integer count) {
        List<RabbitMQMessageDTO> messages = managementService.getMessages(clusterId, vhost, queueName, count);
        return Result.success(messages);
    }

    /**
     * 发布消息
     */
    @PostMapping("/message/publish")
    public Result<Void> publishMessage(
            @RequestParam Long clusterId,
            @RequestParam String vhost,
            @RequestParam String exchange,
            @RequestParam String routingKey,
            @RequestParam String payload,
            @RequestBody(required = false) Map<String, Object> properties) {
        managementService.publishMessage(clusterId, vhost, exchange, routingKey, payload, properties);
        return Result.success();
    }

    // ============ Connections ============

    /**
     * 获取所有连接
     */
    @GetMapping("/connections")
    public Result<List<RabbitMQConnectionDTO>> getAllConnections(@RequestParam Long clusterId) {
        List<RabbitMQConnectionDTO> connections = managementService.getAllConnections(clusterId);
        return Result.success(connections);
    }

    /**
     * 关闭连接
     */
    @DeleteMapping("/connection")
    public Result<Void> closeConnection(
            @RequestParam Long clusterId,
            @RequestParam String connectionName) {
        managementService.closeConnection(clusterId, connectionName);
        return Result.success();
    }

    // ============ Channels ============

    /**
     * 获取所有通道
     */
    @GetMapping("/channels")
    public Result<List<RabbitMQChannelDTO>> getAllChannels(@RequestParam Long clusterId) {
        List<RabbitMQChannelDTO> channels = managementService.getAllChannels(clusterId);
        return Result.success(channels);
    }

    // ============ Virtual Hosts ============

    /**
     * 获取所有虚拟主机
     */
    @GetMapping("/vhosts")
    public Result<List<RabbitMQVhostDTO>> getAllVhosts(@RequestParam Long clusterId) {
        List<RabbitMQVhostDTO> vhosts = managementService.getAllVhosts(clusterId);
        return Result.success(vhosts);
    }

    /**
     * 创建虚拟主机
     */
    @PostMapping("/vhost")
    public Result<Void> createVhost(
            @RequestParam Long clusterId,
            @RequestParam String vhost) {
        managementService.createVhost(clusterId, vhost);
        return Result.success();
    }

    /**
     * 删除虚拟主机
     */
    @DeleteMapping("/vhost")
    public Result<Void> deleteVhost(
            @RequestParam Long clusterId,
            @RequestParam String vhost) {
        managementService.deleteVhost(clusterId, vhost);
        return Result.success();
    }

    // ============ Nodes ============

    /**
     * 获取所有节点
     */
    @GetMapping("/nodes")
    public Result<List<RabbitMQNodeDTO>> getAllNodes(@RequestParam Long clusterId) {
        List<RabbitMQNodeDTO> nodes = managementService.getAllNodes(clusterId);
        return Result.success(nodes);
    }
}
