package com.example.chatsystem.controller;

import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private final WebSocketService webSocketService;

    @Autowired
    public ChatWebSocketController(WebSocketService webSocketService){
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/chat.sendToBot")
    public void sendToBot(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(Objects.requireNonNull(attributes).get("userId").toString());
        webSocketService.handleMessageToBot(messageSendDTO, userId);
    }

    @MessageMapping("/chat.sendFileToBot")
    public void sendFileToBot(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(Objects.requireNonNull(attributes).get("userId").toString());
        String fileType = Objects.requireNonNull(headerAccessor.getNativeHeader("file-type")).getFirst();
        UUID botId = UUID.fromString(Objects.requireNonNull(headerAccessor
                .getNativeHeader("receiver")).getFirst());
        webSocketService.handleFileToBot(payload, MessageType.fromValue(fileType), userId, botId);
    }

    @MessageMapping("/chat.sendToPrivate")
    public void sendToPrivate(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(Objects.requireNonNull(attributes).get("userId").toString());
        webSocketService.handlePrivateMessage(messageSendDTO, userId);
    }

    @MessageMapping("/chat.sendFileToPrivate")
    public void sendFileToPrivate(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(Objects.requireNonNull(attributes).get("userId").toString());
        String fileType = Objects.requireNonNull(headerAccessor.getNativeHeader("file-type")).getFirst();
        UUID receiverId = UUID.fromString(Objects.requireNonNull(headerAccessor
                .getNativeHeader("receiver")).getFirst());
        webSocketService.handleFileToPrivate(payload, MessageType.fromValue(fileType), userId, receiverId);
    }

    @MessageMapping("/chat.sendToGroup")
    public void sendToGroup(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(Objects.requireNonNull(attributes).get("userId").toString());
        webSocketService.handleGroupMessage(messageSendDTO, userId);
    }

    @MessageMapping("/chat.sendFileToGroup")
    public void sendFileToGroup(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        UUID userId = UUID.fromString(Objects.requireNonNull(attributes).get("userId").toString());
        String fileType = Objects.requireNonNull(headerAccessor.getNativeHeader("file-type")).getFirst();
        UUID groupId = UUID.fromString(Objects.requireNonNull(headerAccessor
                .getNativeHeader("receiver")).getFirst());
        webSocketService.handleFileToGroup(payload, MessageType.fromValue(fileType), userId, groupId);
    }
}
