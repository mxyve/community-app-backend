package top.xym.community.app.module.conversation.service;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/app/chat/{userId}")
public class WebSocketService {

    // 在线用户连接池：key=userId，value=session
    public static final Map<Long, Session> USER_SESSION_MAP = new ConcurrentHashMap<>();

    // 连接成功
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        USER_SESSION_MAP.put(userId, session);
        log.info("用户{}连接WebSocket，当前在线：{}", userId, USER_SESSION_MAP.size());
    }

    // 关闭连接
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        USER_SESSION_MAP.remove(userId);
        log.info("用户{}断开连接，当前在线：{}", userId, USER_SESSION_MAP.size());
    }

    // 收到消息
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        log.info("用户{}发来消息：{}", userId, message);
    }

    // 异常
    @OnError
    public void onError(Throwable error) {
        log.error("WebSocket异常", error);
    }

    // ====================== 推送方法 ======================
    // 给指定用户发消息
    public static void sendToUser(Long userId, String message) {
        Session session = USER_SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("推送消息给用户{}失败", userId);
            }
        }
    }
}