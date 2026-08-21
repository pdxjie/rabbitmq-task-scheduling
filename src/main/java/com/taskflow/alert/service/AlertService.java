package com.taskflow.alert.service;

import com.taskflow.alert.model.AlertCondition;
import com.taskflow.entity.AlertRecord;
import com.taskflow.entity.AlertRule;

/**
 * 告警服务接口
 */
public interface AlertService {

    /**
     * 创建告警规则
     */
    AlertRule createAlertRule(AlertRule rule);

    /**
     * 触发告警
     */
    void triggerAlert(Long ruleId, String title, String content, String level);

    /**
     * 发送告警通知
     */
    void sendAlert(AlertRecord record);

    /**
     * 检查告警条件
     */
    boolean checkAlertCondition(AlertCondition condition, Object value);

    /**
     * 任务失败告警
     */
    void alertTaskFailed(Long taskId, String taskName, String errorMessage);

    /**
     * 队列积压告警
     */
    void alertQueueBacklog(Long clusterId, String queueName, Long messageCount);

    /**
     * 消费者离线告警
     */
    void alertConsumerOffline(Long clusterId, String queueName);

    /**
     * 执行时长告警
     */
    void alertExecutionTimeout(Long taskId, String taskName, Long duration);
}
