package com.example.chatsystem.controller;

import com.example.chatsystem.bot.ChatGPT;
import com.example.chatsystem.bot.ChatRequestDTO;
import com.example.chatsystem.bot.ChatResponseDTO;
import com.example.chatsystem.bot.Message;
import com.example.chatsystem.dto.MessageSendDTO;
import com.example.chatsystem.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

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

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageSendDTO messageSendDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderName = headerAccessor.getUser().getName();;
        webSocketService.handleMessage(messageSendDTO, senderName);
    }
}
