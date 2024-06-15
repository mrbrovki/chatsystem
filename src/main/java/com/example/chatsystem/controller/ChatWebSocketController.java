package com.example.chatsystem.controller;

import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.dto.MessageResponseDTO;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.User;
import com.example.chatsystem.security.JwtService;
import com.example.chatsystem.model.Message;
import com.example.chatsystem.service.ChatService;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Objects;

@Controller
public class ChatWebSocketController {

    public SimpMessagingTemplate messagingTemplate;
    public JwtService jwtService;
    public UserService userService;
    public MessageService messageService;
    public ChatService chatService;

    @Autowired
    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, JwtService jwtService, UserService userService, MessageService messageService, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.jwtService = jwtService;
        this.userService = userService;
        this.messageService = messageService;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendPublic")
    @SendTo("/topic/public")
    public Message sendPublic(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        message.setSenderId((ObjectId) headerAccessor.getSessionAttributes().get("senderId"));
        //persist data
        return message;
    }

    @MessageMapping("/chat.sendPrivate")
    public void sendPrivate(@Payload MessageDTO messageDTO, SimpMessageHeaderAccessor headerAccessor) {
        String senderIdStr = (String) headerAccessor.getSessionAttributes().get("senderId");
        String receiverIdStr = messageDTO.getReceiverId();
        LocalDateTime now = LocalDateTime.now();

        MessageResponseDTO messageResponseDTO = new MessageResponseDTO(now,
                messageDTO.getMessage(),
                senderIdStr, messageDTO.getMessage());

        //  persist
        Message message = new Message();
        message.setSenderId(new ObjectId(senderIdStr));
        message.setReceiverId(new ObjectId(receiverIdStr));
        message.setTimestamp(now);
        message.setId(message.getSenderId() + message.getTimestamp().toString());

        User user1 = userService.findById(message.getSenderId());
        User user2 = userService.findById(message.getReceiverId());

        System.out.println(user2);

        String collectionName = chatService.createPrivateChatCollection(user1.getUserId(), user2.getUserId());
        messageService.saveMessage(collectionName, message);
        messagingTemplate.convertAndSendToUser(user2.getEmail(),"/queue/messages", messageResponseDTO);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public Message addUser(@Payload MessageResponseDTO message, SimpMessageHeaderAccessor headerAccessor) {
        String token = headerAccessor.getNativeHeader("Authorization").get(0).substring(7);

        String messageSenderName = jwtService.extractUsername(token);
        ObjectId messageSenderId = userService.findByUsername(messageSenderName).getUserId();

        message.setSenderId(messageSenderId.toHexString());
        Message message1 = new Message();
        message1.setSenderId(messageSenderId);
        System.out.println(message.getSenderId());

        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("senderId", messageSenderId.toHexString());
        return message1;
    }
}
