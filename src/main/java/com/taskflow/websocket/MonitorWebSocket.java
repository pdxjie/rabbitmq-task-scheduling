package com.taskflow.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 服务端点
 * 实时推送监控数据和告警消息
 */
@Slf4j
@Component
@ServerEndpoint("/ws/monitor/{clientId}")
public class MonitorWebSocket {

    /**
     * 在线客户端集合（线程安全）
     */
    private static final CopyOnWriteArraySet<MonitorWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 客户端会话映射（clientId -> Session）
     */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 当前会话
     */
    private Session session;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) {
        this.session = session;
        this.clientId = clientId;

        webSocketSet.add(this);
        sessionMap.put(clientId, session);

        log.info("WebSocket 连接建立: clientId={}, 当前在线数: {}", clientId, webSocketSet.size());

        // 发送欢迎消息
        sendMessage("连接成功，开始接收实时数据");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        sessionMap.remove(clientId);

        log.info("WebSocket 连接关闭: clientId={}, 当前在线数: {}", clientId, webSocketSet.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息: clientId={}, message={}", clientId, message);

        // 处理客户端消息（如订阅特定数据）
        if (message.startsWith("subscribe:")) {
            String topic = message.substring(10);
            log.info("客户端订阅: clientId={}, topic={}", clientId, topic);
        }
    }

    /**
     * 发生错误时调用的方法
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误: clientId={}", clientId, error);
    }

    /**
     * 向当前客户端发送消息
     */
    public void sendMessage(String message) {
        try {
            if (this.session != null && this.session.isOpen()) {
                this.session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error("发送消息失败: clientId={}", clientId, e);
        }
    }

    /**
     * 向指定客户端发送消息
     */
    public static void sendMessageToClient(String clientId, String message) {
        Session session = sessionMap.get(clientId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("发送消息到客户端: clientId={}", clientId);
            } catch (IOException e) {
                log.error("发送消息失败: clientId={}", clientId, e);
            }
        }
    }

    /**
     * 广播消息给所有客户端
     */
    public static void broadcast(String message) {
        for (MonitorWebSocket client : webSocketSet) {
            client.sendMessage(message);
        }
        log.debug("广播消息给所有客户端, 在线数: {}", webSocketSet.size());
    }

    /**
     * 获取在线客户端数量
     */
    public static int getOnlineCount() {
        return webSocketSet.size();
    }
}
