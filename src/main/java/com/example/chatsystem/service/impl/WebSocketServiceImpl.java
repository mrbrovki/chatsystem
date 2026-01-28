package com.example.chatsystem.service.impl;

import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.ChatState;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.chatsystem.utils.LocalServer.wolLocalServer;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final BotService botService;

    @Autowired
    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate, MessageService messageService, BotService botService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.botService = botService;
    }

    @Override
    public void sendPrivateMessage(UUID receiverId, MessageReceiveDTO message) {
        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/private/messages", message);
    }

    @Override
    public void sendBotMessage(UUID receiverId, MessageReceiveDTO message) {
        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/bot/messages", message);
    }

    @Override
    public void sendGroupMessage(UUID groupId, MessageReceiveDTO message) {
        messagingTemplate.convertAndSend("/group/" + groupId.toString() + "/messages", message);
    }

    @Override
    public void handleMessageToBot(MessageSendDTO messageSendDTO, UUID senderId) {
        UUID botId = messageSendDTO.getReceiverId();

        MessageReceiveDTO typingMessage = MessageReceiveDTO.builder()
                .content(ChatState.TYPING.name())
                .type(MessageType.STATE)
                .senderId(botId)
                .build();
        sendBotMessage(senderId, typingMessage);

        //wolLocalServer();

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO userMessageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderId, timestamp);

        //  persist user message
        messageService.persistBotMessage(userMessageReceiveDTO, senderId, botId);

        //  pull messages
        List<MessageDTO> messageDTOs = messageService.getBotChatMessages(senderId, botId);

        //  prompt bot
        MessageSendDTO botSendDTO = botService.handleMessage(messageDTOs, messageSendDTO, senderId);

        //  persist bot message
        MessageReceiveDTO botMessageReceiveDTO = buildMessageReceiveDTO(botSendDTO, botId, timestamp);
        messageService.persistBotMessage(botMessageReceiveDTO, botId, senderId);

        sendBotMessage(senderId, botMessageReceiveDTO);
    }

    @Override
    public void handlePrivateMessage(MessageSendDTO messageSendDTO, UUID senderId) {
        UUID receiverId = messageSendDTO.getReceiverId();

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderId, timestamp);

        //  persist
        messageService.persistPrivateMessage(messageReceiveDTO, senderId, receiverId);

        sendPrivateMessage(receiverId, messageReceiveDTO);
    }

    @Override
    public void handleGroupMessage(MessageSendDTO messageSendDTO, UUID senderId) {
        UUID groupId = messageSendDTO.getReceiverId();

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderId, timestamp);

        //  persist
        messageService.persistGroupMessage(messageReceiveDTO, senderId, groupId);

        sendGroupMessage(messageSendDTO.getReceiverId(), messageReceiveDTO);
    }

    @Override
    public void handleFileToBot(byte[] payload, MessageType messageType, UUID senderId, UUID botId){
        InputStream inputStream = new ByteArrayInputStream(payload);

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = MessageReceiveDTO.builder()
                .senderId(senderId)
                .timestamp(timestamp)
                .content(String.valueOf(timestamp))
                .type(messageType)
                .build();

        //wolLocalServer();

        messageService.persistBotFile(messageReceiveDTO, inputStream, senderId, botId);

        //  send back
        MessageReceiveDTO sendBack = MessageReceiveDTO.builder()
                .senderId(botId)
                .content("I'm not enough paid to analyze your media☝️")
                .type(MessageType.TEXT)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        sendBotMessage(senderId, sendBack);
    }

    @Override
    public void handleFileToPrivate(byte[] payload, MessageType messageType, UUID senderId,
                                    UUID receiverId) {
        InputStream inputStream = new ByteArrayInputStream(payload);
        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = MessageReceiveDTO.builder()
                .senderId(senderId)
                .timestamp(timestamp)
                .content(String.valueOf(timestamp))
                .type(messageType)
                .build();

        messageService.persistPrivateFile(messageReceiveDTO, inputStream, senderId, receiverId);

        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", messageType.getValue());
        headers.put("sender", senderId);

        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/private/messages", payload, headers);
    }

    @Override
    public void handleFileToGroup(byte[] payload, MessageType messageType, UUID senderId,
                                  UUID groupId) {
        InputStream inputStream = new ByteArrayInputStream(payload);
        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = MessageReceiveDTO.builder()
                .senderId(senderId)
                .timestamp(timestamp)
                .content(String.valueOf(timestamp))
                .type(messageType)
                .build();

        messageService.persistGroupFile(messageReceiveDTO, inputStream, senderId, groupId);


        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", messageType.getValue());
        headers.put("sender", senderId);

        messagingTemplate.convertAndSend("/group/" + groupId + "/messages", payload ,headers);
    }

    @Override
    public void subscribeUserToGroup(UUID userId, UUID groupId) {
        MessageReceiveDTO message = MessageReceiveDTO.builder()
                .content(groupId.toString())
                .type(MessageType.JOIN)
                .build();
        sendPrivateMessage(userId, message);
    }

    @Override
    public void unsubscribeUserFromGroup(UUID userId, UUID groupId) {
        MessageReceiveDTO message = MessageReceiveDTO.builder()
                .content(groupId.toString())
                .type(MessageType.LEAVE)
                .build();
        sendPrivateMessage(userId, message);
    }

    private MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, UUID senderId, long timestamp) {
        return MessageReceiveDTO.builder()
                .timestamp(timestamp)
                .senderId(senderId)
                .content(messageSendDTO.getContent())
                .type(messageSendDTO.getType())
                .build();
    }
}
