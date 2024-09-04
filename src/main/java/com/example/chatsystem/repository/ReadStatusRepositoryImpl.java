package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReadStatusRepositoryImpl implements ReadStatusRepository {

    private final MongoTemplate mongoTemplate;

    public ReadStatusRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    @Override
    public List<ReadStatus> saveAll(List<ReadStatus> readStatusList, String collectionName) {
        return mongoTemplate.insert(readStatusList, collectionName).stream().toList();
    }

    @Override
    public ReadStatus save(ReadStatus readStatus, String collectionName) {
        return mongoTemplate.save(readStatus, collectionName);
    }

    @Override
    public ReadStatus findById(String id, String collectionName) {
        return mongoTemplate.findById(id, ReadStatus.class, collectionName);
    }
}
