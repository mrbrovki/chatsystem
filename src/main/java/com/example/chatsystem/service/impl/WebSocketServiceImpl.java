package com.example.chatsystem.service.impl;

import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.WebSocketService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void sendPrivateMessage(String receiverName, MessageReceiveDTO message) {
        messagingTemplate.convertAndSendToUser(receiverName, "/private/messages", message);
    }

    @Override
    public void sendBotMessage(String receiverName, MessageReceiveDTO message) {
        messagingTemplate.convertAndSendToUser(receiverName, "/bot/messages", message);
    }

    @Override
    public void sendGroupMessage(String groupId, MessageReceiveDTO message) {
        messagingTemplate.convertAndSend("/group/" + groupId + "/messages", message);
    }

    @Override
    public void handleMessageToBot(MessageSendDTO messageSendDTO, ObjectId senderId, String senderName) {
        String botName = messageSendDTO.getReceiverName();
        ObjectId botId = botService.getBotByName(botName).getId();

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO userMessageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderName, timestamp);

        //  persist user message
        messageService.persistBotMessage(userMessageReceiveDTO, senderId, botId);

        //  pull messages
        List<MessageDTO> messageDTOs = messageService.getBotChatMessages(senderId, senderName, botName);

        //  prompt bot
        MessageSendDTO botSendDTO = botService.handleMessage(messageDTOs, messageSendDTO, senderName);

        //  persist bot message
        MessageReceiveDTO botMessageReceiveDTO = buildMessageReceiveDTO(botSendDTO, botName, timestamp);
        messageService.persistBotMessage(botMessageReceiveDTO, botId, senderId);

        sendBotMessage(senderName, botMessageReceiveDTO);
    }

    @Override
    public void handlePrivateMessage(MessageSendDTO messageSendDTO, ObjectId senderId, String senderName) {
        String receiverName = messageSendDTO.getReceiverName();

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderName, timestamp);

        //  persist
        messageService.persistPrivateMessage(messageReceiveDTO, senderId, receiverName);

        sendPrivateMessage(receiverName, messageReceiveDTO);
    }

    @Override
    public void handleGroupMessage(MessageSendDTO messageSendDTO, ObjectId senderId, String senderName) {
        ObjectId groupId = new ObjectId(messageSendDTO.getReceiverName());

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = buildMessageReceiveDTO(messageSendDTO, senderName, timestamp);

        //  persist
        messageService.persistGroupMessage(messageReceiveDTO, senderId, groupId);

        sendGroupMessage(messageSendDTO.getReceiverName(), messageReceiveDTO);
    }

    @Override
    public void handleFileToBot(byte[] payload, MessageType messageType, ObjectId senderId,
                                String senderName, String botName){
        ObjectId botId = botService.getBotByName(botName).getId();
        InputStream inputStream = new ByteArrayInputStream(payload);

        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = MessageReceiveDTO.builder()
                .senderName(senderName)
                .timestamp(timestamp)
                .content(String.valueOf(timestamp))
                .type(messageType)
                .build();

        messageService.persistBotFile(messageReceiveDTO, inputStream, senderId, botId);

        //  send back
        MessageReceiveDTO sendBack = MessageReceiveDTO.builder()
                .senderName(botName)
                .content("I'm not enough paid to analyze your media☝️")
                .type(MessageType.TEXT)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        sendBotMessage(senderName, sendBack);
    }

    @Override
    public void handleFileToPrivate(byte[] payload, MessageType messageType, ObjectId senderId,
                                    String senderName, String receiverName) {
        InputStream inputStream = new ByteArrayInputStream(payload);
        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = MessageReceiveDTO.builder()
                .senderName(senderName)
                .timestamp(timestamp)
                .content(String.valueOf(timestamp))
                .type(messageType)
                .build();

        messageService.persistPrivateFile(messageReceiveDTO, inputStream, senderId, receiverName);

        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", messageType.getValue());
        headers.put("sender", senderName);

        messagingTemplate.convertAndSendToUser(receiverName, "/private/messages", payload, headers);
    }

    @Override
    public void handleFileToGroup(byte[] payload, MessageType messageType, ObjectId senderId,
                                  String senderName, String groupId) {
        InputStream inputStream = new ByteArrayInputStream(payload);
        long timestamp = Instant.now().toEpochMilli();
        MessageReceiveDTO messageReceiveDTO = MessageReceiveDTO.builder()
                .senderName(senderName)
                .timestamp(timestamp)
                .content(String.valueOf(timestamp))
                .type(messageType)
                .build();

        messageService.persistGroupFile(messageReceiveDTO, inputStream, senderId, new ObjectId(groupId));


        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", messageType.getValue());

        messagingTemplate.convertAndSend("/group/" + groupId + "/messages", payload ,headers);
    }

    @Override
    public void subscribeUserToGroup(String username, ObjectId groupId) {
        MessageReceiveDTO message = MessageReceiveDTO.builder()
                .content(groupId.toHexString())
                .type(MessageType.JOIN)
                .build();
        sendPrivateMessage(username, message);
    }

    @Override
    public void unsubscribeUserToGroup(String username, ObjectId groupId) {
        MessageReceiveDTO message = MessageReceiveDTO.builder()
                .content(groupId.toHexString())
                .type(MessageType.LEAVE)
                .build();
        sendPrivateMessage(username, message);
    }

    private MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, String senderName, long timestamp) {
        return MessageReceiveDTO.builder()
                .timestamp(timestamp)
                .senderName(senderName)
                .content(messageSendDTO.getContent())
                .type(messageSendDTO.getType())
                .build();
    }
}
