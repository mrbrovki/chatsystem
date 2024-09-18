package com.example.chatsystem.service;

import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import org.bson.types.ObjectId;

public interface WebSocketService {

    void sendPrivateMessage(String receiverName, MessageReceiveDTO message);

    void sendBotMessage(String receiverName, MessageReceiveDTO message);

    void sendGroupMessage(String groupId, MessageReceiveDTO message);

    void handleMessageToBot(MessageSendDTO messageSendDTO, String senderName);

    void handlePrivateMessage(MessageSendDTO messageSendDTO, String senderName);

    void handleGroupMessage(MessageSendDTO messageSendDTO, String senderName);

    void handleFileToBot(byte[] payload, MessageType messageType, String senderName, String receiverName);
    void handleFileToPrivate(byte[] payload, MessageType messageType, String senderName, String receiverName);
    void handleFileToGroup(byte[] payload, MessageType messageType, String senderName, String receiverName);

    void subscribeUserToGroup(String username, ObjectId groupId);

    void unsubscribeUserToGroup(String username, ObjectId groupId);
}
