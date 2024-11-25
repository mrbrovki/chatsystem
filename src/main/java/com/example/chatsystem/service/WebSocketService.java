package com.example.chatsystem.service;

import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.MessageType;


import java.util.UUID;

public interface WebSocketService {

    void sendPrivateMessage(UUID receiverId, MessageReceiveDTO message);

    void sendBotMessage(UUID receiverId, MessageReceiveDTO message);

    void sendGroupMessage(UUID groupId, MessageReceiveDTO message);

    void handleMessageToBot(MessageSendDTO messageSendDTO, UUID senderId);

    void handlePrivateMessage(MessageSendDTO messageSendDTO, UUID senderId);

    void handleGroupMessage(MessageSendDTO messageSendDTO, UUID senderId);

    void handleFileToBot(byte[] payload, MessageType messageType, UUID senderId, UUID receiverId);
    void handleFileToPrivate(byte[] payload, MessageType messageType, UUID senderId, UUID receiverId);
    void handleFileToGroup(byte[] payload, MessageType messageType, UUID senderId, UUID groupId);

    void subscribeUserToGroup(UUID userId, UUID groupId);

    void unsubscribeUserFromGroup(UUID userId, UUID groupId);
}
