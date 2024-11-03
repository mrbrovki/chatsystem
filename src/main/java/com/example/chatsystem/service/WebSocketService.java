package com.example.chatsystem.service;

import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.model.MessageType;
import org.bson.types.ObjectId;

public interface WebSocketService {

    void sendPrivateMessage(String receiverName, MessageReceiveDTO message);

    void sendBotMessage(String receiverName, MessageReceiveDTO message);

    void sendGroupMessage(String groupId, MessageReceiveDTO message);

    void handleMessageToBot(MessageSendDTO messageSendDTO, ObjectId senderId, String senderName);

    void handlePrivateMessage(MessageSendDTO messageSendDTO, ObjectId senderId, String senderName);

    void handleGroupMessage(MessageSendDTO messageSendDTO, ObjectId senderId, String senderName);

    void handleFileToBot(byte[] payload, MessageType messageType, ObjectId senderId, String senderName, String receiverName);
    void handleFileToPrivate(byte[] payload, MessageType messageType, ObjectId senderId, String senderName, String receiverName);
    void handleFileToGroup(byte[] payload, MessageType messageType, ObjectId senderId, String senderName, String receiverName);

    void subscribeUserToGroup(String username, ObjectId groupId);

    void unsubscribeUserToGroup(String username, ObjectId groupId);
}
