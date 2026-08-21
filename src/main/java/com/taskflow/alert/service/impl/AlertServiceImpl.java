package com.taskflow.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.alert.model.AlertCondition;
import com.taskflow.alert.service.AlertService;
import com.taskflow.alert.service.NotificationService;
import com.taskflow.entity.AlertRecord;
import com.taskflow.entity.AlertRule;
import com.taskflow.mapper.AlertRecordMapper;
import com.taskflow.mapper.AlertRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public AlertRule createAlertRule(AlertRule rule) {
        if (rule.getStatus() == null) {
            rule.setStatus("ENABLED");
        }
        alertRuleMapper.insert(rule);
        log.info("告警规则创建成功: {}", rule.getRuleName());
        return rule;
    }

    @Override
    public void triggerAlert(Long ruleId, String title, String content, String level) {
        AlertRule rule = alertRuleMapper.selectById(ruleId);
        if (rule == null || !"ENABLED".equals(rule.getStatus())) {
            return;
        }

        // 创建告警记录
        AlertRecord record = new AlertRecord();
        record.setRuleId(ruleId);
        record.setAlertTitle(title);
        record.setAlertContent(content);
        record.setAlertLevel(level);
        record.setStatus("PENDING");

        alertRecordMapper.insert(record);

        // 发送告警
        sendAlert(record);
    }

    @Override
    public void sendAlert(AlertRecord record) {
        try {
            AlertRule rule = alertRuleMapper.selectById(record.getRuleId());
            if (rule == null) {
                return;
            }

            String channels = rule.getNotificationChannels();
            if (channels == null || channels.isEmpty()) {
                return;
            }

            String[] channelArray = channels.split(",");
            for (String channel : channelArray) {
                switch (channel.trim().toUpperCase()) {
                    case "EMAIL" -> sendEmailAlert(rule, record);
                    case "DINGTALK" -> sendDingTalkAlert(rule, record);
                    case "WECHAT" -> sendWeChatAlert(rule, record);
                    case "SMS" -> sendSmsAlert(rule, record);
                    default -> log.warn("不支持的通知渠道: {}", channel);
                }
            }

            // 更新告警状态
            record.setStatus("SENT");
            record.setSendTime(LocalDateTime.now());
            alertRecordMapper.updateById(record);

            log.info("告警发送成功: {}", record.getAlertTitle());

        } catch (Exception e) {
            log.error("告警发送失败", e);
            record.setStatus("FAILED");
            alertRecordMapper.updateById(record);
        }
    }

    @Override
    public boolean checkAlertCondition(AlertCondition condition, Object value) {
        if (!(value instanceof Number)) {
            return false;
        }

        double numValue = ((Number) value).doubleValue();
        double threshold = condition.getThreshold();

        return switch (condition.getOperator()) {
            case "GT" -> numValue > threshold;
            case "LT" -> numValue < threshold;
            case "EQ" -> numValue == threshold;
            case "GTE" -> numValue >= threshold;
            case "LTE" -> numValue <= threshold;
            default -> false;
        };
    }

    @Override
    public void alertTaskFailed(Long taskId, String taskName, String errorMessage) {
        // 查找匹配的告警规则
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getRuleType, "TASK_FAIL")
                        .eq(AlertRule::getStatus, "ENABLED")
        );

        for (AlertRule rule : rules) {
            String title = String.format("【任务失败】%s", taskName);
            String content = String.format(
                    "**任务ID**: %d\n\n**任务名称**: %s\n\n**失败时间**: %s\n\n**错误信息**: %s",
                    taskId, taskName, LocalDateTime.now(), errorMessage
            );

            triggerAlert(rule.getId(), title, content, "ERROR");
        }
    }

    @Override
    public void alertQueueBacklog(Long clusterId, String queueName, Long messageCount) {
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getRuleType, "QUEUE_BACKLOG")
                        .eq(AlertRule::getClusterId, clusterId)
                        .eq(AlertRule::getStatus, "ENABLED")
        );

        for (AlertRule rule : rules) {
            try {
                AlertCondition condition = objectMapper.readValue(
                        rule.getConditionJson(), AlertCondition.class
                );

                if (checkAlertCondition(condition, messageCount)) {
                    String title = String.format("【队列积压】%s", queueName);
                    String content = String.format(
                            "**队列名称**: %s\n\n**消息数量**: %d\n\n**阈值**: %.0f\n\n**时间**: %s",
                            queueName, messageCount, condition.getThreshold(), LocalDateTime.now()
                    );

                    triggerAlert(rule.getId(), title, content, "WARNING");
                }
            } catch (Exception e) {
                log.error("解析告警条件失败", e);
            }
        }
    }

    @Override
    public void alertConsumerOffline(Long clusterId, String queueName) {
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getRuleType, "CONSUMER_OFFLINE")
                        .eq(AlertRule::getClusterId, clusterId)
                        .eq(AlertRule::getStatus, "ENABLED")
        );

        for (AlertRule rule : rules) {
            String title = String.format("【消费者离线】%s", queueName);
            String content = String.format(
                    "**队列名称**: %s\n\n**状态**: 无活跃消费者\n\n**时间**: %s",
                    queueName, LocalDateTime.now()
            );

            triggerAlert(rule.getId(), title, content, "CRITICAL");
        }
    }

    @Override
    public void alertExecutionTimeout(Long taskId, String taskName, Long duration) {
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getRuleType, "EXECUTION_TIME")
                        .eq(AlertRule::getStatus, "ENABLED")
        );

        for (AlertRule rule : rules) {
            try {
                AlertCondition condition = objectMapper.readValue(
                        rule.getConditionJson(), AlertCondition.class
                );

                if (checkAlertCondition(condition, duration)) {
                    String title = String.format("【执行超时】%s", taskName);
                    String content = String.format(
                            "**任务ID**: %d\n\n**任务名称**: %s\n\n**执行时长**: %d 秒\n\n**阈值**: %.0f 秒\n\n**时间**: %s",
                            taskId, taskName, duration / 1000, condition.getThreshold(), LocalDateTime.now()
                    );

                    triggerAlert(rule.getId(), title, content, "WARNING");
                }
            } catch (Exception e) {
                log.error("解析告警条件失败", e);
            }
        }
    }

    private void sendEmailAlert(AlertRule rule, AlertRecord record) {
        if (rule.getNotificationUsers() != null) {
            String[] users = rule.getNotificationUsers().split(",");
            for (String user : users) {
                notificationService.sendEmail(user.trim(), record.getAlertTitle(), record.getAlertContent());
            }
        }
    }

    private void sendDingTalkAlert(AlertRule rule, AlertRecord record) {
        // TODO: 从配置中获取钉钉 webhook
        String webhook = "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN";
        notificationService.sendDingTalk(webhook, record.getAlertTitle(), record.getAlertContent());
    }

    private void sendWeChatAlert(AlertRule rule, AlertRecord record) {
        // TODO: 从配置中获取企业微信 webhook
        String webhook = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=YOUR_KEY";
        notificationService.sendWeChat(webhook, record.getAlertTitle(), record.getAlertContent());
    }

    private void sendSmsAlert(AlertRule rule, AlertRecord record) {
        if (rule.getNotificationUsers() != null) {
            String[] users = rule.getNotificationUsers().split(",");
            for (String user : users) {
                notificationService.sendSms(user.trim(), record.getAlertContent());
            }
        }
    }
}
