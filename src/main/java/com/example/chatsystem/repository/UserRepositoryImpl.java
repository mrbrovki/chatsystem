package com.example.chatsystem.repository;

import com.example.chatsystem.model.User;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    @Override
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }

    @Override
    public Optional<User> findById(ObjectId id) {
        return Optional.ofNullable(mongoTemplate.findById(id, User.class));
    }

    @Override
    public User save(User user) {
        return mongoTemplate.save(user);
    }

    @Override
    public User update(User user) {
        return mongoTemplate.save(user);
    }

    @Override
    public boolean delete(User user) {
        DeleteResult deleteResult = mongoTemplate.remove(user);
        return deleteResult.wasAcknowledged();
    }
}
