package com.taskflow.alert.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.taskflow.alert.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendEmail(String to, String subject, String content) {
        log.info("发送邮件: to={}, subject={}", to, subject);
        // TODO: 实现邮件发送（使用 Spring Mail）
    }

    @Override
    public void sendDingTalk(String webhook, String title, String content) {
        try {
            JSONObject message = new JSONObject();
            message.set("msgtype", "markdown");

            JSONObject markdown = new JSONObject();
            markdown.set("title", title);
            markdown.set("text", content);
            message.set("markdown", markdown);

            String result = HttpRequest.post(webhook)
                    .body(JSONUtil.toJsonStr(message))
                    .execute()
                    .body();

            log.info("钉钉通知发送成功: {}", result);
        } catch (Exception e) {
            log.error("钉钉通知发送失败", e);
        }
    }

    @Override
    public void sendWeChat(String webhook, String title, String content) {
        try {
            JSONObject message = new JSONObject();
            message.set("msgtype", "markdown");

            JSONObject markdown = new JSONObject();
            markdown.set("content", "## " + title + "\n" + content);
            message.set("markdown", markdown);

            String result = HttpRequest.post(webhook)
                    .body(JSONUtil.toJsonStr(message))
                    .execute()
                    .body();

            log.info("企业微信通知发送成功: {}", result);
        } catch (Exception e) {
            log.error("企业微信通知发送失败", e);
        }
    }

    @Override
    public void sendSms(String phone, String content) {
        log.info("发送短信: phone={}, content={}", phone, content);
        // TODO: 实现短信发送（接入阿里云、腾讯云等短信服务）
    }
}
