package com.example.chatsystem.repository;

import com.example.chatsystem.model.Message;


import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    List<Message> findAll(String collectionName);

    List<Message> findAfter(String messageId, String collectionName);

    Optional<Message> findById(String collectionName, String id);

    Message save(String collectionName, Message message);

    boolean collectionExists(String collectionName);

    void delete(String collectionName, Message message);

    Message update(String collectionName, Message message);

    void deleteAll(String collectionName);

    Optional<Message> findLastMessage(String collectionName);
}
