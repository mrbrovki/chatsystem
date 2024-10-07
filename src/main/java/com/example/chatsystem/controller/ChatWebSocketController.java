package com.example.chatsystem.controller;

import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final WebSocketService webSocketService;

    @Autowired
    public ChatWebSocketController(WebSocketService webSocketService){
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/chat.sendToBot")
    public void sendToBot(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        webSocketService.handleMessageToBot(messageSendDTO, senderName);
    }

    @MessageMapping("/chat.sendFileToBot")
    public void sendFileToBot(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String fileType = headerAccessor.getNativeHeader("file-type").get(0);
        String botName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleFileToBot(payload, MessageType.fromValue(fileType), senderName, botName);
    }

    @MessageMapping("/chat.sendToPrivate")
    public void sendToPrivate(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        webSocketService.handlePrivateMessage(messageSendDTO, senderName);
    }

    @MessageMapping("/chat.sendFileToPrivate")
    public void sendFileToPrivate(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String fileType = headerAccessor.getNativeHeader("file-type").get(0);
        String receiverName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleFileToPrivate(payload, MessageType.fromValue(fileType), senderName, receiverName);
    }

    @MessageMapping("/chat.sendToGroup")
    public void sendToGroup(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        webSocketService.handleGroupMessage(messageSendDTO, senderName);
    }

    @MessageMapping("/chat.sendFileToGroup")
    public void sendFileToGroup(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String fileType = headerAccessor.getNativeHeader("file-type").get(0);
        String groupName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleFileToGroup(payload, MessageType.fromValue(fileType), senderName, groupName);
    }
}
