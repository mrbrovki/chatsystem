package com.example.chatsystem.repository;

import com.example.chatsystem.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepositoryImpl implements MessageRepository{
    private MongoTemplate mongoTemplate;

    @Autowired
    public MessageRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Message> findAllMessages(String collectionName) {
        return mongoTemplate.findAll(Message.class, collectionName);
    }

    @Override
    public Message findMessageById(String collectionName, String id) {
        return mongoTemplate.findById(id, Message.class, collectionName);
    }

    @Override
    public Message saveMessage(String collectionName, Message message) {
        return mongoTemplate.save(message, collectionName);
    }

    @Override
    public void deleteMessage(String collectionName, String id) {
        mongoTemplate.remove(findMessageById(collectionName, id), collectionName);
    }

    @Override
    public Message updateMessage(String collectionName, Message message) {
        return mongoTemplate.save(message, collectionName);
    }

    @Override
    public void deleteAllMessages(String collectionName) {
        mongoTemplate.remove(new Query(), collectionName);
    }
}
