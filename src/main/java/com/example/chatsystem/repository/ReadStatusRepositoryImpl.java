package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class ReadStatusRepositoryImpl implements ReadStatusRepository {

    private final MongoTemplate mongoTemplate;

    public ReadStatusRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ReadStatus save(ReadStatus readStatus, String collectionName) {
        return mongoTemplate.save(readStatus, collectionName);
    }

    @Override
    public Optional<ReadStatus> findById(UUID userId, String collectionName) {
        return Optional.ofNullable(mongoTemplate.findById(userId, ReadStatus.class, collectionName));
    }

    @Override
    public boolean collectionExists(String collectionName){
        return mongoTemplate.collectionExists(collectionName);
    }

    @Override
    public void upsert(UUID userId, Map<String, Object> fieldsToUpdate, String collectionName){
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        fieldsToUpdate.forEach(update::set);
        mongoTemplate.upsert(query, update, ReadStatus.class, collectionName);
    }
}
