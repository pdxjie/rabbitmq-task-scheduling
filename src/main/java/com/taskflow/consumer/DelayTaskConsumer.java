package com.taskflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.config.RabbitMQConfig;
import com.taskflow.entity.TaskDefinition;
import com.taskflow.service.TaskDefinitionService;
import com.taskflow.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 延迟任务消费者
 * 监听执行队列，执行到期的延迟任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayTaskConsumer {

    private final TaskDefinitionService taskService;
    private final TaskExecutorService taskExecutor;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.EXECUTE_QUEUE, concurrency = "5-10")
    public void handleDelayTask(String message) {
        try {
            log.info("收到延迟任务消息: {}", message);

            // 解析消息
            @SuppressWarnings("unchecked")
            Map<String, Object> taskMessage = objectMapper.readValue(message, Map.class);

            Long taskId = Long.valueOf(taskMessage.get("taskId").toString());
            String taskName = (String) taskMessage.get("taskName");

            // 获取任务定义
            TaskDefinition task = taskService.getById(taskId);
            if (task == null) {
                log.warn("任务不存在，跳过执行: {}", taskId);
                return;
            }

            // 检查任务状态
            if (!"ENABLED".equals(task.getStatus())) {
                log.warn("任务未启用，跳过执行: {}", taskName);
                return;
            }

            // 执行任务
            log.info("开始执行延迟任务: {}", taskName);
            taskExecutor.executeTask(task);

        } catch (Exception e) {
            log.error("处理延迟任务失败: {}", message, e);
            // 消息会重新入队，根据重试策略决定是否继续重试
        }
    }
}
