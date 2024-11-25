package com.example.chatsystem.service;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.message.MessagesResponse;
import com.example.chatsystem.model.*;
import com.example.chatsystem.security.MyUserDetails;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface MessageService{

    List<Message> findAllMessages(String collectionName);

    List<Message> findAfter(String messageId, String collectionName);

    Message findMessageById(String collectionName, String id);

    Message saveMessage(String collectionName, Message message);

    Message updateMessage(String collectionName, Message message);

    void deleteMessage(String collectionName, String id);

    void deleteAllMessages(String collectionName);

    void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, UUID senderId, UUID receiverId);

    void persistPrivateMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                               UUID senderId, UUID receiverId);

    void persistBotMessage(MessageReceiveDTO messageReceiveDTO, UUID senderId, UUID receiverId);




    void persistBotMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                           UUID senderId, UUID receiverId);

    void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, UUID senderId, UUID groupId);


    void persistGroupMessage(MessageReceiveDTO messageReceiveDTO, String collectionName,
                             UUID senderId);

    void persistGroupFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, UUID senderId, UUID groupId);

    void persistBotFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream, UUID senderId, UUID receiverId);

    void persistPrivateFile(MessageReceiveDTO messageReceiveDTO, InputStream inputStream,
                            UUID senderId, UUID receiverId);

    boolean collectionExists(String collectionName);

    MessagesResponse getAllMessages(MyUserDetails userDetails);


    List<MessageDTO> getPrivateChatMessages(UUID userId, UUID targetUserId);

    List<MessageDTO> getBotChatMessages(UUID userId, UUID botId);

    List<MessageDTO> getGroupChatMessages(UUID userId, GroupChat groupChat);

    S3File findFileById(MyUserDetails userDetails, UUID chatId,
                        UUID senderId, ChatType chatType, String fileId);

    void updateLastMessageStatus(UUID userId, String collectionName);

    void updatePrivateReadStatus(UUID userId, UUID chatId);

    void updateGroupReadStatus(UUID userId, UUID groupId);

    void updateBotReadStatus(UUID userId, UUID botId);
}