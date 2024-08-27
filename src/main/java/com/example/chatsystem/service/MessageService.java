package com.example.chatsystem.service;

import com.example.chatsystem.config.websocket.aws.S3File;
import com.example.chatsystem.dto.ImageRequestDTO;
import com.example.chatsystem.dto.MessageDTO;
import com.example.chatsystem.dto.MessageReceiveDTO;
import com.example.chatsystem.dto.MessageSendDTO;
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

    void persistMessage(MessageSendDTO messageSendDTO, MessageReceiveDTO messageReceiveDTO, MessageType messageType);

    void persistImage(InputStream inputStream, String imageType, String senderName, String receiverName);

    boolean collectionExists(String collectionName);

    List<MessageDTO> getChatMessages(MyUserDetails userDetails, String targetUserName);

    List<MessageDTO> getGroupChatMessages(MyUserDetails userDetails, GroupChat groupChat);

    MessageReceiveDTO buildMessageReceiveDTO(MessageSendDTO messageSendDTO, String senderName);

    S3File findImageById(MyUserDetails userDetails, ImageRequestDTO imageRequestDTO, String imageId);
}