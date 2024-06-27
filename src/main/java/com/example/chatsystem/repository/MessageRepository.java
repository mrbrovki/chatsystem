package com.example.chatsystem.repository;

import com.example.chatsystem.model.Message;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    List<Message> findAll(String collectionName);

    Optional<Message> findById(String collectionName, ObjectId id);

    Message save(String collectionName, Message message);

    boolean collectionExists(String collectionName);

    void delete(String collectionName, Message message);

    Message update(String collectionName, Message message);

    void deleteAll(String collectionName);
}
