package com.example.chatsystem.service;

import com.example.chatsystem.model.ReadStatus;
import org.bson.types.ObjectId;

public interface ReadStatusService {
    void persist(ObjectId userId, String collectionName);

    ReadStatus getReadStatus(String collectionName, ObjectId userId);

    void updateTimeRead(String collectionName, ObjectId userId);

    void updateLastMessage(String collectionName, String messageId, ObjectId userId);
}
