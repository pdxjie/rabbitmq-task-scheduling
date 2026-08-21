package com.taskflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.config.RabbitMQConfig;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.service.DelayTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 延迟任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DelayTaskServiceImpl implements DelayTaskService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendDelayTask(TaskDefinition task, int delaySeconds) {
        try {
            // 选择合适的延迟队列
            String queueName = selectDelayQueue(delaySeconds);

            // 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("taskId", task.getId());
            message.put("taskName", task.getTaskName());
            message.put("taskType", task.getTaskType());
            message.put("taskContent", task.getTaskContent());
            message.put("delaySeconds", delaySeconds);
            message.put("timestamp", System.currentTimeMillis());

            // 发送到延迟队列
            rabbitTemplate.convertAndSend(queueName, objectMapper.writeValueAsString(message));

            log.info("延迟任务已发送: {}, 延迟: {}秒, 队列: {}",
                    task.getTaskName(), delaySeconds, queueName);

        } catch (Exception e) {
            log.error("发送延迟任务失败: {}", task.getTaskName(), e);
            throw new RuntimeException("发送延迟任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String selectDelayQueue(int delaySeconds) {
        // 根据延迟时间选择合适的队列
        if (delaySeconds <= 5) {
            return RabbitMQConfig.DELAY_QUEUE_5S;
        } else if (delaySeconds <= 30) {
            return RabbitMQConfig.DELAY_QUEUE_30S;
        } else if (delaySeconds <= 60) {
            return RabbitMQConfig.DELAY_QUEUE_1M;
        } else if (delaySeconds <= 300) {
            return RabbitMQConfig.DELAY_QUEUE_5M;
        } else if (delaySeconds <= 1800) {
            return RabbitMQConfig.DELAY_QUEUE_30M;
        } else {
            return RabbitMQConfig.DELAY_QUEUE_1H;
        }
    }
}
