package com.example.chatsystem.controller;

import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.MessageSendDTO;
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

    @MessageMapping("/chat.sendImageToBot")
    public void sendImageToBot(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String imageType = headerAccessor.getNativeHeader("image-type").get(0);
        String botName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleImageToBot(payload, imageType, senderName, botName);
    }

    @MessageMapping("/chat.sendToPrivate")
    public void sendToPrivate(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        webSocketService.handlePrivateMessage(messageSendDTO, senderName);
    }

    @MessageMapping("/chat.sendImageToPrivate")
    public void sendImageToPrivate(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String imageType = headerAccessor.getNativeHeader("image-type").get(0);
        String receiverName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleImageToPrivate(payload, imageType, senderName, receiverName);
    }

    @MessageMapping("/chat.sendToGroup")
    public void sendToGroup(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        webSocketService.handleGroupMessage(messageSendDTO, senderName);
    }

    @MessageMapping("/chat.sendImageToGroup")
    public void sendImageToGroup(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String imageType = headerAccessor.getNativeHeader("image-type").get(0);
        String groupName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleImageToGroup(payload, imageType, senderName, groupName);
    }
}
