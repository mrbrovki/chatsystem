package com.example.chatsystem.service.impl;

import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.WebSocketService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @Autowired
    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    @Override
    public void sendPrivateMessage(String receiverName, MessageReceiveDTO message) {
        messagingTemplate.convertAndSendToUser(receiverName, "/queue/messages", message);

    }
    @Override
    public void sendGroupMessage(String groupId, MessageReceiveDTO message) {
        messagingTemplate.convertAndSend("/group/" + groupId + "/queue/messages", message);
    }

    @Override
    public MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, String senderName) {
        LocalDateTime now = LocalDateTime.now();

        return MessageReceiveDTO.builder()
                .timestamp(now)
                .senderName(senderName)
                .message(messageSendDTO.getMessage())
                .type(messageSendDTO.getType())
                .build();
    }

    @Override
    public void handleMessage(MessageSendDTO messageSendDTO, String senderName) {
        MessageReceiveDTO messageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderName);
        System.out.println(messageReceiveDTO);
        System.out.println(messageSendDTO);
        //  persist
        messageService.persistMessage(messageSendDTO, messageReceiveDTO, messageSendDTO.getType());

        switch (messageReceiveDTO.getType()){
            case PRIVATE -> sendPrivateMessage(messageSendDTO.getReceiverName(), messageReceiveDTO);
            case GROUP -> sendGroupMessage(messageSendDTO.getReceiverName(), messageReceiveDTO);
        }
    }

    @Override
    public void subscribeUserToGroup(String username, ObjectId groupId) {
        MessageReceiveDTO message = MessageReceiveDTO.builder()
                .message(groupId.toHexString())
                .type(MessageType.JOIN)
                .build();
        sendPrivateMessage(username, message);
    }

    @Override
    public void unsubscribeUserToGroup(String username, ObjectId groupId) {
        MessageReceiveDTO message = MessageReceiveDTO.builder()
                .message(groupId.toHexString())
                .type(MessageType.LEAVE)
                .build();
        sendPrivateMessage(username, message);
    }
}
