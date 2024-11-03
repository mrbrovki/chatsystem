package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Optional;

public interface ReadStatusRepository {

    ReadStatus save(ReadStatus readStatus, String collectionName);

    Optional<ReadStatus> findById(ObjectId userId, String collectionName);

    boolean collectionExists(String collectionName);

    void upsert(ObjectId userId, Map<String, Object> fieldsToUpdate, String collectionName);
}
