package com.example.chatsystem.repository;

import com.example.chatsystem.model.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public User findByUsername(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(username));
        return mongoTemplate.findOne(query, User.class);
    }

    @Override
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }

    @Override
    public User findById(ObjectId id) {
        return mongoTemplate.findById(id, User.class);
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
    public void deleteById(ObjectId id) {
        mongoTemplate.remove(findById(id));
    }

    @Override
    public void deleteByUsername(String username) {
        mongoTemplate.remove(findByUsername(username));
    }
}
