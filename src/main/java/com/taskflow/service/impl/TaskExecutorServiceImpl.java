package com.taskflow.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.taskflow.entity.RabbitMQCluster;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.service.RabbitMQClusterService;
import com.taskflow.service.TaskExecutorService;
import com.taskflow.service.TaskLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

/**
 * 任务执行服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutorServiceImpl implements TaskExecutorService {

    private final RabbitMQClusterService clusterService;
    private final TaskLogService taskLogService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void executeTask(TaskDefinition task) {
        String traceId = IdUtil.fastSimpleUUID();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("开始执行任务: {}, TraceId: {}", task.getTaskName(), traceId);

        // 记录任务开始
        taskLogService.logTaskStart(task.getId(), traceId);

        try {
            String result = null;

            // 根据任务类型执行
            switch (task.getTaskType()) {
                case "MESSAGE" -> {
                    sendMessageToQueue(task, task.getTaskContent());
                    result = "消息发送成功";
                }
                case "HTTP" -> result = executeHttpTask(task);
                case "SHELL" -> result = executeShellTask(task);
                case "CODE" -> result = "代码执行功能待实现";
                default -> throw new RuntimeException("不支持的任务类型: " + task.getTaskType());
            }

            // 记录任务成功
            taskLogService.logTaskSuccess(task.getId(), traceId, startTime, result);
            log.info("任务执行成功: {}, 耗时: {}ms", task.getTaskName(),
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());

        } catch (Exception e) {
            // 记录任务失败
            taskLogService.logTaskFailure(task.getId(), traceId, startTime, e);
            log.error("任务执行失败: {}, TraceId: {}", task.getTaskName(), traceId, e);
        }
    }

    @Override
    @Async
    public void executeTaskAsync(TaskDefinition task) {
        executeTask(task);
    }

    @Override
    public void sendMessageToQueue(TaskDefinition task, String message) {
        RabbitMQCluster cluster = clusterService.getById(task.getClusterId());
        if (cluster == null) {
            throw new RuntimeException("集群不存在: " + task.getClusterId());
        }

        try {
            // 如果指定了交换机
            if (task.getExchangeName() != null && !task.getExchangeName().isEmpty()) {
                rabbitTemplate.convertAndSend(
                        task.getExchangeName(),
                        task.getRoutingKey() != null ? task.getRoutingKey() : "",
                        message
                );
                log.info("消息已发送到交换机: {}, 路由键: {}", task.getExchangeName(), task.getRoutingKey());
            }
            // 如果指定了队列
            else if (task.getQueueName() != null && !task.getQueueName().isEmpty()) {
                rabbitTemplate.convertAndSend(task.getQueueName(), message);
                log.info("消息已发送到队列: {}", task.getQueueName());
            } else {
                throw new RuntimeException("未指定交换机或队列");
            }
        } catch (Exception e) {
            log.error("发送消息失败", e);
            throw new RuntimeException("发送消息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String executeHttpTask(TaskDefinition task) {
        try {
            // 解析任务内容为 URL
            String url = task.getTaskContent();
            log.info("执行 HTTP 请求: {}", url);

            // 发送 HTTP 请求
            HttpResponse response = HttpRequest.get(url)
                    .timeout(task.getTimeoutSeconds() * 1000)
                    .execute();

            String result = response.body();
            log.info("HTTP 请求成功, 状态码: {}", response.getStatus());

            return result;
        } catch (Exception e) {
            log.error("HTTP 请求失败", e);
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String executeShellTask(TaskDefinition task) {
        try {
            String script = task.getTaskContent();
            log.info("执行 Shell 脚本");

            // 创建临时脚本文件
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", script);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待执行完成
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("脚本执行失败, 退出码: " + exitCode + "\n" + output);
            }

            log.info("Shell 脚本执行成功");
            return output.toString();

        } catch (Exception e) {
            log.error("Shell 脚本执行失败", e);
            throw new RuntimeException("Shell 脚本执行失败: " + e.getMessage(), e);
        }
    }
}
