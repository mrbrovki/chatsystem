package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;
import org.bson.types.ObjectId;

import java.util.Optional;

public interface ReadStatusRepository {

    ReadStatus save(ReadStatus readStatus, String collectionName);

    Optional<ReadStatus> findById(ObjectId userId, String collectionName);
}
