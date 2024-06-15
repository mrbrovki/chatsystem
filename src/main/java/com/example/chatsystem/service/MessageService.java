package com.example.chatsystem.service;

import com.example.chatsystem.model.Message;
import org.bson.types.ObjectId;

import java.util.List;

public interface MessageService{

    List<Message> findAllMessages(String collectionName);

    Message findMessageById(String collectionName, String id);

    Message saveMessage(String collectionName, Message message);

    Message updateMessage(String collectionName, Message message);

    void deleteMessage(String collectionName, String id);

    void deleteAllMessages(String collectionName);
}
