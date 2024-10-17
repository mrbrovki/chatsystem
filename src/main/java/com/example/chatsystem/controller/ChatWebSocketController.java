package com.example.chatsystem.controller;

import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.service.WebSocketService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Objects;

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
        ObjectId userId = (ObjectId) Objects.requireNonNull(attributes).get("userId");
        //String username = (String) attributes.get("username");
        webSocketService.handleMessageToBot(messageSendDTO, userId);
    }

    @MessageMapping("/chat.sendFileToBot")
    public void sendFileToBot(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        ObjectId userId = (ObjectId) Objects.requireNonNull(attributes).get("userId");
        //String username = (String) attributes.get("username");
        String fileType = Objects.requireNonNull(headerAccessor.getNativeHeader("file-type")).getFirst();
        String botName = Objects.requireNonNull(headerAccessor.getNativeHeader("receiver-name")).getFirst();
        webSocketService.handleFileToBot(payload, MessageType.fromValue(fileType), userId, botName);
    }

    @MessageMapping("/chat.sendToPrivate")
    public void sendToPrivate(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        ObjectId userId = (ObjectId) Objects.requireNonNull(attributes).get("userId");
        //String username = (String) attributes.get("username");
        webSocketService.handlePrivateMessage(messageSendDTO, userId);
    }

    @MessageMapping("/chat.sendFileToPrivate")
    public void sendFileToPrivate(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        ObjectId userId = (ObjectId) Objects.requireNonNull(attributes).get("userId");
        //String username = (String) attributes.get("username");
        String fileType = Objects.requireNonNull(headerAccessor.getNativeHeader("file-type")).getFirst();
        String receiverName = Objects.requireNonNull(headerAccessor.getNativeHeader("receiver-name")).getFirst();
        webSocketService.handleFileToPrivate(payload, MessageType.fromValue(fileType), userId, receiverName);
    }

    @MessageMapping("/chat.sendToGroup")
    public void sendToGroup(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        ObjectId userId = (ObjectId) Objects.requireNonNull(attributes).get("userId");
        //String username = (String) attributes.get("username");
        webSocketService.handleGroupMessage(messageSendDTO, userId);
    }

    @MessageMapping("/chat.sendFileToGroup")
    public void sendFileToGroup(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        ObjectId userId = (ObjectId) Objects.requireNonNull(attributes).get("userId");
        //String username = (String) attributes.get("username");
        String fileType = Objects.requireNonNull(headerAccessor.getNativeHeader("file-type")).getFirst();
        String groupName = Objects.requireNonNull(headerAccessor.getNativeHeader("receiver-name")).getFirst();
        webSocketService.handleFileToGroup(payload, MessageType.fromValue(fileType), userId, groupName);
    }
}
