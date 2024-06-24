package com.example.chatsystem.repository;

import com.example.chatsystem.model.GroupChat;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChatRepositoryImpl implements ChatRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ChatRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<GroupChat> findById(ObjectId id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, GroupChat.class));
    }

    @Override
    public GroupChat save(GroupChat groupChat) {
        return mongoTemplate.save(groupChat);
    }

    @Override
    public void delete(GroupChat groupChat) {
        mongoTemplate.remove(groupChat);
    }

    @Override
    public GroupChat update(GroupChat groupChat) {
        return mongoTemplate.save(groupChat);
    }

    @Override
    public List<GroupChat> findAll() {
        return mongoTemplate.findAll(GroupChat.class);
    }
}
