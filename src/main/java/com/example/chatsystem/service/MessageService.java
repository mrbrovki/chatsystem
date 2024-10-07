package com.example.chatsystem.service;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.message.MessageDTO;
import com.example.chatsystem.dto.websocket.MessageReceiveDTO;
import com.example.chatsystem.dto.websocket.MessageSendDTO;
import com.example.chatsystem.dto.message.MessagesResponse;
import com.example.chatsystem.model.ChatType;
import com.example.chatsystem.model.GroupChat;
import com.example.chatsystem.model.Message;
import com.example.chatsystem.model.MessageType;
import com.example.chatsystem.security.MyUserDetails;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.util.List;

public interface MessageService{

    List<Message> findAllMessages(String collectionName);

    Message findMessageById(String collectionName, ObjectId id);

    Message saveMessage(String collectionName, Message message);

    Message updateMessage(String collectionName, Message message);

    void deleteMessage(String collectionName, ObjectId id);

    void deleteAllMessages(String collectionName);

    void persistPrivateMessage(MessageSendDTO messageSendDTO, MessageReceiveDTO messageReceiveDTO);

    void persistBotMessage(MessageReceiveDTO messageReceiveDTO, ObjectId senderId, ObjectId receiverId);

    void persistGroupMessage(MessageSendDTO messageSendDTO, MessageReceiveDTO messageReceiveDTO);


    void persistGroupFile(InputStream inputStream, MessageType messageType, ObjectId senderId, ObjectId groupId);


    void persistBotFile(InputStream inputStream, MessageType messageType, ObjectId senderId, ObjectId receiverId,
                        ChatType chatType);

    void persistPrivateFile(InputStream inputStream, MessageType messageType, String senderName, String receiverName,
                            ChatType chatType);

    boolean collectionExists(String collectionName);

    MessagesResponse getAllMessages(MyUserDetails userDetails);

    List<MessageDTO> getPrivateChatMessages(MyUserDetails userDetails, String targetUserName);

    List<MessageDTO> getBotChatMessages(MyUserDetails userDetails, String botName);

    List<MessageDTO> getGroupChatMessages(MyUserDetails userDetails, GroupChat groupChat);


    MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, String senderName);

    S3File findFileById(MyUserDetails userDetails, String chatName,
                         String senderName, ChatType chatType, String fileId);

    void updatePrivateReadStatus(ObjectId userId, String chatName);

    void updateGroupReadStatus(ObjectId userId, ObjectId groupChatId);

    void updateBotReadStatus(ObjectId userId, String chatName);
}