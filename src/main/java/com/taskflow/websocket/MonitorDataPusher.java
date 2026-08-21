package com.taskflow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.monitor.model.SystemMetrics;
import com.taskflow.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控数据推送器
 * 定时推送系统监控数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorDataPusher {

    private final MonitorService monitorService;
    private final ObjectMapper objectMapper;

    /**
     * 每 5 秒推送一次系统监控数据
     */
    @Scheduled(fixedRate = 5000)
    public void pushSystemMetrics() {
        try {
            if (MonitorWebSocket.getOnlineCount() == 0) {
                return;
            }

            SystemMetrics metrics = monitorService.getSystemMetrics();

            Map<String, Object> message = new HashMap<>();
            message.put("type", "system_metrics");
            message.put("data", metrics);
            message.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(message);
            MonitorWebSocket.broadcast(json);

            log.debug("推送系统监控数据, 在线客户端: {}", MonitorWebSocket.getOnlineCount());

        } catch (Exception e) {
            log.error("推送系统监控数据失败", e);
        }
    }

    /**
     * 推送告警消息
     */
    public void pushAlert(String title, String content, String level) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "alert");
            message.put("data", Map.of(
                    "title", title,
                    "content", content,
                    "level", level
            ));
            message.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(message);
            MonitorWebSocket.broadcast(json);

            log.info("推送告警消息: {}", title);

        } catch (Exception e) {
            log.error("推送告警消息失败", e);
        }
    }

    /**
     * 推送任务状态更新
     */
    public void pushTaskStatus(Long taskId, String taskName, String status) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "task_status");
            message.put("data", Map.of(
                    "taskId", taskId,
                    "taskName", taskName,
                    "status", status
            ));
            message.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(message);
            MonitorWebSocket.broadcast(json);

            log.debug("推送任务状态: taskId={}, status={}", taskId, status);

        } catch (Exception e) {
            log.error("推送任务状态失败", e);
        }
    }
}
