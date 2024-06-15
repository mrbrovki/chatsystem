package com.example.chatsystem.repository;

import com.example.chatsystem.model.Message;

import java.util.List;

public interface MessageRepository {
    public List<Message> findAllMessages(String collectionName);

    public Message findMessageById(String collectionName, String id);

    public Message saveMessage(String collectionName, Message message);

    public void deleteMessage(String collectionName, String id);

    public Message updateMessage(String collectionName, Message message);

    public void deleteAllMessages(String collectionName);
}
