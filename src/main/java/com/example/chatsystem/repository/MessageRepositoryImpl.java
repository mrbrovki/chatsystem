package com.example.chatsystem.repository;

import com.example.chatsystem.model.Message;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepositoryImpl implements MessageRepository{
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MessageRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Message> findAll(String collectionName) {
        return mongoTemplate.findAll(Message.class, collectionName);
    }

    @Override
    public Optional<Message> findById(String collectionName, ObjectId id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Message.class, collectionName));
    }

    @Override
    public Message save(String collectionName, Message message) {
        return mongoTemplate.insert(message, collectionName);
    }

    @Override
    public boolean collectionExists(String collectionName) {
        return mongoTemplate.collectionExists(collectionName);
    }

    @Override
    public void delete(String collectionName, Message message) {
        mongoTemplate.remove(message, collectionName);
    }

    @Override
    public Message update(String collectionName, Message message) {
        return mongoTemplate.save(message, collectionName);
    }

    @Override
    public void deleteAll(String collectionName) {
        mongoTemplate.remove(new Query(), collectionName);
    }
}
