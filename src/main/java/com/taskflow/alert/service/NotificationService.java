package com.taskflow.alert.service;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送邮件通知
     */
    void sendEmail(String to, String subject, String content);

    /**
     * 发送钉钉通知
     */
    void sendDingTalk(String webhook, String title, String content);

    /**
     * 发送企业微信通知
     */
    void sendWeChat(String webhook, String title, String content);

    /**
     * 发送短信通知
     */
    void sendSms(String phone, String content);
}
