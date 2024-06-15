package com.example.chatsystem.controller;

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

    @MessageMapping("/chat.sendMessage")
    public void sendPrivate(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();;
        webSocketService.handleMessage(messageSendDTO, senderName);
    }
}
