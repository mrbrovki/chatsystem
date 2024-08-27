package com.example.chatsystem.controller;

import com.example.chatsystem.bot.ChatGPT;
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
    private final ChatGPT chatGPT;

    @Autowired
    public ChatWebSocketController(WebSocketService webSocketService, ChatGPT chatGPT){
        this.webSocketService = webSocketService;
        this.chatGPT = chatGPT;
    }

    @MessageMapping("/chat.sendToBot")
    public void sendToBot(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        chatGPT.handleMessageToBot(messageSendDTO, senderName);
    }

    @MessageMapping("/chat.sendImage")
    public void sendImage(@Payload byte[] payload, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        String imageType = headerAccessor.getNativeHeader("image-type").get(0);
        String receiverName = headerAccessor.getNativeHeader("receiver-name").get(0);
        webSocketService.handleImage(payload, imageType, senderName, receiverName);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();
        webSocketService.handleMessage(messageSendDTO, senderName);
    }
}
