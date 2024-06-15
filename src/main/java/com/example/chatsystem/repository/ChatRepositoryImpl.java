package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatRepositoryImpl implements ChatRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ChatRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public GroupChat findById(ObjectId id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, GroupChat.class);
    }

    @Override
    public GroupChat save(GroupChat groupChat) {
        return mongoTemplate.save(groupChat);
    }

    @Override
    public void deleteById(ObjectId id) {
        mongoTemplate.remove(findById(id));
    }

    @Override
    public GroupChat update(GroupChat groupChat) {
        return mongoTemplate.save(groupChat);
    }

    @Override
    public List<GroupChat> findAllChats() {
        return mongoTemplate.findAll(GroupChat.class);
    }
}
