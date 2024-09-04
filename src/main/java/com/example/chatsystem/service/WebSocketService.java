package com.example.chatsystem.service;

import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
import org.bson.types.ObjectId;

public interface WebSocketService {

    void sendPrivateMessage(String receiverName, MessageReceiveDTO message);

    void sendGroupMessage(String groupId, MessageReceiveDTO message);

    void handleMessageToBot(MessageSendDTO messageSendDTO, String senderName);

    void handlePrivateMessage(MessageSendDTO messageSendDTO, String senderName);

    void handleGroupMessage(MessageSendDTO messageSendDTO, String senderName);

    void handleImageToBot(byte[] payload, String imageType, String senderName, String receiverName);
    void handleImageToPrivate(byte[] payload, String imageType, String senderName, String receiverName);
    void handleImageToGroup(byte[] payload, String imageType, String senderName, String receiverName);

    void subscribeUserToGroup(String username, ObjectId groupId);

    void unsubscribeUserToGroup(String username, ObjectId groupId);
}
