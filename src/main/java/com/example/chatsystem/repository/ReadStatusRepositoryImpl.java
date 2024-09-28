package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
    public Optional<ReadStatus> findById(ObjectId id, String collectionName) {
        return Optional.ofNullable(mongoTemplate.findById(id, ReadStatus.class, collectionName));
    }
}
