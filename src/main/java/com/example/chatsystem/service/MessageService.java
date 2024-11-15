package com.example.chatsystem.service;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.message.MessagesResponse;
import com.example.chatsystem.model.*;
import com.example.chatsystem.security.MyUserDetails;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.util.List;

public interface MessageService{

    List<Message> findAllMessages(String collectionName);

    List<Message> findAfter(String messageId, String collectionName);

    Message findMessageById(String collectionName, ObjectId id);

    Message saveMessage(String collectionName, Message message);

    Message updateMessage(String collectionName, Message message);

    void deleteMessage(String collectionName, ObjectId id);

    void deleteAllMessages(String collectionName);

    void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, String receiverName);

    void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                               ObjectId senderId, ObjectId receiverId);

    void persistBotMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, ObjectId receiverId);




    void persistBotMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                           ObjectId senderId, ObjectId receiverId);

    void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, ObjectId groupId);


    void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                             ObjectId senderId);

    void persistGroupFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, ObjectId senderId, ObjectId groupId);

    void persistBotFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, ObjectId senderId, ObjectId receiverId);

    void persistPrivateFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream,
                            ObjectId senderId, String receiverName);

    boolean collectionExists(String collectionName);

    MessagesResponse getAllMessages(MyUserDetails userDetails);


    List<MessageDTO> getPrivateChatMessages(ObjectId userId, String username, String targetUserName);

    List<MessageDTO> getBotChatMessages(ObjectId userId, String username, String botName);

    List<MessageDTO> getGroupChatMessages(ObjectId userId, GroupChat groupChat);

    S3File findFileById(MyUserDetails userDetails, String chatName,
                        String senderName, ChatType chatType, String fileId);

    void updateLastMessageStatus(ObjectId userId, String collectionName);

    void updatePrivateReadStatus(ObjectId userId, String chatName);

    void updateGroupReadStatus(ObjectId userId, ObjectId groupChatId);

    void updateBotReadStatus(ObjectId userId, String chatName);
}