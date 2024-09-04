package com.example.chatsystem.repository;

import com.example.chatsystem.model.ReadStatus;

import java.util.List;

public interface ReadStatusRepository {
    List<ReadStatus> saveAll(List<ReadStatus> readStatusList, String collectionName);

    ReadStatus save(ReadStatus readStatus, String collectionName);

    ReadStatus findById(String id, String collectionName);
}
