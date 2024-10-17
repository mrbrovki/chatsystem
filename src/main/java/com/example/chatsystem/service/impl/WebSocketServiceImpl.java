package com.example.chatsystem.service.impl;

import com.example.chatsystem.bot.BotService;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.model.User;
import com.example.chatsystem.security.MyUserDetails;
import com.example.chatsystem.service.MessageService;
import com.example.chatsystem.service.UserService;
import com.example.chatsystem.service.WebSocketService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final BotService botService;
    private final UserService userService;

    @Autowired
    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate, MessageService messageService, BotService botService, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.botService = botService;
        this.userService = userService;
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
    public void handleMessageToBot(MessageSendDTO messageSendDTO, ObjectId senderId) {
        String botName = messageSendDTO.getReceiverName();
        User sender = userService.findById(senderId);

        //  persist user message
        MessageReceiveDTO userMessageReceiveDTO = messageService.buildMessageReceiveDTO(messageSendDTO, sender.getUsername());
        ObjectId botId = botService.getBotByName(botName).getId();
        messageService.persistBotMessage(userMessageReceiveDTO, senderId, botId);

        //  pull messages
        MyUserDetails userDetails = new MyUserDetails(sender.getUsername(), null,
                null, senderId.toHexString(), null);
        List<MessageDTO> messageDTOs = messageService.getBotChatMessages(userDetails, botName);

        //  prompt bot
        MessageSendDTO botSendDTO = botService.handleMessage(messageDTOs, messageSendDTO, sender.getUsername());

        //  persist bot message
        MessageReceiveDTO botMessageReceiveDTO = messageService.buildMessageReceiveDTO(botSendDTO, botName);
        messageService.persistBotMessage(botMessageReceiveDTO, botId, senderId);

        sendBotMessage(botSendDTO.getReceiverName(), botMessageReceiveDTO);
    }

    @Override
    public void handlePrivateMessage(MessageSendDTO messageSendDTO, ObjectId senderId){
        User sender = userService.findById(senderId);
        MessageReceiveDTO messageReceiveDTO = messageService.buildMessageReceiveDTO(messageSendDTO, sender.getUsername());

        //  persist
        messageService.persistPrivateMessage(messageSendDTO, messageReceiveDTO);

        sendPrivateMessage(messageSendDTO.getReceiverName(), messageReceiveDTO);
    }

    @Override
    public void handleGroupMessage(MessageSendDTO messageSendDTO, ObjectId senderId){
        User sender = userService.findById(senderId);
        MessageReceiveDTO messageReceiveDTO = messageService.buildMessageReceiveDTO(messageSendDTO, sender.getUsername());
        //  persist
        messageService.persistGroupMessage(messageSendDTO, messageReceiveDTO);

        sendGroupMessage(messageSendDTO.getReceiverName(), messageReceiveDTO);
    }

    @Override
    public void handleFileToBot(byte[] payload, MessageType messageType, ObjectId senderId, String botName){
        User sender = userService.findById(senderId);
        ObjectId receiverId = botService.getBotByName(botName).getId();

        messageService.persistBotFile(new ByteArrayInputStream(payload), messageType, senderId, receiverId, ChatType.BOT);
        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", messageType.getValue());

        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/private/messages", payload, headers);
    }

    @Override
    public void handleFileToPrivate(byte[] payload, MessageType messageType, ObjectId senderId, String receiverName) {
        User sender = userService.findById(senderId);
        messageService.persistPrivateFile(new ByteArrayInputStream(payload),
                messageType, sender.getUsername(), receiverName, ChatType.PRIVATE);

        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", messageType.getValue());
        headers.put("sender", sender.getUsername());
        messagingTemplate.convertAndSendToUser(receiverName, "/private/messages", payload, headers);
    }

    @Override
    public void handleFileToGroup(byte[] payload, MessageType messageType, ObjectId senderId, String groupId) {
        messageService.persistGroupFile(new ByteArrayInputStream(payload), messageType, senderId, new ObjectId(groupId));
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
}
