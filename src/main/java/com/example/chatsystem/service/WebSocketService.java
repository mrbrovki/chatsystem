package com.example.chatsystem.service;

import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
import org.bson.types.ObjectId;

public interface WebSocketService {

    void sendPrivateMessage(String receiverName, MessageReceiveDTO message);

    void sendGroupMessage(String groupId, MessageReceiveDTO message);

    MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, String senderName);

    void handleMessage(MessageSendDTO messageSendDTO, String senderName);

    void subscribeUserToGroup(String username, ObjectId groupId);

    void unsubscribeUserToGroup(String username, ObjectId groupId);
}
