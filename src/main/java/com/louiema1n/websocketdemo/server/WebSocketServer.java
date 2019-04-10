package com.louiema1n.websocketdemo.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.louiema1n.websocketdemo.domain.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端
 */
@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    // 全部在线会话 Concurrent - 线程安全的
    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();

    /**
     * 当客户端请求连接
     * 1.添加会话对象；2.更新在线人数
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) throws JsonProcessingException {
        onlineSessions.put(session.getId(), session);
        sendMsgToAll(Message.jsonStr(Message.ENTER, "", "", onlineSessions.size()));

    }

    /**
     * 关闭连接
     * @param session
     * @throws JsonProcessingException
     */
    @OnClose
    public void onClose(Session session) throws JsonProcessingException {
        onlineSessions.remove(session.getId());
        sendMsgToAll(Message.jsonStr(Message.QUIT, "", "下线了", onlineSessions.size()));
    }

    /**
     * 公共方法
     * 发送消息给所有在线会话
     *
     * @param msg
     */
    private static void sendMsgToAll(String msg) {
        onlineSessions.forEach((id, session) -> {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        });
    }
}
