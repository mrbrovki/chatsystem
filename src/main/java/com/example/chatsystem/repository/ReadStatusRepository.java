package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

    ReadStatus save(ReadStatus readStatus, String collectionName);

    Optional<ReadStatus> findById(UUID userId, String collectionName);

    boolean collectionExists(String collectionName);

    void upsert(UUID userId, Map<String, Object> fieldsToUpdate, String collectionName);
}
