package com.example.chatsystem.service;

import com.example.chatsystem.model.ReadStatus;


import java.util.UUID;

public interface ReadStatusService {
    void persist(UUID userId, String collectionName);

    ReadStatus getReadStatus(String collectionName, UUID userId);

    void updateTimeRead(String collectionName, UUID userId);

    void updateLastMessage(String collectionName, String messageId, UUID userId);
}
